package game.assets.items;

import java.awt.*;
import java.util.Random;

import game.assets.items.item.Item;
import game.system.main.Game;
import game.system.systems.gameObject.GameObject;
import game.enums.ID;
import game.system.systems.gameObject.HasItem;
import game.system.systems.gameObject.Interactable;
import game.textures.Textures;

public class Item_Ground extends GameObject implements Interactable, HasItem {

	private Random r = new Random();

	private game.assets.items.item.Item inventoryItem;

	private int x_diff, y_diff, timer;
	private int lifeTimer = (60 * 60) * 5; // 5 mins till destroyed
	private boolean direction = false;

	private double buffer_x, buffer_y;

	public Item_Ground(int x, int y, int z_index, ID id, game.assets.items.item.Item inventoryItem) {
		super(x, y, z_index, id);
		this.inventoryItem = inventoryItem;
		velY = (r.nextFloat() * 3) - 2;
		velX = (r.nextFloat() * 3) - 2;
		buffer_x = x;
		buffer_y = y;
	}

	public void tick() {
		timer++;

		buffer_x += velX;
		buffer_y += velY;
		x = (int) Math.round(buffer_x);
		y = (int) Math.round(buffer_y);
		double xTarg = 0;
		velX += (xTarg - velX) * (0.1f);
		double yTarg = 0;
		velY += (yTarg - velY) * (0.1f);

		if (timer >= 10) {
			timer = 0;
			if (direction) {
				y_diff++;
				if (y_diff >= 0) {
					direction = !direction;
				}
			} else {
				y_diff--;
				if (y_diff <= -5) {
					direction = !direction;
				}
			}
		}
		lifeTimer--;
		if (lifeTimer <= 0) {
			Game.world.getHandler().findAndRemoveObject(this);
			// Game.handler.removeObject(z_index, this);
		}
	}

	public void render(Graphics g) {
		g.drawImage(Textures.entity_shadow, x, y + 5, null);
		g.drawImage(inventoryItem.getTexture().getTexure(), x + x_diff, y + y_diff, 16, 16, null);
	}

	public Rectangle getSelectBounds() {
		return new Rectangle(x, y, 16, 16);
	}

	public Item getItem() {
		return this.inventoryItem;
	}

	public void interact() {
		Game.world.getInventorySystem().pickupItemToPlayerInv(this);
	}

	public void setX(int x) {
		this.x = x;
		buffer_x = x;
	}

	public void setY(int y) {
		this.y = y;
		buffer_y = y;
	}
}
