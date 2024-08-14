/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TextEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import java.util.*

@ModuleInfo("NameProtect", category = ModuleCategory.VISUAL)
object NameProtect : Module() {

    val allPlayers = BoolValue("AllPlayers", false)
    val skinProtect = BoolValue("SkinProtect", true)
    private val fakeNameValue = TextValue("FakeName", "&cProtected User")
    private val allFakeNameValue = TextValue("AllPlayersFakeName", "FDP")
    private val selfValue: BoolValue = BoolValue("Yourself", true)
    private val tagValue: BoolValue = BoolValue("Tag", false)
    private val nameSpoofValue: BoolValue = BoolValue("NameSpoof", false)
    private val customNameValue: TextValue = TextValue("CustomName", "")

    @EventTarget
    fun onText(event: TextEvent) {
        if (mc.thePlayer == null || Objects.requireNonNull(event.text)
            !!.contains("§8[§9§l" + FDPClient.CLIENT_NAME + "§8] §3") || event.text!!.startsWith("/") || event.text!!.startsWith(
                FDPClient.commandManager.prefix.toString() + ""
            )
        ) return

        for (friend in FDPClient.fileManager.friendsConfig.friends) event.text =
            StringUtils.replace(event.text, friend.playerName, translateAlternateColorCodes(friend.alias) + "§f")

        var playerName = mc.thePlayer.name
        if (nameSpoofValue.get() && customNameValue.get().isNotEmpty()) {
            playerName = customNameValue.get()
        }

        event.text = StringUtils.replace(
            event.text,
            mc.thePlayer.name,
            (if (selfValue.get()) (if (tagValue.get()) StringUtils.injectAirString(playerName) + " §7(§r" + translateAlternateColorCodes(
                fakeNameValue.get() + "§r§7)"
            ) else translateAlternateColorCodes(fakeNameValue.get()) + "§r") else playerName)
        )

        if (allPlayers.get()) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                var playerInfoName = playerInfo.gameProfile.name
                if (nameSpoofValue.get() && customNameValue.get().isNotEmpty()) {
                    playerInfoName = customNameValue.get()
                }
                event.text = StringUtils.replace(
                    event.text,
                    playerInfoName,
                    translateAlternateColorCodes(allFakeNameValue.get()) + "§f"
                )
            }
        }
    }

}