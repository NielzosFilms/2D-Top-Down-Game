package game.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import game.entities.Player;
import game.lighting.Light;
import game.main.GameObject;
import game.main.ID;
import game.objects.Mushroom;
import game.objects.Tile;
import game.objects.Tree;
import game.textures.Textures;

public class Chunk {

	private static Random r = new Random();

	public LinkedList<LinkedList<GameObject>> entities = new LinkedList<LinkedList<GameObject>>();
	public LinkedList<Light> lights = new LinkedList<Light>();
	public LinkedList<HashMap<Point, GameObject>> tiles = new LinkedList<HashMap<Point, GameObject>>();
	public static int tile_width = 16, tile_height = 16;
	public int x, y;
	private static Long seed;
	private static Long temp_seed;
	private static Long moist_seed;
	private World world;
	private Textures textures;

	public Chunk(int x, int y, Long seed, Long temp_seed, Long moist_seed, World world, Player player,
			Textures textures) {
		this.x = x;
		this.y = y;
		this.seed = seed;
		this.temp_seed = temp_seed;
		this.moist_seed = moist_seed;
		this.world = world;
		this.textures = textures;
		// entities.add(new Enemy((x+8)*16, (y+8)*16, ID.Enemy));
		// generate chunk tiles 16x16 then add to world
		tiles.add(new HashMap<Point, GameObject>());
		tiles.add(new HashMap<Point, GameObject>());
		entities.add(new LinkedList<GameObject>());
		GenerateTiles(world, player);
	}

	public void tick() {
		for (LinkedList<GameObject> list : entities) {
			for (int i = 0; i < list.size(); i++) {
				GameObject entity = list.get(i);
				if (entity.getX() > (this.x + 16) * 16) {
					Chunk chunk = this.world.getChunkWithCoords(this.x + 16, this.y);
					chunk.entities.get(0).add(entity);
					list.remove(i);
				} else if (entity.getX() < (this.x) * 16) {
					Chunk chunk = this.world.getChunkWithCoords(this.x - 16, this.y);
					chunk.entities.get(0).add(entity);
					list.remove(i);
				} else if (entity.getY() > (this.y + 16) * 16) {
					Chunk chunk = this.world.getChunkWithCoords(this.x, this.y + 16);
					chunk.entities.get(0).add(entity);
					list.remove(i);
				} else if (entity.getY() < (this.y - 16) * 16) {
					Chunk chunk = this.world.getChunkWithCoords(this.x, this.y - 16);
					chunk.entities.get(0).add(entity);
					list.remove(i);
				} else {
					entity.tick();
				}
			}
		}
	}

	public void renderBorder(Graphics g) {
		g.setColor(Color.decode("#70deff"));
		g.drawRect(x * 16, y * 16, 16 * 16, 16 * 16);
	}

	public void renderTiles(Graphics g) {
		for (HashMap<Point, GameObject> list : tiles) {
			Iterator it = list.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				GameObject tile = (GameObject) pair.getValue();
				tile.render(g);
			}
		}
	}

	public void renderEntities(Graphics g) {
		for (LinkedList<GameObject> list : entities) {
			for (GameObject entity : list) {
				entity.render(g);
			}
		}
	}

	private void GenerateTiles(World world, Player player) {
		float[][] osn = world.getOsn(x, y, tile_width, tile_height);
		float[][] temp_osn = world.getTemperatureOsn(x, y, tile_width, tile_height);
		float[][] moist_osn = world.getMoistureOsn(x, y, tile_width, tile_height);

		// create simple tiles
		for (int yy = 0; yy < osn.length; yy++) {
			for (int xx = 0; xx < osn[yy].length; xx++) {
				float val = osn[xx][yy];

				float temp_val = temp_osn[xx][yy];
				float moist_val = moist_osn[xx][yy];

				int resized_x = xx * 16;
				int world_x = resized_x + x * 16;

				int resized_y = yy * 16;
				int world_y = resized_y + y * 16;

				tiles.get(0).put(new Point(resized_x + x, resized_y + y),
								new Tile(world_x, world_y, 0, ID.Tile, textures, World.getBiome(val, temp_val, moist_val)));
				
				if(World.getBiome(val, temp_val, moist_val) == "forest") {
					int num = r.nextInt(100);
					if (num == 0) {
						entities.get(0).add(new Tree(world_x, world_y, 1, ID.Tree, "forest", player, textures));
					} else if (num == 1) {
						entities.get(0).add(new Mushroom(world_x, world_y, 1, ID.Mushroom, textures));
					}
				}
			}
		}

		// change texture of tile
		Iterator it = tiles.get(0).entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Tile tile = (Tile) pair.getValue();

			int tex_id = tile.getTextureId(this.tiles.get(0), (Point) pair.getKey(), tile.tex_id, world, this);
			tile.setTexture(tex_id);
		}
	}

	public LinkedList<GameObject> getTilesEntities() {
		LinkedList<GameObject> tmp_tiles = new LinkedList<GameObject>();

		for (HashMap<Point, GameObject> list : tiles) {
			Iterator it = list.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				GameObject tile = (GameObject) pair.getValue();
				tmp_tiles.add(tile);
			}
		}

		for (LinkedList<GameObject> list : entities) {
			for (GameObject obj : list) {
				tmp_tiles.add(obj);
			}
		}

		return tmp_tiles;
	}

	public void removeFromTilesEntities(GameObject object) {
		for (LinkedList<GameObject> list : entities) {
			if (list.contains(object)) {
				list.remove(object);
				return;
			}
		}
		// this.entities.remove(object);
	}
}
