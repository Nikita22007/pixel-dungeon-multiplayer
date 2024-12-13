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

import android.content.SharedPreferences;

import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public enum Preferences {

	INSTANCE;
	
	public static final String KEY_LANDSCAPE	= "landscape";
	public static final String KEY_IMMERSIVE	= "immersive";
	public static final String KEY_GOOGLE_PLAY	= "google_play";
	public static final String KEY_SCALE_UP		= "scaleup";
	public static final String KEY_MUSIC		= "music";
	public static final String KEY_SOUND_FX		= "soundfx";
	public static final String KEY_ZOOM			= "zoom";
	public static final String KEY_LAST_CLASS	= "last_class";
	public static final String KEY_CHALLENGES	= "challenges";
	public static final String KEY_DONATED		= "donated";
	public static final String KEY_INTRO		= "intro";
	public static final String KEY_BRIGHTNESS	= "brightness";
	public static final String KEY_ONLINE_MODE	= "online_mode";
	public static final String KEY_CUSTOM_RELAY	= "use_custom_relay";
	public static final String KEY_RELAY_ADDR	= "custom_relay_addr";
	public static final String KEY_RELAY_PORT	= "custom_relay_port";
	
	private SharedPreferences prefs;
	
	private SharedPreferences get() {
		if (prefs == null) {
			prefs = Game.instance.getPreferences( Game.MODE_PRIVATE );
		}
		return prefs;
	}
	
	int getInt( String key, int defValue  ) {
		return get().getInt( key, defValue );
	}
	
	boolean getBoolean( String key, boolean defValue  ) {
		return get().getBoolean( key, defValue );
	}
	
	String getString( String key, String defValue  ) {
		return get().getString( key, defValue );
	}
	
	void put( String key, int value ) {
		get().edit().putInt( key, value ).commit();
	}
	
	void put( String key, boolean value ) {
		get().edit().putBoolean( key, value ).commit();
	}
	
	void put( String key, String value ) {
		get().edit().putString( key, value ).commit();
	}
	Bundle heroUUIDs = new Bundle();
	public String heroUUID(String serverUUID) {
		if (serverUUID != null) {
			if (getString("hero_uuids", null) != null && heroUUIDs != null && !heroUUIDs.isNull()) {
				ByteArrayInputStream bais = new ByteArrayInputStream(getString("hero_uuids", null).getBytes());
				try {
					heroUUIDs = Bundle.read(bais);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return heroUUIDs.getString(serverUUID);
		}
		return null;
	}
	public void heroUUID(String serverUUID, String heroUUID) {
		if (serverUUID != null) {
			heroUUIDs.put(serverUUID, heroUUID);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Bundle.write(heroUUIDs, baos);
			put("hero_uuids", baos.toString());
		}
	}
	public void clearUUIDs(){
		heroUUIDs = new Bundle();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Bundle.write(heroUUIDs, baos);
		put("hero_uuids", baos.toString());
	}
}
