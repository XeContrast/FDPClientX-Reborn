package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Targets
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color

class MoonTH(inst: Targets) : TargetStyle("Moon",inst,true) {
    override fun drawTarget(entity: EntityLivingBase) {
        updateAnim(entity.health)
        val name = entity.name
        val health = entity.health
        val tWidth = (45F + Fonts.font48.getStringWidth(name)
            .coerceAtLeast(Fonts.font72.getStringWidth(decimalFormat.format(health)))).coerceAtLeast(120F)
        val playerInfo = mc.netHandler.getPlayerInfo(entity.uniqueID)
        // background
        RenderUtils.drawRoundedRect(0F, 0F, tWidth, 40F, 7F, targetInstance.bgColor.rgb)
        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)
        // head
        if (playerInfo != null) {
            Stencil.write(false)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            RenderUtils.fastRoundedRect(4F, 4F, 34F, 34F, 7F)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            Stencil.erase(true)
            drawHead(playerInfo.locationSkin, 4, 4, 30, 30, (1F - targetInstance.getFadeProgress()).toInt().toFloat())
            Stencil.dispose()
        }

        GlStateManager.resetColor()
        GL11.glColor4f(1F, 1F, 1F, 1F)

        Fonts.font48.drawString(name, 40, 7, getColor(-1).rgb)
        Fonts.font32.drawString("$health HP",40,20,Color.WHITE.rgb)
        RenderUtils.drawRoundedRect(40F, 28F, tWidth - 4F, 34F, 3F, targetInstance.barColor.darker(0.5F).rgb)


        Stencil.write(false)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        RenderUtils.fastRoundedRect(40F, 28F, tWidth - 4F, 34F, 3F)
        GL11.glDisable(GL11.GL_BLEND)
        Stencil.erase(true)
        RenderUtils.customRounded(
            40F,
            28F,
            40F + (easingHealth / entity.maxHealth) * (tWidth - 4F),
            34F,
            0F,
            3F,
            3F,
            0F,
            targetInstance.barColor.rgb
        )
        Stencil.dispose()
    }
    override fun getBorder(entity: EntityLivingBase?): Border {
        entity ?: return Border(0F, 0F, 120F, 40F)
        val tWidth = (45F + Fonts.font48.getStringWidth(entity.name)
            .coerceAtLeast(Fonts.font72.getStringWidth(decimalFormat.format(entity.health)))).coerceAtLeast(120F)
        return Border(0F, 0F, tWidth, 40F)
    }
}