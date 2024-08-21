package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.other

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalMode

class GrimCritical : CriticalMode("Grim") {
    override fun onAttack(event: AttackEvent) {
        if (!mc.thePlayer.onGround) {
            critical.sendCriticalPacket(yOffset = -0.00001, ground = false)
        }
    }
}