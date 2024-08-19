package net.ccbluex.liquidbounce.features.command.rawinput

import net.ccbluex.liquidbounce.utils.RawInputHandler
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender

class RescanCommand : CommandBase() {
    override fun getCommandName(): String {
        return "rescan"
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "Rescans input devices: /rescan"
    }

    @Throws(CommandException::class)
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        RawInputHandler.getMouse()
    }

    override fun getRequiredPermissionLevel(): Int {
        return -1
    }
}