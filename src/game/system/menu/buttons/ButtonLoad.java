package game.system.menu.buttons;

import game.enums.BUTTONS;
import game.enums.GAMESTATES;
import game.system.main.Game;
import game.system.main.Logger;
import game.system.world.World;
import game.textures.Fonts;
import game.textures.Textures;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ButtonLoad extends Button {
	public ButtonLoad(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.btn_type = BUTTONS.Load;
	}

	public void render(Graphics g, Graphics2D g2d) {
		this.setColor(g);
		g.drawImage(Textures.default_btn, x, y, null);
		g.fillRect(x, y, width, height);

		g.setColor(Color.BLACK);
		g.setFont(Fonts.default_fonts.get(10));
		g.drawString("Load World", x, y + height / 2);
	}

	public void handleClick(MouseEvent e) {
		Game.game_state = GAMESTATES.Game;
		loadChunks();
		World.loaded = true;
	}

	public void loadChunks() {
		String directory = "saves/";
		Logger.print("Load world");
		try {
			FileInputStream fis = new FileInputStream(directory + "test_save.data");
			ObjectInputStream ois = new ObjectInputStream(fis);
			World loaded_world = (World) ois.readObject();
			Game.world = loaded_world;
			Game.world.setPlayer(loaded_world.getPlayer());
			Game.world.setSeeds(loaded_world.getSeed(), loaded_world.getTemp_seed(), loaded_world.getMoist_seed());
			ois.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
