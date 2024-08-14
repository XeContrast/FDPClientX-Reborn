package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.aac

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class AAC5 : NoWebMode("AAC5") {

    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.jumpMovementFactor = 0.42f

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }
    }
}