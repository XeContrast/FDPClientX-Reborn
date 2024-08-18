package net.ccbluex.liquidbounce.features.module.modules.movement.steps.anticheat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.fakeJump
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepX
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepY
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepZ
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.timer
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.wasTimer
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.minecraft.network.play.client.C03PacketPlayer

class Matrix670 : StepMode("Matrix6.7.0") {
    override fun onUpdate(event: UpdateEvent) {
        Step.off = false
        Step.doncheck = false
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround && Step.lastOnGround) {
            mc.thePlayer.stepHeight = Step.heightValue.get()
        }
    }

    override fun onStep(event: StepEvent) {
        if (event.eventState == EventState.PRE) {
            if (event.stepHeight > 0.6F && !Step.canStep) return
            if (event.stepHeight <= 0.6F) return
        } else {
            val rstepHeight = mc.thePlayer.entityBoundingBox.minY - stepY
            fakeJump()
            when {
                rstepHeight <= 3.0042 && rstepHeight > 2.95 -> {
                    val stpPacket = arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032, 2.23371, 2.35453, 2.40423)
                    stpPacket.forEach {
                        if(it in 0.9..1.01) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, true))
                        }else {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, false))
                        }
                    }
                    mc.timer.timerSpeed = 0.11f
                    wasTimer = true
                }

                rstepHeight <= 2.95 && rstepHeight > 2.83 -> {
                    val stpPacket = arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032, 2.23371, 2.35453)
                    stpPacket.forEach {
                        if(it in 0.9..1.01) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, true))
                        }else {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, false))
                        }
                    }
                    mc.timer.timerSpeed = 0.12f
                    wasTimer = true
                }

                rstepHeight <= 2.83 && rstepHeight > 2.64 -> {
                    val stpPacket = arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032, 2.23371)
                    stpPacket.forEach {
                        if(it in 0.9..1.01) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, true))
                        }else {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, false))
                        }
                    }
                    mc.timer.timerSpeed = 0.13f
                    wasTimer = true
                }

                rstepHeight <= 2.64 && rstepHeight > 2.37 -> {
                    val stpPacket = arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289, 2.04032)
                    stpPacket.forEach {
                        if(it in 0.9..1.01) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, true))
                        }else {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, false))
                        }
                    }
                    mc.timer.timerSpeed = 0.14f
                    wasTimer = true
                }

                rstepHeight <= 2.37 && rstepHeight > 2.02 -> {
                    val stpPacket = arrayOf(0.41951, 0.75223, 0.99990, 1.42989, 1.77289)
                    stpPacket.forEach {
                        if(it in 0.9..1.01) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, true))
                        }else {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, false))
                        }
                    }
                    mc.timer.timerSpeed = 0.16f
                    wasTimer = true
                }

                rstepHeight <= 2.02 && rstepHeight > 1.77 -> {
                    val stpPacket = arrayOf(0.41951, 0.75223, 0.99990, 1.42989)
                    stpPacket.forEach {
                        if(it in (0.9..1.01)) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, true))
                        }else {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, false))
                        }
                    }
                    mc.timer.timerSpeed = 0.21f
                    wasTimer = true
                }

                rstepHeight <= 1.77 && rstepHeight > 1.6 -> {
                    val stpPacket = arrayOf(0.41999998688698, 0.7531999805212, 1.17319996740818)
                    stpPacket.forEach {
                        if(it in (0.753..0.754)) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, true))
                        }else {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    stepX,
                                stepY + it, stepZ, false))
                        }
                    }
                    mc.timer.timerSpeed = 0.28f
                    wasTimer = true
                }

                rstepHeight <= 1.6 && rstepHeight > 1.3525 -> {
                    val stpPacket = arrayOf(0.41999998688698, 0.7531999805212, 1.001335979112147)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                    mc.timer.timerSpeed = 0.28f
                    wasTimer = true
                }

                rstepHeight <= 1.3525 && rstepHeight > 1.02 -> {
                    val stpPacket = arrayOf(0.41999998688698, 0.7531999805212)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                    mc.timer.timerSpeed = 0.34f
                    wasTimer = true
                }

                rstepHeight <= 1.02 && rstepHeight > 0.6 -> {
                    val stpPacket = arrayOf(0.41999998688698)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                    mc.timer.timerSpeed = 0.5f
                    wasTimer = true
                }
            }
            timer.reset()
        }
    }
}