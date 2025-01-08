package net.ccbluex.liquidbounce.features.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.Element.Companion.MAX_GRADIENT_COLORS
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationHelper
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.gui.FontRenderer
import java.awt.Color
import java.util.*
import kotlin.jvm.internal.Intrinsics
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class Value<T>(val name: String, var value: T,var canDisplay: () -> Boolean) : ReadWriteProperty<Any?, T> {
    val default = value
    var textHovered: Boolean = false

    private var displayableFunc: () -> Boolean = { canDisplay.invoke() }

    fun displayable(func: () -> Boolean): Value<T> {
        displayableFunc = func
        return this
    }

    val stateDisplayable: Boolean
        get() = displayableFunc()

    val displayableFunction: () -> Boolean
        get() = displayableFunc

    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
            FDPClient.configManager.smartSave()
        } catch (e: Exception) {
            ClientUtils.logError("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    // Support for delegating values using the `by` keyword.
    override operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        set(value)
    }

    fun get() = value

    fun setDefault() {
        value = default
    }

    open fun changeValue(value: T) {
        this.value = value
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}

    // this is better api for ListValue and TextValue

    open class ColorValue(name: String, value: Int, displayable: () -> Boolean = { true }) : Value<Int>(name, value,displayable) {
        val minimum: Int = -10000000
        val maximum: Int = 1000000
        fun set(newValue: Number) {
            set(newValue.toInt())
        }
        override fun toJson() = JsonPrimitive(value)
        override fun fromJson(element: JsonElement) {
            if (element.isJsonPrimitive)
                value = element.asInt
        }
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (value is String && other is String) {
            return (value as String).equals(other, true)
        }
        return value?.equals(other) ?: false
    }

    fun contains(text: String/*, ignoreCase: Boolean*/): Boolean {
        return if (value is String) {
            (value as String).contains(text, true)
        } else {
            false
        }
    }

    private var Expanded = false

    open fun getExpanded(): Boolean {
        return Expanded
    }

    open fun setExpanded(b: Boolean) {
        this.Expanded
    }

    open fun isExpanded(): Boolean {
        return Expanded
    }


    open fun getAwtColor(): Color {
        return Color((this as Value<Number>).value.toInt(), true)
    }

    open fun ColorValue(name: String, value: Int) {
        Intrinsics.checkParameterIsNotNull(name, "name")
        ColorValue(name, value)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + canDisplay.hashCode()
        result = 31 * result + (default?.hashCode() ?: 0)
        result = 31 * result + textHovered.hashCode()
        result = 31 * result + displayableFunc.hashCode()
        result = 31 * result + Expanded.hashCode()
        return result
    }
}

/**
 * Text value represents a value with a string
 */
open class TextValue(name: String, value: String,displayable: () -> Boolean = { true }) : Value<String>(name, value,displayable) {
    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asString
        }

    }
    fun append(o: Any): TextValue {
        set(get() + o)
        return this
    }

}

/**
 * List value represents a selectable list of values
 */
open class ListValue(name: String, val values: Array<String>, value: String,displayable: () -> Boolean = { true }) : Value<String>(name, value,displayable) {
    @JvmField
    var openList = false

    @JvmField
    var isShown = false

    var anim=0

    @JvmField
    var open=true

    init {
        this.value = value
    }

    fun listtoggle(){
        openList=!openList;
    }

    fun getModeListNumber(mode: String) = values.indexOf(mode)
    init {
        this.value = value
    }

    fun containsValue(string: String): Boolean {
        return Arrays.stream(values).anyMatch { it.equals(string, ignoreCase = true) }
    }

    override fun changeValue(value: String) {
        for (element in values) {
            if (element.equals(value, ignoreCase = true)) {
                this.value = element
                break
            }
        }
    }

    open fun getModes() : List<String> {
        return this.values.toList()
    }

    open fun getModeGet(i: Int): String {
        return values[i]
    }

    fun isMode(string: String): Boolean {
        return this.value.equals(string, ignoreCase = true)
    }

    fun indexOf(mode: String): Int {
        for (i in values.indices) {
            if (values[i].equals(mode, true)) return i
        }
        return 0
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) changeValue(element.asString)
    }
}

/**
 * Integer value represents a value with a integer
 */
