package net.ccbluex.liquidbounce.extensions

import java.awt.Color

fun Color.setAlpha(factor: Float) = Color(this.red / 255F, this.green / 255F, this.blue / 255F, factor.coerceIn(0F, 1F))