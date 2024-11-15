/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import kevin.utils.component1
import kevin.utils.component2
import kevin.utils.component3
import kevin.utils.minus
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.StringUtils.contains
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.server.*
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraft.world.WorldSettings
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ModuleInfo("BackTrack", category = ModuleCategory.COMBAT)
object Backtrack : Module() {

    private val nextBacktrackDelay = IntegerValue("NextBacktrackDelay", 0, 0,2000).displayable { mode.get() == "Modern" }
    private val delay = object : IntegerValue("Delay", 80, 0,700) {
        override fun onChange(oldValue: Int, newValue: Int) {
            if (mode.get() == "Modern") {
                clearPackets()
                reset()
            }
        }
    }

    val mode = object : ListValue("Mode", arrayOf("Legacy", "Modern"), "Modern") {
        override fun onChanged(oldValue: String, newValue: String) {
            clearPackets()
            backtrackedPlayer.clear()
        }
    }

    // Legacy
    private val legacyPos = ListValue(
        "Caching mode",
        arrayOf("ClientPos", "ServerPos"),
        "ClientPos"
    ).displayable { mode.get() == "Legacy" }

    // Modern
    private val style = ListValue("Style", arrayOf("Pulse", "Smooth"), "Smooth").displayable { mode.get() == "Modern" }

    private val maxDistance: FloatValue = object : FloatValue("MaxDistance", 3.0f, 0.0f,3.5f) {
    }
    private val minDistance = object : FloatValue("MinDistance", 2.0f, 0.0f,3.0f) {
    }
    private val smart = BoolValue("Smart", true).displayable { mode.get() == "Modern" }

    // ESP
    val espMode = ListValue(
        "ESP-Mode",
        arrayOf("None", "Box", "Model"),
        "Box",
    ).displayable { mode.get() == "Modern" }
    private val rainbow = BoolValue("Rainbow", true).displayable { mode.get() == "Modern" && espMode.get() == "Box" }
    private val red = IntegerValue(
        "R",
        0,
        0,255,
    ).displayable { !rainbow.get() && mode.get() == "Modern" && espMode.get() == "Box" }
    private val green = IntegerValue(
        "G",
        255,
        0,255,
    ).displayable { !rainbow.get() && mode.get() == "Modern" && espMode.get() == "Box" }
    private val blue = IntegerValue(
        "B",
        0,
        0,255,
    ).displayable { !rainbow.get() && mode.get() == "Modern" && espMode.get() == "Box" }

    private val packetQueue = LinkedHashMap<Packet<*>, Long>()
    private val positions = mutableListOf<Pair<Vec3, Long>>()

    var target: EntityLivingBase? = null

    private var globalTimer = MSTimer()

    var shouldRender = true

    private var ignoreWholeTick = false

    private var delayForNextBacktrack = 0L

    // Legacy
    private val maximumCachedPositions = IntegerValue("MaxCachedPositions", 10, 1,20).displayable { mode.get() == "Legacy" }

    private val backtrackedPlayer = ConcurrentHashMap<UUID, MutableList<BacktrackData>>()

    private val nonDelayedSoundSubstrings = arrayOf("game.player.hurt", "game.player.die")

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (FDPClient.moduleManager[Blink::class.java]!!.state)
            return

        if (event.isCancelled)
            return

