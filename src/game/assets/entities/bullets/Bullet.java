package game.assets.entities.bullets;

import game.assets.items.item.CanAttack;
import game.enums.ID;
import game.system.helpers.Logger;
import game.system.main.Game;
import game.system.systems.gameObject.Bounds;
import game.system.systems.gameObject.Destroyable;
import game.system.systems.gameObject.GameObject;
import game.system.systems.hitbox.Hitbox;
import game.system.systems.hitbox.HitboxContainer;
import game.textures.TEXTURE_LIST;
import game.textures.Texture;

import java.awt.*;
import java.util.LinkedList;

public class Bullet extends GameObject implements game.system.systems.gameObject.Bullet {
    private final float max_vel = 2f;
    private int lifeTime = 300;
    private int angle;
    private LinkedList<GameObject> hit_objects = new LinkedList<>();
    private GameObject created_by;

    public Bullet(int x, int y, int z_index, int angle, GameObject created_by) {
        super(x, y, z_index, ID.Bullet);
        this.tex = new Texture(TEXTURE_LIST.bullets, 1, 0);
        this.angle = angle;
        velX = (float) (max_vel*Math.cos(Math.toRadians(angle)));
        velY = (float) (max_vel*Math.sin(Math.toRadians(angle)));
        this.created_by = created_by;
        hit_objects.add(created_by);
    }

    @Override
    public void tick() {
        buffer_x += velX;
        buffer_y += velY;

        x = Math.round(buffer_x);
        y = Math.round(buffer_y);

        if(lifeTime <= 0) {
            destroy();
        }

        lifeTime--;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(tex.getTexure(), x-8, y-8, null);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x-5, y-5, 10, 10);
    }

    @Override
    public int getDamage() {
        return 2;
    }

    @Override
    public void addHitObject(GameObject object) {
        hit_objects.add(object);
    }

    @Override
    public LinkedList<GameObject> getHitObjects() {
        return hit_objects;
    }

    @Override
    public GameObject getCreatedBy() {
        return created_by;
    }

    @Override
    public void destroy() {
        Logger.print("destroy");
        Game.world.getHandler().removeBullet(this);
    }
}
