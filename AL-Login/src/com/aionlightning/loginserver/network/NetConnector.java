/**
 * This file is part of aion-lightning <aion-lightning.org>.
 * 
 * aion-lightning is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * aion-lightning is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionlightning.loginserver.network;

import com.aionemu.commons.network.NioServer;
import com.aionemu.commons.network.ServerCfg;
import com.aionlightning.loginserver.configs.Config;
import com.aionlightning.loginserver.network.aion.AionConnectionFactoryImpl;
import com.aionlightning.loginserver.network.gameserver.GsConnectionFactoryImpl;

/**
 * 
 * @author KID
 *
 */
public class NetConnector {
	/**
	 * NioServer instance that will handle io.
	 */
	private final static NioServer instance;

	static {
		ServerCfg aion = new ServerCfg(Config.LOGIN_BIND_ADDRESS, Config.LOGIN_PORT, "Aion Connections",
			new AionConnectionFactoryImpl());

		ServerCfg gs = new ServerCfg(Config.GAME_BIND_ADDRESS, Config.GAME_PORT, "Gs Connections",
			new GsConnectionFactoryImpl());

		instance = new NioServer(Config.NIO_READ_THREADS, gs, aion);
	}

	/**
	 * @return NioServer instance.
	 */
	public static NioServer getInstance() {
		return instance;
	}
}
