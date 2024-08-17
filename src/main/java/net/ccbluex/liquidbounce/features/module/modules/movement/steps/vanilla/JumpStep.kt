package net.ccbluex.liquidbounce.features.module.modules.movement.steps.vanilla

import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.StepTest
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.ccbluex.liquidbounce.features.value.FloatValue

class JumpStep : StepMode("Jump") {
    private val jumpHeightValue = FloatValue("JumpMotion", 0.42F, 0.37F, 0.42F)
    override fun onUpdate(event: UpdateEvent) {
        StepTest.off = true
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround
            && !mc.gameSettings.keyBindJump.isKeyDown) {
            StepTest.fakeJump()
            mc.thePlayer.motionY = jumpHeightValue.get().toDouble()
        }
    }
}