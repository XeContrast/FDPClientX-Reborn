/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timer

import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.minecraft.util.MathHelper

class TimerUtils {
    private var lastMS = 0L
    private var previousTime: Long

    init {
        this.previousTime = -1L
    }


    private val currentMS: Long
        get() = System.nanoTime() / 1000000L

    fun hasReached(milliseconds: Double): Boolean {
        if ((this.currentMS - this.lastMS).toDouble() >= milliseconds) {
            return true
        }
        return false
    }

    fun delay(milliSec: Float): Boolean {
        if ((this.time - this.lastMS).toFloat() >= milliSec) {
            return true
        }
        return false
    }

    var time: Long
        get() = System.nanoTime() / 1000000L
        set(time) {
            this.lastMS = time
        }

    fun hasTimeElapsed(time: Long): Boolean {
        return System.currentTimeMillis() - lastMS > time
    }

    fun check(milliseconds: Float): Boolean {
        return System.currentTimeMillis() - previousTime >= milliseconds
    }

    fun delay(milliseconds: Double): Boolean {
        return MathHelper.clamp_float((currentMS - lastMS).toFloat(), 0f, milliseconds.toFloat()) >= milliseconds
    }

    fun reset() {
        this.previousTime = System.currentTimeMillis()
        this.lastMS = this.currentMS
    }

    fun time(): Long {
        return System.nanoTime() / 1000000L - lastMS
    }


    fun delay(nextDelay: Long): Boolean {
        return System.currentTimeMillis() - lastMS >= nextDelay
    }

    fun delay(nextDelay: Float, reset: Boolean): Boolean {
        if (System.currentTimeMillis() - lastMS >= nextDelay) {
            if (reset) {
                this.reset()
            }
            return true
        }
        return false
    }

    companion object {
        fun randomDelay(minDelay: Int, maxDelay: Int): Long {
            return nextInt(minDelay, maxDelay).toLong()
        }

        fun randomClickDelay(minCPS: Int, maxCPS: Int): Long {
            return ((Math.random() * (1000 / minCPS - 1000 / maxCPS + 1)) + 1000 / maxCPS).toLong()
        }
    }
}