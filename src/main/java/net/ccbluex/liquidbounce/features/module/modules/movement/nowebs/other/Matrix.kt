package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class Matrix : NoWebMode("Matrix") {
    private var usedTimer = false
    @EventTarget
    override fun onUpdate() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
        mc.thePlayer.jumpMovementFactor = 0.12425f
        mc.thePlayer.motionY = -0.0125
        if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY = -0.1625

        if (mc.thePlayer.ticksExisted % 40 == 0) {
            mc.timer.timerSpeed = 3.0F
            usedTimer = true
        }
    }
}