package net.ccbluex.liquidbounce.utils.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import java.nio.ByteBuffer

/**
 * 异步纹理绑定
 *
 * @version 1.1.0
 *
 * @author LingYuWeiGuang
 * @author HyperTap
 * @author ChengFeng
 */
class TextureBinder {
    private var width = 0
    private var height = 0

    // 使用唯一的TextureID
    private val textureID = GL11.glGenTextures()
    var buffer: ByteBuffer? = null

    /**
     * 设定纹理数据、宽度、高度
     *
     * @param buffer 纹理数据
     * @param width 纹理宽度
     * @param height 纹理高度
     */
    fun setTexture(buffer: ByteBuffer?, width: Int, height: Int) {
        this.buffer = buffer
        this.width = width
        this.height = height
    }

    /**
     * 绑定纹理
     */
    fun bindTexture() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB,
            this.width,
            this.height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
            this.buffer
        )
    }
}