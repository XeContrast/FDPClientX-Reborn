package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.hypixel

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

class HypixelJumpNoFall : NoFallMode("HypixelJump") {
    private var jump = false
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer.C04PacketPlayerPosition) {
            if (player.fallDistance >= 3.3) {
                jump = true
            }

            if (jump && player.onGround) {
                packet.onGround = false

                if (!mc.gameSettings.keyBindJump.isKeyDown) {
                    player.setPosition(packet.positionX, packet.positionY + 0.09, packet.positionZ)
                }

                jump = false
            }
        }
    }
}