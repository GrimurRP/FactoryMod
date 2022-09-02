package com.github.igotyou.FactoryMod.inputItem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

class InputItemStack extends InputItem {
	private final ItemStack _itemStack;
	private final ItemMeta _meta;

	public InputItemStack(ItemStack itemStack, int amount) {
		super(amount, 0, false);
		_itemStack = itemStack;
		_meta = itemStack.getItemMeta();
	}

	@Override
	public InputItem clone() {
		return new InputItemStack(_itemStack.clone(), getAmount());
	}

	@Override
	public boolean isStack(ItemStack itemStack) {
		return _itemStack.getType() == itemStack.getType()
				&& MetaUtils.areMetasEqual(_meta, itemStack.getItemMeta());
	}

	@Override
	public boolean canBeUsed(ItemStack itemStack) {
		return itemStack.getType() == _itemStack.getType()
				&& MetaUtils.areMetasEqual(_meta, itemStack.getItemMeta());
	}

	@Override
	protected ItemStack getItemStack(boolean useCustomName) {
		return _itemStack.asQuantity(getAmount());
	}
}
