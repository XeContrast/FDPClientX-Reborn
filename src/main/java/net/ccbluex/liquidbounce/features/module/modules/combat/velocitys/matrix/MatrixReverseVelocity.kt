package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixReverseVelocity : VelocityMode("MatrixReverse") {
    private var flag = false
    override fun onEnable() {
        flag = false
    }

    @EventTarget
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
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
}