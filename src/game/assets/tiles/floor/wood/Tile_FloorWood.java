package game.assets.tiles.floor.wood;

import game.assets.items.item.Item;
import game.assets.tiles.tile.Tile;
import game.system.systems.gameObject.HasItem;
import game.textures.TEXTURE_LIST;
import game.enums.TILE_TYPE;
import game.system.helpers.TileHelperFunctions;
import game.system.world.Chunk;
import game.textures.Texture;

import java.awt.*;
import java.util.HashMap;

public class Tile_FloorWood extends Tile implements HasItem {
	private HashMap<TILE_TYPE, Texture> textures = new HashMap<>();

	public Tile_FloorWood(int x, int y, int chunk_x, int chunk_y, int z_index, Chunk chunk) {
		super(x, y, chunk_x, chunk_y, z_index, null, chunk);

		textures.put(TILE_TYPE.center, new Texture(TEXTURE_LIST.floorTiles_list, 1, 1));

		textures.put(TILE_TYPE.top, new Texture(TEXTURE_LIST.floorTiles_list, 1, 0));
		textures.put(TILE_TYPE.right, new Texture(TEXTURE_LIST.floorTiles_list, 2, 1));
		textures.put(TILE_TYPE.bottom, new Texture(TEXTURE_LIST.floorTiles_list, 1, 2));
		textures.put(TILE_TYPE.left, new Texture(TEXTURE_LIST.floorTiles_list, 0, 1));
		textures.put(TILE_TYPE.top_left, new Texture(TEXTURE_LIST.floorTiles_list, 0, 0));
		textures.put(TILE_TYPE.top_right, new Texture(TEXTURE_LIST.floorTiles_list, 2, 0));
		textures.put(TILE_TYPE.bottom_left, new Texture(TEXTURE_LIST.floorTiles_list, 0, 2));
		textures.put(TILE_TYPE.bottom_right, new Texture(TEXTURE_LIST.floorTiles_list, 2, 2));

		texture = textures.get(TILE_TYPE.center);
		//chunk.updateSameTiles(this);
	}

	public void tick() {

	}

	public void render(Graphics g) {
		g.drawImage(texture.getTexure(), x, y, 16, 16, null);
	}

	public void findAndSetEdgeTexture() {
		//TILE_TYPE tileType = TileHelperFunctions.getTileType4DirTile(this, chunk, z_index);
		//this.texture = textures.get(tileType);
	}

	public void update() {
		findAndSetEdgeTexture();
	}

	public Item getItem() {
		return new Item_FloorWood(1);
	}
}
