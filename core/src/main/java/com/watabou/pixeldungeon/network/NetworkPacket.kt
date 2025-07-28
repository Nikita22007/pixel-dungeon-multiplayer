package com.watabou.pixeldungeon.network

import com.watabou.pixeldungeon.Preferences
import com.watabou.pixeldungeon.actors.hero.Hero
import com.watabou.pixeldungeon.items.CustomItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class NetworkPacket {
    internal enum class CellState {
        VISITED, UNVISITED, MAPPED;

        override fun toString(): String {
            return name.lowercase(Locale.getDefault())
        }
    }

    companion object {
        @JvmField
        var password: String? = null
        @JvmField
        var redirectUUID: String? = null
        const val CELLS = "cells"
        const val MAP = "map"
        const val ACTORS = "actors"
    }

    @JvmField
    var dataRef: AtomicReference<JSONObject> = AtomicReference(JSONObject())

    fun clearData() {
        synchronized(dataRef) { dataRef.set(JSONObject()) }
    }

    fun packAndAddHeroClass(heroClass: String?) {
        synchronized(dataRef) {
            try {
                dataRef.get().put("hero_class", heroClass);
                dataRef.get().put("uuid", Preferences.INSTANCE.heroUUID(ParseThread.serverUUID));
            } catch (ignored: Exception) {
            }
        }
    }

    fun packAndAddCellListenerCell(cell: Int?) {
        synchronized(dataRef) {
            try {
                if (cell == null) {
                    dataRef.get().put("cell_listener", -1)
                } else {
                    dataRef.get().put("cell_listener", cell.toInt())
                }
            } catch (ignored: Exception) {
            }
        }
    }

    fun packAndAddUsedAction(item: CustomItem, action: String, hero: Hero) {
        assert(action !== "")

        val action_obj = JSONObject()
        action_obj.put("action_name", action)
        val slot = hero.belongings.pathOfItem(item) ?: error("slot is null")

        assert(slot.isNotEmpty()) { "slot is empty" }

        action_obj.put("slot", JSONArray(slot))

        synchronized(dataRef) {
            dataRef.get().put("action", action_obj)
        }
    }

    fun packAndAddWindowsResult(
        id: Int,
        pressedButton: Int,
        args: JSONObject? = null
    ) {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("button", pressedButton)
        obj.put("result", args)
        synchronized(dataRef) {
            dataRef.get().put("window", obj)
        }
    }

    fun packAndAddTollbarAction(action: String) {
        val obj = JSONObject()
        obj.put("action_name", action)
        synchronized(dataRef) {
            dataRef.get().put("toolbar_action", obj)
        }
    }

}