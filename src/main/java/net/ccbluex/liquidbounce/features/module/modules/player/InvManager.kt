package net.ccbluex.liquidbounce.features.module.modules.player

import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.access.IItemStack
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils.canPlaceBlock
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.item.ArmorComparator.getBestArmorSet
import net.ccbluex.liquidbounce.utils.item.ArmorPiece
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.item.ItemUtils.isSplashPotion
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityLiving.getArmorPosition
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.potion.Potion

@ModuleInfo(name = "InvManager", category = ModuleCategory.PLAYER)
object InvManager : Module() {

    /**
     * OPTIONS
     */
    private val instantValue = BoolValue("Instant", false)
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 600, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minCPS = minDelayValue.get()
            if (minCPS > newValue) set(minCPS)
        }
    }.displayable { !instantValue.get() } as IntegerValue

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 400, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelay = maxDelayValue.get()
            if (maxDelay < newValue) set(maxDelay)
        }
    }.displayable { !instantValue.get() } as IntegerValue
    private val delayValue = IntegerValue("OpenDelay", 0, 0, 1000)
    private val invTimer = MSTimer()

    private val invOpenValue = BoolValue("InvOpen", false)
    private val simulateInventory = BoolValue("InvSpoof", true)
    private val simulateDelayValue = IntegerValue("InvSpoof", 0, 0, 1000).displayable { simulateInventory.get() }
    private val noMoveValue = BoolValue("NoMove", false)
    private val hotbarValue = BoolValue("Hotbar", true)
    private val randomSlotValue = BoolValue("RandomSlot", false)
    private val sortValue = BoolValue("Sort", true)
    private val throwValue = BoolValue("Drop", true)
    private val armorValue = BoolValue("Armor", true)
    private val armorHotbarValue = BoolValue("ArmorHotbar", true)
    private val silentArmorHotbarValue = BoolValue("SilentArmorHotbar",true).displayable { armorHotbarValue.get() }
    private val noCombatValue = BoolValue("NoCombat", false)
    private val itemDelayValue = IntegerValue("ItemDelay", 0, 0, 5000)
    private val swingValue = BoolValue("Swing", true)
    private val nbtGoalValue =
        ListValue("NBTGoal", ItemUtils.EnumNBTPriorityType.entries.map { it.toString() }.toTypedArray(), "NONE")
    private val nbtWeaponPriority =
        FloatValue("NBTWeaponPriority", 0f, 0f, 5f).displayable { !nbtGoalValue.equals("NONE") }
    private val ignoreVehiclesValue = BoolValue("IgnoreVehicles", false)
    private val onlyGoodPotions = BoolValue("OnlyGoodPotion", false)
