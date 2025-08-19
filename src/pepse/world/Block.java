package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a single terrain block with fixed size and immovable physics.
 */
public class Block extends GameObject{
    /**
     * default block size 30x30
     * */
    public static final int SIZE = 30;

    /**
     * Constructs a new Block object.
     * @param topLeftCorner The top-left position of the block.
     * @param renderable The visual representation of the block.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }
}