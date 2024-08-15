/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.FDPClient
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData

object ServerUtils : MinecraftInstance() {
    @JvmField
    var serverData: ServerData? = null

    @JvmStatic
    fun connectToLastServer() {
        if (serverData == null) return

        mc.displayGuiScreen(GuiConnecting(GuiMultiplayer(FDPClient.mainMenu), mc, serverData))
    }

    val remoteIp: String
        get() {
            var serverIp = "Idling"

            if (mc.isIntegratedServerRunning) {
                serverIp = "SinglePlayer"
            } else if (mc.theWorld != null && mc.theWorld.isRemote) {
                val serverData = mc.currentServerData
                if (serverData != null) serverIp = serverData.serverIP
            }

            return serverIp
        }
}