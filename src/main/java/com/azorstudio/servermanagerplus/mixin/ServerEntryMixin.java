package com.azorstudio.servermanagerplus.mixin;

import com.azorstudio.servermanagerplus.data.ServerDataManager;
import com.azorstudio.servermanagerplus.gui.EnhancedMultiplayerScreen;
import com.azorstudio.servermanagerplus.gui.PinContextMenu;
import com.azorstudio.servermanagerplus.util.CountryLookupUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin {

    @Shadow @Final private ServerInfo server;

    // ── Draw pin icon + country flag on each entry ────────────────────────────
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int index, int y,
                          boolean hovered, float tickDelta, CallbackInfo ci) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;

        double scaleX = client.getWindow().getScaledWidth()  / (double) client.getWindow().getWidth();
        double scaleY = client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight();
        int mouseX = (int)(client.mouse.getX() * scaleX);
        int mouseY = (int)(client.mouse.getY() * scaleY);

        int entryX = 32;
        int entryH = 36;
        int entryW = client.currentScreen != null ? client.currentScreen.width - 40 : 200;

        ServerDataManager dm = ServerDataManager.getInstance();
        String address = server.address;

        // Pin icon
        if (dm.isServerPinned(address)) {
            context.drawText(client.textRenderer,
                Text.literal("📌"), entryX + 2, y + 2, 0xFFD700, true);
        }

        // Country flag
        Optional<String> countryCode = dm.getCountryCode(address);
        if (countryCode.isEmpty()) {
            CountryLookupUtil.lookupAsync(address, code -> {});
        } else {
            String code  = countryCode.get();
            String flag  = CountryLookupUtil.countryCodeToFlag(code);
            int flagX    = entryX + entryW - 24;
            int flagY    = y + (entryH / 2) - 4;

            context.drawText(client.textRenderer, Text.literal(flag), flagX, flagY, 0xFFFFFF, true);

            if (hovered && mouseX >= flagX && mouseX <= flagX + 16
                        && mouseY >= flagY && mouseY <= flagY + 10) {
                context.drawTooltip(client.textRenderer,
                    Text.literal(flag + " " + CountryLookupUtil.getCountryName(code)),
                    mouseX, mouseY);
            }
        }
    }

    // ── Right-click → Pin/Unpin context menu ─────────────────────────────────
    // In 1.21.11 the signature is mouseClicked(Click, boolean)
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(Click click, boolean doubled,
                                CallbackInfoReturnable<Boolean> cir) {
        if (click.button() == 1) { // right mouse button
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.setScreen(new PinContextMenu(
                    client.currentScreen,
                    server.address, server.name,
                    true,
                    (int) click.x(), (int) click.y()
                ));
            }
            cir.setReturnValue(true);
        }
    }
}
