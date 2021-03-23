package game.assets.entities.bullets;

import game.system.systems.gameObject.GameObject;
import game.textures.TEXTURE_LIST;
import game.textures.Texture;

import java.awt.*;

public class EnemyBullet extends Bullet {
    public EnemyBullet(int x, int y, int z_index, int angle, GameObject created_by) {
        super(x, y, z_index, angle, created_by);
        this.tex = new Texture(TEXTURE_LIST.bullets, 0, 2);
        this.max_vel = 1.7f;
        updateVelocity();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x-3, y-3, 6, 6);
    }
}
