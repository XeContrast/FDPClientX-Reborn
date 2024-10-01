/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.utils.MainMenuButton
import net.ccbluex.liquidbounce.utils.render.*
import net.minecraft.client.gui.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import org.bytedeco.javacv.FFmpegFrameGrabber
import java.awt.Color
import java.io.IOException


class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private val butt = ArrayList<Any?>()
    private var currentX = 0f
    private var currentY = 0f
    private var res: ScaledResolution? = null
    private val videoPlayer = VideoPlayer()

    init {
        try {
            videoPlayer.init(ResourceLocation("fdpclient/video/bg.mp4"))
        } catch (e: FFmpegFrameGrabber.Exception) {
            throw RuntimeException(e)
        }
    }

    override fun initGui() {
        butt.clear()
        butt.add(MainMenuButton("G", "SinglePlayer") { mc.displayGuiScreen(GuiSelectWorld(this)) })
        butt.add(MainMenuButton("H", "MultiPlayer") { mc.displayGuiScreen(GuiMultiplayer(this)) })
        butt.add(MainMenuButton("I", "AltManager") { mc.displayGuiScreen(GuiAltManager(this)) })
        butt.add(MainMenuButton("J", "Mods", { mc.displayGuiScreen(GuiModList(this)) }, 0.5f))
        butt.add(MainMenuButton("K", "Options") {
            mc.displayGuiScreen(
                GuiOptions(
                    this,
                    mc.gameSettings
                )
            )
        })
        butt.add(MainMenuButton("L", "Languages") {
            mc.displayGuiScreen(
                GuiLanguage(
                    this,
                    mc.gameSettings,
                    mc.languageManager
                )
            )
        })
        butt.add(MainMenuButton("M", "Quit") { mc.shutdown() })
        if (videoPlayer.paused.get()) {
            videoPlayer.paused.set(false)
        }
        this.res = ScaledResolution(this.mc)
        super.initGui()
    }

    override fun onGuiClosed() {
        // 暂停抓取
        videoPlayer.paused.set(true)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        try {
            this.drawGradientRect(0, 0, this.width, this.height, 16777215, 16777215)
            val xDiff = ((mouseX - height / 2).toFloat() - this.currentX) / res!!.scaleFactor.toFloat()
            val yDiff = ((mouseY - width / 2).toFloat() - this.currentY) / res!!.scaleFactor.toFloat()
            this.currentX += xDiff * 0.3f
            this.currentY += yDiff * 0.3f
            videoPlayer.render(0,0,width,height)

            ParticleUtils.drawParticles(mouseX, mouseY)

            FontLoaders.F40.drawCenteredString(
                "FDP",
                (width.toFloat() / 2.0f).toDouble(),
                (height.toFloat() / 2.0f - 70.0f).toDouble(),
                Color(255, 255, 255).rgb
            )

            var startX = width.toFloat() / 2.0f - 64.5f * (butt.size.toFloat() / 2.0f)
            val var9: Iterator<*> = butt.iterator()
            while (var9.hasNext()) {
                val button = var9.next() as MainMenuButton
                button.draw(startX, height.toFloat() / 2.0f + 20.0f, mouseX, mouseY)
                startX += 75.0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (o in this.butt) {
            val button = o as MainMenuButton
            button.mouseClick(mouseX, mouseY, mouseButton)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun updateScreen() {
        this.res = ScaledResolution(this.mc)
        super.updateScreen()
    }
}
