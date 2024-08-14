package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.server.S12PacketEntityVelocity

class LegitSmartVelocity : VelocityMode("LegitSmart") {
    var jumped = 0
    var input = false

    override fun onEnable() {
        jumped = 0
        input = false
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround && mc.thePlayer.hurtTime == 9 && mc.thePlayer.isSprinting && mc.currentScreen == null) {
            if (jumped > 2) {
                jumped = 0
            } else {
                ++jumped
                if (mc.thePlayer.ticksExisted % 5 != 0) mc.gameSettings.keyBindJump.pressed = true
            }
        } else if (mc.thePlayer.hurtTime == 8) {
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            input = false
        }
    }

    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            if ((mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer)
                return
            if (packet.motionX * packet.motionX + packet.motionZ * packet.motionZ + packet.motionY * packet.motionY > 640000) {
                input =
                    true
            }
        }
    }
}