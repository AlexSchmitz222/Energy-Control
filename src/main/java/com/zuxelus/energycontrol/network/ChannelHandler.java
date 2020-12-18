package com.zuxelus.energycontrol.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ChannelHandler {
	public static SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel("EnCtrl");

	public static void init() {
		network.registerMessage(PacketAlarm.class, PacketAlarm.class, 1, Side.CLIENT); //server to client
		network.registerMessage(PacketCard.class, PacketCard.class, 2, Side.CLIENT);
		network.registerMessage(PacketChat.class, PacketChat.class, 3, Side.CLIENT);
		network.registerMessage(PacketTileEntity.class, PacketTileEntity.class, 5, Side.CLIENT);
		network.registerMessage(PacketTileEntity.class, PacketTileEntity.class, 6, Side.SERVER);
		network.registerMessage(PacketClientSensor.class, PacketClientSensor.class, 7, Side.SERVER);
		network.registerMessage(PacketOreHelper.class, PacketOreHelper.class, 8, Side.CLIENT);
	}
}