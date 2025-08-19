package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Objects;

/**
 * Manages the creation and placement of trees in the game world with consistent generation.
 * It determines where trees should be planted based on reproducible random generation
 * and creates the necessary tree components (trunk and leaves).
 */
public class Flora {
    private static final float HALF_SIZE = 2f;
    private static final String COMMA_SEP = ",";
    private static final int LEAVES_LAYER = Layer.DEFAULT - 1;
    private static final int FRUIT_LAYER = Layer.DEFAULT;
    private static final int TRUNK_LAYER = Layer.STATIC_OBJECTS;
    private final TerranGetter terrain;
    private final int baseSeed;
    private final float playerStartX;

    private static final Color TRUNK_BASE_COLOR = new Color(100, 50, 20);
    private static final Color LEAF_BASE_COLOR = new Color(50, 200, 30);
    private static final float TREE_PROBABILITY = 0.1f;
    private static final int MIN_TRUNK_HEIGHT_BLOCKS = 4;
    private static final int MAX_TRUNK_HEIGHT_BLOCKS = 7;
    private static final int LEAF_CROWN_SIZE_BLOCKS = 5;
    private static final float LEAF_PROBABILITY = 0.7f;
    private static final float FRUIT_PROBABILITY_PER_LEAF = 0.2f;
    private static final Color RED_FRUIT_COLOR = Color.RED;
    private static final Color ORANGE_FRUIT_COLOR = new Color(255, 165, 0);
    private static final int PLAYER_SAFE_ZONE_BLOCKS = 2;
    private final GameObjectRemover remover;
    private final GameObjectAdder adder;

    /**
     * Constructs a Flora object.
     * @param adder A function that adds a GameObject to the game.
     * @param remover A function that removes a GameObject from the game.
     * @param terrainGetter A function that returns the terrain height at a given x-coordinate.
     * @param seed A seed for random number generation to ensure repeatable tree placement.
     * @param playerStartX The starting X position of the player character.
     */
    public Flora(GameObjectAdder adder, GameObjectRemover remover,
                 TerranGetter terrainGetter, int seed, float playerStartX) {
        this.terrain = terrainGetter;
        this.adder = adder;
        this.remover = remover;
        this.baseSeed = seed;
        this.playerStartX = playerStartX;
    }

    /**
     * Creates trees within a specified horizontal range with consistent generation.
     * Trees are placed at random locations based on position-specific seeds,
     * ensuring the same trees appear in the same locations every time.
     *
     * @param minX The minimum x-coordinate for tree placement (inclusive).
     * @param maxX The maximum x-coordinate for tree placement (exclusive).
     * @return A list of all GameObjects created for the trees (trunks and leaves).
     */
    public List<GameObject> createInRange(int minX, int maxX) {
        List<GameObject> allTreeObjects = new ArrayList<>();

        int startX = (int) (Math.floor((float) minX / Block.SIZE) * Block.SIZE);
        int endX = (int) (Math.ceil((float) maxX / Block.SIZE) * Block.SIZE);

        for (int x = startX; x < endX; x += Block.SIZE) {
            if (isInPlayerSafeZone(x)) {
                continue;
            }

            Random positionRandom = new Random(Objects.hash(x, baseSeed));

            if (positionRandom.nextFloat() < TREE_PROBABILITY) {
                float groundHeight = terrain.accept(x);
                int trunkHeightBlocks =
                        positionRandom.nextInt(MAX_TRUNK_HEIGHT_BLOCKS - MIN_TRUNK_HEIGHT_BLOCKS + 1)
                                + MIN_TRUNK_HEIGHT_BLOCKS;
                int trunkHeightPixels = trunkHeightBlocks * Block.SIZE;

                Vector2 trunkTopLeft = new Vector2(x, groundHeight - trunkHeightPixels);

                // Create Trunk
                GameObject trunk = Trunk.create(
                        trunkTopLeft,
                        new Vector2(Block.SIZE, trunkHeightPixels),
                        new RectangleRenderable(ColorSupplier.approximateColor(TRUNK_BASE_COLOR))
                );
                adder.accept(trunk, TRUNK_LAYER);
                allTreeObjects.add(trunk);

                List<GameObject> leaves = createLeafCrown(trunkTopLeft, positionRandom);
                for (GameObject leaf : leaves) {
                    adder.accept(leaf, LEAVES_LAYER);
                    allTreeObjects.add(leaf);
                }

                List<Fruit> fruits = createFruits(leaves, trunkTopLeft, positionRandom);
                for (Fruit fruit : fruits) {
                    adder.accept(fruit, FRUIT_LAYER);
                    allTreeObjects.add(fruit);
                }
            }
        }
        return allTreeObjects;
    }

    /**
     * Checks whether a given position is within the avatar's safe zone.
     * @param x The position to check.
     * @return true if the position is within the safe zone, false otherwise.
     */
    private boolean isInPlayerSafeZone(int x) {
        if (playerStartX == Float.MAX_VALUE) {
            return false;
        }

        if (Float.isNaN(playerStartX) || Float.isInfinite(playerStartX)) {
            return false;
        }

        float safeZoneRadius = PLAYER_SAFE_ZONE_BLOCKS * Block.SIZE;
        float distance = Math.abs(x - playerStartX);

        return distance <= safeZoneRadius;
    }

