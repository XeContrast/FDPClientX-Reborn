package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.script.api.global.Chat

class IntaveReduceVelocity : VelocityMode("IntaveReduce") {
    @EventTarget
    override fun onAttack(event: AttackEvent) {
        if (mc.thePlayer.hurtTime < 3) return
        if (mc.thePlayer.isSprinting && mc.thePlayer.moveForward != 0f && mc.thePlayer.isSwingInProgress) {
            mc.thePlayer.motionX *= 0.6
            mc.thePlayer.motionZ *= 0.6
            if (velocity.debug.get()) {
                Chat.alert("Motion *= 0.6")
            }
        }
    }
}