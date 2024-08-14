package net.ccbluex.liquidbounce.utils

import java.awt.Point
import kotlin.Float.Companion.NaN

class Rectangle(var x: Float = NaN, var y: Float = NaN, var width: Float = NaN, var height: Float = NaN) {
    constructor(rect: Rectangle) : this(rect.x, rect.y, rect.width, rect.height)

    fun contains(point: Point) = point.x.toFloat() in x..x + width && point.y.toFloat() in y..y + height
    fun contains(x: Float, y: Float) = contains(Point(x.toInt(), y.toInt()))
    fun contains(x: Int, y: Int) = contains(Point(x.toFloat().toInt(), y.toFloat().toInt()))

    val x2
        get() = x + width

    val y2
        get() = y + height
}