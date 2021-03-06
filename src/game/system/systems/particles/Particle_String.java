package game.system.systems.particles;

import game.enums.ID;
import game.system.main.Game;
import game.system.systems.gameObject.GameObject;
import game.textures.Fonts;

import java.awt.*;

public class Particle_String extends GameObject {
    private int lifetime;
    private float velX, velY;
    private float buffer_x, buffer_y;
    private ParticleSystem ps;
    private int alpha;
    private String text;

    public Particle_String(int x, int y, float velX, float velY, int lifetime, String text) {
        super(x, y, 0, ID.NULL);
        this.buffer_x = x;
        this.buffer_y = y;
        this.lifetime = lifetime;
        this.velX = velX;
        this.velY = velY;
        this.ps = Game.gameController.getPs();
        this.alpha = 255;
        this.text = text;
    }

    public void tick() {
        buffer_x += velX;
        buffer_y += velY;
        x = Math.round(buffer_x);
        y = Math.round(buffer_y);
        lifetime--;
        alpha = alpha - (alpha / lifetime);
        if (lifetime <= 1) {
            ps.removeParticle(this);
        }

    }

    public void render(Graphics g) {
        g.setColor(new Color(255, 255, 255, alpha));
        g.setFont(Fonts.default_fonts.get(5));
        g.drawString(text, x, y);
    }
}
