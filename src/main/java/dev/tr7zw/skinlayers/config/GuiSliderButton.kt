package dev.tr7zw.skinlayers.config

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.util.MathHelper
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.text.DecimalFormat
import java.util.function.Consumer
import java.util.function.Supplier

@SideOnly(Side.CLIENT)
class GuiSliderButton(
    private val translationKey: String, private val min: Float, private val max: Float, private val steps: Float,
    private val current: Supplier<Double>, private val update: Consumer<Double>
) : GuiButton(0, 0, 0, 150, 20, "") {
    private var sliderValue: Float
    private var dragging: Boolean = false


    init {
        this.sliderValue = ((current.get() - min) / (max - min)).toFloat()
        this.displayString = I18n.format(translationKey) + ": " + getRounded(
            current.get()
        )
    }

    override fun getHoverState(p_getHoverState_1_: Boolean): Int {
        return 0
    }

    override fun mouseDragged(p_mouseDragged_1_: Minecraft, p_mouseDragged_2_: Int, p_mouseDragged_3_: Int) {
        if (!this.visible) return
        if (this.dragging) {
            this.sliderValue = (p_mouseDragged_2_.toFloat() - xPosition.toFloat() + 4f) / (width.toFloat() - 8f)
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0f, 1.0f)
            var lvt_4_1_ = min + (sliderValue * (max - min))
            lvt_4_1_ = (lvt_4_1_ / steps).toInt().toFloat()
            lvt_4_1_ *= steps
            update.accept(lvt_4_1_.toDouble())
            this.sliderValue = ((current.get() - min) / (max - min)).toFloat()
            this.displayString = I18n.format(translationKey) + ": " + getRounded(
                current.get()
            )
        }
        p_mouseDragged_1_.textureManager.bindTexture(buttonTextures)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        drawTexturedModalRect(
            this.xPosition + (this.sliderValue * (this.width - 8)).toInt(), this.yPosition, 0, 66, 4,
            20
        )
        drawTexturedModalRect(
            this.xPosition + ((this.sliderValue * (this.width - 8)).toInt()) + 4, this.yPosition, 196, 66,
            4, 20
        )
    }

    override fun mousePressed(p_mousePressed_1_: Minecraft, p_mousePressed_2_: Int, p_mousePressed_3_: Int): Boolean {
        if (super.mousePressed(p_mousePressed_1_, p_mousePressed_2_, p_mousePressed_3_)) {
            this.sliderValue = (p_mousePressed_2_.toFloat() - xPosition.toFloat() + 4f) / (width.toFloat() - 8f)
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0f, 1.0f)
            val lvt_4_1_ = min + (sliderValue * (max - min))
            update.accept(lvt_4_1_.toDouble())
            this.sliderValue = (max / current.get()).toFloat()
            this.displayString = I18n.format(translationKey) + ": " + getRounded(
                current.get()
            )
            this.dragging = true
            return true
        }
        return false
    }

    override fun mouseReleased(p_mouseReleased_1_: Int, p_mouseReleased_2_: Int) {
        this.dragging = false
    }

    private fun getRounded(d: Double): String {
        val f = DecimalFormat("##.00")
        return f.format(d)
    }
}