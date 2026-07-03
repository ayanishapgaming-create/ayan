package com.azorstudio.servermanagerplus.mixin;

import com.azorstudio.servermanagerplus.data.ServerDataManager;
import com.azorstudio.servermanagerplus.gui.PinContextMenu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screen.world.WorldListWidget$WorldEntry")
public abstract class WorldEntryMixin {

    @Shadow @Final private LevelSummary level;

    // ── Draw pin icon ────────────────────────────────────────────────────────
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int index, int y,
                          boolean hovered, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null || level == null) return;

        if (ServerDataManager.getInstance().isWorldPinned(level.getName())) {
            context.drawText(client.textRenderer,
                Text.literal("📌"),
                34, y + 2, 0xFFD700, true);
        }
    }

    // ── Right-click → Pin/Unpin context menu ─────────────────────────────────
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button,
                                CallbackInfoReturnable<Boolean> cir) {
        if (button == 1 && level != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.setScreen(new PinContextMenu(
                    client.currentScreen,
                    level.getName(),
                    level.getDisplayName(),
                    false,
                    (int) mouseX, (int) mouseY
                ));
            }
            cir.setReturnValue(true);
        }
    }
}
