package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vulcan

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S32PacketConfirmTransaction

private var tran = false
class VulcanVelocity : VelocityMode("Vulcan") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S32PacketConfirmTransaction) {
            event.cancelEvent()
            mc.thePlayer.sendQueue.addToSendQueue(C0FPacketConfirmTransaction(if (tran) 1 else -1,if (tran) -1 else 1,false))
            tran = !tran
        }
        if (packet is S12PacketEntityVelocity) {
            event.cancelEvent()
        }
    }
}
