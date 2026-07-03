package com.azorstudio.servermanagerplus.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class EnhancedMultiplayerScreen {

    public static String serverSearchQuery = "";
    public static boolean showPinnedOnly = false;

    // Hold a reference to the list widget so we can refresh it on query change
    private static MultiplayerServerListWidget activeWidget = null;
    private static ServerList activeServerList = null;

    public static void injectIntoMultiplayerScreen(MultiplayerScreen screen,
                                                    MultiplayerServerListWidget widget) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        activeWidget = widget;
        // Load the server list once so we can refresh on filter change
        activeServerList = new ServerList(client);
        activeServerList.loadFile();

        int sw = screen.width;
        int fieldW = sw / 2 - 12;
        int searchY = 22;

        // ── Search field ──────────────────────────────────────────────────────
        TextFieldWidget searchField = new TextFieldWidget(
            client.textRenderer, 8, searchY, fieldW, 18,
            Text.translatable("servermanagerplus.search.servers")
        );
        searchField.setSuggestion("🔍 Search by name or IP...");
        searchField.setMaxLength(128);
        searchField.setText(serverSearchQuery);
        searchField.setChangedListener(query -> {
            serverSearchQuery = query.toLowerCase().trim();
            refreshList();
        });
        addChild(screen, searchField);

        // ── Pinned toggle ─────────────────────────────────────────────────────
        ButtonWidget pinnedBtn = ButtonWidget.builder(
            pinnedBtnText(),
            btn -> {
                showPinnedOnly = !showPinnedOnly;
                btn.setMessage(pinnedBtnText());
                refreshList();
            }
        ).dimensions(sw / 2 + 4, searchY, fieldW, 18).build();
        addChild(screen, pinnedBtn);
    }

    /** Tells the server list widget to repopulate itself (triggers our mixin filter). */
    public static void refreshList() {
        if (activeWidget != null && activeServerList != null) {
            activeWidget.setServers(activeServerList);
        }
    }

    private static Text pinnedBtnText() {
        return showPinnedOnly
            ? Text.literal("★ Pinned Only")
            : Text.literal("☆ All Servers");
    }

    private static void addChild(MultiplayerScreen screen, Object child) {
        try {
            for (var m : net.minecraft.client.gui.screen.Screen.class.getDeclaredMethods()) {
                if (m.getName().equals("addDrawableChild")) {
                    m.setAccessible(true);
                    m.invoke(screen, child);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
