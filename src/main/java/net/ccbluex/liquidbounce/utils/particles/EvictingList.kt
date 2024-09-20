/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.particles

import java.util.*

class EvictingList<T>(private val maxSize: Int) : LinkedList<T>() {
    override fun add(element: T): Boolean {
        if (size >= maxSize) removeFirst()
        return super.add(element)
    }
}