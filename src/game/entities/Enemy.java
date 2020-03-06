package game.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import game.main.GameObject;
import game.main.ID;

public class Enemy extends GameObject{

	public Enemy(int x, int y, int z_index, ID id) {
		super(x, y, z_index, id);
	}

	public void tick() {
		
	}

	public void render(Graphics g) {
		g.setColor(Color.red);
		g.drawRect(x, y, 16, 16);
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, 16, 16);
	}

}
