package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.init.Blocks.lava
import net.minecraft.init.Blocks.water
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.EnumFacing

class NoFluidJesus : JesusMode("NoFluid") {
    private val grim = BoolValue("Grim", false)

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (grim.get()) {
            val searchBlocks = BlockUtils.searchBlocks(2, setOf(water, lava))
            for (block in searchBlocks) {
                val blockpos = block.key
                //TODO:only do this for blocks that player touched
                PacketUtils.sendPacket(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        blockpos,
                        EnumFacing.DOWN
                    )
                )
            }
        }
    }
}