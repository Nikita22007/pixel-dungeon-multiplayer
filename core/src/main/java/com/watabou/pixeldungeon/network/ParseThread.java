package com.watabou.pixeldungeon.network;

import android.util.Log;

import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.CustomBuff;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.CustomMob;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.effects.CheckedCell;
import com.watabou.pixeldungeon.effects.DeathRay;
import com.watabou.pixeldungeon.effects.Degradation;
import com.watabou.pixeldungeon.effects.Enchanting;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.FloatingText;
import com.watabou.pixeldungeon.effects.Lightning;
import com.watabou.pixeldungeon.effects.MagicMissile;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.effects.particles.BlastParticle;
import com.watabou.pixeldungeon.effects.particles.EarthParticle;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.effects.particles.EnergyParticle;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.effects.particles.FlowParticle;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.effects.particles.SacrificialParticle;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.effects.particles.SmokeParticle;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.effects.particles.WebParticle;
import com.watabou.pixeldungeon.effects.particles.WindParticle;
import com.watabou.pixeldungeon.effects.particles.WoolParticle;
import com.watabou.pixeldungeon.items.CustomItem;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.bags.CustomBag;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.levels.SewerLevel;
import com.watabou.pixeldungeon.plants.CustomPlant;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.GooSprite;
import com.watabou.pixeldungeon.sprites.HeroCustomSprite;
import com.watabou.pixeldungeon.sprites.HeroSprite;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.sprites.RatSprite;
import com.watabou.pixeldungeon.ui.Banner;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.ui.SpecialSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.PointF;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static com.watabou.pixeldungeon.Dungeon.hero;
import static com.watabou.pixeldungeon.Dungeon.level;
import static com.watabou.pixeldungeon.network.Client.disconnect;
import static com.watabou.pixeldungeon.scenes.GameScene.updateMap;
import static com.watabou.pixeldungeon.utils.Utils.ToPascalCase;
import static java.lang.Thread.sleep;

public class ParseThread implements Callable<String> {

    @NotNull
    private final BufferedReader reader;
    @NotNull
    private final Socket socket;
    private static ParseThread activeThread;
    @NotNull
    private FutureTask<String> jsonCall;

    public ParseThread(InputStreamReader readStream, Socket socket) {
        this(new BufferedReader(readStream), socket);
    }

    public ParseThread(@NotNull BufferedReader readStream, @NotNull Socket socket) {
        this.socket = socket;
        this.reader = readStream;
        activeThread = this;
        updateTask();
    }

    public static ParseThread getActiveThread() {
        if (activeThread == null) {
            return null;
        }
        if ((activeThread.socket == null) || (activeThread.socket.isClosed())) {
            return null;
        }
        return activeThread;
    }

    protected void updateTask() {
        if ((jsonCall == null) || (jsonCall.isDone())) {
            jsonCall = new FutureTask<String>(this);
            new Thread(jsonCall).start();
        }
    }

    @Override
    public String call() {
        if (socket.isClosed()) {
            return null;
        }
        try {
            return reader.readLine();
        } catch (IOException e) {
            Log.e("ParseThread", e.getMessage());
            return null;
        }
    }

