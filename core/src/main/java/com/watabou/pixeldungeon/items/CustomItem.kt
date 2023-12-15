package com.watabou.pixeldungeon.items

import com.watabou.pixeldungeon.actors.hero.Hero
import com.watabou.pixeldungeon.items.bags.CustomBag
import com.watabou.pixeldungeon.network.SendData.SendItemAction
import com.watabou.pixeldungeon.sprites.ItemSprite
import org.json.JSONArray
import org.json.JSONObject

open class CustomItem() : Item() {
    protected var descString: String? = null

    protected var actionsList: ArrayList<String> = ArrayList();

    protected var identified = false;
    protected var maxDurability: Int = 1;
    protected var glowing: ItemSprite.Glowing? = null;

    var showBar: Boolean = false;
    public var ui: UI = UI();

    companion object {
        @JvmStatic
        public fun createItem(obj: JSONObject): CustomItem {
            if (obj.has("size")) {
                return CustomBag(obj)
            }
            return CustomItem(obj)
        }
    }

    protected constructor(obj: JSONObject) : this() {
        cursedKnown = true // todo check it
        update(obj)

    }

    override fun update(obj: JSONObject) {
        val it = obj.keys()
        while (it.hasNext()) {
            val token = it.next()
            when (token) {
                "name" -> {
                    name = obj.getString(token)
                }
                "info" -> {
                    descString = obj.getString(token);
                }
                "image" -> {
                    image = obj.getInt(token);
                }
                "stackable" -> {
                    stackable = obj.getBoolean(token);
                }
                "quantity", "count" -> {
                    quantity = obj.getInt(token)
                }
                "durability" -> {
                    durability = obj.getInt(token)
                }
                "max_durability" -> {
                    maxDurability = obj.getInt(token)
                }
                "level" -> {
                    level = obj.getInt(token)
                }
                "level_known" -> {
                    levelKnown = obj.getBoolean(token)
                }
                "cursed" -> {
                    cursed = obj.getBoolean(token)
                }
                "identified" -> {
                    identified = obj.getBoolean(token)
                }
                "actions" -> {
                    parseActions(obj.getJSONArray(token))
                }
                "default_action" -> {
                    val action: String = obj.getString(token);
                    defaultAction = if (action == "null") {
                        null
                    } else {
                        action
                    };
                }
                "ui" -> {
                    val uiObj = obj.getJSONObject(token);
                    ui = UI(uiObj)
                }
                "show_bar" -> {
                    showBar = obj.getBoolean(token);
                }
                "glowing" -> {
                    if (obj.isNull(token)){
                        glowing = null;
                    }
                    else {
                        val glowingObj = obj.getJSONObject(token);
                        glowing = ItemSprite.Glowing(glowingObj);
                    }
                }
                "sprite_sheet" -> {
                    this.spriteSheet= obj.getString(token);
                }
            }
        }
    }

    private fun parseActions(actionsArr: JSONArray) {
        val actions = ArrayList<String>(actionsArr.length());
        for (i in 0 until actionsArr.length()) {
            val action = actionsArr.getString(i);
            actions.add(action)
        }
        actionsList = actions;
    }

    override fun desc(): String {
        return descString ?: "idk,wtf"
    }

    override fun actions(hero: Hero?): ArrayList<String> {
        return actionsList.clone() as ArrayList<String>
    }

    override fun isIdentified(): Boolean {
        return identified;
    }

    override fun maxDurability(lvl: Int): Int {
        return maxDurability;
    }

    override fun execute(hero: Hero, action: String) {
        SendItemAction(this, hero, action)
    }

    override fun visiblyUpgraded(): Int {
        return level;
    }

    override fun glowing(): ItemSprite.Glowing? {
        return glowing;
    }

    public class UI {
        val topLeft: Label;
        val topRight: Label;
        val bottomRight: Label;

        constructor(obj: JSONObject) {
            val topLeftObj: JSONObject? = if (obj.isNull("top_left")) null else obj.optJSONObject("top_left");
            if (topLeftObj == null) {
                topLeft = Label(null, null, false);
            } else {
                var color: Int? = null;
                if (topLeftObj.has("color") && !topLeftObj.isNull("color")) {
                    color = topLeftObj.optInt("color", 0)
                }
                topLeft = Label(
                    color,
                        if (topLeftObj.isNull("text")) null else
                    topLeftObj.optString("text", ""),
                    topLeftObj.optBoolean("visible", false)
                );
            }

            val topRightObj: JSONObject? = obj.optJSONObject("top_right");
            if (topRightObj == null) {
                topRight = Label(null, null, false);
            } else {
                var color: Int? = null;
                if (topRightObj.has("color") && !topRightObj.isNull("color")) {
                    color = topRightObj.optInt("color", 0)
                }
                topRight = Label(
                    color,
                        if (topRightObj.isNull("text")) null else
                    topRightObj.optString("text", ""),
                    topRightObj.optBoolean("visible", false)
                );
            }

            val bottomRightObj: JSONObject? = obj.optJSONObject("bottom_right");
            if (bottomRightObj == null) {
                bottomRight = Label(null, null, false);
            } else {
                var color: Int? = null;
                if (bottomRightObj.has("color") && !bottomRightObj.isNull("color")) {
                    color = bottomRightObj.optInt("color", 0)
                }
                bottomRight = Label(
                    color,
                        if (bottomRightObj.isNull("text")) null else
                        bottomRightObj.optString("text",""),
                    bottomRightObj.optBoolean("visible", false)
                );
            }
        }

        constructor() {
            topLeft = Label(null, null, false);
            topRight = Label(null, null, false);
            bottomRight = Label(null, null, false);
        }

        public class Label {
            val text: String?;
            val color: Int?;
            val visible: Boolean;

            constructor(color: Int?, text: String?, visible: Boolean) {
                this.text = text;
                this.color = color;
                this.visible = visible;
            }
        }
    }
}