package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.MotionEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;

@ModuleInfo(name = "Bobbing", description = "Bobbing.", category = ModuleCategory.RENDER)
public class Bobbing extends Module {

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!mc.getThePlayer().getOnGround()) {
            mc.getThePlayer().setCameraYaw(mc.getThePlayer().getCameraYaw() + 0.015F);
        }
    }
}
