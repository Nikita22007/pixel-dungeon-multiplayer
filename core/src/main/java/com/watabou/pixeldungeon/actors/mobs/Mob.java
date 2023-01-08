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
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

public abstract class Mob extends Char {
	
	public AiState SLEEPEING	= new Sleeping();
	public AiState HUNTING		= new Hunting();
	public AiState WANDERING	= new Wandering();
	public AiState FLEEING		= new Fleeing();
	public AiState PASSIVE		= new Passive();
	public AiState state = SLEEPEING;
	
	public Class<? extends CharSprite> spriteClass;
	
	protected int target = -1;
	
	protected int defenseSkill = 0;
	
	protected int EXP = 1;
	protected int maxLvl = 30;
	
	protected Char enemy;
	
	public boolean hostile = true;
	
	private static final String STATE	= "state";
	private static final String TARGET	= "target";

	private String desc = "A creature unknown to science.";
	
	public CharSprite sprite() {
		CharSprite sprite = null;
		try {
			sprite = spriteClass.newInstance();
		} catch (Exception e) {
		}
		return sprite;
	}

	public boolean moveSprite(int from, int to) {

		if (sprite.isVisible() && (Dungeon.visible[from] || Dungeon.visible[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.place( to );
			return true;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		Dungeon.level.mobs.remove( this );
	}
	
	public String description() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public void yell( String str ) {
		GLog.n( "%s: \"%s\" ", name, str );
	}
	
	public interface AiState {
		public String status();
	}
	
	private class Sleeping implements AiState {
		
		public static final String TAG	= "SLEEPING";

		@Override
		public String status() {
			return Utils.format( "This %s is sleeping", name );
		}
	}
	
	private class Wandering implements AiState {
		
		public static final String TAG	= "WANDERING";

		@Override
		public String status() {
			return Utils.format( "This %s is wandering", name );
		}
	}
	
	private class Hunting implements AiState {
		
		public static final String TAG	= "HUNTING";

		@Override
		public String status() {
			return Utils.format( "This %s is hunting", name );
		}
	}
	
	protected class Fleeing implements AiState {
		
		public static final String TAG	= "FLEEING";

		protected void nowhereToRun() {
		}
		
		@Override
		public String status() {
			return Utils.format( "This %s is fleeing", name );
		}
	}
	
	private class Passive implements AiState {
		
		public static final String TAG	= "PASSIVE";

		@Override
		public String status() {
			return Utils.format( "This %s is passive", name );
		}
	}
}
