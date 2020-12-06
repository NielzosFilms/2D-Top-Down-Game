package game.system.world;

import java.awt.*;
import java.io.*;
import java.util.*;

import game.assets.entities.Player;
import game.assets.structures.Structure;
import game.assets.structures.waterfall.Waterfall;
import game.assets.tiles.Tile;
import game.assets.tiles.TileGrass;
import game.assets.tiles.TileWater;
import game.enums.BIOME;
import game.enums.ID;
import game.enums.TILE_TYPE;
import game.system.helpers.Logger;
import game.system.systems.Collision;
import game.system.systems.GameObject;
import game.system.systems.hitbox.HitboxSystem;
import game.system.systems.hud.HUD;
import game.system.inputs.KeyInput;
import game.system.inputs.MouseInput;
import game.system.systems.inventory.InventorySystem;
import game.system.systems.lighting.Light;
import game.system.systems.lighting.LightingSystem;
import game.system.main.*;
import game.system.systems.particles.ParticleSystem;
import game.textures.Texture;
import game.textures.Textures;

public class World implements Serializable {

	public Long seed, temp_seed, moist_seed;
	public HashMap<Point, Chunk> chunks = new HashMap<Point, Chunk>();
	public static boolean loaded = false;
	private Player player;
	private transient Textures textures;
	private Random r;
	private transient KeyInput keyInput;

	private transient Generation generation;

	private Handler handler;
	private Camera cam;
	private static Collision collision;
	private static HitboxSystem hitboxSystem;

	private HUD hud;

	private InventorySystem inventorySystem;
	private static LightingSystem lightingSystem;
	private static ParticleSystem ps;

	private Structure active_structure;

	public World() {
		handler = new Handler();
		cam = new Camera(0, 0);
		inventorySystem = new InventorySystem();

		hud = new HUD();
	}

	public void setRequirements(Player player, Textures textures, KeyInput keyInput, MouseInput mouseInput) {
		collision = new Collision();
		hitboxSystem = new HitboxSystem();

		ps = new ParticleSystem();
		lightingSystem = new LightingSystem();

		this.textures = textures;
		this.keyInput = keyInput;
		if(this.player != null) handler.removeObject(this.player);
		this.player = player;
		this.player.setKeyInput(keyInput);
		this.player.setId(ID.Player);

		this.keyInput = keyInput;
		keyInput.setRequirements(this);
		mouseInput.setWorld(this);

		handler.setRequirements(this, cam, ps);
		collision.setRequirements(handler, this, this.player);
		hitboxSystem.setRequirements(handler);

		inventorySystem.setRequirements(handler, mouseInput, this, this.player, cam);
		lightingSystem.setRequirements(handler, this, cam);

		hud.setRequirements(handler, this.player, mouseInput, this, cam);
		handler.addObject(this.player);
	}

	public void tick() {
		if(!loaded) return;
		int camX = (Math.round(-cam.getX() / 16));
		int camY = (Math.round(-cam.getY() / 16));
		int camW = (Math.round(Game.WIDTH / 16));
		int camH = (Math.round(Game.HEIGHT / 16));

		handler.tick();
		ps.tick();

		runWaterAnimations();
		generateNewChunksOffScreen(camX, camY, camW, camH);

		collision.tick();
		hitboxSystem.tick();

		inventorySystem.tick();
		cam.tick(player);
		hud.tick();
	}

	private void generateNewChunksOffScreen(int camX, int camY, int camW, int camH) {
		for (int y = camY - 32; y < camY + camH + 16; y++) {
			for (int x = camX - 32; x < camX + camW + 16; x++) {
				if (getActiveChunks().containsKey(new Point(x, y))) {
					getActiveChunks().get(new Point(x, y)).tick();
					if (!getActiveChunks().containsKey(new Point(x - 16, y))) {
						getActiveChunks().put(
								new Point(x - 16, y),
								new Chunk(x - 16, y, seed, temp_seed, moist_seed, this, player));
					} else if (!getActiveChunks().containsKey(new Point(x + 16, y))) {
						getActiveChunks().put(
								new Point(x + 16, y),
								new Chunk(x + 16, y, seed, temp_seed, moist_seed, this, player));
					}
					if (!getActiveChunks().containsKey(new Point(x, y - 16))) {
						getActiveChunks().put(
								new Point(x, y - 16),
								new Chunk(x, y - 16, seed, temp_seed, moist_seed, this, player));
					} else if (!getActiveChunks().containsKey(new Point(x, y + 16))) {
						getActiveChunks().put(
								new Point(x, y + 16),
								new Chunk(x, y + 16, seed, temp_seed, moist_seed, this, player));
					}
				}
			}
		}
	}

