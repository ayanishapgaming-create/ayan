package com.azorstudio.servermanagerplus.mixin;

import com.azorstudio.servermanagerplus.data.ServerDataManager;
import com.azorstudio.servermanagerplus.gui.EnhancedMultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts MultiplayerServerListWidget#setServers to apply
 * search filtering and pin-first sorting before the list is displayed.
 */
@Mixin(MultiplayerServerListWidget.class)
public abstract class ServerListWidgetMixin {

    /**
     * Called whenever the server list is (re)loaded — we filter and sort here.
     */
    @Inject(method = "setServers", at = @At("HEAD"), cancellable = true)
    private void onSetServers(ServerList servers, CallbackInfo ci) {
        // Let vanilla populate the full list first, then we'll filter
        // We cancel and repopulate only when a filter is active
        String query = EnhancedMultiplayerScreen.serverSearchQuery;
        boolean pinnedOnly = EnhancedMultiplayerScreen.showPinnedOnly;

        if (query.isEmpty() && !pinnedOnly) {
            // No filter — just sort pinned to top
            // We still let vanilla run, then sort in RETURN inject
            return;
        }
        // Filter active: cancel vanilla and rebuild filtered list
        ci.cancel();
        rebuildFilteredList((MultiplayerServerListWidget)(Object)this, servers);
    }

    /**
     * After vanilla populates (no filter), sort pinned servers to the top.
     */
    @Inject(method = "setServers", at = @At("RETURN"))
    private void onSetServersReturn(ServerList servers, CallbackInfo ci) {
        // Sort pinned to top without filtering
        String query = EnhancedMultiplayerScreen.serverSearchQuery;
        boolean pinnedOnly = EnhancedMultiplayerScreen.showPinnedOnly;
        if (!query.isEmpty() || pinnedOnly) return; // already handled above
        sortPinnedFirst((MultiplayerServerListWidget)(Object)this);
    }

    private void rebuildFilteredList(MultiplayerServerListWidget widget, ServerList servers) {
        try {
            // Clear existing entries
            var childrenField = net.minecraft.client.gui.widget.EntryListWidget.class
                .getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> children = (List<Object>) childrenField.get(widget);
            children.clear();

            ServerDataManager dm = ServerDataManager.getInstance();
            String query = EnhancedMultiplayerScreen.serverSearchQuery;
            boolean pinnedOnly = EnhancedMultiplayerScreen.showPinnedOnly;

            List<ServerInfo> pinned = new ArrayList<>();
            List<ServerInfo> rest = new ArrayList<>();

            for (int i = 0; i < servers.size(); i++) {
                ServerInfo info = servers.get(i);
                // Apply pinned filter
                if (pinnedOnly && !dm.isServerPinned(info.address)) continue;
                // Apply search filter
                if (!query.isEmpty()) {
                    String name = info.name != null ? info.name.toLowerCase() : "";
                    String addr = info.address != null ? info.address.toLowerCase() : "";
                    if (!name.contains(query) && !addr.contains(query)) continue;
                }
                if (dm.isServerPinned(info.address)) pinned.add(info);
                else rest.add(info);
            }

            // Add pinned first
            var addServerMethod = MultiplayerServerListWidget.class
                .getDeclaredMethod("addEntry", ServerInfo.class);
            addServerMethod.setAccessible(true);
            for (ServerInfo s : pinned) addServerMethod.invoke(widget, s);
            for (ServerInfo s : rest) addServerMethod.invoke(widget, s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sortPinnedFirst(MultiplayerServerListWidget widget) {
        try {
            var childrenField = net.minecraft.client.gui.widget.EntryListWidget.class
                .getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> children = (List<Object>) childrenField.get(widget);

            var serverField = MultiplayerServerListWidget.ServerEntry.class
                .getDeclaredField("server");
            serverField.setAccessible(true);

            ServerDataManager dm = ServerDataManager.getInstance();
            List<Object> pinned = new ArrayList<>();
            List<Object> rest = new ArrayList<>();

            for (Object entry : children) {
                try {
                    ServerInfo info = (ServerInfo) serverField.get(entry);
                    if (dm.isServerPinned(info.address)) pinned.add(entry);
                    else rest.add(entry);
                } catch (Exception e) {
                    rest.add(entry); // non-server entries (LAN header etc.)
                }
            }

            children.clear();
            children.addAll(pinned);
            children.addAll(rest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
