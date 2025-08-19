package pepse.util;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Displays the avatar's current energy as a percentage in black text.
 */
public class EnergyDisplay {
    private static final String INITIAL_VALUE = "100%";
    private static final Vector2 TEXT_BOX_SIZE = new Vector2(100, 40);
    private static final float MAX_ENERGY = 100f;
    private static final String PERCENTAGE = "%";
    private final GameObject energyText;
    private final TextRenderable textRenderable;
    private final Supplier<Float> energySupplier;


    /**
     * Constructs a simple numeric energy display.
     *
     * @param position        Where to display the percentage text
     * @param energySupplier  Function that supplies the avatar's energy
     */
    public EnergyDisplay(Vector2 position, Supplier<Float> energySupplier) {
        this.energySupplier = energySupplier;
        this.textRenderable = new TextRenderable(INITIAL_VALUE);
        this.textRenderable.setColor(Color.BLACK);

        this.energyText = new GameObject(position, TEXT_BOX_SIZE, textRenderable);
        energyText.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
    }

    /**
     * Updates the energy percentage text.
     */
    public void update() {
        float energy = energySupplier.get();
        int percent = Math.round((energy / MAX_ENERGY) * MAX_ENERGY);
        textRenderable.setString(percent + PERCENTAGE);
    }

    /**
     * get the energy text game object
     * @return The GameObject displaying the energy text.
     */
    public GameObject getGameObject() {
        return energyText;
    }
}
