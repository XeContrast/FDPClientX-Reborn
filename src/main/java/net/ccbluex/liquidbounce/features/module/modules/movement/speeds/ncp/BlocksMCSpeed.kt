/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

class BlocksMCSpeed : SpeedMode("BlocksMC") {
    private val strafe = BoolValue("DamageStrafe",true)

    override fun onPreMotion() {
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.fallDistance > 0) {
                mc.thePlayer.motionY -= 0.004f
            }
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.4
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtils.strafe(0.48f * (1.0f + 0.13f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)))
                } else {
                    MovementUtils.strafe(0.49f)
                }
            } else {
                mc.thePlayer.motionY += 0.23 * 0.03
                MovementUtils.strafe()
            }
            if (strafe.get()) {
                if (mc.thePlayer.hurtTime > 5) {
                    MovementUtils.strafe(0.39f)
                }
            }
        }
    }
}