package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;

import pepse.util.EnergyDisplay;
import pepse.world.*;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * The PepseGameManager class is responsible for initializing and managing
 * the main components of the game world including the environment, avatar,
 * lighting effects, user interface, and infinite world generation.
 */
public class PepseGameManager extends GameManager {
    /**the cycle length of the day night cycle.*/
    public static final float CYCLE_LENGTH = 30f;
    private static final int SEED = 5;
    private static final int WORLD_BUFFER_SIZE = 800;
    private static final int WORLD_CLEANUP_DISTANCE = 1200;
    private static final int CHUNK_SIZE = Block.SIZE * 10;
    private static final float UPDATE_THRESHOLD = Block.SIZE * 0.5f;
    private static final Vector2 ENERGY_DISPLAY_WIDTH = Vector2.ONES.mult(20);
    private static final float INITIAL_AVATAR_HEIGHT = 2.5f;
    private static final float CENTER_VAL = 2;
    private static final String TRUNK_TAG = "trunk";
    private static final int SUN_LAYER = Layer.BACKGROUND + 10;
    private static final int HALO_LAYER = Layer.BACKGROUND + 9;
    private static final float HALF = 2f;

    private Avatar avatar;
    private Vector2 windowDimensions;
    private GameObjectCollection gameObjects;
    private final Map<Integer, List<Block>> terrainChunks = new HashMap<>();
    private final Map<Integer, List<GameObject>> floraChunks = new HashMap<>();
    private float lastWorldUpdateX = 0;
    private Terrain terrain;
    private EnergyDisplay energyDisplay;

    /**
     * Initializes the entire game world, including environment and player avatar.
     * Sets up background layers, terrain, sun, halo, avatar, energy display, and infinite world.
     * @param imageReader the image reader.
     * @param soundReader the sound reader.
     * @param inputListener the user input listener.
     * @param windowController the window controller.
     */
    @Override
    public void initializeGame(ImageReader imageReader,
                               SoundReader soundReader,
                               UserInputListener inputListener,
                               WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);

