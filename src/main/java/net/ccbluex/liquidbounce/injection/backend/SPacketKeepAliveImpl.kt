/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */

package net.ccbluex.liquidbounce.injection.backend

import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketKeepAlive
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ISPacketKeepAlive
import net.minecraft.network.play.client.CPacketKeepAlive
import net.minecraft.network.play.server.SPacketKeepAlive

class SPacketKeepAliveImpl<T : SPacketKeepAlive>(wrapped: T) : PacketImpl<T>(wrapped), ISPacketKeepAlive {

}

inline fun ISPacketKeepAlive.unwrap(): SPacketKeepAlive = (this as SPacketKeepAliveImpl<*>).wrapped
inline fun SPacketKeepAlive.wrap(): ISPacketKeepAlive = SPacketKeepAliveImpl(this)