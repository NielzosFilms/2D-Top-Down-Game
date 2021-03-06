package game.assets.items;

import java.awt.*;
import java.util.Random;

import game.assets.entities.player.Player;
import game.assets.items.item.Item;
import game.audio.SoundEffect;
import game.system.helpers.Logger;
import game.system.main.Game;
import game.system.systems.gameObject.Bounds;
import game.system.systems.gameObject.GameObject;
import game.enums.ID;
import game.system.systems.gameObject.Trigger;
import game.textures.Textures;

public class Item_Ground extends GameObject implements Bounds, Trigger {

	private Random r = new Random();

	private Item inventoryItem;

	private int x_diff, y_diff, timer;
	private int lifeTimer = (60 * 60) * 5; // 5 mins till destroyed
	private boolean direction = false;

	private double buffer_x, buffer_y;

	private boolean can_trigger = true;

	public Item_Ground(int x, int y, int z_index, ID id, Item inventoryItem) {
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
			Game.gameController.getHandler().findAndRemoveObject(this);
			// Game.handler.removeObject(z_index, this);
		}
	}

	public void render(Graphics g) {
		g.drawImage(Textures.entity_shadow, x, y + 5, null);
		g.drawImage(inventoryItem.getTexture().getTexure(), x + x_diff, y + y_diff, 16, 16, null);
	}

	public void setX(int x) {
		this.x = x;
		buffer_x = x;
	}

	public void setY(int y) {
		this.y = y;
		buffer_y = y;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(this.x, this.y, 16, 16);
	}

	@Override
	public Rectangle getTopBounds() {
		return null;
	}

	@Override
	public Rectangle getBottomBounds() {
		return null;
	}

	@Override
	public Rectangle getLeftBounds() {
		return null;
	}

	@Override
	public Rectangle getRightBounds() {
		return null;
	}

	@Override
	public boolean canTrigger() {
		return can_trigger;
	}

	@Override
	public void setTriggerActive(boolean triggerActive) {

	}

	@Override
	public boolean triggerCollision() {
		return false;
	}

	@Override
	public void triggered(Player player) {
		Logger.printStackStrace();
		can_trigger = false;
		SoundEffect.inv_pickup_item.play();
		Game.gameController.getPlayer().addItem(this.inventoryItem);
		Game.gameController.getHandler().findAndRemoveObject(this);
	}
}
