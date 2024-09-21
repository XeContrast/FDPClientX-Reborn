package dev.tr7zw.skinlayers.opengl

import lombok.Getter
import java.nio.ByteBuffer

class NativeImage(format: Format, i: Int, j: Int, bl: Boolean) : AutoCloseable {
    private val format: Format

    @Getter
    private val width: Int

    @Getter
    private val height: Int

    private val buffer: ByteBuffer

    constructor(i: Int, j: Int, bl: Boolean) : this(Format.RGBA, i, j, bl)

    init {
        require(!(i <= 0 || j <= 0)) { "Invalid texture size: " + i + "x" + j }
        this.format = format
        this.width = i
        this.height = j
        val size = i * j * format.components()
        buffer = ByteBuffer.allocateDirect(size)
    }

    private fun isOutsideBounds(i: Int, j: Int): Boolean {
        return ((i < 0 || i >= width) || j < 0 || j >= this.height)
    }

    override fun close() {
        // nothing to do?
    }

    fun format(): Format {
        return this.format
    }

    private fun getPixelRGBA(i: Int, j: Int): Int {
        require(this.format == Format.RGBA) {
            String.format(
                "getPixelRGBA only works on RGBA images; have %s",
                format
            )
        }
        require(!isOutsideBounds(i, j)) {
            String.format(
                "(%s, %s) outside of image bounds (%s, %s)", i,
                j, this.width, this.height
            )
        }
        val l = (i + j * this.width) * 4
        return buffer.getInt(l)
    }

    private fun setPixelRGBA(i: Int, j: Int, k: Int) {
        require(this.format == Format.RGBA) {
            String.format(
                "getPixelRGBA only works on RGBA images; have %s",
                format
            )
        }
        require(!isOutsideBounds(i, j)) {
            String.format(
                "(%s, %s) outside of image bounds (%s, %s)", i,
                j, this.width, this.height
            )
        }
        val l = (i + j * this.width) * 4
        buffer.putInt(l, k)
    }

    fun getLuminanceOrAlpha(i: Int, j: Int): Byte {
        require(format.hasLuminanceOrAlpha()) { String.format("no luminance or alpha in %s", this.format) }
        require(!isOutsideBounds(i, j)) {
            String.format(
                "(%s, %s) outside of image bounds (%s, %s)", i,
                j, this.width, this.height
            )
        }
        val k = (i + j * this.width) * format.components() + format.luminanceOrAlphaOffset() / 8
        return buffer[k]
    }


    fun downloadTexture(i: Int, bl: Boolean) {
        //RenderSystem.assertOnRenderThread();
        format.setPackPixelStoreState()
        GlStateManager._getTexImage(
            3553, i,
            format.glFormat(), 5121, this.buffer
        )
        if (bl && format.hasAlpha()) for (j in 0 until height) {
            for (k in 0 until width) setPixelRGBA(k, j, getPixelRGBA(k, j) or (255 shl format.alphaOffset()))
        }
    }

    enum class Format(
        private val components: Int,
        private val glFormat: Int,
        private val hasLuminance: Boolean,
        private val hasAlpha: Boolean,
        private val luminanceOffset: Int,
        private val alphaOffset: Int
    ) {
        RGBA(4, 6408, false, true, 255, 24), RGB(
            3, 6407, false,
            false, 255, 255
        ),
        LUMINANCE_ALPHA(
            2, 33319, true, true,
            0, 8
        ),
        LUMINANCE(1, 6403, true, false, 0, 255);

        fun components(): Int {
            return this.components
        }

        fun setPackPixelStoreState() {
            //RenderSystem.assertOnRenderThread();
            GlStateManager._pixelStore(3333, components())
        }

        fun glFormat(): Int {
            return this.glFormat
        }

        fun hasAlpha(): Boolean {
            return this.hasAlpha
        }

        fun alphaOffset(): Int {
            return this.alphaOffset
        }

        fun hasLuminanceOrAlpha(): Boolean {
            return (this.hasLuminance || this.hasAlpha)
        }

        fun luminanceOrAlphaOffset(): Int {
            return if (this.hasLuminance) this.luminanceOffset else this.alphaOffset
        }

        companion object {
            fun getStbFormat(i: Int): Format {
                when (i) {
                    1 -> return LUMINANCE
                    2 -> return LUMINANCE_ALPHA
                    3 -> return RGB
                }
                return RGBA
            }
        }
    }
}