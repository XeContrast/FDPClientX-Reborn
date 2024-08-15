/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.gui.GuiDownloadTerrain
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.GuiConnecting

class SessionUtils : MinecraftInstance(), Listenable {
    @EventTarget
    fun onWorld(event: WorldEvent) {
        lastWorldTime = System.currentTimeMillis() - worldTimer.time
        worldTimer.reset()

        if (event.worldClient == null) {
            backupSessionTime = System.currentTimeMillis() - sessionTimer.time
            requireDelay = true
        } else {
            requireDelay = false
        }
    }

    @EventTarget
    fun onSession(event: SessionEvent?) {
        handleConnection()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent) {
        if (event.guiScreen == null && lastScreen != null && (lastScreen is GuiDownloadTerrain || lastScreen is GuiConnecting)) handleReconnection()

        lastScreen = event.guiScreen
    }

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        private val sessionTimer = MSTimer()
        private val worldTimer = MSTimer()

        private var lastSessionTime: Long = 0L
        var backupSessionTime: Long = 0L
        var lastWorldTime: Long = 0L

        private var requireDelay = false

        private var lastScreen: GuiScreen? = null

        @JvmStatic
        fun handleConnection() {
            backupSessionTime = 0L
            requireDelay = true
            lastSessionTime = System.currentTimeMillis() - sessionTimer.time
            if (lastSessionTime < 0L) lastSessionTime = 0L
            sessionTimer.reset()
        }

        fun handleReconnection() {
            if (requireDelay) sessionTimer.time = System.currentTimeMillis() - backupSessionTime
        }

        val formatSessionTime: String
            get() {
                if (System.currentTimeMillis() - sessionTimer.time < 0L) sessionTimer.reset()

                val realTime = (System.currentTimeMillis() - sessionTimer.time).toInt() / 1000
                val hours = realTime / 3600
                val seconds = (realTime % 3600) % 60
                val minutes = (realTime % 3600) / 60

                return hours.toString() + "h " + minutes + "m " + seconds + "s"
            }
    }
}