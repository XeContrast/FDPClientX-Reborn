/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue

@ModuleInfo(name = "TrueSight", category = ModuleCategory.VISUAL)
object TrueSight : Module() {

    val barriersValue = BoolValue("Barriers", true)
    val entitiesValue = BoolValue("Entities", true)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (barriersValue.get()) {
            if (mc.gameSettings.particleSetting == 2) {
                mc.gameSettings.particleSetting = 1
            }
        }
    }
}