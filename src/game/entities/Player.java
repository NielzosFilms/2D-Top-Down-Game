package game.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Random;

import game.main.Animation;
import game.main.Game;
import game.main.GameObject;
import game.main.ID;
import game.main.KeyInput;
import game.main.Textures;

public class Player extends GameObject{
	
	Random r = new Random();
	private KeyInput keyInput;
	public boolean onGround, direction, falling, crouch, moving, sliding, jumping;
	private int sliding_timer, sliding_timer_wait = 0;
	
	private Animation idle, running, jumping_ani, crouch_ani, slide;

	public Player(int x, int y, ID id, KeyInput keyInput) {
		super(x, y, id);
		this.onGround = false;
		this.keyInput = keyInput;
		
		idle = new Animation(6, Textures.playerImg.get(0), Textures.playerImg.get(1), Textures.playerImg.get(2), Textures.playerImg.get(3));
		running = new Animation(6, Textures.playerImg.get(8), Textures.playerImg.get(9), Textures.playerImg.get(10), Textures.playerImg.get(11), Textures.playerImg.get(12), Textures.playerImg.get(13));
		jumping_ani = new Animation(5, Textures.playerImg.get(16), Textures.playerImg.get(17),
				Textures.playerImg.get(18), Textures.playerImg.get(19), Textures.playerImg.get(20), Textures.playerImg.get(21), Textures.playerImg.get(22), Textures.playerImg.get(23));
		crouch_ani = new Animation(6, Textures.playerImg.get(4), Textures.playerImg.get(5), Textures.playerImg.get(6), Textures.playerImg.get(7));
		slide = new Animation(6, Textures.playerImg.get(24), Textures.playerImg.get(25), Textures.playerImg.get(26), Textures.playerImg.get(27), Textures.playerImg.get(28));
	}

