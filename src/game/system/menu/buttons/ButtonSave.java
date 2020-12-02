package game.system.menu.buttons;

import game.enums.BUTTONS;
import game.enums.GAMESTATES;
import game.system.main.Game;
import game.textures.Fonts;
import game.textures.Textures;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ButtonSave extends Button {
	public ButtonSave(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.btn_type = BUTTONS.Save;
	}

	public void render(Graphics g, Graphics2D g2d) {
		this.setColor(g);
		g.drawImage(Textures.default_btn, x, y, null);
		g.fillRect(x, y, width, height);

		g.setColor(Color.BLACK);
		g.setFont(Fonts.default_fonts.get(10));
		g.drawString("Save", x, y + height / 2);
	}

	public void handleClick(MouseEvent e) {
		Game.saveChunks();
		//Game.game_state = GAMESTATES.Game;
	}
}
