/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import com.mojang.authlib.GameProfile;
import net.ccbluex.liquidbounce.features.module.modules.combat.KeepSprint;
import net.ccbluex.liquidbounce.utils.CooldownHelper;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase {

    @Shadow public abstract String getName();

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    public abstract GameProfile getGameProfile();

    @Shadow
    protected abstract boolean canTriggerWalking();

    @Shadow
    protected abstract String getSwimSound();

    @Shadow
    public abstract FoodStats getFoodStats();

    @Shadow
    protected int flyToggleTimer;

    @Shadow
    public PlayerCapabilities capabilities;

    @Shadow
    public abstract int getItemInUseDuration();

    @Shadow
    public abstract ItemStack getItemInUse();

    @Shadow
    public abstract boolean isUsingItem();
    
    @Shadow public InventoryPlayer inventory;

    @Shadow protected abstract void entityInit();

    @Shadow public abstract void fall(float p_fall_1_, float p_fall_2_);

    @Shadow public float cameraYaw;

    @Shadow public abstract void func_175145_a(StatBase p_175145_1_);

    @Unique
    private ItemStack fDP1$cooldownStack;
    @Unique
    private int fDP1$cooldownStackSlot;

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void injectCooldown(final CallbackInfo callbackInfo) {
        if (this.getGameProfile() == Minecraft.getMinecraft().thePlayer.getGameProfile()) {
            CooldownHelper.INSTANCE.incrementLastAttackedTicks();
            CooldownHelper.INSTANCE.updateGenericAttackSpeed(getHeldItem());

            if (fDP1$cooldownStackSlot != inventory.currentItem || !ItemStack.areItemStacksEqual(fDP1$cooldownStack, getHeldItem())) {
                CooldownHelper.INSTANCE.resetLastAttackedTicks();
            }

            fDP1$cooldownStack = getHeldItem();
            fDP1$cooldownStackSlot = inventory.currentItem;
        }
    }
    @ModifyConstant(method = "attackTargetEntityWithCurrentItem", constant = @Constant(doubleValue = 0.6))
    private double injectKeepSprintA(double constant) {
        return KeepSprint.INSTANCE.handleEvents() && isSprinting() ? KeepSprint.INSTANCE.getMotionAfterAttack() : constant;
    }

    @Redirect(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSprinting(Z)V"))
    private void injectKeepSprintB(EntityPlayer instance, boolean sprint) {
        boolean keepSprint = Boolean.FALSE.equals(MovementUtils.INSTANCE.getAffectSprintOnAttack());

        if (!KeepSprint.INSTANCE.handleEvents() && !keepSprint) {
            instance.setSprinting(sprint);
        }

        // Only affect motion when sprinting. Knock-back modifier factor is ignored.
        if (keepSprint && !KeepSprint.INSTANCE.handleEvents() && isSprinting()) {
            // Reverse the motion effects done by sprinting
            motionX /= 0.6;
            motionZ /= 0.6;
        }
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectSprintState(Entity entity, CallbackInfo ci, float f, int i, float f1, boolean flag, boolean flag1, int j, double d0, double d1, double d2) {
        Boolean sprint = MovementUtils.INSTANCE.getAffectSprintOnAttack();

        if (sprint == null || !sprint || isSprinting())
            return;

        // This will be used later in line 1058
        //noinspection UnusedAssignment
        i++;
    }
}
