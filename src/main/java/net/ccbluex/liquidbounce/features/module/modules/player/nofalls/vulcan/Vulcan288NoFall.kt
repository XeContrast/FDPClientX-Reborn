package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.minecraft.network.play.client.C03PacketPlayer

class Vulcan288NoFall : NoFallMode("Vulcan2.8.8") {
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer.C04PacketPlayerPosition) {
            val fallingPlayer = FallingPlayer(mc.thePlayer)
            if (player.fallDistance in 2.5..50.0) {
                // Checks to prevent fast falling to void.
                if (fallingPlayer.findCollision(500) != null) {
                    packet.onGround = true

                    player.motionZ *= 0.0
                    player.motionX *= 0.0
                    player.motionY *= 0.0
                    player.motionY = -99.887575
                    player.isSneaking = true
                }
            }
        }
    }
}