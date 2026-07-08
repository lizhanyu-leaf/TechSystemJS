package com.leaf.techjs.context.items;

import com.leaf.techjs.TechSystemJS;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public class TechItemInfo {
    private final String id;
    private final ResourceLocation techId;
    private Supplier<ItemStack> icon;
    private ResourceLocation iconId;

    private TechItemInfo(String id, ResourceLocation techId, Supplier<ItemStack> icon) {
        this.id = id;
        this.techId = techId;
        this.icon = icon;
    }

    private TechItemInfo(String id, ResourceLocation techId) {
        this(id, techId, () -> ItemStack.EMPTY);
    }

    private TechItemInfo setIconId(ResourceLocation id) {
        iconId = id;
        return this;
    }

    public static TechItemInfo of(String id, ResourceLocation techId, Supplier<ItemStack> icon) {
        return new TechItemInfo(id, techId, icon);
    }

    public static TechItemInfo of(String id, ResourceLocation techId, ResourceLocation iconId) {
        return new TechItemInfo(id, techId).setIconId(iconId);
    }

    public static TechItemInfo of(String id, String techId, String iconId) {
        try {
            return of(id, ResourceLocation.parse(techId), ResourceLocation.parse(iconId));
        } catch (ResourceLocationException e) {
            TechSystemJS.LOGGER.warn("TechSystemJS : Parsing {} failed", id);
            return null;
        }
    }

    public TechItemInfo getIconFromRegistry(IForgeRegistry<Item> registry) {
        var optional = registry.getHolder(iconId);
        if (optional.isEmpty()) return this;
        var itemHolder = optional.get();
        var item = itemHolder.get().getDefaultInstance();
        icon = () -> item;
        return this;
    }

    public Supplier<ItemStack> getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public ResourceLocation getItemId() {
        return ResourceLocation.fromNamespaceAndPath(TechSystemJS.MOD_ID, id);
    }

    public ResourceLocation getTechId() {
        return techId;
    }
}
