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
import net.ccbluex.liquidbounce.features.module.modules.world.scaffold.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.scaffold.Scaffold2
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

@ModuleInfo("FakeLag",category = ModuleCategory.COMBAT)
object FakeLag : Module() {

    private val delay = IntegerValue("Delay", 550, 0,1000)
    private val recoilTime = IntegerValue("RecoilTime", 750, 0,2000)

    private val maxAllowedDistToEnemy: FloatValue = object : FloatValue("MaxAllowedDistToEnemy", 3.5f, 0f,6f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = minAllowedDistToEnemy.get()
            if (i > newValue) set(i)
        }
    }
    private val minAllowedDistToEnemy: FloatValue = object : FloatValue("MinAllowedDistToEnemy", 1.5f, 0f,6f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = maxAllowedDistToEnemy.get()
            if (i < newValue) set(i)
        }
    }

    private val blinkOnAction = BoolValue("BlinkOnAction", true)

    private val pauseOnNoMove = BoolValue("PauseOnNoMove", true)
    private val pauseOnChest = BoolValue("PauseOnChest", false)

    private val line = BoolValue("Line", true)
    private val rainbow = BoolValue("Rainbow", false).displayable { line.get() }
    private val red = IntegerValue("R",
        0,
        0,255,
    ).displayable { !rainbow.get() && line.get() }
    private val green = IntegerValue("G",
        255,
        0,255,
    ).displayable { !rainbow.get() && line.get() }
    private val blue = IntegerValue("B",
        0,
        0,255,
    ).displayable { !rainbow.get() && line.get() }

    private val packetQueue = LinkedHashMap<Packet<*>, Long>()
    private val positions = LinkedHashMap<Vec3, Long>()
    private val resetTimer = MSTimer()
    private var wasNearEnemy = false
    private var ignoreWholeTick = false

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        blink()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (!handleEvents())
            return

        if (player.isDead)
            return

        if (event.isCancelled)
            return

        if (maxAllowedDistToEnemy.get() > 0.0 && wasNearEnemy)
            return

        if (ignoreWholeTick)
            return

        if (pauseOnNoMove.get() && !isMoving) {
            blink()
            return
        }

        // Flush on damaged received
        if (player.health < player.maxHealth) {
            if (player.hurtTime != 0) {
                blink()
                return
            }
        }

        // Flush on scaffold/tower usage
        if (FDPClient.moduleManager[Scaffold::class.java]!!.state || FDPClient.moduleManager[Scaffold2::class.java]!!.state) {
            blink()
            return
        }

        // Flush on attack/interact
        if (blinkOnAction.get() && packet is C02PacketUseEntity) {
            blink()
            return
        }

        if (pauseOnChest.get() && mc.currentScreen is GuiContainer) {
            blink()
            return
        }

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> return

            // Flush on window clicked (Inventory)
            is C0EPacketClickWindow, is C0DPacketCloseWindow -> {
                blink()
                return
            }

            // Flush on doing action/getting action
            is S08PacketPlayerPosLook, is C08PacketPlayerBlockPlacement, is C07PacketPlayerDigging, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                blink()
                return
            }

            // Flush on knockback
            is S12PacketEntityVelocity -> {
                if (player.entityId == packet.entityID) {
                    blink()
                    return
                }
            }

            is S27PacketExplosion -> {
                if (packet.field_149153_g != 0f || packet.field_149152_f != 0f || packet.field_149159_h != 0f) {
                    blink()
                    return
                }
            }
        }

        if (!resetTimer.hasTimePassed(recoilTime.get().toLong()))
            return

        if (event.eventType == EventState.SEND) {
            event.cancelEvent()
            if (packet is C03PacketPlayer && packet.isMoving) {
                val packetPos = Vec3(packet.x, packet.y, packet.z)
                synchronized(positions) {
                    positions[packetPos] = System.currentTimeMillis()
                }
            }
            synchronized(packetQueue) {
                packetQueue[packet] = System.currentTimeMillis()
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // Clear packets on disconnect only
        if (event.worldClient == null)
            blink(false)
    }

    private fun getTruePositionEyes(player: EntityPlayer): Vec3? {
        val mixinPlayer = player as? IMixinEntity ?: return null
        return Vec3(mixinPlayer.trueX, mixinPlayer.trueY + player.getEyeHeight().toDouble(), mixinPlayer.trueZ)
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        val player = mc.thePlayer ?: return

        if (maxAllowedDistToEnemy.get() > 0) {
            val playerPos = player.positionVector
            val serverPos = positions.keys.firstOrNull() ?: playerPos

            val otherPlayers = mc.theWorld.playerEntities.filter { it != player }

            val (dx, dy, dz) = serverPos - playerPos
            val playerBox = player.hitBox.offset(dx, dy, dz)

            wasNearEnemy = false

            for (otherPlayer in otherPlayers) {
                val entityMixin = otherPlayer as? IMixinEntity
                if (entityMixin != null) {
                    val eyes = getTruePositionEyes(otherPlayer) ?: return
                    if (eyes.distanceTo(getNearestPointBB(eyes, playerBox)) in minAllowedDistToEnemy.get()..maxAllowedDistToEnemy.get()) {
                        blink()
                        wasNearEnemy = true
                        return
                    }
                }
            }
        }

        if (FDPClient.moduleManager[Blink::class.java]!!.state || player.isDead || player.isUsingItem) {
            blink()
            return
        }

        if (!resetTimer.hasTimePassed(recoilTime.get().toLong()))
            return

        handlePackets()
        ignoreWholeTick = false
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!line.get()) return

        val color = if (rainbow.get()) rainbow() else Color(red.get(), green.get(), blue.get())

        if (FDPClient.moduleManager[Blink::class.java]!!.state)
            return

        synchronized(positions.keys) {
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

            for (pos in positions.keys)
                glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ)

            glColor4d(1.0, 1.0, 1.0, 1.0)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    override val tag
        get() = packetQueue.size.toString()

    private fun blink(handlePackets: Boolean = true) {
        synchronized(packetQueue) {
            if (handlePackets) {
                resetTimer.reset()

                packetQueue.forEach { (packet) -> sendPacket(packet, false) }
            }
        }

        packetQueue.clear()
        positions.clear()
        ignoreWholeTick = true
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - delay.get()) {
                    sendPacket(packet, false)
                    true
                } else false
            }
        }

        synchronized(positions) {
            positions.entries.removeAll { (_, timestamp) -> timestamp <= System.currentTimeMillis() - delay.get() }
        }
    }

}
