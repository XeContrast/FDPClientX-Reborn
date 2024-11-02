/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.particles

import net.minecraft.util.MathHelper

class Vec3(x: Double, y: Double, z: Double) {
    /**
     * X coordinate of Vec3D
     */
    var xCoord: Double

    /**
     * Y coordinate of Vec3D
     */
    var yCoord: Double

    /**
     * Z coordinate of Vec3D
     */
    var zCoord: Double

    init {
        var x = x
        var y = y
        var z = z
        if (x == -0.0) {
            x = 0.0
        }

        if (y == -0.0) {
            y = 0.0
        }

        if (z == -0.0) {
            z = 0.0
        }

        this.xCoord = x
        this.yCoord = y
        this.zCoord = z
    }

    fun lengthVector() : Double {
        return MathHelper.sqrt_double(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord).toDouble()
    }

    override fun toString(): String {
        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")"
    }
}
