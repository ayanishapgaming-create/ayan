package com.azorstudio.servermanagerplus.mixin;

import com.azorstudio.servermanagerplus.gui.EnhancedWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin {

    @Shadow
    protected WorldListWidget levelList;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        EnhancedWorldScreen.injectIntoWorldScreen(
            (SelectWorldScreen)(Object)this,
            levelList
        );
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        EnhancedWorldScreen.worldSearchQuery = "";
        EnhancedWorldScreen.showPinnedWorldsOnly = false;
    }
}
