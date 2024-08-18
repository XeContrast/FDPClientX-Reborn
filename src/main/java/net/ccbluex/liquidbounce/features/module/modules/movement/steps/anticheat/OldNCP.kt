package net.ccbluex.liquidbounce.features.module.modules.movement.steps.anticheat

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.isStep
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.minecraft.network.play.client.C03PacketPlayer

class OldNCP : StepMode("OldNPC") {
    override fun onPacket(event: PacketEvent) {
        Step.off = true
        Step.doncheck = true
        val packet = event.packet

        if (packet is C03PacketPlayer && isStep) {
            packet.y += 0.07
            isStep = false
        }
    }
}