package com.kuri0.rawinput

import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.*

/***********************
 * UnintendedUsageWarnings
 *
 * A Java class written by Erymanthus / RayDeeUx as a means to minimize instances of unintentionally loading
 * the RawInput mod in Minecraft instances outside of the Microsoft Windows operating system.
 * An additional check is included to detect a SkyClient instance specifically to complement the soon-to-be
 * (as of time of writing) plan to add a warning for users not on Windows who attempt to select the RawInput
 * mod in the SkyClient installer, as folks who can't read are probably going to go out of their way to sneak
 * the RawInput mod into their SkyClient installation despite those warnings.
 *
 * We truly are living in the future, folks.
 */
class UnintendedUsageWarnings {
    private var hasSentWarningForSession = false
    private val warningMessage =
        "his mod is only intended for Windows. Please remove this mod from your mods folder and relaunch your game as soon as possible, as this mod is dormant when used outside of Windows."
    private var warningMessagePrefix = "[RawInput] T"


    @SubscribeEvent
    fun sendWarning(event: ClientTickEvent?) {
        if (hasSentWarningForSession || (System.getProperty("os.name")
                .lowercase(Locale.getDefault()).contains("windows")) || Minecraft.getMinecraft().thePlayer == null
        ) return
        if ((Loader.isModLoaded("skyclientcosmetics") || Loader.isModLoaded("skyblockclientupdater"))) warningMessagePrefix =
            "[RawInput] Hi there! This is another reminder that t"
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + warningMessagePrefix + warningMessage + " §ePlease run §l/modfolder§r§e to open it now."))
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            ChatComponentText(
                EnumChatFormatting.RED.toString() + "[RawInput] You are currently on " + System.getProperty(
                    "os.name"
                ) + ". If you feel that this message was a mistake, please screenshot this message and ping Erymanthus#5074 in the SkyClient Discord server: https://inv.wtf/skyclient"
            )
        )
        //prevent sending warning more than once per session
        hasSentWarningForSession = true
    }
}