//open class IntegerValue(name: String, value: Int, val minimum: Int = 0, val maximum: Int = Integer.MAX_VALUE) : Value<Int>(name, value) {

open class IntegerValue(name: String, value: Int, val minimum: Int = 0, val maximum: Int = Integer.MAX_VALUE, val suffix: String, displayable: () -> Boolean = { true })
    : Value<Int>(name, value,displayable) {

    constructor(name: String, value: Int, minimum: Int, maximum: Int, displayable: () -> Boolean = { true }): this(name, value, minimum, maximum, "", displayable)
    constructor(name: String, value: Int, minimum: Int, maximum: Int, suffix: String): this(name, value, minimum, maximum, suffix, { true } )
    constructor(name: String, value: Int, minimum: Int, maximum: Int): this(name, value, minimum, maximum, { true } )

    fun set(newValue: Number) {
        set(newValue.toInt())
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asInt
        }
    }
}

/**
 * Block value represents a value with a block
 */
class BlockValue(name: String, value: Int) : IntegerValue(name, value, 1, 197)

/**
 * Bool value represents a value with a boolean
 */
open class BoolValue(name: String, value: Boolean,displayable: () -> Boolean = { true }) : Value<Boolean>(name, value,displayable) {

    val animation = AnimationHelper(this)
    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asBoolean || element.asString.equals("true", ignoreCase = true)
        }
    }
    init {
        animation.animationX = if (value) 5F else -5F
    }
    open fun toggle(){
        this.value = !this.value
    }

}

/**
 * Float value represents a value with a float
 */
open class FloatValue(name: String, value: Float, val minimum: Float = 0F, val maximum: Float = Float.MAX_VALUE, val suffix: String, displayable: () -> Boolean = { true })
    : Value<Float>(name, value,displayable) {

    constructor(name: String, value: Float, minimum: Float, maximum: Float, displayable: () -> Boolean = { true }): this(name, value, minimum, maximum, "", displayable)
    constructor(name: String, value: Float, minimum: Float, maximum: Float, suffix: String): this(name, value, minimum, maximum, suffix, { true } )
    constructor(name: String, value: Float, minimum: Float, maximum: Float): this(name, value, minimum, maximum, { true } )
    fun set(newValue: Number) {
        set(newValue.toFloat())
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asFloat
        }
    }
}

open class  ColorValue(name : String, value: Int,displayable: () -> Boolean = { true }) : Value<Int>(name, value,displayable) {
    open fun getValue(): Int {
        return super.get()
    }

    fun set(newValue: Number) {
        set(newValue.toInt())
    }


    override fun toJson() = JsonPrimitive(getValue())

    override fun fromJson(element: JsonElement) {
        if(element.isJsonPrimitive)
            value = element.asInt
    }

    open fun getHSB(): FloatArray {
        val hsbValues = FloatArray(3)
        val saturation: Float
        val brightness: Float
        var hue: Float
        var cMax: Int = (getValue() ushr 16 and 0xFF).coerceAtLeast(getValue() ushr 8 and 0xFF)
        if (getValue() and 0xFF > cMax) cMax = getValue() and 0xFF
        var cMin: Int = (getValue() ushr 16 and 0xFF).coerceAtMost(getValue() ushr 8 and 0xFF)
        if (getValue() and 0xFF < cMin) cMin = getValue() and 0xFF
        brightness = cMax.toFloat() / 255.0f
        saturation = if (cMax != 0) (cMax - cMin).toFloat() / cMax.toFloat() else 0F
        if (saturation == 0f) {
            hue = 0f
        } else {
            val redC: Float = (cMax - (getValue() ushr 16 and 0xFF)).toFloat() / (cMax - cMin).toFloat()
            // @off
            val greenC: Float = (cMax - (getValue() ushr 8 and 0xFF)).toFloat() / (cMax - cMin).toFloat()
            val blueC: Float = (cMax - (getValue() and 0xFF)).toFloat() / (cMax - cMin).toFloat() // @on
            hue =
                (if (getValue() ushr 16 and 0xFF == cMax) blueC - greenC else if (getValue() ushr 8 and 0xFF == cMax) 2.0f + redC - blueC else 4.0f + greenC - redC) / 6.0f
            if (hue < 0) hue += 1.0f
        }
        hsbValues[0] = hue
        hsbValues[1] = saturation
        hsbValues[2] = brightness
        return hsbValues
    }


}

