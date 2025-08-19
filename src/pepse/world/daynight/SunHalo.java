package pepse.world.daynight;

import danogl.GameObject;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * A class responsible for creating the sun halo GameObject.
 * The halo is a semi-transparent yellow circle that surrounds the sun.
 */
public class SunHalo {
    /** Tag used to identify the sun halo object. */
    private static final String SUN_HALO_TAG = "sunHalo";
    private static final Color haloColor = new Color(255, 255, 0, 20);
    private static final float SIZE_RELATION = 2f;
    private static final float RELATIVE_POSITION = 0.5f;


    /**
     * Creates a semi-transparent yellow halo around the given sun GameObject.
     *
     * @param sun The sun GameObject to wrap with a halo.
     * @return A GameObject representing the sun halo.
     */
    public static GameObject create(GameObject sun) {
        OvalRenderable haloRenderer = new OvalRenderable(haloColor);
        Vector2 haloDimensions = sun.getDimensions().mult(SIZE_RELATION);

        // Center the halo relative to sun position
        Vector2 haloTopLeft = sun.getCenter().subtract(haloDimensions.mult(RELATIVE_POSITION));
        GameObject halo = new GameObject(haloTopLeft, haloDimensions, haloRenderer);

        // Match coordinate space
        halo.setCoordinateSpace(sun.getCoordinateSpace());
        halo.setTag(SUN_HALO_TAG);
        halo.addComponent(deltaTime -> {
            Vector2 newTopLeft = sun.getCenter().subtract(
                    halo.getDimensions().mult(RELATIVE_POSITION));
            halo.setTopLeftCorner(newTopLeft);
        });
        return halo;
    }
}
