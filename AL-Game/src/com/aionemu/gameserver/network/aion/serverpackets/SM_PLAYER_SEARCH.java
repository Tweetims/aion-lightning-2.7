/*
 * This file is part of aion-emu <aion-emu.com>.
 *
 *  aion-emu is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-emu is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Sent to fill the search panel of a players social window<br />
 * I.E.: In response to a <tt>CM_PLAYER_SEARCH</tt>
 * 
 * @author Ben
 */
public class SM_PLAYER_SEARCH extends AionServerPacket {

	private static final Logger log = LoggerFactory.getLogger(SM_PLAYER_SEARCH.class);

	private List<Player> players;
	private int region;

	/**
	 * Constructs a new packet that will send these players
	 * 
	 * @param players
	 *          List of players to show
	 * @param region
	 *          of search - should be passed as parameter to prevent null in player.getActiveRegion()
	 */
	public SM_PLAYER_SEARCH(List<Player> players, int region) {
		this.players = new ArrayList<Player>(players);
		this.region = region;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeH(players.size());
		for (Player player : players) {
			if (player.getActiveRegion() == null) {
				log.warn("CHECKPOINT: null active region for " + player.getObjectId() + "-" + player.getX() + "-"
					+ player.getY() + "-" + player.getZ());
			}
			writeD(player.getActiveRegion() == null ? region : player.getActiveRegion().getMapId());
			writeF(player.getPosition().getX());
			writeF(player.getPosition().getY());
			writeF(player.getPosition().getZ());
			writeC(player.getPlayerClass().getClassId());
			writeC(player.getGender().getGenderId());
			writeC(player.getLevel());
			// TODO: When groups finish, send 3 here if in group
			writeC(player.isLookingForGroup() ? 0x02 : 0x00); // Status. 2 = LFG, 3 = In group, others = solo
			writeS(player.getName(), 52);

		}
	}

}
