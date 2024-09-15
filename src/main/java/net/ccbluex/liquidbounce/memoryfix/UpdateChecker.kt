//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package net.ccbluex.liquidbounce.memoryfix

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumTypeAdapterFactory
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.ForgeVersion
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.Consumer

class UpdateChecker(private val url: String, private val callback: Consumer<UpdateResponse>) : Thread() {
    private val gson: Gson = GsonBuilder().registerTypeHierarchyAdapter(
        IChatComponent::class.java, IChatComponent.Serializer()
    ).registerTypeHierarchyAdapter(ChatStyle::class.java, ChatStyle.Serializer())
        .registerTypeAdapterFactory(EnumTypeAdapterFactory()).setFieldNamingPolicy(
        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
    ).create()

    override fun run() {
        var retry = 0

        while (retry < 3) {
            try {
                val response = this.check(this.url)

                try {
                    callback.accept(response)
                } catch (var5: Exception) {
                    var5.printStackTrace()
                }

                return
            } catch (var6: Exception) {
                println("GET " + this.url + " failed:")
                println(var6.toString())

                try {
                    sleep(10000L)
                } catch (var4: InterruptedException) {
                    return
                }

                ++retry
            }
        }
    }

    @Throws(IOException::class)
    private fun check(url: String): UpdateResponse {
        var con: HttpURLConnection? = null

        val var7: Any
        try {
            con = URL(url).openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            val agent =
                "Java/" + System.getProperty("java.version") + " " + "Forge/" + ForgeVersion.getVersion() + " " + System.getProperty(
                    "os.name"
                ) + " " + System.getProperty("os.arch") + " "
            con.setRequestProperty("User-Agent", agent)
            val response = con.responseCode
            if (response != 200) {
                throw IOException("HTTP $response")
            }

            val `in` = InputStreamReader(con.inputStream, "UTF-8")
            var var6: Throwable? = null

            try {
                var7 = gson.fromJson(`in`, UpdateResponse::class.java) as UpdateResponse
            } catch (var23: Throwable) {
                var6 = var23
                throw var23
            } finally {
                if (var6 != null) {
                    try {
                        `in`.close()
                    } catch (var22: Throwable) {
                        var6.addSuppressed(var22)
                    }
                } else {
                    `in`.close()
                }
            }
        } finally {
            con?.disconnect()
        }

        return var7 as UpdateResponse
    }

    class UpdateResponse(@JvmField val updateMessage: IChatComponent)
}
