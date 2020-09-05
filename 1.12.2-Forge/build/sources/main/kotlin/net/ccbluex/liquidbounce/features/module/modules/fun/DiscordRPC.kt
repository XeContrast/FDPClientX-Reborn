package net.ccbluex.liquidbounce.features.module.modules.`fun`

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.value.TextValue

@ModuleInfo(name = "DiscordRPC", description = "Custom discord rich presence", category = ModuleCategory.FUN)
class DiscordRPC : Module() {

    public val setDetails2 = TextValue("Details", "hahahahaha")

    public val setState2 = TextValue("State", "no u")

    public val setLargeImage2 = TextValue("SetLargeImage", "you are gay")

    override fun onDisable() {
        (LiquidBounce.moduleManager.getModule(DiscordRPC::class.java) as DiscordRPC?)!!.state = true
    }
}