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
package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.Game;
import com.watabou.noosa.Visual;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.PointF;

public class Pushing extends Visual {

	private CharSprite sprite;
	private int from;
	private int to;


	private static final float DELAY = 0.15f;

	private PointF end;

	private float delay;

	public Pushing(CharSprite sprite, int from, int to) {
		super(0, 0, 0, 0);

		this.sprite = sprite;
		this.from = from;
		this.to = to;

		point(sprite.worldToCamera(from));
		end = sprite.worldToCamera(to);

		speed.set(2 * (end.x - x) / DELAY, 2 * (end.y - y) / DELAY);
		acc.set(-speed.x / DELAY, -speed.y / DELAY);

		delay = 0;

		sprite.parent.add(this);
	}

	@Override
	public void update() {
		super.update();

		if ((delay += Game.elapsed) < DELAY) {

			sprite.x = x;
			sprite.y = y;

		} else {

			sprite.point(end);

			killAndErase();
		}
	}
}


