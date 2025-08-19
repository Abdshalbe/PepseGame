package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * The Terrain class is responsible for procedurally generating ground blocks in the game world.
 * It uses smooth noise to determine terrain height at each x-coordinate and constructs a vertical
 * column of blocks for each x. The terrain is rendered using a base ground color and is tagged
 * appropriately for collision detection.
 */
public class Terrain {
    // constants
    /**initial ground height*/
    public static final float GROUND_LENGTH = 0.6f;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final int TERRAIN_DEPTH = 20;
    private static final String GROUND_TAG = "ground";
    private static final float NOISE_FACTOR = Block.SIZE * 10;

    private final float groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;
    private ArrayList<Block> blocks;

    /**
     * Constructor for Terrain class
     * @param windowDimensions window dimensions
     * @param seed seed for terrain generation
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_LENGTH;
        this.noiseGenerator = new NoiseGenerator(seed, (int) groundHeightAtX0);
    }

    /**
     * Get ground height at x
     * @param x x coordinate
     * @return ground height at x
     */
    public float groundHeightAt(float x) {
        float noise = (float) noiseGenerator.noise(x, NOISE_FACTOR);
        return groundHeightAtX0 + noise;
    }

    /**
     * Create blocks in the given X range
     * @param minX minimum X
     * @param maxX maximum X
     * @return list of blocks
     */
    public List<Block> createInRange(int minX, int maxX) {
        blocks = new ArrayList<>();
        int newMin = roundToNearestStep.apply(minX, Direction.DOWN);
        int newMax = roundToNearestStep.apply(maxX, Direction.UP);
        for (int x = newMin; x <= newMax; x += Block.SIZE) {
            createColumn(x);
        }
        return blocks;
    }
    /**
     * create a column of blocks at x
     * @param x x coordinate of column
     * */
    private void createColumn(int x){
        float topY = groundHeightAt(x);
        int topBlockY = roundToNearestStep.apply((int) topY, Direction.DOWN);
        for (int i = 0; i < TERRAIN_DEPTH; i++) {
            int y = topBlockY + i * Block.SIZE;
            Renderable blockImage = new RectangleRenderable(
                    ColorSupplier.approximateColor(BASE_GROUND_COLOR));
            Block block = new Block(new Vector2(x, y), blockImage);
            block.setTag(GROUND_TAG);
            blocks.add(block);
        }
    }

    /** Direction enum for rounding */
    enum Direction {
        UP, DOWN
    }

    /** BiFunction for rounding to nearest block grid */
    BiFunction<Integer, Direction, Integer> roundToNearestStep = (curr, direction) -> {
        int step = Block.SIZE;
        return direction == Direction.UP
                ? ((curr + step - 1) / step) * step   // ceiling
                : (curr / step) * step;              // floor
    };
}
