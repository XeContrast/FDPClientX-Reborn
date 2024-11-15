/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.interpolatedPosition
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import java.awt.Color

@ModuleInfo("ForwardTrack", category = ModuleCategory.COMBAT)
object ForwardTrack : Module() {
    val espMode = ListValue("ESP-Mode", arrayOf("Box", "Model"), "Model")

    private val rainbow = BoolValue("Rainbow", true).displayable { espMode.get() == "Box" }
    private val red = IntegerValue("R", 0, 0,255).displayable { !rainbow.get() && espMode.get() == "Box" }
    private val green = IntegerValue("G", 255, 0,255).displayable { !rainbow.get() && espMode.get() == "Box" }
    private val blue = IntegerValue("B", 0, 0,255).displayable { !rainbow.get() && espMode.get() == "Box" }

    val color
        get() = if (rainbow.get()) ColorUtils.rainbow() else Color(red.get(), green.get(), blue.get())

    /**
     * Any good anti-cheat will easily detect this module.
     */
    fun includeEntityTruePos(entity: Entity, action: () -> Unit) {
        if (!handleEvents() || entity !is EntityLivingBase || entity is EntityPlayerSP)
            return

        // Would be more fun if we simulated instead.
        Backtrack.runWithSimulatedPosition(entity, usePosition(entity)) {
            action()

            null
        }
    }

    fun usePosition(entity: Entity): Vec3 {
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
        if (espMode.get() != "Box")
            return

        val renderManager = mc.renderManager

        for (target in mc.theWorld.loadedEntityList) {
            if (target is EntityPlayerSP)
                continue

            target.run {
                val vec = usePosition(this)

                val x = vec.xCoord - renderManager.renderPosX
                val y = vec.yCoord - renderManager.renderPosY
                val z = vec.zCoord - renderManager.renderPosZ

                val axisAlignedBB = entityBoundingBox.offset(-posX, -posY, -posZ).offset(x, y, z)

                drawBacktrackBox(
                    AxisAlignedBB.fromBounds(
                        axisAlignedBB.minX,
                        axisAlignedBB.minY,
                        axisAlignedBB.minZ,
                        axisAlignedBB.maxX,
                        axisAlignedBB.maxY,
                        axisAlignedBB.maxZ
                    ), color
                )
            }
        }
    }
}