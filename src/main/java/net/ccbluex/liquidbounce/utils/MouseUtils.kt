/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.features.module.modules.exploit.MultiActions
import net.ccbluex.liquidbounce.features.module.modules.world.Breaker
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.minecraft.block.material.Material
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import org.lwjgl.input.Mouse
import java.nio.ByteBuffer

object MouseUtils {
    @JvmStatic
    fun mouseWithinBounds(mouseX: Int, mouseY: Int, x: Float, y: Float, x2: Float, y2: Float) = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2

    fun setMouseButtonState(mouseButton: Int, held: Boolean) {
        val m = MouseEvent()
        ObfuscationReflectionHelper.setPrivateValue(MouseEvent::class.java, m, mouseButton, "button")
        ObfuscationReflectionHelper.setPrivateValue(MouseEvent::class.java, m, held, "buttonstate")
        MinecraftForge.EVENT_BUS.post(m)
        val buttons = ObfuscationReflectionHelper.getPrivateValue<ByteBuffer, Mouse?>(
            Mouse::class.java, null, "buttons"
        )
        buttons.put(mouseButton, (if (held) 1 else 0).toByte())
        ObfuscationReflectionHelper.setPrivateValue<Mouse?, ByteBuffer>(Mouse::class.java, null, buttons, "buttons")
    }
    fun sendClickBlockToController(leftClick: Boolean) {
        if (!leftClick) {
            mc.leftClickCounter = 0
        }

        if (mc.leftClickCounter <= 0 && (!mc.thePlayer.isUsingItem || FDPClient.moduleManager.getModule(
                MultiActions::class.java
            )!!.state)
        ) {
            if ((leftClick && mc.objectMouseOver != null) && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                val blockpos: BlockPos = mc.objectMouseOver.blockPos

                if (mc.leftClickCounter == 0) FDPClient.eventManager.callEvent(
                    ClickBlockEvent(
                        blockpos,
                        mc.objectMouseOver.sideHit
                    )
                )

                if (mc.theWorld.getBlockState(blockpos).block
                        .material !== Material.air && mc.playerController.onPlayerDamageBlock(
                        blockpos,
                        mc.objectMouseOver.sideHit
                    )
                ) {
                    mc.effectRenderer.addBlockHitEffects(blockpos, mc.objectMouseOver.sideHit)
                    mc.thePlayer.swingItem()
                }
            } else if (!FDPClient.moduleManager.getModule(Breaker::class.java)!!.state) {
                mc.playerController.resetBlockRemoving()
            }
        }
    }
}