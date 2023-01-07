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
package com.watabou.pixeldungeon.actors.blobs;

import java.util.Arrays;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.utils.Bundle;

public class Blob extends Actor {
	
	public static final int WIDTH	= Level.WIDTH;
	public static final int HEIGHT	= Level.HEIGHT;
	public static final int LENGTH	= Level.LENGTH;
	
	public int volume = 0;
	
	public int[] cur;
	protected int[] off;
	
	public BlobEmitter emitter;
	
	protected Blob() {
		
		cur = new int[LENGTH];
		off = new int[LENGTH];
		
		volume = 0;
	}
	
	private static final String CUR		= "cur";
	private static final String START	= "start";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		
		if (volume > 0) {
		
			int start;
			for (start=0; start < LENGTH; start++) {
				if (cur[start] > 0) {
					break;
				}
			}
			int end;
			for (end=LENGTH-1; end > start; end--) {
				if (cur[end] > 0) {
					break;
				}
			}
			
			bundle.put( START, start );
			bundle.put( CUR, trim( start, end + 1 ) );
			
		}
	}
	
	private int[] trim( int start, int end ) {
		int len = end - start;
		int[] copy = new int[len];
		System.arraycopy( cur, start, copy, 0, len );
		return copy;
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		int[] data = bundle.getIntArray( CUR );
		if (data != null) {
			int start = bundle.getInt( START );	
			for (int i=0; i < data.length; i++) {
				cur[i + start] = data[i];
				volume += data[i];
			}
		}
		
		if (Level.resizingNeeded) {
			int[] cur = new int[Level.LENGTH];
			Arrays.fill( cur, 0 );
			
			int loadedMapSize = Level.loadedMapSize;
			for (int i=0; i < loadedMapSize; i++) {
				System.arraycopy( this.cur, i * loadedMapSize, cur, i * Level.WIDTH, loadedMapSize );
			}
			
			this.cur = cur;
		}
	}

    public void use( BlobEmitter emitter ) {
		this.emitter = emitter;
	}

	public void seed( int cell, int amount ) {
		cur[cell] += amount;
		volume += amount;
	}
	
	public void clear( int cell ) {
		volume -= cur[cell];
		cur[cell] = 0;
	}

	public void clearBlob( ) {
		Arrays.fill( cur, 0 );
		volume = 0;
	}

	public String tileDesc() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static<T extends Blob> T seed( int cell, int amount, Class<T> type ) {
		assert false;
		return (T) new ConfusionGas();
	}
	public static<T extends Blob> T seed(int id, int cell, int amount, Class<T> type ) {
		try {
			
			T gas = (T)Dungeon.level.blobs.get( type );
			if (gas == null) {
				gas = type.newInstance();
				Dungeon.level.blobs.put( type, gas );
			}
			((T) gas).setId(id);
			gas.seed( cell, amount );
			
			return gas;
			
		} catch (Exception e) {
			PixelDungeon.reportException( e );
			return null;
		}
	}
}
