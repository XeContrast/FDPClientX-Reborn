/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.extensions.getSmoothDistanceToEntity
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.Slight.RenderUtil
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.animations.ContinualAnimation
import net.ccbluex.liquidbounce.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.utils.animations.impl.SmoothStepAnimation
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.CombatRender.drawCircle
import net.ccbluex.liquidbounce.utils.render.CombatRender.drawCrystal
import net.ccbluex.liquidbounce.utils.render.CombatRender.drawEntityBoxESP
import net.ccbluex.liquidbounce.utils.render.CombatRender.drawPlatformESP
import net.ccbluex.liquidbounce.utils.render.CombatRender.drawZavz
import net.ccbluex.liquidbounce.utils.render.CombatRender.points
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.audio.SoundCategory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.BufferedInputStream
import java.io.IOException
import java.util.*
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


@ModuleInfo("CombatVisuals", category = ModuleCategory.VISUAL)
object CombatVisuals : Module() {

    // Mark
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer", "Health"), "Custom")
    val markValue = ListValue("MarkMode", arrayOf("None", "Box", "RoundBox", "Head", "Mark", "Sims","Jello", "Zavz","Rectangle","Round", "Points"), "Zavz")
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val colorTeam = BoolValue("Team", false)
    private val isMarkMode: Boolean
        get() = markValue.get() != "None"

    val colorRedValue = IntegerValue("Mark-Red", 0, 0,255).displayable { isMarkMode }
    val colorGreenValue = IntegerValue("Mark-Green", 160, 0,255).displayable { isMarkMode }
    val colorBlueValue = IntegerValue("Mark-Blue", 255, 0,255).displayable { isMarkMode }

    val colorRedTwoValue = IntegerValue("Mark-Red 2", 0, 0, 255).displayable { isMarkMode && markValue.get() == "Zavz" }
    val colorGreenTwoValue = IntegerValue("Mark-Green 2", 160, 0,255).displayable { isMarkMode && markValue.get() == "Zavz" }
    val colorBlueTwoValue = IntegerValue("Mark-Blue 2", 255, 0,255).displayable { isMarkMode && markValue.get() == "Zavz" }

    private val boxOutline = BoolValue("Mark-Outline", true).displayable { isMarkMode && markValue.get() == "RoundBox" }

    // fake sharp
    private val fakeSharp = BoolValue("FakeSharp", true)

    private val particle = ListValue("Particle",
        arrayOf("None", "Blood", "Lighting", "Fire", "Heart", "Water", "Smoke", "Magic", "Crits"), "Blood")

    private val amount = IntegerValue("ParticleAmount", 5, 1,20) { particle.get() != "None" }

