package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.polar

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.minecraft.network.play.server.S12PacketEntityVelocity

class PolarReduceVelocity : VelocityMode("PolarReduce") {
    private var ms = 0
    private var ticks = 0
    private var start = false
    override fun onDisable() {
        ticks = 0
        ms = 0
        start = false
    }

    @EventTarget
    override fun onVelocityPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S12PacketEntityVelocity && KillAura.currentTarget != null) {
            if (packet.entityID == mc.thePlayer.entityId && !start) {
                if (velocity.debug.get()) Chat.alert("Start = True")
                start = true
                ms = 0
            }
        }
    }

    @EventTarget
    override fun onTick(event: TickEvent) {
        if (mc.thePlayer != null && mc.theWorld != null) {
            ms ++
        }
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (start) {
            if (ticks >= 20) {
                start = false
                ticks = 0
                if (velocity.debug.get()) Chat.alert("Start = False")
            }
            if (mc.thePlayer.motionY <= -0.1) {
                ticks ++
                if (ticks % 2 == 0) {
                    mc.thePlayer.motionY = -0.1
                    if (velocity.debug.get()) Chat.alert("MotionY = -0.1")
                    mc.thePlayer.jumpMovementFactor = 0.0265f
                } else {
                    mc.thePlayer.motionY = -0.16
                    if (velocity.debug.get()) Chat.alert("MotionY = -0.16")
                    mc.thePlayer.jumpMovementFactor = 0.0265f
                }
            } else {
                ticks = 0
            }
        }
    }
}