	private void runWaterAnimations() {
		for(TILE_TYPE key : Textures.water_red.keySet()) {
			Textures.water_red.get(key).runAnimation();
		}
	}

	public LinkedList<Chunk> getChunksOnScreen() {
		LinkedList<Chunk> tmp_chunks = new LinkedList<Chunk>();

		int camX = (Math.round(-cam.getX() / 16));
		int camY = (Math.round(-cam.getY() / 16));
		int camW = (Math.round(Game.WIDTH / 16));
		int camH = (Math.round(Game.HEIGHT / 16));

		for (int y = camY - 32; y < camY + camH + 16; y++) {
			for (int x = camX - 32; x < camX + camW + 16; x++) {
				if (getActiveChunks().containsKey(new Point(x, y))) {
					Chunk chunk = getActiveChunks().get(new Point(x, y));
					tmp_chunks.add(chunk);
				}
			}
		}
		return tmp_chunks;
	}

	public HashMap<Point, Chunk> getActiveChunks() {
		if(structureActive()) {
			return active_structure.getChunks();
		}
		return chunks;
	}

	public void render(Graphics g, Graphics2D g2d) {
		g2d.translate(cam.getX(), cam.getY()); // start of cam

		handler.render(g, Game.WIDTH, Game.HEIGHT);
		ps.render(g);
		hitboxSystem.render(g);

		// ongeveer 30-35 ms
		Long start = System.currentTimeMillis();
		//lightingSystem.render(g);
		Long finish = System.currentTimeMillis();
		// System.out.println("Light System Render Time: " + (finish - start));

		inventorySystem.renderCam(g);
		hud.renderCam(g, g2d);
		g2d.translate(-cam.getX(), -cam.getY()); // end of cam
		hud.render(g, g2d);
		inventorySystem.render(g);
	}

	public boolean addEntityToChunk(GameObject entity) {
		if(entity.getId() == ID.Player) return false;
		int x = entity.getX();
		int y = entity.getY();
		Chunk crsp_chunk = getChunkWithCoordsPoint(getChunkPointWithCoords(x, y));
		if (crsp_chunk != null) { // adds enemy to a chunk to be unloaded
			//Logger.print(String.valueOf(entity.getId()));
			crsp_chunk.addEntity(entity);
			return true;
		}
		return false;
	}

	public boolean addLightToChunk(Light light) {
		int x = light.getX();
		int y = light.getY();
		Chunk crsp_chunk = getChunkWithCoordsPoint(getChunkPointWithCoords(x, y));
		if (crsp_chunk != null) { // adds enemy to a chunk to be unloaded
			crsp_chunk.addLight(light);

			return true;
		}
		return false;
	}

	// functions to get specific tiles/tiletypes

	private boolean OnScreen(int x, int y, int w, int h) {
		return (x + (16 * 16) > -cam.getX() && x < -cam.getX() + Game.WIDTH)
				&& (y + (16 * 16) > -cam.getY() && y < -cam.getY() + Game.HEIGHT);
	}

	public Chunk getChunkWithCoords(int x, int y) {
		Point chunk_point = new Point(x, y);
		return getActiveChunks().get(chunk_point);
	}

	public Chunk getChunkWithCoordsPoint(Point coords) {
		return getActiveChunks().get(coords);
	}

	public Point getChunkPointWithCoords(int x, int y) {
		x = x / 16;
		y = y / 16;

		x -= Math.floorMod(x, 16);
		y -= Math.floorMod(y, 16);

		return new Point(x, y);
	}

