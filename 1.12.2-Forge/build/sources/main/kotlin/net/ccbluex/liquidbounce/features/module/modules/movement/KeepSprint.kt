package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.ModuleCategory

@ModuleInfo(name = "KeepSprint", description = "Prevents server from stopping you sprinting.", category = ModuleCategory.MOVEMENT)
class KeepSprint : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return

        player.sprinting = true
        player.serverSprintState = true

        mc.netHandler.addToSendQueue(classProvider.createCPacketEntityAction(player, ICPacketEntityAction.WAction.START_SPRINTING))
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if(classProvider.isCPacketEntityAction(packet)) {
            event.cancelEvent()
        }
    }
}