package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.mojang.patchy.BlockedServers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockedServers.class)
public abstract class MixinBlockedServers {
    /**
     * @author koitoyuu
     */
    @Unique
    private static boolean fDPClient$isBlockedServer(String server) {
        return false;
    }
}
