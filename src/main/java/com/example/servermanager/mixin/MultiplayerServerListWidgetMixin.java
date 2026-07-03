package com.example.servermanager.mixin;

import com.example.servermanager.client.ServerSearchManager;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiplayerServerListWidget.class)
public abstract class MultiplayerServerListWidgetMixin {

    @Shadow
    private List<MultiplayerServerListWidget.ServerEntry> servers;

    @Shadow
    private List<MultiplayerServerListWidget.LanServerEntry> lanServers;

    @Shadow
    private MultiplayerServerListWidget.Entry scanningEntry;

    @Shadow
    protected abstract void clearEntries();

    @Shadow
    protected abstract int addEntry(MultiplayerServerListWidget.Entry entry);

    @Inject(method = "updateEntries", at = @At("HEAD"), cancellable = true)
    private void onUpdateEntries(CallbackInfo ci) {
        this.clearEntries();

        String query = ServerSearchManager.searchQuery == null ? "" : ServerSearchManager.searchQuery.trim().toLowerCase();

        List<MultiplayerServerListWidget.ServerEntry> filteredServers = new ArrayList<>();
        if (this.servers != null) {
            for (MultiplayerServerListWidget.ServerEntry entry : this.servers) {
                ServerInfo serverInfo = entry.getServer();
                if (query.isEmpty()
                        || serverInfo.name.toLowerCase().contains(query)
                        || serverInfo.address.toLowerCase().contains(query)) {
                    filteredServers.add(entry);
                }
            }
        }

        filteredServers.sort((a, b) -> {
            boolean aPinned = ServerSearchManager.pinnedServers.contains(a.getServer().address);
            boolean bPinned = ServerSearchManager.pinnedServers.contains(b.getServer().address);
            if (aPinned && !bPinned) return -1;
            if (!aPinned && bPinned) return 1;
            return 0;
        });

        filteredServers.forEach(this::addEntry);

        if (this.scanningEntry != null) {
            this.addEntry(this.scanningEntry);
        }

        if (this.lanServers != null) {
            this.lanServers.forEach(this::addEntry);
        }

        ci.cancel();
    }
}
