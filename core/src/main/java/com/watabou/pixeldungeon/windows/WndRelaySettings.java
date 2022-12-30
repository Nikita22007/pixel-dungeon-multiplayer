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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.widget.EditText;

import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndRelaySettings extends Window {

    public static final String TXT_CLOSE = "Close";
    ;
    private static final String TXT_USE_CUSTOM_RELAY = "Use custom relay";
    private static final String TXT_SET_RELAY_ADDR = "Custom relay address";
    private static final String TXT_SET_RELAY_PORT = "Custom relay port";

    private static final int WIDTH = 112;
    private static final int BTN_HEIGHT = 20;
    private static final int GAP = 2;

    public WndRelaySettings() {
        super();

        CheckBox btnCustomRelay = new CheckBox(TXT_USE_CUSTOM_RELAY) {
            @Override
            protected void onClick() {
                super.onClick();
                PixelDungeon.useCustomRelay(!PixelDungeon.useCustomRelay());
                Sample.INSTANCE.play(Assets.SND_CLICK);
            }
        };
        btnCustomRelay.setRect(0, 0, WIDTH, BTN_HEIGHT);
        btnCustomRelay.checked(PixelDungeon.useCustomRelay());
        add(btnCustomRelay);

        Button btnCustomRelayAddr = new RedButton(TXT_SET_RELAY_ADDR) {
            @Override
            protected void onClick() {

                hide();
                //GameScene.show( new WndSetServerName() );
                final EditText input = new EditText(PixelDungeon.instance);
                input.setText(PixelDungeon.customRelayAddress());
                PixelDungeon.instance.runOnUiThread(() -> {
                    new AlertDialog.Builder(PixelDungeon.instance)
                            .setTitle(TXT_SET_RELAY_ADDR)
                            .setView(input)
                            .setMessage("Set empty to use default address")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Editable editable = input.getText();
                                    PixelDungeon.customRelayAddress(editable.toString());
                                    // deal with the editable
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                }
                            }).show();
                });
            }
        };
        btnCustomRelayAddr.setRect(0, btnCustomRelay.bottom() + GAP, WIDTH, BTN_HEIGHT);
        add(btnCustomRelayAddr);

        Button btnCustomRelayPort = new RedButton(TXT_SET_RELAY_PORT) {
            @Override
            protected void onClick() {

                //GameScene.show( new WndSetServerName() );
                final EditText input = new EditText(PixelDungeon.instance);
                int i = PixelDungeon.customRelayPort();
                input.setText(String.valueOf(i));
                Group wndParent = parent.parent;
                PixelDungeon.instance.runOnUiThread(() -> {
                    new AlertDialog.Builder(PixelDungeon.instance)
                            .setTitle(TXT_SET_RELAY_PORT)
                            .setView(input)
                            .setMessage("Set 0 to use default port")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Editable editable = input.getText();
                                    String value = editable.toString();
                                    boolean parsed = false;
                                    try {
                                        int portValue = Integer.parseInt(value);
                                        if ((portValue >= 0) && (portValue <= 25565)) {
                                            PixelDungeon.customRelayPort(portValue);
                                            parsed = true;
                                        }
                                    } catch (NumberFormatException ignored) {

                                    }
                                    if (!parsed) {
                                        wndParent.add(new WndError("Incorrect port!"));
                                    }
                                    // deal with the editable
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                }
                            }).show();
                });
                hide();
            }
        };
        btnCustomRelayPort.setRect(0, btnCustomRelayAddr.bottom() + GAP, WIDTH, BTN_HEIGHT);
        add(btnCustomRelayPort);

        Button btnClose = new RedButton(TXT_CLOSE) {
            @Override
            protected void onClick() {
                super.onClick();
                hide();
            }
        };
        btnClose.setRect(0, btnCustomRelayPort.bottom() + GAP, WIDTH, BTN_HEIGHT);
        add(btnClose);
        resize( WIDTH, (int)btnClose.bottom() );
    }
}
