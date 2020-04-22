package com.zuxelus.energycontrol.items.cards;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.crossmod.EnergyStorageData;
import com.zuxelus.energycontrol.utils.CardState;
import com.zuxelus.energycontrol.utils.PanelSetting;
import com.zuxelus.energycontrol.utils.PanelString;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemCardEnergy extends ItemCardBase {
	public ItemCardEnergy() {
		super(ItemCardType.CARD_ENERGY, "card_energy");
	}

	@Override
	public String getUnlocalizedName() {
		return "item.card_energy";
	}

	@Override
	public CardState update(World world, ItemCardReader reader, int range, BlockPos pos) {
		BlockPos target = reader.getTarget();
		if (target == null)
			return CardState.NO_TARGET;
		
		TileEntity entity = world.getTileEntity(target);
		if (entity == null)
			return CardState.NO_TARGET;
			
		EnergyStorageData storage = CrossModLoader.crossIc2.getEnergyStorageData(entity);
		if (storage == null)
			return CardState.NO_TARGET;

		return updateCardValues(reader, storage);
	}

	private CardState updateCardValues(ItemCardReader reader, EnergyStorageData storage) {
		reader.setDouble("storage", storage.values.get(0));
		reader.setDouble("energy", storage.values.get(1));
		return CardState.OK;
	}

	@Override
	public List<PanelString> getStringData(int displaySettings, ItemCardReader card, boolean showLabels) {
		List<PanelString> result = new LinkedList<PanelString>();

		double energy = card.getDouble("energy");
		double storage = card.getDouble("storage");

		if ((displaySettings & 1) > 0)
			result.add(new PanelString("msg.ec.InfoPanelEnergy", energy, showLabels));
		if ((displaySettings & 2) > 0)
			result.add(new PanelString("msg.ec.InfoPanelFree", storage - energy, showLabels));
		if ((displaySettings & 4) > 0)
			result.add(new PanelString("msg.ec.InfoPanelStorage", storage, showLabels));
		if ((displaySettings & 8) > 0)
			result.add(new PanelString("msg.ec.InfoPanelPercentage", storage == 0 ? 100 : ((energy / storage) * 100), showLabels));
		return result;
	}

	@Override
	protected List<PanelSetting> getSettingsList(ItemStack stack) {
		List<PanelSetting> result = new ArrayList<PanelSetting>(4);
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelEnergy"), 1, damage));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelFree"), 2, damage));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelStorage"), 4, damage));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelPercentage"), 8, damage));
		return result;
	}
}
