package net.ccbluex.liquidbounce.features.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.gui.FontRenderer

/**
 * Font value represents a value with a font
 */
class FontValue(valueName: String, value: FontRenderer) : Value<FontRenderer>(valueName, value) {

    private val cache: MutableList<Pair<String, FontRenderer>> = mutableListOf()
    private fun updateCache() {
        cache.clear()
        for (fontOfFonts in Fonts.getFonts()) {
            val details = Fonts.getFontDetails(fontOfFonts) ?: continue
            val name = details[0].toString()
            val size = details[1].toString().toInt()
            val format = "$name $size"

            cache.add(format to fontOfFonts)
        }

        cache.sortBy { it.first }
    }
    private fun getAllFontDetails(): Array<Pair<String, FontRenderer>> {
        if (cache.size == 0) updateCache()

        return cache.toTypedArray()
    }
    override fun toJson(): JsonElement {
        val fontDetails = Fonts.getFontDetails(value)
        val valueObject = JsonObject()
        valueObject.addProperty("fontName", fontDetails[0] as String)
        valueObject.addProperty("fontSize", fontDetails[1] as Int)
        return valueObject
    }

    override fun fromJson(element: JsonElement) {
        if (!element.isJsonObject) return
        val valueObject = element.asJsonObject
        value = Fonts.getFontRenderer(valueObject["fontName"].asString, valueObject["fontSize"].asInt)
    }

    fun set(name: String): Boolean {
        if (name.equals("Minecraft", true)) {
            set(Fonts.minecraftFont)
            return true
        } else if (name.contains(" - ")) {
            val spiced = name.split(" - ")
            set(Fonts.getFontRenderer(spiced[0], spiced[1].toInt()) ?: return false)
            return true
        }
        return false
    }
    val values
        get() = getAllFontDetails().map { it.second }

    fun setByName(name: String) {
        set((getAllFontDetails().find { it.first.equals(name, true)} ?: return).second )
    }
}