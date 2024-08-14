package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.aac

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class LAAC : NoWebMode("LAAC") {

    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.jumpMovementFactor = if (mc.thePlayer.movementInput.moveStrafe != 0f) 1.0f else 1.21f

        if (!mc.gameSettings.keyBindSneak.isKeyDown) {
            mc.thePlayer.motionY = 0.0
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }
    }
}