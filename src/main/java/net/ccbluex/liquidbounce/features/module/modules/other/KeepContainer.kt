package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.ScreenEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notifications
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard

@ModuleInfo("KeepContainer", category = ModuleCategory.OTHER)
object KeepContainer : Module() {
    private var container: GuiContainer? = null

    override fun onDisable() {
        if (container != null)
            PacketUtils.sendPacket(C0DPacketCloseWindow(container!!.inventorySlots.windowId),false)

        container = null
    }

    @EventTarget
    fun onGui(event: ScreenEvent) {
        if (event.guiScreen is GuiContainer && event.guiScreen !is GuiInventory)
            container = event.guiScreen
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        if (event.key == Keyboard.KEY_INSERT) {
            if (container == null)
                return
            FDPClient.hud.addNotification(Notification("KeepCon.","Open Container",NotifyType.INFO,1000))
            mc.displayGuiScreen(container)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C0DPacketCloseWindow)
            event.cancelEvent()
        else if (event.packet is S2EPacketCloseWindow) {
            if (event.packet.windowId == container?.inventorySlots?.windowId)
                container = null
        }
    }
}