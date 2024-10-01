package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.util.ResourceLocation
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import org.lwjgl.opengl.GL11
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 *
 *
 * @author LingYuWeiGuang
 * @author HyperTap
 * @author ChengFeng
 * @version 1.1.0
 */
class VideoPlayer : MinecraftInstance() {
    private var frameGrabber: FFmpegFrameGrabber? = null
    private var textureBinder: TextureBinder? = null

    private var frameLength = 0

    val paused = AtomicBoolean(false)

    private var scheduler: ScheduledExecutorService? = null
    private var scheduledFuture: ScheduledFuture<*>? = null

    /**
     * 读取一个视频文件
     *
     * @param resource 资源地址
     */
    @Throws(FFmpegFrameGrabber.Exception::class)
    fun init(resource: ResourceLocation?) {
        val videoTemp: File

        // 创建缓存文件
        try {
            videoTemp = File.createTempFile("video_temp", ".mp4")
            val inputStream = mc.resourceManager.getResource(resource).inputStream

            // 覆写
            Files.copy(inputStream, videoTemp.toPath(), StandardCopyOption.REPLACE_EXISTING)
            videoTemp.deleteOnExit()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        frameGrabber = FFmpegFrameGrabber.createDefault(videoTemp)
        frameGrabber!!.pixelFormat = avutil.AV_PIX_FMT_RGB24
        avutil.av_log_set_level(avutil.AV_LOG_QUIET) // Log level -> quiet

        textureBinder = TextureBinder()

        frameGrabber!!.start()
        frameLength = frameGrabber!!.lengthInFrames

        val frameRate = frameGrabber!!.frameRate

        scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduledFuture = scheduler!!.scheduleAtFixedRate(
            { this.grabNextFrame() },
            0,
            (1000 / frameRate).toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * 没完没了地抓取下一帧并设置纹理绑定器数据
     */
    private fun grabNextFrame() {
        // 暂停了就不再抓了
        if (paused.get()) return
        try {
            val frame = frameGrabber!!.grabImage()
            if (frame?.image != null) {
                textureBinder!!.setTexture(frame.image[0] as ByteBuffer, frame.imageWidth, frame.imageHeight)
                if (frameGrabber!!.frameNumber == frameLength - 1) {
                    frameGrabber!!.frameNumber = 0
                }
            }
        } catch (e: FFmpegFrameGrabber.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 绑定纹理并渲染到指定矩形。
     *
     * @param left   左端
     * @param top    顶端
     * @param right  右端
     * @param bottom 底端
     */
    @Throws(FrameGrabber.Exception::class)
    fun render(left: Int, top: Int, right: Int, bottom: Int) {
        textureBinder!!.bindTexture()

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDepthMask(false)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

        // 绘制矩形
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0.0f, 1.0f)
        GL11.glVertex3f(left.toFloat(), bottom.toFloat(), 0f)
        GL11.glTexCoord2f(1.0f, 1.0f)
        GL11.glVertex3f(right.toFloat(), bottom.toFloat(), 0f)
        GL11.glTexCoord2f(1.0f, 0.0f)
        GL11.glVertex3f(right.toFloat(), top.toFloat(), 0f)
        GL11.glTexCoord2f(0.0f, 0.0f)
        GL11.glVertex3f(left.toFloat(), top.toFloat(), 0f)
        GL11.glEnd()

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glPopMatrix()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    /**
     * 调用这个，你就别想再启动了！
     * @throws FFmpegFrameGrabber.Exception
     */
    @Throws(FFmpegFrameGrabber.Exception::class)
    fun stop() {
        if (scheduledFuture != null && !scheduledFuture!!.isCancelled) {
            scheduledFuture!!.cancel(true)
        }

        if (scheduler != null && !scheduler!!.isShutdown) {
            scheduler!!.shutdownNow()
        }

        textureBinder = null

        if (frameGrabber != null) {
            frameGrabber!!.stop()
            frameGrabber!!.release()
            frameGrabber = null
        }
    }
}
