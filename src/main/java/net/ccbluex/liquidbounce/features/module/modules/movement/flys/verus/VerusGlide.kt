package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe

class VerusGlide : FlyMode("VerusGlide") {

    override fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        if (!player.onGround && player.fallDistance > 1) {
            player.motionY = -0.09800000190734863
            strafe(0.3345f)
        }
    }
}