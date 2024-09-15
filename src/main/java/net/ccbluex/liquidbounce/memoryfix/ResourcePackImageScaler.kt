//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package net.ccbluex.liquidbounce.memoryfix

import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.io.IOException

object ResourcePackImageScaler {
    const val SIZE: Int = 64

    @Throws(IOException::class)
    fun scalePackImage(image: BufferedImage?): BufferedImage? {
        if (image == null) {
            return null
        } else {
            println("Scaling resource pack icon from " + image.width + " to " + 64)
            val smallImage = BufferedImage(64, 64, 2)
            val graphics = smallImage.graphics
            graphics.drawImage(image, 0, 0, 64, 64, null as ImageObserver?)
            graphics.dispose()
            return smallImage
        }
    }
}
