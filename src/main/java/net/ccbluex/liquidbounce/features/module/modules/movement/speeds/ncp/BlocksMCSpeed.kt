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

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtils.strafe(0.48f * (1.0f + 0.13f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)))
                } else {
                    MovementUtils.strafe(0.39f)
                }
            } else {
                if (MovementUtils.getSpeed() < 0.25f) {
                    MovementUtils.strafe(0.25f)
                }
            }
            if (strafe.get()) {
                if (mc.thePlayer.hurtTime > 0) {
                    MovementUtils.strafe()
                }
            }
        }
    }
}