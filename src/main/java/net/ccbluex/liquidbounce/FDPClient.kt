/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.handler.macro.MacroManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.config.ConfigManager
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.handler.discord.DiscordRPC
import net.ccbluex.liquidbounce.handler.network.BungeeCordSpoof
import net.ccbluex.liquidbounce.handler.network.ClientFixes
import net.ccbluex.liquidbounce.handler.other.ServerSpoof
import net.ccbluex.liquidbounce.ui.client.gui.EnumLaunchFilter
import net.ccbluex.liquidbounce.ui.client.gui.LaunchFilterInfo
import net.ccbluex.liquidbounce.ui.client.gui.LaunchOption
import net.ccbluex.liquidbounce.ui.client.gui.GuiLaunchOptionSelectMenu
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.keybind.KeyBindManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.ui.sound.TipSoundManager
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.WaitTickUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import java.util.*

object FDPClient {

    // Client information
    const val CLIENT_NAME = "FDPClientX"
    const val COLORED_NAME = "§7[§b§lFDPClient§7] "
    const val CLIENT_CREATOR = "CCBlueX,Xebook1, Zywl && SkidderMC TEAM"
    const val CLIENT_WEBSITE = ""
    const val CLIENT_VERSION = "Reborn"
    const val VERSIONTYPE = "Xe Preview"

    // Flags
    var isStarting = true
    var isLoadingConfig = true
    private var latest = ""

    @JvmField
    val gitInfo = Properties().also {
        val inputStream = FDPClient::class.java.classLoader.getResourceAsStream("git.properties")
        if (inputStream != null) {
            it.load(inputStream)
        } else {
            it["git.branch"] = "Main"
        }
    }

    @JvmField
    val CLIENT_BRANCH = (gitInfo["git.branch"] ?: "unknown").let {
        if (it == "main") "Main" else it
    }

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var tipSoundManager: TipSoundManager
    lateinit var combatManager: CombatManager
    lateinit var macroManager: MacroManager
    lateinit var configManager: ConfigManager

    // Some UI things
    lateinit var hud: HUD
    lateinit var mainMenu: GuiScreen
    lateinit var keyBindManager: KeyBindManager

    // Menu Background
    var background: ResourceLocation? = ResourceLocation("fdpclient/bg2.png")

    val launchFilters = mutableListOf<EnumLaunchFilter>()
    private val dynamicLaunchOptions: Array<LaunchOption>
        get() = ClassUtils.resolvePackage(
            "${LaunchOption::class.java.`package`.name}.options",
            LaunchOption::class.java
        )
            .filter {
                val annotation = it.getDeclaredAnnotation(LaunchFilterInfo::class.java)
                if (annotation != null) {
                    return@filter annotation.filters.toMutableList() == launchFilters
                }
                false
            }
            .map {
                try {
                    it.newInstance()
                } catch (e: IllegalAccessException) {
                    ClassUtils.getObjectInstance(it) as LaunchOption
                }
            }.toTypedArray()

    /**
     * Execute if client will be started
     */
    fun initClient() {
        ClientUtils.logInfo("Loading $CLIENT_NAME $CLIENT_VERSION")
        ClientUtils.logInfo("Initializing...")
        val startTime = System.currentTimeMillis()

        // Initialize managers
        fileManager = FileManager()
        configManager = ConfigManager()
        eventManager = EventManager()
        commandManager = CommandManager()
        macroManager = MacroManager()
        moduleManager = ModuleManager()
        scriptManager = ScriptManager()
        keyBindManager = KeyBindManager()
        combatManager = CombatManager()
        tipSoundManager = TipSoundManager()

        // Load language
        LanguageManager.switchLanguage(Minecraft.getMinecraft().gameSettings.language)

        // Register listeners
        eventManager.registerListenerAll(
            RotationUtils(),
            ClientFixes,
            InventoryUtils,
            BungeeCordSpoof(),
            ServerSpoof,
            WaitTickUtils,
            SessionUtils(),
            StatisticsUtils(),
            LocationCache(),
            PacketUtils,
            MiniMapRegister,
            macroManager,
            combatManager,
            BlinkComponent
        )

        // Load client fonts
        Fonts.loadFonts()

        // Setup modules
        moduleManager.registerModules()

        // Load and enable scripts
        try {
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.logError("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()

        // Load GUI
        GuiCapeManager.load()
        mainMenu = GuiLaunchOptionSelectMenu()
        hud = HUD.createDefault()

        // Load configs
        fileManager.loadConfigs(
            fileManager.accountsConfig,
            fileManager.friendsConfig,
            fileManager.specialConfig,
            fileManager.subscriptsConfig,
            fileManager.hudConfig,
            fileManager.xrayConfig
        )

        // Set title
        ClientUtils.setTitle()

        // Log success
        ClientUtils.logInfo("$CLIENT_NAME $CLIENT_VERSION loaded in ${(System.currentTimeMillis() - startTime)}ms!")
    }

    /**
     * Execute if client ui type is selected
     */
    // Start dynamic launch options
    fun startClient() {
        dynamicLaunchOptions.forEach {
            it.start()
        }

        // Load configs
        configManager.loadLegacySupport()
        configManager.loadConfigSet()

        // Set is starting status
        isStarting = false
        isLoadingConfig = false

        ClientUtils.logInfo("$CLIENT_NAME $CLIENT_VERSION started!")
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Check if client is not starting or loading configurations
        if (!isStarting && !isLoadingConfig) {
            ClientUtils.logInfo("Shutting down $CLIENT_NAME $CLIENT_VERSION!")

            // Call client shutdown
            eventManager.callEvent(ClientShutdownEvent())

            // Save configurations
            GuiCapeManager.save() // Save capes
            configManager.save(true, forceSave = true) // Save configs
            fileManager.saveAllConfigs() // Save file manager configs

            // Stop dynamic launch options
            dynamicLaunchOptions.forEach {
                it.stop()
            }
        }
        // Stop Discord RPC
        try {
            DiscordRPC.stop()
        } catch (e: Throwable) {
            ClientUtils.logError("Failed to shutdown DiscordRPC.", e)
        }
    }
}
