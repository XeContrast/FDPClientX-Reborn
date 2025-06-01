package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.MotionEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "InventoryMove", description = "Allows you to walk with GUI's opened.", category = ModuleCategory.MOVEMENT)
public class InvMove extends Module {

    @EventTarget
    public void onUpdate(MotionEvent event) {
            if (mc.getCurrentScreen2() != null && !(mc.getCurrentScreen2() instanceof GuiChat)) {
                mc2.gameSettings.keyBindForward.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindForward.getKeyCode());
                mc2.gameSettings.keyBindBack.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindBack.getKeyCode());
                mc2.gameSettings.keyBindLeft.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindLeft.getKeyCode());
                mc2.gameSettings.keyBindRight.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindRight.getKeyCode());
                mc2.gameSettings.keyBindJump.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindJump.getKeyCode());
            }
    }
}
