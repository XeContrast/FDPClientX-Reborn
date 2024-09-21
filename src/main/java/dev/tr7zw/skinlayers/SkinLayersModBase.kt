package dev.tr7zw.skinlayers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.tr7zw.skinlayers.accessor.PlayerSettings
import dev.tr7zw.skinlayers.config.CustomConfigScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.math.min

abstract class SkinLayersModBase {
    private val settingsFile = File("config", "skinlayers.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun onInitialize() {
        instance = this
        if (settingsFile.exists()) {
            try {
                config = Gson().fromJson(
                    String(Files.readAllBytes(settingsFile.toPath()), StandardCharsets.UTF_8), Config::class.java
                )
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
        if (config == null) {
            config = Config()
            writeConfig()
        }
    }

    fun writeConfig() {
        if (settingsFile.exists()) settingsFile.delete()
        try {
            Files.write(settingsFile.toPath(), gson.toJson(config).toByteArray(StandardCharsets.UTF_8))
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    class ConfigScreen(lastScreen: GuiScreen?) : CustomConfigScreen(lastScreen!!, "text.skinlayers.title") {
        private val minecraft: Minecraft = Minecraft.getMinecraft()

        override fun initialize() {
            val options: MutableList<GuiButton> = ArrayList()
            options.add(
                getOnOffOption("text.skinlayers.enable.hat", { config!!.enableHat },
                    { b: Boolean? -> config!!.enableHat = b!! })
            )
            options.add(
                getOnOffOption("text.skinlayers.enable.jacket", { config!!.enableJacket },
                    { b: Boolean? -> config!!.enableJacket = b!! })
            )
            options.add(
                getOnOffOption("text.skinlayers.enable.leftsleeve", { config!!.enableLeftSleeve },
                    { b: Boolean? -> config!!.enableLeftSleeve = b!! })
            )
            options.add(
                getOnOffOption("text.skinlayers.enable.rightsleeve", { config!!.enableRightSleeve },
                    { b: Boolean? -> config!!.enableRightSleeve = b!! })
            )
            options.add(
                getOnOffOption("text.skinlayers.enable.leftpants", { config!!.enableLeftPants },
                    { b: Boolean? -> config!!.enableLeftPants = b!! })
            )
            options.add(
                getOnOffOption("text.skinlayers.enable.rightpants", { config!!.enableRightPants },
                    { b: Boolean? -> config!!.enableRightPants = b!! })
            )
            options.add(
                getIntOption("text.skinlayers.renderdistancelod", 5f, 40f, { config!!.renderDistanceLOD },
                    { i: Int? -> config!!.renderDistanceLOD = i!! })
            )
            options.add(getDoubleOption("text.skinlayers.basevoxelsize", 1.001f, 1.4f, 0.001f,
                { config!!.baseVoxelSize.toDouble() }, { i: Double ->
                    config!!.baseVoxelSize = i.toFloat()
                    instance!!.refreshLayers(minecraft.thePlayer)
                })
            )
            options.add(getDoubleOption("text.skinlayers.headvoxelsize", 1.001f, 1.25f, 0.001f,
                { config!!.headVoxelSize.toDouble() }, { i: Double ->
                    config!!.headVoxelSize = i.toFloat()
                    instance!!.refreshLayers(minecraft.thePlayer)
                })
            )
            options.add(getDoubleOption("text.skinlayers.bodyvoxelwidthsize", 1.001f, 1.4f, 0.001f,
                { config!!.bodyVoxelWidthSize.toDouble() }, { i: Double ->
                    config!!.bodyVoxelWidthSize = i.toFloat()
                    instance!!.refreshLayers(minecraft.thePlayer)
                })
            )
            options.add(
                getOnOffOption("text.skinlayers.skulls.enable", { config!!.enableSkulls },
                    { b: Boolean? -> config!!.enableSkulls = b!! })
            )
            options.add(
                getOnOffOption("text.skinlayers.skullsitems.enable", { config!!.enableSkullsItems },
                    { b: Boolean? -> config!!.enableSkullsItems = b!! })
            )
            options.add(getDoubleOption("text.skinlayers.skulls.voxelsize", 1.001f, 1.2f, 0.001f,
                { config!!.skullVoxelSize.toDouble() }, { i: Double -> config!!.skullVoxelSize = i.toFloat() })
            )
            options.add(
                getOnOffOption("text.skinlayers.fastrender.enable", { config!!.fastRender },
                    { b: Boolean? -> config!!.fastRender = b!! })
            )
            addOptionsList(options)
        }

        override fun save() {
            instance!!.writeConfig()
        }

        override fun drawScreen(p_drawScreen_1_: Int, p_drawScreen_2_: Int, p_drawScreen_3_: Float) {
            super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_)
            if (minecraft.theWorld != null) {
                val x = this.width / 2
                val y = this.height - 45
                val size = (40f * (this.height / 200f)).toInt()
                val lookX = x - p_drawScreen_1_
                var lookY = y - 80 - p_drawScreen_2_
                // Prevent the model from clipping into the back of the gui^^
                lookY = min(lookY.toDouble(), 10.0).toInt()
                GlStateManager.enableDepth()
                GuiInventory.drawEntityOnScreen(
                    x, y, size, lookX.toFloat(), lookY.toFloat(),
                    minecraft.thePlayer
                )
            }
        }
    }

    fun refreshLayers(player: EntityPlayer?) {
        if (player !is PlayerSettings) return
        val settings = player as PlayerSettings
        settings.setupSkinLayers(null)
        settings.setupHeadLayers(null)
    }

    companion object {
        var instance: SkinLayersModBase? = null
        val LOGGER: Logger = LogManager.getLogger()
        @JvmField
        var config: Config? = null
    }
}
