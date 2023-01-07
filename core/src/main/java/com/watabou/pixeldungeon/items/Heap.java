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
package com.watabou.pixeldungeon.items;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.Collection;
import java.util.LinkedList;

public class Heap implements Bundlable {

	private static final String TXT_MIMIC = "This is a mimic!";
	
	private static final int SEEDS_TO_POTION = 3;
	
	private static final float FADE_TIME = 0.6f;

	public int getCustomImage() {
		return customImage;
	}

	public void setCustomImage(int customImage) {
		if (customImage == this.customImage) {
			return;
		}
		this.customImage = customImage;
		pseudoItem = new Item() {
			public int image() { return getCustomImage(); };
		};
	}

	public enum Type {
		HEAP, 
		FOR_SALE, 
		CHEST, 
		LOCKED_CHEST, 
		CRYSTAL_CHEST,
		TOMB, 
		SKELETON,
		MIMIC,
		HIDDEN
	}
	public Type type = Type.HEAP;
	
	public int pos = 0;
	
	public ItemSprite sprite;
	
	public LinkedList<Item> items = new LinkedList<Item>();

	protected int customImage = -1;
	protected Item pseudoItem = null;
	public boolean showsItem = false;

	public int image() {
		if (getCustomImage() != -1){
			return getCustomImage();
		}
		switch (type) {
		case HEAP:
		case FOR_SALE:
			return size() > 0 ? items.peek().image() : 0;
		case CHEST:
		case MIMIC:
			return ItemSpriteSheet.CHEST;
		case LOCKED_CHEST:
			return ItemSpriteSheet.LOCKED_CHEST;
		case CRYSTAL_CHEST:
			return ItemSpriteSheet.CRYSTAL_CHEST;
		case TOMB:
			return ItemSpriteSheet.TOMB;
		case SKELETON:
			return ItemSpriteSheet.BONES;
		case HIDDEN:
			return ItemSpriteSheet.HIDDEN;
		default:
			return 0;
		}
	}
	
	public ItemSprite.Glowing glowing() {
		return (type == Type.HEAP || type == Type.FOR_SALE) && items.size() > 0 ? items.peek().glowing() : null;
	}

	public int size() {
		return items.size();
	}
	
	public Item pickUp() {
		
		Item item = items.removeFirst();
		if (items.isEmpty()) {
			destroy();
		} else if (sprite != null) {
			sprite.view( image(), glowing() );
		}
		
		return item;
	}
	
	public Item peek() {
		if ((showsItem) || (getCustomImage() == -1) ||  (pseudoItem == null)) {
			return items.peek();
		} else {
			return pseudoItem;
		}


	}
	
	public void drop( Item item ) {
		
		if (item.stackable) {
			
			Class<?> c = item.getClass();
			for (Item i : items) {
				if (i.getClass() == c) {
					i.quantity += item.quantity;
					item = i;
					break;
				}
			}
			items.remove( item );
			
		}

		items.addFirst( item );
		
		if (sprite != null) {
			sprite.view( image(), glowing() );
		}
	}
	
	public void replace( Item a, Item b ) {
		int index = items.indexOf( a );
		if (index != -1) {
			items.remove( index );
			items.add( index, b );
		}
	}

	public static void burnFX( int pos ) {
		CellEmitter.get( pos ).burst( ElmoParticle.FACTORY, 6 );
		Sample.INSTANCE.play( Assets.SND_BURNING );
	}
	
	public static void evaporateFX( int pos ) {
		CellEmitter.get( pos ).burst( Speck.factory( Speck.STEAM ), 5 );
	}
	
	public boolean isEmpty() {
		return items == null || items.size() == 0;
	}
	
	public void destroy() {
		Dungeon.level.heaps.remove( this.pos );
		if (sprite != null) {
			sprite.kill();
		}
		items.clear();
		items = null;
	}

	private static final String POS		= "pos";
	private static final String TYPE	= "type";
	private static final String ITEMS	= "items";
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		pos = bundle.getInt( POS );
		type = Type.valueOf( bundle.getString( TYPE ) );
		items = new LinkedList<Item>( (Collection<Item>) ((Collection<?>) bundle.getCollection( ITEMS )) );
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( POS, pos );
		bundle.put( TYPE, type.toString() );
		bundle.put( ITEMS, items );
	}
	
}
