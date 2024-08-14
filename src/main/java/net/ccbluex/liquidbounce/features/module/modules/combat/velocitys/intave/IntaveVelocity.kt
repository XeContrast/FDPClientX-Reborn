package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.script.api.global.Chat
import kotlin.math.cos
import kotlin.math.sin

class IntaveVelocity : VelocityMode("IntaveReveres") {
    private val reverse = BoolValue("Reveres", false)
    private val yreuce = FloatValue("ReduceY", 0.05f, 0f, 0.5f)

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (mc.objectMouseOver == null) {
            return
        }
        if (mc.thePlayer.hurtTime <= 6 && mc.thePlayer.isSwingInProgress && mc.thePlayer.hurtTime > 0) {
            if (reverse.get() && !mc.thePlayer.onGround) {
                if (velocity.debug.get()) {
                    Chat.alert("Reverse")
                }
                mc.thePlayer.motionX = -sin(Math.toRadians(mc.thePlayer.rotationYaw.toDouble())) * 0.019999999552965164
                mc.thePlayer.motionZ = cos(Math.toRadians(mc.thePlayer.rotationYaw.toDouble())) * 0.019999999552965164
            }
            val var10000 = mc.thePlayer
            var10000.motionY *= 1.0 - yreuce.get()
            if (velocity.debug.get()) {
                Chat.alert("MotionY *= " + (1 - yreuce.get()).toString())
            }
        }
    }
}