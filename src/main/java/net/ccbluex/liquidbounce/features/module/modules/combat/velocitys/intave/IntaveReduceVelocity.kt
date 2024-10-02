package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.script.api.global.Chat

class IntaveReduceVelocity : VelocityMode("IntaveReduce") {
    private val reduceAmount = FloatValue("ReduceAmount", 0.8f, 0.3f, 1f)

    @EventTarget
    override fun onAttack(event: AttackEvent) {
        if (mc.thePlayer.hurtTime < 3) return
        if (mc.thePlayer.isSprinting && mc.thePlayer.moveForward != 0f && mc.thePlayer.isSwingInProgress) {
            mc.thePlayer.motionX *= reduceAmount.get().toDouble()
            mc.thePlayer.motionZ *= reduceAmount.get().toDouble()
            if (velocity.debug.get()) {
                Chat.alert("Motion *=" + reduceAmount.get())
            }
        }
    }
}