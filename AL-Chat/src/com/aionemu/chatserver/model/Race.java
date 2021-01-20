/*
 * This file is part of InPanic Core <Ver:3.1>.
 *
 *  InPanic-Core is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  InPanic-Core is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with InPanic-Core.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aionemu.chatserver.model;

/**
 * @author ATracer
 */
public enum Race {
	ELYOS(0),
	ASMODIANS(1);

	/**
	 * id of race
	 */
	private int raceId;

	/**
	 * @param raceId
	 */
	private Race(int raceId) {
		this.raceId = raceId;
	}

	/**
	 * @return the raceId
	 */
	public int getRaceId() {
		return raceId;
	}
}