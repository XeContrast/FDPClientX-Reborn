package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class Spartan : NoWebMode("Spartan") {
    private var usedTimer = false
    @EventTarget
    override fun onUpdate() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
        MovementUtils.strafe(0.27F)
        mc.timer.timerSpeed = 3.7F
        if (!mc.gameSettings.keyBindSneak.isKeyDown) {
            mc.thePlayer.motionY = 0.0
        }
        if (mc.thePlayer.ticksExisted % 2 == 0) {
            mc.timer.timerSpeed = 1.7F
        }
        if (mc.thePlayer.ticksExisted % 40 == 0) {
            mc.timer.timerSpeed = 3F
        }
        usedTimer = true
    }
}