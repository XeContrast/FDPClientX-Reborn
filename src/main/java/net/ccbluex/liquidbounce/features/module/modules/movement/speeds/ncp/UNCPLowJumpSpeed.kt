package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

class UNCPLowJumpSpeed : SpeedMode("UNCPLowJump") {

    private var airtick = 0
    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        if (player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (MovementUtils.isMoving()) {
            if (player.onGround) {
                player.jump()
                airtick = 0
            } else {
                if (airtick == 4) {
                    player.motionY = -0.09800000190734863
                }

                airtick++
            }

            strafe()
        }
    }
}