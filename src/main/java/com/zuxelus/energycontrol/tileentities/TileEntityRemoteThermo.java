package com.zuxelus.energycontrol.tileentities;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.cards.ItemCardMain;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.items.cards.ItemCardType;
import com.zuxelus.energycontrol.utils.ReactorHelper;
import com.zuxelus.zlib.containers.slots.ISlotItemFilter;
import com.zuxelus.zlib.tileentities.IBlockHorizontal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.reactor.IReactor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRemoteThermo extends TileEntityThermo implements IEnergySink, ISlotItemFilter, IBlockHorizontal {
	public static final int SLOT_CHARGER = 0;
	public static final int SLOT_CARD = 1;
	private static final double BASE_PACKET_SIZE = 32.0D;
	private static final int BASE_STORAGE = 600;
	private static final int STORAGE_PER_UPGRADE = 10000;
	private static final int LOCATION_RANGE = 8;

	private int deltaX;
	private int deltaY;
	private int deltaZ;
	private double prevMaxStorage;
	private double maxStorage;
	private double prevMaxPacketSize;
	private double maxPacketSize;
	private int prevTier;
	private int tier;
	private int heat;

	private double energy;
	private boolean addedToEnet;

	public TileEntityRemoteThermo() {
		super();
		customName = "tile.remote_thermo.name";
		addedToEnet = false;
		maxStorage = BASE_STORAGE;
		maxPacketSize = BASE_PACKET_SIZE;
		tier = 1;
		deltaX = 0;
		deltaY = 0;
		deltaZ = 0;
		energy = 0;
		heat = 0;
	}

	public int getHeat() {
		return heat;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double value) {
		energy = value;
	}

	public void setTier(int value) {
		tier = value;
		if (!worldObj.isRemote && tier != prevTier)
			notifyBlockUpdate();
		prevTier = tier;
	}

	public void setMaxPacketSize(double value) {
		maxPacketSize = value;
		if (!worldObj.isRemote && maxPacketSize != prevMaxPacketSize)
			notifyBlockUpdate();
		prevMaxPacketSize = maxPacketSize;
	}

	public double getMaxStorage() {
		return maxStorage;
	}

	public void setMaxStorage(double value) {
		maxStorage = value;
		if (!worldObj.isRemote && maxStorage != prevMaxStorage)
			notifyBlockUpdate();
		prevMaxStorage = maxStorage;
	}

	@Override
	protected void readProperties(NBTTagCompound tag) {
		super.readProperties(tag);
		if (tag.hasKey("heat"))
			heat = tag.getInteger("heat");
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		energy = tag.getDouble("energy");
		markDirty();
	}

	@Override
	protected NBTTagCompound writeProperties(NBTTagCompound tag) {
		tag = super.writeProperties(tag);
		tag.setInteger("heat", heat);
		return tag;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setDouble("energy", energy);
	}

	public void onLoad() {
		if (!addedToEnet && !worldObj.isRemote && Info.isIc2Available()) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			addedToEnet = true;
		}
	}

	@Override
	public void invalidate() {
		onChunkUnload();
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		if (addedToEnet && !worldObj.isRemote && Info.isIc2Available()) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			addedToEnet = false;
		}
	}

	@Override
	protected void checkStatus() {
		markDirty();

		int newStatus;
		int newHeat = 0;
		if (energy >= EnergyControl.config.remoteThermalMonitorEnergyConsumption) {
			IReactor reactor = ReactorHelper.getReactorAt(worldObj, xCoord + deltaX, yCoord + deltaY, zCoord + deltaZ);
			if (reactor == null) {
				if (getStackInSlot(SLOT_CARD) != null) {
					ChunkCoordinates target = new ItemCardReader(getStackInSlot(SLOT_CARD)).getTarget();
					if (target != null)
						reactor = ReactorHelper.getReactor3x3(worldObj, target.posX, target.posY, target.posZ);
				}
			}

			if (reactor != null) {
				if (tickRate == -1) {
					tickRate = reactor.getTickRate() / 2;
					if (tickRate == 0)
						tickRate = 1;
					updateTicker = tickRate;
				}
				newHeat = reactor.getHeat();
				if (newHeat > getHeatLevel())
					newStatus = 1;
				else
					newStatus = 0;
			} else {
				newStatus = -1;
				if (getStackInSlot(SLOT_CARD) != null) {
					ChunkCoordinates target = new ItemCardReader(getStackInSlot(SLOT_CARD)).getTarget();
					if (target != null) {
						newHeat = ReactorHelper.getReactorHeat(worldObj, target.posX, target.posY, target.posZ);
						newStatus = newHeat == -1 ? -1 : newHeat >= getHeatLevel() ? 1 : 0;
						if (newHeat == -1)
							newHeat = 0;
					}
				}
			}
		} else
			newStatus = -2;

		if (newStatus != status || newHeat != heat) {
			status = newStatus;
			heat = newHeat;
			notifyBlockUpdate();
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!addedToEnet)
			onLoad();
		if (worldObj.isRemote)
			return;
		// If is server
		int consumption = EnergyControl.config.remoteThermalMonitorEnergyConsumption;
		ItemStack stack = getStackInSlot(SLOT_CHARGER);
		if (stack != null && energy < maxStorage && stack.getItem() instanceof IElectricItem) {
			IElectricItem ielectricitem = (IElectricItem) stack.getItem();
			if (ielectricitem.canProvideEnergy(stack))
				energy += ElectricItem.manager.discharge(stack, maxStorage - energy, tier, false, false, false);
		}

		if (energy >= consumption) {
			energy -= consumption;
		} else 
			energy = 0;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		int upgradeCountTransormer = 0;
		int upgradeCountStorage = 0;
		int upgradeCountRange = 0;
		for (int i = 2; i < 5; i++) {
			ItemStack itemStack = getStackInSlot(i);

			if (itemStack == null)
				continue;

			if (itemStack.isItemEqual(CrossModLoader.ic2.getItemStack("transformer"))) {
				upgradeCountTransormer += itemStack.stackSize;
			} else if (itemStack.isItemEqual(CrossModLoader.ic2.getItemStack("energy_storage"))) {
				upgradeCountStorage += itemStack.stackSize;
			} else if (itemStack.getItem() instanceof ItemUpgrade && itemStack.getItemDamage() == ItemUpgrade.DAMAGE_RANGE)
				upgradeCountRange += itemStack.stackSize;
		}
		if (getStackInSlot(SLOT_CARD) != null) {
			ChunkCoordinates target = new ItemCardReader(getStackInSlot(SLOT_CARD)).getTarget();
			if (target != null) {
				deltaX = target.posX - xCoord;
				deltaY = target.posY - yCoord;
				deltaZ = target.posZ - zCoord;
				if (upgradeCountRange > 7)
					upgradeCountRange = 7;
				int range = LOCATION_RANGE * (int) Math.pow(2, upgradeCountRange);
				if (Math.abs(deltaX) > range || Math.abs(deltaY) > range || Math.abs(deltaZ) > range)
					deltaX = deltaY = deltaZ = 0;
			} else {
				deltaX = 0;
				deltaY = 0;
				deltaZ = 0;
			}
		} else {
			deltaX = 0;
			deltaY = 0;
			deltaZ = 0;
			status = -2;
		}
		upgradeCountTransormer = Math.min(upgradeCountTransormer, 4);
		if (worldObj != null && !worldObj.isRemote) {
			tier = upgradeCountTransormer + 1;
			setTier(tier);
			maxPacketSize = BASE_PACKET_SIZE * Math.pow(4D, upgradeCountTransormer);
			setMaxPacketSize(maxPacketSize);
			maxStorage = BASE_STORAGE + STORAGE_PER_UPGRADE * upgradeCountStorage;
			setMaxStorage(maxStorage);
			if (energy > maxStorage)
				energy = maxStorage;
		}
	}

	// Inventory
	@Override
	public int getSizeInventory() {
		return 5;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return isItemValid(index, stack);
	}

	@Override
	public boolean isItemValid(int slotIndex, ItemStack stack) {
		if (stack == null)
			return false;
		switch (slotIndex) {
		case SLOT_CHARGER:
			if (stack.getItem() instanceof IElectricItem) {
				IElectricItem item = (IElectricItem) stack.getItem();
				if (item.canProvideEnergy(stack) && item.getTier(stack) <= tier)
					return true;
			}
			return false;
		case SLOT_CARD:
			return stack.getItem() instanceof ItemCardMain && (stack.getItemDamage() == ItemCardType.CARD_REACTOR
					|| stack.getItemDamage() == ItemCardType.CARD_REACTOR5X5
					|| stack.getItemDamage() == ItemCardType.CARD_BIG_REACTORS);
		default:
			return stack.isItemEqual(CrossModLoader.ic2.getItemStack("transformer"))
					|| stack.isItemEqual(CrossModLoader.ic2.getItemStack("energy_storage"))
					|| (stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() == ItemUpgrade.DAMAGE_RANGE);
		}
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
		return true;
	}

	@Override
	public double getDemandedEnergy() {
		return Math.max(0, maxStorage - energy);
	}

	@Override
	public int getSinkTier() {
		return tier;
	}

	@Override
	public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
		energy += amount;
		double left = 0.0;

		if (energy > maxStorage) {
			left = energy - maxStorage;
			energy = maxStorage;
		}
		return left;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldBlock != newBlock;
	}
}
