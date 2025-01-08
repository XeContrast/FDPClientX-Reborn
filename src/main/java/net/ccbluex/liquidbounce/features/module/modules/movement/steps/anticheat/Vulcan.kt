package net.ccbluex.liquidbounce.features.module.modules.movement.steps.anticheat

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

class Vulcan : StepMode("Vulcan") {
    override fun onStep(event: StepEvent) {
        if (event.eventState == EventState.PRE) {} else {
            val rstepHeight = mc.thePlayer.entityBoundingBox.minY - stepY
            fakeJump()
            when {
                rstepHeight > 2.0 -> {
                    val stpPacket = arrayOf(0.5, 1.0, 1.5, 2.0)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, true))
                    }
                }

                rstepHeight <= 2.0 && rstepHeight > 1.5 -> {
                    val stpPacket = arrayOf(0.5, 1.0, 1.5)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, true))
                    }
                }

                rstepHeight <= 1.5 && rstepHeight > 1.0 -> {
                    val stpPacket = arrayOf(0.5, 1.0)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, true))
                    }
                }

                rstepHeight <= 1.0 && rstepHeight > 0.6 -> {
                    val stpPacket = arrayOf(0.5)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, true))
                    }
                }
            }
            timer.reset()
        }
    }
}