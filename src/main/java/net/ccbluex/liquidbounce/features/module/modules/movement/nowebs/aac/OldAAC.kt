package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.aac

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class OldAAC : NoWebMode("OldAAC") {
    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.jumpMovementFactor = 0.59f

        if (!mc.gameSettings.keyBindSneak.isKeyDown) {
            mc.thePlayer.motionY = 0.0
        }
    }
}