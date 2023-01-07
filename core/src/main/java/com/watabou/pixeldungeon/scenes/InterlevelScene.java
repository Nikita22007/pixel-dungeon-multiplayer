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
package com.watabou.pixeldungeon.scenes;

import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.network.ParseThread;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndStory;

import java.io.FileNotFoundException;

public class InterlevelScene extends PixelScene {

	private static final float TIME_TO_FADE = 0.3f;
	
	private static final String TXT_DESCENDING	= "Descending...";
	private static final String TXT_ASCENDING	= "Ascending...";
	private static final String TXT_LOADING		= "Loading...";
	private static final String TXT_RESURRECTING= "Resurrecting...";
	private static final String TXT_RETURNING	= "Returning...";
	private static final String TXT_FALLING		= "Falling...";
	
	private static final String ERR_FILE_NOT_FOUND	= "File not found. For some reason.";
	private static final String ERR_GENERIC			= "Something went wrong..."	;

	public static enum Mode {
		DESCEND, ASCEND, CONTINUE, RESURRECT, RETURN, FALL, NONE
	};
	public static Mode mode;

	public static boolean first_decend = false;

	public static int returnDepth;
	public static int returnPos;
	
	public static boolean noStory = false;
	
	public static boolean fallIntoPit;
	public static String customMessage;

	public static boolean reset_level;

	public enum Phase {
		FADE_IN, STATIC, FADE_OUT, NONE
	};
	public static volatile Phase phase;
	private float timeLeft;
	
	private BitmapText message;
	
	private Thread thread;
	private String error = null;
	
	@Override
	public void create() {
		super.create();

		phase=Phase.FADE_IN;
		String text = "";
		switch (mode) {
		case DESCEND:
			text = TXT_DESCENDING;
			break;
		case ASCEND:
			text = TXT_ASCENDING;
			break;
		case CONTINUE:
			text = TXT_LOADING;
			break;
		case RESURRECT:
			text = TXT_RESURRECTING;
			break;
		case RETURN:
			text = TXT_RETURNING;
			break;
		case FALL:
			text = TXT_FALLING;
			break;
		default:
		}

		if (customMessage != null) {
			text = customMessage;
			customMessage = null;
		}

		message = PixelScene.createText( text, 9 );
		message.measure();
		message.x = (Camera.main.width - message.width()) / 2; 
		message.y = (Camera.main.height - message.height()) / 2;
		add( message );

		if (phase == Phase.NONE){
			phase = Phase.FADE_IN;
		}

		timeLeft = TIME_TO_FADE;
		
		thread = new Thread() {
			@Override
			public void run() {
				try {

					switch (mode) {
					case DESCEND:
						descend();
						break;
					case ASCEND:
						ascend();
						break;
					case CONTINUE:
						restore();
						break;
					case RESURRECT:
						resurrect();
						break;
					case RETURN:
						returnTo();
						break;
					case FALL:
						fall();
						break;
					default:
					}

					if ((Dungeon.depth % 5) == 0) {
						Sample.INSTANCE.load( Assets.SND_BOSS );
					}
					
				} catch (FileNotFoundException e) {
					
					error = ERR_FILE_NOT_FOUND;
					
				} catch (Exception e ) {
					
					error = ERR_GENERIC;
					
				}
				
				//if (phase == Phase.STATIC && error == null) {
				//	phase = Phase.FADE_OUT;
			//		timeLeft = TIME_TO_FADE;
			//	}
			}
		};
		thread.start();
	}
	
	@Override
	public void update() {
		super.update();
		if (ParseThread.getActiveThread() != null) {
			ParseThread.getActiveThread().parseIfHasData();
		}
		float p = timeLeft / TIME_TO_FADE;

		if (reset_level){
			resetLevel();
			reset_level = false;
		}

		switch (phase) {
		
		case FADE_IN:
			message.alpha( 1 - p );
			phase = Phase.STATIC;

			break;
			
		case FADE_OUT:
			message.alpha( p );
			if (mode == Mode.CONTINUE || (mode == Mode.DESCEND && Dungeon.depth == 1)) {
				Music.INSTANCE.volume( p );
			}
			if ((timeLeft -= Game.elapsed) <= 0) {
				phase = Phase.NONE;
				Game.switchScene( GameScene.class );
			}
			break;
			
		case STATIC:
			message.alpha( 1 );
			if (error != null) {
				add( new WndError( error ) {
					public void onBackPressed() {
						super.onBackPressed();
						Game.switchScene( StartScene.class );
					};
				} );
				error = null;
			}
			break;
		}
	}

	private void resetLevel(){
		Actor.fixTime();
		Dungeon.level = new SewerLevel();
		Dungeon.level.create();
		Dungeon.init();
	}

	private void descend() throws Exception {

		Actor.fixTime();
		GameLog.wipe();
		if (Dungeon.hero == null) {
			resetLevel();
			SendData.SendHeroClass(StartScene.curClass);
			if (noStory) {
				Dungeon.chapters.add( WndStory.ID_SEWERS );
				noStory = false;
			}
		} else {
		}

	}

	private void fall() throws Exception {
		
		Actor.fixTime();
		GameLog.wipe();
//todo
}
	
	private void ascend() throws Exception {
		Actor.fixTime();
		GameLog.wipe();
	}
	
	private void returnTo() throws Exception {
		
		Actor.fixTime();
		GameLog.wipe();
	}
	
	private void restore() throws Exception {
		Actor.fixTime();
		GameLog.wipe();

	}
	
	private void resurrect() throws Exception {
		Actor.fixTime();
		GameLog.wipe();
	}
	
	@Override
	protected void onBackPressed() {
		// Do nothing
	}
}
