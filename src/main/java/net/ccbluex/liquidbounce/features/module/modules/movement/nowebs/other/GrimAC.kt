package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.init.Blocks
import net.minecraft.init.Blocks.web
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class GrimAC : NoWebMode("GrimAC") {

    @EventTarget
    override fun onUpdate() {
        val searchBlocks = BlockUtils.searchBlocks(2, setOf(web))
        mc.thePlayer.isInWeb = false
        for (block in searchBlocks) {
            val blockpos = block.key
            sendPacket(C07PacketPlayerDigging(Action.STOP_DESTROY_BLOCK,blockpos, EnumFacing.DOWN))
        }
    }
}