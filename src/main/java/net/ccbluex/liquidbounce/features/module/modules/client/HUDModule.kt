/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import cn.hanabi.gui.cloudmusic.ui.MusicOverlayRenderer
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager.height
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule.colorBlueValue
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule.colorGreenValue
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule.colorRedValue
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

@ModuleInfo(name = "HUD", category = ModuleCategory.CLIENT, array = false, defaultOn = true)
object HUDModule : Module() {
    val tabHead = BoolValue("Tab-HeadOverlay", true)
    val shadowValue = ListValue("TextShadowMode", arrayOf("LiquidBounce", "Outline", "Default", "Autumn"), "Default")
    private val clolormode = ListValue("ColorMode", arrayOf("Rainbow", "Light Rainbow", "Static", "Double Color", "Default"), "Light Rainbow")
    private val MusicDisplay = BoolValue("MusicDisplay",true)
    private val eatbar = BoolValue("EatingBar",false)
    val inventoryOnHotbar = BoolValue("InventoryOnHotbar", false)
    private val hueInterpolation = BoolValue("HueInterpolation", false)
    val inventoryParticle = BoolValue("InventoryParticle", false)
    private val blurValue = BoolValue("Blur", false)
    private val HealthValue = BoolValue("Health", true)
    private val mark = ListValue("Mark", arrayOf("FDPCN","FDP","NeverLose", "None"),"FDP")
    val rainbowStartValue = FloatValue("RainbowStart", 0.55f, 0f, 1f)
    val rainbowStopValue = FloatValue("RainbowStop", 0.85f, 0f, 1f)
    val rainbowSaturationValue = FloatValue("RainbowSaturation", 0.45f, 0f, 1f)
    val rainbowBrightnessValue = FloatValue("RainbowBrightness", 0.85f, 0f, 1f)
    val rainbowSpeedValue = IntegerValue("RainbowSpeed", 1500, 500, 7000)
    val arraylistXAxisAnimSpeedValue = IntegerValue("ArraylistXAxisAnimSpeed", 10, 5, 20)
    val arraylistXAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistXAxisAnimType")
    val arraylistXAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistXAxisHotbarAnimOrder").displayable { !arraylistXAxisAnimTypeValue.equals("NONE") }
    val arraylistYAxisAnimSpeedValue = IntegerValue("ArraylistYAxisAnimSpeed", 10, 5, 20)
    val arraylistYAxisAnimTypeValue = EaseUtils.getEnumEasingList("ArraylistYAxisAnimType")
    val arraylistYAxisAnimOrderValue = EaseUtils.getEnumEasingOrderList("ArraylistYAxisHotbarAnimOrder").displayable { !arraylistYAxisAnimTypeValue.equals("NONE") }
    private val fontEpsilonValue = FloatValue("FontVectorEpsilon", 0.5f, 0f, 1.5f)

    private var lastFontEpsilon = 0f
    private var tick: Double = 0.0
    private var idk: Double = 0.0

