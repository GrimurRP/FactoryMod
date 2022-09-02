package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.util.List;

import com.github.igotyou.FactoryMod.inputItem.InputItemMap;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DummyParsingRecipe extends InputRecipe {

	public DummyParsingRecipe(String identifier, String name, int productionTime, InputItemMap input) {
		super(identifier, name, productionTime, input);
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		return true;
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return null;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return null;
	}

	@Override
	public String getTypeIdentifier() {
		return "DUMMY";
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return null;
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return null;
	}

}
