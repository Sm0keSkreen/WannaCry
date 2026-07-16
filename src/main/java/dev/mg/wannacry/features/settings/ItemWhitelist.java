package dev.mg.wannacry.features.settings;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class ItemWhitelist {

    public static final String FIST_ID = "satellite:fist";

    private final LinkedHashSet<String> ids = new LinkedHashSet<>();

    public ItemWhitelist() {}

    public ItemWhitelist(String... initialIds) {
        ids.addAll(Arrays.asList(initialIds));
    }

    public boolean isAllowed(ItemStack held) {
        if (held == null || held.isEmpty()) {
            return ids.contains(FIST_ID);
        }
        String id = itemToId(held.getItem());
        return ids.contains(id);
    }

    public boolean contains(String id)  { return ids.contains(id); }
    public boolean isEmpty()            { return ids.isEmpty(); }
    public int     size()               { return ids.size(); }

    public boolean add(String id)       { return ids.add(id); }

    public boolean remove(String id)    { return ids.remove(id); }

    public void toggle(String id) {
        if (!ids.remove(id)) ids.add(id);
    }

    public Set<String> getIds() { return Collections.unmodifiableSet(ids); }

    public String serialize() {
        return String.join(",", ids);
    }

    public void deserialize(String data) {
        ids.clear();
        if (data == null || data.isBlank()) return;
        for (String part : data.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) ids.add(trimmed);
        }
    }

    public static String itemToId(Item item) {
        Identifier key = BuiltInRegistries.ITEM.getKey(item);
        return key != null ? key.toString() : "minecraft:air";
    }

    public static Item idToItem(String id) {
        try {
            return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
        } catch (Exception e) {
            return Items.AIR;
        }
    }

    public static String displayName(String id) {
        if (FIST_ID.equals(id)) return "Fist (Empty Hand)";
        Item item = idToItem(id);
        if (item == Items.AIR) return id;
        return item.getName(item.getDefaultInstance()).getString();
    }

    public static List<String> searchItems(String query) {
        String lower = query == null ? "" : query.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();

        if (lower.isEmpty() || "fist".contains(lower) || "empty hand".contains(lower)
                || FIST_ID.contains(lower)) {
            result.add(FIST_ID);
        }

        BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .sorted(Comparator.comparing(ItemWhitelist::itemToId))
                .forEach(item -> {
                    String id   = itemToId(item);
                    String name = item.getName(item.getDefaultInstance()).getString().toLowerCase(Locale.ROOT);
                    if (lower.isEmpty() || id.contains(lower) || name.contains(lower)) {
                        result.add(id);
                    }
                });

        return result;
    }
}
