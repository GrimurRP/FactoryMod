package com.github.igotyou.FactoryMod.inputItem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

import java.util.List;

class InputItemList extends InputItem {
	private final List<Material> _materials;
	private final ItemStack _icon;
	private final ItemMeta _meta;

	public InputItemList(List<Material> materials, ItemMeta meta, ItemStack icon, int amount) {
		super(amount, 1, true);
		_materials = materials;
		_icon = icon;
		_meta = meta;
	}

	@Override
	public InputItem clone() {
		return new InputItemList(_materials, _meta.clone(), _icon.clone(), getAmount());
	}

	@Override
	public boolean isList(List<Material> materials, ItemMeta meta) {
		return materials.equals(_materials)
				&& MetaUtils.areMetasEqual(_meta, meta);
	}

	@Override
	public boolean canBeUsed(ItemStack itemStack) {
		return _materials.contains(itemStack.getType())
				&& MetaUtils.areMetasEqual(_meta, itemStack.getItemMeta());
	}

	@Override
	protected ItemStack getItemStack(boolean useCustomName) {
		ItemStack clone = _icon.asQuantity(getAmount());
		if (!useCustomName) {
			clone.setItemMeta(_meta.clone());
		}
		return clone;
	}
}
