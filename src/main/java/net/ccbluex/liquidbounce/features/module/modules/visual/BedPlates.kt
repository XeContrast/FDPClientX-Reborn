package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.extensions.getAllInBoxMutable
import net.ccbluex.liquidbounce.extensions.set
import net.ccbluex.liquidbounce.extensions.withGCD
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GLUtils.project2D
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.Block
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@ModuleInfo(name = "BedPlates", category = ModuleCategory.VISUAL)
class BedPlates : Module() {
    val distance by FloatValue("Distance", 50f, 10f, 75f)
    private val updateRate by FloatValue("Update Rate", 1000f, 250f, 5000f)
    private val layers by IntegerValue("Layers", 5, 1, 10)
    private val beds: MutableList<BlockPos?> = ArrayList()
    private val bedBlocks: MutableList<MutableList<Block>> = ArrayList()
    private val retardedList = HashSet<BlockPos>(256)
    private val timer = MSTimer()

    val from = BlockPos.MutableBlockPos()
    val to = BlockPos.MutableBlockPos()

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        var index = 0
        var ind = 0
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        val searchCenter = player.position ?: return
        if (timer.hasTimePassed(updateRate.toLong())) {
            beds.clear()
            bedBlocks.clear()
            repeat(7) {
                beds.add(null)
                bedBlocks.add(ArrayList())
            }
            for (it in searchCenter.getAllInBoxMutable(distance.toInt())) {
                val blockState = world.getBlockState(it)
                if (blockState.block != Blocks.bed)
                    continue

                if (retardedList.contains(it))
                    continue

                val maxLayers = layers

                from.set(it, -maxLayers - 1, 0, -maxLayers - 1)
                to.set(it, maxLayers + 1, maxLayers + 1, maxLayers + 1)

                val found = find(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), ind)
                if (found) {
                    retardedList.add(it.north())
                    retardedList.add(it.south())
                    retardedList.add(it.east())
                    retardedList.add(it.west())
                    ind++
                    if (ind > 8)
                        ind = 8
                }
            }
            timer.reset()
        }

        if (beds.isNotEmpty()) {
            for (blockPos in this.beds) {
                if (blockPos == null) continue
                if (beds[index] != null) {
                    mc.entityRenderer.setupCameraTransform(event.partialTicks, 0)

                    val vectors = getDoubles(blockPos)

                    var projection: FloatArray?
                    val position = floatArrayOf(Float.MAX_VALUE, Float.MAX_VALUE, -1.0f, -1.0f)

                    for (vec in vectors) {
                        projection = project2D(
                            vec[0].toFloat(),
                            vec[1].toFloat(),
                            vec[2].toFloat(),
                            event.scaledResolution.scaleFactor
                        )
                        if (projection != null && projection[2] >= 0.0f && projection[2] < 1.0f) {
                            val pX = projection[0]
                            val pY = projection[1]
                            position[0] = min(position[0].toDouble(), pX.toDouble()).toFloat()
                            position[1] = min(position[1].toDouble(), pY.toDouble()).toFloat()
                            position[2] = max(position[2].toDouble(), pX.toDouble()).toFloat()
                            position[3] = max(position[3].toDouble(), pY.toDouble()).toFloat()
                        }
                    }

                    mc.entityRenderer.setupOverlayRendering()
                    val width = (bedBlocks[index].size * 20 + 4).toFloat()
                    val posX = position[0] - width / 2f
                    val posY = position[1]
                    Fonts.font35.drawCenteredString(
                        (mc.thePlayer.getDistanceSq(blockPos).toInt()).toString() + "m",
                        posX + width / 2,
                        posY + 4,
                        -1
                    )
                    var curX = posX + 4
                    for (block in bedBlocks[index]) {
                        val stack = ItemStack(block)
                        GlStateManager.pushMatrix()
                        GlStateManager.pushAttrib()
                        RenderHelper.enableGUIStandardItemLighting()
                        GlStateManager.disableAlpha()
                        GlStateManager.clear(256)
                        mc.renderItem.zLevel = -150.0f
                        GlStateManager.disableLighting()
                        GlStateManager.disableDepth()
                        GlStateManager.disableBlend()
                        GlStateManager.enableLighting()
                        GlStateManager.enableDepth()
                        GlStateManager.disableLighting()
                        GlStateManager.disableDepth()
                        GlStateManager.disableTexture2D()
                        GlStateManager.disableAlpha()
                        GlStateManager.disableBlend()
                        GlStateManager.enableBlend()
                        GlStateManager.enableAlpha()
                        GlStateManager.enableTexture2D()
                        GlStateManager.enableLighting()
                        GlStateManager.enableDepth()
                        mc.renderItem.renderItemIntoGUI(stack, curX.toInt(), (posY + Fonts.font35.height + 8).toInt())
                        mc.renderItem.renderItemOverlayIntoGUI(
                            mc.fontRendererObj,
                            stack,
                            curX.toInt(),
                            (posY + Fonts.font35.height + 8).toInt(),
                            null
                        )
                        mc.renderItem.zLevel = 0.0f
                        GlStateManager.enableAlpha()
                        RenderHelper.disableStandardItemLighting()
                        GlStateManager.popAttrib()
                        GlStateManager.popMatrix()
                        curX += 20f
                    }
                    mc.entityRenderer.setupOverlayRendering()
                    index++
                }
            }
        }
    }

    private fun find(x: Double, y: Double, z: Double, index: Int): Boolean {
        val bedPos = BlockPos(x, y, z)
        val bed = mc.theWorld.getBlockState(bedPos).block
        bedBlocks[index].clear()
        beds[index] = null

        if (beds.contains(bedPos)) {
            return false
        }

        val targetBlocks = arrayOf(
            Blocks.wool,
            Blocks.stained_hardened_clay,
            Blocks.stained_glass,
            Blocks.planks,
            Blocks.log,
            Blocks.log2,
            Blocks.end_stone,
            Blocks.obsidian,
            Blocks.bedrock
        ) // BW normal def blocks list ^ ^

        for (it in BlockPos.getAllInBoxMutable(from,to)) {
            for (targetBlock in targetBlocks) {
                if (mc.theWorld.getBlockState(it).block == targetBlock && !bedBlocks[index].contains(targetBlock)) {
                    bedBlocks[index].add(targetBlock)
                }
            }
        }

        if (bed == Blocks.bed) {
            beds[index] = bedPos
            return true
        }

        return false
    }

    companion object {
        private fun getDoubles(blockPos: BlockPos): Array<DoubleArray> {
            val x = blockPos.x - mc.renderManager.viewerPosX
            val y = blockPos.y - mc.renderManager.viewerPosY
            val z = blockPos.z - mc.renderManager.viewerPosZ

            val bb = AxisAlignedBB(x, y - 1, z, x, y + 1, z)

            return arrayOf(
                doubleArrayOf(bb.minX, bb.minY, bb.minZ),
                doubleArrayOf(bb.minX, bb.maxY, bb.minZ),
                doubleArrayOf(bb.minX, bb.maxY, bb.maxZ),
                doubleArrayOf(bb.minX, bb.minY, bb.maxZ),
                doubleArrayOf(bb.maxX, bb.minY, bb.minZ),
                doubleArrayOf(bb.maxX, bb.maxY, bb.minZ),
                doubleArrayOf(bb.maxX, bb.maxY, bb.maxZ),
                doubleArrayOf(bb.maxX, bb.minY, bb.maxZ)
            )
        }
    }
}
