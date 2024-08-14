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
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.render.DrRenderUtils.resetColor
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.RenderUtils.*
import net.minecraft.client.renderer.GlStateManager.popMatrix
import net.minecraft.client.renderer.GlStateManager.pushMatrix
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.glu.Cylinder
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object CombatRender: MinecraftInstance() {

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
}