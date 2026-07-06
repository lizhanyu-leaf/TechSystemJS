package com.leaf.techjs.kubejs;

import com.leaf.techjs.context.TechInfo;
import com.leaf.techjs.kubejs.event.TechSystemRecipesEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public interface TechSystemEvents {
    EventGroup GROUP = EventGroup.of("TechSystemEvents");

    Extra SUPPORTS_TECHNOLOGY = new Extra()
            .transformer(TechSystemEvents::transformTech).toString(obj -> ((TechInfo) obj).id)
            .identity().describeType(ctx -> ctx.javaType(TechInfo.class));

    private static TechInfo transformTech(Object obj) {
        if (obj == null) return TechInfo.EMPTY;
        if (obj instanceof TechInfo)
            return (TechInfo) obj;
        if (obj instanceof String)
            return TechInfo.of((String) obj);
        return TechInfo.EMPTY;
    }

    EventHandler ON_TECHNOLOGY_LOAD
            = GROUP.server("onTechLoad", () -> TechSystemRecipesEventJS.class).extra(SUPPORTS_TECHNOLOGY);
}
