package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class GrimSmallVelocity : VelocityMode("GrimSmall") {

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer
        if (thePlayer.hurtTime > 0){
            thePlayer.motionX += -1.0E-7
            thePlayer.motionZ += -1.0E-7
        }
        if (thePlayer.hurtTime > 5){
            thePlayer.motionX += -1.1E-7
            thePlayer.motionZ += -1.2E-7
        }
    }
}