package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityOtherPlayerMP.class)
public class MixinEntityOtherPlayerMP {
    @Unique
    private boolean fDP1$isItemInUse;
    @Unique
    private int fDP1$otherPlayerMPPosRotationIncrements;
    @Unique
    public double fDP1$otherPlayerMPX;
    @Unique
    public double fDP1$otherPlayerMPY;
    @Unique
    public double fDP1$otherPlayerMPZ;
    @Unique
    public double fDP1$otherPlayerMPYaw;
    @Unique
    public double fDP1$otherPlayerMPPitch;
    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityOtherPlayerMP;updateArmSwingProgress()V", shift = At.Shift.AFTER), cancellable = true)
    private void removeUselessAnimations(CallbackInfo ci) {
        ci.cancel();
    }
}
