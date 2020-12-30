package com.zuxelus.energycontrol.gui.controls;

import org.lwjgl.opengl.GL11;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.tileentities.TileEntityInfoPanel;
import com.zuxelus.zlib.network.NetworkHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiInfoPanelShowLabels extends GuiButton {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(
			EnergyControl.MODID + ":textures/gui/gui_info_panel.png");

	private TileEntityInfoPanel panel;
	private boolean checked;

	public GuiInfoPanelShowLabels(int id, int x, int y, TileEntityInfoPanel panel) {
		super(id, x, y, 0, 0, "");
		height = 9;
		width = 18;
		this.panel = panel;
		checked = panel.getShowLabels();
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (!visible)
			return;

		mc.getTextureManager().bindTexture(TEXTURE_LOCATION);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int delta = checked ? 12 : 21;
		drawTexturedModalRect(xPosition, yPosition + 1, 176, delta, 18, 9);
	}

	@Override
	public int getHoverState(boolean flag) {
		return 0;
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int i, int j) {
		if (super.mousePressed(minecraft, i, j)) {
			checked = !checked;
			if (panel.getWorldObj().isRemote && panel.getShowLabels() != checked) {
				NetworkHelper.updateSeverTileEntity(panel.xCoord, panel.yCoord, panel.zCoord, 3, checked ? 1 : 0);
				panel.setShowLabels(checked);
			}
			return true;
		}
		return false;
	}
}
