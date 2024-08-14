/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue

@ModuleInfo("KeepSprint", category = ModuleCategory.COMBAT)
object KeepSprint : Module() {
    private val motionAfterAttackOnGround = FloatValue("MotionAfterAttackOnGround", 0.6f, 0.0f,1f)
    private val motionAfterAttackInAir = FloatValue("MotionAfterAttackInAir", 0.6f, 0.0f,1f)

    val motionAfterAttack
        get() = if (mc.thePlayer.onGround) motionAfterAttackOnGround else motionAfterAttackInAir
}