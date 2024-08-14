package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.intave

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

class Intave : NoWebMode("Intave") {
    @EventTarget
    override fun onUpdate() {
        val thePlayer = mc.thePlayer ?: return
        if (MovementUtils.isMoving() && thePlayer.moveStrafing == 0.0f) {
            if (thePlayer.onGround) {
                if (mc.thePlayer.ticksExisted % 3 == 0) {
                    strafe(0.734f)
                } else {
                    mc.thePlayer.jump()
                    strafe(0.346f)
                }
            }
        }
    }
}