/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.GameTickEvent

object WaitTickUtils : MinecraftInstance(), Listenable {

    private val scheduledActions = mutableListOf<ScheduledAction>()

    fun scheduleTicks(ticks: Int, action: () -> Unit) {
        scheduledActions.add(ScheduledAction(ClientUtils.runTimeTicks + ticks, action))
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        val currentTick = ClientUtils.runTimeTicks
        val iterator = scheduledActions.iterator()

        while (iterator.hasNext()) {
            val scheduledAction = iterator.next()
            if (currentTick >= scheduledAction.ticks) {
                scheduledAction.action.invoke()
                iterator.remove()
            }
        }
    }

    private data class ScheduledAction(val ticks: Int, val action: () -> Unit)

    override fun handleEvents() = true
}