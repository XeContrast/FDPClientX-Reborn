package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixVelocity : VelocityMode("Matrix") {
    private val mode = ListValue("Mode", arrayOf(
        "Ground",
        "Reduce",
        "Reverse",
        "Simple",
        "Spoof",
        "Clip"
    ),"Ground")
    private var flag = false
    private var isMatrixOnGround = false

    override fun onEnable() {
        isMatrixOnGround = false
        flag = false
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        when (mode.get().lowercase()) {
            "ground" -> isMatrixOnGround = mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown
            "clip" -> {
                if (mc.thePlayer.hurtTime in 1..4 && mc.thePlayer.fallDistance > 0) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "reduce" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    if (mc.thePlayer.onGround) {
                        if (mc.thePlayer.hurtTime <= 6) {
                            mc.thePlayer.motionX *= 0.70
                            mc.thePlayer.motionZ *= 0.70
                        }
                        if (mc.thePlayer.hurtTime <= 5) {
                            mc.thePlayer.motionX *= 0.80
                            mc.thePlayer.motionZ *= 0.80
                        }
                    } else if (mc.thePlayer.hurtTime <= 10) {
                        mc.thePlayer.motionX *= 0.60
                        mc.thePlayer.motionZ *= 0.60
                    }
                }
            }
        }
    }

    @EventTarget
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        when (mode.get().lowercase()) {
            "spoof" -> {
                if(packet is S12PacketEntityVelocity) {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + packet.motionX / - 24000.0, mc.thePlayer.posY + packet.motionY / -24000.0, mc.thePlayer.posZ + packet.motionZ / 8000.0, false))
                }
            }
            "simple" -> {
                if(packet is S12PacketEntityVelocity) {
                    packet.motionX = (packet.getMotionX() * 0.36).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                    if (mc.thePlayer.onGround) {
                        packet.motionX = (packet.getMotionX() * 0.9).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0.9).toInt()
                    }
                }
            }
            "reverse" -> {
                if (packet is S12PacketEntityVelocity) {
                    if (packet.entityID == mc.thePlayer.entityId) {
                        if (!flag) {
                            event.cancelEvent()
                            flag = true
                        } else {
                            flag = false
                            packet.motionX = (packet.getMotionX() * -0.1).toInt()
                            packet.motionZ = (packet.getMotionZ() * -0.1).toInt()
                        }
                    }
                }
            }
            "ground" -> {
                if(packet is S12PacketEntityVelocity) {
                    packet.motionX = (packet.getMotionX() * 0.36).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                    if (isMatrixOnGround) {
                        packet.motionY = (-628.7).toInt()
                        packet.motionX = (packet.getMotionX() * 0.6).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                        mc.thePlayer.onGround = false
                    }
                }
            }
        }
    }
}