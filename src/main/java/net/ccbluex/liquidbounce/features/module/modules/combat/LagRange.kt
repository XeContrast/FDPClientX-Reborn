package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameLoopEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Text.Companion.DECIMAL_FORMAT
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils

@ModuleInfo("LagRange", category = ModuleCategory.COMBAT)
class LagRange : Module() {
    private val lagTime = IntegerValue("LagTime", 50, 0, 500)
    private val min : FloatValue = object : FloatValue("MinRange", 3.6f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = max.get()
            if (i < newValue) set(i)
        }
    }
    private val max : FloatValue = object : FloatValue("MaxRange", 5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = min.get()
            if (i > newValue) set(i)
        }
    }
    private val delay = IntegerValue("Delay", 150, 50, 2000)
    private val fov = IntegerValue("Fov", 90, 0, 180)
    private val onGround = BoolValue("OnGround", false)

    private var lastLagTime: Long = 0
    private var reach: Float = 0F

    @EventTarget
    private fun onGameLoop(event: GameLoopEvent) {
        if (!shouldStart()) return

        Thread.sleep(lagTime.get().toLong())
        lastLagTime = System.currentTimeMillis()
    }

    private fun shouldStart(): Boolean {
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false
        if (onGround.get() && !mc.thePlayer.onGround) return false
        if (!MovementUtils.isMoving) return false
        if (fov.get() == 0) return false
        if (System.currentTimeMillis() - lastLagTime < delay.get()) return false

        world.loadedEntityList
            .filter { it != null && EntityUtils.isSelected(it,true)}
            .filter { player.getDistanceToEntityBox(it) <= reach && it != player}
            .filter { EntityUtils.isLookingOnEntities(
                it,
                fov.get().toDouble())
            }
            .forEach { _ ->
                reach = RandomUtils.nextFloat(min.get(), max.get())
                return true
            }


//        for (entity in mc.theWorld.loadedEntityList) {
//            reach = RandomUtils.nextFloat(min.get(), max.get())
//            if (EntityUtils.isSelected(
//                    entity,
//                    true
//                ) && mc.thePlayer.getDistanceToEntityBox(entity) <= reach && entity != mc.thePlayer && EntityUtils.isLookingOnEntities(
//                    entity,
//                    fov.get().toDouble()
//                )
//            ) {
//                return true
//            }
//        }
        return false
    }

    override val tag: String
        get() = DECIMAL_FORMAT.format(reach)


}