/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.misc

import org.apache.commons.io.IOUtils
import java.util.*
import kotlin.text.Charsets.UTF_8

object StringUtils {
    private val pinyinMap: MutableMap<String, String> = HashMap()
    private val airCache = HashMap<String, String>()

    @JvmOverloads
    fun toCompleteString(args: Array<String>, start: Int, join: String? = " "): String {
        if (args.size <= start) return ""

        return java.lang.String.join(join, *Arrays.copyOfRange(args, start, args.size))
    }

    fun replace(string: String, searchChars: String, replaceChars: String?): String {
        var replaceChars = replaceChars
        if (string.isEmpty() || searchChars.isEmpty() || searchChars == replaceChars) return string

        if (replaceChars == null) replaceChars = ""

        val stringLength = string.length
        val searchCharsLength = searchChars.length
        val stringBuilder = StringBuilder(string)

        for (i in 0 until stringLength) {
            val start = stringBuilder.indexOf(searchChars, i)

            if (start == -1) {
                if (i == 0) return string

                return stringBuilder.toString()
            }

            stringBuilder.replace(start, start + searchCharsLength, replaceChars)
        }

        return stringBuilder.toString()
    }

    fun toPinyin(inString: String, fill: String?): String {
        if (pinyinMap.isEmpty()) {
            try {
                val dict: Array<String> = IOUtils.toString(
                    StringUtils::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/misc/pinyin"),
                    UTF_8
                ).split(";".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                for (word in dict) {
                    val wordData = word.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    pinyinMap[wordData[0]] = wordData[1]
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val strSections = inString.split("".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val result = StringBuilder()
        var lastIsPinyin = false
        for (section in strSections) {
            if (pinyinMap.containsKey(section)) {
                result.append(fill)
                result.append(pinyinMap[section])
                lastIsPinyin = true
            } else {
                if (lastIsPinyin) {
                    result.append(fill)
                }
                result.append(section)
                lastIsPinyin = false
            }
        }
        return result.toString()
    }

    fun injectAirString(str: String): String? {
        if (airCache.containsKey(str)) return airCache[str]

        val stringBuilder = StringBuilder()

        var hasAdded = false
        for (c in str.toCharArray()) {
            stringBuilder.append(c)
            if (!hasAdded) stringBuilder.append('\uF8FF')
            hasAdded = true
        }

        val result = stringBuilder.toString()
        airCache[str] = result

        return result
    }

    operator fun String?.contains(substrings: Array<String>): Boolean {
        val lowerCaseString = this?.lowercase() ?: return false
        return substrings.any { it in lowerCaseString }
    }
}
