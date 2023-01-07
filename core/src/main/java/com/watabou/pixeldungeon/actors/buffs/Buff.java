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
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import java.util.Dictionary;
import java.util.Hashtable;

public class Buff extends Actor {

	public Char target;

	public int buff_id = -1;

	public static Dictionary<Integer, Buff> all_buffs = new Hashtable<>();


	public boolean attachTo( Char target ) {

		if (target.immunities().contains( getClass() )) {
			return false;
		}

		if (buff_id < 0) {
			return false;
		}

		all_buffs.put(buff_id, this);

		this.target = target;
		target.add( this );
		
		return true;
	}
	
	public void detach() {
		all_buffs.remove(buff_id);
		target.remove( this );
	}

	public int icon() {
		return BuffIndicator.NONE;
	}

	// static
	public static<T extends Buff> T append( Char target, Class<T> buffClass ) {
		try {
			T buff = buffClass.newInstance();
			buff.attachTo( target );
			return buff;
		} catch (Exception e) {
			return null;
		}
	}

	public static void detach(int id) {
		detach(all_buffs.get(id));
	}

	public static void detach( Buff buff ) {
		if (buff != null) {
			buff.detach();
		}
	}
	
	public static void detach( Char target, Class<? extends Buff> cl ) {
		detach( target.buff( cl ) );
	}


	public static Buff get(int id) {
		return all_buffs.get(id);
	}

}
