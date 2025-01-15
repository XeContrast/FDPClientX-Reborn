/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.minecraft.block.BlockContainer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.Display
import org.lwjgl.util.glu.GLU
import java.awt.Color
import kotlin.random.Random

@ModuleInfo(name = "Stealer", category = ModuleCategory.PLAYER)
object Stealer : Module() {
    /**
     * OPTIONS
     */

    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 200, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) {
                set(i)
            }

            nextDelay = TimeUtils.randomDelay(minDelayValue.get(), get())
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 150, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()

            if (i < newValue) {
                set(i)
            }

            nextDelay = TimeUtils.randomDelay(get(), maxDelayValue.get())
        }
    }

    private val chestValue = IntegerValue("ChestOpenDelay", 300, 0, 1000)
    private val takeRandomizedValue = BoolValue("TakeRandomized", false)
    private val onlyItemsValue = BoolValue("OnlyItems", false)
    private val instantValue = BoolValue("Instant", false)
    private val stopMotionValue = BoolValue("StopMotion", false)
    private val noDuplicateValue = BoolValue("NoDuplicateNonStackable", false)
    private val noCompassValue = BoolValue("NoCompass", false)
    private val autoCloseValue = BoolValue("AutoClose", true)
    val silentTitleValue = BoolValue("SilentTitle", false)
    val silenceValue = BoolValue("SilentMode", true)
    val showStringValue = BoolValue("Silent-ShowString", true) { silenceValue.get() }
    val stillDisplayValue = BoolValue("Silent-StillDisplay", true) { silenceValue.get() }
    private val silentView by BoolValue("SilentView",true) { silenceValue.get() }

    private val autoCloseMaxDelayValue: IntegerValue = object : IntegerValue("AutoCloseMaxDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMinDelayValue.get()
            if (i > newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), this.get())
        }
    }.displayable { autoCloseValue.get() } as IntegerValue

    private val autoCloseMinDelayValue: IntegerValue = object : IntegerValue("AutoCloseMinDelay", 0, 0, 400) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = autoCloseMaxDelayValue.get()
            if (i < newValue) set(i)
            nextCloseDelay = TimeUtils.randomDelay(this.get(), autoCloseMaxDelayValue.get())
        }
    }.displayable { autoCloseValue.get() } as IntegerValue

    private val closeOnFullValue = BoolValue("CloseOnFull", true)
    private val chestTitleValue = BoolValue("ChestTitle", false)

    /**
     * VALUES
     */
    private val delayTimer = MSTimer()
    private val chestTimer = MSTimer()
    private var nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())

    private var currentContainerPos: BlockPos? = null

    private val autoCloseTimer = MSTimer()
    private var nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())

    private var contentReceived = 0
    var once = false

    override fun onDisable() {
        once = false
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (stopMotionValue.get() && mc.currentScreen is GuiChest) {
            event.x = 0.0
            event.z = 0.0
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (silentView) {
            if (mc.thePlayer.openContainer == null || mc.currentScreen == null)
                return
            val container = mc.thePlayer?.openContainer ?: return
            val slots = container.inventorySlots.size

            val scaleFactor: Int = event.scaledResolution.scaleFactor

            if (slots > 0) {
                val projection: FloatArray = calculate(currentContainerPos ?: return, scaleFactor) ?: return

                val roundX = projection[0] - (164 / 2f)
                val roundY = projection[1] / 1.5f

                GlStateManager.pushMatrix()
                GlStateManager.translate(roundX + 82, roundY + 30, 0f)
                GlStateManager.translate(-(roundX + 82), -(roundY + 30), 0f)

                RoundedUtil.drawRound(roundX, roundY, 164F, 60f, 3f, Color(0, 0, 0, 120))

                val startX = (roundX + 5).toDouble()
                val startY = (roundY + 5).toDouble()

                val itemRender = mc.renderItem

                GlStateManager.pushMatrix()
                RenderHelper.enableGUIStandardItemLighting()
                itemRender.zLevel = 200.0f

                for (slot in container.inventorySlots) {
                    if (slot.inventory != mc.thePlayer.inventory) {
                        val x = (startX + (slot.slotNumber % 9) * 18).toInt()
                        val y = (startY + (slot.slotNumber.toDouble() / 9) * 18).toInt()

                        itemRender.renderItemAndEffectIntoGUI(slot.stack, x, y)
                    }
                }

                GlStateManager.popMatrix()

                itemRender.zLevel = 0.0f
                GlStateManager.popMatrix()
                GlStateManager.disableLighting()
            }
        }
    }

    @EventTarget
    fun onTick(event: PlayerTickEvent) {
        if (event.state == EventState.PRE) {
            if (!chestTimer.hasTimePassed(chestValue.get().toLong())) {
                return
            }

            val screen = mc.currentScreen ?: return

            if (screen !is GuiChest || !delayTimer.hasTimePassed(nextDelay.toLong())) {
                autoCloseTimer.reset()
                return
            }

            // No Compass
            if (noCompassValue.get() && mc.thePlayer.inventory.getCurrentItem()?.item?.unlocalizedName == "item.compass") {
                return
            }

            // Chest title
            if (chestTitleValue.get() && (screen.lowerChestInventory == null || !screen.lowerChestInventory.name.contains(
                    ItemStack(Item.itemRegistry.getObject(ResourceLocation("minecraft:chest"))).displayName
                ))
            ) {
                return
            }

            // inventory cleaner
            val invManager = FDPClient.moduleManager[InvManager::class.java]!!

            // check if it's empty?
            if (!isEmpty(screen) && !(closeOnFullValue.get() && fullInventory)) {
                autoCloseTimer.reset()

                // Randomized
                if (takeRandomizedValue.get()) {
                    do {
                        val items = mutableListOf<Slot>()

                        for (slotIndex in 0 until screen.inventoryRows * 9) {
                            val slot = screen.inventorySlots.inventorySlots[slotIndex]

                            if (slot.stack != null && (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }
                                    .map { it.item!! }
                                    .contains(slot.stack.item)) && (!invManager.state || invManager.isUseful(
                                    slot.stack,
                                    -1
                                ))
                            )
                                items.add(slot)
                        }

                        val randomSlot = Random.nextInt(items.size)
                        val slot = items[randomSlot]

                        move(screen, slot)
                    } while (delayTimer.hasTimePassed(nextDelay.toLong()) && items.isNotEmpty())
                    return
                }

                // Non randomized
                for (slotIndex in 0 until screen.inventoryRows * 9) {
                    val slot = screen.inventorySlots.inventorySlots[slotIndex]

                    if (delayTimer.hasTimePassed(nextDelay.toLong()) && slot.stack != null &&
                        (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!invManager.state || invManager.isUseful(
                            slot.stack,
                            -1
                        ))
                    ) {
                        move(screen, slot)
                    }
                }
            } else if (autoCloseValue.get() && screen.inventorySlots.windowId == contentReceived && autoCloseTimer.hasTimePassed(
                    nextCloseDelay.toLong()
                )
            ) {
                mc.thePlayer.closeScreen()
                nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())
            }
            if (instantValue.get()) {
                if (mc.currentScreen is GuiChest) {
                    val chest = mc.currentScreen as GuiChest
                    val rows = chest.inventoryRows * 9
                    for (i in 0 until rows) {
                        val slot = chest.inventorySlots.getSlot(i)
                        if (slot.hasStack) {
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C0EPacketClickWindow(
                                    chest.inventorySlots.windowId,
                                    i,
                                    0,
                                    1,
                                    slot.stack,
                                    1.toShort()
                                )
                            )
                        }
                    }
                }
                mc.thePlayer.closeScreen()
            }
            performStealer(screen)
        }
    }

    private fun performStealer(screen: GuiScreen) {
        if (once && screen !is GuiChest) {
            // prevent a bug where the chest suddenly closed while not finishing stealing items inside, leaving cheststealer turned on alone.
            state = false
            return
        }

        if (screen !is GuiChest || !delayTimer.hasTimePassed(nextDelay.toLong())) {
            autoCloseTimer.reset()
            return
        }

        // No Compass
        if (!once && noCompassValue.get() && mc.thePlayer.inventory.getCurrentItem()?.item?.unlocalizedName == "item.compass")
            return

        // Chest title
        if (!once && chestTitleValue.get() && (screen.lowerChestInventory == null || !screen.lowerChestInventory.name.contains(
                ItemStack(Item.itemRegistry.getObject(ResourceLocation("minecraft:chest"))).displayName
            ))
        )
            return

        // inventory cleaner
        val inventoryCleaner = FDPClient.moduleManager[InvManager::class.java] as InvManager

        // Is empty?
        if (!isEmpty(screen) && !(closeOnFullValue.get() && fullInventory)) {
            autoCloseTimer.reset()

            // Randomized
            if (takeRandomizedValue.get()) {
                var noLoop = false
                do {
                    val items = mutableListOf<Slot>()

                    for (slotIndex in 0 until screen.inventoryRows * 9) {
                        val slot = screen.inventorySlots.inventorySlots[slotIndex]

                        if (slot.stack != null && (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }
                                .map { it.item!! }
                                .contains(slot.stack.item)) && (!inventoryCleaner.state || inventoryCleaner.isUseful(
                                slot.stack,
                                -1
                            ))
                        )
                            items.add(slot)
                    }

                    val randomSlot = Random.nextInt(items.size)
                    val slot = items[randomSlot]

                    move(screen, slot)
                    if (nextDelay.toLong() == 0L || delayTimer.hasTimePassed(nextDelay.toLong()))
                        noLoop = true
                } while (delayTimer.hasTimePassed(nextDelay.toLong()) && items.isNotEmpty() && !noLoop)
                return
            }

            // Non randomized
            for (slotIndex in 0 until screen.inventoryRows * 9) {
                val slot = screen.inventorySlots.inventorySlots[slotIndex]

                if (delayTimer.hasTimePassed(nextDelay.toLong()) && slot.stack != null &&
                    (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }
                        .map { it.item!! }
                        .contains(slot.stack.item)) && (!inventoryCleaner.state || inventoryCleaner.isUseful(
                        slot.stack,
                        -1
                    ))
                ) {
                    move(screen, slot)
                }
            }
        } else if (autoCloseValue.get() && screen.inventorySlots.windowId == contentReceived && autoCloseTimer.hasTimePassed(
                nextCloseDelay.toLong()
            )
        ) {
            mc.thePlayer.closeScreen()

            if (silenceValue.get() && !stillDisplayValue.get()) {
                FDPClient.hud.addNotification(
                    Notification(
                        "Closed chest.", "!!!",
                        NotifyType.INFO
                    )
                )
            }
            nextCloseDelay = TimeUtils.randomDelay(autoCloseMinDelayValue.get(), autoCloseMaxDelayValue.get())

            if (once) {
                once = false
                state = false
                return
            }
        }
    }

    @EventTarget
    private fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S30PacketWindowItems) {
            contentReceived = packet.func_148911_c()
        }

        if (packet is S2DPacketOpenWindow) {
            chestTimer.reset()
        }

        if (silentView) {
            if (packet is C08PacketPlayerBlockPlacement) {
                if (packet.position != null) {
                    val block = mc.theWorld.getBlockState(packet.position).block
                    if (block is BlockContainer) {
                        currentContainerPos = packet.position
                    }
                }
            }
        }
    }

    private fun move(screen: GuiChest, slot: Slot) {
        screen.handleMouseClick(slot, slot.slotNumber, 0, 1)
        delayTimer.reset()
        nextDelay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    }

    private fun isEmpty(chest: GuiChest): Boolean {
        val inventoryCleaner = FDPClient.moduleManager[InvManager::class.java] as InvManager

        for (i in 0 until chest.inventoryRows * 9) {
            val slot = chest.inventorySlots.inventorySlots[i]

            if (slot.stack != null && (!onlyItemsValue.get() || slot.stack.item !is ItemBlock) && (!noDuplicateValue.get() || slot.stack.maxStackSize > 1 || !mc.thePlayer.inventory.mainInventory.filter { it != null && it.item != null }
                    .map { it.item!! }
                    .contains(slot.stack.item)) && (!inventoryCleaner.state || inventoryCleaner.isUseful(
                    slot.stack,
                    -1
                ))
            )
                return false
        }

        return true
    }

    fun calculate(blockPos: BlockPos, factor: Int): FloatArray? {
        try {
            val renderX = mc.renderManager.renderPosX
            val renderY = mc.renderManager.renderPosY
            val renderZ = mc.renderManager.renderPosZ

            val x = blockPos.x + 0.5f - renderX
            val y = blockPos.y + 0.5f - renderY
            val z = blockPos.z + 0.5f - renderZ

            val projectedCenter: FloatArray = project(x, y, z, factor)!!
            if (projectedCenter[2] >= 0.0 && projectedCenter[2] < 1.0) {
                return floatArrayOf(projectedCenter[0], projectedCenter[1], projectedCenter[0], projectedCenter[1])
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    private fun project(x: Double, y: Double, z: Double, factor: Int): FloatArray? {
        if (GLU.gluProject(
                x.toFloat(),
                y.toFloat(),
                z.toFloat(),
                ActiveRenderInfo.MODELVIEW,
                ActiveRenderInfo.PROJECTION,
                ActiveRenderInfo.VIEWPORT,
                ActiveRenderInfo.OBJECTCOORDS
            )
        ) {
            return floatArrayOf(
                (ActiveRenderInfo.OBJECTCOORDS[0] / factor),
                ((Display.getHeight() - ActiveRenderInfo.OBJECTCOORDS[1]) / factor),
                ActiveRenderInfo.OBJECTCOORDS[2]
            )
        }
        return null
    }

    private val fullInventory: Boolean
        get() = mc.thePlayer.inventory.mainInventory.none { it == null }
}
