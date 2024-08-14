package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.script.api.global.Chat.alert
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class BlocksMCFly : FlyMode("BlocksMc") {
    private var bmcSpeed = 0.0
    private var starteds = false
    override fun onEnable() {
        bmcSpeed = 0.0
        starteds = false
        val bb = mc.thePlayer.entityBoundingBox.offset(0.0, 1.0, 0.0)
        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty() && !starteds) {
            alert("failed to toggle fly")
             Flight.state= false
        } else alert("waiting...")
    }

    @EventTarget
    override fun onMotion(event: MotionEvent) {
        if (event.eventState === EventState.PRE) {
            val bb = mc.thePlayer.entityBoundingBox.offset(0.0, 1.0, 0.0)

            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty() && !starteds) {
                starteds = true
                mc.thePlayer.jump()
                MovementUtils.strafe(5.also { bmcSpeed = it.toDouble() }.toFloat())
                alert("started")
            }

            if (starteds) {
                val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
                if (mc.thePlayer.onGround) {
                    PacketUtils.sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(
                            pos,
                            1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)),
                            0.0F,
                            0.5F + Math.random().toFloat() * 0.44.toFloat(),
                            0.0F
                        )
                    )
                }
                if (mc.thePlayer.ticksExisted % 4 == 0) {
                    alert("sent c08")
                    PacketUtils.sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(
                            pos,
                            1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)),
                            0.0F,
                            0.5F + Math.random().toFloat() * 0.44.toFloat(),
                            0.0F
                        )
                    )
                }
                MovementUtils.strafe(0.95f.let { bmcSpeed *= it; bmcSpeed }.toFloat())
                if (bmcSpeed >= 3.1f)
                    mc.timer.timerSpeed = 0.45f
                else mc.timer.timerSpeed = 0.2f
                alert(bmcSpeed.toString())
                if (bmcSpeed <= 3f) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    alert("disabled")
                    Flight.state = false
                }
            }
        }
    }

    override fun onDisable() {
        mc.thePlayer.motionZ = 0.0
        mc.thePlayer.motionX = 0.0
    }
}