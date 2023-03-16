package com.watabou.pixeldungeon.ui;

import com.watabou.pixeldungeon.items.Item;

public class SpecialSlot {

    public int id;
    public String sprite = "items.png";
    public int image_id = 127;
    public Item item = null;

    public void updateWith(SpecialSlot newData) {
        assert(id==newData.id);
        sprite = newData.sprite;
        image_id = newData.image_id;
        item = newData.item;
    }
}
