/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue

@ModuleInfo("KeepSprint", category = ModuleCategory.COMBAT)
object KeepSprint : Module() {
    private val motionAfterAttackOnGround by FloatValue("MotionAfterAttackOnGround", 0.6f, 0.0f, 1f)
    private val motionAfterAttackInAir by FloatValue("MotionAfterAttackInAir", 0.6f, 0.0f, 1f)
    private val whenHurt by BoolValue("WhenHurt", false)
    private val motionWhenHurt by FloatValue("MotionWhenHurt", 0.6f, 0.0f, 1.0f) { whenHurt }

    val motionAfterAttack
        get() = if (mc.thePlayer.hurtTime > 0) {
            if (whenHurt) {
                motionWhenHurt
            } else {
                if (mc.thePlayer.onGround) {
                    motionAfterAttackOnGround
                } else {
                    motionAfterAttackInAir
                }
            }
        } else {
            if (mc.thePlayer.onGround) {
                motionAfterAttackOnGround
            } else {
                motionAfterAttackInAir
            }
        }
}