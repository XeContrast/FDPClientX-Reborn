package net.ccbluex.liquidbounce.features.module.modules.movement.steps.anticheat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.canStep
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.fakeJump
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.heightValue
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.lastOnGround
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepX
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepY
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepZ
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.timer
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.minecraft.network.play.client.C03PacketPlayer

class NCPNew : StepMode("NCPNew") {
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround && lastOnGround) {
            mc.thePlayer.stepHeight = heightValue.get()
        }
    }

    override fun onStep(event: StepEvent) {
        if (event.eventState == EventState.PRE) {
            if (event.stepHeight > 0.6F && !canStep) return
            if (event.stepHeight <= 0.6F) return
        } else {
            val rstepHeight = mc.thePlayer.entityBoundingBox.minY - stepY
            fakeJump()
            when {
                rstepHeight > 2.019 -> {
                    val stpPacket = arrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.919)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }

                rstepHeight <= 2.019 && rstepHeight > 1.869 -> {
                    val stpPacket = arrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }

                rstepHeight <= 1.869 && rstepHeight > 1.5 -> {
                    val stpPacket = arrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }

                rstepHeight <= 1.5 && rstepHeight > 1.015 -> {
                    val stpPacket = arrayOf(0.42, 0.7532, 1.01, 1.093, 1.015)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                }

                rstepHeight <= 1.015 && rstepHeight > 0.875 -> {
                    val stpPacket = arrayOf(0.41999998688698, 0.7531999805212)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                }

                rstepHeight <= 0.875 && rstepHeight > 0.6 -> {
                    val stpPacket = arrayOf(0.39, 0.6938)
                    stpPacket.forEach {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                stepX,
                            stepY + it, stepZ, false))
                    }
                }
            }
            timer.reset()
        }
    }
}