package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.ListValue

@ModuleInfo("FPSPlus", category = ModuleCategory.CLIENT)
object FPSPlus : Module() {
    val modes by ListValue("Version", arrayOf("Riven","Half-Riven","BetterMathHelper","LIBGDX","Vanilla"),"Riven")
}