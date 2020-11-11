package game.inventory;

import game.items.Item;
import game.main.Game;
import game.main.MouseInput;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Inventory {
	private final int slot_w = InventorySystem.slot_w, slot_h = InventorySystem.slot_h;
	private int x = 100, y = 100;
	private int size_x, size_y;
	private ArrayList<InventorySlot> slots = new ArrayList<>();

	public Inventory(int size_x, int size_y) {
		this.size_x = size_x;
		this.size_y = size_y;

		for(int y = 0; y < size_y; y++) {
			for(int x = 0; x < size_x; x++) {
				// translate x, y to screen coords
				slots.add(new InventorySlot(this.x, this.y,x + x * slot_w, y + y * slot_h));
			}
		}
	}

	public void tick() {
		for(InventorySlot slot : slots) {
			slot.tick();
		}
	}

	public void render(Graphics g) {
		for(InventorySlot slot : slots) {
			slot.render(g);
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
				// drop overflow item?
			}
		}
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
		return new Rectangle(x, y, width, height);
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
		for(InventorySlot slot : slots) {
			slot.setInvXY(this.x, this.y);
		}
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
}
