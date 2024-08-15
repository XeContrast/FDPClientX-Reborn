package net.ccbluex.liquidbounce.utils

import java.awt.Color

object Colors {
    fun getColor(color: Int, a: Int): Int {
        val color1 = Color(color)
        return Color(color1.red, color1.green, color1.blue, a).rgb
    }

    fun getColor(brightness: Int): Int {
        return getColor(brightness, brightness, brightness, 255)
    }


    fun getColor(red: Int, green: Int, blue: Int, alpha: Int): Int {
        var color = 0
        color = color or (alpha shl 24)
        color = color or (red shl 16)
        color = color or (green shl 8)
        color = color or blue
        return color
    }
}
