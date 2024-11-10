/*
 * 类似木糖醇的压缩timer，属于仿制品并且有瑕疵
 * by Pursue(193923709)
 * 开源地址：https://github.com/FKtzs/Nattalie
 * 群号：673246810
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

@ModuleInfo(name = "TimerBalancer", description = "短时间内叠够足够的负数time后立刻释放", category = ModuleCategory.WORLD)
class TimeBalancer : Module() {

    private val timer = FloatValue("TimerSpeed", 4F, 0.1F, 10F) // Timer速度
    private val time = IntegerValue("TimerMaxSet", 5, 0, 30) // 等待积攒时间
    private val tie = IntegerValue("TimerSet", 5, 0, 30) // 释放时间

    private val counter = BoolValue("Counter",true) // 渲染框架
    private val setaX = ListValue("StartXMode", arrayOf("-","+"), "-")
    private val xV = IntegerValue("StartX", 75, 0, 450)
    private val setY = ListValue("StartYMode", arrayOf("-","+"), "+")
    private val yV = IntegerValue("StartY", 20, 0, 450)

    private val fontR = IntegerValue("FontR", 255, 0, 255)
    private val fontG = IntegerValue("FontG", 255, 0, 255)
    private val fontB = IntegerValue("FontB", 255, 0, 255)
    private val fontA = IntegerValue("FontA", 255, 0, 255)

    private val rectangleR = IntegerValue("RectangleR", 0, 0, 255)
    private val rectangleG = IntegerValue("RectangleG", 0, 0, 255)
    private val rectangleB = IntegerValue("RectangleB", 0, 0, 255)
    private val rectangleA = IntegerValue("RectangleA", 125, 0, 255)

    private val setFrame = ListValue("FrameMode", arrayOf("<","^"), "^")
    private val frameR = IntegerValue("FrameR", 0, 0, 255)
    private val frameG = IntegerValue("FrameG", 255, 0, 255)
    private val frameB = IntegerValue("FrameB", 255, 0, 255)
    private val frameA = IntegerValue("FrameA", 255, 0, 255)

    private var lastMS = 0L
    private var s = 0
    private var m = 0
    private var timr = false
    private var progress = 0f


    override fun onEnable() {
        progress = 0f
    }

    override fun onDisable() {
        s = 0
        m = 0
        timr = false
        mc.timer.timerSpeed = 1F
    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (timr) {
            s++
        } else {
            m++
        }
        if (m == time.get()) {
            timr = true
            mc.timer.timerSpeed = timer.get()
            m = 0
            s = 0
        }
        if (s == tie.get()) {
            timr = false
            mc.timer.timerSpeed = 1F
            m = 0
            s = 0
        }
    }

    @EventTarget
    fun onTick(event: GameTickEvent?) {
        val timer = mc.timer ?: return
        try {
            val f = timer.javaClass.getDeclaredField("field_74277_g")
            f.setAccessible(true)
            val t = f[timer] as Long
            f[timer] = t + 50L
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        progress = (System.currentTimeMillis() - lastMS).toFloat() / 100f
        if (progress >= 1) progress = 1f
        val scaledResolution = ScaledResolution(mc)
        val counterMode = counter.get()
        val startX = if (setaX.get() == "-") scaledResolution.scaledWidth / 2 - xV.get() else scaledResolution.scaledWidth / 2 + xV.get()
        val startY = if (setY.get() == "-") scaledResolution.scaledHeight / 2 - yV.get() else scaledResolution.scaledHeight / 2 + yV.get()

        if (counterMode) {
            RenderUtils.drawShadow(startX.toFloat(), startY.toFloat(), 160f, 21f)
            GlStateManager.resetColor()

            RoundedUtil.drawRound(startX.toFloat(), startY.toFloat(), 160f, 22f, 3F, Color(rectangleR.get(), rectangleG.get(), rectangleB.get(), rectangleA.get()))
            when (setFrame.get().toLowerCase()) {
                "^" -> {
                    RoundedUtil.drawRound(startX.toFloat(), startY.toFloat(), 160f, 3f, 3F, Color(frameR.get(), frameG.get(),frameB.get(), frameA.get()))
                    GlStateManager.resetColor()
                    Fonts.font40.drawString("Timer：$m ReleaseTime：$s", (startX - 4 + 36).toFloat(), (startY + 7.5).toFloat(), Color(fontR.get(),fontG.get(),fontB.get(),fontA.get()).rgb)
                    GlStateManager.resetColor()
                }
                "<" -> {
                    RoundedUtil.drawRound(startX.toFloat(), startY.toFloat(), 3f, 22f, 3F, Color(frameR.get(), frameG.get(),frameB.get(), frameA.get()))
                    GlStateManager.resetColor()
                    Fonts.font40.drawString("Timer：$m ReleaseTime：$s", (startX - 4 + 36).toFloat(), (startY + 6.5).toFloat(), Color(fontR.get(),fontG.get(),fontB.get(),fontA.get()).rgb)
                    GlStateManager.resetColor()
                }
            }
            GlStateManager.resetColor()
        }
    }
}