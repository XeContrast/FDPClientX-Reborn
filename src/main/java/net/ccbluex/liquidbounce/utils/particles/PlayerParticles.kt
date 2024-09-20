/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.particles

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos

object PlayerParticles {
    private val mc: Minecraft = Minecraft.getMinecraft()

    fun getBlock(offsetX: Double, offsetY: Double, offsetZ: Double): Block {
        return mc.theWorld.getBlockState(BlockPos(offsetX, offsetY, offsetZ)).block
    }
}
