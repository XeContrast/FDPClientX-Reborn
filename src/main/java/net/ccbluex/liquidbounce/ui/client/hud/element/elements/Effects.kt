/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FontValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.realpha
import net.ccbluex.liquidbounce.utils.render.Colors
import net.ccbluex.liquidbounce.utils.render.PotionData
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Translate
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@ElementInfo(name = "Effects")
class Effects : Element() {
    private val modeValue: ListValue = ListValue("Mode", arrayOf("FDP", "Default","Vanilla"), "FDP")
    val shadow: BoolValue = BoolValue("Shadow", true)
    private val iconValue: BoolValue = BoolValue("Icon", true)
    private val fontValue = FontValue("Font", Fonts.font35).displayable {modeValue.equals("Vanilla")}
    private val anotherStyle = BoolValue("New", false).displayable { modeValue == {"Vanilla"} }
    private val nameValue: BoolValue = BoolValue("Name", true)
    private val colorValue: BoolValue = BoolValue("Color", false)

    override var x: Double = 10.0
    override var y: Double = 100.0
    private val potionMap: MutableMap<Potion, PotionData> = HashMap()

    private fun draw(): Border? {
        if (modeValue.get() == "Vanilla") {
            val fontRenderer = fontValue.get()

            var y = 0F
            var width = 0F

            assumeNonVolatile = true

            for (effect in mc.thePlayer.activePotionEffects) {
                if (side.vertical == Side.Vertical.DOWN)
                    y -= fontRenderer.FONT_HEIGHT + if (anotherStyle.get()) 1F else 0F

                val potion = Potion.potionTypes[effect.potionID]

                val number = when {
                    effect.amplifier == 1 -> "II"
                    effect.amplifier == 2 -> "III"
                    effect.amplifier == 3 -> "IV"
                    effect.amplifier == 4 -> "V"
                    effect.amplifier == 5 -> "VI"
                    effect.amplifier == 6 -> "VII"
                    effect.amplifier == 7 -> "VIII"
                    effect.amplifier == 8 -> "IX"
                    effect.amplifier == 9 -> "X"
                    effect.amplifier > 10 -> "X+"
                    else -> "I"
                }

                val duration = if (effect.isPotionDurationMax) 30 else effect.duration / 20
                val name = if (anotherStyle.get())
                    "${I18n.format(potion.name)} $number ${if (duration < 15) "§c" else if (duration < 30) "§6" else "§7"}${Potion.getDurationString(effect)}"
                else
                    "${I18n.format(potion.name)} $number§f: §7${Potion.getDurationString(effect)}"
                val stringWidth = fontRenderer.getStringWidth(name).toFloat()

                if (side.horizontal == Side.Horizontal.RIGHT) {
                    if (width > -stringWidth)
                        width = -stringWidth
                } else {
                    if (width < stringWidth)
                        width = stringWidth
                }

                when (side.horizontal) {
                    Side.Horizontal.RIGHT -> fontRenderer.drawString(name, -stringWidth, y + if (side.vertical == Side.Vertical.UP) -fontRenderer.FONT_HEIGHT.toFloat() else 0F, potion.liquidColor, shadow.get())
                    Side.Horizontal.LEFT, Side.Horizontal.MIDDLE -> fontRenderer.drawString(name, 0F, y + if (side.vertical == Side.Vertical.UP) -fontRenderer.FONT_HEIGHT.toFloat() else 0F, potion.liquidColor, shadow.get())
                }

                if (side.vertical == Side.Vertical.UP)
                    y += fontRenderer.FONT_HEIGHT + if (anotherStyle.get()) 1F else 0F
            }

            assumeNonVolatile = false

            if (width == 0F)
                width = if (side.horizontal == Side.Horizontal.RIGHT) -40F else 40F

            if (y == 0F) // alr checked above
                y = if (side.vertical == Side.Vertical.UP) fontRenderer.FONT_HEIGHT.toFloat() else -fontRenderer.FONT_HEIGHT.toFloat()

            return Border(0F, 0F, width, y)
        }
        if ((modeValue.get() == "FDP")) {
            GlStateManager.pushMatrix()
            var y = 0
            for (potionEffect: PotionEffect in mc.thePlayer.activePotionEffects) {
                val potion: Potion = Potion.potionTypes[potionEffect.potionID]
                val name: String = I18n.format(potion.name)
                val potionData: PotionData?
                if (potionMap.containsKey(potion) && potionMap[potion]!!.level == potionEffect.amplifier) potionData =
                    potionMap[potion]
                else potionMap[potion] =
                    (PotionData(Translate(0f, -40f + y), potionEffect.amplifier).also {
                        potionData = it
                    })
                var flag = true
                for (checkEffect: PotionEffect in mc.thePlayer.activePotionEffects) if (checkEffect.amplifier == potionData!!.level) {
                    flag = false
                    break
                }
                if (flag) potionMap.remove(potion)
                var potionTime: Int
                var potionMaxTime: Int
                try {
                    potionTime =
                        Potion.getDurationString(potionEffect).split(":".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0].toInt()
                    potionMaxTime =
                        Potion.getDurationString(potionEffect).split(":".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[1].toInt()
                } catch (ignored: Exception) {
                    potionTime = 100
                    potionMaxTime = 1000
                }
                val lifeTime: Int = (potionTime * 60 + potionMaxTime)
                if (potionData!!.getMaxTimer() == 0 || lifeTime > potionData.getMaxTimer()
                        .toDouble()
                ) potionData.maxTimer = lifeTime
                var state = 0.0f
                if (lifeTime >= 0.0) state =
                    (lifeTime / (potionData.getMaxTimer().toFloat()).toDouble() * 100.0).toFloat()
                val position: Int = Math.round(potionData.translate.y + 5)
                state = max(state.toDouble(), 2.0).toFloat()
                potionData.translate.interpolate(0f, y.toFloat(), 0.1)
                potionData.animationX = getAnimationState(
                    potionData.getAnimationX().toDouble(),
                    (1.2f * state).toDouble(),
                    (max(
                        10.0,
                        (abs((potionData.animationX - 1.2f * state).toDouble()) * 15.0f)
                    ) * 0.3f)
                ).toFloat()

                RenderUtils.drawRect(
                    0f, potionData.translate.y, 120f, potionData.translate.y + 30f, realpha.reAlpha(
                        Colors.GREY.c, 0.1f
                    )
                )
                RenderUtils.drawRect(
                    0f, potionData.translate.y, potionData.animationX, potionData.translate.y + 30f, realpha.reAlpha(
                        (Color(34, 24, 20)).brighter().rgb, 0.3f
                    )
                )
                RenderUtils.drawShadow(0f, Math.round(potionData.translate.y).toFloat(), 120f, 30f)
                val posY: Float = potionData.translate.y + 13f
                Fonts.font40.drawString(
                    name + " " + intToRomanByGreedy(potionEffect.amplifier + 1),
                    29f,
                    posY - mc.fontRendererObj.FONT_HEIGHT,
                    realpha.reAlpha(
                        Colors.WHITE.c, 0.8f
                    )
                )
                Fonts.font35.drawString(
                    Potion.getDurationString(potionEffect), 29f, posY + 4.0f, realpha.reAlpha(
                        (Color(200, 200, 200)).rgb, 0.5f
                    )
                )
                if (potion.hasStatusIcon()) {
                    GlStateManager.pushMatrix()
                    GL11.glDisable(2929)
                    GL11.glEnable(3042)
                    GL11.glDepthMask(false)
                    OpenGlHelper.glBlendFunc(770, 771, 1, 0)
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                    val statusIconIndex: Int = potion.statusIconIndex
                    mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
                    mc.ingameGUI.drawTexturedModalRect(
                        6f,
                        (position + 1).toFloat(), statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18
                    )
                    GL11.glDepthMask(true)
                    GL11.glDisable(3042)
                    GL11.glEnable(2929)
                    GlStateManager.popMatrix()
                }
                y -= 35
            }
            GlStateManager.popMatrix()
            return Border(0f, 0f, 120f, 30f)
        }
        if ((modeValue.get() == "Default")) {
            val xOffset = 21
            var yOffset = 14

            val activePotions: Collection<PotionEffect> = mc.thePlayer.activePotionEffects
            val sortedPotions: ArrayList<PotionEffect> = ArrayList(activePotions)
            sortedPotions.sortWith(Comparator.comparingInt { potion: PotionEffect -> -potion.duration })

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            GlStateManager.disableLighting()

            val fontRenderer: FontRenderer = font.get()

            for (potion: PotionEffect in sortedPotions) {
                val effect: Potion = checkNotNull(Potion.potionTypes[potion.potionID])
                if (effect.hasStatusIcon() && iconValue.get()) {
                    drawStatusIcon(
                        xOffset,
                        yOffset,
                        effect.statusIconIndex % 8 * 18,
                        198 + effect.statusIconIndex / 8 * 18
                    )
                }

                if (nameValue.get()) {
                    drawPotionName(potion, effect, xOffset, yOffset, fontRenderer)
                }

                if (nameValue.get()) {
                    drawPotionDuration(potion, xOffset, yOffset, fontRenderer)
                }

                drawPotionDuration(potion, xOffset, yOffset, fontRenderer)

                yOffset += fontRenderer.FONT_HEIGHT * 2 + 4 // Add some space between the effects
            }

            val height: Float = (yOffset - 4).toFloat()

            val width = 100.0f
            return Border(0F, 0F, width, height)
        }
        return null
    }

    private fun intToRomanByGreedy(num: Int): String {
        var num: Int = num
        val values: IntArray = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val symbols: Array<String> = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        val stringBuilder: StringBuilder = StringBuilder()
        var i = 0
        while (i < values.size && num >= 0) {
            while (values[i] <= num) {
                num -= values[i]
                stringBuilder.append(symbols[i])
            }
            i++
        }

        return stringBuilder.toString()
    }

    private fun getAnimationState(animation: Double, finalState: Double, speed: Double): Double {
        var animation: Double = animation
        val add: Float = (0.01 * speed).toFloat()
        if (animation < finalState) {
            if (animation + add < finalState) animation += add.toDouble()
            else animation = finalState
        } else {
            if (animation - add > finalState) animation -= add.toDouble()
            else animation = finalState
        }
        return animation
    }

    private fun drawStatusIcon(xOffset: Int, yOffset: Int, textureX: Int, textureY: Int) {
        mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
        RenderUtils.drawTexturedModalRect(x.toInt() + xOffset - 20, y.toInt() + yOffset, textureX, textureY, 18, 18, 0f)
    }

    private fun drawPotionName(
        potion: PotionEffect,
        effect: Potion,
        xOffset: Int,
        yOffset: Int,
        fontRenderer: FontRenderer
    ) {
        var level: String? = I18n.format(effect.name)
        if (potion.amplifier > 0) {
            val amplifier: Int = min((potion.amplifier + 1).toDouble(), 4.0).toInt()
            level += " " + I18n.format("enchantment.level.$amplifier")
        }

        val potionColor: Int =
            if (colorValue.get()) getPotionColor(potion) else Color.WHITE.rgb // Use white color when colorValue is false
        fontRenderer.drawString(level, (x + xOffset).toFloat(), (y + yOffset).toFloat(), potionColor, shadow.get())
    }

    private fun drawPotionDuration(potion: PotionEffect, xOffset: Int, yOffset: Int, fontRenderer: FontRenderer) {
        val durationString: String = Potion.getDurationString(potion)
        val potionColor: Int =
            if (colorValue.get()) getPotionColor(potion) else Color.WHITE.rgb // Use white color when colorValue is false
        fontRenderer.drawString(
            durationString,
            (x + xOffset).toFloat(),
            (y + yOffset + (if (nameValue.get()) fontRenderer.FONT_HEIGHT.toFloat() else fontRenderer.FONT_HEIGHT.toFloat() / 2)).toFloat(),
            potionColor,
            shadow.get()
        )
    }

    private fun getPotionColor(potion: PotionEffect): Int {
        return if (potion.duration < 200) {
            Color(215, 59, 59).rgb
        } else if (potion.duration < 400) {
            Color(231, 143, 32).rgb
        } else {
            Color(172, 171, 171).rgb
        }
    }


    override fun drawElement(partialTicks: Float): Border? {
        return draw()
    }

    companion object {
        var font: FontValue = FontValue("Font", Fonts.minecraftFont)
    }
}