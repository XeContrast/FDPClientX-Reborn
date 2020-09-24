/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntity;
import net.ccbluex.liquidbounce.api.minecraft.client.entity.IEntityLivingBase;
import net.ccbluex.liquidbounce.api.minecraft.client.network.INetworkPlayerInfo;
import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import net.ccbluex.liquidbounce.event.AttackEvent;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.WorldEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ModuleInfo(name = "AntiBot", description = "Prevents KillAura from attacking AntiCheat bots.", category = ModuleCategory.MISC)
public class AntiBot extends Module {
    private final BoolValue tabValue = new BoolValue("Tab", true);
    private final ListValue tabModeValue = new ListValue("TabMode", new String[] {"Equals", "Contains"}, "Contains");
    private final BoolValue entityIDValue = new BoolValue("EntityID", true);
    private final BoolValue colorValue = new BoolValue("Color", false);
    private final BoolValue livingTimeValue = new BoolValue("LivingTime", false);
    private final IntegerValue livingTimeTicksValue = new IntegerValue("LivingTimeTicks", 40, 1, 200);
    private final BoolValue groundValue = new BoolValue("Ground", true);
    private final BoolValue airValue = new BoolValue("Air", false);
    private final BoolValue invalidGroundValue = new BoolValue("InvalidGround", true);
    private final BoolValue swingValue = new BoolValue("Swing", false);
    private final BoolValue healthValue = new BoolValue("Health", false);
    private final BoolValue derpValue = new BoolValue("Derp", true);
    private final BoolValue wasInvisibleValue = new BoolValue("WasInvisible", false);
    private final BoolValue armorValue = new BoolValue("Armor", false);
    private final BoolValue pingValue = new BoolValue("Ping", false);
    private final BoolValue needHitValue = new BoolValue("NeedHit", false);
    private final BoolValue duplicateInWorldValue = new BoolValue("DuplicateInWorld", false);
    private final BoolValue duplicateInTabValue = new BoolValue("DuplicateInTab", false);
    private final BoolValue alwaysInRadiusValue = new BoolValue("AlwaysInRadius", false);
    private final FloatValue alwaysRadiusValue = new FloatValue("AlwaysInRadiusBlocks", 20f, 5f, 30f);

    private final List<Integer> ground = new ArrayList<>();
    private final List<Integer> air = new ArrayList<>();
    private final Map<Integer, Integer> invalidGround = new HashMap<>();
    private final List<Integer> swing = new ArrayList<>();
    private final List<Integer> invisible = new ArrayList<>();
    private final List<Integer> hitted = new ArrayList<>();
    private final List notAlwaysInRadius = new ArrayList();

    @Override
    public void onDisable() {
        clearAll();
        super.onDisable();
    }

    @EventTarget
    public void onPacket(final PacketEvent event) {
        if(mc.getThePlayer() == null || mc.getTheWorld() == null)
            return;

        final IPacket packet = event.getPacket();

        if(packet instanceof SPacketEntity) {
            final SPacketEntity packetEntity = (SPacketEntity) event.getPacket();
            final Entity entity = packetEntity.getEntity(mc2.world);

            if(entity instanceof EntityPlayer) {
                if(packetEntity.getOnGround() && !ground.contains(entity.getEntityId()))
                    ground.add(entity.getEntityId());

                if(!packetEntity.getOnGround() && !air.contains(entity.getEntityId()))
                    air.add(entity.getEntityId());

                if(packetEntity.getOnGround()) {
                    if(entity.prevPosY != entity.posY)
                        invalidGround.put(entity.getEntityId(), invalidGround.getOrDefault(entity.getEntityId(), 0) + 1);
                }else{
                    final int currentVL = invalidGround.getOrDefault(entity.getEntityId(), 0) / 2;

                    if (currentVL <= 0)
                        invalidGround.remove(entity.getEntityId());
                    else
                        invalidGround.put(entity.getEntityId(), currentVL);
                }

                if(entity.isInvisible() && !invisible.contains(entity.getEntityId()))
                    invisible.add(entity.getEntityId());

                if (!notAlwaysInRadius.contains(entity.getEntityId()) && mc.getThePlayer().getDistanceSqToEntity((IEntity) entity) > alwaysRadiusValue.get())
                notAlwaysInRadius.add(entity.getEntityId());
            }
        }

        if(packet instanceof SPacketAnimation) {
            final SPacketAnimation packetAnimation = (SPacketAnimation) event.getPacket();
            final Entity entity = mc2.world.getEntityByID(packetAnimation.getEntityID());

            if(entity instanceof EntityLivingBase && packetAnimation.getAnimationType() == 0 && !swing.contains(entity.getEntityId()))
                swing.add(entity.getEntityId());
        }
    }