//    private val ignoreDurabilityUnder = FloatValue("IgnoreDurabilityUnder", 0.3f, 0f, 1f)

    private val items = arrayOf(
        "None",
        "Ignore",
        "Sword",
        "Bow",
        "Pickaxe",
        "Axe",
        "Food",
        "Block",
        "Water",
        "Gapple",
        "Pearl",
        "Potion",
        "Missile",
        "Rod"
    )
    private val maxmiss = IntegerValue("MaxMissile", 128, 0, 2304)
    private val maxblock = IntegerValue("MaxBlock", 128, 0, 2304)
    private val maxarrow = IntegerValue("MaxArrow", 128, 0, 2304)
    private val maxfood = IntegerValue("MaxFood", 128, 0, 2304)
    private val maxRod = IntegerValue("MaxRod", 1,0,36)
    private val sortSlot1Value = ListValue("SortSlot-1", items, "Sword").displayable { sortValue.get() }
    private val sortSlot2Value = ListValue("SortSlot-2", items, "Gapple").displayable { sortValue.get() }
    private val sortSlot3Value = ListValue("SortSlot-3", items, "Potion").displayable { sortValue.get() }
    private val sortSlot4Value = ListValue("SortSlot-4", items, "Pickaxe").displayable { sortValue.get() }
    private val sortSlot5Value = ListValue("SortSlot-5", items, "Axe").displayable { sortValue.get() }
    private val sortSlot6Value = ListValue("SortSlotF-6", items, "None").displayable { sortValue.get() }
    private val sortSlot7Value = ListValue("SortSlot-7", items, "Block").displayable { sortValue.get() }
    private val sortSlot8Value = ListValue("SortSlot-8", items, "Pearl").displayable { sortValue.get() }
    private val sortSlot9Value = ListValue("SortSlot-9", items, "Food").displayable { sortValue.get() }

    private val openInventory: Boolean
        get() = mc.currentScreen !is GuiInventory && simulateInventory.get()

    /**
     * means of simulating inventory
     */
    private var invOpened = false
        set(value) {
            if (value != field) {
                if (value) {
                    InventoryUtils.openPacket()
                } else {
                    InventoryUtils.closePacket()
                }
            }
            field = value
        }

    private val goal: ItemUtils.EnumNBTPriorityType
        get() = ItemUtils.EnumNBTPriorityType.valueOf(nbtGoalValue.get())

    private var delay = 0L
    private val simDelayTimer = MSTimer()

    override fun onDisable() {
        invOpened = false
    }

    private fun checkOpen(): Boolean {
        if (!invOpened && openInventory) {
            invOpened = true
            simDelayTimer.reset()
            return true
        }

        return !simDelayTimer.hasTimePassed(simulateDelayValue.get().toLong())
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C16PacketClientStatus) {
            invTimer.reset()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!invTimer.hasTimePassed(delayValue.get().toLong())) {
            return
        }

        if (!InventoryUtils.CLICK_TIMER.hasTimePassed(delay) && !instantValue.get()) {
            return
        }
        if (armorHotbarValue.get()) {
            val bestArmor = getBestArmorSet(mc.thePlayer.openContainer.inventory) ?: return

            for (i in 0..3) {
                if (mc.currentScreen == null) {
                    val (index, stack) = bestArmor[i] ?: continue

                    val armorPos = getArmorPosition(stack) - 1

                    if (mc.thePlayer.inventory.armorInventory[armorPos] != null)
                        continue

                    val hotbarIndex = index?.toHotbarIndex(mc.thePlayer.openContainer.inventory.size) ?: continue

                    if (!silentArmorHotbarValue.get()) {
                        mc.thePlayer.inventory.currentItem = hotbarIndex
                        if (mc.thePlayer.inventory.currentItem == hotbarIndex) PacketUtils.sendPacket(
                            C08PacketPlayerBlockPlacement(stack)
                        )
                    } else {
                        if (hotbarIndex in 0..8) {
                            PacketUtils.sendPackets(
                                C09PacketHeldItemChange(hotbarIndex),
                                C08PacketPlayerBlockPlacement(stack),
                                C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem)
                            )
                        }
                    }

                    mc.thePlayer.inventory.armorInventory[armorPos] = stack
                    mc.thePlayer.inventory.mainInventory[hotbarIndex] = null

                    if (!instantValue.get()) {
                        delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get()).toLong()
                        return
                    }
                }
            }
        }
        if (noMoveValue.get() && isMoving ||
            mc.thePlayer.openContainer != null && mc.thePlayer.openContainer.windowId != 0 ||
            (FDPClient.combatManager.inCombat && noCombatValue.get())
        ) {
            if (InventoryUtils.CLICK_TIMER.hasTimePassed(simulateDelayValue.get().toLong())) {
                invOpened = false
            }
            return
        }

        if ((mc.currentScreen !is GuiInventory && invOpenValue.get())) return

        if (armorValue.get()) {
            // Find best armor
            val bestArmor = getBestArmorSet(mc.thePlayer.openContainer.inventory) ?: return

            // Swap armor
            for (i in 0..3) {
                val (index, stack) = bestArmor[i] ?: continue

                when (mc.thePlayer.openContainer.inventory[i + 5]) {
                    stack -> {
                        continue
                    }
                    null -> {
                        mc.playerController?.windowClick(
                            mc.thePlayer.openContainer.windowId, index
                                ?: return, 0, 1, mc.thePlayer
                        )
                    }

                    else -> {
                        mc.playerController?.windowClick(mc.thePlayer.openContainer.windowId, i + 5, 0, 4, mc.thePlayer)
                        mc.playerController?.windowClick(
                            mc.thePlayer.openContainer.windowId, index
                                ?: return, 0, 1, mc.thePlayer
                        )
                    }
                }
                if (!instantValue.get()) {
                    delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get()).toLong()
                    return
                }
            }
        }

        if (sortValue.get()) {
            for (index in 0..8) {
                val bestItem = findBetterItem(index, mc.thePlayer.inventory.getStackInSlot(index)) ?: continue

                if (bestItem != index) {
                    if (checkOpen()) {
                        return
                    }

                    mc.playerController.windowClick(
                        0,
                        if (bestItem < 9) bestItem + 36 else bestItem,
                        index,
                        2,
                        mc.thePlayer
                    )

                    if (!instantValue.get()) {
                        delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get()).toLong()
                        return
                    }
                }
            }
        }

        if (throwValue.get()) {
            val garbageItems = items(5, if (hotbarValue.get()) 45 else 36)
                .filter { !isUseful(it.value, it.key) }
                .keys

            val garbageItem = if (garbageItems.isNotEmpty()) {
                if (randomSlotValue.get()) {
                    // pick random one
                    garbageItems.toList()[RandomUtils.nextInt(0, garbageItems.size)]
                } else {
                    garbageItems.first()
                }
            } else {
                null
            }
            if (garbageItem != null) {
                // Drop all useless items
                    if (checkOpen()) {
                        return
                    }

                if (swingValue.get()) mc.thePlayer.swingItem()

                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, garbageItem, 1, 4, mc.thePlayer)

                if (!instantValue.get()) {
                    delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get()).toLong()
                    return
                }
            }
        }

        if (InventoryUtils.CLICK_TIMER.hasTimePassed(simulateDelayValue.get().toLong())) {
            invOpened = false
        }
    }

    private fun Int.toHotbarIndex(stacksSize: Int): Int {
        val parsed = this - stacksSize + 9

        return if (parsed in 0..8) parsed else -1
    }


    /**
     * Checks if the item is useful
     *
     * @param slot Slot id of the item. If the item isn't in the inventory -1
     * @return Returns true when the item is useful
     */
    fun isUseful(itemStack: ItemStack, slot: Int): Boolean {
        return try {
            when (val item = itemStack.item) {
                is ItemSword, is ItemTool -> {

                    if (slot >= 36 && findBetterItem(
                            slot - 36,
                            mc.thePlayer.inventory.getStackInSlot(slot - 36)
                        ) == slot - 36
                    ) {
                        return true
                    }

                    for (i in 0..8) {
                        if (type(i).equals("sword", true) && item is ItemSword ||
                            type(i).equals("pickaxe", true) && item is ItemPickaxe ||
                            type(i).equals("axe", true) && item is ItemAxe
                        ) {
                            if (findBetterItem(i, mc.thePlayer.inventory.getStackInSlot(i)) == null) {
                                return true
                            }
                        }
                    }

                    val damage = (itemStack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount
                        ?: 0.0) + ItemUtils.getWeaponEnchantFactor(itemStack, nbtWeaponPriority.get(), goal)

                    return items(0, 45).none { (_, stack) ->
                        if (stack != itemStack && stack.javaClass == itemStack.javaClass) {
                            val dmg = (stack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount
                                ?: 0.0) + ItemUtils.getWeaponEnchantFactor(stack, nbtWeaponPriority.get(), goal)
                            if (damage == dmg) {
                                val currDamage = item.getDamage(itemStack)
                                currDamage >= stack.item.getDamage(stack)
                            } else damage < dmg
                        } else {
                            false
                        }
                    }
                }
                is ItemBow -> {
                    val currPower = ItemUtils.getEnchantment(itemStack, Enchantment.power)
                    return items().none { (_, stack) ->
                        if (itemStack != stack && stack.item is ItemBow) {
                            val power = ItemUtils.getEnchantment(stack, Enchantment.power)

                            if (currPower == power) {
                                val currDamage = item.getDamage(itemStack)
                                currDamage >= stack.item.getDamage(stack)
                            } else currPower < power
                        } else {
                            false
                        }
                    }
                }
                is ItemArmor -> {
                    val stacks = mc.thePlayer.openContainer.inventory
                    return itemStack in getBestArmorSet(stacks, null)!!
                }
                is ItemFlintAndSteel -> {
                    val currDamage = item.getDamage(itemStack)
                    return items().none { (_, stack) ->
                        itemStack != stack && stack.item is ItemFlintAndSteel && currDamage >= stack.item.getDamage(stack)
                    }
                }
                is ItemSnowball, is ItemEgg -> return amount[0] <= maxmiss.get()
                is ItemFood -> return amount[3] <= maxfood.get()
                is ItemBlock -> return !InventoryUtils.isBlockListBlock(item) && amount[1] <= maxblock.get()
                is ItemPotion -> return isUsefulPotion(itemStack)
                is ItemBoat,is ItemMinecart -> return ignoreVehiclesValue.get()
                is ItemFishingRod -> return mc.thePlayer?.openContainer?.inventory?.count { it?.item is ItemFishingRod }!! < maxRod.get()
                is ItemBed, is ItemEnderPearl,is ItemBucket -> return true
            }

            when (itemStack.unlocalizedName) {
                "item.compass" -> {
                    return items(0, 45).none { (_, stack) -> itemStack != stack && stack.unlocalizedName == "item.compass" }
                }
                "item.arrow" -> return amount[2] <= maxarrow.get()
                "item.slimeball" -> return true
                else -> {return false}
            }
        } catch (ex: Exception) {
            ClientUtils.logError("(InvManager) Failed to check item: ${itemStack.unlocalizedName}.", ex)
            true
        }
    }

    private fun isUsefulPotion(stack: ItemStack?): Boolean {
        val NEGATIVE_EFFECT_IDS = intArrayOf(
            Potion.moveSlowdown.id, Potion.digSlowdown.id, Potion.harm.id, Potion.confusion.id, Potion.blindness.id,
            Potion.hunger.id, Potion.weakness.id, Potion.poison.id, Potion.wither.id,
        )
        val item = stack?.item ?: return false

        if (item !is ItemPotion) return false

        val isSplash = stack.isSplashPotion()
        val isHarmful = item.getEffects(stack)?.any { it.potionID in NEGATIVE_EFFECT_IDS } ?: return false

        // Only keep helpful potions and, if 'onlyGoodPotions' is disabled, also splash harmful potions
        return !isHarmful || (!onlyGoodPotions.get() && isSplash)
    }

    private fun findBetterItem(targetSlot: Int, slotStack: ItemStack?): Int? {
        val type = type(targetSlot)

        when (type.lowercase()) {
            "sword", "pickaxe", "axe" -> {
                val currentType: Class<out Item> = when {
                    type.equals("Sword", ignoreCase = true) -> ItemSword::class.java
                    type.equals("Pickaxe", ignoreCase = true) -> ItemPickaxe::class.java
                    type.equals("Axe", ignoreCase = true) -> ItemAxe::class.java
                    else -> return null
                }

                var bestWeapon = if (slotStack?.item?.javaClass == currentType) {
                    targetSlot
                } else {
                    -1
                }

                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (itemStack != null && itemStack.item.javaClass == currentType && !type(index).equals(
                            type,
                            ignoreCase = true
                        )
                    ) {
                        if (bestWeapon == -1) {
                            bestWeapon = index
                        } else {
                            val currDamage = (itemStack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount
                                ?: 0.0) + ItemUtils.getWeaponEnchantFactor(itemStack, nbtWeaponPriority.get(), goal)

                            val bestStack = mc.thePlayer.inventory.getStackInSlot(bestWeapon) ?: return@forEachIndexed
                            val bestDamage = (bestStack.attributeModifiers["generic.attackDamage"].firstOrNull()?.amount
                                ?: 0.0) + ItemUtils.getWeaponEnchantFactor(bestStack, nbtWeaponPriority.get(), goal)

                            if (bestDamage < currDamage) {
                                bestWeapon = index
                            }
                        }
                    }
                }

                return if (bestWeapon != -1 || bestWeapon == targetSlot) bestWeapon else null
            }

            "bow" -> {
                var bestBow = if (slotStack?.item is ItemBow) targetSlot else -1
                var bestPower = if (bestBow != -1) {
                    ItemUtils.getEnchantment(slotStack!!, Enchantment.power)
                } else {
                    0
                }

                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, itemStack ->
                    if (itemStack?.item is ItemBow && !type(index).equals(type, ignoreCase = true)) {
                        if (bestBow == -1) {
                            bestBow = index
                        } else {
                            val power = ItemUtils.getEnchantment(itemStack, Enchantment.power)

                            if (ItemUtils.getEnchantment(itemStack, Enchantment.power) > bestPower) {
                                bestBow = index
                                bestPower = power
                            }
                        }
                    }
                }

                return if (bestBow != -1) bestBow else null
            }

            "food" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemFood && item !is ItemAppleGold && !type(index).equals("Food", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemFood

                        return if (replaceCurr) index else null
                    }
                }
            }

            "block" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemBlock && !InventoryUtils.BLOCK_BLACKLIST.contains(item.block) &&
                        !type(index).equals("Block", ignoreCase = true)
                    ) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemBlock

                        return if (replaceCurr) index else null
                    }
                }
            }

            "water" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemBucket && item.isFull == Blocks.flowing_water && !type(index).equals(
                            "Water",
                            ignoreCase = true
                        )
                    ) {
                        val replaceCurr =
                            slotStack == null || slotStack.item !is ItemBucket || (slotStack.item as ItemBucket).isFull != Blocks.flowing_water

                        return if (replaceCurr) index else null
                    }
                }
            }

            "gapple" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemAppleGold && !type(index).equals("Gapple", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemAppleGold

                        return if (replaceCurr) index else null
                    }
                }
            }

            "missile" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if ((item is ItemEgg || item is ItemSnowball) && !type(index).equals(
                            "Missile",
                            ignoreCase = true
                        )
                    ) {
                        val replaceCurr =
                            slotStack == null || (slotStack.item !is ItemEgg && slotStack.item !is ItemSnowball)

                        return if (replaceCurr) index else null
                    }
                }
            }

            "pearl" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemEnderPearl && !type(index).equals("Pearl", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemEnderPearl

                        return if (replaceCurr) index else null
                    }
                }
            }

            "rod" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if (item is ItemFishingRod && !type(index).equals("Rod", ignoreCase = true)) {
                        val replaceCurr = slotStack == null || slotStack.item !is ItemFishingRod

                        return if (replaceCurr) index else null
                    }
                }
            }

            "potion" -> {
                mc.thePlayer.inventory.mainInventory.forEachIndexed { index, stack ->
                    val item = stack?.item

                    if ((item is ItemPotion && ItemPotion.isSplash(stack.itemDamage)) &&
                        !type(index).equals("Potion", ignoreCase = true)
                    ) {
                        val replaceCurr =
                            slotStack == null || slotStack.item !is ItemPotion || !ItemPotion.isSplash(slotStack.itemDamage)

                        return if (replaceCurr) index else null
                    }
                }
            }
        }

        return null
    }

    /**
     * Get items in inventory
     */
    private fun items(start: Int = 0, end: Int = 45): Map<Int, ItemStack> {
        val items = mutableMapOf<Int, ItemStack>()

        for (i in end - 1 downTo start) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue
            itemStack.item ?: continue

            if (i in 36..44 && type(i).equals("Ignore", ignoreCase = true)) {
                continue
            }

            if (System.currentTimeMillis() - (itemStack as IItemStack).itemDelay >= itemDelayValue.get()) {
                items[i] = itemStack
            }
        }

        return items
    }

    val amount: IntArray
        get() {
            var missileAmount = 0
            var blockAmount = 0
            var arrowAmount = 0
            var foodAmount = 0
            var rod = 0
            mc.thePlayer.inventory.mainInventory.forEachIndexed { i, _ ->
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null) {
                    when (itemStack.item) {
                        is ItemSnowball, is ItemEgg -> {
                            missileAmount += itemStack.stackSize
                        }

                        is ItemBlock -> {
                            if (canPlaceBlock((itemStack.item as ItemBlock).block)) {
                                blockAmount += itemStack.stackSize
                            }
                        }

                        is ItemFood -> {
                            foodAmount += itemStack.stackSize
                        }

                        is ItemFishingRod -> rod += itemStack.stackSize
                    }
                    if (itemStack.unlocalizedName == "item.arrow") {
                        arrowAmount += itemStack.stackSize
                    }
                }
            }
            return intArrayOf(missileAmount, blockAmount, arrowAmount,foodAmount,rod)
        }

    private fun fishingRod() : Int {
        return mc.thePlayer.inventory.mainInventory.count { it.item is ItemFishingRod }
    }

    /**
     * Get type of [targetSlot]
     */
    private fun type(targetSlot: Int) = when (targetSlot) {
        0 -> sortSlot1Value.get()
        1 -> sortSlot2Value.get()
        2 -> sortSlot3Value.get()
        3 -> sortSlot4Value.get()
        4 -> sortSlot5Value.get()
        5 -> sortSlot6Value.get()
        6 -> sortSlot7Value.get()
        7 -> sortSlot8Value.get()
        8 -> sortSlot9Value.get()
        else -> ""
    }
}