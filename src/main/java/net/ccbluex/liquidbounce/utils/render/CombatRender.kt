package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.DOUBLE_PI
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorBlueTwoValue
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorBlueValue
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorGreenTwoValue
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorGreenValue
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorRedTwoValue
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.colorRedValue
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.start
import net.ccbluex.liquidbounce.features.module.modules.visual.CombatVisuals.startTime
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.render.DrRenderUtils.resetColor
import net.ccbluex.liquidbounce.utils.MathUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.RenderUtils.color
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableSmoothLine
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawAxisAlignedBB
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableSmoothLine
import net.ccbluex.liquidbounce.utils.render.RenderUtils.endSmooth
import net.ccbluex.liquidbounce.utils.render.RenderUtils.getGradientOffset
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.interpolate
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetCaps
import net.ccbluex.liquidbounce.utils.render.RenderUtils.startSmooth
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.popMatrix
import net.minecraft.client.renderer.GlStateManager.pushMatrix
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import kotlin.math.*

object CombatRender: MinecraftInstance() {
    private val glowCircle = ResourceLocation("fdpclient/glow_circle.png")

    fun drawEntityBoxESP(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        pushMatrix()
        glBlendFunc(770, 771)
        enableGlCap(3042)
        disableGlCap(3553, 2929)
        glDepthMask(false)
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
        val entityBox = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )
        glTranslated(x, y, z)
        glRotated(-entity.rotationYawHead.toDouble(), 0.0, 1.0, 0.0)
        glTranslated(-x, -y, -z)
        glLineWidth(3.0f)
        enableGlCap(2848)
        glColor(0, 0, 0, 255)
        RenderGlobal.drawSelectionBoundingBox(axisAlignedBB)
        glLineWidth(1.0f)
        enableGlCap(2848)
        glColor(color.red, color.green, color.blue, 255)
        RenderGlobal.drawSelectionBoundingBox(axisAlignedBB)
        resetColor()
        glDepthMask(true)
        resetCaps()
        popMatrix()
    }

    /**
     * Draws a visual effect around the specified entity in 3D space.
     *
     * @param event The render event containing the partial tick time for smooth rendering.
     */
    fun drawZavz(entity: EntityLivingBase, event: Render3DEvent, dual: Boolean) {
        val speed = 0.1f

        val ticks = event.partialTicks
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)

        startSmooth()

        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glLineWidth(2.0f)
        glBegin(GL_LINE_STRIP)

        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * ticks - mc.renderManager.renderPosX
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * ticks - mc.renderManager.renderPosZ
        var y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ticks - mc.renderManager.renderPosY

        val radius = 0.65
        val precision = 360

        var startPos = start % 360

        start += speed

        for (i in 0..precision) {
            val posX = x + radius * cos(startPos + i * DOUBLE_PI / (precision / 2.0))
            val posZ = z + radius * sin(startPos + i * DOUBLE_PI / (precision / 2.0))

            glColor(
                getGradientOffset(
                    Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), Color(
                        colorRedTwoValue.get(), colorGreenTwoValue.get(), colorBlueTwoValue.get(), 1
                    ), abs((System.currentTimeMillis() / 10L).toDouble()) / 100.0 + y
                )
            )

            glVertex3d(posX, y, posZ)

            y += entity.height / precision

            glColor(0, 0, 0, 0)
        }

        glEnd()
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)

        endSmooth()

        glEnable(GL_TEXTURE_2D)
        glPopMatrix()

        if (dual) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)

            startSmooth()

            glDisable(GL_DEPTH_TEST)
            glDepthMask(false)
            glLineWidth(2.0f)
            glBegin(GL_LINE_STRIP)

            startPos = start % 360

            start += speed

            y =
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ticks - mc.renderManager.renderPosY + entity.height

            for (i in 0..precision) {
                val posX = x + radius * cos(-(startPos + i * DOUBLE_PI / (precision / 2.0)))
                val posZ = z + radius * sin(-(startPos + i * DOUBLE_PI / (precision / 2.0)))

                glColor(
                    getGradientOffset(
                        Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()),
                        Color(colorRedTwoValue.get(), colorGreenTwoValue.get(), colorBlueTwoValue.get(), 1),
                        abs((System.currentTimeMillis() / 10L).toDouble()) / 100.0 + y
                    )
                )

                glVertex3d(posX, y, posZ)

                y -= entity.height / precision

                glColor(0, 0, 0, 0)
            }

            glEnd()
            glDepthMask(true)
            glEnable(GL_DEPTH_TEST)

            endSmooth()

            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    fun drawPlatformESP(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        val axisAlignedBB = entity.entityBoundingBox.offset(-entity.posX, -entity.posY, -entity.posZ).offset(
            (entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * (timer.renderPartialTicks.toDouble()))) - renderManager.renderPosX,
            (entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * (timer.renderPartialTicks.toDouble()))) - renderManager.renderPosY,
            (entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * (timer.renderPartialTicks.toDouble()))) - renderManager.renderPosZ
        )
        drawAxisAlignedBB(
            AxisAlignedBB(
                axisAlignedBB.minX,
                axisAlignedBB.maxY - 0.5,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.maxZ
            ), color
        )
    }

    /**
     * Draws an ESP (Extra Sensory Perception) effect around the given entity.
     *
     * @param entity The entity to draw the ESP effect around.
     * @param color The color of the ESP effect.
     * @param e The Render3DEvent containing partial ticks for interpolation.
     */
    fun drawCrystal(entity: EntityLivingBase, color: Int, e: Render3DEvent) {
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * e.partialTicks - mc.renderManager.renderPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * e.partialTicks - mc.renderManager.renderPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * e.partialTicks - mc.renderManager.renderPosZ
        val radius = 0.15f
        val side = 4

        glPushMatrix()
        glTranslated(x, y + 2, z)
        glRotatef(-entity.width, 0.0f, 1.0f, 0.0f)

        glColor(color)
        enableSmoothLine(1.5f)

        val c = Cylinder()
        glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
        c.drawStyle = 100012
        glColor(if ((entity.hurtTime <= 0)) Color(80, 255, 80, 200) else Color(255, 0, 0, 200))
        c.draw(0.0f, radius, 0.3f, side, 1)
        c.drawStyle = 100012

        glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0.0f, 0.3f, side, 1)

        glRotatef(90.0f, 0.0f, 0.0f, 1.0f)
        c.drawStyle = 100011

        glTranslated(0.0, 0.0, -0.3)
        glColor(color)
        c.draw(0.0f, radius, 0.3f, side, 1)
        c.drawStyle = 100011

        glTranslated(0.0, 0.0, 0.3)
        c.draw(radius, 0.0f, 0.3f, side, 1)

        disableSmoothLine()
        glPopMatrix()
    }

    @JvmStatic
    fun drawOnBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        drawRect(x, y, x2, y2, color2)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glColor(color1)
        glLineWidth(width)
        glBegin(1)
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }
    fun drawjello(color: Color) {
        val it = FDPClient.combatManager.target
        val drawTime = (System.currentTimeMillis() % 2000).toInt()
        val drawMode=drawTime>1000
        var drawPercent=drawTime/1000.0
        //true when goes up
        if(!drawMode){
            drawPercent=1-drawPercent
        }else{
            drawPercent-=1
        }
        drawPercent=EaseUtils.easeInOutQuad(drawPercent)
        val points = mutableListOf<Vec3>()
        val bb= it!!.entityBoundingBox
        val radius=bb.maxX-bb.minX
        val height=bb.maxY-bb.minY
        val posX = it.lastTickPosX + (it.posX - it.lastTickPosX) * mc.timer.renderPartialTicks
        var posY = it.lastTickPosY + (it.posY - it.lastTickPosY) * mc.timer.renderPartialTicks
        if(drawMode){
            posY-=0.5
        }else{
            posY+=0.5
        }
        val posZ = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * mc.timer.renderPartialTicks
        for(i in 0..360 step 7){
            points.add(Vec3(posX - sin(i * Math.PI / 180F) * radius,posY+height*drawPercent,posZ + cos(i * Math.PI / 180F) * radius))
        }
        points.add(points[0])
        //draw
        mc.entityRenderer.disableLightmap()
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glBegin(GL_LINE_STRIP)
        val baseMove=(if(drawPercent>0.5){1-drawPercent}else{drawPercent})*2
        val min=(height/60)*20*(1-baseMove)*(if(drawMode){-1}else{1})
        for(i in 0..20) {
            var moveFace=(height/60F)*i*baseMove
            if(drawMode){
                moveFace=-moveFace
            }
            val firstPoint=points[0]
            glVertex3d(
                firstPoint.xCoord - mc.renderManager.viewerPosX, firstPoint.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                firstPoint.zCoord - mc.renderManager.viewerPosZ
            )
            glColor4f(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(),0.7F*(i/20F))
            for (vec3 in points) {
                glVertex3d(
                    vec3.xCoord - mc.renderManager.viewerPosX, vec3.yCoord - moveFace - min - mc.renderManager.viewerPosY,
                    vec3.zCoord - mc.renderManager.viewerPosZ
                )
            }
            glColor4f(0F, 0F, 0F,0F)
        }
        glEnd()
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    val PI2: Float = (Math.PI * 2.0).roundToInt().toFloat()
    var ticks: Double = 0.0
    private var lastFrame: Long = 0

    fun drawCircle(entity: Entity, partialTicks: Float, rad: Double, color: Int, alpha: Float) {
        /*Got this from the people i made the Gui for*/
        ticks += .004 * (System.currentTimeMillis() - lastFrame)

        lastFrame = System.currentTimeMillis()

        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        GlStateManager.color(1f, 1f, 1f, 1f)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glShadeModel(GL_SMOOTH)
        GlStateManager.disableCull()

        val x = interpolate(
            entity.lastTickPosX,
            entity.posX,
            mc.timer.renderPartialTicks.toDouble()
        ) - mc.renderManager.renderPosX
        val y = interpolate(
            entity.lastTickPosY,
            entity.posY,
            mc.timer.renderPartialTicks.toDouble()
        ) - mc.renderManager.renderPosY + sin(ticks) + 1
        val z = interpolate(
            entity.lastTickPosZ,
            entity.posZ,
            mc.timer.renderPartialTicks.toDouble()
        ) - mc.renderManager.renderPosZ

        glBegin(GL_TRIANGLE_STRIP)

        run {
            var i = 0f
            while (i < (Math.PI * 2)) {
                val vecX = x + rad * cos(i.toDouble())
                val vecZ = z + rad * sin(i.toDouble())

                color(color, 0f)

                glVertex3d(vecX, y - sin(ticks + 1) / 2.7f, vecZ)

                color(color, .52f * alpha)


                glVertex3d(vecX, y, vecZ)
                i += ((Math.PI * 2) / 64f).toFloat()
            }
        }

        glEnd()


        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        glLineWidth(1.5f)
        glBegin(GL_LINE_STRIP)
        GlStateManager.color(1f, 1f, 1f, 1f)
        color(color, .5f * alpha)
        for (i in 0..180) {
            glVertex3d(x - sin((i * (Math.PI * 2.0).roundToInt() / 90).toDouble()) * rad, y, z + cos((i * (Math.PI * 2.0).roundToInt() / 90).toDouble()) * rad)
        }
        glEnd()

        glShadeModel(GL_FLAT)
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
        GlStateManager.enableCull()
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
        glColor4f(1f, 1f, 1f, 1f)
    }

    fun points(target: Entity) {
        val markerX: Double = MathUtils.interporate(mc.timer.renderPartialTicks.toDouble(), target.lastTickPosX, target.posX)
        val markerY: Double = MathUtils.interporate(
            mc.timer.renderPartialTicks.toDouble(),
            target.lastTickPosY,
            target.posY
        ) + target.height / 1.6f
        val markerZ: Double = MathUtils.interporate(mc.timer.renderPartialTicks.toDouble(), target.lastTickPosZ, target.posZ)
        var time: Float =
            ((((System.currentTimeMillis() - startTime) / 1500f)) + (sin((((System.currentTimeMillis() - startTime) / 1500f)).toDouble()) / 10f)).toFloat()
        val alpha = 0.5f * 1
        var pl = 0f
        var fa = false
        for (iteration in 0..2) {
            var i = time * 360
            while (i < time * 360 + 90) {
                val max = time * 360 + 90
                val dc: Float = MathUtils.normalize(i, time * 360 - 45, max)
                val rf = 0.6f
                val radians = Math.toRadians(i.toDouble())
                val plY = pl + sin(radians * 1.2f) * 0.1f
                val firstColor = ColorUtils.rainbow(0).rgb
                val secondColor = ColorUtils.rainbow(90).rgb
                pushMatrix()
                RenderUtils.setupOrientationMatrix(markerX, markerY, markerZ)

                val idk = floatArrayOf(mc.renderManager.playerViewY, mc.renderManager.playerViewX)

                glRotated(-idk[0].toDouble(), 0.0, 1.0, 0.0)
                glRotated(idk[1].toDouble(), 1.0, 0.0, 0.0)

                GlStateManager.depthMask(false)
                val q =
                    ((if (!fa) 0.25f else 0.15f) * (max(
                        (if (fa) 0.25f else 0.15f).toDouble(),
                        (if (fa) dc else (1f + (0.4f - dc)) / 2f).toDouble()
                    ) + 0.45f)).toFloat()
                val size = q * (2f + ((0.5f - alpha) * 2))
                RenderUtils.drawImage(
                    glowCircle,
                    (cos(radians) * rf - size / 2f),
                    (plY - 0.7),
                    (sin(radians) * rf - size / 2f), size.toDouble(), size.toDouble(),
                    firstColor,
                    secondColor,
                    secondColor,
                    firstColor
                )
                glEnable(GL_DEPTH_TEST)
                GlStateManager.depthMask(true)
                popMatrix()
                i += 2f
            }
            time *= -1.025f
            fa = !fa
            pl += 0.45f
        }
    }
}