package com.leaf.techjs.kubejs;

import com.leaf.techjs.context.TechSystem;
import com.leaf.techjs.context.TechSystemStorage;
import net.minecraft.server.MinecraftServer;

public class TechSystemTool {
    public void setActive(String technologyId, boolean active) {
        if (TechSystemStorage.getInstance() == null) return;
        TechSystemStorage.getInstance().setActive(technologyId, active);
    }

    public boolean isActive(String technologyId) {
        if (TechSystemStorage.getInstance() == null) return false;
        return TechSystemStorage.getInstance().isActive(technologyId);
    }

    public void applyTechnology(MinecraftServer server) {
        TechSystem.apply(server);
    }
}
