package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.util.ResourceLocation
import java.awt.Color

open class ButtonGui : GuiButton {
    protected var png: ResourceLocation? = null

    protected var color: Color = Color(255, 255, 255, 255)

    protected var `is`: Boolean

    protected var x2: Int = 0
    protected var y2: Int = 0

    constructor(buttonId: Int, x: Int, y: Int, widthIn: Int, heightIn: Int, buttonText: String?) : super(
        buttonId,
        x,
        y,
        buttonText
    ) {
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.width = widthIn
        this.height = heightIn
        this.displayString = buttonText
        this.`is` = false
    }

    constructor(
        buttonId: Int,
        x: Int,
        y: Int,
        widthIn: Int,
        heightIn: Int,
        buttonText: String?,
        image: ResourceLocation?
    ) : super(buttonId, x, y, buttonText) {
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.width = widthIn
        this.height = heightIn
        this.displayString = buttonText
        this.png = image
        this.`is` = false
    }

    constructor(
        buttonId: Int,
        x: Int,
        y: Int,
        widthIn: Int,
        heightIn: Int,
        buttonText: String?,
        image: ResourceLocation?,
        x2: Int,
        y2: Int
    ) : super(buttonId, x, y, buttonText) {
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.width = widthIn
        this.height = heightIn
        this.displayString = buttonText
        this.png = image
        this.x2 = x2
        this.y2 = y2
        this.`is` = true
    }

    constructor(
        buttonId: Int,
        x: Int,
        y: Int,
        widthIn: Int,
        heightIn: Int,
        buttonText: String?,
        x2: Int,
        y2: Int
    ) : super(buttonId, x, y, buttonText) {
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.width = widthIn
        this.height = heightIn
        this.displayString = buttonText
        this.x2 = x2
        this.y2 = y2
        this.`is` = true
    }

    constructor(
        buttonId: Int,
        x: Int,
        y: Int,
        widthIn: Int,
        heightIn: Int,
        buttonText: String?,
        image: ResourceLocation?,
        color: Color
    ) : super(buttonId, x, y, buttonText) {
        this.enabled = true
        this.visible = true
        this.id = buttonId
        this.xPosition = x
        this.yPosition = y
        this.width = widthIn
        this.height = heightIn
        this.displayString = buttonText
        this.png = image
        this.color = color
        this.`is` = false
    }

    fun func_191745_a(p_191745_1_: Minecraft, p_191745_2_: Int, p_191745_3_: Int, p_191745_4_: Float) {
        val fontManager = Fonts.minecraftFont

        if (this.visible) {
            this.hovered =
                (p_191745_2_ >= this.xPosition) && (p_191745_3_ >= this.yPosition) && (p_191745_2_ < this.xPosition + this.width) && (p_191745_3_ < this.yPosition + this.height)

            val hovered2 =
                p_191745_2_ >= 0 && p_191745_3_ >= yPosition && p_191745_2_ < x2 && p_191745_3_ < yPosition + this.y2

            if (hovered2 && `is`) {
                draw(p_191745_1_, p_191745_2_, p_191745_3_, fontManager)
            } else if (!`is`) {
                draw(p_191745_1_, p_191745_2_, p_191745_3_, fontManager)
            }
        }
    }

    private fun draw(p_191745_1_: Minecraft, p_191745_2_: Int, p_191745_3_: Int, fontManager: FontRenderer) {
        if (hovered) {
            RoundedUtil.drawRound(this.xPosition.toFloat(),
                this.yPosition.toFloat(), width.toFloat(), height.toFloat(), 2F, Color(0, 0, 0, 200))

            RoundedUtil.drawRound(
                this.xPosition.toFloat(),
                (this.yPosition + height).toFloat(), width.toFloat(), 2F, 0F, Color(255, 255, 255, 255)
            )
            RoundedUtil.drawRound(this.xPosition.toFloat(),
                (this.yPosition - 1).toFloat(), width.toFloat(), 2F, 0F, Color(255, 255, 255, 255))

            RoundedUtil.drawRound(
                (this.xPosition + width - 1).toFloat(),
                this.yPosition.toFloat(), 2F, height.toFloat(), 0F, Color(255, 255, 255, 255)
            )
            RoundedUtil.drawRound((this.xPosition - 1).toFloat(),
                this.yPosition.toFloat(), 2F, height.toFloat(), 0F, Color(255, 255, 255, 255))
        } else {
            RoundedUtil.drawRound(this.xPosition.toFloat(),
                this.yPosition.toFloat(), width.toFloat(), height.toFloat(), 2F, Color(0, 0, 0, 120))
        }

        RenderUtils.drawImage(this.png,this.xPosition, this.yPosition + 2, 30, 30)

        this.mouseDragged(p_191745_1_, p_191745_2_, p_191745_3_)

        fontManager.drawString(
            this.displayString,
            (this.xPosition + width / 2f - fontManager.getStringWidth(this.displayString) / 2f).toInt(),
            ((this.yPosition + height / 2f - fontManager.FONT_HEIGHT / 2f) + 2).toInt(), Color(255, 255, 255).rgb
        )
    }
}