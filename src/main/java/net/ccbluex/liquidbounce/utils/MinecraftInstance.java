/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.api.IClassProvider;
import net.ccbluex.liquidbounce.api.IExtractedFunctions;
import net.ccbluex.liquidbounce.api.minecraft.client.IMinecraft;
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.minecraft.client.Minecraft;

public class MinecraftInstance {
    protected static final IMinecraft mc = LiquidBounce.wrapper.getMinecraft();
    protected static final Minecraft mc2 = Minecraft.getMinecraft();
    protected static final IClassProvider classProvider = LiquidBounce.INSTANCE.getWrapper().getClassProvider();
    protected static final IExtractedFunctions functions = LiquidBounce.INSTANCE.getWrapper().getFunctions();
}
