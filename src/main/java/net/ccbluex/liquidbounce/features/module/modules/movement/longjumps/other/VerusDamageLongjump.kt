package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.LongJump
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer

class VerusDamageLongjump : LongJumpMode("VerusDamage") {

    private val autoDisable = BoolValue("AutoDisable",true)
    var damaged = false

    override fun onEnable() {
        val player = mc.thePlayer ?: return
        if (!MovementUtils.isMoving) {
            Chat.alert("Pls move while toggling LongJump. Using AutoJump option is recommended.")
            return
        }
        PacketUtils.sendPacket(C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 3.0001, player.posZ, false),false)
        PacketUtils.sendPacket(
            C03PacketPlayer.C06PacketPlayerPosLook(
                player.posX,
                player.posY,
                player.posZ,
                player.rotationYaw,
                player.rotationPitch,
                false
            ),
            false
        )
        PacketUtils.sendPacket(
            C03PacketPlayer.C06PacketPlayerPosLook(
                player.posX,
                player.posY,
                player.posZ,
                player.rotationYaw,
                player.rotationPitch,
                true
            ),
            false
        )
        damaged = true
    }
    override fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        if (player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) {
            LongJump.state = false
            return
        }

        /**
         * You can long jump up to 13-14+ blocks
         */
        if (damaged && MovementUtils.isMoving) {
            player.jumpMovementFactor = 0.15f
            player.motionY += 0.015f

            // player onGround checks will not work due to sendPacket ground, so for temporary. I'll be using player motionY.
            if (autoDisable.get() && player.motionY <= -0.4330104027478734) {
                player.motionZ *= 0.0
                player.motionX *= 0.0
                player.motionY *= 0.0
                LongJump.state = false
            }
        } else if (autoDisable.get()) {
            LongJump.state = false
        }
    }
}