package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.intave

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class IntaveTest : NoWebMode("IntaveTest") {
    @EventTarget
    override fun onUpdate() {
        if (mc.thePlayer.movementInput.moveStrafe == 0.0F && mc.gameSettings.keyBindForward.isKeyDown && mc.thePlayer.isCollidedVertically) {
            mc.thePlayer.jumpMovementFactor = 0.74F
        } else {
            mc.thePlayer.jumpMovementFactor = 0.2F
            mc.thePlayer.onGround = true
        }
    }
}