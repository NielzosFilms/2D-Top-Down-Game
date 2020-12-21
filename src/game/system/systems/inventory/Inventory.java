package game.system.systems.inventory;

import game.assets.items.item.Item;
import game.assets.items.Item_Ground;
import game.system.inputs.MouseInput;
import game.system.main.Game;
import game.textures.TEXTURE_LIST;
import game.textures.Texture;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Inventory implements Serializable {
	private final int slot_w = InventorySystem.slot_w, slot_h = InventorySystem.slot_h;
	private int x = 100, y = 100;
	private int init_x = 100, init_y = 100;
	private int size_x, size_y;
	private ArrayList<InventorySlot> slots = new ArrayList<>();
	private boolean moveable = true;

	private Texture
			top_left = new Texture(TEXTURE_LIST.gui_list, 0, 0),
			top = new Texture(TEXTURE_LIST.gui_list, 1, 0),
			top_right = new Texture(TEXTURE_LIST.gui_list, 3, 0),
			right = new Texture(TEXTURE_LIST.gui_list, 3, 1),
			bot_right = new Texture(TEXTURE_LIST.gui_list, 3, 2),
			bot = new Texture(TEXTURE_LIST.gui_list, 1, 2),
			bot_left = new Texture(TEXTURE_LIST.gui_list, 0, 2),
			left = new Texture(TEXTURE_LIST.gui_list, 0, 1);

	public Inventory(int size_x, int size_y) {
		this.size_x = size_x;
		this.size_y = size_y;

		for(int y = 0; y < size_y; y++) {
			for(int x = 0; x < size_x; x++) {
				// translate x, y to screen coords
				slots.add(new InventorySlot(this, x * slot_w, y * slot_h));
			}
		}
	}

	public void tick() {
		for(InventorySlot slot : slots) {
			slot.tick();
		}
	}

	public void render(Graphics g) {
		for(int y=-1; y<size_y+1; y++) {
			for(int x=-1; x<size_x+1; x++) {
				if(y == -1) {
					if(x == -1) {
						g.drawImage(top_left.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					} else if(x == size_x) {
						g.drawImage(top_right.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					} else {
						g.drawImage(top.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					}
				} else if(y == size_y) {
					if(x == -1) {
						g.drawImage(bot_left.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					} else if(x == size_x) {
						g.drawImage(bot_right.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					} else {
						g.drawImage(bot.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					}
				} else {
					if(x == -1) {
						g.drawImage(left.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					} else if(x == size_x) {
						g.drawImage(right.getTexure(), this.x + x * slot_w, this.y + y * slot_h, slot_w, slot_h, null);
					}
				}

			}
		}
		for(InventorySlot slot : slots) {
			slot.render(g);
		}
		if(Game.DEBUG_MODE) {
			g.setColor(Color.magenta);
			g.drawRect(getInventoryBounds().x, getInventoryBounds().y, getInventoryBounds().width, getInventoryBounds().height);
			if(moveable) {
				g.setColor(Color.green);
				g.drawRect(getInventoryMoveBounds().x, getInventoryMoveBounds().y, getInventoryMoveBounds().width, getInventoryMoveBounds().height);
			}
		}
	}

	public void addItem(Item item) {
		if(inventoryContainsItemAndCanStack(item)) {
			InventorySlot slot = getNextStackableSlot(item);
			if(slot != null) {
				Item rest = slot.addItem(item, this);
				if (rest != null) {
					this.addItem(rest);
				}
			}
		} else {
			if(hasFreeSlot()) {
				InventorySlot slot = getNextFreeSlot();
				if(slot != null) {
					Item rest = slot.addItem(item, this);
					if(rest != null) this.addItem(rest);
				}
			} else {
				Item_Ground item_gnd = item.getItemGround();
				Point new_coords = new Point(Game.world.getPlayer().getX(), Game.world.getPlayer().getY());
				item_gnd.setX(new_coords.x);
				item_gnd.setY(new_coords.y);
				Game.world.getInventorySystem().dropItem(item_gnd);
			}
		}
	}

	// TODO stack overflow checks
	public boolean addItemAtPos(Item item, int pos) {
		InventorySlot slot = slots.get(pos);
		if(!slot.hasItem()) {
			slot.setItem(item);
			return true;
		}
		return false;
	}

	public void mouseClick(MouseEvent e, MouseInput mouseInput, InventorySystem invSys) {
		InventorySlot slot = getClickedSlot(e, mouseInput);
		if (slot != null) {
			slot.onClick(e,this,  invSys);
		}
	}

	private InventorySlot getClickedSlot(MouseEvent e, MouseInput mouseInput) {
		for(InventorySlot slot : slots) {
			if(mouseInput.mouseOverLocalRect(slot.getBounds())) {
				return slot;
			}
		}
		return null;
	}

	public Rectangle getInventoryBounds() {
		int width = size_x * slot_w;
		int height = size_y * slot_h;
		if(moveable) {
			return new Rectangle(x, y - 12, width, height + 12);
		}
		return new Rectangle(x, y, width, height);
	}

	public Rectangle getInventoryMoveBounds() {
		if(moveable) {
			int width = size_x * slot_w;
			return new Rectangle(x, y - 12, width, 12);
		} else return null;
	}

	public void setInitXY(int x, int y) {
		this.init_x = x;
		this.init_y = y;
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public int getX() {
		return this.x;
	}
	public int getY() {
		return this.y;
	}

	public boolean canAcceptItem(Item item) {
		if(inventoryContainsItemAndCanStack(item)) {
			return true;
		} else {
			return hasFreeSlot();
		}
	}

	private InventorySlot getNextFreeSlot() {
		for (InventorySlot slot : slots) {
			if (!slot.hasItem()) {
				return slot;
			}
		}
		return null;
	}

	private boolean hasFreeSlot() {
		for(InventorySlot slot : slots) {
			if(!slot.hasItem()) {
				return true;
			}
		}
		return false;
	}

	private boolean inventoryContainsItemAndCanStack(Item item) {
		for(InventorySlot slot : slots) {
			if(!slot.hasItem()) continue;
			Item slotItem = slot.getItem();
			if(slotItem.getItemType() == item.getItemType()) {
				if(slotItem.getAmount() < InventorySystem.stackSize) return true;
			}
		}
		return false;
	}

	private InventorySlot getNextStackableSlot(Item item) {
		for(InventorySlot slot : slots) {
			if(!slot.hasItem()) continue;
			Item slotItem = slot.getItem();
			if(slotItem.getItemType() == item.getItemType()) {
				if(slotItem.getAmount() < InventorySystem.stackSize) return slot;
			}
		}
		return null;
	}

	public int getSizeX() {
		return this.size_x;
	}

	public int getSizeY() {
		return this.size_y;
	}

	public ArrayList<InventorySlot> getSlots() {
		return slots;
	}

	public void setMoveable(boolean moveable) {
		this.moveable = moveable;
	}

	public boolean isMoveable() {
		return moveable;
	}

	public void open() {
		this.setXY(init_x, init_y);
		Game.world.getInventorySystem().addOpenInventory(this);
	}

	public void close() {
		Game.world.getInventorySystem().removeOpenInventory(this);
	}

	public void fillRandom(LinkedList<Item> items) {
		for(Item item : items) {
			boolean item_placed = false;
			while(!item_placed) {
				if(!this.hasFreeSlot()) break;
				item_placed = this.addItemAtPos(item, new Random().nextInt(size_x * size_y));
			}
		}
	}

	public void fill(LinkedList<Item> items) {
		for(Item item : items) {
			this.addItem(item);
		}
	}
}
