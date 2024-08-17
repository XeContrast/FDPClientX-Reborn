/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.button

import net.ccbluex.liquidbounce.font.CFontRenderer
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

open class FlatButtonRenderer : GuiButton {
    private var time: MSTimer
    var displayString = String
    var id = Int
    var enabled = Boolean
    var visible = Boolean
    protected var hovered: Boolean = false
    private var color: Int
    private var opacity = 0f
    private var font: CFontRenderer
    var round: Boolean = false
    private var rad: Int = 0

    constructor(buttonId: Int, x: Int, y: Int, widthIn: Int, heightIn: Int, buttonText: String?, color: Int) : super(
        buttonId,
        x,
        y,
        10,
        12,
        buttonText
    ) {
        this.time = MSTimer()
        this.width = widthIn
        this.height = heightIn
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.displayString = buttonText
        this.color = color
        this.font = FontLoaders.F18
    }

    constructor(buttonId: Int, x: Int, y: Int, scale: Int, buttonText: String?, color: Int, round: Boolean) : super(
        buttonId,
        x,
        y,
        10,
        12,
        buttonText
    ) {
        this.time = MSTimer()
        this.width = scale
        this.height = scale
        this.rad = scale
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.displayString = buttonText
        this.color = color
        this.round = round
        this.font = FontLoaders.F18
    }

    constructor(
        buttonId: Int,
        x: Int,
        y: Int,
        scale: Int,
        buttonText: String?,
        color: Int,
        round: Boolean,
        font: CFontRenderer
    ) : super(buttonId, x, y, 10, 12, buttonText) {
        this.time = MSTimer()
        this.width = scale
        this.height = scale
        this.rad = scale
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.displayString = buttonText
        this.color = color
        this.round = round
        this.font = font
    }

    constructor(
        buttonId: Int,
        x: Int,
        y: Int,
        widthIn: Int,
        heightIn: Int,
        buttonText: String?,
        color: Int,
        font: CFontRenderer
    ) : super(buttonId, x, y, 10, 12, buttonText) {
        this.time = MSTimer()
        this.width = widthIn
        this.height = heightIn
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.displayString = buttonText
        this.color = color
        this.font = font
    }

    override fun getHoverState(mouseOver: Boolean): Int {
        var i = 1
        if (!this.enabled) {
            i = 0
        } else if (mouseOver) {
            i = 2
        }
        return i
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (this.visible) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            this.hovered =
                (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height)
            val var5 = this.getHoverState(this.hovered)
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            GlStateManager.blendFunc(770, 771)
            if (!this.hovered) {
                time.reset()
                this.opacity = 0.0f
            }
            if (this.hovered) {
                this.opacity += 0.5f
                if (this.opacity > 1.0f) {
                    this.opacity = 1.0f
                }
            }
            val radius = this.height / 2.0f
            if (this.round) {
                RenderUtils.drawFilledCircle(xPosition, yPosition, rad.toFloat(), this.color)
            } else {
                RenderUtils.drawRoundedCornerRect(
                    (this.xPosition - this.opacity * 0.1f).toInt()
                        .toFloat(), this.yPosition - this.opacity,
                    (this.xPosition + this.width + (this.opacity * 0.1f)).toInt()
                        .toFloat(), this.yPosition + (radius * 2.0f) + this.opacity, 3f, this.color
                )
            }
            GL11.glColor3f(2.55f, 2.55f, 2.55f)
            this.mouseDragged(mc, mouseX, mouseY)
            GL11.glPushMatrix()
            GL11.glPushAttrib(1048575)
            GL11.glScaled(1.0, 1.0, 1.0)
            val var6 = true
            val var7 = font.height.toFloat()
            font.drawCenteredString(
                this.displayString,
                ((this.xPosition + this.width / 2).toFloat() + 2).toDouble(),
                (this.yPosition + ((this.height - font.height) / 2.0f) + 6f).toDouble(),
                Color(255, 255, 255).rgb
            )
            GL11.glPopAttrib()
            GL11.glPopMatrix()
        }
    }

    private fun darkerColor(c: Color, step: Int): Color {
        val red = c.red
        val blue = c.blue
        val green = c.green
        if (red >= step) {
        }
        if (blue >= step) {
        }
        if (green >= step) {
        }
        return c.darker()
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int) {
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return this.enabled && this.visible && (mouseX >= this.xPosition) && (mouseY >= this.yPosition) && (mouseX < this.xPosition + this.width) && (mouseY < this.yPosition + this.height)
    }

    override fun isMouseOver(): Boolean {
        return this.hovered
    }

    override fun drawButtonForegroundLayer(mouseX: Int, mouseY: Int) {
    }

    override fun playPressSound(soundHandlerIn: SoundHandler) {
        soundHandlerIn.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
    }

    override fun getButtonWidth(): Int {
        return this.width
    }

    override fun setWidth(width: Int) {
        this.width = width
    }
}
