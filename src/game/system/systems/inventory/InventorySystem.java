package game.system.systems.inventory;

import game.assets.entities.Player;
import game.assets.items.item.Item;
import game.assets.items.Item_Ground;
import game.assets.items.item.Placeable;
import game.assets.tiles.tile.Tile;
import game.audio.SoundEffect;
import game.system.helpers.Helpers;
import game.system.helpers.Timer;
import game.system.main.*;
import game.system.inputs.MouseInput;
import game.system.systems.gameObject.GameObject;
import game.system.systems.gameObject.HasItem;
import game.system.systems.hud.Selection;
import game.system.systems.inventory.inventoryDef.AcceptsItems;
import game.system.systems.inventory.inventoryDef.InventoryDef;
import game.system.systems.inventory.inventoryDef.InventorySlotDef;
import game.system.systems.particles.Particle_String;
import game.system.world.Chunk;
import game.system.world.World;
import game.textures.TEXTURE_LIST;
import game.textures.Texture;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.Serializable;
import java.util.ArrayList;

public class InventorySystem implements Serializable {
	public static final int slot_w = 22, slot_h = 22;
	public static final int item_w = 16, item_h = 16;
	public static final int stackSize = 99;
	public static final Color slot_bg = new Color(0, 0, 0, 127);
	public static final Color slot_outline = new Color(20, 20, 20, 255);
	public static final Color slot_hover = new Color(255, 255, 255, 50);

	private transient Handler handler;
	private transient MouseInput mouseInput;
	private transient GameController gameController;
	private transient InventoryDef player_inv;
	private transient Camera cam;
	public transient InventoryDef player_hotbar;

	public int hotbar_selected = 0;
	private ArrayList<InventoryDef> open_inventories = new ArrayList<>();

	private Item holding = null;

	private Timer placeTimer = new Timer(10);

	private Texture hotbar_selection = new Texture(TEXTURE_LIST.gui_list, 4, 3);

	public InventorySystem() {}

	public void setRequirements(Handler handler, MouseInput mouseInput, GameController gameController, Player player, Camera cam) {
		this.handler = handler;
		this.mouseInput = mouseInput;
		this.gameController = gameController;
		this.player_hotbar = player.hotbar;
		this.open_inventories.clear();
		this.open_inventories.add(this.player_hotbar);
		this.player_inv = player.inventory;
		this.cam = cam;
	}

	public void tick() {
		placeTimer.tick();
		for(int i=0; i<open_inventories.size(); i++) {
			InventoryDef inv = open_inventories.get(i);
			inv.tick();
		}

		if(!mouseOverInventory()) {
			mouseOutside();
		} else {
			InventoryDef inv = getHoveredInventory();
			if(inv.isMoveable() && mouseInput.leftMouseDown()) {
				if(mouseInput.mouseOverLocalRect(inv.getInventoryMoveBounds())) {
					inv.setXY(mouseInput.mouse_x - inv.getInventoryMoveBounds().width / 2, mouseInput.mouse_y + 12 / 2);
				}
			}
		}
	}

	public void renderCam(Graphics g) {
		if(isHolding()) {
			if(holding instanceof Placeable) {
				if(!mouseOverInventory()) {
					Graphics2D g2d = (Graphics2D) g;
					Point world_coords = Helpers.getWorldCoords(mouseInput.mouse_x, mouseInput.mouse_y, cam);
					Point tile_coords = Helpers.getTileCoords(world_coords, item_w, item_h);
					Rectangle bnds = new Rectangle(tile_coords.x, tile_coords.y, item_w, item_h);
					if(Helpers.getDistanceBetweenBounds(Game.gameController.getPlayer().getBounds(), bnds) < Game.gameController.getPlayer().REACH) {
						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
					} else {
						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
					}
					holding.render(g, tile_coords.x, tile_coords.y);
					g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}
			}
		}
	}

