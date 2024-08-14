package net.ccbluex.liquidbounce.features.module.modules.world.scaffold

import kevin.utils.multiply
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.stats.StatList
import net.minecraft.util.*
import java.awt.Color
import javax.vecmath.Vector2d
import kotlin.math.abs
import kotlin.math.min

@ModuleInfo("LegitScaffold", category = ModuleCategory.WORLD)
class LegitScaffold :
    Module() {
    private val startTimeHelper = MSTimer()
    private val startTimeHelper2 = MSTimer()
    private val adTimeHelper = MSTimer()
    val yawSpeed: FloatValue = FloatValue("YawSpeed", 40.0f, 0.0f, 180.0f)
    val pitchSpeed: FloatValue = FloatValue("PitchSpeed", 40.0f, 0.0f, 180.0f)
    val yawOffset: FloatValue = FloatValue("YawOffSet", -180f, -200f, 200f)
    val moveFix: BoolValue = BoolValue("MoveFix", true)
    val esp: BoolValue = BoolValue("ESP", true)
    val adStrafe: BoolValue = BoolValue("AdStrafe", true)
    val swing: BoolValue = BoolValue("Swing", false)
    val extraClick: BoolValue = BoolValue("ExtraClickTime", true)
    val sameY: BoolValue = BoolValue("SameY", false)
    val preMotionClick: BoolValue = BoolValue("PreMotionClick", false)
    val silentMode = ListValue("SilentMode", arrayOf("Switch", "Spoof", "None"), "Spoof")
    var hitpoints: ArrayList<DoubleArray> = ArrayList()
    private var rots: Rotation = Rotation(0F, 0F)
    private var enumFacing: EnumFacing? = null
    private var objectPosition: MovingObjectPosition? = null
    var slotID: Int = 0
        private set
    private var block: ItemStack? = null
    private var lastSlotID = 0
    private var blockPos: BlockPos? = null
    private var start = true
    private var xyz = DoubleArray(3)
    private val hashMap = HashMap<Float, MovingObjectPosition>()

    override fun onEnable() {
        if (mc.thePlayer != null && mc.theWorld != null) {
            this.objectPosition = null
            this.blockPos = null
            this.slotID = mc.thePlayer.inventory.currentItem
            this.lastSlotID = mc.thePlayer.inventory.currentItem
            this.start = true
            startTimeHelper.reset()
        }
    }

    override fun onDisable() {
        if (mc.thePlayer.inventory.currentItem != this.slotID) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }
        RotationUtils.setTargetRotation(
            Rotation(
                mc.thePlayer.rotationYaw + rots.yaw / 2,
                mc.thePlayer.rotationPitch + rots.pitch / 2
            )
        )
        mc.thePlayer.isSprinting = false

        this.slotID = mc.thePlayer.inventory.currentItem
    }

    @EventTarget
    fun onUpdate(ignored: UpdateEvent?) {
        if (this.blockPos != null) this.setRotation()
        if (mc.thePlayer != null && mc.theWorld != null) {
            mc.thePlayer.isSprinting = false

            if (extraClick.get()) click()
        }
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        this.blockPos = this.aimBlockPos
        this.start =
            (mc.thePlayer.motionX == 0.0) && (mc.thePlayer.motionZ == 0.0) && mc.thePlayer.onGround || !startTimeHelper.hasTimePassed(
                200L
            )
        if (this.start) {
            startTimeHelper2.reset()
        }

        if (this.blockPos != null) {
            this.rots = nearestRotation()

            this.setRotation()
        }
        if (objectPosition != null) {
            mc.objectMouseOver = objectPosition
        }
    }

    @EventTarget
    fun onClick(event: ClickUpdateEvent) {
//        Vec3 vec3 = mc.thePlayer.getPositionEyes(1f);
//        Vec3 vec31 = OtherExtensionsKt.multiply(rots.toDirection(), 4.5f).add(vec3);
//        mc.objectMouseOver = mc.theWorld.rayTraceBlocks(vec3, vec31, false, false, true);
        event.cancelEvent()
        this.setRotation()
        click()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!preMotionClick.get()) return
        if (event.eventState === EventState.PRE) {
            //          vec3 = mc.thePlayer.getPositionEyes(1f);
            // NOPE, we updated position this tick but not in server side.
            // so we need to...
            val vec3 = Vec3(
                mc.thePlayer.posX,
                mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posY
            )
            val vec31: Vec3 = rots.toDirection().multiply(4.5).add(vec3)
            mc.objectMouseOver = mc.theWorld.rayTraceBlocks(vec3, vec31, false, false, true)
            click()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet: Packet<*> = event.packet
        if (packet is C09PacketHeldItemChange) {
            val id = packet.slotId
            if (id == lastSlotID) event.cancelEvent()
            lastSlotID = id
        } else if (packet is S09PacketHeldItemChange) {
            lastSlotID = packet.heldItemHotbarIndex
        }
    }

    private fun nearestRotation(): Rotation {
            this.objectPosition = null
            var rot: Rotation = rots.cloneSelf()
            val b = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
            hashMap.clear()
            if (this.start) {
                rot.pitch = 80.34f
                rot.yaw = (mc.thePlayer.rotationYaw + yawOffset.get())
                rot = RotationUtils.limitAngleChange(
                    RotationUtils.serverRotation!!,
                    rot,
                    yawSpeed.get() + RandomUtils.nextFloat(0F, 2F),
                    pitchSpeed.get() - RandomUtils.nextFloat(0F, 2F)
                )
            } else {
                rot.yaw = (mc.thePlayer.rotationYaw + yawOffset.get())
                rot = RotationUtils.limitAngleChange(
                    RotationUtils.serverRotation!!,
                    rot,
                    yawSpeed.get() + RandomUtils.nextFloat(0F, 2F),
                    pitchSpeed.get() + RandomUtils.nextFloat(0F, 2F)
                )
                var x: Double = mc.thePlayer.posX
                var z: Double = mc.thePlayer.posZ
                val add1 = 1.288
                val add2 = 0.288
                if (!this.buildForward()) {
                    x += mc.thePlayer.posX - xyz[0]
                    z += mc.thePlayer.posZ - xyz[2]
                }

                this.xyz = doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                val maX = blockPos!!.x.toDouble() + add1
                val miX = blockPos!!.x.toDouble() - add2
                val maZ = blockPos!!.z.toDouble() + add1
                val miZ = blockPos!!.z.toDouble() - add2
                if (x in miX..maX && z <= maZ && z >= miZ) {
                    rot.pitch = (rots.pitch)
                } else {
                    val movingObjectPositions = ArrayList<MovingObjectPosition>()
                    val pitches = ArrayList<Float>()
                    val vec3: Vec3 = mc.thePlayer.getPositionEyes(1f)
                    var mm = (rots.pitch - 20.0f).coerceAtLeast(-90.0f)
                    while (mm < (rots.pitch + 20.0f).coerceAtMost(90.0f)) {
                        rot.pitch = mm
                        rot.fixedSensitivity()
                        val vec31: Vec3 = rot.toDirection().multiply(4.5).add(vec3)
                        val m4: MovingObjectPosition = mc.theWorld.rayTraceBlocks(vec3, vec31, false, false, true)
                        if ((m4.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.isOkBlock(
                                m4.blockPos
                            )) && m4.blockPos == this.blockPos && (m4.sideHit != EnumFacing.DOWN) && (m4.sideHit != EnumFacing.UP || (!sameY.get() && mc.gameSettings.keyBindJump.isKeyDown)) && (m4.blockPos.y <= b.y)
                        ) {
                            movingObjectPositions.add(m4)
                            val rotPitch: Float = rot.pitch
                            hashMap[rotPitch] = m4
                            pitches.add(rotPitch)
                        }
                        mm += 0.02f
                    }

                    movingObjectPositions.sortWith(Comparator.comparingDouble { m: MovingObjectPosition ->
                        mc.thePlayer.getDistanceSq(
                            m.blockPos.add(0.5, 0.5, 0.5)
                        )
                    })
                    var mm1: MovingObjectPosition? = null
                    if (movingObjectPositions.isNotEmpty()) {
                        mm1 = movingObjectPositions[0]
                    }

                    if (mm1 != null) {
                        pitches.sortWith(Comparator.comparingDouble { pitch: Float ->
                            this.distanceToLastPitch(
                                pitch
                            )
                        })
                        if (pitches.isNotEmpty()) {
                            val rotPitch = pitches[0]
                            rot.pitch = rotPitch
                            this.objectPosition = hashMap[rotPitch]
                            this.blockPos = objectPosition!!.blockPos
                        }

                        return rot
                    }
                }
            }

            return rot
        }

    private fun canPlace(rotation: Rotation): Boolean {
        val b = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
        val vec31: Vec3 = mc.thePlayer.getPositionEyes(1f)
        val vec32: Vec3 = rotation.toDirection().multiply(4.5).add(vec31)
        val m4: MovingObjectPosition = mc.theWorld.rayTraceBlocks(vec31, vec32, false, false, true)
        if ((m4.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.isOkBlock(
                m4.blockPos
            )) && m4.blockPos == this.blockPos && (m4.sideHit != EnumFacing.DOWN) && (m4.sideHit != EnumFacing.UP) && (m4.blockPos.y <= b.y)
        ) {
            hashMap[rotation.pitch] = m4
            return true
        } else {
            return false
        }
    }

    private fun distanceToLastRots(predictRots: Rotation): Double {
        val diff1: Float = abs(predictRots.yaw - rots.yaw)
        val diff2: Float = abs(predictRots.pitch - rots.pitch)
        return (diff1 * diff1 + diff2 * diff2).toDouble()
    }

    private fun distanceToLastPitch(pitch: Float): Double {
        return abs(pitch - rots.pitch).toDouble()
    }

    private fun getAdvancedDiagonalExpandXZ(blockPos: BlockPos): DoubleArray {
        val xz = DoubleArray(2)
        val difference = Vector2d(blockPos.x.toDouble() - mc.thePlayer.posX, blockPos.z.toDouble() - mc.thePlayer.posZ)
        if (difference.x > -1.0 && difference.x < 0.0 && difference.y < -1.0) {
            this.enumFacing = EnumFacing.SOUTH
            xz[0] = difference.x * -1.0
            xz[1] = 1.0
        }

        if (difference.y < 0.0 && difference.y > -1.0 && difference.x < -1.0) {
            this.enumFacing = EnumFacing.EAST
            xz[0] = 1.0
            xz[1] = difference.y * -1.0
        }

        if (difference.x > -1.0 && difference.x < 0.0 && difference.y > 0.0) {
            this.enumFacing = EnumFacing.NORTH
            xz[0] = difference.x * -1.0
            xz[1] = 0.0
        }

        if (difference.y < 0.0 && difference.y > -1.0 && difference.x > 0.0) {
            xz[0] = 0.0
            xz[1] = difference.y * -1.0
            this.enumFacing = EnumFacing.WEST
        }

        if (difference.x >= 0.0 && difference.y < -1.0) {
            xz[1] = 1.0
        }

        if ((difference.y >= 0.0) and (difference.x < -1.0)) {
            xz[0] = 1.0
        }

        if (difference.x >= 0.0 && difference.y > 0.0) {
        }

        if (difference.y <= -1.0 && difference.x < -1.0) {
            xz[0] = 1.0
            xz[1] = 1.0
        }

        return xz
    }

    private val placeSide: EnumFacing
        get() {
            val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
            if (playerPos == this.blockPos) {
                println("Error")
            }

            return if (playerPos.add(0, 1, 0) == this.blockPos) EnumFacing.UP else ((if (playerPos.add(
                    0,
                    -1,
                    0
                ) == this.blockPos
            ) EnumFacing.DOWN else (if (playerPos.add(
                    1,
                    0,
                    0
                ) == this.blockPos
            ) EnumFacing.WEST else (if (playerPos.add(
                    -1,
                    0,
                    0
                ) == this.blockPos
            ) EnumFacing.EAST else (if (playerPos.add(
                    0,
                    0,
                    1
                ) == this.blockPos
            ) EnumFacing.NORTH else (if (playerPos.add(
                    0,
                    0,
                    -1
                ) == this.blockPos
            ) EnumFacing.SOUTH else (if (playerPos.add(
                    1,
                    0,
                    1
                ) == this.blockPos
            ) EnumFacing.WEST else (if (playerPos.add(
                    -1,
                    0,
                    1
                ) == this.blockPos
            ) EnumFacing.EAST else (if (playerPos.add(
                    -1,
                    0,
                    1
                ) == this.blockPos
            ) EnumFacing.NORTH else (if (playerPos.add(
                    -1,
                    0,
                    -1
                ) == this.blockPos
            ) EnumFacing.SOUTH else null)))))))))!!)
        }

    fun click() {
        if (this.block == null) {
            this.block = mc.thePlayer.inventory.getCurrentItem()
        }

        if (this.blockPos != null && mc.currentScreen == null) {
            val lastItem: ItemStack = mc.thePlayer.inventory.getCurrentItem()
            var itemstack: ItemStack = mc.thePlayer.inventory.getCurrentItem()
            if (silentMode.get() != "None") {
                this.slotID = InventoryUtils.findAutoBlockBlock() - 36
                if (slotID == -1) return
                itemstack = mc.thePlayer.inventoryContainer.getSlot(this.slotID + 36).stack
                block = itemstack
                if (silentMode.get() == "Spoof" && this.lastSlotID != this.slotID) {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(slotID))
                }
            } else {
                this.slotID = mc.thePlayer.inventory.currentItem
                this.lastSlotID = mc.thePlayer.inventory.currentItem
            }

            val var26 = objectPosition
            if (var26 != null) {
                val var27 = var26.blockPos
                if (var26.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(var27)
                        .block.material !== Material.air
                ) {
                    if (itemstack.item !is ItemBlock) {
                        return
                    }

                    hitpoints.add(doubleArrayOf(var26.hitVec.xCoord, var26.hitVec.yCoord, var26.hitVec.zCoord))
                    if (mc.thePlayer.posY < var27.y.toDouble() + 1.5) {
                        if (var26.sideHit != EnumFacing.UP && var26.sideHit != EnumFacing.DOWN) {
                            if (silentMode.get() == "Switch") {
                                mc.thePlayer.inventory.setCurrentItem(block!!.item, 0, false, false)
                            }

                            if (mc.playerController.onPlayerRightClick(
                                    mc.thePlayer,
                                    mc.theWorld,
                                    itemstack,
                                    var27,
                                    var26.sideHit,
                                    var26.hitVec
                                )
                            ) {
                                if (swing.get()) mc.thePlayer.swingItem()
                                else mc.netHandler.addToSendQueue(C0APacketAnimation())
                            }

                            if (itemstack.stackSize == 0) {
                                mc.thePlayer.inventory.mainInventory[this.slotID] = null
                            }

                            MouseUtils.sendClickBlockToController( mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown && mc.inGameHasFocus)
                        }
                    } else if ((var26.sideHit != EnumFacing.DOWN) && var26.blockPos == this.blockPos && mc.gameSettings.keyBindJump.isKeyDown) {
                        if (silentMode.get() == "Switch") {
                            mc.thePlayer.inventory.setCurrentItem(block!!.item, 0, false, false)
                        }

                        if (mc.playerController.onPlayerRightClick(
                                mc.thePlayer,
                                mc.theWorld,
                                itemstack,
                                var27,
                                var26.sideHit,
                                var26.hitVec
                            )
                        ) {
                            if (swing.get()) mc.thePlayer.swingItem()
                            else mc.netHandler.addToSendQueue(C0APacketAnimation())
                        }

                        if (itemstack.stackSize == 0) {
                            mc.thePlayer.inventory.mainInventory[this.slotID] = null
                        }

                        MouseUtils.sendClickBlockToController(mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown && mc.inGameHasFocus)
                    }
                }
            }

            if (silentMode.get() == "Switch") {
                mc.thePlayer.inventory.setCurrentItem(lastItem.item, 0, false, false)
            }

            this.lastSlotID = this.slotID
        }
    }

    @EventTarget
    fun onEventStrafe(event: StrafeEvent) {
        if (moveFix.get() && !event.isCancelled) {
            event.cancelEvent()
            var strafe: Float
            strafe = event.strafe
            var forward: Float = -event.forward
            val friction: Float = event.friction
            if (adStrafe.get() && strafe == 0f) {
                val b = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.5, mc.thePlayer.posZ)
                if (mc.theWorld.getBlockState(b).block
                        .material === Material.air && mc.currentScreen == null &&  /*!Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCodeDefault()) && */this.buildForward() && mc.thePlayer.movementInput.moveForward != 0.0f
                ) {
                    strafe = if (mc.thePlayer.horizontalFacing === EnumFacing.EAST) {
                        if (b.z.toDouble() + 0.5 > mc.thePlayer.posZ) {
                            0.98f
                        } else {
                            -0.98f
                        }
                    } else if (mc.thePlayer.horizontalFacing === EnumFacing.WEST) {
                        if (b.z.toDouble() + 0.5 < mc.thePlayer.posZ) {
                            0.98f
                        } else {
                            -0.98f
                        }
                    } else if (mc.thePlayer.horizontalFacing === EnumFacing.SOUTH) {
                        if (b.x.toDouble() + 0.5 < mc.thePlayer.posX) {
                            0.98f
                        } else {
                            -0.98f
                        }
                    } else if (b.x.toDouble() + 0.5 > mc.thePlayer.posX) {
                        0.98f
                    } else {
                        -0.98f
                    }
                    if (mc.thePlayer.movementInput.sneak) {
                        strafe *= 0.3.toFloat()
                    }

                    adTimeHelper.reset()
                }
            }
            strafe = -strafe
            var f = strafe * strafe + forward * forward

            if (!(f < 1.0E-4f)) {
                f = MathHelper.sqrt_float(f)

                if (f < 1.0f) {
                    f = 1.0f
                }

                f = friction / f
                strafe *= f
                forward *= f
                val f1 = MathHelper.sin(RotationUtils.targetRotation!!.yaw * Math.PI.toFloat() / 180.0f)
                val f2 = MathHelper.cos(RotationUtils.targetRotation!!.yaw * Math.PI.toFloat() / 180.0f)
                mc.thePlayer.motionX += strafe * f2 - forward * f1
                mc.thePlayer.motionZ += forward * f2 + strafe * f1
            }
        }
    }

    @EventTarget
    fun onEventJump(event: JumpEvent) {
        if (event.isCancelled) return
        if (moveFix.get()) {
            // no sprint jump
            event.cancelEvent()
            mc.thePlayer.motionY = event.motion.toDouble()
            mc.thePlayer.triggerAchievement(StatList.jumpStat)
        }
    }

    private fun setRotation() {
        if (mc.currentScreen == null) {
            RotationUtils.setTargetRotation(rots, 2)
        }
    }

    private fun buildForward(): Boolean {
        val realYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)
        return realYaw.toDouble() > 77.5 && realYaw.toDouble() < 102.5 || (!(realYaw.toDouble() <= 167.5) || !(realYaw >= -167.0f) || (realYaw.toDouble() < -77.5 && realYaw.toDouble() > -102.5 || realYaw.toDouble() > -12.5 && realYaw.toDouble() < 12.5))
    }

    private val aimBlockPos: BlockPos?
        get() {
            val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
            if ((mc.gameSettings.keyBindJump.isKeyDown || !mc.thePlayer.onGround) && (mc.thePlayer.moveForward == 0.0f) && (mc.thePlayer.moveStrafing == 0.0f) && this.isOkBlock(
                    playerPos.add(0, -1, 0)
                )
            ) {
                return playerPos.add(0, -1, 0)
            } else {
                var blockPos: BlockPos? = null
                val bp = this.getBlockPos()
                val blockPositions = ArrayList<BlockPos>()
                if (bp.isNotEmpty()) {
                    for (i in 0 until min(bp.size.toDouble(), 18.0).toInt()) {
                        blockPositions.add(bp[i])
                    }

                    blockPositions.sortWith(Comparator.comparingDouble { blockPos: BlockPos ->
                        this.getDistanceToBlockPos(
                            blockPos
                        )
                    })
                    if (blockPositions.isNotEmpty()) {
                        blockPos = blockPositions[0]
                    }
                }

                return blockPos
            }
        }

    private fun getBlockPos(): ArrayList<BlockPos> {
        val playerPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        val blockPoses = ArrayList<BlockPos>()

        for (x in playerPos.x - 2..playerPos.x + 2) {
            for (y in playerPos.y - 1..playerPos.y) {
                for (z in playerPos.z - 2..playerPos.z + 2) {
                    if (this.isOkBlock(BlockPos(x, y, z))) {
                        blockPoses.add(BlockPos(x, y, z))
                    }
                }
            }
        }

        if (blockPoses.isNotEmpty()) {
            blockPoses.sortWith(Comparator.comparingDouble { blockPos: BlockPos ->
                mc.thePlayer.getDistanceSq(
                    blockPos.x.toDouble() + 0.5,
                    blockPos.y.toDouble() + 0.5,
                    blockPos.z.toDouble() + 0.5
                )
            })
        }

        return blockPoses
    }

    private fun getDistanceToBlockPos(blockPos: BlockPos): Double {
        var distance = 1337.0

        var x = blockPos.x.toFloat()
        while (x <= (blockPos.x + 1).toFloat()) {
            var y = blockPos.y.toFloat()
            while (y <= (blockPos.y + 1).toFloat()) {
                var z = blockPos.z.toFloat()
                while (z <= (blockPos.z + 1).toFloat()) {
                    val d0: Double = mc.thePlayer.getDistanceSq(x.toDouble(), y.toDouble(), z.toDouble())
                    if (d0 < distance) {
                        distance = d0
                    }
                    z = (z.toDouble() + 0.2).toFloat()
                }
                y = (y.toDouble() + 0.2).toFloat()
            }
            x = (x.toDouble() + 0.2).toFloat()
        }

        return distance
    }

    private fun isOkBlock(blockPos: BlockPos): Boolean {
        val block: Block = mc.theWorld.getBlockState(blockPos).block
        return block !is BlockLiquid && block !is BlockAir && block !is BlockChest && block !is BlockFurnace
    }

    @EventTarget
    fun onEventRender3D(event: Render3DEvent?) {
        if (esp.get() && this.blockPos != null) {
//            GL11.glEnable(3042);
//            GL11.glBlendFunc(770, 771);
//            GL11.glEnable(2848);
//            GL11.glDisable(2929);
//            GL11.glDisable(3553);
//            GlStateManager.disableCull();
//            GL11.glDepthMask(false);
            val red = 0.16470589f
            val green = 0.5686275f
            val blue = 0.96862745f
            //            final RenderManager renderManager = mc.getRenderManager();
//            float lineWidth = 0.0F;
//            if(this.blockPos != null) {
//                if(mc.thePlayer.getDistance(this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ()) > 1.0D) {
//                    double d0 = 1.0D - mc.thePlayer.getDistance(this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ()) / 20.0D;
//                    if(d0 < 0.3D) {
//                        d0 = 0.3D;
//                    }
//
//                    lineWidth = (float)((double)lineWidth * d0);
//                }
//                GL11.glLineWidth(lineWidth);
//                GL11.glColor4f(red, green, blue, 0.39215687F);
//
//                RenderUtils.drawFilledBox(new AxisAlignedBB(this.blockPos.getX() - renderManager.getRenderPosX(), this.blockPos.getY() - renderManager.getRenderPosY(), this.blockPos.getZ() - renderManager.getRenderPosZ(), this.blockPos.getX() + 1 - renderManager.getRenderPosX(), this.blockPos.getY() + 1 - renderManager.getRenderPosY(), this.blockPos.getZ() + 1 - renderManager.getRenderPosZ()));
//            }
//
//            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//            GL11.glDepthMask(true);
//            GlStateManager.enableCull();
//            GL11.glEnable(3553);
//            GL11.glEnable(2929);
//            GL11.glDisable(3042);
//            GL11.glBlendFunc(770, 771);
//            GL11.glDisable(2848);
            RenderUtils.drawBlockBox(this.blockPos, Color(red, green, blue, 0.39215687f), false)
        }
    }
}