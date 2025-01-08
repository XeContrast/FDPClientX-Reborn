package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.Animation
import net.ccbluex.liquidbounce.utils.Easing
import net.ccbluex.liquidbounce.utils.render.RenderUtils.color
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin


@ModuleInfo("BlueArchiveHalos", category = ModuleCategory.VISUAL)
class BlueArchiveHalos : Module() {
    private val mode by ListValue("Mode", arrayOf("Shiroko", "Hoshino", "Aris", "Yuuka", "Natsu", "Reisa", "Shiroko*Terror"),"Shiroko")
    private val showInFirstPerson by BoolValue("FirstPerson",true)

    private var animations: Animation = Animation(Easing.LINEAR, 2000)
    private var isReversing = false

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.gameSettings.thirdPersonView == 0 && !showInFirstPerson) return

        when (mode.lowercase()) {
            "shiroko" -> drawShirokoHalo()
            "hoshino" -> drawHoshinoHalo()
            "aris" -> drawArisHalo()
            "yuuka" -> drawYuukaHalo()
            "natsu" -> drawNatsuHalo()
            "reisa" -> drawReisaHalo()
            "shiroko*terror" -> drawShiroko_TerrorHalo()
        }
    }


    private fun drawShirokoHalo() {
        val player = mc.thePlayer ?: return
        animations.run(if (isReversing) 0.0 else 0.1)

        if (animations.value == 0.0) isReversing = false
        if (animations.value == 0.1) isReversing = true

        val height = player.height + 0.25f + animations.value

        GL11.glPushMatrix()
        GL11.glTranslated(
            player.lastTickPosX + (player.posX - player.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            player.lastTickPosY + (player.posY - player.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + height,
            player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        color(
            Color(
                0 + (animations.value * 1800).toInt(),
                (230 + animations.value * 200).toInt(), 250, 220
            ).rgb
        )

        val yaw = player.rotationYaw

        // 使光环中心随玩家朝向旋转
        GL11.glRotatef(-yaw, 0f, 1f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        // 绘制内圈
        GL11.glLineWidth(2.5f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 0
            while (i <= 360) {
                GL11.glVertex2f(
                    cos(Math.toRadians(i.toDouble())).toFloat() * 0.18f,
                    sin(Math.toRadians(i.toDouble())).toFloat() * 0.18f
                )
                i += 5
            }
        }
        GL11.glEnd()

        // 绘制外圈
        GL11.glTranslated(0.0, 0.0, -0.02)
        GL11.glLineWidth(3.7f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = 0
        while (i <= 360) {
            val angle = Math.toRadians(i.toDouble()).toFloat()
            val x = cos(angle.toDouble()).toFloat() * 0.3f
            val y = sin(angle.toDouble()).toFloat() * 0.3f

            GL11.glVertex2f(x, y)

            // 在每个四等分点上添加突出效果
            if (i % 90 == 0) { // 四等分点
                val offset = 0.1f
                val inwardOffset = 0.03f

                // 向外突出顶点
                GL11.glVertex2f(
                    x + cos(angle.toDouble()).toFloat() * offset, y + sin(angle.toDouble())
                        .toFloat() * offset
                )
                // 回到原始顶点
                GL11.glVertex2f(x, y)
                // 向内突出顶点
                GL11.glVertex2f(
                    x - cos(angle.toDouble()).toFloat() * inwardOffset, y - sin(angle.toDouble())
                        .toFloat() * inwardOffset
                )
                // 回到原始顶点
                GL11.glVertex2f(x, y)
            }
            i += 5
        }
        GL11.glEnd()

        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()

        GL11.glPopMatrix()
    }

    private fun drawHoshinoHalo() {
        animations.run(if (isReversing) 0.0 else 0.1)

        if (animations.value == 0.0) isReversing = false
        if (animations.value == 0.1) isReversing = true

        val height = mc.thePlayer.height + 0.25f + animations.value
        val extraHeight = 0.035f
        val extensionLength = 0.18f
        val smallExtensionLength = 0.08f

        GL11.glPushMatrix()
        GL11.glTranslated(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + height,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        GlStateManager.enableBlend()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        color(Color(237, 110 + (animations.value * 600).toInt(), 183, 220).rgb) // RGB for pink

        val yaw = mc.thePlayer.rotationYaw

        // 使光环中心随玩家朝向旋转
        GL11.glRotatef(-yaw, 0f, 1f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        // 绘制内圈
        GL11.glLineWidth(4.0f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 0
            while (i <= 360) {
                GL11.glVertex2f(
                    cos(Math.toRadians(i.toDouble())).toFloat() * 0.13f,
                    sin(Math.toRadians(i.toDouble())).toFloat() * 0.13f
                )
                i += 5
            }
        }
        GL11.glEnd()

        // 绘制中间圈
        GL11.glTranslated(0.0, 0.0, -extraHeight.toDouble())
        GL11.glLineWidth(2.5f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 0
            while (i <= 360) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val x = cos(angle.toDouble()).toFloat() * 0.20f
                val y = sin(angle.toDouble()).toFloat() * 0.20f

                GL11.glVertex2f(x, y)
                i += 5
            }
        }
        GL11.glEnd()

        // 绘制外圈
        GL11.glTranslated(0.0, 0.0, -extraHeight.toDouble())
        GL11.glLineWidth(4.0f * getExtraWidth())

        // 绘制外圈的第一半
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 15
            while (i <= 165) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val x = cos(angle.toDouble()).toFloat() * 0.27f
                val y = sin(angle.toDouble()).toFloat() * 0.27f
                GL11.glVertex2f(x, y)
                i += 5
            }
        }
        GL11.glEnd()

        // 绘制外圈的第二半
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = 195
        while (i <= 345) {
            val angle = Math.toRadians(i.toDouble()).toFloat()
            val x = cos(angle.toDouble()).toFloat() * 0.27f
            val y = sin(angle.toDouble()).toFloat() * 0.27f
            GL11.glVertex2f(x, y)
            i += 5
        }
        GL11.glEnd()

        // 绘制缺口位置的延伸线条
        GL11.glLineWidth(4.0f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINES)

        // 0度位置线条
        GL11.glVertex3f(
            cos(Math.toRadians(0.0)).toFloat() * 0.27f, sin(Math.toRadians(0.0))
                .toFloat() * 0.27f, 0.0f
        )
        GL11.glVertex3f(
            cos(Math.toRadians(0.0)).toFloat() * (0.27f + extensionLength), sin(Math.toRadians(0.0))
                .toFloat() * (0.27f + extensionLength), 0.0f
        )

        // 180度位置线条
        GL11.glVertex3f(
            cos(Math.toRadians(180.0)).toFloat() * 0.27f, sin(Math.toRadians(180.0))
                .toFloat() * 0.27f, 0.0f
        )
        GL11.glVertex3f(
            cos(Math.toRadians(180.0)).toFloat() * (0.27f + extensionLength), sin(Math.toRadians(180.0))
                .toFloat() * (0.27f + extensionLength), 0.0f
        )

        GL11.glEnd()

        // 绘制缺口边缘的短线
        GL11.glLineWidth(4.0f * getExtraWidth()) // 短线条的粗细
        GL11.glBegin(GL11.GL_LINES)

        // 15度位置的短线
        GL11.glVertex3f(
            cos(Math.toRadians(15.0)).toFloat() * 0.268f, sin(Math.toRadians(15.0))
                .toFloat() * 0.27f, 0.0f
        )
        GL11.glVertex3f(
            cos(Math.toRadians(15.0)).toFloat() * (0.27f + smallExtensionLength), sin(Math.toRadians(12.0))
                .toFloat() * (0.27f + smallExtensionLength), 0.0f
        )

        // 165度位置的短线
        GL11.glVertex3f(
            cos(Math.toRadians(165.0)).toFloat() * 0.268f, sin(Math.toRadians(165.0))
                .toFloat() * 0.27f, 0.0f
        )
        GL11.glVertex3f(
            cos(Math.toRadians(165.0)).toFloat() * (0.27f + smallExtensionLength), sin(Math.toRadians(168.0))
                .toFloat() * (0.27f + smallExtensionLength), 0.0f
        )

        // 195度位置的短线
        GL11.glVertex3f(
            cos(Math.toRadians(195.0)).toFloat() * 0.268f, sin(Math.toRadians(195.0))
                .toFloat() * 0.27f, 0.0f
        )
        GL11.glVertex3f(
            cos(Math.toRadians(195.0)).toFloat() * (0.27f + smallExtensionLength), sin(Math.toRadians(192.0))
                .toFloat() * (0.27f + smallExtensionLength), 0.0f
        )

        // 345度位置的短线
        GL11.glVertex3f(
            cos(Math.toRadians(345.0)).toFloat() * 0.268f, sin(Math.toRadians(345.0))
                .toFloat() * 0.27f, 0.0f
        )
        GL11.glVertex3f(
            cos(Math.toRadians(345.0)).toFloat() * (0.27f + smallExtensionLength), sin(Math.toRadians(348.0))
                .toFloat() * (0.27f + smallExtensionLength), 0.0f
        )

        GL11.glEnd()

        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)

        GL11.glPopMatrix()
    }

    private fun drawArisHalo() {
        animations.run(if (isReversing) 0.0 else 0.1)

        if (animations.value == 0.0) isReversing = false
        if (animations.value == 0.1) isReversing = true

        val height = mc.thePlayer.height + 0.25f + animations.value

        GL11.glPushMatrix()
        GL11.glTranslated(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + height,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        GlStateManager.enableBlend()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        color(Color(161, 253, 228, 220).rgb)

        val yaw = mc.thePlayer.rotationYaw

        // 使光环中心随玩家朝向旋转
        GL11.glRotatef(-yaw, 0f, 1f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        drawRectangle(0.20f, 0.02f, 0.26f, 0.26f, 4f, false)
        drawRectangle(0.2f, 0.3f, 0.4f, 0.4f, 6f, false)
        drawRectangle(-0.09f, 0.21f, 0.35f, 0.35f, 5f, false)
        drawRectangle(-0.13f, 0.45f, 0.15f, 0.05f, 4f, false)
        drawRectangle(0.12f, 0.49f, 0.1f, 0f, 6f, false)

        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)

        GL11.glPopMatrix()
    }

    private fun drawRectangle(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, filled: Boolean) {
        GL11.glPushMatrix()
        GL11.glTranslatef(x - 0.05f, y - 0.15f, 0.0f)

        // 绘制长方形的边缘
        if (filled) {
            GL11.glLineWidth(lineWidth * getExtraWidth())
            GL11.glBegin(GL11.GL_TRIANGLE_FAN)
            GL11.glVertex2f(-width / 2, -height / 2)
            GL11.glVertex2f(width / 2, -height / 2)
            GL11.glVertex2f(width / 2, height / 2)
            GL11.glVertex2f(-width / 2, height / 2)
            GL11.glEnd()
        } else {
            GL11.glLineWidth(lineWidth * getExtraWidth())
            GL11.glBegin(GL11.GL_LINE_LOOP)
            GL11.glVertex2f(-width / 2, -height / 2)
            GL11.glVertex2f(width / 2, -height / 2)
            GL11.glVertex2f(width / 2, height / 2)
            GL11.glVertex2f(-width / 2, height / 2)
            GL11.glEnd()
        }
        GL11.glPopMatrix()
    }

    private fun drawStar(x: Float, y: Float, radius: Float) {
        val POINTS = 5 // 五角星的5个外顶点
        val angles = FloatArray(POINTS * 2)

        // 计算每个点的角度（逆时针计算）
        for (i in 0 until POINTS * 2) {
            angles[i] = Math.toRadians((i * 360.0f / (POINTS * 2) - 90.0f).toDouble()).toFloat() // 从 -90 度开始使第一个点位于正上方
        }

        // 计算外层和内层的顶点
        val vertices = FloatArray(POINTS * 4)
        val innerRadius = radius * 0.6f // 调整内半径的比例（使边更短）

        for (i in 0 until POINTS * 2) {
            val angle = angles[i]
            val currentRadius = if ((i % 2 == 0)) radius else innerRadius
            vertices[i * 2] = x + cos(angle.toDouble()).toFloat() * currentRadius
            vertices[i * 2 + 1] = y + sin(angle.toDouble()).toFloat() * currentRadius
        }

        // 绘制五角星的边框（按顺序连接每一个点）
        GL11.glBegin(GL11.GL_LINE_LOOP)
        for (i in 0 until POINTS * 2) {
            GL11.glVertex2f(vertices[i * 2], vertices[i * 2 + 1])
        }
        GL11.glEnd()
    }

    private fun drawTriangle(x: Float, y: Float, base: Float, height: Float, rotationAngle: Float) {
        // 三角形的3个顶点
        val vertices = FloatArray(6)

        // 计算三角形的3个顶点坐标
        // 顶点1：底边的左端点
        vertices[0] = -base / 2
        vertices[1] = 0f

        // 顶点2：底边的右端点
        vertices[2] = base / 2
        vertices[3] = 0f

        // 顶点3：三角形的顶点（在底边的正上方，定义的高度）
        vertices[4] = 0f
        vertices[5] = height

        GL11.glPushMatrix()

        // 移动到三角形的中心
        GL11.glTranslatef(x, y, 0f)

        // 旋转三角形
        GL11.glRotatef(rotationAngle, 0f, 0f, 1f)

        // 绘制三角形的边框（按顺序连接每一个点）
        GL11.glBegin(GL11.GL_LINE_LOOP)
        for (i in 0..2) {
            GL11.glVertex2f(vertices[i * 2], vertices[i * 2 + 1])
        }
        GL11.glEnd()

        GL11.glPopMatrix()
    }

    private fun getExtraWidth(): Float {
        return 1f
    }

    private fun drawShiroko_TerrorHalo() {
        animations.run(if (isReversing) 0.0 else 0.1)

        if (animations.value == 0.0) isReversing = false
        if (animations.value == 0.1) isReversing = true

        val height = mc.thePlayer.height + 0.25f + animations.value

        GL11.glPushMatrix()
        GL11.glTranslated(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + height,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        color(Color(79, 112, 117, 255).rgb)

        val yaw = mc.thePlayer.rotationYaw

        // 使光环中心随玩家朝向旋转
        GL11.glRotatef(-yaw, 0f, 1f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        // 绘制内圈
        GL11.glLineWidth(2.2f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 0
            while (i <= 360) {
                GL11.glVertex2f(
                    cos(Math.toRadians(i.toDouble())).toFloat() * 0.2f,
                    sin(Math.toRadians(i.toDouble())).toFloat() * 0.2f
                )
                i += 5
            }
        }
        GL11.glEnd()

        // 绘制外圈
        GL11.glTranslated(0.0, 0.0, -0.02)
        GL11.glLineWidth(5f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 0
            while (i <= 360) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val x = cos(angle.toDouble()).toFloat() * 0.3f
                val y = sin(angle.toDouble()).toFloat() * 0.3f

                GL11.glVertex2f(x, y)
                i += 5
            }
        }
        GL11.glEnd()

        var i = 0
        while (i < 360) {
            val angle = Math.toRadians(i.toDouble()).toFloat()
            val x = cos(angle.toDouble()).toFloat() * 0.3f
            val y = sin(angle.toDouble()).toFloat() * 0.3f

            // 在每个四等分点上添加突出效果
            if (i % 90 == 0) { // 四等分点
                val offset = 0.1f
                val triangleX = x + cos(angle.toDouble()).toFloat() * offset
                val triangleY = y + sin(angle.toDouble()).toFloat() * offset

                // 计算旋转角度，使底边朝向圆心
                val rotationAngle = (i - 90).toFloat() // 使底边朝向圆心

                GL11.glLineWidth(5f * getExtraWidth())
                drawTriangle(triangleX / 1.35f, triangleY / 1.35f, 0.012f, 0.1f, rotationAngle)
            }
            i += 90
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()

        GL11.glPopMatrix()
    }

    private fun drawReisaHalo() {
        animations.run(if (isReversing) 0.0 else 0.1)

        if (animations.value == 0.0) isReversing = false
        if (animations.value == 0.1) isReversing = true

        val height = mc.thePlayer.height + 0.25f + animations.value
        GL11.glPushMatrix()
        GL11.glTranslated(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + height,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )

        GlStateManager.enableBlend()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        val yaw = mc.thePlayer.rotationYaw

        // 使光环中心随玩家朝向旋转
        GL11.glRotatef(-yaw, 0f, 1f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        color(Color(200, 200, 250, 220).rgb)

        // 绘制大五角星
        GL11.glLineWidth(3.0f * getExtraWidth())
        drawStar(0.0f, 0.0f, 0.3f) // 半径为0.3的五角星

        // 绘制小五角星
        GL11.glPushMatrix()
        GL11.glRotatef(36f, 0f, 0f, 1f) // 旋转角度，使小五角星的尖端对准外部五角星的凹陷部分
        drawStar(0.0f, 0.0f, 0.14f) // 半径为0.15的五角星
        GL11.glPopMatrix()

        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)

        GL11.glPopMatrix()
    }

    private fun drawNatsuHalo() {
        animations.run(if (isReversing) 0.0 else 0.1)

        if (animations.value == 0.0) isReversing = false
        if (animations.value == 0.1) isReversing = true

        val height = mc.thePlayer.height + 0.25f + animations.value

        GL11.glPushMatrix()
        GL11.glTranslated(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + height,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        GlStateManager.enableBlend()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        color(Color(254, 200, 200, 240).rgb)

        val yaw = mc.thePlayer.rotationYaw

        // 使光环中心随玩家朝向旋转
        GL11.glRotatef(-yaw, 0f, 1f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        // 绘制外圈
        GL11.glLineWidth(3.5f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 0
            while (i <= 360) {
                GL11.glVertex2f(
                    cos(Math.toRadians(i.toDouble())).toFloat() * 0.3f,
                    sin(Math.toRadians(i.toDouble())).toFloat() * 0.3f
                )
                i += 5
            }
        }
        GL11.glEnd()

        // 绘制内圈
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = 0
        while (i <= 360) {
            val angle = Math.toRadians(i.toDouble()).toFloat()
            val x = cos(angle.toDouble()).toFloat() * 0.15f
            val y = sin(angle.toDouble()).toFloat() * 0.15f

            // 绘制外圈的线条
            GL11.glVertex2f(x, y)

            // 在每个四等分点上添加突出效果
            if (i % 90 == 0) {
                val offset = 0.05f
                val inwardOffset = 0.05f

                // 向外突出顶点
                GL11.glVertex2f(
                    x + cos(angle.toDouble()).toFloat() * offset, y + sin(angle.toDouble())
                        .toFloat() * offset
                )
                // 回到原始顶点
                GL11.glVertex2f(x, y)
                // 向内突出顶点
                GL11.glVertex2f(
                    x - cos(angle.toDouble()).toFloat() * inwardOffset, y - sin(angle.toDouble())
                        .toFloat() * inwardOffset
                )
                // 回到原始顶点
                GL11.glVertex2f(x, y)
            }
            i += 5
        }
        GL11.glEnd()

        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)

        GL11.glPopMatrix()
    }

    private fun drawYuukaHalo() {
        animations.run(if (isReversing) 0.0 else 0.1)

        if (animations.value == 0.0) isReversing = false
        if (animations.value == 0.1) isReversing = true

        val height = mc.thePlayer.height + 0.25f + animations.value

        GL11.glPushMatrix()
        GL11.glTranslated(
            mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + height,
            mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        GlStateManager.enableBlend()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.disableTexture2D()
        GlStateManager.disableDepth()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        val yaw = mc.thePlayer.rotationYaw

        // 使光环中心随玩家朝向旋转
        GL11.glRotatef(-yaw, 0f, 1f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        // 绘制圆1
        color(Color(80, 150, 180, 250).rgb)
        GL11.glLineWidth(2f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        run {
            var i = 0
            while (i <= 360) {
                GL11.glVertex2f(
                    cos(Math.toRadians(i.toDouble())).toFloat() * 0.292f,
                    sin(Math.toRadians(i.toDouble())).toFloat() * 0.292f
                )
                i += 5
            }
        }
        GL11.glEnd()

        // 绘制圆2
        color(Color(30, 30, 30, 200).rgb)
        GL11.glLineWidth(6f * getExtraWidth())
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = 0
        while (i <= 360) {
            GL11.glVertex2f(
                cos(Math.toRadians(i.toDouble())).toFloat() * 0.3f,
                sin(Math.toRadians(i.toDouble())).toFloat() * 0.3f
            )
            i += 5
        }
        GL11.glEnd()

        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)

        GL11.glPopMatrix()
    }
}