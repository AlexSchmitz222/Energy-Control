package com.zuxelus.energycontrol.gui;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.ICardGui;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.gui.controls.CompactButton;
import com.zuxelus.energycontrol.gui.controls.GuiInfoPanelCheckBox;
import com.zuxelus.energycontrol.gui.controls.GuiInfoPanelShowLabels;
import com.zuxelus.energycontrol.items.cards.ItemCardMain;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.items.cards.ItemCardSettingsReader;
import com.zuxelus.energycontrol.items.cards.ItemCardType;
import com.zuxelus.energycontrol.tileentities.TileEntityInfoPanel;
import com.zuxelus.zlib.containers.ContainerBase;
import com.zuxelus.zlib.network.NetworkHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiInfoPanel extends GuiContainer {
	private static final ResourceLocation TEXTURE = new ResourceLocation(
			EnergyControl.MODID + ":textures/gui/gui_info_panel.png");

	protected String name;
	private TileEntityInfoPanel panel;
	public ItemStack prevCard;
	protected GuiTextField textboxTitle;
	protected byte activeTab;
	protected boolean modified;
	public boolean isColored;

	public GuiInfoPanel(ContainerBase container) {
		super(container);
		ySize = 201;
		panel = (TileEntityInfoPanel)container.te;
		name = I18n.format("tile.info_panel.name");
		modified = false;
		// inverted value on start to force initControls
		isColored = !this.panel.getColored();
		activeTab = 0;
	}

	@Override
	protected void drawHoveringText(List list, int par2, int par3, FontRenderer font) {
		if (list.isEmpty())
			return;
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		int k = 0;
		Iterator iterator = list.iterator();

		while (iterator.hasNext()) {
			String s = (String) iterator.next();
			int l = font.getStringWidth(s);
			if (l > k)
				k = l;
		}

		int i1 = par2 + 12;
		int j1 = par3 - 12;
		int k1 = 8;

		if (list.size() > 1)
			k1 += 2 + (list.size() - 1) * 10;

		if (i1 + k > this.width)
			i1 -= 28 + k;

		if (j1 + k1 + 6 > this.height)
			j1 = this.height - k1 - 6;

		this.zLevel = 300.0F;
		itemRender.zLevel = 300.0F;
		int l1 = -267386864;
		drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
		drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
		drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
		drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
		drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
		int i2 = 1347420415;
		int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
		drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
		drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
		drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
		drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

		for (int k2 = 0; k2 < list.size(); ++k2) {
			String s1 = (String) list.get(k2);
			font.drawStringWithShadow(s1, i1, j1, -1);

			if (k2 == 0)
				j1 += 2;

			j1 += 10;
		}

		this.zLevel = 0.0F;
		itemRender.zLevel = 0.0F;
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	}

	protected void initControls() {
		ItemStack stack = panel.getCards().get(0);
		if (stack == null && prevCard == null && panel.getColored() == isColored)
			return;

		buttonList.clear();
		prevCard = stack;
		isColored = panel.getColored();
		buttonList.add(new GuiInfoPanelShowLabels(0, guiLeft + xSize - 25, guiTop + 42, panel));
		int delta = 0;
		if (isColored) {
			buttonList.add(new CompactButton(112, guiLeft + xSize - 25, guiTop + 55, 18, 12, "T"));
			delta = 15;
		}
		if (stack != null && stack.getItem() instanceof ItemCardMain) {
			int slot = panel.getCardSlot(stack);
			if (stack.getItemDamage() == ItemCardType.CARD_TEXT)
				buttonList.add(new CompactButton(111, guiLeft + xSize - 25, guiTop + 55 + delta, 18, 12, "..."));
			List<PanelSetting> settingsList = ItemCardMain.getSettingsList(stack);

			int hy = fontRendererObj.FONT_HEIGHT + 1;
			int y = 1;
			int x = guiLeft + 24;
			if (settingsList != null)
				for (PanelSetting panelSetting : settingsList) {
					buttonList.add(new GuiInfoPanelCheckBox(0, x + 4, guiTop + 28 + hy * y, panelSetting, panel, slot, fontRendererObj));
					y++;
				}
			if (!modified) {
				textboxTitle = new GuiTextField(fontRendererObj, 7, 16, 162, 18);
				textboxTitle.setFocused(true);
				textboxTitle.setText(new ItemCardReader(stack).getTitle());
			}
		} else {
			modified = false;
			textboxTitle = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		initControls();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(TEXTURE);
		int left = (width - xSize) / 2;
		int top = (height - ySize) / 2;
		drawTexturedModalRect(left, top, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(name, (xSize - fontRendererObj.getStringWidth(name)) / 2, 6, 0x404040);
		if (textboxTitle != null)
			textboxTitle.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (textboxTitle != null) {
			boolean focused = textboxTitle.isFocused();
			textboxTitle.mouseClicked(mouseX - guiLeft, mouseY - guiTop, mouseButton);
			if (textboxTitle.isFocused() != focused)
				updateTitle();
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (textboxTitle != null)
			textboxTitle.updateCursorCounter();
		initControls();
	}

	protected void updateTitle() {
		if (textboxTitle == null)
			return;
		if (panel.getWorldObj().isRemote) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("type", 4);
			tag.setInteger("slot", activeTab);
			tag.setString("title", textboxTitle.getText());
			NetworkHelper.updateSeverTileEntity(panel.xCoord, panel.yCoord, panel.zCoord, tag);
			ItemStack card = panel.getStackInSlot(activeTab);
			if (card != null && card.getItem() instanceof ItemCardMain)
				new ItemCardReader(card).setTitle(textboxTitle.getText());
		}
	}

	@Override
	public void onGuiClosed() {
		updateTitle();
		super.onGuiClosed();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 112) { // color upgrade
			GuiScreen colorGui = new GuiScreenColor(this, panel);
			mc.displayGuiScreen(colorGui);
		} else if (button.id == 111) {
			ItemStack card = panel.getCards().get(0);
			if (card == null)
				return;
			if (card.getItem() instanceof ItemCardMain && card.getItemDamage() == ItemCardType.CARD_TEXT) {
				ItemCardReader reader = new ItemCardReader(card);
				ICardGui guiObject = ItemCardMain.getSettingsScreen(reader);
				if (!(guiObject instanceof GuiScreen)) {
					EnergyControl.logger.warn("Invalid card, getSettingsScreen method should return GuiScreen object");
					return;
				}
				GuiScreen gui = (GuiScreen) guiObject;
				ItemCardSettingsReader wrapper = new ItemCardSettingsReader(card, panel, this, (byte)0);
				((ICardGui) gui).setCardSettingsHelper(wrapper);
				mc.displayGuiScreen(gui);
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (textboxTitle != null && textboxTitle.isFocused())
			if (keyCode == 1)
				mc.thePlayer.closeScreen();
			else if (typedChar == 13)
				updateTitle();
			else {
				modified = true;
				textboxTitle.textboxKeyTyped(typedChar, keyCode);
			}
		else
			super.keyTyped(typedChar, keyCode);
	}
}
