package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils


class HypixelNewHop : SpeedMode("NewHypixelHop") {

    @EventTarget
    override fun onUpdate() {
        val player = mc.thePlayer
        if (player.onGround && MovementUtils.isMoving) {
            if (player.isUsingItem) {
                player.jump()
            } else {
                player.jump()
                MovementUtils.strafe(0.48f)
            }
        }
    }
}