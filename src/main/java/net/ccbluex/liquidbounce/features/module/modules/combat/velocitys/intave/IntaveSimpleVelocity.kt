package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.script.api.global.Chat

class IntaveSimpleVelocity : VelocityMode("IntaveSimple") {
    private var sprintvel = FloatValue("SprintingHorizontal",0.6f,0f,1f)
    private var vel = FloatValue("NoSprintHorizontal",0.6f,0f,1f)

    @EventTarget
    override fun onAttack(event: AttackEvent) {
        if (mc.thePlayer.hurtTime > 0) {
            if (mc.thePlayer.isSprinting) {
                if (velocity.debug.get()) {
                    Chat.alert("Spring - Motion *= " + sprintvel.get().toString())
                }
                mc.thePlayer.motionZ *= sprintvel.get()
                mc.thePlayer.motionX *= sprintvel.get()
            } else {
                if (velocity.debug.get()) {
                    Chat.alert("Walking - Motion *= " + vel.get().toString())
                }
                mc.thePlayer.motionZ *= vel.get()
                mc.thePlayer.motionX *= vel.get()
            }
        }
    }
}