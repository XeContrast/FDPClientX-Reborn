package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos

@ModuleInfo("StepTest", category = ModuleCategory.MOVEMENT)
object StepTest : Module() {
    var isStep = false
    var stepX = 0.0
    var stepY = 0.0
    var stepZ = 0.0

    var ncpNextStep = 0
    var spartanSwitch = false
    var isAACStep = false
    var wasTimer = false
    var lastOnGround = false
    var canStep = false
    val timer = MSTimer()
    var high = 0.0f
    var off = false //你麻痹不每个设置一个直接狗日的不工作了
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.steps", StepMode::class.java)
        .map { it.newInstance() as StepMode }
        .sortedBy { it.modeName }
    val heightValue = FloatValue("Height", 1F, 0.6F, 10F)
    val delayValue = IntegerValue("Delay", 0, 0, 500)

    private val mode: StepMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Vanilla") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    override fun onDisable() {
        mc.thePlayer ?: return

        // Change step height back to default (0.5 is default)
        mc.thePlayer.stepHeight = 0.6F
        mc.timer.timerSpeed = 1.0F
        wasTimer = false
        lastOnGround = mc.thePlayer.onGround
    }

    override fun onEnable() {
        high = 0f
        off = false
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        mc.thePlayer ?: return
        if (!mc.thePlayer.onGround || !timer.hasTimePassed(delayValue.get().toLong())) {
            mc.thePlayer.stepHeight = 0.6F
            event.stepHeight = 0.6F
            return
        }
        if (event.eventState == EventState.PRE) {
            if (off) {
                mc.thePlayer.stepHeight = 0.6f
                event.stepHeight = 0.6f
                return
            }
            // Set step height
            val height = heightValue.get()

            // Detect possible step

            mc.thePlayer.stepHeight = height
            event.stepHeight = height

            if (event.stepHeight > 0.6F) {
                isStep = true
                stepX = mc.thePlayer.posX
                stepY = mc.thePlayer.posY
                stepZ = mc.thePlayer.posZ
            }
        } else {
            mode.onStep(event)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mode.onUpdate(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    override val tag: String
        get() = modeValue.get()

    fun fakeJump() {
        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    override val values = super.values.toMutableList().also {
        modes.map { mode ->
            mode.values.forEach { value ->
                //it.add(value.displayable { modeValue.equals(mode.modeName) })
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.equals(mode.modeName) })
            }
        }
    }
}