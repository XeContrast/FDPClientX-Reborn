package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other;

import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;

public class MineplexHop extends SpeedMode {
    public MineplexHop() {
    super("MineplexHop");
    }

    private double mineplex = 0, stage;

    public static boolean isMoving2() {
        return ((mc2.player.moveForward != 0.0F || mc2.player.moveStrafing != 0.0F));
    }

    public static boolean isOnGround(double height) {
        if(!mc2.world.getCollisionBoxes(mc2.player, mc2.player.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static void setMotion(double speed) {
        double forward = mc2.player.movementInput.moveForward;
        double strafe = mc2.player.movementInput.moveStrafe;
        float yaw = mc2.player.rotationYaw;
        if ((forward == 0.0D) && (strafe == 0.0D)) {
            mc2.player.motionX = 0;
            mc2.player.motionZ = 0;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }
            mc2.player.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F));
            mc2.player.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        }
    }

    @Override
    public void onMotion() {
    }

    @EventTarget
    public void onMotion(final MotionEvent event) {
        final EventState eventState = event.getEventState();
            double speed = 0.15;
            if (mc2.player.collidedHorizontally || !isMoving2()) {
                mineplex = -2;
            }
            if (isOnGround(0.001) && isMoving2()) {
                stage = 0;
                mc2.player.motionY = 0.42;
                if (mineplex < 0)
                    mineplex++;
                if (mc2.player.posY != (int)mc2.player.posY) {
                    mineplex = -1;
                }
                mc.getTimer().setTimerSpeed(2.001f);
            }else{
                if (mc.getTimer().getTimerSpeed() == 2.001f)
                    mc.getTimer().setTimerSpeed(1F);
                speed = 0.62 - stage / 300 + mineplex / 5;
                stage++;

            }
            setMotion(speed);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        final IPacket packet = event.getPacket();
        if(classProvider.isSPacketPlayerPosLook(packet)){
            mineplex = -2;
        }
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void onMove(MoveEvent event) {
    }
}
