package com.azorstudio.servermanagerplus.mixin;

import com.azorstudio.servermanagerplus.data.ServerDataManager;
import com.azorstudio.servermanagerplus.gui.EnhancedWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts WorldListWidget population to apply search filtering
 * and sort pinned worlds to the top.
 */
@Mixin(WorldListWidget.class)
public abstract class WorldListWidgetMixin {

    /**
     * Called when the world list loads entries — we filter and sort here.
     */
    @Inject(method = "show", at = @At("RETURN"))
    private void onShow(List<LevelSummary> levels, CallbackInfo ci) {
        String query = EnhancedWorldScreen.worldSearchQuery;
        boolean pinnedOnly = EnhancedWorldScreen.showPinnedWorldsOnly;
        if (query.isEmpty() && !pinnedOnly) {
            sortPinnedWorldsFirst();
            return;
        }
        applyFilter(levels);
    }

    private void applyFilter(List<LevelSummary> allLevels) {
        try {
            var childrenField = net.minecraft.client.gui.widget.EntryListWidget.class
                .getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> children = (List<Object>) childrenField.get(this);
            children.clear();

            ServerDataManager dm = ServerDataManager.getInstance();
            String query = EnhancedWorldScreen.worldSearchQuery;
            boolean pinnedOnly = EnhancedWorldScreen.showPinnedWorldsOnly;

            // Get the method to add a world entry
            var addEntryMethod = WorldListWidget.class
                .getDeclaredMethod("addEntry", LevelSummary.class);
            addEntryMethod.setAccessible(true);

            List<LevelSummary> pinned = new ArrayList<>();
            List<LevelSummary> rest = new ArrayList<>();

            for (LevelSummary level : allLevels) {
                if (level == null) continue;
                String folder = level.getName() != null ? level.getName() : "";
                String display = level.getDisplayName() != null
                    ? level.getDisplayName().toLowerCase() : folder.toLowerCase();

                if (pinnedOnly && !dm.isWorldPinned(folder)) continue;
                if (!query.isEmpty()) {
                    if (!display.contains(query) && !folder.toLowerCase().contains(query)) continue;
                }
                if (dm.isWorldPinned(folder)) pinned.add(level);
                else rest.add(level);
            }

            for (LevelSummary l : pinned) addEntryMethod.invoke(this, l);
            for (LevelSummary l : rest) addEntryMethod.invoke(this, l);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sortPinnedWorldsFirst() {
        try {
            var childrenField = net.minecraft.client.gui.widget.EntryListWidget.class
                .getDeclaredField("children");
            childrenField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> children = (List<Object>) childrenField.get(this);

            // Find the WorldEntry class and its level field
            Class<?> worldEntryClass = null;
            for (Class<?> inner : WorldListWidget.class.getDeclaredClasses()) {
                if (inner.getSimpleName().equals("WorldEntry")) {
                    worldEntryClass = inner;
                    break;
                }
            }
            if (worldEntryClass == null) return;

            var levelField = worldEntryClass.getDeclaredField("level");
            levelField.setAccessible(true);

            ServerDataManager dm = ServerDataManager.getInstance();
            List<Object> pinned = new ArrayList<>();
            List<Object> rest = new ArrayList<>();

            for (Object entry : children) {
                try {
                    LevelSummary summary = (LevelSummary) levelField.get(entry);
                    if (dm.isWorldPinned(summary.getName())) pinned.add(entry);
                    else rest.add(entry);
                } catch (Exception e) {
                    rest.add(entry);
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
