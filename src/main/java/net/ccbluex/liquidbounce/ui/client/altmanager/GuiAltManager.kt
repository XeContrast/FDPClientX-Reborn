/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.thealtening.AltService
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiButton
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiScreen
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiTextField
import net.ccbluex.liquidbounce.api.util.WrappedGuiScreen
import net.ccbluex.liquidbounce.api.util.WrappedGuiSlot
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.*
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.altgenerator.GuiMCLeaks
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.altgenerator.GuiTheAltening
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.login.LoginUtils.LoginResult
import net.ccbluex.liquidbounce.utils.login.LoginUtils.login
import net.ccbluex.liquidbounce.utils.login.LoginUtils.loginCracked
import net.ccbluex.liquidbounce.utils.login.MinecraftAccount
import net.ccbluex.liquidbounce.utils.login.UserUtils.isValidTokenOffline
import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.mcleaks.MCLeaks
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import javax.swing.JOptionPane
import kotlin.math.max

class GuiAltManager(private val prevGui: IGuiScreen) : WrappedGuiScreen() {
    @JvmField
    var status: String = "§7Idle..."
    private var loginButton: IGuiButton? = null
    private var randomButton: IGuiButton? = null
    private var altsList: GuiList? = null
    private var searchField: IGuiTextField? = null

    override fun initGui() {
        val textFieldWidth = max((representedScreen.width / 8).toDouble(), 70.0).toInt()

        searchField = classProvider.createGuiTextField(
            2,
            Fonts.font40,
            representedScreen.width - textFieldWidth - 10,
            10,
            textFieldWidth,
            20
        )
        searchField!!.maxStringLength = Int.MAX_VALUE

        altsList = GuiList(representedScreen)
        altsList!!.represented.registerScrollButtons(7, 8)

        var index = -1

        for (i in LiquidBounce.fileManager.accountsConfig.accounts.indices) {
            val minecraftAccount = LiquidBounce.fileManager.accountsConfig.accounts[i]

            if (minecraftAccount != null && (((minecraftAccount.password == null || minecraftAccount.password.isEmpty()) && minecraftAccount.name != null && minecraftAccount.name == mc.session.username) || minecraftAccount.accountName != null && minecraftAccount.accountName == mc.session.username)) {
                index = i
                break
            }
        }

        altsList!!.elementClicked(index, false, 0, 0)
        altsList!!.represented.scrollBy(index * altsList!!.represented.slotHeight)

        val j = 22
        representedScreen.buttonList.add(
            classProvider.createGuiButton(
                1,
                representedScreen.width - 80,
                j + 24,
                70,
                20,
                "Add"
            )
        )
        representedScreen.buttonList.add(
            classProvider.createGuiButton(
                2,
                representedScreen.width - 80,
                j + 24 * 2,
                70,
                20,
                "Remove"
            )
        )
        representedScreen.buttonList.add(
            classProvider.createGuiButton(
                7,
                representedScreen.width - 80,
                j + 24 * 3,
                70,
                20,
                "Import"
            )
        )
        representedScreen.buttonList.add(
            classProvider.createGuiButton(
                12,
                representedScreen.width - 80,
                j + 24 * 4,
                70,
                20,
                "Export"
            )
        )
        representedScreen.buttonList.add(
            classProvider.createGuiButton(
                8,
                representedScreen.width - 80,
                j + 24 * 5,
                70,
                20,
                "Copy"
            )
        )

        representedScreen.buttonList.add(
            classProvider.createGuiButton(
                0,
                representedScreen.width - 80,
                representedScreen.height - 65,
                70,
                20,
                "Back"
            )
        )

        representedScreen.buttonList.add(
            classProvider.createGuiButton(3, 5, j + 24, 90, 20, "Login").also { loginButton = it })
        representedScreen.buttonList.add(
            classProvider.createGuiButton(4, 5, j + 24 * 2, 90, 20, "Random").also { randomButton = it })
        representedScreen.buttonList.add(classProvider.createGuiButton(6, 5, j + 24 * 3, 90, 20, "Direct Login"))
        representedScreen.buttonList.add(classProvider.createGuiButton(88, 5, j + 24 * 4, 90, 20, "Change Name"))

        if (GENERATORS.getOrDefault("mcleaks", true)) representedScreen.buttonList.add(
            classProvider.createGuiButton(
                5,
                5,
                j + 24 * 5 + 5,
                90,
                20,
                "MCLeaks"
            )
        )
        if (GENERATORS.getOrDefault(
                "thealtening",
                true
            )
        ) representedScreen.buttonList.add(classProvider.createGuiButton(9, 5, j + 24 * 6 + 5, 90, 20, "TheAltening"))

        representedScreen.buttonList.add(classProvider.createGuiButton(10, 5, j + 24 * 7 + 5, 90, 20, "Session Login"))
        representedScreen.buttonList.add(classProvider.createGuiButton(11, 5, j + 24 * 8 + 10, 90, 20, "Cape"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        representedScreen.drawBackground(0)

        altsList!!.represented.drawScreen(mouseX, mouseY, partialTicks)

        Fonts.font40.drawCenteredString("AltManager", representedScreen.width / 2.0f, 6f, 0xffffff)
        Fonts.font35.drawCenteredString(
            if (searchField!!.text.isEmpty()) (LiquidBounce.fileManager.accountsConfig.accounts.size.toString() + " Alts") else altsList!!.accounts!!.size.toString() + " Search Results",
            representedScreen.width / 2.0f,
            18f,
            0xffffff
        )
        Fonts.font35.drawCenteredString(status, representedScreen.width / 2.0f, 32f, 0xffffff)
        Fonts.font35.drawStringWithShadow(
            "§7User: §a" + (if (MCLeaks.isAltActive()) MCLeaks.getSession().username else mc.session.username),
            6,
            6,
            0xffffff
        )
        Fonts.font35.drawStringWithShadow(
            "§7Type: §a" + (if (altService.currentService == AltService.EnumAltService.THEALTENING) "TheAltening" else if (MCLeaks.isAltActive()) "MCLeaks" else if (isValidTokenOffline(
                    mc.session.token
                )
            ) "Premium" else "Cracked"), 6, 15, 0xffffff
        )

        searchField!!.drawTextBox()

        if (searchField!!.text.isEmpty() && !searchField!!.isFocused) Fonts.font40.drawStringWithShadow(
            "§7Search...",
            searchField!!.xPosition + 4, 17, 0xffffff
        )


        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: IGuiButton) {
        if (!button.enabled) return

        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiAdd(this)))
            2 -> if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.getSize()) {
                LiquidBounce.fileManager.accountsConfig.removeAccount(altsList!!.accounts!![altsList!!.getSelectedSlot()])
                LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig)
                status = "§aThe account has been removed."

