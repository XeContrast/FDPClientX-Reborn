/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.particles

import net.ccbluex.liquidbounce.utils.timer.ParticleTimer
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockBush
import net.minecraft.block.BlockLiquid

class Particle(val position: Vec3) {
    private val removeTimer = ParticleTimer()

    private val delta = Vec3(
        (Math.random() * 2.5 - 1.25) * 0.04,
        (Math.random() * 0.5 - 0.2) * 0.04,
        (Math.random() * 2.5 - 1.25) * 0.04
    )

    init {
        removeTimer.reset()
    }

    fun update() {
        val block1 = PlayerParticles.getBlock(
            position.xCoord,
            position.yCoord,
            position.zCoord + delta.zCoord
        )
        if (!(block1 is BlockAir || block1 is BlockBush || block1 is BlockLiquid)) delta.zCoord *= -0.8

        val block2 = PlayerParticles.getBlock(
            position.xCoord,
            position.yCoord + delta.yCoord,
            position.zCoord
        )
        if (!(block2 is BlockAir || block2 is BlockBush || block2 is BlockLiquid)) {
            delta.xCoord *= 0.99
            delta.zCoord *= 0.99

            delta.yCoord *= -0.5
        }

        val block3 = PlayerParticles.getBlock(
            position.xCoord + delta.xCoord,
            position.yCoord,
            position.zCoord
        )
        if (!(block3 is BlockAir || block3 is BlockBush || block3 is BlockLiquid)) delta.xCoord *= -0.8

        this.updateWithoutPhysics()
    }

    fun updateWithoutPhysics() {
        position.xCoord += delta.xCoord
        position.yCoord += delta.yCoord
        position.zCoord += delta.zCoord
        delta.xCoord *= 0.998
        delta.yCoord -= 0.000031
        delta.zCoord *= 0.998
    }
}
