package com.github.igotyou.FactoryMod.inputItem;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// List of tags: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Tag.html
// List of tags for 1.18: https://helpch.at/docs/1.18/org/bukkit/Tag.html
public class InputItemParser {

	private static final Logger Logger = Bukkit.getLogger();
	private static final String TagPrefix = "tags.";

	public static InputItemMap parse(ConfigurationSection config) {
		final var result = new InputItemMap();
		if (config == null) {
			return result;
		}
		for (final String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current != null) {
				parse(result, current);
			}
		}
		return result;
	}

	private static void parse(InputItemMap result, ConfigurationSection current) {
		if (current.isList("material")) {
			parseItemList(result, current);
			return;
		}

		String material = current.getString("material");
		if (material != null
				&& material.toLowerCase().startsWith(TagPrefix)
				&& material.length() > TagPrefix.length()
		) {
			parseItemTag(result, current);
			return;
		}

		ItemMap partMap = ConfigHelper.parseItemMapDirectly(current);
		merge(result, partMap);
	}

	private static void parseItemList(InputItemMap result, ConfigurationSection current) {
		List<Material> materials = new ArrayList<>();
		List<String> materialNames = current.getStringList("material");
		for (String materialName : materialNames) {
			try {
				materials.add(Material.valueOf(materialName));
			} catch (IllegalArgumentException iae) {
				Logger.severe("Failed to find material " + current.getString("material") + " in section " + current.getCurrentPath());
				return;
			}
		}

		int amount = current.getInt("amount", 1);
		String iconName = current.getString("icon_name");
		List<Component> lore = parseLore(current);
		Material iconMaterial = findIconMaterial(current, materials.get(0));

		ItemStack icon = new ItemStack(iconMaterial);
		ItemMeta iconMeta = icon.getItemMeta();
		ItemMeta meta = iconMeta.clone();

		if (iconName != null && iconName.length() > 0) {
			iconMeta.displayName(Component.text(iconName));
		}
		if (lore != null && lore.size() > 0) {
			iconMeta.lore(lore);
			meta.lore(lore);
		}

		icon.setItemMeta(iconMeta);

		result.addItemList(materials, meta, icon, amount);
	}

	private static void parseItemTag(InputItemMap result, ConfigurationSection current) {
		Tag<Material> tag = findTag(current);
		if (tag == null) {
			return;
		}

		int amount = current.getInt("amount", 1);
		String iconName = current.getString("icon_name");
		List<Component> lore = parseLore(current);
		Material iconMaterial = findIconMaterial(current, tag.getValues().stream().findFirst().orElse(null));

		ItemStack icon = new ItemStack(iconMaterial);
		ItemMeta iconMeta = icon.getItemMeta();
		ItemMeta meta = iconMeta.clone();

		if (iconName != null && iconName.length() > 0) {
			iconMeta.displayName(Component.text(iconName));
		}
		if (lore != null && lore.size() > 0) {
			iconMeta.lore(lore);
			meta.lore(lore);
		}

		icon.setItemMeta(iconMeta);

		result.addItemTag(tag, meta, icon, amount);
	}

	private static List<Component> parseLore(ConfigurationSection section) {
		List<String> lore = section.getStringList("lore");
		if (lore.size() == 0) {
			return null;
		}

		var loreList = new ArrayList<Component>();
		for (String line : lore) {
			loreList.add(Component.text(line));
		}

		return loreList;
	}

	private static Material findIconMaterial(ConfigurationSection section, Material defaultIcon) {
		String iconName = section.getString("icon");
		if (iconName == null || iconName.length() == 0) {
			return defaultIcon;
		}

		try {
			return Material.valueOf(iconName);
		} catch (IllegalArgumentException e) {
			Logger.severe("Failed to find icon material " + iconName + " in section " + section.getCurrentPath());
			return defaultIcon;
		}
	}

	private static Tag<Material> findTag(ConfigurationSection section) {
		String material = section.getString("material");
		if (material == null) {
			return null;
		}

		String tagName = material.substring(TagPrefix.length()).toUpperCase();

		try {
			Field field = Tag.class.getField(tagName);
			return (Tag<Material>)field.get(null);
		} catch (NoSuchFieldException
				 | SecurityException
				 | IllegalArgumentException
				 | IllegalAccessException
				 | ClassCastException e
		) {
			Logger.severe("Failed to find tag " + tagName + " in section " + section.getCurrentPath());
			return null;
		}
	}

	private static void merge(InputItemMap inputItemMap, ItemMap itemMap) {
		for (Map.Entry<ItemStack, Integer> entry : itemMap.getEntrySet()) {
			ItemStack itemStack = entry.getKey().asQuantity(entry.getValue());
			inputItemMap.addItemStack(itemStack);
		}
	}
}