                altsList!!.updateAccounts(searchField!!.text)
            } else status = "§cSelect an account."

            3 -> if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.getSize()) {
                loginButton!!.enabled = false
                randomButton!!.enabled = false

                val thread = Thread({
                    val minecraftAccount =
                        altsList!!.accounts!![altsList!!.getSelectedSlot()]
                    status = "§aLogging in..."
                    status = login(minecraftAccount)

                    loginButton!!.enabled = true
                    randomButton!!.enabled = true
                }, "AltLogin")
                thread.start()
            } else status = "§cSelect an account."

            4 -> {
                if (altsList!!.accounts!!.size <= 0) {
                    status = "§cThe list is empty."
                    return
                }

                val randomInteger = Random().nextInt(altsList!!.accounts!!.size)

                if (randomInteger < altsList!!.getSize()) altsList!!.selectedSlot = randomInteger

                loginButton!!.enabled = false
                randomButton!!.enabled = false

                val thread = Thread({
                    val minecraftAccount = altsList!!.accounts!![randomInteger]
                    status = "§aLogging in..."
                    status = login(minecraftAccount)

                    loginButton!!.enabled = true
                    randomButton!!.enabled = true
                }, "AltLogin")
                thread.start()
            }

            5 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiMCLeaks(this)))
            6 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiDirectLogin(this)))
            7 -> {
                val file = MiscUtils.openFileChooser() ?: return

                val fileReader = FileReader(file)
                val bufferedReader = BufferedReader(fileReader)

                var line: String
                while ((bufferedReader.readLine().also { line = it }) != null) {
                    val accountData = line.split(":".toRegex(), limit = 2).toTypedArray()

                    if (!LiquidBounce.fileManager.accountsConfig.accountExists(accountData[0])) {
                        if (accountData.size > 1) LiquidBounce.fileManager.accountsConfig.addAccount(
                            accountData[0],
                            accountData[1]
                        )
                        else LiquidBounce.fileManager.accountsConfig.addAccount(accountData[0])
                    }
                }

                fileReader.close()
                bufferedReader.close()

                altsList!!.updateAccounts(searchField!!.text)
                LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig)
                status = "§aThe accounts were imported successfully."
            }

            8 -> if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.getSize()) {
                val minecraftAccount =
                    altsList!!.accounts!![altsList!!.getSelectedSlot()]

                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection(minecraftAccount.name + ":" + minecraftAccount.password),
                    null
                )
                status = "§aCopied account into your clipboard."
            } else status = "§cSelect an account."

            88 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiChangeName(this)))
            9 -> mc.displayGuiScreen(classProvider.wrapGuiScreen(GuiTheAltening(this)))
            10 -> mc.displayGuiScreen(
                classProvider.wrapGuiScreen(
                    GuiSessionLogin(
                        this
                    )
                )
            )

            11 -> mc.displayGuiScreen(
                classProvider.wrapGuiScreen(
                    GuiDonatorCape(
                        this
                    )
                )
            )

            12 -> {
                if (LiquidBounce.fileManager.accountsConfig.accounts.size == 0) {
                    status = "§cThe list is empty."
                    return
                }

                val selectedFile = MiscUtils.saveFileChooser()

                if (selectedFile == null || selectedFile.isDirectory) return

                try {
                    if (!selectedFile.exists()) selectedFile.createNewFile()

                    val fileWriter = FileWriter(selectedFile)

                    for (account in LiquidBounce.fileManager.accountsConfig.accounts) {
                        if (account.isCracked) {
                            fileWriter.write(account.name + "\r\n")
                        } else {
                            fileWriter.write(account.name + ":" + account.password + "\r\n")
                        }
                    }

                    fileWriter.flush()
                    fileWriter.close()
                    JOptionPane.showMessageDialog(
                        null,
                        "Exported successfully!",
                        "AltManager",
                        JOptionPane.INFORMATION_MESSAGE
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    MiscUtils.showErrorPopup(
                        "Error", """
     Exception class: ${e.javaClass.name}
     Message: ${e.message}
     """.trimIndent()
                    )
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (searchField!!.isFocused) {
            searchField!!.textboxKeyTyped(typedChar, keyCode)
            altsList!!.updateAccounts(searchField!!.text)
        }

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                mc.displayGuiScreen(prevGui)
                return
            }

            Keyboard.KEY_UP -> {
                var i = altsList!!.getSelectedSlot() - 1
                if (i < 0) i = 0
                altsList!!.elementClicked(i, false, 0, 0)
            }

            Keyboard.KEY_DOWN -> {
                var i = altsList!!.getSelectedSlot() + 1
                if (i >= altsList!!.getSize()) i = altsList!!.getSize() - 1
                altsList!!.elementClicked(i, false, 0, 0)
            }

            Keyboard.KEY_RETURN -> {
                altsList!!.elementClicked(altsList!!.getSelectedSlot(), true, 0, 0)
            }

            Keyboard.KEY_NEXT -> {
                altsList!!.represented.scrollBy(representedScreen.height - 100)
            }

            Keyboard.KEY_PRIOR -> {
                altsList!!.represented.scrollBy(-representedScreen.height + 100)
                return
            }
        }

        representedScreen.superKeyTyped(typedChar, keyCode)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        representedScreen.superHandleMouseInput()

        altsList!!.represented.handleMouseInput()
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        searchField!!.mouseClicked(mouseX, mouseY, mouseButton)

        representedScreen.superMouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun updateScreen() {
        searchField!!.updateCursorCounter()
    }

    private inner class GuiList(prevGui: IGuiScreen) :
        WrappedGuiSlot(mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30) {
        var accounts: MutableList<MinecraftAccount>? = null
        var selectedSlot: Int = 0

        init {
            updateAccounts(null)
        }

        fun updateAccounts(search: String?) {
            var search = search
            if (search.isNullOrEmpty()) {
                this.accounts = LiquidBounce.fileManager.accountsConfig.accounts
                return
            }

            search = search.toLowerCase()

            this.accounts = ArrayList()

            for (account in LiquidBounce.fileManager.accountsConfig.accounts) {
                if (account.name != null && account.name.toLowerCase().contains(search)
                    || account.accountName != null && account.accountName.toLowerCase().contains(search)
                ) {
                    (accounts as ArrayList<MinecraftAccount>).add(account)
                }
            }
        }

        override fun isSelected(id: Int): Boolean {
            return selectedSlot == id
        }

        fun getSelectedSlot(): Int {
            if (selectedSlot > accounts!!.size) selectedSlot = -1
            return selectedSlot
        }

        fun setSelectedSlot(selectedSlot: Int) {
            this.selectedSlot = selectedSlot
        }

        override fun getSize(): Int {
            return accounts!!.size
        }

        override fun elementClicked(var1: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = var1

            if (doubleClick) {
                if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.getSize() && loginButton!!.enabled) {
                    loginButton!!.enabled = false
                    randomButton!!.enabled = false

                    Thread({
                        val minecraftAccount = accounts!![altsList!!.getSelectedSlot()]
                        status = "§aLogging in..."
                        status = "§c" + login(minecraftAccount)

                        loginButton!!.enabled = true
                        randomButton!!.enabled = true
                    }, "AltManagerLogin").start()
                } else status = "§cSelect an account."
            }
        }

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val minecraftAccount = accounts!![id]

            Fonts.font40.drawCenteredString(
                if (minecraftAccount.accountName == null) minecraftAccount.name else minecraftAccount.accountName,
                (representedScreen.width / 2).toFloat(),
                (y + 2).toFloat(),
                Color.WHITE.rgb,
                true
            )
            Fonts.font40.drawCenteredString(
                if (minecraftAccount.isCracked) "Cracked" else (if (minecraftAccount.accountName == null) "Premium" else minecraftAccount.name),
                (representedScreen.width / 2).toFloat(),
                (y + 15).toFloat(),
                if (minecraftAccount.isCracked) Color.GRAY.rgb else (if (minecraftAccount.accountName == null) Color.GREEN.rgb else Color.LIGHT_GRAY.rgb),
                true
            )
        }

        override fun drawBackground() {
        }
    }

    companion object {
        @JvmField
        val altService: AltService = AltService()
        private val GENERATORS: MutableMap<String, Boolean> = HashMap()
        fun loadGenerators() {
            try {
                // Read versions json from cloud
                val jsonElement = JsonParser().parse(get(LiquidBounce.CLIENT_CLOUD + "/generators.json"))

                // Check json is valid object
                if (jsonElement.isJsonObject) {
                    // Get json object of element
                    val jsonObject = jsonElement.asJsonObject

                    jsonObject.entrySet().forEach(Consumer { stringJsonElementEntry: Map.Entry<String, JsonElement> ->
                        GENERATORS[stringJsonElementEntry.key] = stringJsonElementEntry.value.asBoolean
                    })
                }
            } catch (throwable: Throwable) {
                // Print throwable to console
                ClientUtils.getLogger().error("Failed to load enabled generators.", throwable)
            }
        }

        @JvmStatic
        fun login(minecraftAccount: MinecraftAccount?): String {
            if (minecraftAccount == null) return ""

            if (altService.currentService != AltService.EnumAltService.MOJANG) {
                try {
                    altService.switchService(AltService.EnumAltService.MOJANG)
                } catch (e: NoSuchFieldException) {
                    ClientUtils.getLogger().error("Something went wrong while trying to switch alt service.", e)
                } catch (e: IllegalAccessException) {
                    ClientUtils.getLogger().error("Something went wrong while trying to switch alt service.", e)
                }
            }

            if (minecraftAccount.isCracked) {
                loginCracked(minecraftAccount.name)
                MCLeaks.remove()
                return "§cYour name is now §8" + minecraftAccount.name + "§c."
            }

            val result = login(minecraftAccount.name, minecraftAccount.password)
            if (result == LoginResult.LOGGED) {
                MCLeaks.remove()
                val userName = mc.session.username
                minecraftAccount.accountName = userName
                LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig)
                return "§cYour name is now §f§l$userName§c."
            }

            if (result == LoginResult.WRONG_PASSWORD) return "§cWrong password."

            if (result == LoginResult.NO_CONTACT) return "§cCannot contact authentication server."

            if (result == LoginResult.INVALID_ACCOUNT_DATA) return "§cInvalid username or password."

            if (result == LoginResult.MIGRATED) return "§cAccount migrated."

            return ""
        }
    }
}