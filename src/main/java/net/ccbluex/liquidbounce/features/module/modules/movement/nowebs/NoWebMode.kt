package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.NoWeb
import net.ccbluex.liquidbounce.features.value.Value
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.util.BlockPos

abstract class NoWebMode(val modeName: String) : MinecraftInstance() {
    protected val valuePrefix = "$modeName-"

    protected val noweb: NoWeb
        get() = FDPClient.moduleManager[NoWeb::class.java]!!

    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    open fun onEnable() {}
    open fun onDisable() {}

    open fun onPreMotion() {}
    open fun onMotion(event: MotionEvent) {}
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onTick() {}
    open fun onStrafe(event: StrafeEvent) {}
    open fun onBlockBB(event: BlockBBEvent) {}
    open fun onBlockPos(pos: BlockPos) {}
}