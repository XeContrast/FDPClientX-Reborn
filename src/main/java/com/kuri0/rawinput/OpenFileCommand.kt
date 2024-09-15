package com.kuri0.rawinput

import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.Loader
import java.awt.Desktop

class OpenFileCommand : CommandBase() {
    override fun getCommandName(): String {
        return "modfolder"
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return ""
    }

    @Throws(CommandException::class)
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        try {
            Desktop.getDesktop().open(Loader.instance().activeModContainer().source.parentFile)
        } catch (e: Exception) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                ChatComponentText(
                    EnumChatFormatting.RED.toString() + "[RawInput] You are currently on " + System.getProperty(
                        "os.name"
                    ) + ", and RawInput had a problem trying to access your mods folder. Please screenshot this message and ping Erymanthus#5074 in the SkyClient Discord server: https://inv.wtf/skyclient"
                )
            )
        }
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }
}
