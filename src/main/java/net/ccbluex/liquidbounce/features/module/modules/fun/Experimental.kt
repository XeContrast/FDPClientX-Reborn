package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "Experimental", description = "idk", category = ModuleCategory.FUN)
class Experimental : Module() {

    val devSpeedValue = FloatValue("Speed", 1.6f, 0.2f, 9f)
    val devTimer = FloatValue("Timer", 1f, 0.1f, 2f)
    val devYValue = FloatValue("Y", 0f, 0f, 4f)
    val devStrafeValue = BoolValue("Strafe", true)
    val devResetXZValue = BoolValue("ResetXZ", false)
    val devResetYValue = BoolValue("ResetY", false)

    @EventTarget
    fun onMotion(event: MotionEvent) {
        run {
            if (MovementUtils.isMoving) {
                mc.timer.timerSpeed = devTimer.get()
                when {
                    mc.thePlayer!!.onGround -> {
                        MovementUtils.strafe(devSpeedValue.get())
                        mc.thePlayer!!.motionY = devYValue.get().toDouble()
                    }
                    devStrafeValue.get() -> MovementUtils.strafe(devSpeedValue.get())
                    else -> MovementUtils.strafe()
                }
            } else {
                mc.thePlayer!!.motionZ = 0.0
                mc.thePlayer!!.motionX = mc.thePlayer!!.motionZ
            }
        }
    }

    override fun onEnable() {
        run {
            if (devResetXZValue.get()) {
                mc.thePlayer!!.motionZ = 0.0
                mc.thePlayer!!.motionX = mc.thePlayer!!.motionZ
            }
            if (devResetYValue.get()) mc.thePlayer!!.motionY = 0.0
        }
        super.onEnable()
    }

    override val tag: String
        get() = "Dev"
}