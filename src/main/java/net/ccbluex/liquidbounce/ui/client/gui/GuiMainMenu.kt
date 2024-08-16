/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.utils.MainMenuButton
import net.ccbluex.liquidbounce.utils.render.ParticleUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import java.awt.Color
import java.io.IOException

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    val butt = ArrayList<Any?>()
    private var currentX = 0f
    private var currentY = 0f
    private var res: ScaledResolution? = null

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
        this.res = ScaledResolution(this.mc)
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        try {
            this.drawGradientRect(0, 0, this.width, this.height, 16777215, 16777215)
            val h = this.height
            val w = this.width
            val xDiff = ((mouseX - h / 2).toFloat() - this.currentX) / res!!.scaleFactor.toFloat()
            val yDiff = ((mouseY - w / 2).toFloat() - this.currentY) / res!!.scaleFactor.toFloat()
            this.currentX += xDiff * 0.3f
            this.currentY += yDiff * 0.3f
            GlStateManager.translate(this.currentX / 30.0f, this.currentY / 15.0f, 0.0f)
            RenderUtils.drawImage(
                ResourceLocation("fdpclient/japanesebackground.png"), -30, -30,
                res!!.scaledWidth + 60,
                res!!.scaledHeight + 60
            )
            GlStateManager.translate(-this.currentX / 30.0f, -this.currentY / 15.0f, 0.0f)
            RenderUtils.drawRoundedCornerRect(
                width.toFloat() / 2.0f - (80.0f * (butt.size.toFloat() / 2.0f)) - 3f,
                height.toFloat() / 2.0f - 100.0f - 3f,
                width.toFloat() / 2.0f + (80.0f * (butt.size.toFloat() / 2.0f)) + 3f,
                height.toFloat() / 2.0f + 103.0f, 10f, Color(0, 0, 0, 80).rgb
            )
            FontLoaders.F18.drawCenteredString(
                "Made by SkidderMC with love.",
                (width.toFloat() / 2.0f).toDouble(),
                (height.toFloat() / 2.0f + 70.0f).toDouble(),
                Color(255, 255, 255, 255).rgb
            )
            //BlurUtils.INSTANCE.draw(0, 0, mc.displayWidth, mc.displayHeight, 30f);
            FontLoaders.F40.drawCenteredString(
                "FDPCLIENTX-REBORN",
                (width.toFloat() / 2.0f).toDouble(),
                (height.toFloat() / 2.0f - 70.0f).toDouble(),
                Color(255, 255, 255).rgb
            )
            //BlurUtils.INSTANCE.draw(0, 0, mc.displayWidth, mc.displayHeight, 10f);
            ParticleUtils.drawParticles(mouseX, mouseY)
            RenderUtils.drawRoundedCornerRect(
                width.toFloat() / 2.0f - 80.0f * (butt.size.toFloat() / 2.0f),
                height.toFloat() / 2.0f - 100.0f,
                width.toFloat() / 2.0f + 80.0f * (butt.size.toFloat() / 2.0f),
                height.toFloat() / 2.0f + 100.0f, 10f, Color(0, 0, 0, 100).rgb
            )
            //RenderUtils.drawRect((float)this.width / 2.0F - 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F + 20.0F, (float)this.width / 2.0F + 50.0F * ((float)this.butt.size() / 2.0F), (float)this.height / 2.0F + 50.0F, 1040187392);
            var startX = width.toFloat() / 2.0f - 64.5f * (butt.size.toFloat() / 2.0f)

            val var9: Iterator<*> = butt.iterator()
            while (var9.hasNext()) {
                val button = var9.next() as MainMenuButton
                button.draw(startX, height.toFloat() / 2.0f + 20.0f, mouseX, mouseY)
                startX += 75.0f
            }
            FontLoaders.F40.drawCenteredString(
                "FDPCLIENTX-REBORN",
                (width.toFloat() / 2.0f).toDouble(),
                (height.toFloat() / 2.0f - 70.0f).toDouble(),
                Color(255, 255, 255).rgb
            )
            FontLoaders.F18.drawCenteredString(
                FDPClient.CLIENT_VERSION,
                (width.toFloat() / 2.0f).toDouble(),
                (height.toFloat() / 2.0f - 30.0f).toDouble(),
                Color(255, 255, 255).rgb
            )
            RenderUtils.drawRect(
                width.toFloat() / 2.0f - 30f,
                height.toFloat() / 2.0f - 40.0f,
                width.toFloat() / 2.0f + 30f,
                height.toFloat() / 2.0f - 39.5f, Color(255, 255, 255, 100).rgb
            )
            FontLoaders.F18.drawCenteredString(
                "Made by SkidderMC with love.",
                (width.toFloat() / 2.0f).toDouble(),
                (height.toFloat() / 2.0f + 70.0f).toDouble(),
                Color(255, 255, 255, 100).rgb
            )
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
