package game.system.main;

import game.assets.entities.Player;
import game.assets.entities.enemies.Enemy;
import game.assets.objects.tree.Tree;
import game.assets.structures.Structure;
import game.enums.ID;
import game.system.helpers.Logger;
import game.system.inputs.KeyInput;
import game.system.inputs.MouseInput;
import game.system.main.Camera;
import game.system.main.Handler;
import game.system.systems.Collision;
import game.system.systems.gameObject.GameObject;
import game.system.systems.hitbox.HitboxSystem;
import game.system.systems.hud.HUD;
import game.system.systems.inventory.InventorySystem;
import game.system.systems.lighting.LightingSystem;
import game.system.systems.particles.ParticleSystem;
import game.system.world.Chunk;
import game.system.world.Generation;
import game.system.world.JsonStructureLoader;
import game.textures.Textures;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class GameController implements Serializable {
//    public HashMap<Point, Chunk> chunks = new HashMap<Point, Chunk>();
    public static boolean loaded = false;
    private transient Textures textures;
    private transient KeyInput keyInput;
    private Player player;
    private Random r;

    private Handler handler;
    private Camera cam;
    private static Collision collision;
    private static HitboxSystem hitboxSystem;

    private HUD hud;

    private InventorySystem inventorySystem;
    private static LightingSystem lightingSystem;
    private static ParticleSystem ps;

    public GameController() {
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
        mouseInput.setGameController(this);

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
//        player.tick();
//        generateNewChunksOffScreen(camX, camY, camW, camH);
//        tickChunksOnScreen(camX, camY, camW, camH);
        collision.tick();
        hitboxSystem.tick();

        inventorySystem.tick();
        cam.tick(player);
        hud.tick();
    }

    private void runWaterAnimations() {
        for(int key : Textures.water_red.keySet()) {
            Textures.water_red.get(key).runAnimation();
        }
        for(int key : Textures.generated_animations.keySet()) {
            Textures.generated_animations.get(key).runAnimation();
        }
    }

    public void render(Graphics g, Graphics2D g2d) {
        g2d.translate(cam.getX(), cam.getY()); // start of cam

        handler.render(g, Game.WIDTH, Game.HEIGHT);
        ps.render(g);
        hitboxSystem.render(g);
//        player.render(g);

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

    public void generate() {
        loaded = false;
        setRequirements(new Player(0, 0, 10, ID.Player, keyInput), Game.textures, Game.keyInput, Game.mouseInput);

        //Logger.print("[seed]: " + this.seed);
        Logger.print("World Generating...");
        JsonStructureLoader jsonLoader = new JsonStructureLoader("assets/structures/dungeon_map.json");
//        chunks = jsonLoader.getChunks();
//        if(jsonLoader.getPlayerSpawn() != null) {
//            getPlayer().setX(jsonLoader.getPlayerSpawn().x);
//            getPlayer().setY(jsonLoader.getPlayerSpawn().y);
//        }
        handler.addObject(new Enemy(80, 64, 10, ID.Enemy));
        handler.addObject(new Tree(64, 64, 10, ID.Tree, null));
        loaded = true;
    }

    public LinkedList<LinkedList<GameObject>> getAllGameObjects() {
        return new LinkedList<>();
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        handler.removeObject(this.player);
        this.player = player;
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

    private void updatePlayerPosition(int x, int y) {
        player.setX(x);
        player.setY(y);
        cam.setCoordsWithPlayerCoords(x, y);
    }

    public LinkedList<LinkedList<GameObject>> getObjectsOnHud() {
//        LinkedList<LinkedList<GameObject>> ret = new LinkedList<>();
//        for(Point key : getActiveChunks().keySet()) {
//            Chunk chunk = getActiveChunks().get(key);
//            for(int i=0; i<chunk.getObjectsOnHud().size(); i++) {
//                GameObject object = chunk.getObjectsOnHud().get(i);
//                int z_index = object.getZIndex();
//                for(int z=ret.size(); z<=z_index; z++) {
//                    ret.add(new LinkedList<>());
//                }
//                ret.get(z_index).add(object);
//            }
//        }
        return new LinkedList<>();
    }
}
