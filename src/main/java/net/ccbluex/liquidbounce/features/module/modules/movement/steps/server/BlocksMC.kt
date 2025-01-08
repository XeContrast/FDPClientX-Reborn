package net.ccbluex.liquidbounce.features.module.modules.movement.steps.server

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.fakeJump
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepX
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepY
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.stepZ
import net.ccbluex.liquidbounce.features.module.modules.movement.Step.timer
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class BlocksMC : StepMode("BlocksMC") {
    override fun onStep(event: StepEvent) {
        if (event.eventState == EventState.PRE) else {
            fakeJump()

            val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)

            mc.netHandler.addToSendQueue(
                C08PacketPlayerBlockPlacement(pos, 1,
                    ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
            )

            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    stepX,
                stepY + 0.41999998688698, stepZ, false))
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    stepX,
                stepY + 0.7531999805212, stepZ, false))
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    stepX,
                stepY + 1, stepZ, true))

            // Reset timer
            timer.reset()
        }
    }
}