	public static BIOME getBiome(float val, float temp_val, float moist_val) {
		// biome generation needs refinement
		/*if ((temp_val > -0.5 && temp_val < 0.5) && (moist_val > 0.5)) { // forest
			if (val < -0.3) {
				return BIOME.Beach;
			} else {
				return BIOME.Forest;
			}
		} else if (temp_val < 0 && moist_val < 0) { // desert
			return BIOME.Desert;
		} else if (temp_val > 0 && moist_val < 0) { // dirt
			return BIOME.Dirt;
		}
		return BIOME.Ocean;*/
		if (val > -0.5) {
			return BIOME.Forest;
		}
		return BIOME.Ocean;
	}

	public BIOME getBiomeWithCoords(int x, int y) {
		x /= 16;
		y /= 16;
		float[] arr = generation.getHeightMapValuePoint(x, y);
		return getBiome(arr[0], arr[1], arr[2]);
	}

	public void generate(Long seed) {
		this.r = new Random(seed);
		this.seed = seed;
		this.temp_seed = r.nextLong();
		this.moist_seed = r.nextLong();
		generation = new Generation(seed, temp_seed, moist_seed);
		generation.setHeight_scale(0.05f);
		loaded = false;
		chunks.clear();
		setRequirements(new Player(0, 0, 2, ID.Player, keyInput), Game.textures, Game.keyInput, Game.mouseInput);

		Logger.print("[seed]: " + this.seed);

		Point chunk_point = getChunkPointWithCoords(player.getX(), player.getY());
		chunks.put(chunk_point, new Chunk(chunk_point.x, chunk_point.y, seed, temp_seed, moist_seed, this, player));
		handler.addObject(new Waterfall(0, 0, 1));
		loaded = true;
	}

	public Tile getGeneratedTile(int x, int y, float height, float temp, float moist, Chunk chunk, int world_x, int world_y) {
		if(!structureActive()) {
			if (getBiome(height, temp, moist) == BIOME.Forest) {
				return new TileGrass(world_x, world_y, x, y, 1, BIOME.Forest, chunk);
			} else {
				return new TileWater(world_x, world_y, x, y, 1, BIOME.Ocean, chunk);
			}
		} else {
			return active_structure.getGeneratedTile(x, y, height, temp, moist, chunk, world_x, world_y);
		}
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(Player player) {
		handler.removeObject(this.player);
		this.player = player;
	}

	public Long getSeed() {
		return this.seed;
	}

	public Long getTemp_seed() {
		return temp_seed;
	}

	public Long getMoist_seed() {
		return moist_seed;
	}

	public void setSeeds(Long seed, Long temp_seed, Long moist_seed) {
		this.seed = seed;
		this.temp_seed = temp_seed;
		this.moist_seed = moist_seed;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public Camera getCam() {
		return cam;
	}

	public void setCam(Camera cam) {
		this.cam = cam;
	}

	public Collision getCollision() {
		return collision;
	}

	public void setCollision(Collision collision) {
		this.collision = collision;
	}

	public HitboxSystem getHitboxSystem() {
		return hitboxSystem;
	}

	public void setHitboxSystem(HitboxSystem hitboxSystem) {
		this.hitboxSystem = hitboxSystem;
	}

	public HUD getHud() {
		return hud;
	}

	public void setHud(HUD hud) {
		this.hud = hud;
	}

	public InventorySystem getInventorySystem() {
		return inventorySystem;
	}

	public void setInventorySystem(InventorySystem inventorySystem) {
		this.inventorySystem = inventorySystem;
	}

	public LightingSystem getLightingSystem() {
		return lightingSystem;
	}

	public void setLightingSystem(LightingSystem lightingSystem) {
		this.lightingSystem = lightingSystem;
	}

	public ParticleSystem getPs() {
		return ps;
	}

	public void setPs(ParticleSystem ps) {
		this.ps = ps;
	}

	public Generation getGeneration() {
		return this.generation;
	}

	public boolean structureActive() {
		return active_structure != null;
	}

	public void setActiveStructure(Structure structure) {
		this.active_structure = structure;
		structure.generate(this, player);
	}
}
