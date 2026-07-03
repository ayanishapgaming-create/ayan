package com.azorstudio.servermanagerplus.gui;

import com.azorstudio.servermanagerplus.data.ServerDataManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that injects a search bar + pin controls into the
 * vanilla MultiplayerScreen. Called from MultiplayerScreenMixin.
 *
 * Layout:
 * ┌─────────────────────────────────────────────┐
 * │  [🔍 Search servers...  ]  [Show Pinned ★]  │  ← injected row
 * │  ─────────────────────────────────────────  │
 * │  [📌 Server A]  [Server B]  [Server C]  ... │  ← vanilla list (filtered)
 * └─────────────────────────────────────────────┘
 */
@Environment(EnvType.CLIENT)
public class EnhancedMultiplayerScreen {

    // Holds the current search query so the widget filter can read it
    public static String serverSearchQuery = "";
    public static boolean showPinnedOnly = false;

    /**
     * Called from the mixin after vanilla init() finishes.
     * Adds a search field and a "Pinned" toggle button at the top of the screen.
     */
    public static void injectIntoMultiplayerScreen(MultiplayerScreen screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        int screenWidth = screen.width;
        int searchWidth = screenWidth / 2 - 10;
        int searchX = 8;
        int searchY = 18; // below the title, above the list

        // ── Search field ──────────────────────────────────────────────────────
        TextFieldWidget searchField = new TextFieldWidget(
            client.textRenderer,
            searchX, searchY,
            searchWidth, 16,
            Text.translatable("servermanagerplus.search.servers")
        );
        // setSuggestion shows grey hint text when the field is empty
        searchField.setSuggestion("🔍 Search by name or IP...");
        searchField.setMaxLength(128);
        searchField.setText(serverSearchQuery);
        searchField.setChangedListener(query -> {
            serverSearchQuery = query.toLowerCase();
        });

        addChildToScreen(screen, searchField);

        // ── Pinned toggle button ───────────────────────────────────────────────
        ButtonWidget pinnedButton = ButtonWidget.builder(
            getPinnedButtonText(),
            btn -> {
                showPinnedOnly = !showPinnedOnly;
                btn.setMessage(getPinnedButtonText());
            }
        ).dimensions(screenWidth / 2 + 4, searchY, searchWidth, 16).build();

        addChildToScreen(screen, pinnedButton);
    }

    private static Text getPinnedButtonText() {
        return showPinnedOnly
            ? Text.literal("★ Pinned Only")
            : Text.literal("☆ All Servers");
    }

    /**
     * Reflectively invokes the protected Screen#addDrawableChild so that
     * this external utility class can add widgets to a Screen subclass.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Element & Drawable & Selectable> void addChildToScreen(
            MultiplayerScreen screen, T child) {
        try {
            Method m = net.minecraft.client.gui.screen.Screen.class
                .getDeclaredMethod("addDrawableChild", Element.class);
            m.setAccessible(true);
            m.invoke(screen, child);
        } catch (Exception e) {
            // Fallback: try with the concrete type
            try {
                for (Method m : net.minecraft.client.gui.screen.Screen.class.getDeclaredMethods()) {
                    if (m.getName().equals("addDrawableChild")) {
                        m.setAccessible(true);
                        m.invoke(screen, child);
                        return;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Called by the server list widget to determine if a server should be visible.
     * Filters by search query and pinned state.
     */
    public static boolean shouldShowServer(ServerInfo serverInfo) {
        ServerDataManager dm = ServerDataManager.getInstance();

        if (showPinnedOnly && !dm.isServerPinned(serverInfo.address)) {
            return false;
        }

        if (!serverSearchQuery.isEmpty()) {
            String name = serverInfo.name != null ? serverInfo.name.toLowerCase() : "";
            String addr = serverInfo.address != null ? serverInfo.address.toLowerCase() : "";
            return name.contains(serverSearchQuery) || addr.contains(serverSearchQuery);
        }

        return true;
    }

    /**
     * Sort servers: pinned first, then alphabetical.
     */
    public static List<ServerInfo> sortWithPinnedFirst(List<ServerInfo> servers) {
        ServerDataManager dm = ServerDataManager.getInstance();
        List<ServerInfo> pinned = new ArrayList<>();
        List<ServerInfo> unpinned = new ArrayList<>();
        for (ServerInfo s : servers) {
            if (dm.isServerPinned(s.address)) pinned.add(s);
            else unpinned.add(s);
        }
        List<ServerInfo> result = new ArrayList<>(pinned);
        result.addAll(unpinned);
        return result;
    }
}
