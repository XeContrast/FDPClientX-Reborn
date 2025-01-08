package net.ccbluex.liquidbounce.features.module.modules.movement.steps.server

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.fakeJump
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepX
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepY
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepZ
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.timer
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.minecraft.network.play.client.C03PacketPlayer

class Rewinside : StepMode("Rewinside") {
    override fun onStep(event: StepEvent) {
        if (event.eventState == EventState.PRE) {} else {
            fakeJump()

            // Vanilla step (3 packets) [COULD TRIGGER TOO MANY PACKETS]
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    stepX,
                stepY + 0.41999998688698, stepZ, false))
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    stepX,
                stepY + 0.7531999805212, stepZ, false))
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    stepX,
                stepY + 1.001335979112147, stepZ, false))

            // Reset timer
            timer.reset()
        }
    }
}