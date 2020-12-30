package com.zuxelus.energycontrol.gui.controls;

import org.lwjgl.opengl.GL11;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.tileentities.TileEntityInfoPanel;
import com.zuxelus.zlib.network.NetworkHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiInfoPanelCheckBox extends GuiButton {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(
			EnergyControl.MODID + ":textures/gui/gui_info_panel.png");

	private TileEntityInfoPanel panel;
	private boolean checked;
	private PanelSetting setting;
	private int slot;

	public GuiInfoPanelCheckBox(int id, int x, int y, PanelSetting setting, TileEntityInfoPanel panel, int slot, FontRenderer renderer) {
		super(id, x, y, 0, 0, setting.title);
		this.setting = setting;
		this.slot = slot;
		height = renderer.FONT_HEIGHT + 1;
		width = renderer.getStringWidth(setting.title) + 8;
		this.panel = panel;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (!visible)
			return;
		checked = (panel.getDisplaySettingsForCardInSlot(slot) & setting.displayBit) > 0;
		mc.getTextureManager().bindTexture(TEXTURE_LOCATION);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int delta = checked ? 6 : 0;
		drawTexturedModalRect(xPosition, yPosition + 1, 176, delta, 6, 6);
		mc.fontRenderer.drawString(displayString, xPosition + 8, yPosition, 0x404040);
	}

	@Override
	public int getHoverState(boolean flag) {
		return 0;
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
		if (super.mousePressed(minecraft, mouseX, mouseY)) {
			checked = !checked;
			int value;
			if (checked)
				value = panel.getDisplaySettingsForCardInSlot(slot) | setting.displayBit;
			else
				value = panel.getDisplaySettingsForCardInSlot(slot) & (~setting.displayBit);
			UpdateServerSettings(value);
			panel.setDisplaySettings(slot, value);
			return true;
		}
		return false;
	}
	
	private void UpdateServerSettings(int value) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("type", 1);
		tag.setInteger("slot", slot);
		tag.setInteger("value", value);
		NetworkHelper.updateSeverTileEntity(panel.xCoord, panel.yCoord, panel.zCoord, tag);
	}
}