    /**
     * Creates the square crown of leaves for a tree with consistent generation.
     *
     * @param trunkTopLeft The top-left corner of the trunk.
     * @param random       The random generator for this specific tree.
     * @return A list of Leaf GameObjects.
     */
    private List<GameObject> createLeafCrown(Vector2 trunkTopLeft, Random random) {
        List<GameObject> leaves = new ArrayList<>();
        int crownSize = LEAF_CROWN_SIZE_BLOCKS;
        int crownTopY = (int) trunkTopLeft.y() - (crownSize - 1) * Block.SIZE;
        int crownLeftX = (int) trunkTopLeft.x() - (crownSize / (int) HALF_SIZE) *
                Block.SIZE + (int) (Block.SIZE / HALF_SIZE);

        for (int row = 0; row < crownSize; row++) {
            for (int col = 0; col < crownSize; col++) {
                if (random.nextFloat() < LEAF_PROBABILITY) {
                    Vector2 leafTopLeft = new Vector2(crownLeftX + col * Block.SIZE,
                            crownTopY + row * Block.SIZE);

                    Random leafRandom = new Random(Objects.hash(
                            (int)leafTopLeft.x(), (int)leafTopLeft.y(), baseSeed));

                    GameObject leaf = Leaf.create(
                            leafTopLeft,
                            new Vector2(Block.SIZE, Block.SIZE),
                            new RectangleRenderable(ColorSupplier.approximateColor(LEAF_BASE_COLOR)),
                            leafRandom
                    );
                    leaves.add(leaf);
                }
            }
        }
        return leaves;
    }

    /**
     * Creates fruits positioned in empty spaces within the crown grid with consistent generation.
     * @param leaves The list of leaves in the crown.
     * @param trunkTopLeft The top-left corner of the trunk for reference.
     * @param random The random generator for this specific tree.
     * @return A list of Fruit GameObjects.
     */
    private List<Fruit> createFruits(List<GameObject> leaves, Vector2 trunkTopLeft, Random random) {
        List<Fruit> fruits = new ArrayList<>();
        int crownSize = LEAF_CROWN_SIZE_BLOCKS;
        int crownTopY = (int) trunkTopLeft.y() - (crownSize - 1) * Block.SIZE;
        int crownLeftX = (int) trunkTopLeft.x() - (crownSize / (int)HALF_SIZE)
                * Block.SIZE + (int) (Block.SIZE / HALF_SIZE);
        // Create a set of occupied positions by leaves
        java.util.Set<String> leafPositions = new java.util.HashSet<>();
        for (GameObject leaf : leaves) {
            Vector2 leafPos = leaf.getTopLeftCorner();
            leafPositions.add(leafPos.x() + "," + leafPos.y());
        }
        // Go through all crown positions and place fruits in empty spots
        for (int row = 0; row < crownSize; row++) {
            for (int col = 0; col < crownSize; col++) {
                Vector2 gridPosition = new Vector2(crownLeftX + col * Block.SIZE,
                        crownTopY + row * Block.SIZE);
                String positionKey = gridPosition.x() + COMMA_SEP + gridPosition.y();

                // Only place fruit if this position is empty (no leaf) and random chance succeeds
                if (!leafPositions.contains(positionKey) && random.nextFloat() < FRUIT_PROBABILITY_PER_LEAF) {
                    // Randomly choose between red and orange fruit color
                    Color fruitColor = random.nextBoolean() ? RED_FRUIT_COLOR : ORANGE_FRUIT_COLOR;

                    Fruit fruit = new Fruit(gridPosition, adder,remover, fruitColor);
                    fruits.add(fruit);
                }
            }
        }
        return fruits;
    }

    /**
     * Functional interface for adding a {@link GameObject} to a specific layer.
     * Typically used to delegate object addition to the main game logic
     * or rendering system.
     */
    @FunctionalInterface
    public interface GameObjectAdder {
        /**
         * Adds the given {@code GameObject} to the specified rendering layer.
         *
         * @param obj the game object to be added
         * @param layer the layer index to which the object should be added
         */
        void accept(GameObject obj, int layer);
    }

    /**
     * Functional interface for removing a {@link GameObject} from a specific layer.
     * Enables decoupled logic for removing game objects at runtime.
     */
    @FunctionalInterface
    public interface GameObjectRemover {
        /**
         * Removes the given {@code GameObject} from the specified rendering layer.
         *
         * @param obj the game object to be removed
         * @param layer the layer index from which the object should be removed
         */
        void accept(GameObject obj, int layer);
    }

    /**
     * Functional interface for retrieving terrain height at a given x-coordinate.
     * Useful for terrain generation, physics checks, or object placement
     * relative to the ground.
     */
    @FunctionalInterface
    public interface TerranGetter {
        /**
         * Returns the y-coordinate (height) of the terrain at the given x-coordinate.
         *
         * @param x the x-coordinate for which to retrieve the terrain height
         * @return the corresponding terrain height (y-coordinate)
         */
        float accept(float x);
    }

}