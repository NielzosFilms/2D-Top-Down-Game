package game.system.menu;

import game.enums.MENUSTATES;
import game.system.inputs.MouseInput;
import game.system.main.Game;
import game.system.menu.buttons.Button;
import game.system.menu.buttons.ButtonPlay;
import game.system.menu.buttons.ButtonQuit;
import game.textures.Animation;
import game.textures.TEXTURE_LIST;
import game.textures.Texture;
import game.textures.Textures;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MenuSystem {
	private static final int screenWidth = Game.WIDTH, screenHeight = Game.HEIGHT;
	public MENUSTATES state;
	public MENUSTATES previous_state;
	private MouseInput mouse;

	private MainMenu mainMenu;
	private PauzedMenu pauzedMenu;
	private SettingsMenu settingsMenu;
	private MenuWorldSelect menuWorldSelect;

	private boolean saving = false;
	private boolean loading = false;
	private Animation saving_animation;
	private Animation loading_animation;

	public MenuSystem() {
		this.state = MENUSTATES.Main;
		this.saving_animation = new Animation(10,
				new Texture(TEXTURE_LIST.cave_list, 11, 9),
				new Texture(TEXTURE_LIST.cave_list, 12, 9));
		this.loading_animation = new Animation(10,
				new Texture(TEXTURE_LIST.cave_list, 10, 9),
				new Texture(TEXTURE_LIST.cave_list, 9, 9));
	}

	public void setRequirements(MouseInput mouseInput) {
		this.mouse = mouseInput;

		this.mainMenu = new MainMenu(mouse);
		this.pauzedMenu = new PauzedMenu(mouse);
		this.settingsMenu = new SettingsMenu(mouse);
		this.menuWorldSelect = new MenuWorldSelect(mouse);
	}

	public void update() {
		this.mainMenu = new MainMenu(mouse);
		this.pauzedMenu = new PauzedMenu(mouse);
		this.settingsMenu = new SettingsMenu(mouse);
		this.menuWorldSelect = new MenuWorldSelect(mouse);
	}

	public void tick() {
		Menu active = getActiveMenu();
		if(active != null) active.tick();

		if(saving) saving_animation.runAnimation();
		if(loading) loading_animation.runAnimation();
	}

	public void render(Graphics g, Graphics2D g2d) {
		Menu active = getActiveMenu();
		if(active != null) active.render(g, g2d);

		if(saving) saving_animation.drawAnimation(g, 0, 0, 16, 16);
		if(loading) loading_animation.drawAnimation(g, 0, 0, 16, 16);
	}

	public void mousePressed(MouseEvent e) {
		Menu active = getActiveMenu();
		if(active != null) active.mousePressed(e);
	}

	public void mouseReleased(MouseEvent e) {
		Menu active = getActiveMenu();
		if(active != null) active.mouseReleased(e);
	}

	public void keyPressed(KeyEvent e) {
		Menu active = getActiveMenu();
		if(active != null) active.keyPressed(e);
	}

	public Menu getActiveMenu() {
		Menu ret;
		switch(state) {
			case Main -> ret = mainMenu;
			case Settings -> ret = settingsMenu;
			case Pauzed -> ret = pauzedMenu;
			case SaveSlotSelect -> ret = menuWorldSelect;
			default -> ret = null;
		}
		return ret;
	}

	public MENUSTATES getState() {
		return state;
	}

	public void setState(MENUSTATES state) {
		this.previous_state = this.state;
		this.state = state;
	}

	public MENUSTATES getPreviousState() { return this.previous_state; }

	public void setSaving(boolean saving) {
		this.saving = saving;
	}

	public boolean getSaving() {
		return this.saving;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}
}