class FontValue(valueName: String, value: FontRenderer,displayable: () -> Boolean = { true }) : Value<FontRenderer>(valueName, value,displayable) {

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

class ColorSettingsFloat(owner: Any, name: String, val index: Int? = null, generalApply: () -> Boolean = { true }) {
    private val r = FloatValue(
        "$name-R${index ?: ""}",
        if ((index ?: 0) % 3 == 1) 255f else 0f,
        0f,255f
    ).displayable { generalApply() }
    private val g = FloatValue(
        "$name-G${index ?: ""}",
        if ((index ?: 0) % 3 == 2) 255f else 0f,
        0f,255f
    ).displayable { generalApply() }
    private val b = FloatValue(
        "$name-B${index ?: ""}",
        if ((index ?: 0) % 3 == 0) 255f else 0f,
        0f,255f
    ).displayable { generalApply() }

    fun color() = Color(r.get() / 255f, g.get() / 255f, b.get() / 255f)

    init {
        when (owner) {
            is Element -> owner.addConfigurable(this)
            is Module -> owner.addConfigurable(this)
            // Should any other class use this, add here
        }
    }

    companion object {
        fun create(
            owner: Any, name: String, colors: Int = MAX_GRADIENT_COLORS, generalApply: (Int) -> Boolean = { true },
        ): List<ColorSettingsFloat> {
            return (1..colors).map { ColorSettingsFloat(owner, name, it) { generalApply(it) } }
        }
    }
}

class ColorSettingsInteger(
    owner: Any, name: String? = null, val index: Int? = null, withAlpha: Boolean = true,
    zeroAlphaCheck: Boolean = false,
    alphaApply: Boolean? = null, applyMax: Boolean = false, generalApply: () -> Boolean = { true },
) {
    private val string = if (name == null) "" else "$name-"
    private val max = if (applyMax) 255 else 0

    private var red = IntegerValue(
        "${string}R${index ?: ""}",
        max,
        0,255
    ).displayable { generalApply() && (!zeroAlphaCheck || a > 0) }
    private var green = IntegerValue(
        "${string}G${index ?: ""}",
        max,
        0,255
    ).displayable { generalApply() && (!zeroAlphaCheck || a > 0) }
    private var blue = IntegerValue(
        "${string}B${index ?: ""}",
        max,
        0,255
    ).displayable { generalApply() && (!zeroAlphaCheck || a > 0) }
    private var alpha = IntegerValue(
        "${string}Alpha${index ?: ""}",
        255,
        0,255
    ).displayable { alphaApply ?: generalApply() && withAlpha }

    private var r = red.get()
    private var g = green.get()
    private var b = blue.get()
    private var a = alpha.get()

    fun color(a: Int = this.a) = Color(r, g, b, a)

    fun color() = Color(r, g, b, a)

    fun with(r: Int? = null, g: Int? = null, b: Int? = null, a: Int? = null): ColorSettingsInteger {
        r?.let { red.set(it) }
        g?.let { green.set(it) }
        b?.let { blue.set(it) }
        a?.let { alpha.set(it) }

        return this
    }

    fun with(color: Color) = with(color.red, color.green, color.blue, color.alpha)

    init {
        when (owner) {
            is Element -> owner.addConfigurable(this)
            is Module -> owner.addConfigurable(this)
            // Should any other class use this, add here
        }
    }

    companion object {
        fun create(
            owner: Any, name: String, colors: Int, withAlpha: Boolean = true, zeroAlphaCheck: Boolean = true,
            applyMax: Boolean = false, generalApply: (Int) -> Boolean = { true },
        ): List<ColorSettingsInteger> {
            return (1..colors).map {
                ColorSettingsInteger(
                    owner,
                    name,
                    it,
                    withAlpha,
                    zeroAlphaCheck,
                    applyMax = applyMax
                ) { generalApply(it) }
            }
        }
    }
}

fun List<ColorSettingsFloat>.toColorArray(max: Int) = (0 until max).map {
    val colors = this[it].color()

    floatArrayOf(
        colors.red.toFloat() / 255f,
        colors.green.toFloat() / 255f,
        colors.blue.toFloat() / 255f,
        1f
    )
}