    public void parseIfHasData() {
        if (InterlevelScene.phase == InterlevelScene.Phase.FADE_OUT) {
            return;
        }
        if (jsonCall.isCancelled()) {
            disconnect();
            return;
        }
        if (!jsonCall.isDone()) {
            return;
        }
        try {
            String json = jsonCall.get();
            updateTask();
            parse(json);
        } catch (IOException e) {
            GLog.n(e.getMessage());
            disconnect();
            return;
        } catch (InterruptedException e) {
            // disconnect will be upper
            return;
        } catch (JSONException e) {
            Log.w("parsing", e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            {
                Log.w("parsing", e.getMessage());
                disconnect();
                return;
            }
        }
    }

    protected static void returnToMainScreen() {
        Log.i("ParseThread", "parsing stopped");
        PixelDungeon.switchScene(
                TitleScene.class,
                new Game.SceneChangeCallback() {
                    @Override
                    public void beforeCreate() {

                    }

                    @Override
                    public void afterCreate() {
                        PixelDungeon.scene().add(new WndError("Disconnected"));
                    }
                }

        );
    }

    private void parse(String json) throws IOException, JSONException, InterruptedException {
        if (json == null)
            throw new IOException("EOF");
        JSONObject data;
        try {
            data = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Parsing", "json: " + json);
            return;
        }
        if (com.watabou.pixeldungeon.BuildConfig.DEBUG) {
            //Log.i("Parsing", data.toString(4));
        }
        //Log.w("data", data.toString(4));
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case "server_actions": {
                    parseServerActions(data.getJSONArray(token));
                    break;
                }
                        /*case Codes.SERVER_FULL: {
                            PixelDungeon.switchScene(TitleScene.class);
                            // TODO   PixelDungeon.scene().add(new WndError("Server full"));
                            return;
                        }*/
                //level block
                case "map": {
                    parseLevel(data.getJSONObject(token));
                    break;
                }
                //UI block
                case "interlevel_scene": {
                    //todo can cause crash
                    JSONObject ilsObj = data.getJSONObject(token);
                    if (ilsObj.has("state")) {
                        String stateName = data.getJSONObject(token).getString("state").toUpperCase();
                        InterlevelScene.Phase phase = InterlevelScene.Phase.valueOf(stateName);
                        InterlevelScene.phase = phase;
                    }
                    if (ilsObj.has("type")) {
                        String modeName = ilsObj.getString("type").toUpperCase(Locale.ENGLISH);
                        if (modeName.equals("CUSTOM")) {
                            modeName = "NONE";
                        }
                        InterlevelScene.Mode mode = InterlevelScene.Mode.valueOf(modeName);
                        InterlevelScene.mode = mode;
                    }

                    InterlevelScene.reset_level = ilsObj.optBoolean("reset_level");

                    if (ilsObj.has("message")) {
                        InterlevelScene.customMessage = ilsObj.getString("message");
                    }
                    if (!(Game.scene() instanceof InterlevelScene)) {
                        if (!((Game.scene() instanceof GameScene) && (InterlevelScene.phase == InterlevelScene.Phase.FADE_OUT))) {
                            Game.switchScene(InterlevelScene.class);
                        }
                    }
                    break;
                }
                case "actors": {
                    parseActors(data.getJSONArray(token));
                    break;
                }
                case "buffs": {
                    parseBuffs(data.getJSONArray(token));
                    break;
                }
                case "hero": {
                    parseHero(data.getJSONObject(token));
                    break;
                }
                case "actions": {
                    parseActions(data.getJSONArray(token));
                    break;
                }
                case "messages": {
                    parseMessages(data.getJSONArray(token));
                    break;
                }
                case "inventory": {
                    parseInventory(data.getJSONObject(token));
                    break;
                }
                case "heaps": {
                    try {
                        JSONArray heaps = data.getJSONArray(token);
                        for (int i = 0; i < heaps.length(); i++) {
                            parseHeap(heaps.getJSONObject(i));
                        }
                        break;
                    } catch (JSONException e) {
                        Log.e("parseThread", String.format("incorrect heap array. Ignored. Exception: %s ", e.getMessage()));
                    }
                    break;
                }
                case "window": {
                    parseWindow(data.getJSONObject(token));
                    break;
                }
                case "ui": {
                    parseUI(data.getJSONObject(token));
                    break;
                }
                case "plants": {
                    parsePlants(data.getJSONArray(token));
                    break;
                }
                default: {
                    GLog.h("Incorrect packet token: \"%s\". Ignored", token);
                    continue;
                }
            }
        }

    }

    private void parsePlants(JSONArray plantsArray) {
        for (int i = 0; i < plantsArray.length(); i++) {
            JSONObject plantObject = plantsArray.optJSONObject(i);
            if (plantObject == null) {
                Log.e("Parse Thread", "malformed plant.");
                continue;
            }
            try {
                if (plantObject.isNull("plant_info")) {
                    if (level == null) {
                        continue;
                    }
                    if (level.plants == null) {
                        continue;
                    }
                    Plant plant = level.plants.get(plantObject.getInt("pos"));
                    if (plant != null) {
                        plant.wither();
                    }
                    continue;
                }
                JSONObject plantInfo = plantObject.optJSONObject("plant_info");
                Plant plant = new CustomPlant(
                        plantInfo.optInt("sprite_id"),
                        plantObject.getInt("pos"),
                        plantInfo.optString("name", "unknown"),
                        plantInfo.optString("desc", "unknown")
                );
                level.plant(plant, plantObject.getInt("pos"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseUI(JSONObject uiObject) {
        if (uiObject.has("depth")) {
            Dungeon.depth = uiObject.optInt("depth");
        }
        if (uiObject.has("iron_keys_count") || uiObject.has("iron_key_count")) {
            IronKey.curDepthQuantity = uiObject.optInt("iron_keys_count", uiObject.optInt("iron_key_count", -20));
        }
        if (uiObject.has("resume_button_visible")) {
            hero.resume_button_visible = uiObject.optBoolean("resume_button_visible");
        }
        if (uiObject.has("cell_listener_prompt")) {
            GameScene.defaultCellListener.setCustomPrompt(uiObject.optString("cell_listener_prompt", null));
        }
    }

    private void parseWindow(JSONObject windowObj) {
        try {
            int id = windowObj.getInt("id");
            String type = windowObj.getString("type");
            JSONObject args = windowObj.optJSONObject("params");
            if (args == null) {
                args = windowObj.optJSONObject("args");
            }
            switch (type) {
                case "message":
                case "wnd_message": {
                    GameScene.show(new WndMessage(id, args.getString("text")));
                    break;
                }
                case "option":
                case "wnd_option": {
                    JSONArray optionsArr = args.getJSONArray("options");
                    String[] options = new String[optionsArr.length()];
                    for (int i = 0; i < optionsArr.length(); i += 1) {
                        options[i] = optionsArr.getString(i);
                    }
                    GameScene.show(new WndOptions(id, args.getString("title"), args.getString("message"), options));
                    break;
                }
                case "quest":
                case "wnd_quest": {
                    JSONArray optionsArr = args.getJSONArray("options");
                    String[] options = new String[optionsArr.length()];
                    for (int i = 0; i < optionsArr.length(); i += 1) {
                        options[i] = optionsArr.getString(i);
                    }
                    String title = args.getString("title");
                    String text = args.getString("text");
                    CharSprite sprite = spriteFromClass(spriteClassFromName(ToPascalCase(args.getString("sprite")), true));
                    GameScene.show(new WndQuest(id, sprite, title, text, options));
                    break;
                }
                case "bag":
                case "wnd_bag": {
                    String title = args.getString("title");
                    boolean has_listener = args.getBoolean("has_listener");
                    JSONArray allowed_items = args.optJSONArray("allowed_items");
                    JSONArray last_bag_path = args.optJSONArray("last_bag_path"); // todo
                    GameScene.show(new WndBag(id, hero.belongings.backpack, has_listener, allowed_items, title));
                    break;
                }
                default: {
                    Log.e("parse_window", String.format("incorrect window type: %s", type));
                }
            }
        } catch (JSONException e) {
            Log.e("parse_window", String.format("bad_window. %s", e.getMessage()));
        } catch (NullPointerException e) {
            Log.e("parse_window", String.format("bad_window. %s", e.getMessage()));
        }
    }

    private void parseServerActions(JSONArray server_actions_arr) {
        JSONObject debug_action = null;
        for (int i = 0; i < server_actions_arr.length(); i += 1) {
            try {
                debug_action = server_actions_arr.getJSONObject(i);
                parseServerAction(server_actions_arr.getJSONObject(i));
                debug_action = null;
            } catch (JSONException e) {
                String message;
                if (debug_action == null) {
                    message = String.format("can't get action with id:  %d", i);
                } else {
                    try {
                        message = String.format("malformed_action:  %s", debug_action.toString(2));
                    } catch (JSONException e1) {
                        message = String.format("malformed_action. Can't get string. Exception: %s", e1.getMessage());
                    }
                }
                message += String.format("Exception: %s", e.getMessage());
                Log.e("parse_server_actions", message);
            }
        }
    }

    private void parseServerAction(JSONObject action_object) throws JSONException {
        switch (action_object.getString("type")) {
            case "reset_level": {
                level = new SewerLevel();
                level.create();
                break;
            }
            default:
                Log.e("parse_server_actions", String.format("unknown_action  %s", action_object.getString("type")));
        }
    }

    private void parseHeap(JSONObject heapObj) {
        try {
            if (level == null) {
                Log.e("ParseHeap", "level == null");
                return;
            }
            if (level.heaps == null) {
                Log.e("ParseHeap", "level.heaps == null");
                return;
            }
            int pos = heapObj.getInt("pos");
            Heap heap = level.heaps.get(pos, null);
            JSONObject visibleItemObj = heapObj.optJSONObject("visible_item");
            if (heap != null) {
                level.heaps.remove(pos);
                heap.destroy();
            }

            if (visibleItemObj == null) {
                return;
            }
            level.drop(CustomItem.createItem(visibleItemObj), pos);
            level.heaps.get(pos).setCustomImage(heapObj.optInt("visible_sprite", -1));
            level.heaps.get(pos).showsItem = heapObj.optBoolean("show_item", false);
        } catch (JSONException e) {
            Log.e("parse heap", String.format("bad heap. Exception: %s", e.getMessage()));
        }
    }

    private void parseMessages(JSONArray messages) {
        Scene scene = Game.scene();
        if (!(scene instanceof GameScene)) {
            return;
        }
        GameLog log = ((GameScene) scene).getGameLog();
        for (int i = 0; i < messages.length(); i++) {
            try {
                JSONObject messageObj = messages.getJSONObject(i);
                if (messageObj.has("color")) {
                    log.WriteMessage(messageObj.getString("text"), messageObj.getInt("color"));
                } else {
                    log.WriteMessageAutoColor(messageObj.getString("text"));
                }
            } catch (JSONException e) {
                Log.w("ParseThread", "Incorrect message");
            }
        }
    }

    private void parseInventory(JSONObject inv) {
        if (inv.has("backpack")) {
            try {
                hero.belongings.backpack = new CustomBag(inv.getJSONObject("backpack"));
            } catch (JSONException e) {
                Log.w("ParseThread", "Can't parse backpack");
            }
        }
        if (inv.has("special_slots")) {
            JSONArray slotsArr;
            try {
                slotsArr = inv.getJSONArray("special_slots");
            } catch (JSONException ignored) {
                assert false : "wtf";
                slotsArr = new JSONArray();
            }
            try {
                for (int i = 0; i < slotsArr.length(); i++) {
                    JSONObject slotObj = slotsArr.getJSONObject(i);
                    SpecialSlot slot = new SpecialSlot();
                    if (slotObj.has("id")) {
                        slot.id = slotObj.getInt("id");
                    }
                    if (slotObj.has("sprite")) {
                        slot.sprite = slotObj.getString("sprite");
                    }
                    if (slotObj.has("image_id")) {
                        slot.image_id = slotObj.getInt("image_id");
                    }
                    if (slotObj.has("item")) {
                        if (slotObj.isNull("item")) {
                            slot.item = null;
                        } else {
                            slot.item = CustomItem.createItem(slotObj.getJSONObject("item"));
                        }
                    }
                    hero.belongings.updateSpecialSlot(slot);
                }
            } catch (JSONException e) {
                Log.w("ParseThread", "Can't parse slot");
            }
        }
    }

    protected void parseSpriteAction(JSONObject actionObj) throws JSONException {
        String action = actionObj.getString("action");
        int actorID = actionObj.getInt("actor_id");
        Actor actor = Actor.findById(actorID);
        if (actor == null) {
            GLog.h("can't resolve actor");
            return;
        }
        CharSprite sprite = ((Char) actor).sprite;
        if (sprite == null) {
            GLog.h("actor " + actorID + "has null sprite");
            return;
        }

        switch (action) {
            case "idle": {
                sprite.idle();
                break;
            }
            case "place": {
                int to = actionObj.getInt("to");
                sprite.place(to);
                break;
            }
            case "run":
            case "move": {
                int from = actionObj.getInt("from");
                int to = actionObj.getInt("to");
                Char ch = (Char) actor;
                if (ch.pos != from) {
                    GLog.h("from != pos. ID:" + actorID);
                }
                ch.move(to);
                if (ch instanceof Mob) {
                    ((Mob) ch).moveSprite(from, to);
                } else {
                    sprite.move(from, to);
                }
                break;
            }
            case "operate": {
                sprite.operate(actionObj.getInt("to"));
                break;
            }
            case "attack": {
                sprite.attack(actionObj.getInt("to"));
                break;
            }
            case "zap": {
                sprite.zap(actionObj.getInt("to"));
                break;
            }
            case "jump": {
                sprite.jump(actionObj.getInt("from"), actionObj.getInt("to"), () -> {
                });
                break;
            }
            case "read": {
                sprite.read();
                break;
            }
            case "die": {
                sprite.die();
                break;
            }
            case "flash": {
                sprite.flash((float) actionObj.optDouble("flash_time", CharSprite.FLASH_INTERVAL));
                break;
            }
            case "turn": {
                sprite.turnTo(actionObj.getInt("from"), actionObj.getInt("to"));
                break;
            }
            case "alpha_tweener": {
                sprite.alpha((float) actionObj.getDouble("start_alpha"));
                sprite.parent.add(new AlphaTweener(
                        sprite,
                        (float) actionObj.getDouble("target_alpha"),
                        (float) actionObj.getDouble("interval")
                ));
                break;
            }
            default:
                GLog.n("Unexpected action: " + action + ". ID: " + actorID);
        }
    }

    protected void parseActions(@NotNull JSONArray actions) {
        for (int i = 0; i < actions.length(); i++) {
            JSONObject actionObj;
            try {
                actionObj = actions.getJSONObject(i);
            } catch (JSONException e) {
                Log.wtf("ParseActions", "can't get action from array. " + e.toString());
                e.printStackTrace();
                continue;
            }
            String type = actionObj.optString("action_type");
            try {
                switch (type) {
                    case ("sprite_action"): {
                        parseSpriteAction(actionObj);
                        break;
                    }
                    case ("add_item_to_bag"): {
                        parse_update_bag_action(actionObj);
                        break;
                    }
                    case ("show_status"): {
                        parseShowStatusAction(actionObj);
                        break;
                    }
                    case ("degradation"): {
                        parseDegradationAction(actionObj);
                        break;
                    }
                    case ("visual_show_banner"): {
                        parseBannerShowAction(actionObj);
                        break;
                    }
                    case ("lightning_visual"): {
                        parseLightningVisualAction(actionObj);
                        break;
                    }
                    case ("death_ray_centered_visual"): {
                        parseDeathRayCenteredVisualAction(actionObj);
                        break;
                    }
                    case ("wound_visual"): {
                        parseWoundVisualAction(actionObj);
                        break;
                    }
                    case ("ripple_visual"): {
                        parseRippleVisualAction(actionObj);
                        break;
                    }
                    case ("missile_sprite_visual"): {
                        parseMissileSpriteVisualAction(actionObj);
                        break;
                    }
                    case ("checked_cell_visual"): {
                        if (Dungeon.visible[actionObj.getInt("pos")]) {
                            GameScene.effect(new CheckedCell(actionObj.getInt("pos")));
                        }
                        break;
                    }
                    case ("play_sample"): {
                        Sample.INSTANCE.play(actionObj);
                        break;
                    }
                    case ("load_sample"): {
                        Sample.INSTANCE.load(actionObj.getJSONArray("samples"));
                        break;
                    }
                    case ("unload_sample"): {
                        Sample.INSTANCE.unload(actionObj.getString("sample"));
                        break;
                    }
                    case ("shake_camera"): {
                        Camera.main.shake((float) actionObj.getDouble("magnitude"), (float) actionObj.getDouble("duration"));
                        break;
                    }
                    case ("enchanting_visual"): {
                        int targetCharId = actionObj.getInt("target");
                        Actor actor = Actor.findById(targetCharId);
                        if (!(actor instanceof Char)) {
                            GLog.n("Enchanting: Can't find char with id " + targetCharId + ". Ignored");
                            break;
                        }
                        Item item = CustomItem.createItem(actionObj.getJSONObject("item"));
                        com.watabou.pixeldungeon.effects.Enchanting.show((Char) actor, item);
                    }
                    case ("flare_visual"): {
                        PointF position;
                        if (actionObj.has("pos")) {
                            position = DungeonTilemap.tileCenterToWorld(
                                    actionObj.getInt("pos")
                            );
                        } else {
                            position = new PointF(
                                    (float) actionObj.getDouble("position_x"),
                                    (float) actionObj.getDouble("position_y")
                            );
                        }

                        Flare flare = new Flare(
                                actionObj.getInt("rays"),
                                (float) actionObj.getDouble("radius")
                        );
                        flare.angle = (float) actionObj.optDouble("angle", 45);
                        flare.angularSpeed = (float) actionObj.optDouble("angular_speed", 180);
                        flare.color(actionObj.getInt("color"), actionObj.optBoolean("light_moode", true));
                        GameScene.showFlare(flare, position, (float) actionObj.getDouble("duration"));
                        break;
                    }
                    case ("emitter_visual"): {
                        parseEmitterVisualAction(actionObj);
                        break;
                    }
                    default:
                        GLog.h("unknown action type " + type + ". Ignored");
                }
            } catch (JSONException e) {
                GLog.n("Incorrect action ( " + type + "). Ignored");
            }
        }
    }

    private void parse_update_bag_action(JSONObject actionObj) throws JSONException {
        if (!actionObj.has("slot") ||
                !actionObj.has("update_mode") ||
                (!actionObj.has("item") && actionObj.getString("update_mode").equals("remove"))
        ) {
            Log.w("ParseActions", "bad \"add_item_to_bag\" action");
            return;
        }
        List<Integer> slot = new ArrayList<Integer>(2);
        {
            JSONArray slotArr = actionObj.getJSONArray("slot");
            for (int j = 0; j < slotArr.length(); j++) {
                slot.add(slotArr.getInt(j));
            }
        }
        Belongings belongings = hero.belongings;
        Bag stuff = hero.belongings.backpack;
        String update_mode = actionObj.optString("update_mode");
        switch (update_mode) {
            case ("replace"):
            case ("add"):
            case ("place"): {
                JSONObject itemObj = actionObj.optJSONObject("item");
                Item item = itemObj != null ? CustomItem.createItem(actionObj.getJSONObject("item")) : null;
                if ((slot.size() == 1) && (slot.get(0) < 0)) {
                    hero.belongings.specialSlots.get(-slot.get(0) - 1).item = item;
                } else {
                    if (item != null) {
                        item.addTobag(stuff, slot, update_mode.equals("replace"));
                    } else {
                        stuff.remove(slot);
                    }
                }
                break;
            }
            case ("update"): {
                belongings.get(slot).update(actionObj.getJSONObject("item"));
                break;
            }
            case ("remove"): {
                belongings.remove(slot);
                break;
            }
            default:
                Log.w("ParseThread", "Unexpected item update mode: " + update_mode);
                return;
        }
        QuickSlot.refresh();
    }

    private void parseShowStatusAction(JSONObject actionObj) throws JSONException {
        float x = (float) actionObj.getDouble("x");
        float y = (float) actionObj.getDouble("y");
        Integer key = actionObj.has("key") ? actionObj.getInt("key") : null;
        String text = actionObj.getString("text");
        int color = actionObj.getInt("color");
        boolean ignore_position = actionObj.optBoolean("ignore_position", true);
        if ((key != null) && ignore_position) {
            Char ch = Actor.findChar(key);
            if ((ch != null) && (ch.sprite != null)) {
                ch.sprite.showStatus(color, text);
                return;
            }
        }
        if (key == null) {
            FloatingText.show(x, y, text, color);
        } else {
            FloatingText.show(x, y, key, text, color);
        }
    }

    private void parseDegradationAction(JSONObject actionObj) {
        try {
            PointF point = new PointF((float) actionObj.getDouble("position_x"), (float) actionObj.getDouble("position_y"));
            JSONArray array = actionObj.getJSONArray("matrix");
            int[] matrix = new int[array.length()];
            for (int i = 0; i < matrix.length; i++) {
                matrix[i] = array.getInt(i);
            }
            int color = actionObj.optInt("color", Degradation.Speck.COLOR);
            GameScene.add(new Degradation(point, matrix, color));
        } catch (JSONException e) {
            GLog.n("Incorrect degradation action " + e.getMessage());
        }
    }

    private void parseBannerShowAction(JSONObject actionObj) {
        try {
            BannerSprites.Type bannerType = BannerSprites.Type.valueOf(actionObj.getString(actionObj.getString("banner").toUpperCase()));

            Banner banner = new Banner(BannerSprites.get(bannerType));
            banner.show(actionObj.getInt("color"), (float) actionObj.getDouble("fade_time"), (float) actionObj.getDouble("fade_time"));
            GameScene.showBannerStatic(banner);
        } catch (JSONException e) {
            GLog.n("Incorrect BannerShowAction action " + e.getMessage());
        }
    }


    private void parseLightningVisualAction(JSONObject actionObj) {
        try {
            JSONArray cellsJson = actionObj.getJSONArray("cells");
            int[] cells = new int[cellsJson.length()];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = cellsJson.getInt(i);
            }
            GameScene.addGroup(new Lightning(cells, cells.length, null));

        } catch (JSONException e) {
            GLog.n("Incorrect LightningVisualAction action " + e.getMessage());
        }
    }

    private void parseDeathRayCenteredVisualAction(JSONObject actionObj) {
        try {
            GameScene.effect(new DeathRay(actionObj.getInt("start"), actionObj.getInt("stop"), (float) actionObj.getDouble("duration")));
        } catch (JSONException e) {
            GLog.n("Incorrect DeathRayCenteredVisualAction action " + e.getMessage());
        }
    }

    private void parseWoundVisualAction(JSONObject actionObj) {
        try {
            Wound.hitWithTimeToFade(actionObj.getInt("pos"), (float) actionObj.getDouble("duration"));
        } catch (JSONException e) {
            GLog.n("Incorrect WoundVisualAction action " + e.getMessage());
        }
    }

    private void parseRippleVisualAction(JSONObject actionObj) {
        try {
            GameScene.ripple(actionObj.getInt("pos"));
        } catch (JSONException e) {
            GLog.n("Incorrect RippleVisualAction action " + e.getMessage());
        }
    }

    private void parseMissileSpriteVisualAction(JSONObject actionObj) {
        try {
            MissileSprite.show(actionObj);
        } catch (JSONException e) {
        }
    }

    private void parseEmitterVisualAction(JSONObject actionObj) {
        try {
            Char target = null;

            boolean fillTarget = true;
            PointF position = null;
            PointF shift = null;
            float width;
            float height;
            float interval;
            int quantity;

            Emitter.Factory factory = null;

            if (actionObj.has("target_char")) {
                fillTarget = actionObj.optBoolean("fill_target", true);
                int targetCharId = actionObj.getInt("target_char");
                Actor targetActor = Actor.findById(targetCharId);
                if (targetActor instanceof Char) {
                    target = (Char) targetActor;
                    if (!target.sprite.visible) {
                        return;
                    }
                } else {
                    GLog.n("Incorrect EmitterVisualAction action: target is not char");
                }
            }

            if (actionObj.has("pos")) {
                if (!Dungeon.visible[actionObj.getInt("pos")]) {
                    return;
                }
                position = DungeonTilemap.tileToWorld(actionObj.getInt("pos"));
            } else if (actionObj.has("position_x")) {
                position = new PointF(
                        (float) actionObj.getDouble("position_x"),
                        (float) actionObj.getDouble("position_y")
                );
            }

            if (actionObj.has("shift_x")) {
                shift = new PointF(
                        (float) actionObj.getDouble("shift_x"),
                        (float) actionObj.getDouble("shift_y")
                );
                if (position != null) {
                    if ((shift.x != 0) || (shift.y != 0)) {
                        position.x += shift.x;
                        position.y += shift.y;
                    }
                }
            }

            width = (float) actionObj.getDouble("width");
            height = (float) actionObj.getDouble("height");

            interval = (float) actionObj.getDouble("interval");
            quantity = actionObj.getInt("quantity");

            factory = emitterFactoryFromJSONObject(actionObj.getJSONObject("factory"));
            if (factory == null) {
                return;
            }
            Emitter emitter = GameScene.emitter();
            if (emitter == null) {
                return;
            }
            if ((target == null) && (position == null)) {
                GLog.n("Incorrect EmitterVisualAction action: no any target or position");
                return;
            }
            if ((target != null) && (shift != null)) {
                if ((shift.x != 0) || (shift.y != 0)) {
                    position = new PointF(
                            target.sprite.x + shift.x,
                            target.sprite.y + shift.y
                    );
                    target = null;
                }
            }
            if (target != null) {
                emitter.pos(target.sprite);
            } else {
                emitter.pos(position);
            }
            emitter.width = width;
            emitter.height = height;
            emitter.fillTarget = fillTarget;
            emitter.start(factory, interval, quantity);
        } catch (JSONException e) {
            GLog.n("Incorrect EmitterVisualAction action: " + e.getMessage());
        }
    }

    protected Emitter.Factory emitterFactoryFromJSONObject(JSONObject factoryObj) throws JSONException {
        boolean lightMode = factoryObj.optBoolean("light_mode", false);
        switch (factoryObj.getString("factory_type").toLowerCase(Locale.ENGLISH)) {
            case "blast":
                return BlastParticle.FACTORY;
            case "earth":
                return EarthParticle.FACTORY;
            case "elmo":
                return ElmoParticle.FACTORY;
            case "energy":
                return EnergyParticle.FACTORY;
            case "flame":
                return FlameParticle.FACTORY;
            case "flow":
                return FlowParticle.FACTORY;
            case "leaf":
                return LeafParticle.factory(
                        factoryObj.getInt("first_color"),
                        factoryObj.getInt("second_color")
                );
            case "poison_missile":
                return PoisonParticle.MISSILE;
            case "poison_splash":
                return PoisonParticle.SPLASH;
            case "purple_missile":
                return PurpleParticle.MISSILE;
            case "purple_burst":
                return PurpleParticle.BURST;
            case "sartificial":
                return SacrificialParticle.FACTORY;
            case "shadow_missile":
                return ShadowParticle.MISSILE;
            case "shadow_curse":
                return ShadowParticle.CURSE;
            case "shadow_up":
                return ShadowParticle.UP;
            case "shaft":
                return ShaftParticle.FACTORY;
            case "snow":
                return SnowParticle.FACTORY;
            case "smoke":
                return SmokeParticle.FACTORY;
            case "spark":
                return SparkParticle.FACTORY;
            case "splash":
                return new Splash.SplashFactory(
                        factoryObj.getInt("color"),
                        (float) factoryObj.getDouble("dir"),
                        (float) factoryObj.getDouble("cone")
                );
            case "web":
                return WebParticle.FACTORY;
            case "wind":
                return WindParticle.FACTORY;
            case "wool":
                return WoolParticle.FACTORY;
            case "goo":
                return GooSprite.GooParticle.FACTORY;

            case "speck":
                return Speck.factory(
                        factoryObj.getInt("type"),
                        lightMode
                );

        }
        GLog.n("incorrect factory: " + factoryObj.getString("factory_type"));
        return null;
    }

    protected void parseCell(JSONObject cell) throws JSONException {
        int pos = cell.getInt("position");
        if ((pos < 0) || (pos >= level.LENGTH)) {
            GLog.n("incorrect cell position: \"%s\". Ignored.", pos);
            return;
        }
        for (Iterator<String> it = cell.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case "position": {
                    continue;
                }
                case "id": {
                    level.map[pos] = cell.getInt(token);
                    break;
                }
                case "state": {
                    String state = cell.getString("state");
                    level.visited[pos] = state.equals("visited");
                    level.mapped[pos] = state.equals("mapped");
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in cell. Ignored.", token);
                    break;
                }
            }
        }
    }

    protected void parseLevel(JSONObject levelObj) throws JSONException {
        for (Iterator<String> it = levelObj.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case ("cells"): {
                    JSONArray cells = levelObj.getJSONArray(token);
                    for (int i = 0; i < cells.length(); i++) {
                        JSONObject cell = cells.getJSONObject(i);
                        parseCell(cell);
                    }
                    updateMap();
                    break;
                }
                case "entrance": {
                    level.entrance = levelObj.getInt("entrance");
                    break;
                }

                case "exit": {
                    level.entrance = levelObj.getInt("exit");
                    break;
                }
                case "visible_positions": {
                    JSONArray positions = levelObj.getJSONArray(token);
                    Arrays.fill(Dungeon.visible, false);
                    for (int i = 0; i < positions.length(); i++) {
                        int cell = positions.getInt(i);
                        if ((cell < 0) || (cell >= level.LENGTH)) {
                            GLog.n("incorrect visible position: \"%s\". Ignored.", cell);
                            continue;
                        }
                        Dungeon.visible[cell] = true;
                    }
                    Dungeon.observe();
                    GameScene.setFlag(GameScene.UpdateFlags.AFTER_OBSERVE);
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in level. Ignored.", token);
                    break;
                }
            }
        }
    }


    protected Char parseActorChar(JSONObject actorObj, int ID, Actor actor) throws JSONException {
        Char chr;
        if (actor == null) {
            chr = new CustomMob(ID);
            GameScene.add((Mob) chr);
        } else {
            chr = (Char) actor;
        }
        for (Iterator<String> it = actorObj.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case "id":
                    continue;
                case "erase_old": //todo
                    continue;
                case "type": {
                    continue; // it parsed before
                }
                case "position": {
                    chr.pos = actorObj.getInt(token);
                    break;
                }
                case "hp": {
                    chr.HP = actorObj.getInt(token);
                    break;
                }
                case "max_hp": {
                    chr.HT = actorObj.getInt(token);
                    break;
                }
                case "name": {
                    chr.name = actorObj.getString(token);
                    break;
                }
                case "sprite_name": {
                    CharSprite old_sprite = chr.sprite;
                    Class<? extends CharSprite> new_sprite_class = spriteClassFromName(ToPascalCase(actorObj.getString(token)), chr != hero);
                    if ((old_sprite != null) && (old_sprite.getClass().equals(new_sprite_class))) {
                        break;
                    }
                    CharSprite sprite = spriteFromClass(new_sprite_class);
                    GameScene.updateCharSprite(chr, sprite);
                    break;
                }
                case "animation_name": {
                    assert false : "animation_name";
                    //todo
                    break;
                }
                case "description": {
                    ((Mob) chr).setDesc(actorObj.getString(token));
                    break;
                }
                case "states": {
                    JSONArray statesArr = actorObj.getJSONArray(token);
                    CharSprite sprite = chr.sprite;
                    if (sprite == null) {
                        break;
                    }
                    Set<CharSprite.State> states = sprite.states();
                    Set<CharSprite.State> newStates = new HashSet<>(3);
                    for (int i = 0; i < statesArr.length(); i++) {
                        try {
                            CharSprite.State state = CharSprite.State.valueOf(statesArr.getString(i).toUpperCase());
                            newStates.add(state);
                            if (states.contains(state)) {
                                continue;
                            }
                            sprite.add(state);
                        } catch (IllegalArgumentException e) {
                            GLog.n("Illegal char state: %s", e.getMessage());
                        }
                    }
                    for (CharSprite.State state : states) {
                        if (!newStates.contains(state)) {
                            sprite.remove(state);
                        }
                    }
                    break;
                }
                case "emo": {
                    CharSprite sprite = chr.sprite;
                    if (sprite == null) {
                        break;
                    }
                    JSONObject emoObj = actorObj.getJSONObject(token);
                    sprite.setEmo(emoObj);
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in Actor Char. Ignored.", token);
                    break;
                }
            }
        }
        return chr;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected Class<? extends CharSprite> spriteClassFromName(String spriteName, boolean notHero) {
        String sprite_name = Utils.format("com.watabou.pixeldungeon.sprites.%s", spriteName);
        Class<? extends CharSprite> sprite_class = null;
        CharSprite sprite = null;
        try {
            sprite_class = (Class<? extends CharSprite>) Class.forName(sprite_name);
            if ((sprite_class == HeroSprite.class) && (notHero)) {
                sprite_class = HeroCustomSprite.class;
            }
        } catch (Exception e) {
            GLog.n("Incorrect sprite \"%s\"", sprite_name);
            e.printStackTrace();
        }
        return sprite_class;
    }

    protected CharSprite spriteFromClass(Class<? extends CharSprite> sprite_class) {
        CharSprite sprite = null;
        try {
            sprite = (CharSprite) sprite_class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sprite == null) {
            sprite = new RatSprite();
        }
        return sprite;
    }

    protected void parseActorBlob(JSONObject actorObj, int id, Actor actor) throws JSONException {
        Class blob_class = null;
        if (actor == null) {
            String blob_name = Utils.format("com.watabou.pixeldungeon.actors.blobs.%s", ToPascalCase(actorObj.getString("blob_type")));
            try {
                blob_class = Class.forName(blob_name);
                actor = (Blob) blob_class.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        blob_class = actor.getClass();
        if (blob_class == CustomMob.class) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Blob blob = (Blob) actor;
        blob.clearBlob();
        JSONArray pos_array = actorObj.getJSONArray("positions");
        for (int i = 0; i < pos_array.length(); i += 1) {
            pos_array.get(i);
            GameScene.add(Blob.seed(id, pos_array.getInt(i), 1, blob_class));
        }
    }

    protected void parseActorHero(JSONObject actorObj, int id, Actor actor) throws JSONException {
        if ((actor != null) && !(actor instanceof Hero)) {
            Actor.remove(actor);
            Log.e("ParseThread", Utils.format("Actor is not hero. Deleted. Id:  %d", id));
        }
        if (hero == null) {
            hero = new Hero();
            hero.changeID(id);
        }
        actor = hero;
        actor = parseActorChar(actorObj, id, actor);
        Actor.add(actor); // it has check inside, no more checks
    }


    protected void parseActors(JSONArray actors) throws JSONException {
        for (int i = 0; i < actors.length(); i++) {
            JSONObject actorObj = actors.getJSONObject(i);
            int ID = actorObj.getInt("id");
            boolean erase_old = false;
            if (actorObj.has("erase_old")) {
                erase_old = actorObj.getBoolean("erase_old");
            }
            if (!actorObj.has("type")) {
                GLog.n("Actor does not have type. Ignored");
                continue;
            }
            Actor actor = (erase_old ? null : Actor.findById(ID));
            String type = actorObj.getString("type");
            switch (type) {
                case "remove":
                case "removed":
                case "removing": {
                    if (actor instanceof Char) {
                        Char ch = (Char) actor;
                        ch.destroy();
                    } else {
                        Actor.remove(actor);
                    }
                    break;
                }
                case "char":
                case "character": {
                    parseActorChar(actorObj, ID, actor);
                    break;
                }
                case "hero": {
                    parseActorHero(actorObj, ID, actor);
                    break;
                }
                case "blob": {
                    parseActorBlob(actorObj, ID, actor);
                    break;
                }
                default: {
                    GLog.n("can't resolve actor type: \"" + type + "\". ID: " + ID);
                }
            }
        }
    }

    protected void parseHero(JSONObject heroObj) throws JSONException {
        for (Iterator<String> it = heroObj.keys(); it.hasNext(); ) {
            String token = it.next();
            switch (token) {
                case "actor_id": {
                    hero.changeID(heroObj.getInt(token));
                    break;
                }
                case "strength": {
                    hero.STR = heroObj.getInt(token);
                    break;
                }
                case "lvl": {
                    hero.lvl = heroObj.getInt(token);
                    break;
                }
                case "exp": {
                    hero.exp = heroObj.getInt(token);
                    break;
                }
                case "class": {
                    String className = heroObj.getString(token);
                    className = className.toUpperCase();
                    hero.heroClass = HeroClass.valueOf(className);
                    hero.sprite = new HeroSprite();
                    break;
                }
                case "ready": {
                    if (heroObj.getBoolean(token)) {
                        hero.ready();
                    } else {
                        hero.busy();
                    }
                    break;
                }
                case "gold": {
                    hero.gold = heroObj.getInt(token);
                    break;
                }
                default: {
                    GLog.n("Unexpected token \"%s\" in Hero. Ignored.", token);
                    break;
                }
            }
        }
    }


    private void parseBuffs(JSONArray buffs) {
        for (int i = 0; i < buffs.length(); i++) {
            try {
                JSONObject obj = buffs.getJSONObject(i);
                int id = obj.getInt("id");
                int targetId = obj.optInt("target_id", -1);
                if (targetId == -1) {
                    Buff.detach(id);
                    continue;
                }

                Actor target_actor = Actor.findById(targetId);
                if (!(target_actor instanceof Char)) {
                    Buff.detach(id);
                    continue;
                }
                Char target = (Char) target_actor;

                Buff old_buf = Buff.get(id);
                if ((old_buf instanceof CustomBuff) && (old_buf.target == target)) {
                    ((CustomBuff) old_buf).update(obj);
                    continue;
                }

                CustomBuff buff = new CustomBuff(obj);
                if (!buff.attachTo((Char) Actor.findById(targetId))) {
                    GLog.n("failed to attach buf. Buf id: %d; bug name: %s", buff.buff_id, buff.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

    }
}
