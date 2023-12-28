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
package com.watabou.pixeldungeon.windows;

import static com.watabou.pixeldungeon.utils.Utils.ToPascalCase;

import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.items.CustomItem;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.CustomCharSprite;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WndOptions extends Window {

	private static final int WIDTH			= 120;
	private static final int MARGIN 		= 2;
	private static final int BUTTON_HEIGHT	= 20;

	public WndOptions(int id, String title, String message, String... options ) {
		this(title, message, options);
		this.id =  id;
	}
	public WndOptions( String title, String message, String... options ) {
		this(null, null, title, message, options);

	}
	public WndOptions(Image image, Integer titleColor, String title, String message, String... options ) {
		Create(image, titleColor, title, message, options);
	}
	protected void Create(Image image, Integer titleColor, String title, String message, String... options ) {
		float pos;
		if (image == null) {
			BitmapTextMultiline tfTitle = PixelScene.createMultiline(title, 9);
			tfTitle.hardlight(TITLE_COLOR);
			tfTitle.x = tfTitle.y = MARGIN;
			tfTitle.maxWidth = WIDTH - MARGIN * 2;
			tfTitle.measure();
			add(tfTitle);
			pos = tfTitle.y + tfTitle.height() + MARGIN;
		}
		else {
			IconTitle titlebar = new IconTitle();
			titlebar.icon( image );
			titlebar.label( title );
			titlebar.color( titleColor );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );
			pos = titlebar.bottom() + MARGIN;
		}
		BitmapTextMultiline tfMesage = PixelScene.createMultiline( message, 8 );
		tfMesage.maxWidth = WIDTH - MARGIN * 2;
		tfMesage.measure();
		tfMesage.x = MARGIN;
		tfMesage.y = pos;
		add( tfMesage );
		
		pos = tfMesage.y + tfMesage.height() + MARGIN;
		
		for (int i=0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton( options[i] ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			btn.setRect( MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
			add( btn );
			
			pos += BUTTON_HEIGHT + MARGIN;
		}
		
		resize( WIDTH, (int)pos );
	}

    public WndOptions(JSONObject args) throws JSONException {
		Image image = null;
		JSONArray optionsArr = args.getJSONArray("options");
		String[] options = new String[optionsArr.length()];
		for (int i = 0; i < optionsArr.length(); i += 1) {
			options[i] = optionsArr.getString(i);
		}
		String title = args.getString("title");
		int titleColor = args.optInt("title_color", TITLE_COLOR);
		String text = args.getString("message");
		if (args.has("item"))
		{
			image = new ItemSprite(CustomItem.createItem(args.getJSONObject("item")));
		} else if (args.has("sprite_asset")) {
			image = new CustomCharSprite(args.getString("sprite_asset"));
		} else {
			image = CharSprite.spriteFromClass(
					CharSprite.spriteClassFromName(
							ToPascalCase(args.getString("sprite_class")
							), true)
			);
		}
		Create(image, titleColor, title, text, options);
    }

    protected void onSelect( int index ) {
		SendData.sendWindowResult(id, index);
	};
}
