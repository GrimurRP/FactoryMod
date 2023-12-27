package com.github.igotyou.FactoryMod.inputItem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class InputItem {
	private int _amount;
	private final int _priority;
	private final boolean _useDisplayNameAsName;

	InputItem(int amount, int priority, boolean useDisplayNameAsName) {
		_amount = amount;
		_priority = priority;
		_useDisplayNameAsName = useDisplayNameAsName;
	}

	public abstract InputItem clone();

	public int getAmount() {
		return _amount;
	}

	public void addAmount(int amount) {
		_amount += amount;
	}

	public int getPriority() {
		return _priority;
	}

	public boolean useDisplayNameAsName() {
		return _useDisplayNameAsName;
	}

	public boolean isStack(ItemStack itemStack) {
		return false;
	}

	public boolean isTag(Tag<Material> tag, ItemMeta meta) {
		return false;
	}

	public boolean isList(List<Material> materials, ItemMeta meta) {
		return false;
	}

	public abstract boolean canBeUsed(ItemStack itemStack);

	public ItemStack getGUIItemStack() {
		return getItemStack(null, false, true);
	}

	public ItemStack getDropItemStack() {
		return getItemStack(null, false, false);
	}

	ItemStack getItemStack(ItemMap invMap, boolean showRunNumbers, boolean useCustomName) {
		ItemStack clone = getItemStack(useCustomName);
		if (invMap == null) {
			return clone;
		}

		int inventoryAmount = getInventoryAmount(invMap);
		int count = inventoryAmount / _amount;

		addGUILore(showRunNumbers, clone, count);

		return clone;
	}

	protected abstract ItemStack getItemStack(boolean useCustomName);

	private static void addGUILore(boolean showRunNumbers, ItemStack itemStack, int count) {
		ItemMeta meta = itemStack.getItemMeta();
		String text;
		TextColor color;

		if (showRunNumbers) {
			text = "Enough materials for " + count + " runs";
			color = NamedTextColor.GREEN;
		} else if (count > 0) {
			text = "Enough of this material available to upgrade";
			color = NamedTextColor.GREEN;
		} else {
			text = "Not enough of this materials available to upgrade";
			color = NamedTextColor.RED;
		}

		List<Component> lore = meta.lore();
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.add(Component.text(text, color));

		meta.lore(lore);
		itemStack.setItemMeta(meta);
	}

	private int getInventoryAmount(ItemMap itemMap) {
		int amount = 0;
		for (Map.Entry<ItemStack, Integer> entry : itemMap.getEntrySet()) {
			ItemStack current = entry.getKey();
			Integer currentAmount = entry.getValue();
			if (currentAmount != null && currentAmount > 0 && canBeUsed(current)) {
				amount += currentAmount;
			}
		}
		return amount;
	}
}
