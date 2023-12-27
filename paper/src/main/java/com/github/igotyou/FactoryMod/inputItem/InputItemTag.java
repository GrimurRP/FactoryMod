package com.github.igotyou.FactoryMod.inputItem;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

class InputItemTag extends InputItem {
	private final Tag<Material> _tag;
	private final ItemStack _icon;
	private final ItemMeta _meta;

	public InputItemTag(Tag<Material> tag, ItemMeta meta, ItemStack icon, int amount) {
		super(amount, 1, true);
		_tag = tag;
		_icon = icon;
		_meta = meta;
	}

	@Override
	public InputItem clone() {
		return new InputItemTag(_tag, _meta.clone(), _icon.clone(), getAmount());
	}

	@Override
	public boolean isTag(Tag<Material> tag, ItemMeta meta) {
		return _tag == tag
				&& MetaUtils.areMetasEqual(_meta, meta);
	}

	@Override
	public boolean canBeUsed(ItemStack itemStack) {
		return _tag.isTagged(itemStack.getType())
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
