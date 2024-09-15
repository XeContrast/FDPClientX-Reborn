package com.kuri0.rawinput

import net.minecraft.util.MouseHelper

class RawMouseHelper : MouseHelper() {
    override fun mouseXYChange() {
        this.deltaX = RawInput.dx
        RawInput.dx = 0
        this.deltaY = -RawInput.dy
        RawInput.dy = 0
    }
}
