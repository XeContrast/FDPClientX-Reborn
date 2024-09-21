package dev.tr7zw.skinlayers.config

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n
import java.util.function.Consumer
import java.util.function.Supplier

class GuiEnumButton<T : Enum<*>?>(
    private val translationKey: String,
    private val enumTranslationKey: String,
    private val targetEnum: Class<T>,
    private val current: Supplier<T>,
    private val update: Consumer<T>
) : GuiButton(0, 0, 0, 150, 20, "") {
    override fun drawButton(p_drawButton_1_: Minecraft, p_drawButton_2_: Int, p_drawButton_3_: Int) {
        this.displayString = (I18n.format(translationKey) + ": "
                + I18n.format(enumTranslationKey + "." + current.get()!!.name))
        super.drawButton(p_drawButton_1_, p_drawButton_2_, p_drawButton_3_)
    }

    override fun mousePressed(p_mousePressed_1_: Minecraft, p_mousePressed_2_: Int, p_mousePressed_3_: Int): Boolean {
        if (super.mousePressed(p_mousePressed_1_, p_mousePressed_2_, p_mousePressed_3_)) {
            update.accept(
                targetEnum.enumConstants[(current.get()!!.ordinal + 1)
                        % targetEnum.enumConstants.size]
            )
            return true
        }
        return false
    }
}