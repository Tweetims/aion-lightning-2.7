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
package com.aionemu.gameserver.services.item;

import static com.aionemu.gameserver.services.item.ItemPacketService.sendStorageUpdatePacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.ExchangeService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class ItemSplitService {

	private static final Logger log = LoggerFactory.getLogger(ItemSplitService.class);

	/**
	 * Move part of stack into different slot
	 */
	public static final void splitItem(Player player, int itemObjId, int destinationObjId, long splitAmount,
		short slotNum, byte sourceStorageType, byte destinationStorageType) {
		if (splitAmount <= 0)
			return;
		
		if (player.isTrading()) {
			// You cannot split items in the inventory during a trade.
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300713));
			return;
		}

		IStorage sourceStorage = player.getStorage(sourceStorageType);
		IStorage destStorage = player.getStorage(destinationStorageType);
		if (sourceStorage == null || destStorage == null) {
			log.warn(String.format("storage null playerName sourceStorage destStorage %d %d %d", player.getName(), sourceStorageType, destinationStorageType));
			return;
		}
		Item sourceItem = sourceStorage.getItemByObjId(itemObjId);
		Item targetItem = destStorage.getItemByObjId(destinationObjId);

		if (sourceItem == null) {
			sourceItem = sourceStorage.getKinahItem();
			if (sourceItem == null || sourceItem.getObjectId() != itemObjId) {
				log.warn(String.format("CHECKPOINT: attempt to split null item %d %d %d", itemObjId, splitAmount, slotNum));
				return;
			}
		}

		if (sourceStorageType != destinationStorageType
			&& (ItemRestrictionService.isItemRestrictedTo(player, sourceItem, destinationStorageType) || ItemRestrictionService
				.isItemRestrictedFrom(player, sourceItem, sourceStorageType))) {
			sendStorageUpdatePacket(player, StorageType.getStorageTypeById(sourceStorageType), sourceItem);
			return;
		}

		// To move kinah from inventory to warehouse and vice versa client using split item packet
		if (sourceItem.getItemTemplate().isKinah()) {
			moveKinah(player, sourceStorage, splitAmount);
			return;
		}

		if (targetItem == null) {
			long oldItemCount = sourceItem.getItemCount() - splitAmount;
			if (sourceItem.getItemCount() < splitAmount || oldItemCount == 0)
				return;
			
			if (sourceStorageType != destinationStorageType)
				LegionService.getInstance().addWHItemHistory(player, sourceItem.getItemId(), splitAmount, sourceStorage, destStorage);
			
			Item newItem = ItemFactory.newItem(sourceItem.getItemTemplate().getTemplateId(), splitAmount);
			newItem.setEquipmentSlot(slotNum);
			sourceStorage.decreaseItemCount(sourceItem, splitAmount, ItemUpdateType.DEC_SPLIT);
			PacketSendUtility.sendPacket(player, SM_CUBE_UPDATE.cubeSize(sourceStorage.getStorageType(), player));
			if (destStorage.add(newItem) == null)
				ItemService.releaseItemId(newItem); // if item was not added - we can release its id
		}
		else if (targetItem.getItemId() == sourceItem.getItemId())
			mergeStacks(sourceStorage, destStorage, sourceItem, targetItem, splitAmount);
	}

	/**
	 * Merge 2 stacks with simple validation validation
	 */
	public static void mergeStacks(IStorage sourceStorage, IStorage destStorage, Item sourceItem, Item targetItem,
		long count) {
		if (sourceItem.getItemCount() >= count) {
			long freeCount = targetItem.getFreeCount();
			count = count > freeCount ? freeCount : count;
			long leftCount = destStorage.increaseItemCount(targetItem, count, ItemUpdateType.INC_MERGE);
			sourceStorage.decreaseItemCount(sourceItem, count - leftCount, ItemUpdateType.DEC_SPLIT);
		}

	}

	private static void moveKinah(Player player, IStorage source, long splitAmount) {
		if (source.getKinah() < splitAmount)
			return;
		if (ExchangeService.getInstance().isPlayerInExchange(player))
			return;

		switch (source.getStorageType()) {
			case CUBE: {
				IStorage destination = player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId());
				long chksum = (source.getKinah() - splitAmount) + (destination.getKinah() + splitAmount);

				if (chksum != source.getKinah() + destination.getKinah())
					return;

				updateKinahCount(source, splitAmount, destination);
				break;
			}

			case ACCOUNT_WAREHOUSE: {
				IStorage destination = player.getStorage(StorageType.CUBE.getId());
				long chksum = (source.getKinah() - splitAmount) + (destination.getKinah() + splitAmount);

				if (chksum != source.getKinah() + destination.getKinah())
					return;

				updateKinahCount(source, splitAmount, destination);
				break;
			}
		}
	}

	private static final void updateKinahCount(IStorage source, long splitAmount, IStorage destination) {
		source.decreaseKinah(splitAmount, ItemUpdateType.DEC_SPLIT);
		destination.increaseKinah(splitAmount, ItemUpdateType.INC_MERGE_KINAH);
	}
}
