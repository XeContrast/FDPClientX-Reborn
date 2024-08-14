/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.potion.PotionHealth
import java.awt.Color
import kotlin.math.pow
import kotlin.math.roundToInt

@ElementInfo(name = "HealthRender")
class HealthRender(
    x: Double = -8.0, y: Double = 57.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {
    private val globalAnimSpeed = FloatValue("Health Speed", 3F, 0.1F, 5F)
    var easingHealth = 0f
    fun updateAnim(health: Float) {
        easingHealth += ((health - easingHealth) / 2.0F.pow(10.0F - globalAnimSpeed.get())) * RenderUtils.deltaTime
    }

    override fun drawElement(partialTicks: Float): Border {
        updateAnim(mc.thePlayer.health)

        val health = mc.thePlayer!!.health
        val maxhealth = mc.thePlayer!!.maxHealth
        val exam = mc.thePlayer!!.absorptionAmount

        RoundedUtil.drawRound(0f, 0f, 120f, 15f, 3F, Color(0, 0, 0, 80))

        //
        RenderUtils.drawRoundedRect(0f, 0f, (health / maxhealth) * 120f, 15f, 3F, Color(255, 0, 0, 80).rgb)
        RoundedUtil.drawRound(0f, 0f, (exam / maxhealth) * 120f, 15f, 3F, Color(253, 230, 7, 97))

        Fonts.minecraftFont.drawString((health + exam).roundToInt().toString(),0,0, BlendUtils.getHealthColor(
            health,
            maxhealth
        ).rgb
        )
        Fonts.minecraftFont.drawString(
            (((health + exam) / maxhealth) * 100).roundToInt().toString() + "%",
            0,
            8,
            BlendUtils.getHealthColor(
                health,
                maxhealth
            ).rgb
        )
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        return Border(0f, 0f, 120f, 15f)
    }
}