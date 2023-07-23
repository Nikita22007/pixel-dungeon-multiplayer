/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 * Copyright (C) 2021-2023 Nikita Shaposhnikov
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
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

import org.json.JSONException;
import org.json.JSONObject;

public class MissileSprite extends ItemSprite implements Tweener.Listener {
	
	private Callback callback;
	
	public MissileSprite() {
		super();
		originToCenter();
	}

	public static void show(JSONObject actionObj) throws JSONException {
		MissileSprite sprite = (MissileSprite) GameScene.recycleSprite( MissileSprite.class );
		if (sprite == null){
			return;
		}

		Glowing glowing = null;
		if (!actionObj.isNull("item_glowing")) {
			glowing = new Glowing(actionObj.getJSONObject("item_glowing"));
		}
		sprite.reset(
				actionObj.getInt("from"),
				actionObj.getInt("to"),
				actionObj.getDouble("speed"),
				actionObj.getDouble("angular_speed"),
				actionObj.getDouble("angle"),
				actionObj.getInt("item_image"),
				glowing
		);
	}

	private void reset(int from, int to, double SPEED, double angular_speed, double angle, int image, Glowing glowing) {
		revive();
		
		view( image, glowing );
		
		this.callback = null;

		point( DungeonTilemap.tileToWorld( from ) );
		PointF dest = DungeonTilemap.tileToWorld( to );
		
		PointF d = PointF.diff( dest, point() ); 
		this.speed.set( d ).normalize().scale( (float)SPEED );

		this.angularSpeed = (float)angular_speed;
		this.angle = (float) angle;
		
		PosTweener tweener = new PosTweener( this, dest, d.length() / (float)SPEED );
		tweener.listener = this;
		parent.add( tweener );
	}

	@Override
	public void onComplete( Tweener tweener ) {
		kill();
		if (callback != null) {
			callback.call();
		}
	}
}
