/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import kevin.utils.component1
import kevin.utils.component2
import kevin.utils.component3
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.ColorSettingsInteger
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.interpolatedPosition
import net.ccbluex.liquidbounce.utils.lerpWith
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.renderPos
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

@ModuleInfo("ForwardTrack", category = ModuleCategory.COMBAT)
object ForwardTrack : Module() {
    private val espMode = ListValue("ESP-Mode", arrayOf("Box", "Model", "Wireframe"), "Model")
    private val wireframeWidth = FloatValue("WireFrame-Width", 1f, 0.5f,5f).displayable { espMode.get() == "WireFrame" }

    private val espColorMode = ListValue("ESP-Color", arrayOf("Custom", "Rainbow"), "Custom").displayable { espMode.get() != "Model" }
    private val espColor = ColorSettingsInteger(this, "ESP", withAlpha = false)
    { espColorMode.get() == "Custom" && espMode.get() != "Model" }.with(0, 255, 0)

    val color
        get() = if (espColorMode.get() == "Rainbow") rainbow() else Color(espColor.color().rgb)

    /**
     * Any good anti-cheat will easily detect this module.
     */
    fun includeEntityTruePos(entity: Entity, action: () -> Unit) {
        if (!handleEvents() || !isSelected(entity, true))
            return

        // Would be more fun if we simulated instead.
        Backtrack.runWithSimulatedPosition(entity, usePosition(entity)) {
            action()

            null
        }
    }

    private fun usePosition(entity: Entity): Vec3 {
        entity.run {
            return if (!mc.isSingleplayer) {
                val iEntity = entity as IMixinEntity

                if (iEntity.truePos) iEntity.interpolatedPosition else positionVector
            } else if (this is EntityLivingBase) {
                Vec3(newPosX, newPosY, newPosZ)
            } else positionVector
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val world = mc.theWorld ?: return

        val renderManager = mc.renderManager

        world.loadedEntityList.asSequence()
            .filter { isSelected(it, true) }
            .forEach { target ->

            target?.run {
                val vec = usePosition(this)

                val (x, y, z) = vec - renderManager.renderPos

                when (espMode.get().lowercase()) {
                    "box" -> {
                        val axisAlignedBB = entityBoundingBox.offset(-posX, -posY, -posZ).offset(x, y, z)

                        drawBacktrackBox(axisAlignedBB, color)
                    }

                    "model" -> {
                        glPushMatrix()
                        glPushAttrib(GL_ALL_ATTRIB_BITS)

                        color(0.6f, 0.6f, 0.6f, 1f)
                        renderManager.doRenderEntity(
                            this,
                            x, y, z,
                            (prevRotationYaw..rotationYaw).lerpWith(event.partialTicks),
                            event.partialTicks,
                            true
                        )

                        glPopAttrib()
                        glPopMatrix()
                    }

                    "wireframe" -> {
                        val color = if (espColorMode.get() == "Rainbow") rainbow() else Color(espColor.color().rgb)

                        glPushMatrix()
                        glPushAttrib(GL_ALL_ATTRIB_BITS)

                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                        glDisable(GL_TEXTURE_2D)
                        glDisable(GL_LIGHTING)
                        glDisable(GL_DEPTH_TEST)
                        glEnable(GL_LINE_SMOOTH)

                        glEnable(GL_BLEND)
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

                        glLineWidth(wireframeWidth.get())

                        glColor(color)
                        renderManager.doRenderEntity(
                            this,
                            x, y, z,
                            (prevRotationYaw..rotationYaw).lerpWith(event.partialTicks),
                            event.partialTicks,
                            true
                        )
                        glColor(color)
                        renderManager.doRenderEntity(
                            this,
                            x, y, z,
                            (prevRotationYaw..rotationYaw).lerpWith(event.partialTicks),
                            event.partialTicks,
                            true
                        )

                        glPopAttrib()
                        glPopMatrix()
                    }
                }
            }
        }
    }
}

operator fun Vec3.plus(vec: Vec3): Vec3 = add(vec)
operator fun Vec3.minus(vec: Vec3): Vec3 = subtract(vec)
operator fun Vec3.times(number: Double) = Vec3(xCoord * number, yCoord * number, zCoord * number)
operator fun Vec3.div(number: Double) = times(1 / number)