package dev.tr7zw.skinlayers.accessor;

import dev.tr7zw.skinlayers.render.CustomizableModelPart;

public interface PlayerSettings {

	CustomizableModelPart getHeadLayers();
	
	void setupHeadLayers(CustomizableModelPart box);
	
	CustomizableModelPart[] getSkinLayers();
	
	void setupSkinLayers(CustomizableModelPart[] box);

}
