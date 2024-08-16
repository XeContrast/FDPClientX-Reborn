/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.fonts.impl.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

class MainMenuButton {
    private var icon: String
    var text: String
    var action: Executor?
    var x: Float = 0f
    var y: Float = 0f
    private var yAnimation: Float = 0.0f

    constructor(icon: String, text: String, action: Executor?) {
        this.icon = icon
        this.text = text
        this.action = action
    }

    constructor(icon: String, text: String, action: Executor?, yOffset: Float) {
        this.icon = icon
        this.text = text
        this.action = action
    }

    fun draw(x: Float, y: Float, mouseX: Int, mouseY: Int) {
        this.x = x
        this.y = y
        RenderUtils.drawRoundedCornerRect(x - 30f, y - 30f, x + 30f, y + 30f, 15f, Color(0, 0, 0, 40).rgb)
        this.yAnimation = RenderUtils.smoothAnimation(
            this.yAnimation,
            if (RenderUtils.isHovering(
                    mouseX,
                    mouseY,
                    this.x - 30f,
                    this.y - 30f,
                    this.x + 30.0f,
                    this.y + 30.0f
                )
            ) 4.0f else 0.0f,
            20.0f,
            0.3f
        )
        Fonts.MAINMENU.MAINMENU30.MAINMENU30.drawString(
            this.icon, x - Fonts.MAINMENU.MAINMENU30.MAINMENU30.stringWidth(
                this.icon
            ).toFloat() / 2.0f, y - 6f + (this.yAnimation * -1f), Color.WHITE.rgb, false
        )
        if (this.yAnimation >= 0.11) {
            Fonts.SF.SF_16.SF_16.drawString(
                this.text,
                x - Fonts.SF.SF_16.SF_16.stringWidth(
                    this.text
                ).toFloat() / 2.0f,
                y + 12f + (this.yAnimation * -1f),
                Color(
                    255,
                    255,
                    255,
                    if (((((this.yAnimation / 4.0f)) * 254.0f * 1f) <= 255.0f)) (((this.yAnimation / 4.0f)) * 254.0f + 1f).toInt() else 25
                ).rgb
            )
        } //RenderUtils.drawGradientRect(x, y + 40.0F - this.yAnimation * 3.0F, x + 50.0F, y + 40.0F, 3453695, 2016719615);

        RenderUtils.drawRoundedCornerRect(x - 30f, y - 30f, x + 30f, y + 30f, 15f, Color(255, 255, 255, 50).rgb)
    }

    fun mouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (RenderUtils.isHovering(
                mouseX,
                mouseY,
                x - 30f,
                y - 30f,
                x + 30.0f,
                y + 30.0f
            ) && action != null && mouseButton == 0
        ) {
            action!!.execute()
        }
    }

    fun interface Executor {
        fun execute()
    }
}
