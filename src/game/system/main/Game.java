package game.system.main;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.util.LinkedList;
import java.util.Random;

import game.assets.entities.TargetDummy;
import game.enums.GAMESTATES;
import game.enums.ID;
import game.audioEngine.AudioFiles;
import game.assets.entities.Player;
import game.system.particles.ParticleSystem;
import game.system.hitbox.HitboxSystem;
import game.system.hud.HUD;
import game.system.inputs.KeyInput;
import game.system.inputs.MouseInput;
import game.system.inventory.InventorySystem;
import game.system.lighting.LightingSystem;
import game.system.menu.MenuSystem;
import game.system.world.Chunk;
import game.textures.Fonts;
import game.textures.Textures;
import game.system.world.LevelLoader;
import game.system.world.World;

public class Game extends Canvas implements Runnable {
	private static final long serialVersionUID = 852753996046178928L;
	private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public static final int NEW_WIDTH = (int) screenSize.getWidth(), NEW_HEIGHT = (int) screenSize.getHeight();
	public static final float RATIO = (float) NEW_WIDTH / NEW_HEIGHT;
	public static final int WIDTH = 480, HEIGHT = (int) Math.round(WIDTH / RATIO); // 640 480 idk which is better
	public static final float SCALE_WIDTH = ((float) NEW_WIDTH) / WIDTH, SCALE_HEIGHT = ((float) NEW_HEIGHT) / HEIGHT;
	public static final String TITLE = "Top Down Java Game";
	public static final String VERSION = "ALPHA V 2.36.0 INFDEV";

	public static GAMESTATES game_state = GAMESTATES.Menu;
	public static boolean DEDUG_MODE = true;

	private Thread thread;
	private boolean running = true;
	public static int current_fps = 0;
	static Canvas canvas;

	public static Textures textures;
	public static AudioFiles audioFiles;
	public static Fonts fonts;

	private Random r;
	public static Handler handler;
	public static KeyInput keyInput;
	public static MouseInput mouseInput;
	public static Collision collision;
	public static HitboxSystem hitboxSystem;
	public static Camera cam;
	public static LevelLoader ll;

	public static MenuSystem menuSystem;
	public static HUD hud;

	public static InventorySystem inventorySystem;
	public static LightingSystem lightingSystem;
	public static ParticleSystem ps;
	public static ImageRendering imageRenderer;

	public static World world;

	public Game() {
		r = new Random();

		handler = new Handler();
		keyInput = new KeyInput();
		mouseInput = new MouseInput();
		cam = new Camera(0, 0);
		collision = new Collision();
		hitboxSystem = new HitboxSystem();

		textures = new Textures();
		audioFiles = new AudioFiles();
		fonts = new Fonts();

		inventorySystem = new InventorySystem();
		ps = new ParticleSystem();
		lightingSystem = new LightingSystem();

		menuSystem = new MenuSystem();
		hud = new HUD();

		world = new World();

		//loadChunks();
		setRequirements();

		if(game_state == GAMESTATES.Game) generateRequirements();

		addTestObjects();

		this.addKeyListener(keyInput);
		this.addMouseListener(mouseInput);
		this.addMouseMotionListener(mouseInput);
		this.addMouseWheelListener(mouseInput);
		new Window(NEW_WIDTH, NEW_HEIGHT, TITLE, this);

		//World.loaded = true;
	}

	private void setRequirements() {

		handler.setRequirements(world, cam, ps);
		keyInput.setRequirements(handler, inventorySystem, world, menuSystem);
		mouseInput.setRequirements(this, inventorySystem, menuSystem, cam, hud, handler, world, world.getPlayer());
		collision.setRequirements(handler, world, world.getPlayer());
		hitboxSystem.setRequirements(handler);

		inventorySystem.setRequirements(handler, mouseInput, world, world.getPlayer(), cam);
		lightingSystem.setRequirements(handler, world, cam);

		hud.setRequirements(handler, world.getPlayer(), mouseInput, world, cam);

		menuSystem.setRequirements(mouseInput);

		world.setRequirements(textures, keyInput);
		handler.addObject(world.getPlayer());
	}

