package com.watabou.pixeldungeon.items.bags

import com.watabou.pixeldungeon.actors.Actor
import com.watabou.pixeldungeon.actors.Char
import com.watabou.pixeldungeon.items.Item
import com.watabou.pixeldungeon.ui.Icons
import com.watabou.pixeldungeon.utils.GLog
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class CustomBag(obj: JSONObject) : Bag(obj) {
    public var icon: Icons = Icons.BACKPACK;

    init {
        cursedKnown = true // todo check it
        size = obj.getInt("size");
        if (obj.has("owner")) {
            owner = Actor.findById(obj.getInt("owner")) as Char?
        }
        if (obj.has("items")) {
            addItemsFromJSONArray(obj.getJSONArray("items"))
        }
        if (obj.has("icon")) {
            try {
                Icons.valueOf(obj.getString("items").uppercase(Locale.ENGLISH))
            } catch (e: RuntimeException) {
                GLog.n("incorrect icon: " + e.message);
                e.printStackTrace();
            }
        }
    }

    private fun addItemsFromJSONArray(arr: JSONArray) {
        for (i in 0 until arr.length()) {
            val itemObj = arr.getJSONObject(i);
            val item: Item = createItem(itemObj)
            items.add(item)
        }
    }
}