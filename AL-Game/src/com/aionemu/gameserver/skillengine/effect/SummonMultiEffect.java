/**
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
package com.aionemu.gameserver.skillengine.effect;
import com.aionemu.gameserver.skillengine.effect.SummonEffect;

import java.util.List;
import java.util.concurrent.Future;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.SummonController.UnsummonType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.change.Change;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Simple
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonMultiEffect")
public class SummonMultiEffect extends EffectTemplate {
	@XmlElements({
		@XmlElement(name = "summon", type = SummonEffect.class)
	})
	protected List<SummonEffect> summons;

	@XmlAttribute(name = "time", required = true)
	protected int time; // in seconds

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		int i = 0;
		for (SummonEffect summon: summons) {
			effected.getController().createSummon(summon.npcId, effect.getSkillId(), effect.getSkillLevel(), i == 0 ? true : false);
			i++;
		}
		if ((time > 0) && (effect.getEffected() instanceof Player)) {
			final Player effector = (Player)effect.getEffected();
			final Summon[] summons = effector.getSummons();
			for (final Summon summon: summons) {
				Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if ((summon != null) && (summon.isSpawned()))
							summon.getController().release(UnsummonType.COMMAND);
					}
				}, time * 1000);
				summon.getController().addTask(TaskId.DESPAWN, task);
			}
		}
	}

	@Override
	public void calculate(Effect effect) {
		effect.addSucessEffect(this);
	}
}
