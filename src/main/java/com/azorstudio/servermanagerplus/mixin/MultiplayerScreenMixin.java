package com.azorstudio.servermanagerplus.mixin;

import com.azorstudio.servermanagerplus.gui.EnhancedMultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin {

    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    // Inject search bar + pinned button after vanilla init
    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        EnhancedMultiplayerScreen.injectIntoMultiplayerScreen(
            (MultiplayerScreen)(Object)this,
            serverListWidget
        );
    }

    // When the screen closes, clear the search/filter state
    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        EnhancedMultiplayerScreen.serverSearchQuery = "";
        EnhancedMultiplayerScreen.showPinnedOnly = false;
    }
}
