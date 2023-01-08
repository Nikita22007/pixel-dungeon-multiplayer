/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.actors.hero;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.ui.SpecialSlot;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Belongings implements Iterable<Item> {

	public static final int BACKPACK_SIZE	= 19;

    private Hero owner;

	public Bag backpack;

	public Item weapon = null;
	public Item armor = null;
	public int armorTier = 0;
	public Item ring1 = null;
	public Item ring2 = null;

	public ArrayList<SpecialSlot> specialSlots = new ArrayList<SpecialSlot>(4);

	public ArrayList<Bag> Bags(){
		ArrayList<Bag> bags = new ArrayList<Bag>(6);
		bags.add(backpack);
		bags.addAll(backpack.bagsInside());
		return bags;
	}

	public Belongings( Hero owner ) {
		this.owner = owner;

		backpack = new Bag() {{
			name = "backpack";
			size = BACKPACK_SIZE;
		}};
		backpack.owner = owner;
	}

	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String RING1		= "ring1";
	private static final String RING2		= "ring2";

	public void storeInBundle( Bundle bundle ) {

		backpack.storeInBundle( bundle );

		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( RING1, ring1 );
		bundle.put( RING2, ring2 );
	}

	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				return (T)item;
			}
		}

		return null;
	}

	public void identify() {
		for (Item item : this) {
			item.identify();
		}
	}

	public void observe() {
		if (weapon != null) {
			weapon.identify();
			Badges.validateItemLevelAquired( weapon );
		}
		if (armor != null) {
			armor.identify();
			Badges.validateItemLevelAquired( armor );
		}
		if (ring1 != null) {
			ring1.identify();
			Badges.validateItemLevelAquired( ring1 );
		}
		if (ring2 != null) {
			ring2.identify();
			Badges.validateItemLevelAquired( ring2 );
		}
		for (Item item : backpack) {
			item.cursedKnown = true;
		}
	}

	public Item randomUnequipped() {
		return Random.element( backpack.items );
	}

	public void setSpecialSlot(SpecialSlot specialSlot) {
        for (int i = 0; i < specialSlots.size(); i++) {
            if (specialSlots.get(i).id == specialSlot.id) {
                specialSlots.set(i, specialSlot);
                return;
            }
        }
        specialSlots.add(specialSlot);
    }

    public Item getItemFromSpecialSlot(int id){
		for (int i = 0; i < specialSlots.size(); i++) {
			if (specialSlots.get(i).id == id) {
				return specialSlots.get(i).item;
			}
		}
		return null;
	}

	public void updateSpecialSlot(int id, Item item) {
		SpecialSlot slot = new SpecialSlot();
		slot.id = id;
		slot.item = item;
		updateSpecialSlot(slot);
	}
    public void updateSpecialSlot(SpecialSlot specialSlot) {
        for (int i = 0; i < specialSlots.size(); i++) {
            if (specialSlots.get(i).id == specialSlot.id) {
                specialSlots.get(i).updateWith(specialSlot);
                return;
            }
        }
        // if not found this slot in list
        specialSlots.add(specialSlot);
    }

	public Item get(List<Integer> path) {
		assert !path.isEmpty(): "path is null";
		if (path.get(0) < 0){
			return getItemFromSpecialSlot( -path.get(0) - 1);
		}
		return backpack.get(path);
	}

	public void remove(List<Integer> path) {
		assert !path.isEmpty(): "path is null";
		if (path.get(0) < 0){
			updateSpecialSlot(path.get(0), null);
			return;
		}
		backpack.remove(path);
	}

	public List<Integer> pathOfItem(@NotNull Item item) {
		assert (item != null) : "path of null item";
		for (int i = 0; i < specialSlots.size(); i++) {
			if (specialSlots.get(i) == null) {
				continue;
			}
			if (specialSlots.get(i).item == item) {
				List<Integer> slot = new ArrayList<>(2);
				slot.add(-i - 1);
				return slot;
			}
			if (specialSlots.get(i).item instanceof Bag) {
				List<Integer> path = ((Bag) specialSlots.get(i).item).pathOfItem(item);
				if (path != null) {
					path.add(0, -i - 1);
					return path;
				}
			}
		}
		return backpack.pathOfItem(item);
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}

	private class ItemIterator implements Iterator<Item> {

		private int index = 0;

		private Iterator<Item> backpackIterator = backpack.iterator();

		private Item[] equipped = {weapon, armor, ring1, ring2};
		private int backpackIndex = equipped.length;

		@Override
		public boolean hasNext() {

			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}

			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {

			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}

			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = ring1 = null;
				break;
			case 3:
				equipped[3] = ring2 = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
