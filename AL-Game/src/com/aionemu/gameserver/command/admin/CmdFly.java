package com.aionemu.gameserver.command.admin;

import com.aionemu.gameserver.command.BaseCommand;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.world.World;


public class CmdFly extends BaseCommand {
	
	public void execute(Player admin, String... params) {
		int emotion = 0;
		float x = 0;
		float y = 0;
		float z = 0;
		byte heading = 0;
		
		if (admin.getFlyState() > 0){
			admin.getFlyController().endFly();
		}else {
			admin.getFlyController().startFly();
			PacketSendUtility.broadcastPacket(admin, new SM_EMOTION(admin, EmotionType.FLY, emotion, x, y, z, heading, getTargetObjectId(admin)), true);
		}
	}
	
	/**
	 * @param player
	 * @return
	 */
	private final int getTargetObjectId(Player player) {
		return player.getTarget() == null ? 0 : player.getTarget().getObjectId();
	}
}