    //Sound
    private val sound = ListValue("Sound", arrayOf("None", "Hit", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop")

    private val volume = FloatValue("Volume", 1f, 0.1f, 5f).displayable { sound.get() != "None" }
    private val pitch = FloatValue("Pitch", 1f, 0.1f,5f).displayable { sound.get() != "None" }

    private val squidValue = BoolValue("Squid", false)

    // variables
    private val targetList = HashMap<EntityLivingBase, Long>()
    private val combat = FDPClient.combatManager
    var random = Random()
    const val DOUBLE_PI = Math.PI * 2
    var start = 0.0
    private var killedAmount = 0
    private val auraESPAnim = SmoothStepAnimation(650, 1.0)
    private var squid: EntitySquid? = null
    private var percent = 0.0
    private val anim: ContinualAnimation = ContinualAnimation()
    val startTime = System.currentTimeMillis()

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        targetList.clear()
        killedAmount = 0
    }

//    @EventTarget
//    fun onKilled(event: EntityKilledEvent) {
//        if (miniWorld.get()) {
//            killedAmount += 1
//            try {
//                val file = "fdpclient/sound/1.wav"
//            } catch (e : Exception) {
//                println("Error with playing sound.")
//                e.printStackTrace()
//            }
//            if (killedAmount >= 7) {
//                killedAmount = 0
//            }
//
//            if (debug.get()) {
//                println(killedAmount)
//            }
//        }
//    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (squidValue.get() && this.squid != null) {
            if (mc.theWorld.loadedEntityList.contains(this.squid)) {
                if (this.percent < 1.0) {
                    this.percent += Math.random() * 0.048
                }
                if (this.percent >= 1.0) {
                    this.percent = 0.0
                    for (i in 0..8) {
                        mc.effectRenderer.emitParticleAtEntity(
                            this.squid,
                            EnumParticleTypes.FLAME
                        )
                    }
                    mc.theWorld.removeEntity(this.squid)
                    this.squid = null
                    return
                }
            } else {
                this.percent = 0.0
            }
            val easeInOutCirc: Double = this.easeInOutCirc(1.0 - this.percent)
            this.anim.animate(easeInOutCirc.toFloat(), 450)
            squid!!.setPositionAndUpdate(
                squid!!.posX,
                squid!!.posY + this.anim.getOutput() * 0.9, squid!!.posZ
            )
        }
        if (this.squid != null && squidValue.get()) {
            squid!!.squidPitch = 0.0f
            squid!!.prevSquidPitch = 0.0f
            squid!!.squidYaw = 0.0f
            squid!!.squidRotation = 90.0f
        }
        val target = FDPClient.combatManager.target ?: return
        if (target.health <= 0.0f && !mc.theWorld.loadedEntityList.contains(
                target
            )
        ) {
            if (squidValue.get()) {
                this.playSound(
                    SoundType.KILL,
                    (mc.gameSettings.getSoundLevel(SoundCategory.MASTER) * mc.gameSettings.getSoundLevel(
                        SoundCategory.ANIMALS
                    ))
                )
                this.squid = EntitySquid(mc.theWorld)
                mc.theWorld.addEntityToWorld(-847815, this.squid)
                squid!!.setPosition(target.posX, target.posY, target.posZ)
            }
        }
    }

    fun playSound(st: SoundType, volume: Float) {
        Thread {
            try {
                val `as` = AudioSystem.getAudioInputStream(
                    BufferedInputStream(
                        Objects.requireNonNull(
                            javaClass.getResourceAsStream(
                                "/resources/sounds/" + st.getName()
                            )
                        )
                    )
                )
                val clip = AudioSystem.getClip()
                clip.open(`as`)
                clip.start()
                val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                gainControl.value = volume
                clip.start()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: LineUnavailableException) {
                e.printStackTrace()
            } catch (e: UnsupportedAudioFileException) {
                e.printStackTrace()
            }
        }.start()
    }

    enum class SoundType(val music: String) {
        KILL("kill.wav");

        fun getName(): String {
            return this.music
        }
    }

    fun easeInOutCirc(x: Double): Double {
        return if (x < 0.5) (1.0 - sqrt(1.0 - (2.0 * x).pow(2.0))) / 2.0 else (sqrt(1.0 - (-2.0 * x + 2.0).pow(2.0)) + 1.0) / 2.0
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val entityLivingBase = combat.target ?: return
        val player = mc.thePlayer ?: return
        when (markValue.get().lowercase()) {
            "rectangle","round" -> {
                if (!(entityLivingBase.isDead || player.getDistanceToEntity(
                        entityLivingBase
                    ) > 10)
                ) {
                    val dst = mc.thePlayer.getSmoothDistanceToEntity(entityLivingBase)
                    val vector2f = RenderUtil.targetESPSPos(entityLivingBase, event.partialTicks) ?: return
                    RenderUtil.drawTargetESP2D(
                        vector2f.x,
                        vector2f.y,
                        getColor(entityLivingBase),
                        getColor(entityLivingBase),
                        1.0f - MathHelper.clamp_float(abs((dst - 6.0f)) / 60.0f, 0.0f, 0.75f),
                        1,
                        auraESPAnim.output.toFloat()
                    )
                }
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val renderManager = mc.renderManager
        val entityLivingBase = combat.target ?: return
        (entityLivingBase.lastTickPosX + (entityLivingBase.posX - entityLivingBase.lastTickPosX) * mc.timer.renderPartialTicks
                - renderManager.renderPosX)
        (entityLivingBase.lastTickPosY + (entityLivingBase.posY - entityLivingBase.lastTickPosY) * mc.timer.renderPartialTicks
                - renderManager.renderPosY)
        (entityLivingBase.lastTickPosZ + (entityLivingBase.posZ - entityLivingBase.lastTickPosZ) * mc.timer.renderPartialTicks
                - renderManager.renderPosZ)
        when (markValue.get().lowercase()) {
            "box" -> drawEntityBoxESP(
                entityLivingBase,
                getColor(combat.target)
            )

            "roundbox" -> drawEntityBox(
                entityLivingBase,
                getColor(combat.target),
                boxOutline.get()
            )

            "head" -> drawPlatformESP(
                entityLivingBase,
                getColor(combat.target)
            )

            "mark" -> drawPlatform(
                entityLivingBase,
                getColor(combat.target)
            )

            "sims" -> drawCrystal(
                entityLivingBase,
                getColor(combat.target).rgb,
                event
            )

            "points" -> points(combat.target ?: return)

//            "rectangle","round" -> {
//                if (!(combat.target!!.isDead || mc.thePlayer.getDistanceToEntity(
//                        combat.target
//                    ) > 10)
//                ) {
//                    val dst = mc.thePlayer.getSmoothDistanceToEntity(combat.target)
//                    val vector2f = RenderUtil.targetESPSPos(combat.target, event.partialTicks)
//                    RenderUtil.drawTargetESP2D(
//                        vector2f.x,
//                        vector2f.y,
//                        getColor(combat.target),
//                        getColor(combat.target),
//                        1.0f - MathHelper.clamp_float(abs((dst - 6.0f)) / 60.0f, 0.0f, 0.75f),
//                        1,
//                        auraESPAnim.output.toFloat()
//                    )
//                }
//            }

            "jello" -> {
                val auraESPAnim = DecelerateAnimation(300, 1.0)
                drawCircle(combat.target!!,event.partialTicks, 0.75,getColor(combat.target).rgb, auraESPAnim.output?.toFloat()!!)
            }

            "zavz" -> drawZavz(
                entityLivingBase,
                event,
                dual = true, // or false based on your requirement
            )
        }
    }
    fun getColor(ent: Entity?): Color {
        if (ent is EntityLivingBase) {
            if (colorModeValue.equals("Health")) return BlendUtils.getHealthColor(
                ent.health,
                ent.maxHealth
            )
            if (colorTeam.get()) {
                val chars = ent.displayName.formattedText.toCharArray()
                var color = Int.MAX_VALUE
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size) continue
                    val index = getColorIndex(chars[i + 1])
                    if (index < 0 || index > 15) continue
                    color = ColorUtils.hexColors[index]
                    break
                }
                return Color(color)
            }
        }
        return when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "LiquidSlowly" -> LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            else -> fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity as? EntityLivingBase ?: return

        repeat(amount.get()) {
            doEffect(target)
        }

        doSound()
        attackEntity(target)
    }

    @EventTarget
    private fun attackEntity(entity: EntityLivingBase) {
        val thePlayer = mc.thePlayer ?: return

        // Extra critical effects
        repeat(3) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isPotionActive(
                    Potion.blindness
                ) && thePlayer.ridingEntity == null || Criticals.handleEvents() && Criticals.msTimer.hasTimePassed(
                    Criticals.delayValue.get().toLong()
                ) && !thePlayer.isInWater && !thePlayer.isInLava && !thePlayer.isInWeb) {
                thePlayer.onCriticalHit(entity)
            }

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(thePlayer.heldItem,
                    entity.creatureAttribute
                ) > 0f || fakeSharp.get()
            ) {
                thePlayer.onEnchantmentCritical(entity)
            }
        }
    }

    private fun doSound() {
        val player = mc.thePlayer

        when (sound.get()) {
            "Hit" -> player.playSound("random.bowhit", volume.get(), pitch.get())
            "Orb" -> player.playSound("random.orb", volume.get(), pitch.get())
            "Pop" -> player.playSound("random.pop", volume.get(), pitch.get())
            "Splash" -> player.playSound("random.splash", volume.get(), pitch.get())
            "Lightning" -> {
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.explode"), 1.0f))
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("ambient.weather.thunder"), 1.0f))
            }
            "Explode" -> player.playSound("random.explode", volume.get(), pitch.get())
        }
    }

    private fun doEffect(target: EntityLivingBase) {
        when (particle.get()) {
            "Blood" -> spawnBloodParticle(EnumParticleTypes.BLOCK_CRACK, target)
            "Crits" -> spawnEffectParticle(EnumParticleTypes.CRIT, target)
            "Magic" -> spawnEffectParticle(EnumParticleTypes.CRIT_MAGIC, target)
            "Lighting" -> spawnLightning(target)
            "Smoke" -> spawnEffectParticle(EnumParticleTypes.SMOKE_NORMAL, target)
            "Water" -> spawnEffectParticle(EnumParticleTypes.WATER_DROP, target)
            "Heart" -> spawnEffectParticle(EnumParticleTypes.HEART, target)
            "Fire" -> spawnEffectParticle(EnumParticleTypes.LAVA, target)
        }
    }

    private fun spawnBloodParticle(particleType: EnumParticleTypes, target: EntityLivingBase) {
        mc.theWorld.spawnParticle(particleType,
            target.posX, target.posY + target.height - 0.75, target.posZ,
            0.0, 0.0, 0.0,
            Block.getStateId(Blocks.redstone_block.defaultState)
        )
    }

    private fun spawnEffectParticle(particleType: EnumParticleTypes, target: EntityLivingBase) {
        mc.effectRenderer.spawnEffectParticle(particleType.particleID,
            target.posX, target.posY, target.posZ,
            target.posX, target.posY, target.posZ
        )
    }

    private fun spawnLightning(target: EntityLivingBase) {
        mc.netHandler.handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity(
            EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ)
        ))
    }
}