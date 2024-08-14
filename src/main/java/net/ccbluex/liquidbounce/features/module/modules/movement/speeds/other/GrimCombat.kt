package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.MovementUtils.getSpeed
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.EntityLivingBase

class GrimCombat : SpeedMode("GrimCombat") {
    private val onlyAir = BoolValue("OnlyAir", false)
    private val okstrafe = BoolValue("Strafe", false)
    private val speedUp = BoolValue("SpeedUp", false)
    private val speed2 = IntegerValue("Speed", 0, 0, 15)
    val distance = FloatValue("Range", 0f, 0f, 2f)
    override fun onUpdate() {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase && entity.entityId != mc.thePlayer.entityId && mc.thePlayer.getDistanceToEntityBox(
                    entity
                ) <= distance.get() && ( !onlyAir.get() || !mc.thePlayer.onGround)
            ) {
                if(speedUp.get()) {
                    mc.thePlayer.motionX *= (1 + (speed2.get() * 0.01))
                    mc.thePlayer.motionZ *= (1 + (speed2.get() * 0.01))
                }
                if(okstrafe.get()){
                    strafe(getSpeed())
                }
                return
            }
        }
    }
}