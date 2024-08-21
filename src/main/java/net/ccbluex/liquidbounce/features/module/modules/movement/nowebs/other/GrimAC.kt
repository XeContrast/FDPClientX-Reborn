package net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.nowebs.NoWebMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class GrimAC : NoWebMode("GrimAC") {
    private val breakeronworld = BoolValue("BreakerOnWordl",true)

    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.isInWeb = false
    }
    @EventTarget
    override fun blockPos(pos: BlockPos) {
        if (breakeronworld.get()) mc.theWorld.setBlockState(pos, Blocks.air.defaultState)

        val start = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,pos,EnumFacing.DOWN)
        val abort = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,pos,EnumFacing.DOWN)
        val finsh = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,pos,EnumFacing.DOWN)

        mc.netHandler.addToSendQueue(start)
        mc.netHandler.addToSendQueue(abort)
        mc.netHandler.addToSendQueue(finsh)
    }
}