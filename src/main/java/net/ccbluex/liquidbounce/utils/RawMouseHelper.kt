package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.modules.client.RawInput
import net.minecraft.util.MouseHelper

class RawMouseHelper : MouseHelper() {
    override fun mouseXYChange() {
        this.deltaX = RawInput.dx
        RawInput.dx = 0
        this.deltaY = -RawInput.dy
        RawInput.dy = 0
    }
}
