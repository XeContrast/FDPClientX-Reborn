package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.hypixel

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.network.play.client.C03PacketPlayer

class HypixelTimerNoFall : NoFallMode("Hypixel") {
    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        val fallingPlayer = FallingPlayer(mc.thePlayer)

        if (packet is C03PacketPlayer) {
            if (fallingPlayer.findCollision(500) != null && player.fallDistance - player.motionY >= 3.3) {
                mc.timer.timerSpeed = 0.5f

                packet.onGround = true
                player.fallDistance = 0f

                WaitTickUtils.scheduleTicks(1) {
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }
}