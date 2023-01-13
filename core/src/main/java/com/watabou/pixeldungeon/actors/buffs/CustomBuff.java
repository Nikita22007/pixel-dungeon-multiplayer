package com.watabou.pixeldungeon.actors.buffs;

import androidx.annotation.NonNull;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

import static com.watabou.pixeldungeon.Dungeon.hero;

public class CustomBuff extends Buff{
    private int icon = 0;

    private String desc = "unknown";

    public CustomBuff(@NonNull JSONObject obj) throws JSONException {
        buff_id = obj.getInt("id");
        update(obj);
    }

    public void update(@NonNull JSONObject obj) throws JSONException {
        setIcon(obj.optInt("icon", icon));
        setDesc(obj.optString("desc", desc));
        if (target == hero) {
            BuffIndicator.refreshHero();
        }
    }

    public int icon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setDesc(@NotNull String desc) {
        this.desc = desc;
    }

    @NonNull
    @Override
    public String toString()  {
        return desc;
    }

}
