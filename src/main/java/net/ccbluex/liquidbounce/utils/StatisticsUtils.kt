/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.minecraft.entity.player.EntityPlayer

class StatisticsUtils : Listenable {
    @EventTarget
    fun onTargetKilled(e: EntityKilledEvent) {
        if (e.targetEntity !is EntityPlayer) {
            return
        }

        kills++
    }

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        var kills: Int = 0
            private set
        var deaths: Int = 0
            private set

        @JvmStatic
        fun addDeaths() {
            deaths++
        }
    }
}
