package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue

@ModuleInfo("NoDelay", category = ModuleCategory.PLAYER)
object NoDelay : Module() {
    private val noClickDelay = BoolValue("NoClickDelay",true)
    val noJumpDelay = BoolValue("NoJumpDelay",true)

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer != null && mc.theWorld != null && noClickDelay.get()) {
            mc.leftClickCounter = 0
        }
    }
}