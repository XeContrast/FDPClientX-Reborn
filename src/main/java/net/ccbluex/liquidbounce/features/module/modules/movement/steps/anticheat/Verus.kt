package net.ccbluex.liquidbounce.features.module.modules.movement.steps.anticheat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.fakeJump
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepX
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepY
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepZ
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.wasTimer
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.minecraft.network.play.client.C03PacketPlayer
import kotlin.math.ceil

class Verus : StepMode("Verus") {
    override fun onStep(event: StepEvent) {
        Step.doncheck = false
        Step.off = false
        if (event.eventState == EventState.PRE) else {
            val rstepHeight = mc.thePlayer.entityBoundingBox.minY - stepY
            mc.timer.timerSpeed = 1f / ceil(rstepHeight * 2.0).toFloat()
            var stpHight = 0.0
            fakeJump()
            repeat ((ceil(rstepHeight * 2.0) - 1.0).toInt()) {
                stpHight += 0.5
                mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(stepX, stepY + stpHight, stepZ, true))
            }
            wasTimer = true
        }
    }
}