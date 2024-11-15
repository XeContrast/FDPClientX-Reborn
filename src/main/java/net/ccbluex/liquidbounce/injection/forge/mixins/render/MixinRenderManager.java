/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack;
import net.ccbluex.liquidbounce.features.module.modules.combat.ForwardTrack;
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.PacketUtilsKt;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(RenderManager.class)
@SideOnly(Side.CLIENT)
public abstract class MixinRenderManager {

    @Shadow
    public abstract boolean doRenderEntity(Entity p_doRenderEntity_1_, double p_doRenderEntity_2_, double p_doRenderEntity_4_, double p_doRenderEntity_4_2, float p_doRenderEntity_6_, float p_doRenderEntity_6_2, boolean p_doRenderEntity_8_);

    @Shadow
    public double renderPosX;

    @Shadow
    public double renderPosY;

    @Shadow
    public double renderPosZ;

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F", opcode = PUTFIELD))
    public void getPlayerViewX(RenderManager renderManager, float value) {
        renderManager.playerViewX = RotationUtils.perspectiveToggled ? RotationUtils.cameraPitch : value;
    }

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewY:F", opcode = PUTFIELD))
    public void getPlayerViewY(RenderManager renderManager, float value) {
        renderManager.playerViewY = RotationUtils.perspectiveToggled ? RotationUtils.cameraYaw : value;
    }

    @Inject(method = "renderEntityStatic", at = @At(value = "HEAD"))
    private void renderEntityStatic(Entity entity, float tickDelta, boolean bool, CallbackInfoReturnable<Boolean> cir) {

        if (entity instanceof EntityPlayerSP)
            return;

        if (entity instanceof EntityLivingBase) {
            IMixinEntity iEntity = (IMixinEntity) entity;

            if (iEntity.getTruePos()) {
                PacketUtilsKt.interpolatePosition(iEntity);
            }
        }

        Backtrack backtrack = Backtrack.INSTANCE;
        IMixinEntity targetEntity = (IMixinEntity) backtrack.getTarget();

        boolean shouldBacktrackRenderEntity = backtrack.handleEvents() && backtrack.getShouldRender()
                && backtrack.shouldBacktrack() && backtrack.getTarget() == entity;

        if (backtrack.getEspMode().equals("Model")) {
            if (shouldBacktrackRenderEntity && targetEntity != null && targetEntity.getTruePos()) {
                if (entity.ticksExisted == 0) {
                    entity.lastTickPosX = entity.posX;
                    entity.lastTickPosY = entity.posY;
                    entity.lastTickPosZ = entity.posZ;
                }

                double d0 = targetEntity.getLerpX();
                double d1 = targetEntity.getLerpY();
                double d2 = targetEntity.getLerpZ();
                float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * tickDelta;
                int i = entity.getBrightnessForRender(tickDelta);
                if (entity.isBurning()) {
                    i = 15728880;
                }

                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
                // Darker color to differentiate fake player & real player.
                GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
                this.doRenderEntity(entity, d0 - this.renderPosX, d1 - this.renderPosY, d2 - this.renderPosZ, f, tickDelta, bool);
            }
        }

        ForwardTrack forwardTrack = ForwardTrack.INSTANCE;

        if (forwardTrack.handleEvents() && forwardTrack.getEspMode().equals("Model") && !shouldBacktrackRenderEntity) {
            if (entity.ticksExisted == 0) {
                entity.lastTickPosX = entity.posX;
                entity.lastTickPosY = entity.posY;
                entity.lastTickPosZ = entity.posZ;
            }

            Vec3 pos = forwardTrack.usePosition(entity);

            float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * tickDelta;
            int i = entity.getBrightnessForRender(tickDelta);
            if (entity.isBurning()) {
                i = 15728880;
            }

            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
            // Darker color to differentiate fake player & real player.
            GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
            this.doRenderEntity(entity, pos.xCoord - this.renderPosX, pos.yCoord - this.renderPosY, pos.zCoord - this.renderPosZ, f, tickDelta, bool);
        }
    }
}