    /**
     * Renders the HUD.
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (MusicDisplay.get()) MusicOverlayRenderer.INSTANCE.renderOverlay()
        if (mc.currentScreen is GuiHudDesigner) return
        FDPClient.hud.render(false, event.partialTicks)
        when (mark.get().lowercase()) {
            "fdp" -> renderWatermark()
            "fdpcn" -> renderfdpcn()
            "neverlose" -> neverlose()
            else -> {}
        }
        if (eatbar.get()) {
            if (mc.thePlayer.heldItem.item is ItemFood || mc.thePlayer.heldItem
                    .item is ItemPotion
            ) {
                val scaledResolution = StaticStorage.scaledResolution
                val math: Double
                val height: Double = scaledResolution.scaledHeight.toDouble()
                val width: Double = scaledResolution.scaledWidth.toDouble()
                if (mc.gameSettings.keyBindUseItem.pressed) {
                    idk = (1f - (tick / 32))
                    math = (30 * idk)
                } else {
                    math = 0.0
                }
                val left = (width / 2) - math
                val right = (width / 2) + math
                if (mc.gameSettings.keyBindUseItem.pressed) {
                    Fonts.font40.drawString(
                        ((tick / 32) * 100).roundToInt().toString() + "%",
                        (width / 2).toFloat() - 5,
                        (height / 2).toFloat() + 10, Color(220, 220, 220).rgb
                    )
                    RenderUtils.drawRect((width / 2) - 30,(height / 2) + 20,(width / 2) + 30,(height / 2) + 25,Color(0,0,0,100).rgb)
                }

                RenderUtils.drawRect(left, (height / 2) + 20, right, (height / 2) + 25, Color.WHITE.rgb)
            }
        }
        if (HealthValue.get()) mc.fontRendererObj.drawStringWithShadow(
            MathHelper.ceiling_float_int(mc.thePlayer.health).toString(),
            (width / 2 - 4).toFloat(), (height / 2 - 13).toFloat(), if (mc.thePlayer.health <= 15) Color(255, 0, 0).rgb else Color(0, 255, 0).rgb)
        GlStateManager.resetColor()
    }

    /**
     * Renders the watermark.
     */
    private fun renderWatermark() {
        var width = 3
        val colors = getClientColors()
        mc.fontRendererObj.drawStringWithShadow(
            "FDP",
            3.0f,
            3.0f,
            colors[0].rgb
        )
        width += mc.fontRendererObj.getStringWidth("FDP")
        mc.fontRendererObj.drawStringWithShadow(
            "CLIENT",
            width.toFloat(),
            3.0f,
            colors[1].rgb
        )
    }
    private fun getClientName(): String{
        return "FDPClient".substring(0,3)
    }
    private fun renderfdpcn() {
        FontLoaders.F40.drawString(

            getClientName(), 5.0f, 0.0f,Color(255,255,255,220).rgb
        )
        FontLoaders.C16.drawString(
            "Xe", 5F + FontLoaders.F40.getStringWidth("FDP"), 13.0f,Color(255,255,255,220).rgb
        )
        RenderUtils.drawRect(5f,22.5f,70f,22.8f,Color(200,200,200,220).rgb)
        FontLoaders.C14.drawString(
            FDPClient.CLIENT_VERSION + " | "+ FDPClient.VERSIONTYPE, 5.0f, 27.0f,Color(255,255,255,220).rgb
        )
        FontLoaders.C14.drawString(
            "CN 240817 | Reborn!", 5.0f, 37.0f,Color(255,255,255,220).rgb
        )
    }
    private fun neverlose() {
        val str =
            EnumChatFormatting.DARK_GRAY.toString() + " | " + EnumChatFormatting.WHITE + mc.session.username + EnumChatFormatting.DARK_GRAY + " | " + EnumChatFormatting.WHITE + Minecraft.getDebugFPS() + "fps" + EnumChatFormatting.DARK_GRAY + " | " + EnumChatFormatting.WHITE + (if (mc.isSingleplayer) "SinglePlayer" else mc.currentServerData.serverIP)
        RoundedUtil.drawRound(
            6.0f, 6.0f,
            (Fonts.font35.getStringWidth(str) + 8 + Fonts.font40.getStringWidth(
                CLIENT_NAME.uppercase(
                    Locale.getDefault()
                )
            )).toFloat(), 15.0f, 0.0f, Color(19, 19, 19, 230)
        )
        RoundedUtil.drawRound(
            6.0f, 6.0f,
            (Fonts.font35.getStringWidth(str) + 8 + Fonts.font40.getStringWidth(
                CLIENT_NAME.uppercase(
                    Locale.getDefault()
                )
            )).toFloat(), 1.0f, 1.0f, color(8)
        )
        Fonts.font35.drawString(
            str,
            (11 + Fonts.font40.getStringWidth(CLIENT_NAME.uppercase(Locale.getDefault()))).toFloat(), 11.5f, Color.WHITE.rgb
        )
        Fonts.font40.drawString(
            EnumChatFormatting.BOLD.toString() + CLIENT_NAME.uppercase(
                Locale.getDefault()
            ), 9.5f, 11.5f, color(8).rgb
        )
        Fonts.font40.drawString(
            EnumChatFormatting.BOLD.toString() + CLIENT_NAME.uppercase(
                Locale.getDefault()
            ), 10.0f, 12f, Color.WHITE.rgb
        )
    }

    fun color(tick: Int): Color {
        val textColor: Color = fade(5, tick * 20, rainbow(), 1.0f)
        return textColor
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (eatbar.get()) {
            if (mc.thePlayer.heldItem.item is ItemFood || mc.thePlayer.heldItem.item is ItemPotion
            ) {
                if (mc.gameSettings.keyBindUseItem.pressed) {
                    tick++
                } else {
                    tick = 0.0
                }
                if ((tick / 32) >= 1) {
                    tick = 0.0
                    idk = 0.0
                }
            }
        }
        FDPClient.hud.update()
        if (mc.currentScreen == null && lastFontEpsilon != fontEpsilonValue.get()) {
            lastFontEpsilon = fontEpsilonValue.get()
            alert("You need to reload FDPClient to apply changes!")
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        lastFontEpsilon = fontEpsilonValue.get()
    }

    @EventTarget(ignoreCondition = true)
    fun onScreen(event: ScreenEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        if (state && blurValue.get() && !mc.entityRenderer.isShaderActive && event.guiScreen != null &&
            !(event.guiScreen is GuiChat || event.guiScreen is GuiHudDesigner)) mc.entityRenderer.loadShader(
            ResourceLocation(CLIENT_NAME.lowercase() + "/blur.json")
        ) else if (mc.entityRenderer.shaderGroup != null &&
            "fdpclient/blur.json" in mc.entityRenderer.shaderGroup.shaderGroupName) mc.entityRenderer.stopUseShader()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        FDPClient.hud.handleKey('a', event.key)
    }

    private fun getClientColors(): Array<Color> {
        val firstColor: Color
        val secondColor: Color
        when (clolormode.get().lowercase(Locale.getDefault())) {
            "light rainbow" -> {
                firstColor = ColorUtils.rainbowc(15, 1, .6f, 1F, 1F)!!
                secondColor = ColorUtils.rainbowc(15, 40, .6f, 1F, 1F)!!
            }
            "rainbow" -> {
                firstColor = ColorUtils.rainbowc(15, 1, 1F, 1F, 1F)!!
                secondColor = ColorUtils.rainbowc(15, 40, 1F, 1F, 1F)!!
            }
            "double color" -> {
                firstColor =
                    ColorUtils.interpolateColorsBackAndForth(15, 0, Color.PINK, Color.BLUE, hueInterpolation.get())!!
                secondColor =
                    ColorUtils.interpolateColorsBackAndForth(15, 90, Color.PINK, Color.BLUE, hueInterpolation.get())!!
            }
            "static" -> {
                firstColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                secondColor = firstColor
            }
            else -> {
                firstColor = Color(-1)
                secondColor = Color(-1)
            }
        }
        return arrayOf(firstColor, secondColor)
    }

}
