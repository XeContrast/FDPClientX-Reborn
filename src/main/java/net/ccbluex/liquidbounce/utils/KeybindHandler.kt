package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard

class KeybindHandler {
    @EventTarget
    fun onKeyInput(event: InputEvent.KeyInputEvent?) {
        if (toggleKey!!.isPressed) {
            RawInputHandler.toggleRawInput()
        }
        if (rescanKey!!.isPressed) {
            RawInputHandler.getMouse()
        }
    }

    companion object {
        var toggleKey: KeyBinding? = null
        var rescanKey: KeyBinding? = null

        fun init() {
            toggleKey = KeyBinding("Toggle Raw Input", Keyboard.CHAR_NONE, "Raw Input")
            ClientRegistry.registerKeyBinding(toggleKey)

            rescanKey = KeyBinding("Rescan Key", Keyboard.CHAR_NONE, "Raw Input")
            ClientRegistry.registerKeyBinding(rescanKey)
        }
    }
}