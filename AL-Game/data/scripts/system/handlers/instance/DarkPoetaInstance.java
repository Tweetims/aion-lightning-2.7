/*
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package instance;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.controllers.SummonController.UnsummonType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.DarkPoetaReward;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.portal.EntryPoint;
import com.aionemu.gameserver.model.templates.portal.PortalTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Hilgert, xTz ,Tiger
 */
@InstanceID(300040000)
public class DarkPoetaInstance extends GeneralInstanceHandler {

	private Map<Integer,StaticDoor> doors;
	private final AtomicInteger specNpcKilled = new AtomicInteger();
	private Future<?> instanceTimer;
	private long startTime;
	private DarkPoetaReward instanceReward;
	private boolean isInstanceDestroyed;

	@Override
	public void onDie(Npc npc) {
		int npcId = npc.getNpcId();
		switch(npcId) {
			case 700443:
			case 700444:
			case 700442:
			case 700446:
			case 700447:
			case 700445:
			case 700440:
			case 700441:
			case 700439:
				toScheduleMarbataController(npcId);
				return;
		}

		int points = calculatePointsReward(npc);
		if (instanceReward.getInstanceScoreType().isStartProgress()) {
			instanceReward.addNpcKill();
			instanceReward.addPoints(points);
			sendPacket(npc.getObjectTemplate().getNameId(),  points);
		}
		switch (npcId) {
			case 214896:
			case 214895:
			case 214897:
				int killedCount = specNpcKilled.incrementAndGet();
				if (killedCount == 3) {
					spawn(214904, 275.34537f, 323.02072f, 130.9302f, (byte) 52);
				}
				break;
			case 214904:
				instanceReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
				instanceReward.setRank(checkRank(instanceReward.getPoints()));
				sendPacket(npc.getObjectTemplate().getNameId(),  points);
				break;
		}
	}

	private int getTime() {
		long result = System.currentTimeMillis() - startTime;
		if (result < 60000) {
			return (int) (60000 - result);
		}
		else if (result < 14460000) {
			return (int) (14400000 - (result - 60000));
		}
		return 0;
	}

