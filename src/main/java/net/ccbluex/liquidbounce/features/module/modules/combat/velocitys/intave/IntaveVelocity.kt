package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion

class IntaveVelocity : VelocityMode("Intave") {
    private var tick = 0
    private var vel = false

    override fun onDisable() {
        tick = 0
        vel = false
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        tick++
        if (vel && mc.thePlayer.hurtTime == 2) {
            if (mc.thePlayer.onGround && tick % 2 == 0) {
                if (velocity.debug.get()) {
                    Chat.alert("Jump")
                }
                mc.thePlayer.jump()
                tick = 0
            }
            vel = false
        }
    }

    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return
        val packet = event.packet

        if ((packet is S12PacketEntityVelocity && thePlayer.entityId == packet.entityID && packet.motionY > 0 && (packet.motionX != 0 || packet.motionZ != 0))
            || (packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                    && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0))) {
            vel = true
        }
    }
}