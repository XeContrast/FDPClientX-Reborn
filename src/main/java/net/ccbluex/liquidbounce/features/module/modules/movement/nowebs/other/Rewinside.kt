package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class Rewinside : NoWebMode("Rewinside") {
    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.jumpMovementFactor = 0.42f

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }
    }
}