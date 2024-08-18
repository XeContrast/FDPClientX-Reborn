package net.ccbluex.liquidbounce.features.module.modules.movement.steps.anticheat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.canStep
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.fakeJump
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepX
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepY
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepZ
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.timer
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.minecraft.network.play.client.C03PacketPlayer

class AAC440 : StepMode("AAC4.4.0") {
    override fun onUpdate(event: UpdateEvent) {
        Step.off = false
        Step.doncheck = false
    }

    override fun onStep(event: StepEvent) {
        if (event.eventState == EventState.PRE) {
            if (event.stepHeight > 0.6F && !canStep) return
            if (event.stepHeight <= 0.6F) return
        } else {
            val rstepHeight = mc.thePlayer.entityBoundingBox.minY - stepY
            fakeJump()
            timer.reset()
            when {
                rstepHeight >= 1.0 - 0.015625 && rstepHeight < 1.5 - 0.015625 -> {
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 0.4, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 0.7, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 0.9, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.0, stepZ, true))
                }
                rstepHeight >= 1.5 - 0.015625 && rstepHeight < 2.0 - 0.015625 -> {
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 0.42, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 0.7718, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.0556, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.2714, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.412, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.50, stepZ, true))
                }
                rstepHeight >= 2.0 - 0.015625 -> {
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 0.45, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 0.84375, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.18125, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.4625, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.6875, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.85625, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX,
                        stepY + 1.96875, stepZ, false))
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            stepX + mc.thePlayer.motionX * 0.5,
                        stepY + 2.0000, stepZ + mc.thePlayer.motionZ * 0.5, true))
                }
            }
        }
    }
}