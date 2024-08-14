/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static net.ccbluex.liquidbounce.utils.MinecraftInstance.mc;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    /**
     * The Biped right arm.
     */
    @Shadow
    public ModelRenderer bipedRightArm;

    /**
     * The Held item right.
     */
    @Shadow
    public int heldItemRight;

    /**
     * The Biped head.
     */
    @Shadow
    public ModelRenderer bipedHead;

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void revertSwordAnimation(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_, CallbackInfo callbackInfo) {
        if (heldItemRight == 3) bipedRightArm.rotateAngleY = 0F;

        if (Rotations.INSTANCE.shouldRotate() && p_setRotationAngles_7_ instanceof EntityPlayer && p_setRotationAngles_7_.equals(mc.thePlayer)) {
            bipedHead.rotateAngleX = (float) Math.toRadians(Rotations.INSTANCE.lerp(mc.timer.renderPartialTicks, Rotations.INSTANCE.getPrevHeadPitch(), Rotations.INSTANCE.getHeadPitch()));
        }
    }
}