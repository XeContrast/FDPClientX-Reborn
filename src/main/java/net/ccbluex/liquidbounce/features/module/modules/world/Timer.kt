/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving

@ModuleInfo(name = "Timer", category = ModuleCategory.WORLD)
object Timer : Module() {

    private val bypass = ListValue("Bypass", arrayOf("Matrix","Normal"),"Normal")
    private val mode = ListValue("Mode", arrayOf("OnMove", "NoMove", "Always"), "OnMove")
    private val speed = FloatValue("Speed", 2F, 0.1F,10F)
    private var ticks = 0
    private var ticks2 = 0
    private var start = false

    override fun onDisable() {
        ticks = 0
        ticks2 = 0
        start = false
        if (mc.thePlayer == null)
            return

        mc.timer.timerSpeed = 1F
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mode.get() == "Always" || mode.get() == "OnMove" && isMoving || mode.get() == "NoMove" && !isMoving) {
            if (bypass.equals("Normal")) {
                mc.timer.timerSpeed = speed.get()
            } else {
                if (isMoving) {
                    if (!start) {
                        mc.timer.timerSpeed = 0.1f
                        ticks++
                    }
                } else {
                    start = true
                }
                if (start) {
                    ticks2 ++
                    mc.timer.timerSpeed = speed.get()
                    if (ticks2 == ticks) {
                        FDPClient.moduleManager[Timer::class.java]!!.state = false
                    }
                }
            }
            return
        }

        mc.timer.timerSpeed = 1F
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient != null)
            return

        state = false
    }
}
