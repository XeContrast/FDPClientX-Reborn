package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.vanilla

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class None : NoWebMode("None") {
    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.isInWeb = false
    }
}