package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import kotlin.math.floor


class KarhuVelocity : VelocityMode("Karhu") {
    private var ticks = 0
    override fun onEnable() {
        ticks = 0
    }

    @EventTarget
    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && mc.thePlayer.hurtTime > 0) {
            val x = event.x
            val y = event.y
            val z = event.z

            if (y == (floor(mc.thePlayer.posY) + 1).toInt()) {
                event.boundingBox = AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 1.0, 0.0, 1.0).offset(
                    x.toDouble(),
                    y.toDouble(),
                    z.toDouble()
                )
            }
        }
    }
}