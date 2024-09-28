/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule
import net.ccbluex.liquidbounce.ui.client.gui.ClickGUIModule.generateColor
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.Panel
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.elements.ButtonElement
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.Style
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager.get
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import org.lwjgl.input.Mouse
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode

class LiquidBounceStyle : Style() {
    private var mouseDown = false
    private var rightMouseDown = false

    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel) {
        RenderUtils.drawBorderedRect(
            panel.getX().toFloat() - (if (panel.scrollbar) 4 else 0),
            panel.getY().toFloat(),
            panel.getX().toFloat() + panel.width,
            panel.getY().toFloat() + 19 + panel.fade,
            1f,
            Color(255, 255, 255, 90).rgb,
            Int.MIN_VALUE
        )
        val textWidth = Fonts.font35.getStringWidth(
            "§f" + StringUtils.stripControlCodes(
                get(panel.name.replace("%".toRegex(), ""))
            )
        ).toFloat()
        Fonts.font35.drawString(
            "§f" + get(panel.name.replace("%".toRegex(), "")),
            (panel.getX() - (textWidth - 100.0f) / 2f).toInt(),
            panel.getY() + 7,
            -16777216
        )

        if (panel.scrollbar && panel.fade > 0) {
            RenderUtils.drawRect(
                (panel.getX() - 2).toFloat(),
                (panel.getY() + 21).toFloat(),
                panel.getX().toFloat(),
                (panel.getY() + 16 + panel.fade).toFloat(),
                Int.MAX_VALUE
            )
            RenderUtils.drawRect(
                (panel.getX() - 2).toFloat(),
                panel.getY() + 30 + (panel.fade - 24f) / (panel.elements.size - FDPClient.moduleManager.getModule(
                    ClickGUIModule::class.java
                )!!.maxElementsValue.get()) * panel.dragged - 10.0f,
                panel.getX().toFloat(),
                panel.getY() + 40 + (panel.fade - 24.0f) / (panel.elements.size - FDPClient.moduleManager.getModule(
                    ClickGUIModule::class.java
                )!!.maxElementsValue.get()) * panel.dragged,
                Int.MIN_VALUE
            )
        }
    }

    override fun drawDescription(mouseX: Int, mouseY: Int, text: String) {
        val textWidth = Fonts.font35.getStringWidth(get(text.replace("%".toRegex(), "")))

        RenderUtils.drawBorderedRect(
            (mouseX + 9).toFloat(),
            mouseY.toFloat(),
            (mouseX + textWidth + 14).toFloat(),
            (mouseY + Fonts.font35.FONT_HEIGHT + 3).toFloat(),
            1f,
            Color(255, 255, 255, 90).rgb,
            Int.MIN_VALUE
        )
        GlStateManager.resetColor()
        Fonts.font35.drawString(
            get(text.replace("%".toRegex(), "")),
            mouseX + 12,
            mouseY + (Fonts.font35.FONT_HEIGHT) / 2,
            Int.MAX_VALUE
        )
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement) {
        GlStateManager.resetColor()
        Fonts.font35.drawString(
            get(buttonElement.displayName.replace("%".toRegex(), "")), (buttonElement.x - (Fonts.font35.getStringWidth(
                get(buttonElement.displayName.replace("%".toRegex(), ""))
            ) - 100.0f) / 2.0f).toInt(), buttonElement.y + 6, buttonElement.color
        )
    }

    override fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement) {
        val guiColor = generateColor().rgb
        GlStateManager.resetColor()
        Fonts.font35.drawString(
            get(moduleElement.displayName.replace("%".toRegex(), "")),
            (moduleElement.x - (Fonts.font35.getStringWidth(
                get(moduleElement.displayName.replace("%".toRegex(), ""))
            ) - 100.0f) / 2.0f).toInt(),
            moduleElement.y + 6,
            if (moduleElement.module.state) guiColor else Int.MAX_VALUE
        )

        val moduleValues = moduleElement.module.values

        if (moduleValues.isNotEmpty()) {
            Fonts.font35.drawString(
                "+",
                moduleElement.x + moduleElement.width - 8,
                moduleElement.y + (moduleElement.height / 2),
                Color.WHITE.rgb
            )

            if (moduleElement.isShowSettings) {
                var yPos = moduleElement.y + 4
                for (value in moduleValues) {
                    if (!value.displayable) continue

                    if (value is BoolValue) {
                        val text = value.name
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()

                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8

                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )

                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {

                                value.set(!value.get())
                                mc.soundHandler.playSound(
                                    PositionedSoundRecord.create(
                                        ResourceLocation("gui.button.press"),
                                        1.0f
                                    )
                                )
                            }
                        }

                        GlStateManager.resetColor()
                        Fonts.font35.drawString(
                            text,
                            moduleElement.x + moduleElement.width + 6,
                            yPos + 4,
                            if (value.get()) guiColor else Int.MAX_VALUE
                        )
                        yPos += 12
                    } else if (value is ListValue) {

                        val text = value.name
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()

                        if (moduleElement.settingsWidth < textWidth + 16) moduleElement.settingsWidth = textWidth + 16

                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(
                            "§c$text",
                            moduleElement.x + moduleElement.width + 6,
                            yPos + 4,
                            0xffffff
                        )
                        Fonts.font35.drawString(
                            if (value.openList) "-" else "+",
                            (moduleElement.x + moduleElement.width + moduleElement.settingsWidth - (if (value.openList) 5 else 6)).toInt(),
                            yPos + 4,
                            0xffffff
                        )

                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                                value.openList = !value.openList
                                mc.soundHandler.playSound(
                                    PositionedSoundRecord.create(
                                        ResourceLocation("gui.button.press"),
                                        1.0f
                                    )
                                )
                            }
                        }

                        yPos += 12

                        for (valueOfList in value.values) {
                            val textWidth2 = Fonts.font35.getStringWidth(
                                ">$valueOfList"
                            ).toFloat()

                            if (moduleElement.settingsWidth < textWidth2 + 8) moduleElement.settingsWidth =
                                textWidth2 + 8

                            if (value.openList) {
                                RenderUtils.drawRect(
                                    (moduleElement.x + moduleElement.width + 4).toFloat(),
                                    (yPos + 2).toFloat(),
                                    moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                                    (yPos + 14).toFloat(),
                                    Int.MIN_VALUE
                                )

                                if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                                    if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                                        value.set(valueOfList)
                                        mc.soundHandler.playSound(
                                            PositionedSoundRecord.create(
                                                ResourceLocation("gui.button.press"),
                                                1.0f
                                            )
                                        )
                                    }
                                }

                                GlStateManager.resetColor()
                                Fonts.font35.drawString(
                                    ">",
                                    moduleElement.x + moduleElement.width + 6,
                                    yPos + 4,
                                    Int.MAX_VALUE
                                )
                                Fonts.font35.drawString(
                                    valueOfList,
                                    moduleElement.x + moduleElement.width + 14,
                                    yPos + 4,
                                    if (value.get()
                                            .equals(valueOfList, ignoreCase = true)
                                    ) guiColor else Int.MAX_VALUE
                                )
                                yPos += 12
                            }
                        }
                    } else if (value is FloatValue) {
                        val text = value.name + "§f: §c" + round(value.get())
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()

                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8

                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 24).toFloat(),
                            Int.MIN_VALUE
                        )
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 8).toFloat(),
                            (yPos + 18).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4,
                            (yPos + 19).toFloat(),
                            Int.MAX_VALUE
                        )
                        val sliderValue =
                            moduleElement.x + moduleElement.width + ((moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum))
                        RenderUtils.drawRect(
                            8 + sliderValue,
                            (yPos + 15).toFloat(),
                            sliderValue + 11,
                            (yPos + 21).toFloat(),
                            guiColor
                        )

                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4 && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
                            if (Mouse.isButtonDown(0)) {
                                val i = MathHelper.clamp_double(
                                    ((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(),
                                    0.0,
                                    1.0
                                )
                                value.set(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()).toFloat())
                            }
                        }

                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
                        yPos += 22
                    } else if (value is IntegerValue) {
                        val text =
                            value.name + "§f: §c" + (if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get())
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()

                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8

                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 24).toFloat(),
                            Int.MIN_VALUE
                        )
                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 8).toFloat(),
                            (yPos + 18).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4,
                            (yPos + 19).toFloat(),
                            Int.MAX_VALUE
                        )
                        val sliderValue =
                            moduleElement.x + moduleElement.width + ((moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum))
                        RenderUtils.drawRect(
                            8 + sliderValue,
                            (yPos + 15).toFloat(),
                            sliderValue + 11,
                            (yPos + 21).toFloat(),
                            guiColor
                        )

                        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
                            if (Mouse.isButtonDown(0)) {
                                val i = MathHelper.clamp_double(
                                    ((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(),
                                    0.0,
                                    1.0
                                )
                                value.set((value.minimum + (value.maximum - value.minimum) * i).toInt())
                            }
                        }

                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
                        yPos += 22
                    } else if (value is FontValue) {
                        val fontRenderer = value.get()

                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )

                        var displayString = "Font: Unknown"

                        if (fontRenderer is GameFontRenderer) {

                            displayString =
                                "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
                        } else if (fontRenderer === Fonts.minecraftFont) displayString = "Font: Minecraft"
                        else {
                            val objects = Fonts.getFontDetails(fontRenderer)

                            if (objects != null) {
                                displayString =
                                    objects[0].toString() + (if (objects[1] as Int != -1) " - " + objects[1] else "")
                            }
                        }

                        Fonts.font35.drawString(
                            displayString,
                            moduleElement.x + moduleElement.width + 6,
                            yPos + 4,
                            Color.WHITE.rgb
                        )
                        val stringWidth = Fonts.font35.getStringWidth(displayString)

                        if (moduleElement.settingsWidth < stringWidth + 8) moduleElement.settingsWidth =
                            (stringWidth + 8).toFloat()

                        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 4 && mouseY <= yPos + 12) {
                            val fonts = Fonts.getFonts()

                            if (Mouse.isButtonDown(0)) {
                                var i = 0
                                while (i < fonts.size) {
                                    val font = fonts[i]

                                    if (font === fontRenderer) {
                                        i++

                                        if (i >= fonts.size) i = 0

                                        value.set(fonts[i])
                                        break
                                    }
                                    i++
                                }
                            } else {
                                var i = fonts.size - 1
                                while (i >= 0) {
                                    val font = fonts[i]

                                    if (font === fontRenderer) {
                                        i--

                                        if (i >= fonts.size) i = 0

                                        if (i < 0) i = fonts.size - 1

                                        value.set(fonts[i])
                                        break
                                    }
                                    i--
                                }
                            }
                        }

                        yPos += 11
                    } else {
                        val text = value.name + "§f: §c" + value.get()
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()

                        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8

                        RenderUtils.drawRect(
                            (moduleElement.x + moduleElement.width + 4).toFloat(),
                            (yPos + 2).toFloat(),
                            moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                            (yPos + 14).toFloat(),
                            Int.MIN_VALUE
                        )
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
                        yPos += 12
                    }
                }

                moduleElement.updatePressed()
                mouseDown = Mouse.isButtonDown(0)
                rightMouseDown = Mouse.isButtonDown(1)

                if (moduleElement.settingsWidth > 0f && yPos > moduleElement.y + 4) RenderUtils.drawBorderedRect(
                    (moduleElement.x + moduleElement.width + 4).toFloat(),
                    (moduleElement.y + 6).toFloat(),
                    moduleElement.x + moduleElement.width + moduleElement.settingsWidth,
                    (yPos + 2).toFloat(),
                    1f,
                    Int.MIN_VALUE,
                    0
                )
            }
        }
    }

    private fun round(f: Float): BigDecimal {
        var bd = BigDecimal(f.toString())
        bd = bd.setScale(2, RoundingMode.HALF_UP)
        return bd
    }
}
