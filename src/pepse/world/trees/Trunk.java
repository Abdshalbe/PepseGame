package pepse.world.trees;


import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Block;


/**
 * Represents the trunk of a tree.
 * It's a static, immovable object that the avatar should collide with.
 */
public class Trunk {
    private static final String TRUNK_TAG = "trunk";

    /**
     * Constructs a new Trunk object.
     * @param topLeftCorner The top-left position of the trunk.
     * @param dimensions The dimensions of the trunk.
     * @param renderable The visual representation of the trunk.
     */

    public static Block create(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable) {
        Block block = new Block(topLeftCorner, renderable);
        block.setDimensions(dimensions);
        block.setTopLeftCorner(topLeftCorner);
        block.setTag(TRUNK_TAG);
        return block;
    }
}