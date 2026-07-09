package com.leaf.techjs.context.items;

import com.leaf.techjs.TechSystemJS;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public class TechItemInfo {
    private final String itemId;
    private final ResourceLocation techId;
    private Supplier<ItemStack> icon;
    private ResourceLocation iconId;

    private TechItemInfo(String itemId, ResourceLocation techId, Supplier<ItemStack> icon) {
        this.itemId = itemId;
        this.techId = techId;
        this.icon = icon;
    }

    private TechItemInfo(String itemId, ResourceLocation techId) {
        this(itemId, techId, () -> ItemStack.EMPTY);
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

    public String getItemIdToString() {
        return itemId;
    }

    public ResourceLocation getItemId() {
        return ResourceLocation.fromNamespaceAndPath(TechSystemJS.MOD_ID, itemId);
    }

    public ResourceLocation getTechId() {
        return techId;
    }

    @Override
    public String toString() {
        return "TechItemInfo{" +
                "id='" + itemId + '\'' +
                ", techId=" + techId +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TechItemInfo techItemInfo = (TechItemInfo) obj;
        // 对于物品注册，itemId 是唯一标识符
        return Objects.equals(itemId, techItemInfo.itemId);
    }

    @Override
    public int hashCode() {
        // 对于物品注册，itemId 是唯一标识符
        return Objects.hash(itemId);
    }
}
