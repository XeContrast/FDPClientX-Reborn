package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.minecraft.util.BlockPos

@ModuleInfo("NoWeb", category = ModuleCategory.MOVEMENT)
class NoWeb : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.nowebs", NoWebMode::class.java)
        .map { it.newInstance() as NoWebMode }
        .sortedBy { it.modeName }

    private val mode: NoWebMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "OldAAC") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.isInWeb) return
        mode.onUpdate()
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    @EventTarget
    fun onBlockPos(pos: BlockPos) {
        mode.onBlockPos(pos)
    }

    override val tag: String
        get() = modeValue.get()

    override val values = super.values.toMutableList().also {
        modes.map { mode ->
            mode.values.forEach { value ->
                //it.add(value.displayable { modeValue.equals(mode.modeName) })
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}