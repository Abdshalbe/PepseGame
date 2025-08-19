package pepse.world;

import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
/**
 * cloud and raindrop management
 * */
public final class Cloud {
    private static final float RAINDROP_FADE_TIME = 3f;
    private static final int MAX_VALUE = 3;
    private static final int HALF_VALUE = 2;
    private static final int UNRETURNED_VAL = 2;

    /**
     * Functional interface for adding a {@link GameObject} to the game world.
     * This callback allows external modules to request the addition of objects
     * to a specific rendering layer.
     */
    @FunctionalInterface
    public interface GameObjectAdder {
        /**
         * Adds the given {@code GameObject} to the specified layer.
         *
         * @param go the game object to be added
         * @param layer the rendering layer in which to place the object
         */
        void add(GameObject go, int layer);
    }

    /**
     * Functional interface for removing a {@link GameObject} from the game world.
     * This callback enables controlled removal of objects from a specific rendering layer.
     */
    @FunctionalInterface
    public interface GameObjectRemover {
        /**
         * Removes the specified {@code GameObject} from the given layer.
         *
         * @param go the game object to be removed
         * @param layer the rendering layer from which to remove the object
         */
        void remove(GameObject go, int layer);
    }

    /**
     * Functional interface for detecting jump state of the avatar.
     * Updated to check if avatar can jump (is on ground) rather than just if jump key is pressed.
     */
    @FunctionalInterface
    public interface JumpDetector {
        /**
         * Checks whether the avatar is currently attempting to jump AND is on the ground.
         * This should return true only when the jump key is pressed and the avatar is grounded.
         *
         * @return {@code true} if the avatar is jumping from the ground; {@code false} otherwise
         */
        boolean isAvatarJumpingFromGround();
    }


    /* ==== constants ======================================================== */

    private static final Color  BASE_CLOUD_COLOR   = new Color(255, 255, 255);
    private static final int    CLOUD_LAYER        = Layer.BACKGROUND + 11;
    private static final int    RAIN_LAYER         = Layer.BACKGROUND;
    private static final float  DRIFT_SPEED_FACTOR = 1f;     // spec 8.2
    private static final String CLOUD_TAG          = "cloud";
    private static final float  CLOUD_Y_FACTOR     = 4f;     // 1/6 of window height
    private static final String  RAIN_TAG   = "raindrop";
    private static final Color   RAIN_COLOR = new Color(100, 100, 255);
    private static final Vector2 RAIN_SIZE  = new Vector2(10, 10); // Made bigger and square
    private static final float   RAIN_SPEED = 200f;
    private static final float   CLOUD_BLOCK_SIZE = Block.SIZE / 1.5f; // Made blocks smaller (half size)
    private static final Vector2 DROP_OFFSET = new Vector2(0, 5);
    private static final Random RAND = new Random();

    // Store cloud blocks for rain spawning
    private static final List<GameObject> cloudBlocks = new ArrayList<>();
    private static boolean lastJumpFromGroundState = false;

    /* ==== no instances ===================================================== */
    private Cloud() { }

    /* ==== public API ===================================================== */

