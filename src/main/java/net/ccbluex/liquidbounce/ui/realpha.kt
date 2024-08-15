package net.ccbluex.liquidbounce.ui

import java.awt.Color

object realpha {
    @JvmStatic
    fun reAlpha(n: Int, n2: Float): Int {
        val color = Color(n)
        return Color(0.003921569f * color.red, 0.003921569f * color.green, 0.003921569f * color.blue, n2).rgb
    }
}