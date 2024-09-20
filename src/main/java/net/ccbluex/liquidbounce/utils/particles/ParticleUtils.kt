/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.particles

import net.vitox.ParticleGenerator

object ParticleUtils {
    private val particleGenerator = ParticleGenerator(100)

    @JvmStatic
    fun drawParticles(mouseX: Int, mouseY: Int) {
        particleGenerator.draw(mouseX, mouseY)
    }
}