package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.polar

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.MovingObjectPosition.MovingObjectType

class PolarVelocity : VelocityMode("Polar") {
    private var attack = false
    override fun onDisable() {
        attack = false
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        var var10000: EntityPlayerSP?
        attack = mc.thePlayer.isSwingInProgress
        if (mc.objectMouseOver.typeOfHit.equals(MovingObjectType.ENTITY) && mc.thePlayer.hurtTime > 0 && !attack) {
            if (velocity.debug.get()) {
                Chat.alert("Motion *= 0.45")
            }
            var10000 = mc.thePlayer
            var10000.motionX *= 0.45
            var10000 = mc.thePlayer
            var10000.motionZ *= 0.45
            mc.thePlayer.isSprinting = false
        }
        attack = false
    }
}