	private void sendPacket(final int nameId, final int point) {
		instance.doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (nameId != 0) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), point));
				}
				PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(getTime(), instanceReward));
			}
		});
	}

	private int checkRank(int totalPoints) {
		int timeRemain = getTime();
		int rank = 0;
		if (timeRemain > 7200000 && totalPoints >= 17817) {				
			spawn(215280, 1176f, 1227f, 145f, (byte) 14);
			rank = 1;
		} else if (timeRemain > 5400000 && totalPoints >= 15219) {
			spawn(215281, 1176f, 1227f, 145f, (byte) 14);
			rank = 2;
		} else if (timeRemain > 3600000 && totalPoints > 10913) {
			spawn(215282, 1176f, 1227f, 145f, (byte) 14);
			rank = 3;
		} else if (timeRemain > 1800000 && totalPoints > 6656) {
			spawn(215283, 1176f, 1227f, 145f, (byte) 14);
			rank = 4;
		} else if (timeRemain > 1) {
			spawn(215284, 1176f, 1227f, 145f, (byte) 14);
			rank = 5;
		} else {
			rank = 8;
		}
		spawn(730211, 298.24423f, 316.21954f, 133.29759f, (byte) 56);

		return rank;
	}

	private int calculatePointsReward(Npc npc) {
		int pointsReward = 0;

		// Usually calculated by npcRank
		switch (npc.getObjectTemplate().getRating()) {
			case HERO:
				switch (npc.getObjectTemplate().getHpGauge()) {
					case 21:
						pointsReward = 786;
						break;

					default:
						pointsReward = 300;
				}
				break;
			default:
				if (npc.getObjectTemplate().getRace() == null) {
					break;
				}

				switch (npc.getObjectTemplate().getRace().getRaceId()) {
					case 22: // UNDEAD
						pointsReward = 12;
						break;
					case 9: // BROWNIE
						pointsReward = 18;
						break;
					case 6: // LIZARDMAN
						pointsReward = 24;
						break;
					case 8: // NAGA
					case 18: // DRAGON
					case 24: // MAGICALnpc
						pointsReward = 30;
						break;
					default:
						pointsReward = 11;
						break;
				}
		}

		// Special npcs
		switch (npc.getObjectTemplate().getTemplateId()) {
			// Drana
			case 700520:
				pointsReward = 48;
				break;
			// Walls
			case 700518:
			case 700558:
				pointsReward = 156;
				break;
			// Mutated Fungie
			case 214885:
				pointsReward = 21;
				break;
			// Named1
			case 214841:
			case 215431:
				pointsReward = 162;
				break;
			// Named2
			case 214842:
			case 215429:
			case 215430:
			case 215432:
				pointsReward = 186;
				break;
			// Named3
			case 214871:
			case 215386:
			case 215428:
				pointsReward = 204;
				break;
			// Marabata
			case 214849:
			case 214850:
			case 214851:
				pointsReward = 318;
				break;
			// Generators
			case 214895:
			case 214896:
			case 214897:
				pointsReward = 372;
				break;
			// Atmach
			case 214843:
				pointsReward = 456;
				break;
			// Boss
			case 214864:
			case 214880:
			case 214894:
			case 215387:
			case 215388:
			case 215389:
				pointsReward = 786;
				break;
			case 214904:
				pointsReward = 954;
				break;
		}
		PlayerGroup group = instance.getRegisteredGroup();
		if (group != null) {
			if (group.getLeaderObject().getAbyssRank().getRank().getId() >= 10)
				pointsReward = Math.round(pointsReward * 1.1f);
		}
		return pointsReward;
	}

	@Override
	public void onEnterInstance(final Player player) {
		if (instanceTimer == null) {
			startTime = System.currentTimeMillis();
			instanceTimer =	ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
					sendPacket(0, 0);
					openDoors();
				}

			}, 61000);
		}
		sendPacket(0, 0);
	}

	@Override
	public void onInstanceDestroy() {
		if (instanceTimer != null ) {
			instanceTimer.cancel(false);
		}
		isInstanceDestroyed = true;
		doors.clear();
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		instanceReward = new DarkPoetaReward(mapId, instanceId);
		instanceReward.setInstanceScoreType(InstanceScoreType.PREPARING);
		doors = instance.getDoors();
	}

	private void openDoors() {
		for(StaticDoor door: doors.values())
		if (door != null)
			door.setOpen(true);
	}

	@Override
	public void onGather(Player player) {
		instanceReward.addGather();
		sendPacket(0, instanceId);
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		Summon summon = player.getSummon();
		if (summon != null) {
			summon.getController().release(UnsummonType.UNSPECIFIED);
		}

		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, lastAttacker == null ? 0
				: lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}
	
	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
		TeleportService.teleportTo(player, mapId, instanceId, 1224.1665f, 418.4088f, 140.18988f, 0, true);
		return true;
	}

	private void toScheduleMarbataController(final int npcId) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				Npc boss = null;
				switch (npcId) {
					case 700443:
					case 700444:
					case 700442:
						boss = getNpc(214850);
						break;
					case 700446:
					case 700447:
					case 700445:
						boss = getNpc(214851);
						break;
					case 700440:
					case 700441:
					case 700439:
						boss = getNpc(214849);
				}
				if (!isInstanceDestroyed && boss != null && !boss.getLifeStats().isAlreadyDead()) {
					switch (npcId) {
						case 700443:
							spawn(npcId, 676.257019f, 319.649994f, 99.375000f, (byte) 4);
							break;
						case 700444:
							spawn(npcId, 655.851013f, 292.710999f, 99.375000f, (byte) 90);
							break;
						case 700442:
							spawn(npcId, 636.117981f, 325.536987f, 99.375000f, (byte) 49);
							break;
						case 700446:
							spawn(npcId, 598.706000f, 345.978000f, 99.375000f, (byte) 98);
							break;
						case 700447:
							spawn(npcId, 567.775024f, 366.207001f, 99.375000f, (byte) 59);
							break;
						case 700445:
							spawn(npcId, 605.625000f, 380.479004f, 99.375000f, (byte) 14);
							break;
						case 700440:
							spawn(npcId, 681.851013f, 408.625000f, 100.472000f, (byte) 13);
							break;
						case 700441:
							spawn(npcId, 646.549988f, 406.088013f, 99.375000f, (byte) 49);
							break;
						case 700439:
							spawn(npcId, 665.37400f, 372.75100f, 99.375000f, (byte) 90);
							break;
					}
				}
			}

		}, 28000);
	}

	protected Npc getNpc(int npcId) {
		if (!isInstanceDestroyed) {
			return instance.getNpc(npcId);
		}
		return null;
	}
	
	@Override
	public void onLeaveInstance(Player player) {
		Storage storage = player.getInventory();
		switch (player.getRace()) {
			case ELYOS:
				storage.decreaseByItemId(185000041, 1);
				break;
			case ASMODIANS:
				storage.decreaseByItemId(185000041, 1);
				break;
		default:
			break;
		}
	}

	@Override
	public void onExitInstance(Player player) {
		if (instanceReward.getInstanceScoreType().isEndProgress()) {
			PortalTemplate portal = DataManager.PORTAL_DATA.getInstancePortalTemplate(mapId, player.getRace());
			EntryPoint entryPoint = TeleportService.getEntryPointByRace(portal, player.getRace());
			TeleportService.teleportTo(player, entryPoint.getMapId(), 1, entryPoint.getX(), entryPoint.getY(),
					entryPoint.getZ(), 3000, true);
		}
	}
}