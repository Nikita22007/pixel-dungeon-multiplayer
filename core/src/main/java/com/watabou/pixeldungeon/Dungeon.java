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

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class Dungeon {

	public static int challenges;
	
	public static Hero hero;
	public static Level level;
	
	public static int depth;
	//public static int gold;
	// Reason of death
	public static String resultDescription;
	
	public static HashSet<Integer> chapters;
	
	// Hero's field of view
	public static boolean[] visible = new boolean[Level.LENGTH];
	
	public static boolean nightMode;
	
	public static SparseArray<ArrayList<Item>> droppedItems;
	
	public static void init() {

		challenges = PixelDungeon.challenges();
		
		Actor.clear();
		
		PathFinder.setMapSize( Level.WIDTH, Level.HEIGHT );

		Statistics.reset();
		Journal.reset();
		
		droppedItems = new SparseArray<ArrayList<Item>>();
		
		chapters = new HashSet<Integer>();

		QuickSlot.primaryValue = null;
		QuickSlot.secondaryValue = null;

		if (hero == null) {
			hero = new Hero();
		}
		
		Badges.reset();
		
		//StartScene.curClass.initHero( hero );
	}
	
	public static boolean isChallenged( int mask ) {
		return (challenges & mask) != 0;
	}
	

	public static boolean shopOnLevel() {
		return depth == 6 || depth == 11 || depth == 16;
	}

	public static boolean bossLevel( int depth ) {
		return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25;
	}
	
	private static final String RG_GAME_FILE	= "game.dat";
	private static final String RG_DEPTH_FILE	= "depth%d.dat";
	
	private static final String WR_GAME_FILE	= "warrior.dat";
	private static final String WR_DEPTH_FILE	= "warrior%d.dat";
	
	private static final String MG_GAME_FILE	= "mage.dat";
	private static final String MG_DEPTH_FILE	= "mage%d.dat";
	
	private static final String RN_GAME_FILE	= "ranger.dat";
	private static final String RN_DEPTH_FILE	= "ranger%d.dat";
	
	private static final String VERSION		= "version";
	private static final String CHALLENGES	= "challenges";
	private static final String HERO		= "hero";
	private static final String GOLD		= "gold";
	private static final String DEPTH		= "depth";
	private static final String LEVEL		= "level";
	private static final String DROPPED		= "dropped%d";
	private static final String POS			= "potionsOfStrength";
	private static final String SOU			= "scrollsOfEnhancement";
	private static final String SOE			= "scrollsOfEnchantment";
	private static final String DV			= "dewVial";
	private static final String CHAPTERS	= "chapters";
	private static final String QUESTS		= "quests";
	private static final String BADGES		= "badges";
	
	public static String gameFile( HeroClass cl ) {
		switch (cl) {
		case WARRIOR:
			return WR_GAME_FILE;
		case MAGE:
			return MG_GAME_FILE;
		case HUNTRESS:
			return RN_GAME_FILE;
		default:
			return RG_GAME_FILE;
		}
	}
	
	private static String depthFile( HeroClass cl ) {
		switch (cl) {
		case WARRIOR:
			return WR_DEPTH_FILE;
		case MAGE:
			return MG_DEPTH_FILE;
		case HUNTRESS:
			return RN_DEPTH_FILE;
		default:
			return RG_DEPTH_FILE;
		}
	}
	
	public static void saveGame( String fileName ) throws IOException {
		try {
			Bundle bundle = new Bundle();
			
			bundle.put( VERSION, Game.version );
			bundle.put( CHALLENGES, challenges );
			bundle.put( GOLD, hero.gold);
			bundle.put( DEPTH, depth );
			
			for (int d : droppedItems.keyArray()) {
				bundle.put( String.format( DROPPED, d ), droppedItems.get( d ) );
			}
			
			int count = 0;
			int ids[] = new int[chapters.size()];
			for (Integer id : chapters) {
				ids[count++] = id;
			}
			bundle.put( CHAPTERS, ids );
			
			Bundle quests = new Bundle();
			bundle.put( QUESTS, quests );

			Statistics.storeInBundle( bundle );
			Journal.storeInBundle( bundle );
			
			QuickSlot.save( bundle );

			Bundle badges = new Bundle();
			Badges.saveLocal( badges );
			bundle.put( BADGES, badges );
			
			OutputStream output = Game.instance.openFileOutput( fileName, Game.MODE_PRIVATE );
			Bundle.write( bundle, output );
			output.close();
			
		} catch (Exception e) {

			GamesInProgress.setUnknown( hero.heroClass );
		}
	}

	
	public static void saveAll() throws IOException {
		return;
	}

	
	public static void loadGame( String fileName ) throws IOException {
		
		Bundle bundle = gameBundle( fileName );
		
		Dungeon.challenges = bundle.getInt( CHALLENGES );
		
		Dungeon.level = null;
		Dungeon.depth = -1;

		Bundle badges = bundle.getBundle( BADGES );
		if (!badges.isNull()) {
			Badges.loadLocal( badges );
		} else {
			Badges.reset();
		}
		
		QuickSlot.restore( bundle );
		

		String version = bundle.getString( VERSION );
		
		hero = null;
		hero = (Hero)bundle.get( HERO );
		
		QuickSlot.compress();
		
		hero.gold = bundle.getInt( GOLD );
		depth = bundle.getInt( DEPTH );
		
		Statistics.restoreFromBundle( bundle );
		Journal.restoreFromBundle( bundle );

	}
	
	public static void deleteGame( HeroClass cl, boolean deleteLevels ) {
		
		Game.instance.deleteFile( gameFile( cl ) );
		
		if (deleteLevels) {
			int depth = 1;
			while (Game.instance.deleteFile( Utils.format( depthFile( cl ), depth ) )) {
				depth++;
			}
		}
		
		GamesInProgress.delete( cl );
	}
	
	public static Bundle gameBundle( String fileName ) throws IOException {
		
		InputStream input = Game.instance.openFileInput( fileName );
		Bundle bundle = Bundle.read( input );
		input.close();
		
		return bundle;
	}

	public static void win( String desc ) {
		
		hero.belongings.identify();
		
		if (challenges != 0) {
			Badges.validateChampion();
		}
		
		resultDescription = desc;
		Rankings.INSTANCE.submit( true );
	}
	
	public static void observe() {

		if (level == null) {
			return;
		}
		GameScene.afterObserve();
	}
	
	private static boolean[] passable = new boolean[Level.LENGTH];
	
	public static int findPath( Char ch, int from, int to, boolean pass[], boolean[] visible ) {
		
		if (Level.adjacent( from, to )) {
			return Actor.findChar( to ) == null && (pass[to] || Level.avoid[to]) ? to : -1;
		}

		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}
		
		return PathFinder.getStep( from, to, passable );
		
	}
	
	public static int flee( Char ch, int cur, int from, boolean pass[], boolean[] visible ) {
		
		if (ch.flying) {
			BArray.or( pass, Level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Level.LENGTH );
		}
		
		for (Actor actor : Actor.all()) {
			if (actor instanceof Char) {
				int pos = ((Char)actor).pos;
				if (visible[pos]) {
					passable[pos] = false;
				}
			}
		}
		passable[cur] = true;
		
		return PathFinder.getStepBack( cur, from, passable );
		
	}

}