	public void tick() {
		if(velX == 0 && onGround && !crouch) {
			idle.runAnimation();
			
			running.resetAnimation();
			jumping_ani.resetAnimation();
			crouch_ani.resetAnimation();
			slide.resetAnimation();
		}else if((velX > 0 || velX < 0) && onGround && !crouch && !sliding) {
			running.runAnimation();
			
			idle.resetAnimation();
			jumping_ani.resetAnimation();
			crouch_ani.resetAnimation();
			slide.resetAnimation();
		}else if(!onGround && !falling) {
			jumping_ani.runAnimation();
			
			idle.resetAnimation();
			running.resetAnimation();
			crouch_ani.resetAnimation();
			slide.resetAnimation();
		}else if(onGround && crouch && !sliding) {
			crouch_ani.runAnimation();
			
			idle.resetAnimation();
			running.resetAnimation();
			jumping_ani.resetAnimation();
			slide.resetAnimation();
		}else if(sliding) {
			slide.runAnimation();
			
			idle.resetAnimation();
			running.resetAnimation();
			jumping_ani.resetAnimation();
			crouch_ani.resetAnimation();
		}
		
		if(direction) {
			idle.mirrorAnimationW(true);
			running.mirrorAnimationW(true);
			jumping_ani.mirrorAnimationW(true);
			crouch_ani.mirrorAnimationW(true);
			slide.mirrorAnimationW(true);
		}else {
			idle.mirrorAnimationW(false);
			running.mirrorAnimationW(false);
			jumping_ani.mirrorAnimationW(false);
			crouch_ani.mirrorAnimationW(false);
			slide.mirrorAnimationW(false);
		}
		
		/*if(y == Game.HEIGHT) {
			onGround = true;
		}*/
		
		if(onGround) {
			if(keyInput.keysDown[1] == true && !sliding && (velX > 2 || velX < -1)) {
				if(!moving)
					crouch = true;
				else if(!crouch && sliding_timer_wait >= 10)
					sliding = true;
			}else crouch = false;
			if(keyInput.keysDown[2] == true) velX = velX + (-2 - velX) * (0.07f);
			if(keyInput.keysDown[3] == true) velX = velX + (3 - velX) * (0.07f);
			if(velX > 0)
				if((keyInput.keysDown[2] == true && keyInput.keysDown[3] == true) ||
						(keyInput.keysDown[2] == false && keyInput.keysDown[3] == false)) {
					velX = velX + (-2 - velX) * (0.07f);
					if(velX > -0.2 && velX < 0.2)
						velX = 0;
				}
			if(velX < 0)
				if((keyInput.keysDown[2] == true && keyInput.keysDown[3] == true) ||
						(keyInput.keysDown[2] == false && keyInput.keysDown[3] == false)) {
					velX = velX + (2 - velX) * (0.07f);
					if(velX > -0.2 && velX < 0.2)
						velX = 0;
				}
			if(keyInput.keysDown[4] == true && !jumping) {
				onGround = false;
				jumping = true;
				velY = -5;
			}
		}else if(!onGround && !sliding){
			if(keyInput.keysDown[2] == true) velX = velX + (-2 - velX) * (0.05f);
			if(keyInput.keysDown[3] == true) velX = velX + (2 - velX) * (0.05f);
			if(keyInput.keysDown[1] == true) velY += (9.8 - velY) * (0.05f);
		}
		
		/*if(crouch && velX != 0 && sliding_timer_wait >= 10) {
			sliding = true;
		}*/
		
		if(sliding) {
			sliding_timer++;
			if(!direction)velX = 3;
			else velX = -3;
			if(sliding_timer > 42) {
				sliding = false;
				if(keyInput.keysDown[1] && onGround)crouch = true;
				sliding_timer = 0;
				sliding_timer_wait = 0;
			}
			//velX = Game.clampDouble(velX, -3, 3);
		}else if(!sliding && crouch){
			//velX = Game.clampDouble(velX, -1, 1);
		}else {
			sliding_timer_wait++;
			if(sliding_timer_wait > 10) sliding_timer_wait = 10;
			//velX = Game.clampDouble(velX, -2, 2);
		}
		
		
		//misschien powerup voor reverse gravity
		
		
		
		if(velX < 0) {
			direction = true;
			moving = true;
		}else if(velX > 0) {
			direction = false;
			moving = true;
		}else {
			moving = false;
		}
		
		
		if(onGround) {
			velY = 0;
			jumping = false;
			falling = false;
		}else {
			velY = velY + 0.2;
			if(velY > 4 && jumping)
				falling = true;
			else if(velY > 0 && !jumping)
				falling = true;
			else
				falling = false;
		}
		
		if(y > 320) {
			x = 0;
			y = 0;
		}
		
		velY = Game.clampDouble(velY, -9.8, 9.8);
		x += velX;
		y += velY;
		
		x = Game.clamp(x, -13, 800-50);
		
		//y = Game.clamp(y, 0, Game.HEIGHT);
		
		
	}
	public void render(Graphics g) {
		g.setColor(Color.red);
		//g.fillRect(x, y, 32, 32);
		
		if(velX == 0 && onGround && !crouch) {
			idle.drawAnimation(g, x, y, 50, 37);
		}else if(onGround && !crouch && !sliding){
			running.drawAnimation(g, x, y);
		}else if(!onGround && !falling && jumping){
			jumping_ani.drawAnimation(g, x, y);
		}else if(!onGround && falling) {
			if(direction) {
				AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
				tx.translate(-Textures.playerImg.get(23).getWidth(null), 0);
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				BufferedImage temp = op.filter(Textures.playerImg.get(23), null);
				g.drawImage(temp, x, y, null);
				
			}else
				g.drawImage(Textures.playerImg.get(23), x, y, null);
		}else if(onGround && crouch && !sliding) {
			crouch_ani.drawAnimation(g, x, y);
		}else if(sliding) {
			slide.drawAnimation(g, x, y);
		}
		g.setColor(Color.pink);
		
		if(Game.showHitboxes) g.drawRect(x+19, y+6, 13, 30);
	}
	
	public Rectangle getBoundsRect() {
		return new Rectangle(x+19, y+6, 13, 30);
	}

}
