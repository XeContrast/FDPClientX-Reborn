package net.ccbluex.liquidbounce.rawinput

import net.ccbluex.liquidbounce.rawinput.RawInput
import net.minecraft.util.MouseHelper

class RawMouseHelper : MouseHelper() {
    override fun mouseXYChange() {
        this.deltaX = RawInput.dx
        RawInput.dx = 0
        this.deltaY = -RawInput.dy
        RawInput.dy = 0
    }
}
