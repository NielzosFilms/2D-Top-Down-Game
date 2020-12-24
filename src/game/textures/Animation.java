package game.textures;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public class Animation implements Serializable {

	private long last_frame_time;
	private long frame_duration;
	private int frames;
	private int index = 0;
	private int count = 0;
	
	private boolean mirrorW;
	private boolean ended = false;
	
	private Texture[] images;
	private Texture currentImg;
	
	public Animation(long frame_duration, Texture... args) {
		this.frame_duration = frame_duration;
		this.mirrorW = false;
		images = new Texture[args.length];
		for(int i = 0; i < args.length; i++) {
			images[i] = args[i];
		}
		frames = args.length;
		currentImg = images[0];
	}
	
	public void runAnimation() {
		/*index++;
		if(index > speed) {
			index = 0;
			nextFrame();
		}*/
		long now = System.currentTimeMillis();
		long diff = now - last_frame_time;
		if(diff >= frame_duration) {
			last_frame_time = now;
			nextFrame();
		}
	}
	
	public void resetAnimation() {
		index = 0;
		count = 0;
		currentImg = images[0];
		ended = false;
	}
	
	public void mirrorAnimationW(boolean temp) {
		this.mirrorW = temp;
	}
	
	private void nextFrame() {
		for(int i = 0; i < frames; i++) {
			if(count == i)
				currentImg = images[i];
		}
		count++;
		if(count >= frames){
			count = 0;
			ended = true;
		}
	}
	
	public void drawAnimation(Graphics g, int x, int y) {
		if(mirrorW) {
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-currentImg.getTexure().getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage temp = op.filter(currentImg.getTexure(), null);
			g.drawImage(temp, x, y, null);
			
		}else
			g.drawImage(currentImg.getTexure(), x, y, null);
	}
	
	public void drawAnimation(Graphics g, int x, int y, int scaleX, int scaleY) {
		if(mirrorW) {
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-currentImg.getTexure().getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage temp = op.filter(currentImg.getTexure(), null);
			g.drawImage(temp, x, y, scaleX, scaleY, null);
			
		}else
			g.drawImage(currentImg.getTexure(), x, y, scaleX, scaleY, null);
	}

	public void drawAnimationRotated(Graphics g, int x, int y, int scaleX, int scaleY, int anchX, int anchY, int rotation) {
		if(mirrorW) {
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-currentImg.getTexure().getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage temp = op.filter(currentImg.getTexure(), null);
			//g.drawImage(temp, x, y, scaleX, scaleY, null);
			ImageFilters.renderImageWithRotation(g, temp, x, y, scaleX, scaleY, anchX, anchY, rotation);
		}else {
			//g.drawImage(currentImg.getTexure(), x, y, scaleX, scaleY, null);
			ImageFilters.renderImageWithRotation(g, currentImg.getTexure(), x, y, scaleX, scaleY, anchX, anchY, rotation);
		}
	}

	public boolean animationEnded() {
		return ended;
	}
	
}
