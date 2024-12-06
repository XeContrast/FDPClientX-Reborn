/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.IOException

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private val butt = ArrayList<Any?>()
    private var currentX = 0f
    private var currentY = 0f
    private var res: ScaledResolution? = null

    override fun initGui() {
        val j = this.height / 4
        buttonList.run {
            add(
                ButtonGui(
                    0,
                    0,
                    0,
                    0,
                    0,
                    I18n.format(""),
                    null
                )
            )
            add(
                ButtonGui(
                    1,
                    20,
                    j,
                    200,
                    34,
                    I18n.format("menu.singleplayer"),
                    ResourceLocation("assets/minecraft/fdpclient/mainmenu/1.png")
                )
            )
            add(
                ButtonGui(
                    2,
                    20,
                    j + 44,
                    200,
                    34,
                    I18n.format("menu.multiplayer"),
                    ResourceLocation("fdpclient/mainmenu/2.png")
                )
            )
            add(
                ButtonGui(
                    0,
                    20,
                    j + 44 * 2,
                    200,
                    34,
                    I18n.format("menu.options"),
                    ResourceLocation("fdpclient/mainmenu/5.png")
                )
            )
            add(
                ButtonGui(
                    4,
                    20,
                    j + 44 * 4,
                    200,
                    34,
                    I18n.format("menu.quit"),
                    ResourceLocation("fdpclient/mainmenu/4.png")
                )
            )
            add(
                ButtonGui(
                    7,
                    20,
                    j + 44 * 3,
                    200,
                    34,
                    "AltManager",
                    ResourceLocation("fdpclient/mainmenu/8.png")
                )
            )
        }
        this.res = ScaledResolution(this.mc)
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawGradientRect(0, 0, this.width, this.height, 16777215, 16777215)
        GlStateManager.translate(this.currentX / 30.0f, this.currentY / 15.0f, 0.0f)
        val scaledWidth = res!!.scaledWidth
        val scaledHeight = res!!.scaledHeight
        RenderUtils.drawImage(
            ResourceLocation("fdpclient/japanesebackground.png"), -30, -30,
            scaledWidth + 60,
            scaledHeight + 60
        )
        val j = this.height / 4
        Fonts.fontBold180.drawCenteredString(
            CLIENT_NAME,
            width / 2F,
            height / 8F,
            Color.WHITE.rgb,
            true
        )
        RenderUtils.drawQuads(
            floatArrayOf(scaledWidth * 0.57f - scaledWidth * 0.11f, 0f),
            floatArrayOf(scaledWidth * 0.43f - scaledWidth * 0.11f, scaledHeight.toFloat()),
            floatArrayOf(scaledWidth * 0.57f, 0f),
            floatArrayOf(scaledWidth * 0.43f, scaledHeight.toFloat()),
            Color(0, 0, 0, 150),
            Color(0, 0, 0, 130)
        )
        RenderUtils.drawQuads(
            floatArrayOf(scaledWidth * 0.57f - scaledWidth * 0.22f, 0f),
            floatArrayOf(scaledWidth * 0.43f - scaledWidth * 0.22f, scaledHeight.toFloat()),
            floatArrayOf(scaledWidth * 0.57f - scaledWidth * 0.11f, 0f),
            floatArrayOf(scaledWidth * 0.43f - scaledWidth * 0.11f, scaledHeight.toFloat()),
            Color(0, 0, 0, 200),
            Color(0, 0, 0, 180)
        )
        RenderUtils.drawQuads(
            floatArrayOf(0f, 0f),
            floatArrayOf(0f, height.toFloat()),
            floatArrayOf(scaledWidth * 0.57f - scaledWidth * 0.22f, 0f),
            floatArrayOf(scaledWidth * 0.43f - scaledWidth * 0.22f, scaledHeight.toFloat()),
            Color(0, 0, 0, 220),
            Color(0, 0, 0, 210)
        )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    public override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
        }

        if (button.id == 1) {
            mc.displayGuiScreen(GuiSelectWorld(this))
        }

        if (button.id == 2) {
            mc.displayGuiScreen(GuiMultiplayer(this))
        }

        if (button.id == 4) {
            mc.shutdown()
        }
        if (button.id == 5) {
            mc.displayGuiScreen(GuiLanguage(this, mc.gameSettings, mc.languageManager))
        }
        if (button.id == 7) {
            mc.displayGuiScreen(GuiAltManager(this))
        }
        super.actionPerformed(button)
    }
}
