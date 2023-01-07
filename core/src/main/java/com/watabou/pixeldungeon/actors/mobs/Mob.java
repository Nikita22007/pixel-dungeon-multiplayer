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
	
	private static final String	TXT_DIED	= "You hear something died in the distance";
	
	protected static final String	TXT_ECHO	= "echo of ";
	
	protected static final String TXT_NOTICE1	= "?!";
	protected static final String TXT_RAGE		= "#$%^";
	protected static final String TXT_EXP		= "%+dEXP";
	
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
	protected boolean enemySeen;
	protected boolean alerted = false;

	protected static final float TIME_TO_WAKE_UP = 1f;
	
	public boolean hostile = true;
	
	private static final String STATE	= "state";
	private static final String TARGET	= "target";

	private String desc = "A creature unknown to science.";

	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		if (state == SLEEPEING) {
			bundle.put( STATE, Sleeping.TAG );
		} else if (state == WANDERING) {
			bundle.put( STATE, Wandering.TAG );
		} else if (state == HUNTING) {
			bundle.put( STATE, Hunting.TAG );
		} else if (state == FLEEING) {
			bundle.put( STATE, Fleeing.TAG );
		} else if (state == PASSIVE) {
			bundle.put( STATE, Passive.TAG );
		}
		bundle.put( TARGET, target );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		String state = bundle.getString( STATE );
		if (state.equals( Sleeping.TAG )) {
			this.state = SLEEPEING;
		} else if (state.equals( Wandering.TAG )) {
			this.state = WANDERING;
		} else if (state.equals( Hunting.TAG )) {
			this.state = HUNTING;
		} else if (state.equals( Fleeing.TAG )) {
			this.state = FLEEING;
		} else if (state.equals( Passive.TAG )) {
			this.state = PASSIVE;
		}

		target = bundle.getInt( TARGET );
	}
	
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
	
	protected boolean canAttack( Char enemy ) {
		return Level.adjacent( pos, enemy.pos );
	}
	
	protected boolean getCloser( int target ) {
		
		if (rooted) {
			return false;
		}
		
		int step = Dungeon.findPath( this, pos, target, 
			Level.passable, 
			Level.fieldOfView );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean getFurther( int target ) {
		int step = Dungeon.flee( this, pos, target, 
			Level.passable, 
			Level.fieldOfView );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void move( int step ) {
		super.move( step );
	}

	@Override
	public void onAttackComplete() {
		attack( enemy );
		super.onAttackComplete();
	}


	@Override
	public void destroy() {
		
		super.destroy();
		
		Dungeon.level.mobs.remove( this );
		
		if (Dungeon.hero.isAlive()) {

			if (hostile) {
				Statistics.enemiesSlain++;
				Badges.validateMonstersSlain();
				Statistics.qualifiedForNoKilling = false;
				
				if (Dungeon.nightMode) {
					Statistics.nightHunt++;
				} else {
					Statistics.nightHunt = 0;
				}
				Badges.validateNightHunter();
			}

			int exp = exp();
			if (exp > 0) {
				Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, TXT_EXP, exp );
			}
		}
	}
	
	public int exp() {
		return Dungeon.hero.lvl <= maxLvl ? EXP : 0;
	}
	
	@Override
	public void die( Object cause ) {
		
		super.die( cause );
	}
	
	protected Object loot = null;
	protected float lootChance = 0;

	public boolean reset() {
		return false;
	}
	
	public void beckon( int cell ) {
		
		notice();
		
		if (state != HUNTING) {
			state = WANDERING;
		}
		target = cell;
	}
	
	public String description() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void notice() {
		sprite.showAlert();
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