    /**
     * Builds and registers an animated cloud.
     *
     * @param windowDim     full window dimensions (camera-space)
     * @param cycleLength   simulation-day length – horizontal drift scales with it
     * @param adder         callback that adds a {@link GameObject} at the given layer
     * @param remover       callback that removes a {@link GameObject} from the given layer
     * @param jumpDetector  tells whether the avatar is currently jumping from ground
     */
    public static void create(Vector2              windowDim,
                              float                cycleLength,
                              GameObjectAdder      adder,
                              GameObjectRemover    remover,
                              JumpDetector         jumpDetector) {

        final List<List<Integer>> PATTERN = List.of(
                List.of(0, 1, 1, 0, 0, 0),
                List.of(1, 1, 1, 0, 1, 0),
                List.of(1, 1, 1, 1, 1, 1),
                List.of(1, 1, 1, 1, 1, 1),
                List.of(0, 1, 1, 1, 0, 0),
                List.of(0, 0, 0, 0, 0, 0));

        final float patternWidth  = PATTERN.get(0).size() * CLOUD_BLOCK_SIZE;
        final Vector2 cloudOrigin = new Vector2(-patternWidth, windowDim.y() / CLOUD_Y_FACTOR);

        Renderable pixelRenderable =
                new RectangleRenderable(ColorSupplier.approximateMonoColor(BASE_CLOUD_COLOR));

        // Clear previous cloud blocks
        cloudBlocks.clear();

        for (int row = 0; row < PATTERN.size(); ++row) {
            for (int col = 0; col < PATTERN.get(row).size(); ++col) {
                if (PATTERN.get(row).get(col) == 0) continue; // transparent

                Vector2 topLeft = cloudOrigin.add(new Vector2(col * CLOUD_BLOCK_SIZE,
                        row * CLOUD_BLOCK_SIZE)); // Use smaller block size

                // Create smaller cloud block
                GameObject pixel = new GameObject(topLeft,
                        new Vector2(CLOUD_BLOCK_SIZE, CLOUD_BLOCK_SIZE),
                        pixelRenderable);
                pixel.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
                pixel.setTag(CLOUD_TAG);
                adder.add(pixel, CLOUD_LAYER);

                // Add to our list for rain spawning
                cloudBlocks.add(pixel);

                Vector2 initialCenter = pixel.getCenter(); // use real centre
                float   travel        = windowDim.x() + patternWidth;

                new Transition<>(pixel,
                        dx -> pixel.setCenter(initialCenter.add(new Vector2(dx, 0))),
                        0f,
                        travel,
                        Transition.LINEAR_INTERPOLATOR_FLOAT,
                        cycleLength / DRIFT_SPEED_FACTOR,
                        Transition.TransitionType.TRANSITION_LOOP,
                        null);
            }
        }

        // Add jump detection component to one of the blocks (or create a separate controller)
        if (!cloudBlocks.isEmpty()) {
            cloudBlocks.get(0).addComponent(dt -> {
                boolean currentJumpFromGroundState = jumpDetector.isAvatarJumpingFromGround();

                // Check if jump from ground was just initiated (transition from false to true)
                if (currentJumpFromGroundState && !lastJumpFromGroundState) {
                    spawnRandomRainDrops(adder, remover);
                }

                lastJumpFromGroundState = currentJumpFromGroundState;
            });
        }
    }

    /* ==== internal helpers ================================================ */

    /**
     * Spawns rain drops from random cloud blocks when jump is pressed from ground.
     * @param adder a callback that adds a GameObject at the given layer
     * @param remover a callback that removes a GameObject from the given layer
     */
    private static void spawnRandomRainDrops(GameObjectAdder adder, GameObjectRemover remover) {
        // Ensure at least 3 drops, up to half of available blocks
        int maxDrops = Math.max(MAX_VALUE, cloudBlocks.size() / HALF_VALUE);
        int numDrops = RAND.nextInt(maxDrops - UNRETURNED_VAL) + MAX_VALUE;
        numDrops = Math.min(numDrops, cloudBlocks.size());

        // Create a copy of the list and shuffle it to get random blocks
        List<GameObject> shuffledBlocks = new ArrayList<>(cloudBlocks);
        java.util.Collections.shuffle(shuffledBlocks);

        // Spawn drops from the first numDrops blocks
        for (int i = 0; i < numDrops; i++) {
            GameObject block = shuffledBlocks.get(i);
            spawnDrop(block.getCenter(), adder, remover);
        }
    }

    /**
     * spawns a raindrop at the given position.
     * @param startCenter starting position (camera-space)
     * @param adder a callback that adds a GameObject at the given layer
     * @param remover a callback that removes a GameObject from the given layer
     * */
    private static void spawnDrop(Vector2 startCenter,
                                  GameObjectAdder adder,
                                  GameObjectRemover remover) {

        GameObject drop = new GameObject(startCenter.add(DROP_OFFSET),
                RAIN_SIZE, // Now bigger and square (15x15)
                new RectangleRenderable(RAIN_COLOR));
        drop.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        drop.setTag(RAIN_TAG);
        drop.transform().setVelocity(new Vector2(0, RAIN_SPEED));

        // fade-out and self-remove (longer duration)
        new Transition<>(drop,
                drop.renderer()::setOpaqueness,
                1f,
                0f,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                RAINDROP_FADE_TIME, // Changed from 1f to 3f for longer fade time
                Transition.TransitionType.TRANSITION_ONCE,
                () -> remover.remove(drop, RAIN_LAYER));

        adder.add(drop, RAIN_LAYER);
    }

}