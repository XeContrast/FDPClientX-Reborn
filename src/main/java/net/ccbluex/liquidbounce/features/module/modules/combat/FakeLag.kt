/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

private class PosData {
    var height: Float = 1.9f
    var width: Float = 0.4f
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0
    var prevX: Double = 0.0
    var prevY: Double = 0.0
    var prevZ: Double = 0.0
    private var increment = 0

    val posX: Double
        get() = x / 32.0

    val posY: Double
        get() = y / 32.0

    val posZ: Double
        get() = z / 32.0

    fun motionX(x: Byte) {
        prevX = posX
        this.x += x
        increment = 3
    }

    fun motionY(y: Byte) {
        prevY = posY
        this.y += y
        increment = 3
    }

    fun motionZ(z: Byte) {
        prevZ = posZ
        this.z += z
        increment = 3
    }

    fun update() {
        if (increment > 0) {
            prevX += ((x / 32.0) - prevX) / increment
            prevY += ((y / 32.0) - prevY) / increment
            prevZ += ((z / 32.0) - prevZ) / increment
            --increment
        }
    }
}