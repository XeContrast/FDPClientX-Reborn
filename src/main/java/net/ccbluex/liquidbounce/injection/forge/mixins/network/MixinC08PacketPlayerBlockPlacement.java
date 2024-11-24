package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.exploit.ViaVersionFix;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Objects;

@Mixin(C08PacketPlayerBlockPlacement.class)
public class MixinC08PacketPlayerBlockPlacement {
    @ModifyConstant(method = "readPacketData", constant = @Constant(floatValue = 16.0F))
    private float ViaRightClickFix_A(float constant) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(ViaVersionFix.class)).getState())
            return 1F;
        return 16.0F;
    }

    @ModifyConstant(method = "writePacketData", constant = @Constant(floatValue = 16.0F))
    private float ViaRightClickFix_B(float constant) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(ViaVersionFix.class)).getState())
            return 1F;
        return 16.0F;
    }

}
