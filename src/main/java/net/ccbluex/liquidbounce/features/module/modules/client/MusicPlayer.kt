package net.ccbluex.liquidbounce.features.module.modules.client

import cn.hanabi.gui.cloudmusic.ui.MusicPlayerUI
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.gui.GuiScreen

@ModuleInfo(name = "MusicPlayer", category = ModuleCategory.CLIENT, canEnable = false)
class MusicPlayer : Module() {
    override fun onEnable() {
        mc.displayGuiScreen(MusicPlayerUI())
    }
}