        when (mode.get().lowercase()) {
            "legacy" -> {
                when (packet) {
                    // Check if packet is a spawn player packet
                    is S0CPacketSpawnPlayer -> {
                        // Insert first backtrack data
                        addBacktrackData(
                            packet.player,
                            packet.realX,
                            packet.realY,
                            packet.realZ,
                            System.currentTimeMillis()
                        )
                    }

                    is S14PacketEntity -> {
                        if (legacyPos.get() == "ServerPos") {
                            val entity = mc.theWorld?.getEntityByID(packet.entityId)
                            val entityMixin = entity as? IMixinEntity
                            if (entityMixin != null) {
                                addBacktrackData(
                                    entity.uniqueID,
                                    entityMixin.trueX,
                                    entityMixin.trueY,
                                    entityMixin.trueZ,
                                    System.currentTimeMillis()
                                )
                            }
                        }
                    }

                    is S18PacketEntityTeleport -> {
                        if (legacyPos.get() == "ServerPos") {
                            val entity = mc.theWorld?.getEntityByID(packet.entityId)
                            val entityMixin = entity as? IMixinEntity
                            if (entityMixin != null) {
                                addBacktrackData(
                                    entity.uniqueID,
                                    entityMixin.trueX,
                                    entityMixin.trueY,
                                    entityMixin.trueZ,
                                    System.currentTimeMillis()
                                )
                            }
                        }
                    }
                }
            }

            "modern" -> {
                // Prevent cancelling packets when not needed
                if (packetQueue.isEmpty() && PacketUtils.queuedPackets.isEmpty() && !shouldBacktrack())
                    return

                when (packet) {
                    // Ignore server related packets
                    is C00Handshake, is C00PacketServerQuery, is S02PacketChat, is S01PacketPong ->
                        return

                    // Flush on teleport or disconnect
                    is S08PacketPlayerPosLook, is S40PacketDisconnect -> {
                        clearPackets()
                        return
                    }

                    is S29PacketSoundEffect ->
                        if (nonDelayedSoundSubstrings in packet.soundName)
                            return

                    // Flush on own death
                    is S06PacketUpdateHealth ->
                        if (packet.health <= 0) {
                            clearPackets()
                            return
                        }

                    is S13PacketDestroyEntities ->
                        if (target != null && target!!.entityId in packet.entityIDs) {
                            clearPackets()
                            reset()
                            return
                        }

                    is S1CPacketEntityMetadata ->
                        if (target?.entityId == packet.entityId) {
                            val metadata = packet.func_149376_c() ?: return

                            metadata.forEach {
                                if (it.dataValueId == 6) {
                                    val objectValue = it.getObject().toString().toDoubleOrNull()
                                    if (objectValue != null && !objectValue.isNaN() && objectValue <= 0.0) {
                                        clearPackets()
                                        reset()
                                        return
                                    }
                                }
                            }

                            return
                        }

                    is S19PacketEntityStatus ->
                        if (packet.entityId == target?.entityId)
                            return
                }

                // Cancel every received packet to avoid possible server synchronization issues from random causes.
                if (event.eventType == EventState.RECEIVE) {
                    when (packet) {
                        is S14PacketEntity ->
                            if (packet.entityId == target?.entityId)
                                (target as? IMixinEntity)?.run {
                                    synchronized(positions) {
                                        positions += Pair(Vec3(trueX, trueY, trueZ), System.currentTimeMillis())
                                    }
                                }

                        is S18PacketEntityTeleport ->
                            if (packet.entityId == target?.entityId)
                                (target as? IMixinEntity)?.run {
                                    synchronized(positions) {
                                        positions += Pair(Vec3(trueX, trueY, trueZ), System.currentTimeMillis())
                                    }
                                }
                    }

                    event.cancelEvent()
                    synchronized(packetQueue) {
                        packetQueue[packet] = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        if (mode.get() == "Legacy") {
            backtrackedPlayer.forEach { (key, backtrackData) ->
                // Remove old data
                backtrackData.removeAll { it.time + delay.get() < System.currentTimeMillis() }

                // Remove player if there is no data left. This prevents memory leaks.
                if (backtrackData.isEmpty())
                    removeBacktrackData(key)
            }
        }

        val target = target
        val targetMixin = target as? IMixinEntity
        if (mode.get() == "Modern") {
            if (targetMixin != null) {
                if (!FDPClient.moduleManager[Blink::class.java]!!.state && shouldBacktrack() && targetMixin.truePos) {
                    val trueDist = mc.thePlayer.getDistance(targetMixin.trueX, targetMixin.trueY, targetMixin.trueZ)
                    val dist = mc.thePlayer.getDistance(target.posX, target.posY, target.posZ)

                    if (trueDist <= 6f && (!smart.get() || trueDist >= dist) && (style.get() == "Smooth" || !globalTimer.hasTimePassed(
                            delay.get().toLong()
                        ))
                    ) {
                        shouldRender = true

                        if (mc.thePlayer.getDistanceToEntityBox(target) in minDistance.get()..maxDistance.get())
                            handlePackets()
                        else
                            handlePacketsRange()
                    } else {
                        clearPackets()
                        globalTimer.reset()
                    }
                }
            } else {
                clearPackets()
                globalTimer.reset()
            }
        }

        ignoreWholeTick = false
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (!isSelected(event.targetEntity, true))
            return

        // Clear all packets, start again on enemy change
        if (target != event.targetEntity) {
            clearPackets()
            reset()
        }

        if (event.targetEntity is EntityLivingBase) {
            target = event.targetEntity
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        when (mode.get().lowercase()) {
            "legacy" -> {
                val color = Color.RED

                for (entity in mc.theWorld.loadedEntityList) {
                    if (entity is EntityPlayer) {
                        glPushMatrix()
                        glDisable(GL_TEXTURE_2D)
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                        glEnable(GL_LINE_SMOOTH)
                        glEnable(GL_BLEND)
                        glDisable(GL_DEPTH_TEST)

                        mc.entityRenderer.disableLightmap()

                        glBegin(GL_LINE_STRIP)
                        glColor(color)

                        val renderPosX = mc.renderManager.viewerPosX
                        val renderPosY = mc.renderManager.viewerPosY
                        val renderPosZ = mc.renderManager.viewerPosZ

                        loopThroughBacktrackData(entity) {
                            glVertex3d(entity.posX - renderPosX, entity.posY - renderPosY, entity.posZ - renderPosZ)
                            false
                        }

                        glColor4d(1.0, 1.0, 1.0, 1.0)
                        glEnd()
                        glEnable(GL_DEPTH_TEST)
                        glDisable(GL_LINE_SMOOTH)
                        glDisable(GL_BLEND)
                        glEnable(GL_TEXTURE_2D)
                        glPopMatrix()
                    }
                }
            }

            "modern" -> {
                if (!shouldBacktrack() || packetQueue.isEmpty() || !shouldRender)
                    return

                if (espMode.get() != "Box") return

                val renderManager = mc.renderManager

                target?.run {
                    val targetEntity = target as IMixinEntity

                    if (targetEntity.truePos) {
                        val (x, y, z) = targetEntity.interpolatedPosition - Vec3(
                            renderManager.renderPosX,
                            renderManager.renderPosY,
                            renderManager.renderPosZ
                        )

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
    }

    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        if (mode.get() == "Legacy" && legacyPos.get() == "ClientPos") {
            val entity = event.movedEntity

            // Check if entity is a player
            if (entity is EntityPlayer) {
                // Add new data
                addBacktrackData(entity.uniqueID, entity.posX, entity.posY, entity.posZ, System.currentTimeMillis())
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // Clear packets on disconnect only
        // Set target to null on world change
        if (mode.get() == "Modern") {
            if (event.worldClient == null)
                clearPackets(false)
            target = null
        }
    }

    override fun onEnable() =
        reset()

    override fun onDisable() {
        clearPackets()
        backtrackedPlayer.clear()
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - delay.get()) {
                    PacketUtils.queuedPackets.add(packet)
                    true
                } else false
            }
        }
        synchronized(positions) {
            positions.removeAll { (_, timestamp) -> timestamp < System.currentTimeMillis() - delay.get() }
        }
    }

    private fun handlePacketsRange() {
        val time = getRangeTime()
        if (time == -1L) {
            clearPackets()
            return
        }
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= time) {
                    PacketUtils.queuedPackets.add(packet)
                    true
                } else false
            }
        }
        synchronized(positions) {
            positions.removeAll { (_, timestamp) -> timestamp < time }
        }
    }

    private fun getRangeTime(): Long {
        if (target == null) return 0L
        var time = 0L
        var found = false
        synchronized(positions) {
            for (data in positions) {
                time = data.second
                val targetPos = Vec3(target!!.posX, target!!.posY, target!!.posZ)
                val (dx, dy, dz) = data.first - targetPos
                val targetBox = target!!.hitBox.offset(dx, dy, dz)
                if (mc.thePlayer.getDistanceToBox(targetBox) in minDistance.get()..maxDistance.get()) {
                    found = true
                    break
                }
            }
        }
        return if (found) time else -1L
    }

    private fun clearPackets(handlePackets: Boolean = true) {
        if (packetQueue.isNotEmpty()) {
            delayForNextBacktrack = System.currentTimeMillis() + nextBacktrackDelay.get()
        }

        synchronized(packetQueue) {
            if (handlePackets)
                PacketUtils.queuedPackets.addAll(packetQueue.keys)

            packetQueue.clear()
        }

        positions.clear()
        shouldRender = false
        ignoreWholeTick = true
    }

    private fun addBacktrackData(id: UUID, x: Double, y: Double, z: Double, time: Long) {
        // Get backtrack data of player
        val backtrackData = getBacktrackData(id)

        // Check if there is already data of the player
        if (backtrackData != null) {
            // Check if there is already enough data of the player
            if (backtrackData.size >= maximumCachedPositions.get()) {
                // Remove first data
                backtrackData.removeFirst()
            }

            // Insert new data
            backtrackData += BacktrackData(x, y, z, time)
        } else {
            // Create new list
            backtrackedPlayer[id] = mutableListOf(BacktrackData(x, y, z, time))
        }
    }

    private fun getBacktrackData(id: UUID) = backtrackedPlayer[id]

    private fun removeBacktrackData(id: UUID) = backtrackedPlayer.remove(id)

    /**
     * This function will return the nearest tracked range of an entity.
     */
    fun getNearestTrackedDistance(entity: Entity): Double {
        var nearestRange = 0.0

        loopThroughBacktrackData(entity) {
            val range = entity.getDistanceToEntityBox(mc.thePlayer)

            if (range < nearestRange || nearestRange == 0.0) {
                nearestRange = range
            }

            false
        }

        return nearestRange
    }

    /**
     * This function will loop through the backtrack data of an entity.
     */
    fun loopThroughBacktrackData(entity: Entity, action: () -> Boolean) {
        if (!state || entity !is EntityPlayer || mode.get() == "Modern")
            return

        val backtrackDataArray = getBacktrackData(entity.uniqueID) ?: return

        val currPos = entity.currPos
        val prevPos = entity.prevPos

        // This will loop through the backtrack data. We are using reversed() to loop through the data from the newest to the oldest.
        for ((x, y, z, _) in backtrackDataArray.reversed()) {
            entity.setPosAndPrevPos(Vec3(x, y, z))

            if (action())
                break
        }

        // Reset position
        entity.setPosAndPrevPos(currPos, prevPos)
    }

    fun runWithNearestTrackedDistance(entity: Entity, f: () -> Unit) {
        if (entity !is EntityPlayer || !handleEvents() || mode.get() == "Modern") {
            f()

            return
        }

        var backtrackDataArray = getBacktrackData(entity.uniqueID)?.toMutableList()

        if (backtrackDataArray == null) {
            f()

            return
        }

        backtrackDataArray = backtrackDataArray.sortedBy { (x, y, z, _) ->
            runWithSimulatedPosition(entity, Vec3(x, y, z)) {
                mc.thePlayer.getDistanceToBox(entity.hitBox)
            }
        }.toMutableList()

        val (x, y, z, _) = backtrackDataArray.first()

        runWithSimulatedPosition(entity, Vec3(x, y, z)) {
            f()

            null
        }
    }

    fun runWithSimulatedPosition(entity: Entity, vec3: Vec3, f: () -> Double?): Double? {
        val currPos = entity.currPos
        val prevPos = entity.prevPos

        entity.setPosAndPrevPos(vec3)

        val result = f()

        // Reset position
        entity.setPosAndPrevPos(currPos, prevPos)

        return result
    }

    val color
        get() = if (rainbow.get()) rainbow() else Color(red.get(), green.get(), blue.get())

    fun shouldBacktrack() =
        mc.thePlayer != null && target != null && mc.thePlayer.health > 0 && (target!!.health > 0 || target!!.health.isNaN()) && mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR && System.currentTimeMillis() >= delayForNextBacktrack && target?.let {
            isSelected(it, true) && (mc.thePlayer?.ticksExisted ?: 0) > 20 && !ignoreWholeTick
        } == true

    private fun reset() {
        target = null
        globalTimer.reset()
    }
}

data class BacktrackData(val x: Double, val y: Double, val z: Double, val time: Long)
