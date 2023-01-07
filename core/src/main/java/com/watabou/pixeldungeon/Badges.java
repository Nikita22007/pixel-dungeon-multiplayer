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
package com.watabou.pixeldungeon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class Badges {
	
	public static enum Badge {
		MONSTERS_SLAIN_1( "10 enemies slain", 0 ),
		MONSTERS_SLAIN_2( "50 enemies slain", 1 ),
		MONSTERS_SLAIN_3( "150 enemies slain", 2 ),
		MONSTERS_SLAIN_4( "250 enemies slain", 3 ),
		GOLD_COLLECTED_1( "100 gold collected", 4 ),
		GOLD_COLLECTED_2( "500 gold collected", 5 ),
		GOLD_COLLECTED_3( "2500 gold collected", 6 ),
		GOLD_COLLECTED_4( "7500 gold collected", 7 ),
		LEVEL_REACHED_1( "Level 6 reached", 8 ),
		LEVEL_REACHED_2( "Level 12 reached", 9 ),
		LEVEL_REACHED_3( "Level 18 reached", 10 ),
		LEVEL_REACHED_4( "Level 24 reached", 11 ),
		ALL_POTIONS_IDENTIFIED( "All potions identified", 16 ),
		ALL_SCROLLS_IDENTIFIED( "All scrolls identified", 17 ),
		ALL_RINGS_IDENTIFIED( "All rings identified", 18 ),
		ALL_WANDS_IDENTIFIED( "All wands identified", 19 ),
		ALL_ITEMS_IDENTIFIED( "All potions, scrolls, rings & wands identified", 35, true ),
		BAG_BOUGHT_SEED_POUCH,
		BAG_BOUGHT_SCROLL_HOLDER,
		BAG_BOUGHT_WAND_HOLSTER,
		ALL_BAGS_BOUGHT( "All bags bought", 23 ),
		DEATH_FROM_FIRE( "Death from fire", 24 ),
		DEATH_FROM_POISON( "Death from poison", 25 ),
		DEATH_FROM_GAS( "Death from toxic gas", 26 ),
		DEATH_FROM_HUNGER( "Death from hunger", 27 ),
		DEATH_FROM_GLYPH( "Death from an enchantment", 57 ),
		DEATH_FROM_FALLING( "Death from falling down", 59 ),
		YASD( "Death from fire, poison, toxic gas & hunger", 34, true ),
		BOSS_SLAIN_1_WARRIOR,
		BOSS_SLAIN_1_MAGE,
		BOSS_SLAIN_1_ROGUE,
		BOSS_SLAIN_1_HUNTRESS,
		BOSS_SLAIN_1( "1st boss slain", 12 ),
		BOSS_SLAIN_2( "2nd boss slain", 13 ),
		BOSS_SLAIN_3( "3rd boss slain", 14 ),
		BOSS_SLAIN_4( "4th boss slain", 15 ),
		BOSS_SLAIN_1_ALL_CLASSES( "1st boss slain by Warrior, Mage, Rogue & Huntress", 32, true ),
		BOSS_SLAIN_3_GLADIATOR,
		BOSS_SLAIN_3_BERSERKER,
		BOSS_SLAIN_3_WARLOCK,
		BOSS_SLAIN_3_BATTLEMAGE,
		BOSS_SLAIN_3_FREERUNNER,
		BOSS_SLAIN_3_ASSASSIN,
		BOSS_SLAIN_3_SNIPER,
		BOSS_SLAIN_3_WARDEN,
		BOSS_SLAIN_3_ALL_SUBCLASSES( 
			"3rd boss slain by Gladiator, Berserker, Warlock, Battlemage, Freerunner, Assassin, Sniper & Warden", 33, true ),
		RING_OF_HAGGLER( "Ring of Haggler obtained", 20 ),
		RING_OF_THORNS( "Ring of Thorns obtained", 21 ),
		STRENGTH_ATTAINED_1( "13 points of Strength attained", 40 ),
		STRENGTH_ATTAINED_2( "15 points of Strength attained", 41 ),
		STRENGTH_ATTAINED_3( "17 points of Strength attained", 42 ),
		STRENGTH_ATTAINED_4( "19 points of Strength attained", 43 ),
		FOOD_EATEN_1( "10 pieces of food eaten", 44 ),
		FOOD_EATEN_2( "20 pieces of food eaten", 45 ),
		FOOD_EATEN_3( "30 pieces of food eaten", 46 ),
		FOOD_EATEN_4( "40 pieces of food eaten", 47 ),
		MASTERY_WARRIOR,
		MASTERY_MAGE,
		MASTERY_ROGUE,
		MASTERY_HUNTRESS,
		ITEM_LEVEL_1( "Item of level 3 acquired", 48 ),
		ITEM_LEVEL_2( "Item of level 6 acquired", 49 ),
		ITEM_LEVEL_3( "Item of level 9 acquired", 50 ),
		ITEM_LEVEL_4( "Item of level 12 acquired", 51 ),
		RARE_ALBINO,
		RARE_BANDIT,
		RARE_SHIELDED,
		RARE_SENIOR,
		RARE_ACIDIC,
		RARE( "All rare monsters slain", 37, true ),
		VICTORY_WARRIOR,
		VICTORY_MAGE,
		VICTORY_ROGUE,
		VICTORY_HUNTRESS,
		VICTORY( "Amulet of Yendor obtained", 22 ),
		VICTORY_ALL_CLASSES( "Amulet of Yendor obtained by Warrior, Mage, Rogue & Huntress", 36, true ),
		MASTERY_COMBO( "7-hit combo", 56 ),
		POTIONS_COOKED_1( "3 potions cooked", 52 ),
		POTIONS_COOKED_2( "6 potions cooked", 53 ),
		POTIONS_COOKED_3( "9 potions cooked", 54 ),
		POTIONS_COOKED_4( "12 potions cooked", 55 ),
		NO_MONSTERS_SLAIN( "Level completed without killing any monsters", 28 ),
		GRIM_WEAPON( "Monster killed by a Grim weapon", 29 ),
		PIRANHAS( "6 piranhas killed", 30 ),
		NIGHT_HUNTER( "15 monsters killed at nighttime", 58 ),
		GAMES_PLAYED_1( "10 games played", 60, true ),
		GAMES_PLAYED_2( "100 games played", 61, true ),
		GAMES_PLAYED_3( "500 games played", 62, true ),
		GAMES_PLAYED_4( "2000 games played", 63, true ),
		HAPPY_END( "Happy end", 38 ),
		CHAMPION( "Challenge won", 39, true ),
		SUPPORTER( "Thanks for your support!", 31, true );
		
		public boolean meta;
		
		public String description;
		public int image;
		
		private Badge( String description, int image ) {
			this( description, image, false );
		}
		
		private Badge( String description, int image, boolean meta ) {
			this.description = description;
			this.image = image;
			this.meta = meta;
		}
		
		private Badge() {
			this( "", -1 );
		}
	}
	
	private static HashSet<Badge> global;
	private static HashSet<Badge> local = new HashSet<Badges.Badge>();
	
	private static boolean saveNeeded = false;
	
	public static Callback loadingListener = null;
	
	public static void reset() {
		local.clear();
		loadGlobal();
	}
	
	private static final String BADGES_FILE	= "badges.dat";
	private static final String BADGES		= "badges";
	
	private static HashSet<Badge> restore( Bundle bundle ) {
		HashSet<Badge> badges = new HashSet<Badge>();
		
		String[] names = bundle.getStringArray( BADGES );
		for (int i=0; i < names.length; i++) {
			try {
				badges.add( Badge.valueOf( names[i] ) );
			} catch (Exception e) {
			}
		}
	
		return badges;
	}
	
	private static void store( Bundle bundle, HashSet<Badge> badges ) {
		int count = 0;
		String names[] = new String[badges.size()];
		
		for (Badge badge:badges) {
			names[count++] = badge.toString();
		}
		bundle.put( BADGES, names );
	}
	
	public static void loadLocal( Bundle bundle ) {
		local = restore( bundle );
	}
	
	public static void saveLocal( Bundle bundle ) {
		store( bundle, local );
	}
	
	public static void loadGlobal() {
		if (global == null) {
			try {
				InputStream input = Game.instance.openFileInput( BADGES_FILE );
				Bundle bundle = Bundle.read( input );
				input.close();
				
				global = restore( bundle );
				
			} catch (IOException e) {
				global = new HashSet<Badge>();
			}
		}
	}
	
	public static void saveGlobal() {
		
		Bundle bundle = null;
		
		if (saveNeeded) {
			
			bundle = new Bundle();
			store( bundle, global );
			
			try {
				OutputStream output = Game.instance.openFileOutput( BADGES_FILE, Game.MODE_PRIVATE );
				Bundle.write( bundle, output );
				output.close();
				saveNeeded = false;
			} catch (IOException e) {
				
			}
		}
	}

	public static void validateMonstersSlain() {
		Badge badge = null;
		
		if (!local.contains( Badge.MONSTERS_SLAIN_1 ) && Statistics.enemiesSlain >= 10) {
			badge = Badge.MONSTERS_SLAIN_1;
			local.add( badge );
		}
		if (!local.contains( Badge.MONSTERS_SLAIN_2 ) && Statistics.enemiesSlain >= 50) {
			badge = Badge.MONSTERS_SLAIN_2;
			local.add( badge );
		}
		if (!local.contains( Badge.MONSTERS_SLAIN_3 ) && Statistics.enemiesSlain >= 150) {
			badge = Badge.MONSTERS_SLAIN_3;
			local.add( badge );
		}
		if (!local.contains( Badge.MONSTERS_SLAIN_4 ) && Statistics.enemiesSlain >= 250) {
			badge = Badge.MONSTERS_SLAIN_4;
			local.add( badge );
		}
		
		displayBadge( badge );
	}
	
	public static void validateGoldCollected() {
		Badge badge = null;
		
		if (!local.contains( Badge.GOLD_COLLECTED_1 ) && Statistics.goldCollected >= 100) {
			badge = Badge.GOLD_COLLECTED_1;
			local.add( badge );
		}
		if (!local.contains( Badge.GOLD_COLLECTED_2 ) && Statistics.goldCollected >= 500) {
			badge = Badge.GOLD_COLLECTED_2;
			local.add( badge );
		}
		if (!local.contains( Badge.GOLD_COLLECTED_3 ) && Statistics.goldCollected >= 2500) {
			badge = Badge.GOLD_COLLECTED_3;
			local.add( badge );
		}
		if (!local.contains( Badge.GOLD_COLLECTED_4 ) && Statistics.goldCollected >= 7500) {
			badge = Badge.GOLD_COLLECTED_4;
			local.add( badge );
		}
		
		displayBadge( badge );
	}
	
	public static void validateLevelReached() {
		Badge badge = null;
		
		if (!local.contains( Badge.LEVEL_REACHED_1 ) && Dungeon.hero.lvl >= 6) {
			badge = Badge.LEVEL_REACHED_1;
			local.add( badge );
		}
		if (!local.contains( Badge.LEVEL_REACHED_2 ) && Dungeon.hero.lvl >= 12) {
			badge = Badge.LEVEL_REACHED_2;
			local.add( badge );
		}
		if (!local.contains( Badge.LEVEL_REACHED_3 ) && Dungeon.hero.lvl >= 18) {
			badge = Badge.LEVEL_REACHED_3;
			local.add( badge );
		}
		if (!local.contains( Badge.LEVEL_REACHED_4 ) && Dungeon.hero.lvl >= 24) {
			badge = Badge.LEVEL_REACHED_4;
			local.add( badge );
		}
		
		displayBadge( badge );
	}

	public static void validateItemLevelAquired( Item item ) {
		
		// This method should be called:
		// 1) When an item gets obtained (Item.collect)
		// 2) When an item gets upgraded (ScrollOfUpgrade, ScrollOfWeaponUpgrade, ShortSword, WandOfMagicMissile)
		// 3) When an item gets identified
		if (!item.levelKnown) {
			return;
		}
		
		Badge badge = null;
		
		if (!local.contains( Badge.ITEM_LEVEL_1 ) && item.level() >= 3) {
			badge = Badge.ITEM_LEVEL_1;
			local.add( badge );
		}
		if (!local.contains( Badge.ITEM_LEVEL_2 ) && item.level() >= 6) {
			badge = Badge.ITEM_LEVEL_2;
			local.add( badge );
		}
		if (!local.contains( Badge.ITEM_LEVEL_3 ) && item.level() >= 9) {
			badge = Badge.ITEM_LEVEL_3;
			local.add( badge );
		}
		if (!local.contains( Badge.ITEM_LEVEL_4 ) && item.level() >= 12) {
			badge = Badge.ITEM_LEVEL_4;
			local.add( badge );
		}
		
		displayBadge( badge );
	}


	public static void validateDeathFromFire() {
		Badge badge = Badge.DEATH_FROM_FIRE;
		local.add( badge );
		displayBadge( badge );
		
		validateYASD();
	}
	
	public static void validateDeathFromPoison() {
		Badge badge = Badge.DEATH_FROM_POISON;
		local.add( badge );
		displayBadge( badge );
		
		validateYASD();
	}
	
	public static void validateDeathFromGas() {
		Badge badge = Badge.DEATH_FROM_GAS;
		local.add( badge );
		displayBadge( badge );
		
		validateYASD();
	}
	
	public static void validateDeathFromHunger() {
		Badge badge = Badge.DEATH_FROM_HUNGER;
		local.add( badge );
		displayBadge( badge );
		
		validateYASD();
	}

	public static void validateDeathFromFalling() {
		Badge badge = Badge.DEATH_FROM_FALLING;
		local.add( badge );
		displayBadge( badge );
	}
	
	private static void validateYASD() {
		if (global.contains( Badge.DEATH_FROM_FIRE ) &&
			global.contains( Badge.DEATH_FROM_POISON ) &&
			global.contains( Badge.DEATH_FROM_GAS ) &&
			global.contains( Badge.DEATH_FROM_HUNGER)) {
			
			Badge badge = Badge.YASD;
			local.add( badge );
			displayBadge( badge );
		}
	}

	public static void validateMasteryCombo( int n ) {
		if (!local.contains( Badge.MASTERY_COMBO ) && n == 7) {
			Badge badge = Badge.MASTERY_COMBO;
			local.add( badge );
			displayBadge( badge );
		}
	}

	public static void validateNoKilling() {
		if (!local.contains( Badge.NO_MONSTERS_SLAIN ) && Statistics.completedWithNoKilling) {
			Badge badge = Badge.NO_MONSTERS_SLAIN;
			local.add( badge );
			displayBadge( badge );
		}
	}

	public static void validateNightHunter() {
		if (!local.contains( Badge.NIGHT_HUNTER ) && Statistics.nightHunt >= 15) {
			Badge badge = Badge.NIGHT_HUNTER;
			local.add( badge );
			displayBadge( badge );
		}
	}

	public static void validateGamesPlayed() {
		Badge badge = null;
		if (Rankings.INSTANCE.totalNumber >= 10) {
			badge = Badge.GAMES_PLAYED_1;
		}
		if (Rankings.INSTANCE.totalNumber >= 100) {
			badge = Badge.GAMES_PLAYED_2;
		}
		if (Rankings.INSTANCE.totalNumber >= 500) {
			badge = Badge.GAMES_PLAYED_3;
		}
		if (Rankings.INSTANCE.totalNumber >= 2000) {
			badge = Badge.GAMES_PLAYED_4;
		}
		
		displayBadge( badge );
	}
	
	public static void validateHappyEnd() {
		displayBadge( Badge.HAPPY_END );
	}
	
	public static void validateChampion() {
		displayBadge( Badge.CHAMPION );
	}
	
	private static void displayBadge( Badge badge ) {
		
		if (badge == null) {
			return;
		}
		
		if (global.contains( badge )) {
			
			if (!badge.meta) {
				GLog.h( "Badge endorsed: %s", badge.description );
			}
			
		} else {
			
			global.add( badge );
			saveNeeded = true;
			
			if (badge.meta) {
				GLog.h( "New super badge: %s", badge.description );
			} else {
				GLog.h( "New badge: %s", badge.description );
			}	
			PixelScene.showBadge( badge );
		}
	}
	
	public static boolean isUnlocked( Badge badge ) {
		return global.contains( badge );
	}
	
	public static List<Badge> filtered( boolean global ) {
		
		HashSet<Badge> filtered = new HashSet<Badge>( global ? Badges.global : Badges.local );
		
		{
			Iterator<Badge> iterator = filtered.iterator();
			while (iterator.hasNext()) {
				Badge badge = iterator.next();
				if ((!global && badge.meta) || badge.image == -1) {
					iterator.remove();
				}
			}
		}
		
		leaveBest( filtered, Badge.MONSTERS_SLAIN_1, Badge.MONSTERS_SLAIN_2, Badge.MONSTERS_SLAIN_3, Badge.MONSTERS_SLAIN_4 );
		leaveBest( filtered, Badge.GOLD_COLLECTED_1, Badge.GOLD_COLLECTED_2, Badge.GOLD_COLLECTED_3, Badge.GOLD_COLLECTED_4 );
		leaveBest( filtered, Badge.BOSS_SLAIN_1, Badge.BOSS_SLAIN_2, Badge.BOSS_SLAIN_3, Badge.BOSS_SLAIN_4 );
		leaveBest( filtered, Badge.LEVEL_REACHED_1, Badge.LEVEL_REACHED_2, Badge.LEVEL_REACHED_3, Badge.LEVEL_REACHED_4 );
		leaveBest( filtered, Badge.STRENGTH_ATTAINED_1, Badge.STRENGTH_ATTAINED_2, Badge.STRENGTH_ATTAINED_3, Badge.STRENGTH_ATTAINED_4 );
		leaveBest( filtered, Badge.FOOD_EATEN_1, Badge.FOOD_EATEN_2, Badge.FOOD_EATEN_3, Badge.FOOD_EATEN_4 );
		leaveBest( filtered, Badge.ITEM_LEVEL_1, Badge.ITEM_LEVEL_2, Badge.ITEM_LEVEL_3, Badge.ITEM_LEVEL_4 );
		leaveBest( filtered, Badge.POTIONS_COOKED_1, Badge.POTIONS_COOKED_2, Badge.POTIONS_COOKED_3, Badge.POTIONS_COOKED_4 );
		leaveBest( filtered, Badge.BOSS_SLAIN_1_ALL_CLASSES, Badge.BOSS_SLAIN_3_ALL_SUBCLASSES );
		leaveBest( filtered, Badge.DEATH_FROM_FIRE, Badge.YASD );
		leaveBest( filtered, Badge.DEATH_FROM_GAS, Badge.YASD );
		leaveBest( filtered, Badge.DEATH_FROM_HUNGER, Badge.YASD );
		leaveBest( filtered, Badge.DEATH_FROM_POISON, Badge.YASD );
		leaveBest( filtered, Badge.ALL_POTIONS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED );
		leaveBest( filtered, Badge.ALL_SCROLLS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED );
		leaveBest( filtered, Badge.ALL_RINGS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED );
		leaveBest( filtered, Badge.ALL_WANDS_IDENTIFIED, Badge.ALL_ITEMS_IDENTIFIED );
		leaveBest( filtered, Badge.VICTORY, Badge.VICTORY_ALL_CLASSES );
		leaveBest( filtered, Badge.VICTORY, Badge.HAPPY_END );
 		leaveBest( filtered, Badge.VICTORY, Badge.CHAMPION );
		leaveBest( filtered, Badge.GAMES_PLAYED_1, Badge.GAMES_PLAYED_2, Badge.GAMES_PLAYED_3, Badge.GAMES_PLAYED_4 );
		
		ArrayList<Badge> list = new ArrayList<Badge>( filtered );
		Collections.sort( list );
		
		return list;
	}
	
	private static void leaveBest( HashSet<Badge> list, Badge...badges ) {
		for (int i=badges.length-1; i > 0; i--) {
			if (list.contains( badges[i])) {
				for (int j=0; j < i; j++) {
					list.remove( badges[j] );
				}
				break;
			}
		}
	}
}
