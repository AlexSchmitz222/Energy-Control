package com.zuxelus.energycontrol.containers;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.items.InventoryPortablePanel;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.cards.ItemCardMain;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ContainerPortablePanel extends ContainerBase<InventoryPortablePanel> {
	private EntityPlayer player;
	
	public ContainerPortablePanel(EntityPlayer player) {
		super(new InventoryPortablePanel(player.getHeldItemMainhand(), "item.portable_panel.name"));
		this.player = player;

		addSlotToContainer(new SlotFilter(te, 0, 174, 17));
		addSlotToContainer(new SlotFilter(te, 1, 174, 35));

		addPlayerInventoryTopSlots(player, 8, 188);
	}

	@Override
	public void detectAndSendChanges() {
		processCard();
		if (player instanceof EntityPlayerMP && ((EntityPlayerMP) player).isChangingQuantityOnly) {
			((EntityPlayerMP) player).isChangingQuantityOnly = false;
			super.detectAndSendChanges();
			((EntityPlayerMP) player).isChangingQuantityOnly = true;
		} else
			super.detectAndSendChanges();
	}

	private void processCard() {
		ItemStack card = te.getStackInSlot(InventoryPortablePanel.SLOT_CARD);
		if (card == null)
			return;

		Item item = card.getItem();
		if (!(item instanceof ItemCardMain))
			return;

		ItemCardReader reader = new ItemCardReader(card);
		ItemCardMain.updateCardNBT(player.worldObj, player.getPosition(), reader, te.getStackInSlot(InventoryPortablePanel.SLOT_UPGRADE_RANGE));
	}
}