	public void render(Graphics g) {
		for(int i=0; i<open_inventories.size(); i++) {
			InventoryDef inv = open_inventories.get(i);
			inv.render(g);
			if(inv == player_hotbar) {
				Rectangle bnds = inv.getSlots().get(hotbar_selected).getBounds();
				g.drawImage(hotbar_selection.getTexure(), bnds.x, bnds.y, null);
			}
		}
		if(isHolding()) {
			if(!(holding instanceof Placeable)) {
				holding.render(g, mouseInput.mouse_x - item_w / 2, mouseInput.mouse_y - item_h / 2);
			} else {
				if(mouseOverInventory()) {
					holding.render(g, mouseInput.mouse_x - item_w / 2, mouseInput.mouse_y - item_h / 2);
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if(mouseOverInventory()) {
			if(e.getButton() == MouseEvent.BUTTON1) SoundEffect.inv_select_2.play();
			if(e.getButton() == MouseEvent.BUTTON3) SoundEffect.inv_select_3.play();
			getHoveredInventory().mouseClick(e, mouseInput, this);
		}
	}

	public void mouseOutside() {
		if(mouseInput.leftMouseDown()) {
			if(isHolding()) {
				if(holding instanceof Placeable && placeTimer.timerOver()) {
					placeTimer.resetTimer();
					Point world_coords = Helpers.getWorldCoords(mouseInput.mouse_x, mouseInput.mouse_y, cam);
					Point tile_coords = Helpers.getTileCoords(world_coords, item_w, item_h);
					Rectangle bnds = new Rectangle(tile_coords.x, tile_coords.y, item_w, item_h);
					if(Helpers.getDistanceBetweenBounds(Game.gameController.getPlayer().getBounds(), bnds) < Game.gameController.getPlayer().REACH) {
						if (((Placeable) holding).place(tile_coords.x, tile_coords.y)) {
							holding.setAmount(holding.getAmount() - 1);
							if (holding.getAmount() <= 0) clearHolding();
						}
					} else {
						gameController.getPs().addParticle(new Particle_String(gameController.getPlayer().getX(), gameController.getPlayer().getY(), 0f, -0.5f, 30, "Cannot Reach!"));
					}
				}
			} else {
				if(Game.gameController.getPlayer().canAttack()) {
					Game.gameController.getPlayer().attack();
				}
			}
		} else if(mouseInput.rightMouseDown()) {
			if(placeTimer.timerOver()) {
				placeTimer.resetTimer();
				Point world_coords = Helpers.getWorldCoords(mouseInput.mouse_x, mouseInput.mouse_y, cam);
				Point tile_coords = Helpers.getTileCoords(world_coords, item_w, item_h);
				// Chunk chunk = gameController.getChunkWithCoordsPoint(world.getChunkPointWithCoords(world_coords.x, world_coords.y));
//				if(chunk != null) {
//					tile_coords.x = tile_coords.x / 16 - chunk.x;
//					tile_coords.y = tile_coords.y / 16 - chunk.y;
//					if (chunk.tileExistsCoords(3, tile_coords)) {
//						Tile tile_found = chunk.getTileMap(3).get(tile_coords);
//						Rectangle tile_bnds = new Rectangle(tile_found.getX(), tile_found.getY(), item_w, item_h);
//						if (Helpers.getDistanceBetweenBounds(world.getPlayer().getBounds(), tile_bnds) < world.getPlayer().REACH) {
//							if (tile_found instanceof HasItem && player_inv instanceof AcceptsItems) {
//								Item tile_item = ((HasItem) tile_found).getItem();
//								if (isHolding() && holding.getClass() == tile_item.getClass() && holding.getAmount() + tile_item.getAmount() <= stackSize) {
//									holding.setAmount(holding.getAmount() + tile_item.getAmount());
//								} else if (((AcceptsItems) player_inv).canAcceptItem(tile_item)) {
//									((AcceptsItems) player_inv).addItem(tile_item);
//								} else {
//									dropItemAtPlayer(tile_item);
//								}
//								chunk.removeTile(tile_found);
//								chunk.update();
//							}
//						} else {
//							world.getPs().addParticle(new Particle_String(world.getPlayer().getX(), world.getPlayer().getY(), 0f, -0.5f, 30, "Cannot Reach!"));
//						}
//					}
//				}
			}
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		//AudioPlayer.playSound(AudioFiles.inv_select_1, 0.7f, false, 0);
		int new_index = hotbar_selected + e.getWheelRotation();
		if (new_index > player_hotbar.getSizeX() - 1) {
			new_index = 0;
		} else if (new_index < 0) {
			new_index = player_hotbar.getSizeX() - 1;
		}
		setHotbarSelected(new_index);
	}

	public void keyPressed(KeyEvent e) {
		if(holding != null) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_Q:
					Item_Ground item_gnd = holding.getItemGround();
					Point world_coords = Helpers.getWorldCoords(mouseInput.mouse_x - item_w / 2, mouseInput.mouse_y - item_h / 2, cam);
					item_gnd.setX(world_coords.x);
					item_gnd.setY(world_coords.y);
					dropItem(item_gnd);
					break;
			}
		}
		switch (e.getKeyCode()) {
			case KeyEvent.VK_1:
				hotbarKeyPressed(1);
				break;
			case KeyEvent.VK_2:
				hotbarKeyPressed(2);
				break;
			case KeyEvent.VK_3:
				hotbarKeyPressed(3);
				break;
			case KeyEvent.VK_4:
				hotbarKeyPressed(4);
				break;
			case KeyEvent.VK_5:
				hotbarKeyPressed(5);
				break;
			case KeyEvent.VK_6:
				hotbarKeyPressed(6);
				break;
			case KeyEvent.VK_7:
				hotbarKeyPressed(7);
				break;
			case KeyEvent.VK_8:
				hotbarKeyPressed(8);
				break;
			case KeyEvent.VK_9:
				hotbarKeyPressed(9);
				break;
		}
	}

	public void addOpenInventory(InventoryDef inv) {
		if(!this.open_inventories.contains(inv)) {
			//if(this.open_inventories.size() > 2) closeAll();
			// TODO change inventory positions
			for(InventoryDef inventory : open_inventories) {
				if(inv.getInventoryBounds().intersects(inventory.getInventoryBounds())) {
					removeOpenInventory(inventory);
					break;
				}
			}
			this.open_inventories.add(inv);
		} else {
			removeOpenInventory(inv);
		}
	}
	public void removeOpenInventory(InventoryDef inv) {
		this.open_inventories.remove(inv);
	}

	public void pickupItemToPlayerInv(GameObject obj) {
		if(obj instanceof HasItem) {
			Item item = ((HasItem) obj).getItem();
			if (item != null && player_inv instanceof AcceptsItems) {
				if (((AcceptsItems) player_inv).canAcceptItem(item)) {
					((AcceptsItems) player_inv).addItem(item);
					handler.findAndRemoveObject(obj);
					SoundEffect.inv_pickup_item.play();
				}
			}
		}
	}

	public void dropItem(Item_Ground item) {
		handler.addObject(item);
		holding = null;
	}

	public void dropItemAtPlayer(Item item) {
		Item_Ground item_g = item.getItemGround();
		item_g.setX(Game.gameController.getPlayer().getX());
		item_g.setY(Game.gameController.getPlayer().getY());
		handler.addObject(item_g);
	}

	public boolean inventoryIsOpen() {
		return this.open_inventories.size() > 1;
	}

	public boolean openInventoriesContains(Inventory inv) {
		return this.open_inventories.contains(inv);
	}

	public boolean isHolding() {
		return this.holding != null;
	}

	public void clearHolding() {
		this.holding = null;
	}

	public void setHolding(Item item) {
		this.holding = item;
	}

	public Item getHolding() {
		return this.holding;
	}

	public void closeAll() {
		this.open_inventories.clear();
		this.open_inventories.add(player_hotbar);
	}

	private boolean mouseOverInventory() {
		for(int i=0; i<open_inventories.size(); i++) {
			if(mouseInput.mouseOverLocalRect(open_inventories.get(i).getInventoryBounds())) {
				return true;
			}
		}
		return false;
	}

	public void setHotbarSelected(int index) {
		this.hotbar_selected = Helpers.clampInt(index, 0, this.player_hotbar.getSizeX() - 1);
	}

	private void hotbarKeyPressed(int index) {
		index -= 1;
		setHotbarSelected(index);
		if(isHolding()) {
			Item hotbar_item = getHotbarSelectedItem();
			if(hotbar_item == null) {
				setHotbarSelectedItem(holding);
				clearHolding();
			}
		} else if(mouseOverInventory()) {
			InventoryDef inv = getHoveredInventory();
			for(int i=0; i<inv.getSlots().size(); i++) {
				InventorySlotDef invSlot = inv.getSlots().get(i);
				if(mouseInput.mouseOverLocalRect(invSlot.getBounds())) {
					Item hotbar_item = getHotbarSelectedItem();
					setHotbarSelectedItem(invSlot.getItem());
					invSlot.setItem(hotbar_item);
				}
			}
		}
	}

	public InventoryDef getHoveredInventory() {
		for(int i=0; i<open_inventories.size(); i++) {
			InventoryDef inv = open_inventories.get(i);
			if(mouseInput.mouseOverLocalRect(inv.getInventoryBounds())) {
				return inv;
			}
		}
		return null;
	}

	public void addHotbarSelected(int amount) {
		int new_index = this.hotbar_selected + amount;
		if(new_index > this.player_hotbar.getSizeX() - 1) {
			this.setHotbarSelected(0);
		} else {
			this.setHotbarSelected(new_index);
		}
	}

	public void subHotbarSelected(int amount) {
		int new_index = this.hotbar_selected - amount;
		if(new_index < 0) {
			this.setHotbarSelected(this.player_hotbar.getSizeX()-1);
		} else {
			this.setHotbarSelected(new_index);
		}
	}

	public Item getHotbarSelectedItem() {
		return this.player_hotbar.getSlots().get(hotbar_selected).getItem();
	}

	public void setHotbarSelectedItem(Item item) {
		this.player_hotbar.getSlots().get(hotbar_selected).setItem(item);
	}

}
