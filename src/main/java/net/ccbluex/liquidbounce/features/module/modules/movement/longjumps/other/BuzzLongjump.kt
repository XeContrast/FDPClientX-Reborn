package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class Buzz : LongJumpMode("Buzz") {
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY += 0.4679942989799998
        MovementUtils.getSpeed *= 0.7578698f
    }
}