    @EventTarget
    public void onAttack(final AttackEvent e) {
        final Entity entity = (Entity) e.getTargetEntity();

        if(entity instanceof EntityLivingBase && !hitted.contains(entity.getEntityId()))
            hitted.add(entity.getEntityId());
    }

    @EventTarget
    public void onWorld(final WorldEvent event) {
        clearAll();
    }

    private void clearAll() {
        hitted.clear();
        swing.clear();
        ground.clear();
        invalidGround.clear();
        invisible.clear();
        notAlwaysInRadius.clear();
    }

    public static boolean isBot(final IEntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer))
            return false;

        final AntiBot antiBot = (AntiBot) LiquidBounce.moduleManager.getModule(AntiBot.class);

        if (antiBot == null || !antiBot.getState())
            return false;

        if (antiBot.colorValue.get() && !entity.getDisplayName().getFormattedText()
                .replace("ยงr", "").contains("ยง"))
            return true;

        if (antiBot.livingTimeValue.get() && entity.getTicksExisted() < antiBot.livingTimeTicksValue.get())
            return true;

        if (antiBot.groundValue.get() && !antiBot.ground.contains(entity.getEntityId()))
            return true;

        if (antiBot.airValue.get() && !antiBot.air.contains(entity.getEntityId()))
            return true;

        if(antiBot.swingValue.get() && !antiBot.swing.contains(entity.getEntityId()))
            return true;

        if(antiBot.healthValue.get() && entity.getHealth() > 20F)
            return true;

        if(antiBot.entityIDValue.get() && (entity.getEntityId() >= 1000000000 || entity.getEntityId() <= -1))
            return true;

        if(antiBot.derpValue.get() && (entity.getRotationYaw() > 90F || entity.getRotationPitch() < -90F))
            return true;

        if(antiBot.wasInvisibleValue.get() && antiBot.invisible.contains(entity.getEntityId()))
            return true;

        if(antiBot.armorValue.get()) {
            final EntityPlayer player = (EntityPlayer) entity;

            if (player.inventory.armorInventory.get(0) == null && player.inventory.armorInventory.get(1) == null &&
                    player.inventory.armorInventory.get(2) == null && player.inventory.armorInventory.get(3) == null)
                return true;
        }

        if(antiBot.pingValue.get()) {
            EntityPlayer player = (EntityPlayer) entity;

            if(mc.getNetHandler().getPlayerInfo(player.getUniqueID()).getResponseTime() == 0)
                return true;
        }

        if(antiBot.needHitValue.get() && !antiBot.hitted.contains(entity.getEntityId()))
            return true;

        if(antiBot.invalidGroundValue.get() && antiBot.invalidGround.getOrDefault(entity.getEntityId(), 0) >= 10)
            return true;

        if(antiBot.tabValue.get()) {
            final boolean equals = antiBot.tabModeValue.get().equalsIgnoreCase("Equals");
            final String targetName = ColorUtils.stripColor(entity.getDisplayName().getFormattedText());

            if (targetName != null) {
                for (final INetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                    final String networkName = ColorUtils.stripColor(EntityUtils.getName(networkPlayerInfo));

                    if (networkName == null)
                        continue;

                    if (equals ? targetName.equals(networkName) : targetName.contains(networkName))
                        return false;
                }

                return true;
            }
        }

        if(antiBot.duplicateInWorldValue.get()) {
            if (mc.getTheWorld().getLoadedEntityList().stream()
                    .filter(currEntity -> currEntity instanceof EntityPlayer && ((EntityPlayer) currEntity)
                            .getDisplayNameString().equals(((EntityPlayer) currEntity).getDisplayNameString()))
                    .count() > 1)
                return true;
        }

        if(antiBot.duplicateInTabValue.get()) {
            if (mc.getNetHandler().getPlayerInfoMap().stream()
                    .filter(networkPlayer -> entity.getName().equals(ColorUtils.stripColor(EntityUtils.getName(networkPlayer))))
                    .count() > 1)
                return true;
        }

        if (antiBot.alwaysInRadiusValue.get() && !antiBot.notAlwaysInRadius.contains(entity.getEntityId()))
            return true;
        return entity.getName().isEmpty() || entity.getName().equals(mc.getThePlayer().getName());
    }

}
