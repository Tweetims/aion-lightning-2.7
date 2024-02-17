/*
 * This file is part of aion-unique <aion-unique.org>.
 *
 *  aion-unique is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-unique is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * @author ATracer
 */
public class CM_SUMMON_ATTACK extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_ATTACK.class);

	private int summonObjId;
	private int targetObjId;
	@SuppressWarnings("unused")
	private int unk1;

	private int time;
	@SuppressWarnings("unused")
	private int unk3;

	public CM_SUMMON_ATTACK(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		summonObjId = readD();
		targetObjId = readD();
		unk1 = readC();
		time = readH();
		unk3 = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		log.warn("attack from: " + summonObjId);
		Summon[] summons = player.getSummons();
		if (summons == null || summons.length == 0) {
			log.warn("summon attack without active summon on "+player.getName()+".");
			return;
		}
		
		for (Summon summon: summons) {
			if (summon.getObjectId() != summonObjId)
				return;
			
			VisibleObject obj = summon.getKnownList().getObject(targetObjId);
			if(obj != null && obj instanceof Creature) {
				summon.getController().attackTarget((Creature)obj, time);
			}
			else
				log.warn("summon attack on a wrong target on "+player.getName());
		}
	}
}
