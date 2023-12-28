/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
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

package com.watabou.gltextures;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;

import com.watabou.glwrap.Texture;

public class TextureCache {

	public static Context context;
	public static TextureManagerInterface manager = null;

	private static HashMap<Object,SmartTexture> all = new HashMap<>();
	
	// No dithering, no scaling, 32 bits per pixel
	private static BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
	static {
		bitmapOptions.inScaled = false;
		bitmapOptions.inDither = false;
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
	}

	public static SmartTexture createSolid( int color ) {
		final String key = "1x1:" + color;
		
		if (all.containsKey( key )) {
			
			return all.get( key );
			
		} else {
		
			Bitmap bmp = Bitmap.createBitmap( 1, 1, Bitmap.Config.ARGB_8888 );
			bmp.eraseColor( color );
			
			SmartTexture tx = new SmartTexture( bmp );
			all.put( key, tx );
			
			return tx;
		}
	}
	
	public static SmartTexture createGradient( int width, int height, int... colors ) {
		
		final String key = "" + width + "x" + height + ":" + colors;
		
		if (all.containsKey( key )) {
			
			return all.get( key );
			
		} else {
		
			Bitmap bmp = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
			Canvas canvas = new Canvas( bmp );
			Paint paint = new Paint();
			paint.setShader( new LinearGradient( 0, 0, 0, height, colors, null, TileMode.CLAMP ) );
			canvas.drawPaint( paint );
			
			SmartTexture tx = new SmartTexture( bmp );
			all.put( key, tx );
			return tx;
		}
		
	}
	
	public static void add( Object key, SmartTexture tx ) {
		all.put( key, tx );
	}

	public static SmartTexture get( Object src )
	{
		return get( src, false );
	}
	public static SmartTexture get( Object src, boolean allowsNullBitmap ) {
		
		if (all.containsKey( src )) {
			
			return all.get( src );
			
		} else if (src instanceof SmartTexture) {
			
			return (SmartTexture)src;
			
		} else {
			Bitmap bitmap = getBitmap( src );
			if (allowsNullBitmap)
			{
				if (bitmap == null) {
					return null;
				}
			} else {
				Objects.requireNonNull(bitmap, "Texture: " +src.toString());
			}
			SmartTexture tx = new SmartTexture(bitmap);
			all.put( src, tx );
			return tx;
		}
		
	}
	
	public static void clear() {
		
		for (Texture txt:all.values()) {
			txt.delete();
		}
		all.clear();
		
	}
	
	public static void reload() {
		for (SmartTexture tx:all.values()) {
			tx.reload();
		}
	}
	
	public static Bitmap getBitmap( Object src ) {
		
		try {
			if (src instanceof Integer){
				
				return BitmapFactory.decodeResource(
					context.getResources(), (Integer)src, bitmapOptions );
				
			} else if (src instanceof String) {

				InputStream assetStream = manager.getAssetStream((String)src);
				if (assetStream != null) {
					return BitmapFactory.decodeStream( assetStream
							, null, bitmapOptions);
				}
				return BitmapFactory.decodeStream(
					context.getAssets().open( (String)src ), null, bitmapOptions );
				
			} else if (src instanceof Bitmap) {
				
				return (Bitmap)src;
				
			} else {
				
				return null;
				
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			return null;
			
		}
	}
	
	public static boolean contains( Object key ) {
		return all.containsKey( key );
	}

	public static void reloadFromAssets(){
		Set<Object> keysToReload = new HashSet<Object>();
		Iterator<Map.Entry<Object, SmartTexture>> iterator = all.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Object, SmartTexture> tx = iterator.next();
			if (tx.getKey() instanceof String)
			{
				if (((String) tx.getKey()).startsWith("1x1"))
				{
					continue;
				}
				tx.getValue().delete();
				keysToReload.add(tx.getKey());
				iterator.remove();
			}
		}
		for (Object key: keysToReload) {
			get(key, true);
		}
	}

}
