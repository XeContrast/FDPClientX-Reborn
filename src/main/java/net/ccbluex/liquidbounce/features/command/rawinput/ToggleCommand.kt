package net.ccbluex.liquidbounce.features.command.rawinput

import net.ccbluex.liquidbounce.utils.RawInputHandler
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender

class ToggleCommand : CommandBase() {
    override fun getCommandName(): String {
        return "rawinput"
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "Toggles Raw Input (/rawinput)"
    }

    @Throws(CommandException::class)
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        RawInputHandler.toggleRawInput()
    }

    override fun getRequiredPermissionLevel(): Int {
        return -1
    }
}