package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.intave

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils

class IntaveStrafe : SpeedMode("IntaveStrafe") {
    private val intaveBoostTest = BoolValue("IntaveBoostTest", false)
    private var offGroundTicks = 0

    override fun onMotion(event: MotionEvent) {
        if(!mc.thePlayer.onGround)
            offGroundTicks++
        else offGroundTicks = 0

        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }

        if (offGroundTicks >= 10) {
            MovementUtils.setMoveSpeed(MovementUtils.getSpeed().toDouble())
        }

        if(intaveBoostTest.get()) {
            if (mc.thePlayer.motionY > 0.003) {
                mc.thePlayer.motionX *= 1.0015
                mc.thePlayer.motionZ *= 1.0015
                mc.timer.timerSpeed = 1.1f
            }
        }
    }
}