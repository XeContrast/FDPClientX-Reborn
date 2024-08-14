package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class Test : NoWebMode("Test") {

    @EventTarget
    override fun onUpdate() {
        if (mc.thePlayer.ticksExisted % 7 == 0) {
            mc.thePlayer.jumpMovementFactor = 0.42f
        }
        if (mc.thePlayer.ticksExisted % 7 == 1) {
            mc.thePlayer.jumpMovementFactor = 0.33f
        }
        if (mc.thePlayer.ticksExisted % 7 == 2) {
            mc.thePlayer.jumpMovementFactor = 0.08f
        }
    }
}