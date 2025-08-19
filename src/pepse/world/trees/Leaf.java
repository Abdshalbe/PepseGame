package pepse.world.trees;

import danogl.GameObject;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.components.GameObjectPhysics;
import pepse.world.Block;

import java.util.Random;

/**
 * Represents a single leaf on a tree.
 * It has a visual representation and animates with subtle wind movements.
 */
public class Leaf  {

    private static final float MIN_ANGLE = -5f;
    private static final float MAX_ANGLE = 5f;
    private static final float MIN_WIDTH_FACTOR = 0.9f;
    private static final float MAX_WIDTH_FACTOR = 1.1f;
    private static final float TRANSITION_TIME = 2f;
    private static final float MAX_INITIAL_DELAY = 1f;
    private static final String TAG_LEAF = "leaf";


    /**
     * Constructs a new Leaf object.
     * @param topLeftCorner The top-left position of the leaf.
     * @param dimensions The dimensions of the leaf.
     * @param renderable The visual representation of the leaf.
     * @param random A Random object for varied animation timings.
     */
    public static GameObject create(Vector2 topLeftCorner, Vector2 dimensions,
                                    Renderable renderable, Random random) {
        GameObject leaf = new Block(topLeftCorner, renderable);

        leaf.setDimensions(dimensions);
        leaf.setTag(TAG_LEAF);
        leaf.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
        startWindAnimation(random, leaf,dimensions);
        return leaf;
    }

    /**
     * Initiates the wind animation for the leaf with a random delay.
     * This uses ScheduledTask to delay the start of the Transition.
     */
    private static void startWindAnimation(Random random, GameObject leaf,
                                           Vector2 originalDimensions) {
        float initialDelay = random.nextFloat() * MAX_INITIAL_DELAY;

        new ScheduledTask(
                leaf,
                initialDelay,
                false,
                () -> {
                    new Transition<>(
                            leaf,
                            (angle) -> leaf.renderer().setRenderableAngle(angle),
                            MIN_ANGLE,
                            MAX_ANGLE,
                            Transition.CUBIC_INTERPOLATOR_FLOAT,
                            TRANSITION_TIME,
                            Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                            null
                    );

                    new Transition<>(
                            leaf,
                            (widthFactor) ->
                                    leaf.setDimensions(new Vector2(originalDimensions.x() * widthFactor,
                                            originalDimensions.y())),
                            MIN_WIDTH_FACTOR,
                            MAX_WIDTH_FACTOR,
                            Transition.CUBIC_INTERPOLATOR_FLOAT,
                            TRANSITION_TIME,
                            Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                            null
                    );
                }
        );
    }
}