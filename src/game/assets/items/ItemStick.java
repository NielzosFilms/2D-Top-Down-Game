package game.assets.items;

import game.enums.ITEM_ID;
import game.enums.ID;
import game.textures.Textures;

public class ItemStick extends Item {

    public ItemStick(int amount, ITEM_ID itemType) {
        super(amount, itemType);
        this.tex = Textures.placeholder;
        this.itemGround = new ItemGround(0, 0, 1, ID.Item, this);
    }

}
