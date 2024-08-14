package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class KarhuSpeed : SpeedMode("Karhu") {

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                if (mc.gameSettings.keyBindForward.isKeyDown && !mc.gameSettings.keyBindRight.isKeyDown && !mc.gameSettings.keyBindLeft.isKeyDown) {
                    MovementUtils.strafe()
                }
            }
            if (mc.thePlayer.fallDistance > 0) {
                if (MovementUtils.getSpeed() < 0.125f) {
                    MovementUtils.strafe(0.125f)
                }
            }
        }
    }
}