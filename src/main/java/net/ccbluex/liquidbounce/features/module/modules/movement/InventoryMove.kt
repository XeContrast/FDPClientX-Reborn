/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.ClickWindowEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiEditSign
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "InventoryMove", description = "Allows you to walk while an inventory is opened.", category = ModuleCategory.MOVEMENT)
class InventoryMove : Module() {

    val aacAdditionProValue = BoolValue("AACAdditionPro", false)
    private val noMoveClicksValue = BoolValue("NoClicksWhileMoving", false)

    @EventTarget
    fun onMotion(event: MotionEvent) {
            if (mc.currentScreen != null) {
                mc2.gameSettings.keyBindForward.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindForward.keyCode)
                mc2.gameSettings.keyBindBack.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindBack.keyCode)
                mc2.gameSettings.keyBindLeft.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindLeft.keyCode)
                mc2.gameSettings.keyBindRight.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindRight.keyCode)
                mc2.gameSettings.keyBindJump.pressed = Keyboard
                        .isKeyDown(mc2.gameSettings.keyBindJump.keyCode)
            }
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (noMoveClicksValue.get() && MovementUtils.isMoving)
            event.cancelEvent()
    }

    override val tag: String?
        get() = if (aacAdditionProValue.get()) "AACAdditionPro" else null
}