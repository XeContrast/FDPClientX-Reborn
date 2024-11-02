/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
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
import net.ccbluex.liquidbounce.utils.EntityUtils.getSmoothDistanceToEntity
import net.ccbluex.liquidbounce.utils.animations.Direction
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
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.minecraft.block.Block
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.*
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.abs


@ModuleInfo("CombatVisuals", category = ModuleCategory.VISUAL)
object CombatVisuals : Module() {

    // Mark
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer", "Health"), "Custom")
    val markValue = ListValue("MarkMode", arrayOf("None", "Box", "RoundBox", "Head", "Mark", "Sims","Jello", "Zavz","Rectangle","Round"), "Zavz")
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

    // Sound

    private val miniWorld = BoolValue("MiniWorldSound",false)
    private val particle = ListValue("Particle",
        arrayOf("None", "Blood", "Lighting", "Fire", "Heart", "Water", "Smoke", "Magic", "Crits"), "Blood")

    private val amount = IntegerValue("ParticleAmount", 5, 1,20) { particle.get() != "None" }

    //Sound
    private val sound = ListValue("Sound", arrayOf("None", "Hit", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop")

    private val volume = FloatValue("Volume", 1f, 0.1f, 5f).displayable { sound.get() != "None" }
    private val pitch = FloatValue("Pitch", 1f, 0.1f,5f).displayable { sound.get() != "None" }

    //Dev
    private val debug = BoolValue("Debug",false)

    // variables
    private val targetList = HashMap<EntityLivingBase, Long>()
    private val combat = FDPClient.combatManager
    var random = Random()
    const val DOUBLE_PI = Math.PI * 2
    var start = 0.0
    private var killedAmount = 0
    private val auraESPAnim = SmoothStepAnimation(650, 1.0)

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

            "rectangle","round" -> {
                // No null pointer anymore
                auraESPAnim.setDirection(
                    if (!(combat.target!!.isDead || mc.thePlayer.getDistanceToEntity(
                            combat.target
                        ) > 10)
                    ) Direction.FORWARDS else Direction.BACKWARDS
                )
                if (!auraESPAnim.finished(Direction.BACKWARDS)) {
                    val dst = mc.thePlayer.getSmoothDistanceToEntity(combat.target)
                    val vector2f = RenderUtil.targetESPSPos(combat.target, event.partialTicks) ?: return
                    RenderUtil.drawTargetESP2D(
                        vector2f.x,
                        vector2f.y,
                        getColor(combat.target),
                        getColor(combat.target),
                        1.0f - MathHelper.clamp_float(abs((dst - 6.0f)) / 60.0f, 0.0f, 0.75f),
                        1,
                        auraESPAnim.output.toFloat()
                    )
                }
            }

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