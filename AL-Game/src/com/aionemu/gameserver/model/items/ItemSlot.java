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
package com.aionemu.gameserver.model.items;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum is defining inventory slots, to which items can be equipped.
 * 
 * @author Luno
 */
public enum ItemSlot {
	MAIN_HAND(1),
	SUB_HAND(1 << 1),
	HELMET(1 << 2),
	TORSO(1 << 3),
	GLOVES(1 << 4),
	BOOTS(1 << 5),
	EARRINGS_LEFT(1 << 6),
	EARRINGS_RIGHT(1 << 7),
	RING_LEFT(1 << 8),
	RING_RIGHT(1 << 9),
	NECKLACE(1 << 10),
	SHOULDER(1 << 11),
	PANTS(1 << 12),
	POWER_SHARD_RIGHT(1 << 13),
	POWER_SHARD_LEFT(1 << 14),
	WINGS(1 << 15),
	// non-NPC equips (slot > Short.MAX)
	WAIST(1 << 16),
	MAIN_OFF_HAND(1 << 17),
	SUB_OFF_HAND(1 << 18),

	// combo
	MAIN_OR_SUB(MAIN_HAND.slotIdMask | SUB_HAND.slotIdMask, true), // 3
	EARRING_RIGHT_OR_LEFT(EARRINGS_LEFT.slotIdMask | EARRINGS_RIGHT.slotIdMask, true), // 192
	RING_RIGHT_OR_LEFT(RING_LEFT.slotIdMask | RING_RIGHT.slotIdMask, true), // 768
	SHARD_RIGHT_OR_LEFT(POWER_SHARD_LEFT.slotIdMask | POWER_SHARD_RIGHT.slotIdMask, true), // 24576
	TORSO_GLOVE_FOOT_SHOULDER_LEG(0, true), // TODO

	// STIGMA slots
	STIGMA1(1 << 19),
	STIGMA2(1 << 20),
	STIGMA3(1 << 21),
	STIGMA4(1 << 22),
	STIGMA5(1 << 23),
	STIGMA6(1 << 24),

	REGULAR_STIGMAS(STIGMA1.slotIdMask | STIGMA2.slotIdMask | STIGMA3.slotIdMask | STIGMA4.slotIdMask
		| STIGMA5.slotIdMask | STIGMA6.slotIdMask),

	NONE(1 << 25), // Unknown

	ADV_STIGMA1(1 << 26),
	ADV_STIGMA2(1 << 27),
	ADV_STIGMA3(1 << 28),
	ADV_STIGMA4(1 << 29),
	ADV_STIGMA5(1 << 30),

	ADVANCED_STIGMAS(ADV_STIGMA1.slotIdMask | ADV_STIGMA2.slotIdMask | ADV_STIGMA3.slotIdMask | ADV_STIGMA4.slotIdMask
		| ADV_STIGMA5.slotIdMask),
	
	ALL_STIGMA(REGULAR_STIGMAS.slotIdMask | ADVANCED_STIGMAS.slotIdMask, true);

	private int slotIdMask;
	private boolean combo;
	private static TIntObjectHashMap<ItemSlot[]> itemSlots;
	
	static{
		itemSlots = new TIntObjectHashMap<ItemSlot[]>();
		for(ItemSlot slot : values()){
			ItemSlot[] slotsForMask = calculateSlots(slot.getSlotIdMask());
			itemSlots.put(slot.getSlotIdMask(), slotsForMask);
		}
		//TODO 10 in items - npc's TORSO_GLOVE_FOOT_SHOULDER_LEG
		itemSlots.put(10, calculateSlots(10));
	}

	private ItemSlot(int mask) {
		this(mask, false);
	}

	private ItemSlot(int mask, boolean combo) {
		this.slotIdMask = mask;
		this.combo = combo;
	}

	public int getSlotIdMask() {
		return slotIdMask;
	}

	/**
	 * @return the combo
	 */
	public boolean isCombo() {
		return combo;
	}

	private static ItemSlot[] calculateSlots(int slotIdMask) {
		List<ItemSlot> slots = new ArrayList<ItemSlot>();
		for (ItemSlot itemSlot : values()) {
			int sumMask = itemSlot.slotIdMask & slotIdMask;
			/**
			 * possible values in this check - one of combo slots (MAIN, RIGHT_RING etc)
			 */
			if (sumMask > 0 && sumMask <= slotIdMask && !itemSlot.isCombo())
				slots.add(itemSlot);
		}
		return slots.toArray(new ItemSlot[slots.size()]);
	}
	
	public static ItemSlot[] getSlotsFor(int slotIdMask) {
		ItemSlot[] slots = itemSlots.get(slotIdMask);
		if (slots == null || slots.length == 0)
			throw new IllegalArgumentException("Invalid provided slotIdMask " + slotIdMask);

		return slots;
	}

	public static boolean isStigma(long slot) {
		return (ALL_STIGMA.slotIdMask & slot) == slot;
	}
}
