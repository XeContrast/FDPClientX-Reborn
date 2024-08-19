package net.ccbluex.liquidbounce.utils

import net.minecraft.util.MouseHelper
import org.lwjgl.input.Mouse

class RawMouseHelper : MouseHelper() {
    override fun mouseXYChange() {
        this.deltaX = RawInputHandler.dx
        RawInputHandler.dx = 0
        this.deltaY = -RawInputHandler.dy
        RawInputHandler.dy = 0
    }

    override fun grabMouseCursor() {
        if (System.getProperty("fml.noGrab", "false").toBoolean()) return
        Mouse.setGrabbed(true)
        this.deltaX = 0
        RawInputHandler.dx = 0
        this.deltaY = 0
        RawInputHandler.dy = 0
    }
}