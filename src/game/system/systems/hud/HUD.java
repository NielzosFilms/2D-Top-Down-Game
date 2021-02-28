package game.system.systems.hud;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import game.assets.HealthBar;
import game.assets.entities.Player;
import game.system.helpers.Helpers;
import game.system.helpers.Timer;
import game.system.main.*;
import game.system.inputs.MouseInput;
import game.system.systems.gameObject.GameObject;
import game.system.systems.gameObject.Interactable;
import game.system.world.World;
import game.textures.TEXTURE_LIST;
import game.textures.Texture;

public class HUD implements Serializable {
	private DebugHUD debugHUD;
	private transient double velX, velY;
	private transient Handler handler;
	private transient Player player;
	private transient MouseInput mouseInput;
	private transient GameController gameController;
	private transient Camera cam;

	private LinkedList<LinkedList<GameObject>> objects_on_hud = new LinkedList<>();

	private Selection selection = new Selection();

	public HUD() {
		this.debugHUD = new DebugHUD();
	}

	public void setRequirements(Handler handler, Player player, MouseInput mouseInput, GameController gameController, Camera cam) {
		this.handler = handler;
		this.player = player;
		this.mouseInput = mouseInput;
		this.gameController = gameController;
		this.cam = cam;
		this.debugHUD.setRequirements(mouseInput, player, gameController);
	}

	public void tick() {
		objects_on_hud = gameController.getObjectsOnHud();
		if(Game.DEBUG_MODE) debugHUD.tick();
		selection.tick();
	}

	public void renderCam(Graphics g, Graphics2D g2d) {
		for(LinkedList<GameObject> z_list : objects_on_hud) {
			for(GameObject object : z_list) {
				object.render(g);
			}
		}

		LinkedList<GameObject> objs = handler.getSelectableObjects();
		for (GameObject obj : objs) {
			if (((Interactable)obj).getSelectBounds() != null) {
				if (mouseInput.mouseOverWorldVar(((Interactable)obj).getSelectBounds().x, ((Interactable)obj).getSelectBounds().y,
						((Interactable)obj).getSelectBounds().width, ((Interactable)obj).getSelectBounds().height)) {
					if (Helpers.getDistanceBetweenBounds(Game.gameController.getPlayer().getBounds(), ((Interactable)obj).getSelectBounds()) < Game.gameController.getPlayer().REACH) {
						selection.renderSelection(g, ((Interactable) obj).getSelectBounds(), 2);
						break;
					}
				}
			}
		}
		if(Game.DEBUG_MODE) debugHUD.renderCam(g, g2d);
	}

	public void render(Graphics g, Graphics2D g2d) {
		Font font = new Font("SansSerif", Font.PLAIN, 3);
		g2d.setFont(font);
		FontMetrics fontMetrics = g2d.getFontMetrics(font);

		String version = Game.VERSION;
		String name = "NielzosFilms";

		g.setColor(Color.black);
		g2d.drawString(version, (Game.WIDTH - fontMetrics.stringWidth(version)), fontMetrics.getAscent());
		g2d.drawString(name, (Game.WIDTH - fontMetrics.stringWidth(name)),
				fontMetrics.getHeight() + fontMetrics.getAscent());

		if(Game.DEBUG_MODE) debugHUD.render(g, g2d);
	}

	private int getWorldCoordX(int screen_x) {
		return (int) (screen_x - Math.round(-cam.getX()));
	}

	private int getWorldCoordY(int screen_y) {
		return (int) (screen_y - Math.round(-cam.getY()));
	}

	public void mousePressed(MouseEvent e) {
		debugHUD.mousePressed(e);
	}
	public void mouseReleased(MouseEvent e) {
		debugHUD.mouseReleased(e);
	}

}
