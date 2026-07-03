package com.azorstudio.servermanagerplus.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

@Environment(EnvType.CLIENT)
public class EnhancedWorldScreen {

    public static String worldSearchQuery = "";
    public static boolean showPinnedWorldsOnly = false;

    // Hold reference to widget + full level list so we can refresh on filter change
    private static WorldListWidget activeWidget = null;
    private static List<?> allLevels = null;

    public static void injectIntoWorldScreen(SelectWorldScreen screen, WorldListWidget widget) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        activeWidget = widget;

        // Snapshot the full level list from the widget's current children for refresh
        try {
            var childrenField = net.minecraft.client.gui.widget.EntryListWidget.class
                .getDeclaredField("children");
            childrenField.setAccessible(true);
        } catch (Exception ignored) {}

        int sw = screen.width;
        int fieldW = sw / 2 - 12;
        int searchY = 22;

        // ── Search field ──────────────────────────────────────────────────────
        TextFieldWidget searchField = new TextFieldWidget(
            client.textRenderer, 8, searchY, fieldW, 18,
            Text.translatable("servermanagerplus.search.worlds")
        );
        searchField.setSuggestion("🔍 Search worlds by name...");
        searchField.setMaxLength(128);
        searchField.setText(worldSearchQuery);
        searchField.setChangedListener(query -> {
            worldSearchQuery = query.toLowerCase().trim();
            refreshList();
        });
        addChild(screen, searchField);

        // ── Pinned toggle ─────────────────────────────────────────────────────
        ButtonWidget pinnedBtn = ButtonWidget.builder(
            pinnedBtnText(),
            btn -> {
                showPinnedWorldsOnly = !showPinnedWorldsOnly;
                btn.setMessage(pinnedBtnText());
                refreshList();
            }
        ).dimensions(sw / 2 + 4, searchY, fieldW, 18).build();
        addChild(screen, pinnedBtn);
    }

    /** Re-triggers the world list to repopulate (our WorldListWidgetMixin then filters). */
    public static void refreshList() {
        if (activeWidget == null) return;
        try {
            // Call WorldListWidget#show(List) with the stored full level list
            var showMethod = WorldListWidget.class.getDeclaredMethod("show",
                java.util.List.class);
            showMethod.setAccessible(true);

            // Get the full unfiltered list stored on the widget
            var levelListField = WorldListWidget.class.getDeclaredField("levels");
            levelListField.setAccessible(true);
            Object levels = levelListField.get(activeWidget);
            if (levels != null) {
                showMethod.invoke(activeWidget, levels);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Text pinnedBtnText() {
        return showPinnedWorldsOnly
            ? Text.literal("★ Pinned Worlds")
            : Text.literal("☆ All Worlds");
    }

    private static void addChild(SelectWorldScreen screen, Object child) {
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
