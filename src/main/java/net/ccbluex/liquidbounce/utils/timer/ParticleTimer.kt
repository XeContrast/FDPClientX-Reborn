/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timer

class ParticleTimer {
    private var lastMS: Long = 0

    private val currentMS: Long
        get() = System.nanoTime() / 1000000L

    val elapsedTime: Long
        get() = this.currentMS - this.lastMS

    fun reset() {
        this.lastMS = this.currentMS
    }
}

