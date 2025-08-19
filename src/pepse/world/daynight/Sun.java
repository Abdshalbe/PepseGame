package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import pepse.world.Terrain;

import java.awt.Color;

/**
 * Responsible for creating and animating a sun object that moves in a circular path
 * across the sky to simulate a day-night cycle.
 */
public class Sun {
    private static final float SUN_RADIUS = 100f;
    private static final Vector2 SUN_DIMENSIONS = new Vector2(SUN_RADIUS, SUN_RADIUS);
    private static final Color SUN_COLOR = Color.YELLOW;
    private static final float HALF_SIZE = 2f;
    private static final Float CYCLE_FINISH = 360f;
    private static final String TAG = "sun";
    private static final float Y_CENTER = 300f;

    /**
     * Constructs a sun GameObject that moves in a circular motion.
     * @param windowDimensions The dimensions of the game window (used to calculate path).
     * @param cycleLength      The number of seconds it takes the sun to complete one full orbit.
     * @return The created sun GameObject.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        Renderable sunRenderable = new OvalRenderable(SUN_COLOR);

        GameObject sun = new GameObject(Vector2.ZERO, SUN_DIMENSIONS, sunRenderable);
        float groundHeight = windowDimensions.y() * Terrain.GROUND_LENGTH;
        Vector2 initialSunCenter = new Vector2(windowDimensions.x() / HALF_SIZE, groundHeight - Y_CENTER);
        Vector2 cycleCenter = new Vector2(windowDimensions.x() / HALF_SIZE, groundHeight);

        sun.setCenter(initialSunCenter);
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(TAG);

        new Transition<>(
                sun,
                angle -> sun.setCenter(initialSunCenter.subtract(cycleCenter).rotated(angle)
                        .add(cycleCenter)),
                0f,
                CYCLE_FINISH,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );

        return sun;
    }

}
