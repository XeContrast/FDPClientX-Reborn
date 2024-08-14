package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils

class Horizon : NoWebMode("Horizon") {
    private val horizonSpeed = FloatValue("HorizonSpeed", 0.1F, 0.01F, 0.8F)

    @EventTarget
    override fun onUpdate() {
        if (mc.thePlayer.onGround) {
            MovementUtils.strafe(horizonSpeed.get())
        }
    }
}