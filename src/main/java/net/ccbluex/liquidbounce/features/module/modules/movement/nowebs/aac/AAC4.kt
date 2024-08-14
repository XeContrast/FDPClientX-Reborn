package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.aac

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class AAC4 : NoWebMode("AAC4") {
    @EventTarget
    override fun onUpdate() {
        mc.timer.timerSpeed = 0.99F
        mc.thePlayer.jumpMovementFactor = 0.02958f
        mc.thePlayer.motionY -= 0.00775
        if (mc.thePlayer.onGround) {
            // mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.4050
            mc.timer.timerSpeed = 1.35F
        }
    }

    @EventTarget
    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }
}