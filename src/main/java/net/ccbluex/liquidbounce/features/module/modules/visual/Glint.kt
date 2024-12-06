/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue

@ModuleInfo(name = "Glint", category = ModuleCategory.VISUAL)
class Glint : Module() {
    var redValue: IntegerValue = IntegerValue("Red", 255, 0, 255)
    var greenValue: IntegerValue = IntegerValue("Green", 0, 0, 255)
    var blueValue: IntegerValue = IntegerValue("Blue", 0, 0, 255)
    var modeValue: ListValue = ListValue("Mode", arrayOf("Custom", "Rainbow", "Sky"), "Custom")
    var rainbowSpeedValue: IntegerValue = IntegerValue("Seconds", 1, 1, 6)
    var rainbowDelayValue: IntegerValue = IntegerValue("Delay", 5, 0, 10)
    var rainbowSatValue: FloatValue = FloatValue("Saturation", 1.0f, 0.0f, 1.0f)
    var rainbowBrgValue: FloatValue = FloatValue("Brightness", 1.0f, 0.0f, 1.0f)
}