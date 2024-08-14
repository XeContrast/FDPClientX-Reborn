package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity

class IntaveFullVelocity : VelocityMode("IntaveFull") {

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime > 7) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.motionX = 0.0
        }
    }

    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer.hurtTime > 7) {
            if (packet is C03PacketPlayer.C06PacketPlayerPosLook) {
                event.cancelEvent()
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C05PacketPlayerLook(
                        packet.getYaw(),
                        packet.getPitch(),
                        packet.isOnGround
                    )
                )
            } else if (packet is C03PacketPlayer.C04PacketPlayerPosition) {
                event.cancelEvent()
                mc.netHandler.addToSendQueue(C03PacketPlayer(packet.isOnGround))
            }
        }
    }
}
