package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.vanilla

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode

class FastFall : NoWebMode("FastFall") {
    @EventTarget
    override fun onUpdate() {
        if (mc.thePlayer.onGround) mc.thePlayer.jump()
        if (mc.thePlayer.motionY > 0f) {
            mc.thePlayer.motionY -= mc.thePlayer.motionY * 2
        }
    }
}