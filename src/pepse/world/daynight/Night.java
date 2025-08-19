package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.components.Transition.TransitionType;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import danogl.gui.rendering.Renderable;
import danogl.components.CoordinateSpace;

import java.awt.Color;

/**
 * This class is responsible for simulating the night effect in the game.
 * It creates a semi-transparent black rectangle that overlays the screen
 * and gradually changes its opacity.
 */
public class Night  {
    /** The tag used to identify the night object in the game. */
    public static final String NIGHT_TAG = "night";

    /** The maximum opacity at midnight (final value of the transition). */
    private static final Float MIDNIGHT_OPACITY = 0.5f;
    private static final float RELATIVE_NIGHT_DURATION = 2;
    private static final float INITIAL_OPACITY = 1f;

    /**
     * Creates a translucent black rectangle covering the entire screen,
     * and gradually changes its opacity to simulate the day-night cycle.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength      The duration of a full day-night cycle, in seconds.
     * @return A GameObject representing the night overlay.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        Renderable blackRectangle = new RectangleRenderable(Color.BLACK);
        GameObject night = new GameObject(Vector2.ZERO, windowDimensions, blackRectangle);
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);
        night.renderer().setOpaqueness(INITIAL_OPACITY); // initial value at daytime

        new Transition<>(
                night, // the game object being changed
                night.renderer()::setOpaqueness, // the method to call
                0f, // initial transition value
                MIDNIGHT_OPACITY, // final transition value
                Transition.CUBIC_INTERPOLATOR_FLOAT, // use a cubic interpolator
                cycleLength / RELATIVE_NIGHT_DURATION, // transition fully over half a day
                TransitionType.TRANSITION_BACK_AND_FORTH, // repeat in reverse direction
                null
        );
        return night;
    }
}
