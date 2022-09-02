package com.github.igotyou.FactoryMod.inputItem;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//////////////////////////////////
// The class is not thread-safe //
//////////////////////////////////
public class InputItemMap {
	private final List<InputItem> _items;

	private InputItemMap(List<InputItem> items) {
		_items = items;
	}

	public InputItemMap() {
		_items = new ArrayList<>();
	}

	public InputItemMap(ItemStack itemStack) {
		this();
		addItemStack(itemStack);
	}

	@Override
	public InputItemMap clone() {
		var items = new ArrayList<InputItem>();
		for (InputItem item : _items) {
			items.add(item.clone());
		}
		return new InputItemMap(items);
	}

	public void addItemStack(ItemStack itemStack) {
		int amount = itemStack.getAmount();
		if (amount <= 0) {
			return;
		}

		Optional<InputItem> existing = _items
				.stream()
				.filter(x -> x.isStack(itemStack))
				.findFirst();

		if (existing.isPresent()) {
			existing.get().addAmount(amount);
		} else {
			_items.add(new InputItemStack(itemStack, amount));
		}
	}

	public void addItemTag(Tag<Material> tag, ItemMeta meta, ItemStack icon, int amount) {
		if (amount == 0) {
			return;
		}

		Optional<InputItem> existing = _items
				.stream()
				.filter(x -> x.isTag(tag, meta))
				.findFirst();

		if (existing.isPresent()) {
			existing.get().addAmount(amount);
		} else {
			_items.add(new InputItemTag(tag, meta, icon, amount));
		}
	}

	public void addItemList(List<Material> materials, ItemMeta meta, ItemStack icon, int amount) {
		if (amount == 0) {
			return;
		}

		Optional<InputItem> existing = _items
				.stream()
				.filter(x -> x.isList(materials, meta))
				.findFirst();

		if (existing.isPresent()) {
			existing.get().addAmount(amount);
		} else {
			_items.add(new InputItemList(materials, meta, icon, amount));
		}
	}

	public boolean isContainedIn(Inventory inventory) {
		ItemMap invMap = new ItemMap(inventory);
		return removeFromInventory(invMap, null);
	}

	public boolean containedExactlyIn(Inventory inventory) {
		ItemMap invMap = new ItemMap(inventory);
		if (!removeFromInventory(invMap, null)) {
			return false;
		}

		for (Map.Entry<ItemStack, Integer> entry : invMap.getEntrySet()) {
			if (entry.getValue() != null && entry.getValue() > 0) {
				return false;
			}
		}

		return true;
	}

	public boolean removeSafelyFrom(Inventory inventory) {
		ItemMap invMap = new ItemMap(inventory);
		var removeItems = new ArrayList<ItemStack>();
		if (!removeFromInventory(invMap, removeItems)) {
			return false;
		}

		ItemMap removeMap = new ItemMap();
		for (ItemStack itemStack : removeItems) {
			removeMap.addItemStack(itemStack);
		}

		return removeMap.removeSafelyFrom(inventory);
	}

	public List<ItemStack> getItemStackRepresentation() {
		return getItemStackRepresentation(null, false);
	}

	public List<ItemStack> getItemStackRepresentation(Inventory inventory, boolean showRunNumbers) {
		ItemMap invMap = inventory != null ? new ItemMap(inventory) : null;

		var result = new ArrayList<ItemStack>();
		for (InputItem item : _items) {
			ItemStack itemStack = item.getItemStack(invMap, showRunNumbers, true);
			int amount = item.getAmount();
			while (amount > 0) {
				ItemStack toAdd = itemStack.clone();
				int addAmount = Math.min(amount, itemStack.getMaxStackSize());
				toAdd.setAmount(addAmount);
				result.add(toAdd);
				amount -= addAmount;
			}
		}
		return result;
	}

	public List<InputItem> getItems() {
		return _items;
	}

	public int getMultiplesContainedIn(Inventory inventory) {
		ItemMap invMap = new ItemMap(inventory);
		int count = 0;

		while (removeFromInventory(invMap, null)) {
			count++;
		}

		return count;
	}

	public int getAmount(ItemStack itemStack) {
		int amount = 0;
		for (InputItem item : _items) {
			if (item.canBeUsed(itemStack)) {
				amount += item.getAmount();
			}
		}
		return amount;
	}

	public void merge(InputItemMap map) {
		for (InputItem item : map._items) {
			_items.add(item.clone());
		}
	}

	public boolean isEmpty() {
		return _items.isEmpty();
	}

	private boolean removeFromInventory(ItemMap invMap, List<ItemStack> removeItems) {
		List<InputItem> sortedItems = _items
				.stream()
				.sorted(Comparator.comparingInt(InputItem::getPriority))
				.toList();

		for (InputItem item : sortedItems) {
			int remainedAmount = removeFromInventory(invMap, removeItems, item);
			if (remainedAmount > 0) {
				return false;
			}
		}

		return true;
	}

	private int removeFromInventory(ItemMap invMap, List<ItemStack> removeItems, InputItem item) {
		int amount = item.getAmount();

		for (Map.Entry<ItemStack, Integer> entry : invMap.getEntrySet()) {
			ItemStack current = entry.getKey();
			Integer currentAmount = entry.getValue();
			if (currentAmount == null || currentAmount == 0 || !item.canBeUsed(current)) {
				continue;
			}

			int removeAmount = Math.min(amount, currentAmount);
			ItemStack removeItem = cloneInventoryItemStack(current, removeAmount);

			invMap.removeItemStack(removeItem);

			if (removeItems != null) {
				removeItems.add(removeItem);
			}

			amount -= removeAmount;
			if (amount == 0) {
				break;
			}
		}

		return amount;
	}

	private static ItemStack cloneInventoryItemStack(ItemStack itemStack, int newAmount) {
		ItemStack clone = itemStack.asQuantity(newAmount);
		ItemUtils.handleItemMeta(clone, (Repairable meta) -> {
			meta.setRepairCost(0);
			return true;
		});

		return clone;
	}
}
