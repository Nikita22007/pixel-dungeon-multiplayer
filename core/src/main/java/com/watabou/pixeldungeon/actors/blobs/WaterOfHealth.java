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

import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.Speck;

public class WaterOfHealth extends WellWater {

	private static final String TXT_PROCCED =
		"As you take a sip, you feel your wounds heal completely.";

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );	
		emitter.start( Speck.factory( Speck.HEALING ), 0.5f, 0 );
	}
	
	@Override
	public String tileDesc() {
		return 
			"Power of health radiates from the water of this well. " +
			"Take a sip from it to heal your wounds and satisfy hunger.";
	}
}
