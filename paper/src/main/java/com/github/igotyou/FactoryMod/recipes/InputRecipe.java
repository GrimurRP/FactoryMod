package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.inputItem.InputItem;
import com.github.igotyou.FactoryMod.inputItem.InputItemMap;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

/**
 * A recipe with any form of item input to run it
 *
 */
public abstract class InputRecipe implements IRecipe {
	private record Item(ItemStack itemStack, boolean useDisplayNameAsName){}

	protected String name;
	protected int productionTime;
	protected InputItemMap input;
	protected int fuel_consumption_intervall = -1;
	protected String identifier;

	public InputRecipe(String identifier, String name, int productionTime, InputItemMap input) {
		this.name = name;
		this.productionTime = productionTime;
		this.input = input;
		this.identifier = identifier;
	}

	/**
	 * Used to get a representation of a recipes input materials, which is
	 * displayed in an item gui to illustrate the recipe and to give additional
	 * information. If null is given instead of an inventory or factory, just
	 * general information should be returned, which doesnt depend on a specific
	 * instance
	 * 
	 * @param i
	 *            Inventory for which the recipe would be run, this is used to
	 *            add lore to the items, which tells how often the recipe could
	 *            be run
	 * @param fccf
	 *            Factory for which the representation is meant. Needed for
	 *            recipe run scaling
	 * @return List of itemstacks which represent the input required to run this
	 *         recipe
	 */
	public abstract List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf);
	
	/**
	 * Used to get a representation of a recipes input materials, which is
	 * displayed in chat or an items lore to illustrate the recipe and to give additional
	 * information. If null is given instead of an inventory or factory, just
	 * general information should be returned, which doesnt depend on a specific
	 * instance
	 * 
	 * @param i
	 *            Inventory for which the recipe would be run, this is used to
	 *           	add a count how often the recipe could be run
	 * @param fccf
	 *            Factory for which the representation is meant. Needed for
	 *            recipe run scaling
	 * @return List of Strings each describing one component needed as input for this recipe
	 */
	public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return formatLore(input);
	}

	/**
	 * Used to get a representation of a recipes output materials, which is
	 * displayed in an item gui to illustrate the recipe and to give additional
	 * information. If null is given instead of an inventory or factory, just
	 * general information should be returned, which doesnt depend on a specific
	 * instance
	 * 
	 * @param i
	 *            Inventory for which the recipe would be run, this is used to
	 *            add lore to the items, which tells how often the recipe could
	 *            be run
	 * @param fccf
	 *            Factory for which the representation is meant. Needed for
	 *            recipe run scaling
	 * @return List of itemstacks which represent the output returned when
	 *         running this recipe
	 */
	public abstract List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf);
	
	/**
	 * Used to get a representation of a recipes output, which is
	 * displayed in chat or an items lore to illustrate the recipe and to give additional
	 * information. If null is given instead of an inventory or factory, just
	 * general information should be returned, which doesnt depend on a specific
	 * instance
	 * 
	 * @param i
	 *            Inventory for which the recipe would be run, this is used to
	 *           	add a count how often the recipe could be run
	 * @param fccf
	 *            Factory for which the representation is meant. Needed for
	 *            recipe run scaling
	 * @return List of Strings each describing one component produced as output of this recipe
	 */
	public abstract List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf);

	@Override
	public String getName() {
		return name;
	}
	
	public int getTotalFuelConsumed() {
		if (fuel_consumption_intervall == 0) {
			return 0;
		}
		return productionTime / fuel_consumption_intervall;
	}

	public int getFuelConsumptionIntervall() {
		return fuel_consumption_intervall;
	}

	public void setFuelConsumptionIntervall(int intervall) {
		this.fuel_consumption_intervall = intervall;
	}

	@Override
	public int getProductionTime() {
		return productionTime;
	}

	public InputItemMap getInput() {
		return input;
	}

	@Override
	public boolean enoughMaterialAvailable(Inventory inputInv) {
		return input.isContainedIn(inputInv);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @return A single itemstack which is used to represent this recipe as a
	 *         whole in an item gui
	 */
	public ItemStack getRecipeRepresentation() {
		ItemStack res = getRecipeRepresentationItemStack();
		ItemMeta im = res.getItemMeta();
		im.setDisplayName(ChatColor.DARK_GREEN + getName());
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GOLD + "Input:");
		for(String s : getTextualInputRepresentation(null, null)) {
			lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + s);
		}
		lore.add("");
		lore.add(ChatColor.GOLD + "Output:");
		for(String s : getTextualOutputRepresentation(null, null)) {
			lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + s);
		}
		lore.add("");
		lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + TextUtil
				.formatDuration(getProductionTime() * 50, TimeUnit.MILLISECONDS));
		im.setLore(lore);
		res.setItemMeta(im);
		return res;
	}
	
	public abstract ItemStack getRecipeRepresentationItemStack();

	/**
	 * Creates a list of ItemStack for a GUI representation. This list contains
	 * all the itemstacks contained in the itemstack representation of the input
	 * map and adds to each of the stacks how many runs could be made with the
	 * material available in the chest
	 * 
	 * @param i
	 *            Inventory to calculate the possible runs for
	 * @return ItemStacks containing the additional information, ready for the
	 *         GUI
	 */
	protected List<ItemStack> createLoredStacksForInfo(Inventory i) {
		return input.getItemStackRepresentation(i, true);
	}

	protected void logBeforeRecipeRun(Inventory i, Factory f) {
		LoggingUtils.logInventory(i, "Before executing recipe " + name + " for " + f.getLogData());
	}

	protected void logAfterRecipeRun(Inventory i, Factory f) {
		LoggingUtils.logInventory(i, "After executing recipe " + name + " for " + f.getLogData());
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	protected List<String> formatLore(InputItemMap ingredients) {
		var items = new ArrayList<Item>();
		for (InputItem inputItem : ingredients.getItems()) {
			items.add(new Item(inputItem.getGUIItemStack(), inputItem.useDisplayNameAsName()));
		}
		return formatLore(items);
	}

	protected List<String> formatLore(ItemMap ingredients) {
		var items = new ArrayList<Item>();
		for(Entry<ItemStack, Integer> entry : ingredients.getEntrySet()) {
			items.add(new Item(entry.getKey().asQuantity(entry.getValue()), false));
		}
		return formatLore(items);
	}

	private List<String> formatLore(List<Item> items) {
		List<String> result = new ArrayList<>();
		for(Item item : items) {
			ItemStack itemStack = item.itemStack;
			int amount = itemStack.getAmount();
			if (amount <= 0) {
				continue;
			}

			if (!itemStack.hasItemMeta()) {
				result.add(amount + " " + ItemUtils.getItemName(itemStack));
			} else if (item.useDisplayNameAsName && itemStack.getItemMeta().hasDisplayName()) {
				String name = StringUtils.abbreviate(itemStack.getItemMeta().getDisplayName(), 20);
				String lore = String.format("%s %s%s", amount, ChatColor.DARK_AQUA, name);
				result.add(lore);
			} else {
				String lore = String.format("%s %s%s", amount, ChatColor.ITALIC, ItemUtils.getItemName(itemStack));
				if (itemStack.getItemMeta().hasDisplayName()) {
					lore += String.format("%s [%s]", ChatColor.DARK_AQUA, StringUtils.abbreviate(itemStack.getItemMeta().getDisplayName(), 20));
				}
				result.add(lore);
			}
		}
		return result;
	}
}
