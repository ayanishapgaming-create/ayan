package com.azorstudio.servermanagerplus.gui;

import com.azorstudio.servermanagerplus.data.ServerDataManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class PinContextMenu extends Screen {

    private final Screen parent;
    private final String identifier;   // server address or world folder name
    private final String displayName;
    private final boolean isServer;
    private final int menuX, menuY;

    private static final int MENU_W  = 150;
    private static final int MENU_H  = 54;
    private static final int BG      = 0xDD101020;
    private static final int BORDER  = 0xFF5B6EE1;

    public PinContextMenu(Screen parent, String identifier, String displayName,
                          boolean isServer, int mouseX, int mouseY) {
        super(Text.literal("Pin Menu"));
        this.parent      = parent;
        this.identifier  = identifier;
        this.displayName = displayName != null ? displayName : identifier;
        this.isServer    = isServer;

        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc != null ? mc.getWindow().getScaledWidth()  : 320;
        int sh = mc != null ? mc.getWindow().getScaledHeight() : 240;
        this.menuX = Math.min(mouseX, sw - MENU_W - 4);
        this.menuY = Math.min(mouseY, sh - MENU_H - 4);
    }

    @Override
    protected void init() {
        ServerDataManager dm  = ServerDataManager.getInstance();
        boolean pinned = isServer ? dm.isServerPinned(identifier)
                                  : dm.isWorldPinned(identifier);
        String label = pinned ? "📌 Unpin" : "📌 Pin";

        addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> {
            if (isServer) dm.toggleServerPin(identifier);
            else          dm.toggleWorldPin(identifier);
            // Refresh whichever list is active
            EnhancedMultiplayerScreen.refreshList();
            EnhancedWorldScreen.refreshList();
            close();
        }).dimensions(menuX + 4, menuY + 6, MENU_W - 8, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("✕ Cancel"),
            btn -> close()
        ).dimensions(menuX + 4, menuY + 28, MENU_W - 8, 18).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fill(menuX, menuY, menuX + MENU_W, menuY + MENU_H, BG);
        // Border — top, bottom, left, right
        context.fill(menuX,             menuY,            menuX + MENU_W, menuY + 1,       BORDER);
        context.fill(menuX,             menuY + MENU_H-1, menuX + MENU_W, menuY + MENU_H,  BORDER);
        context.fill(menuX,             menuY,            menuX + 1,      menuY + MENU_H,  BORDER);
        context.fill(menuX + MENU_W-1,  menuY,            menuX + MENU_W, menuY + MENU_H,  BORDER);
        // Title (display name, truncated)
        String title = displayName.length() > 18 ? displayName.substring(0, 16) + "…" : displayName;
        context.drawText(textRenderer, Text.literal(title).withColor(0xFFE0C060),
            menuX + 5, menuY - 11, 0xFFFFFF, true);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        if (mx < menuX || mx > menuX + MENU_W || my < menuY || my > menuY + MENU_H) {
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
