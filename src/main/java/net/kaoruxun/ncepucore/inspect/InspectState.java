package net.kaoruxun.ncepucore.inspect;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InspectState {
    private InspectState() {}

    private static final Set<UUID> INSPECTING = ConcurrentHashMap.newKeySet();

    public static boolean isInspecting(UUID uuid) {
        return INSPECTING.contains(uuid);
    }

    public static boolean setInspecting(UUID uuid, boolean enabled) {
        if (enabled) return INSPECTING.add(uuid);
        return INSPECTING.remove(uuid);
    }

    public static boolean toggle(UUID uuid) {
        if (INSPECTING.contains(uuid)) {
            INSPECTING.remove(uuid);
            return false;
        }
        INSPECTING.add(uuid);
        return true;
    }

    public static void clear(UUID uuid) {
        INSPECTING.remove(uuid);
    }
}


