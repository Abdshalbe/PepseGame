package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.Layer;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.PepseGameManager;
import pepse.world.Avatar;
import pepse.world.Block;

import java.awt.Color;
import java.util.Random; // Import Random for choosing fruit color

/**
 * Represents a fruit that can be collected by the avatar for an energy boost.
 * Fruits reappear after a certain amount of time after being collected.
 */
public class Fruit extends GameObject {

    /**
     * The size of the fruit, smaller than a leaf.
     */
    public static final float SIZE = Block.SIZE;

    private static final Color RED_FRUIT_COLOR = Color.RED;
    private static final Color ORANGE_FRUIT_COLOR = new Color(255, 165, 0);
    private static final String FRUIT_TAG = "fruit";
    private static final int ENERGY_BOOST = 10;
    private final Flora.GameObjectAdder adder;
    private final Flora.GameObjectRemover remover;
    private boolean isEaten = false;

    /**
     * @param topLeftCorner The position of the fruit in the game world.
     *                       The fruit will be rendered as a circle with this size.
     * @param adder The function to add a GameObject to the game world.
     *              This function is used to reappear the fruit after it is eaten.
     * @param remover The function to remove a GameObject from the game world.
     *                This function is used to remove the fruit when it is eaten.
     */
    public Fruit(Vector2 topLeftCorner, Flora.GameObjectAdder adder, Flora.GameObjectRemover remover) {
    super(topLeftCorner, new Vector2(SIZE, SIZE), new OvalRenderable(RED_FRUIT_COLOR));
        this.adder = adder;
        this.remover = remover;
        setTag(FRUIT_TAG);

    }

    /**
     * Constructs a new Fruit object with a specified color.
     * This overloaded constructor allows creating fruits of different colors.
     *
     * @param topLeftCorner The position of the fruit in the game world.
     *                       The fruit will be rendered as a circle with this size.
     * @param adder The function to add a GameObject to the game world.
     *              This function is used to reappear the fruit after it is eaten.
     * @param remover The function to remove a GameObject from the game world.
     *                This function is used to remove the fruit when it is eaten.
     * @param fruitColor The color of this fruit.
     */
    public Fruit(Vector2 topLeftCorner, Flora.GameObjectAdder adder,
                 Flora.GameObjectRemover remover, Color fruitColor) {
        super(topLeftCorner, new Vector2(SIZE, SIZE), new OvalRenderable(fruitColor));
        this.adder = adder;
        this.remover = remover;
        setTag(FRUIT_TAG);
    }


    /**
     * Defines the behavior of the fruit upon collision with another GameObject.
     *
     * @param other The GameObject that this fruit collided with.
     * @param collision Information about the collision.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (!isEaten && other.getTag().equals("avatar")) {
            isEaten = true;
            Avatar avatar = (Avatar) other;
            avatar.addEnergy(ENERGY_BOOST);

            remover.accept(this, Layer.DEFAULT);

            // When the fruit reappears, make it randomly red or orange
            new ScheduledTask(
                    avatar,
                    PepseGameManager.CYCLE_LENGTH,
                    false,
                    () -> {
                        // Randomly choose between red and orange for the new fruit
                        Random random = new Random();
                        Color newFruitColor = random.nextBoolean() ? RED_FRUIT_COLOR : ORANGE_FRUIT_COLOR;
                        Fruit newFruit = new Fruit(this.getTopLeftCorner(), adder,remover, newFruitColor);
                        adder.accept(newFruit, Layer.DEFAULT);
                    }
            );
        }
    }
}