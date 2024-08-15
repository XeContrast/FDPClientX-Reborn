/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

object CPSCounter {
    private const val MAX_CPS = 50
    private val TIMESTAMP_BUFFERS = arrayOfNulls<RollingArrayLongBuffer>(MouseButton.entries.size)

    init {
        for (i in TIMESTAMP_BUFFERS.indices) {
            TIMESTAMP_BUFFERS[i] = RollingArrayLongBuffer(MAX_CPS)
        }
    }

    /**
     * Registers a mouse button click
     *
     * @param button The clicked button
     */
    @JvmStatic
    fun registerClick(button: MouseButton) {
        TIMESTAMP_BUFFERS[button.index]!!.add(System.currentTimeMillis())
    }

    /**
     * Gets the count of clicks that have occurrence since the last 1000ms
     *
     * @param button The mouse button
     * @return The CPS
     */
    fun getCPS(button: MouseButton): Int {
        return TIMESTAMP_BUFFERS[button.index]!!.getTimestampsSince(System.currentTimeMillis() - 1000L)
    }

    enum class MouseButton(val index: Int) {
        LEFT(0), MIDDLE(1), RIGHT(2)
    }
}
