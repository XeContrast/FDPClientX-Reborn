package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixStrafeVelocity : VelocityMode("MatrixClip") {
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime in 1..4 && mc.thePlayer.fallDistance > 0) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}