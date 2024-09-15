//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package net.ccbluex.liquidbounce.memoryfix

import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.IImageBuffer
import net.minecraft.client.renderer.ImageBufferDownload
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.lang.ref.WeakReference

class CapeImageBuffer(player: AbstractClientPlayer?, private val resourceLocation: ResourceLocation) : IImageBuffer {
    var imageBufferDownload: ImageBufferDownload = ImageBufferDownload()
    private val playerRef: WeakReference<AbstractClientPlayer?> = WeakReference(player)

    fun func_78432_a(image: BufferedImage): BufferedImage? {
        return parseCape(image)
    }

    fun func_152634_a() {
        val player = playerRef.get()
        if (player != null) {
            setLocationOfCape(player, this.resourceLocation)
        }
    }

    override fun parseUserSkin(bufferedImage: BufferedImage): BufferedImage? {
        return null
    }

    override fun skinAvailable() {
    }

    companion object {
        private fun parseCape(image: BufferedImage): BufferedImage? {
            return null
        }

        private fun setLocationOfCape(player: AbstractClientPlayer, resourceLocation: ResourceLocation) {
        }
    }
}