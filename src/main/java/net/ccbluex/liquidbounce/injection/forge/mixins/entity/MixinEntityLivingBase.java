/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.EventState;
import net.ccbluex.liquidbounce.event.JumpEvent;
import net.ccbluex.liquidbounce.event.MoveMathEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.features.module.modules.movement.LiquidSpeed;
import net.ccbluex.liquidbounce.features.module.modules.movement.Sprint;
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix;
import net.ccbluex.liquidbounce.features.module.modules.player.NoDelay;
import net.ccbluex.liquidbounce.features.module.modules.visual.AntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.exploit.ViaVersionFix;
import net.ccbluex.liquidbounce.features.module.modules.world.scaffold.Scaffold;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {

    @Shadow
    public float rotationYawHead;
    @Shadow
    protected boolean isJumping;
    @Shadow
    public int jumpTicks;

    @Shadow
    protected abstract float getJumpUpwardsMotion();

    @Shadow
    public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    @Shadow
    public void onLivingUpdate() {
    }

    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    protected abstract void updateAITick();

    @Shadow protected abstract void entityInit();

    @Shadow public abstract void setSprinting(boolean p_setSprinting_1_);

    @Shadow public abstract float getRotationYawHead();

    @Shadow public abstract void fall(float p_fall_1_, float p_fall_2_);

    /**
     * @author CCBlueX
     * @author CoDynamic
     * Modified by Co Dynamic
     * Date: 2023/02/15
     * @reason UPDATE
     */
    @Overwrite
    protected void jump() {
        if (!this.equals(Minecraft.getMinecraft().thePlayer)) {
            return;
        }
        
        /*
          Jump Process Fix
          use updateFixState to reset Jump Fix state
          @param fixedYaw  The yaw player should have (NOT RotationYaw)
         * @param strafeFix StrafeFix Module
         */

        final JumpEvent jumpEvent = new JumpEvent(MovementUtils.INSTANCE.getJumpMotion(),this.rotationYaw, EventState.PRE);
        FDPClient.eventManager.callEvent(jumpEvent);
        if (jumpEvent.isCancelled())
            return;

        this.motionY = jumpEvent.getMotion();
        final Sprint sprint = FDPClient.moduleManager.getModule(Sprint.class);
        final StrafeFix strafeFix = FDPClient.moduleManager.getModule(StrafeFix.class);

        if (this.isSprinting()) {
            float fixedYaw = this.rotationYaw;
            if(RotationUtils.targetRotation != null && Objects.requireNonNull(strafeFix).getDoFix()) {
                fixedYaw = RotationUtils.targetRotation.getYaw();
            }
            if(Objects.requireNonNull(sprint).getState() && sprint.getJumpDirectionsValue().get()) {
                fixedYaw += MovementUtils.INSTANCE.getMovingYaw() - this.rotationYaw;
            }
            final float f = fixedYaw * 0.017453292F;
            this.motionX -= MathHelper.sin(f) * 0.2F;
            this.motionZ += MathHelper.cos(f) * 0.2F;
        }

        this.isAirBorne = true;

        final JumpEvent jumpEventPost = new JumpEvent(MovementUtils.INSTANCE.getJumpMotion(),this.rotationYaw, EventState.POST);
        FDPClient.eventManager.callEvent(jumpEventPost);
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void headLiving(CallbackInfo callbackInfo) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(NoDelay.class)).getState() && FDPClient.moduleManager.getModule(NoDelay.class).getNoJumpDelay().get())
            jumpTicks = 0;
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;isJumping:Z", ordinal = 1))
    private void onJumpSection(CallbackInfo callbackInfo) {
        final LiquidSpeed jesus = FDPClient.moduleManager.getModule(LiquidSpeed.class);

        if (Objects.requireNonNull(jesus).getState() && !isJumping && !isSneaking() && isInWater() &&
                jesus.getModeValue().get().equals("Legit")) {
            this.updateAITick();
        }
    }

    @Inject(method = "moveEntityWithHeading",at = @At("HEAD"), cancellable = true)
    private void callEvent(float p_moveEntityWithHeading_1_, float p_moveEntityWithHeading_2_, CallbackInfo ci) {
        MoveMathEvent event = new MoveMathEvent(p_moveEntityWithHeading_1_,p_moveEntityWithHeading_2_);
        FDPClient.eventManager.callEvent(event);

        if (event.isCancelled())
            ci.cancel();
    }

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.005D))
    private double ViaVersion_MovementThreshold(double constant) {
        final ViaVersionFix viaversionfix = FDPClient.moduleManager.getModule(ViaVersionFix.class);
        if (Objects.requireNonNull(viaversionfix).getState()){
            return 0.003D;
        }
        return 0.005D;
    }

    /**
     * Inject head yaw rotation modification
     */
    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;updateEntityActionState()V", shift = At.Shift.AFTER))
    private void hookHeadRotations(CallbackInfo ci) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        this.rotationYawHead = ((EntityLivingBase) (Object) this) instanceof EntityPlayerSP && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : this.rotationYawHead;
    }

    /**
     * Inject body rotation modification
     */
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F", ordinal = 0))
    private float hookBodyRotationsA(EntityLivingBase instance) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        return instance instanceof EntityPlayerSP && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.rotationYaw;
    }


    /**
     * Inject body rotation modification
     */
    @Redirect(method = "updateDistance", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F"))
    private float hookBodyRotationsB(EntityLivingBase instance) {
        Rotation rotation = Rotations.INSTANCE.getRotation();

        return instance instanceof EntityPlayerSP && Rotations.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.rotationYaw;
    }

    @Inject(method = "getLook", at = @At("HEAD"), cancellable = true)
    private void getLook(CallbackInfoReturnable<Vec3> callbackInfoReturnable) {
        if (((EntityLivingBase) (Object) this) instanceof EntityPlayerSP)
            callbackInfoReturnable.setReturnValue(getVectorForRotation(this.rotationPitch, this.rotationYaw));
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void isPotionActive(Potion p_isPotionActive_1_, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final AntiBlind antiBlind = FDPClient.moduleManager.getModule(AntiBlind.class);

        if ((p_isPotionActive_1_ == Potion.confusion || p_isPotionActive_1_ == Potion.blindness) && Objects.requireNonNull(antiBlind).getState() && antiBlind.getConfusionEffectValue().get())
            callbackInfoReturnable.setReturnValue(false);
    }

    /**
     * @author Liuli
     * @reason getArm
     */
    @Overwrite
    private int getArmSwingAnimationEnd() {
        int speed = this.isPotionActive(Potion.digSpeed) ? 6 - (1 + this.getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (this.isPotionActive(Potion.digSlowdown) ? 6 + (1 + this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);

        if (this.equals(Minecraft.getMinecraft().thePlayer)) {
            speed = (int) (speed * Animations.INSTANCE.getSwingSpeedValue().get());
        }

        return speed;
    }
}
