/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.value.BoolValue
import kotlin.math.max
import kotlin.math.min

class AnimationHelper {
    @JvmField
    var animationX: Float = 0f
    @JvmField
    var alpha: Int = 0
    fun resetAlpha() {
        this.alpha = 0
    }

    constructor() {
        this.alpha = 0
    }

    fun updateAlpha(speed: Int) {
        if (alpha < 255) this.alpha += speed
    }

    constructor(value: BoolValue) {
        animationX = (if (value.get()) 5 else -5).toFloat()
    }

    constructor(module: Module) {
        animationX = (if (module.state) 5 else -5).toFloat()
    }

    companion object {
        @JvmStatic
        fun animate(target: Double, current: Double, speed: Double): Double {
            var current = current
            var speed = speed
            val larger = target > current
            val bl = larger
            if (speed < 0.0) {
                speed = 0.0
            } else if (speed > 1.0) {
                speed = 1.0
            }
            val dif = max(target, current) - min(target, current)
            var factor = dif * speed
            if (factor < 0.1) {
                factor = 0.1
            }
            current = if (larger) (factor.let { current += it; current }) else (factor.let { current -= it; current })
            return current
        }
    }
}
