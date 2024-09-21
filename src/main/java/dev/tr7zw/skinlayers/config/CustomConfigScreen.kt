package dev.tr7zw.skinlayers.config

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import java.io.IOException
import java.util.function.Consumer
import java.util.function.Supplier

abstract class CustomConfigScreen(private val lastScreen: GuiScreen, private var screenTitle: String) :
    GuiScreen() {
    private val buttonActions: MutableMap<GuiButton, Runnable> = HashMap()
    private var optionsRowList: GuiButtonRowList? = null

    override fun initGui() {
        this.screenTitle = I18n.format(screenTitle)
        buttonList.clear()
        buttonActions.clear()
        addButton(GuiButton(200, this.width / 2 - 100, this.height - 27, I18n.format("gui.done"))) { this.onClose() }

        initialize()
    }

    fun addOptionsList(options: List<GuiButton>) {
        this.optionsRowList = GuiButtonRowList(
            this.mc, this.width, this.height, 32, this.height - 32, 25,
            options
        )
    }

    private fun addButton(button: GuiButton, action: Runnable) {
        buttonList.add(button)
        buttonActions[button] = action
    }

    @Throws(IOException::class)
    override fun actionPerformed(p_actionPerformed_1_: GuiButton) {
        if (!p_actionPerformed_1_.enabled) return
        buttonActions[p_actionPerformed_1_]!!.run()
    }

    private fun onClose() {
        save()
        mc.displayGuiScreen(this.lastScreen)
    }

    abstract fun initialize()

    abstract fun save()

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        optionsRowList!!.handleMouseInput()
    }

    @Throws(IOException::class)
    override fun mouseClicked(p_mouseClicked_1_: Int, p_mouseClicked_2_: Int, p_mouseClicked_3_: Int) {
        super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_2_, p_mouseClicked_3_)
        optionsRowList!!.mouseClicked(p_mouseClicked_1_, p_mouseClicked_2_, p_mouseClicked_3_)
    }

    override fun mouseReleased(p_mouseReleased_1_: Int, p_mouseReleased_2_: Int, p_mouseReleased_3_: Int) {
        super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_2_, p_mouseReleased_3_)
        optionsRowList!!.mouseReleased(p_mouseReleased_1_, p_mouseReleased_2_, p_mouseReleased_3_)
    }

    override fun drawScreen(p_drawScreen_1_: Int, p_drawScreen_2_: Int, p_drawScreen_3_: Float) {
        drawDefaultBackground()
        optionsRowList!!.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_)
        drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 5, 16777215)
        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_)
    }

    fun getDoubleOption(
        translationKey: String?, min: Float, max: Float, steps: Float,
        current: Supplier<Double>, update: Consumer<Double>
    ): GuiButton {
        return GuiSliderButton(translationKey!!, min, max, steps, current, update)
    }

    fun getIntOption(
        translationKey: String?, min: Float, max: Float, current: Supplier<Int>,
        update: Consumer<Int?>
    ): GuiButton {
        return GuiSliderButton(
            translationKey!!,
            min,
            max,
            1f,
            { current.get().toDouble() },
            { d: Double -> update.accept(d.toInt()) })
    }

    fun getOnOffOption(translationKey: String?, current: Supplier<Boolean>, update: Consumer<Boolean?>): GuiButton {
        return GuiEnumButton(
            translationKey!!,
            "text.skinlayers.boolean",
            OnOff::class.java,
            { if (current.get()) OnOff.ON else OnOff.OFF },
            { e: OnOff -> update.accept(e == OnOff.ON) })
    }

    enum class OnOff {
        ON, OFF
    }
}