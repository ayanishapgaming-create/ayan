package com.azorstudio.servermanagerplus.mixin;

import com.azorstudio.servermanagerplus.data.ServerDataManager;
import com.azorstudio.servermanagerplus.util.CountryLookupUtil;
import net.minecraft.client.MinecraftClient;
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

import java.util.Optional;

/**
 * Injects into each ServerEntry render call to:
 * 1. Draw a pin icon (📌) for pinned servers
 * 2. Draw a country flag emoji with tooltip on hover
 *
 * In 1.21.11, MultiplayerServerListWidget.ServerEntry#render signature is:
 *   render(DrawContext context, int index, int y, boolean hovered, float tickDelta)
 *
 * The old params x, entryWidth, entryHeight, mouseX, mouseY were removed;
 * mouse position is now obtained via MinecraftClient.getInstance().mouse.
 */
@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin {

    @Shadow
    @Final
    private ServerInfo server;

    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void onRender(DrawContext context, int index, int y,
                          boolean hovered, float tickDelta,
                          CallbackInfo ci) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;

        // In 1.21.11 we no longer receive x/entryWidth/mouseX directly.
        // Obtain mouse position from the Mouse helper instead.
        double mouseX = client.mouse.getX() * client.getWindow().getScaleFactor()
                        / client.getWindow().getWidth()
                        * client.getWindow().getScaledWidth();
        double mouseY = client.mouse.getY() * client.getWindow().getScaleFactor()
                        / client.getWindow().getHeight()
                        * client.getWindow().getScaledHeight();

        // Standard layout constants used by MultiplayerServerListWidget
        // Entry left edge is at x=32 (icon offset), entries are 36px tall
        int entryX = 32;
        int entryHeight = 36;
        // Width available for text is screenWidth - 32 - scrollbar (6) - padding (2)
        int entryWidth = client.currentScreen != null
            ? client.currentScreen.width - 32 - 6 - 2
            : 200;

        ServerDataManager dm = ServerDataManager.getInstance();
        String address = server.address;

        // ── Pin indicator ─────────────────────────────────────────────────────
        if (dm.isServerPinned(address)) {
            context.drawText(client.textRenderer,
                Text.literal("📌"),
                entryX + 1, y + 1,
                0xFFD700,   // gold
                true);
        }

        // ── Country flag ──────────────────────────────────────────────────────
        Optional<String> countryCode = dm.getCountryCode(address);

        if (countryCode.isEmpty()) {
            // Kick off async lookup; result is cached for next frame
            CountryLookupUtil.lookupAsync(address, code -> { /* cached automatically */ });
        } else {
            String code = countryCode.get();
            String flagEmoji = CountryLookupUtil.countryCodeToFlag(code);

            // Position: right side of entry, vertically centred
            int flagX = entryX + entryWidth - 85;
            int flagY = y + (entryHeight / 2) - 4;

            context.drawText(client.textRenderer,
                Text.literal(flagEmoji),
                flagX, flagY,
                0xFFFFFF,
                true);

            // Tooltip when mouse hovers the flag glyph
            boolean overFlag = mouseX >= flagX && mouseX <= flagX + 14
                    && mouseY >= flagY && mouseY <= flagY + 10;

            if (overFlag && hovered) {
                String countryName = CountryLookupUtil.getCountryName(code);
                context.drawTooltip(
                    client.textRenderer,
                    Text.literal(flagEmoji + " " + countryName),
                    (int) mouseX, (int) mouseY
                );
            }
        }
    }
}