	private void generateRequirements() {
//		Long seed = 9034865798355343302L; // r.nextLong();
//		world.generate(seed);
	}

	private void addTestObjects() {
//		handler.addObject(new Crate(0, 0, 1, ID.Crate));
//		handler.addObject(new Crate(16, 0, 1, ID.Crate));
		//handler.addObject(new TargetDummy(0, 0, 1, ID.NULL));
		//ps.addParticle(new Particle(0, 0, 3, ID.Particle, 0, -1, 60, ps));
	}

	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				tick();
				delta--;
			}
			if (running)
				render();
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				current_fps = frames;
				frames = 0;
			}
		}
		stop();
	}

	private void tick() {
		if (game_state == GAMESTATES.Game && World.loaded) {

			handler.tick();
			ps.tick();
			world.tick();
			collision.tick();
			hitboxSystem.tick();

			inventorySystem.tick();

			for (LinkedList<GameObject> list : handler.object_entities) {
				for (GameObject gameObject : list) {
					if (gameObject.getId() == ID.Player) {

						// world.getChunkPointWithCoords(list.get(i).x, list.get(i).y);

						cam.tick(gameObject);
					}
				}
			}
			hud.tick();

			//keyInput.tick();

			/*
			 * if(AudioPlayer.audioEnded(audioFiles.futureopolis)) {
			 * AudioPlayer.stopSound(audioFiles.futureopolis); }
			 */

		} else if ((game_state == GAMESTATES.Pauzed) || game_state == GAMESTATES.Menu) {
			menuSystem.tick();
		}
	}

	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		Graphics2D g2d = (Graphics2D) g;

		/*
		 * AffineTransform scalingTransform =
		 * AffineTransform.getScaleInstance(SCALE_WIDTH,SCALE_HEIGHT);
		 * g2d.transform(scalingTransform);
		 */
		g2d.scale(SCALE_WIDTH, SCALE_HEIGHT);
		g.setColor(Color.decode("#d1e3ff"));
		g.fillRect(0, 0, WIDTH, HEIGHT);

		if (game_state == GAMESTATES.Menu) {
			//menuSystem.setState(MENUSTATES.Main);
			menuSystem.render(g, g2d);
		} else if ((game_state == GAMESTATES.Game || game_state == GAMESTATES.Pauzed) && World.loaded) {
			g2d.translate(cam.getX(), cam.getY()); // start of cam

			handler.render(g, WIDTH, HEIGHT);
			ps.render(g);
			hitboxSystem.render(g);

			// ongeveer 30-35 ms
			Long start = System.currentTimeMillis();
			// lightingSystem.render(g);
			Long finish = System.currentTimeMillis();
			// System.out.println("Light System Render Time: " + (finish - start));

			inventorySystem.renderCam(g);
			hud.renderCam(g, g2d);
			g2d.translate(-cam.getX(), -cam.getY()); // end of cam
			hud.render(g, g2d);
			inventorySystem.render(g);


			if (game_state == GAMESTATES.Pauzed) {
				//menuSystem.setState(MENUSTATES.Pauzed);
				menuSystem.render(g, g2d);
			}
		}

		g.dispose();
		g2d.dispose();
		bs.show();
	}

	public static void saveChunks() {
		String directory = "saves/";
		Logger.print("Save world");
		try {
			FileOutputStream fos = new FileOutputStream(directory + "test_save.data");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(world);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void loadChunks() {
		String directory = "saves/";
		Logger.print("Load world");
		try {
			FileInputStream fis = new FileInputStream(directory + "test_save.data");
			ObjectInputStream ois = new ObjectInputStream(fis);
			World loaded = (World) ois.readObject();
			world = loaded;
			System.out.println(loaded.getPlayer().getX() + " " +loaded.getPlayer().getY());
			ois.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// System.setProperty("sun.java2d.opengl", "true");
		Logger.print("Game starting...");
		canvas = new Game();
	}

}