        windowDimensions = windowController.getWindowDimensions();
        gameObjects = gameObjects();
        // Create sky background
        createSky();
        // Create night and sun effect overlay
        createNight();
        createSun();
        // create Cloud
        cloudCreator();
        createTerrain();
        // Create avatar
        createAvatar(inputListener, imageReader);
        // Set up camera to follow avatar
        setupCamera(windowController);
        // Create and attach energy display UI
        createEnergyDisplay();

    }
    /**
     * Initializes the world around the avatar's starting position.
     */
    private void initializeInitialWorld(float avatarX) {
        float minX = avatarX - WORLD_BUFFER_SIZE;
        float maxX = avatarX + WORLD_BUFFER_SIZE;

        createWorldInRange(minX, maxX);
    }
    /**
     * Updates the infinite world based on avatar position.
     */
    private void updateInfiniteWorld() {
        float avatarX = avatar.getCenter().x();


        if (Math.abs(avatarX - lastWorldUpdateX) > UPDATE_THRESHOLD) {
            float minX = avatarX - WORLD_BUFFER_SIZE;
            float maxX = avatarX + WORLD_BUFFER_SIZE;

            createWorldInRange(minX, maxX);

            cleanupDistantObjects(avatarX);

            lastWorldUpdateX = avatarX;
        }
    }
    /**
     * Creates world objects (terrain and flora) in the specified range.
     */
    private void createWorldInRange(float minX, float maxX) {
        int startChunk = (int) Math.floor(minX / CHUNK_SIZE);
        int endChunk = (int) Math.floor(maxX / CHUNK_SIZE);

        for (int chunkId = startChunk; chunkId <= endChunk; chunkId++) {
            createChunkIfNeeded(chunkId);
        }
    }
    /**
     * Creates a chunk of world if it doesn't exist.
     */
    private void createChunkIfNeeded(int chunkId) {
        if (!terrainChunks.containsKey(chunkId)) {
            float chunkStartX = chunkId * CHUNK_SIZE;
            float chunkEndX = chunkStartX + CHUNK_SIZE;


            Terrain chunkTerrain = new Terrain(windowDimensions,
                    java.util.Objects.hash(chunkId, SEED));
            List<Block> terrainBlocks = chunkTerrain.createInRange((int)chunkStartX, (int)chunkEndX);

            for (Block block : terrainBlocks) {
                gameObjects.addGameObject(block, Layer.STATIC_OBJECTS);
            }
            terrainChunks.put(chunkId, terrainBlocks);

            Flora chunkFlora = new Flora( gameObjects()::addGameObject,
                    gameObjects()::removeGameObject,
                    chunkTerrain::groundHeightAt,
                    java.util.Objects.hash(chunkId, SEED),
                    Float.MAX_VALUE);
            List<GameObject> floraObjects = chunkFlora.createInRange((int)chunkStartX, (int)chunkEndX);
            floraChunks.put(chunkId, floraObjects);
        }
    }
    /**
     * Removes objects that are too far from the avatar to save memory.
     */
    private void cleanupDistantObjects(float avatarX) {
        Iterator<Map.Entry<Integer, List<Block>>> terrainIter = terrainChunks.entrySet().iterator();
        while (terrainIter.hasNext()) {
            Map.Entry<Integer, List<Block>> entry = terrainIter.next();
            int chunkId = entry.getKey();
            float chunkCenterX = chunkId * CHUNK_SIZE + CHUNK_SIZE / HALF;

            if (Math.abs(chunkCenterX - avatarX) > WORLD_CLEANUP_DISTANCE) {
                for (Block block : entry.getValue()) {
                    gameObjects.removeGameObject(block, Layer.STATIC_OBJECTS);
                }
                terrainIter.remove();

                List<GameObject> floraObjects = floraChunks.get(chunkId);
                if (floraObjects != null) {
                    for (GameObject obj : floraObjects) {
                        if (obj.getTag() != null && obj.getTag().equals(TRUNK_TAG)) {
                            gameObjects.removeGameObject(obj, Layer.STATIC_OBJECTS);
                        } else {
                            gameObjects.removeGameObject(obj, Layer.DEFAULT);
                            gameObjects.removeGameObject(obj, Layer.DEFAULT - 1);
                        }
                    }
                    floraChunks.remove(chunkId);
                }
            }
        }
    }


    /**
     * Creates the sky GameObject to be displayed as background.
     */
    private void createSky() {
        GameObject sky = Sky.create(windowDimensions);
        gameObjects.addGameObject(sky, Layer.BACKGROUND);
    }
    /**
     * creates the avatar GameObject to be displayed as background.
     * @param inputListener the user input listener.
     * @param imageReader the image reader.
     * */
    private void createAvatar(UserInputListener inputListener, ImageReader imageReader) {
        float avatarStartX = windowDimensions.x() / CENTER_VAL;
        float groundHeight = terrain.groundHeightAt(avatarStartX);
        float avatarHeight = Block.SIZE * INITIAL_AVATAR_HEIGHT;
        float avatarY = groundHeight - avatarHeight;
        Vector2 avatarTopLeftCorner = new Vector2(avatarStartX, avatarY);
        // Create player avatar
        avatar = new Avatar(avatarTopLeftCorner, inputListener, imageReader);
        gameObjects.addGameObject(avatar, Layer.DEFAULT);
        avatar.addComponent(deltaTime -> energyDisplay.update());
        initializeInitialWorld(avatarStartX);
        avatar.addComponent(deltaTime -> updateInfiniteWorld());
        lastWorldUpdateX = avatarStartX;
    }
    /**
     * creates the energy display GameObject to be displayed as background.
     * */
    private void createEnergyDisplay() {
        energyDisplay = new EnergyDisplay(
                ENERGY_DISPLAY_WIDTH,
                avatar.energySupplier()
        );
        gameObjects.addGameObject(energyDisplay.getGameObject(), Layer.UI);

    }
    /**
     * creates the sun GameObject to be displayed as background.
     * */
    private void createSun() {
        // Create sun and add to game
        GameObject sun = Sun.create(windowDimensions, CYCLE_LENGTH);
        gameObjects.addGameObject(sun, SUN_LAYER);
        // Create sun halo and add to game
        GameObject sunHalo = SunHalo.create(sun);
        gameObjects.addGameObject(sunHalo, HALO_LAYER);
    }
    /**
     * creates the cloud GameObject to be displayed as background.
     * */
    private void cloudCreator() {
        Cloud.create(windowDimensions,
                CYCLE_LENGTH,
                gameObjects()::addGameObject,
                gameObjects()::removeGameObject,
                () -> avatar.isRain() && avatar.isOnGround());
    }
    /**
     * create the terrain.
     * */
    private void createTerrain() {
        // Create terrain
        terrain = new Terrain(windowDimensions, SEED);
    }
    /**
     * creates the night GameObject to be displayed as background.
     * */
    private void createNight() {
        GameObject night = Night.create(windowDimensions, CYCLE_LENGTH);
        gameObjects.addGameObject(night, Layer.FOREGROUND);
    }
    /**
     * Sets up the camera to follow the avatar.
     */
    private void setupCamera(WindowController windowController) {
        setCamera(new Camera(avatar,
                Vector2.ZERO,
                windowController.getWindowDimensions(),
                windowController.getWindowDimensions()));
    }

    /**
     * Launches the game application.
     */
    public static void main(String[] args) {

        new PepseGameManager().run();
    }
}