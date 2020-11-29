package game.assets.items.tools;

import game.assets.items.Item;
import game.assets.items.Item_Ground;
import game.enums.ID;
import game.enums.ITEM_ID;
import game.textures.Textures;

public class Tool_WoodenSword extends Item {
    public Tool_WoodenSword(ITEM_ID itemType) {
        super(1, itemType);
        this.tex = Textures.tools.get(1);
        this.itemGround = new Item_Ground(0, 0, 1, ID.Item, this);
        this.setStackable(false);
        this.setDamage(2);
        this.setAttack_speed((int)(60 * 0.25f));
    }

    public boolean place(int x, int y) {
        return false;
    }
}