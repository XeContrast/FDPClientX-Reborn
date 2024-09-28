package net.ccbluex.liquidbounce.rawinput

import net.ccbluex.liquidbounce.rawinput.RawInput
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

class RescanCommand : CommandBase() {
    override fun getCommandName(): String {
        return "rescan"
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "Rescans input devices: /rescan"
    }

    @Throws(CommandException::class)
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        sender.addChatMessage(ChatComponentText(EnumChatFormatting.GOLD.toString() + "[RawInput] Rescanning input devices..."))
        RawInput.mouse = null
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }
}
