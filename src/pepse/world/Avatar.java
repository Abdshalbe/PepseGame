package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.util.Vector2;

import javax.crypto.SecretKey;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

/**
 * The Avatar class represents the player character in the game.
 * It supports movement, jumping, animation transitions, and energy mechanics.
 */
public class Avatar extends GameObject {
    // ==== Constants ====
    private static final String TAG = "avatar";
    private static final String TRUNK_TAG = "trunk";
    private static final String GROUND_TAG = "ground";
    private static final float INITIAL_MASS = 1f;
    private static final Vector2 AVATAR_SIZE =
            new Vector2(Block.SIZE * 1.5f, Block.SIZE * 2.5f);
    private static final float AVATAR_VELOCITY = 430;
    private static final float AVATAR_JUMP_VELOCITY = -600;
    private static final float GRAVITY = 600f;
    private static final double FRAME_RATE = 0.2;
    private static final float MAX_ENERGY = 100f;
    private static final float MOVEMENT_ENERGY = 0.5f;
    private static final float JUMP_ENERGY = 10f;
    private static final int MINIMUM_FRAMES_TO_REGENERATE_ENERGY = 50;
    private static final float MINIMAL_HORIZONTAL_MOVEMENT = 0.1f;

    private static final String[] IDLE_FRAMES = {
            "assets/idle_0.png", "assets/idle_1.png",
            "assets/idle_2.png", "assets/idle_3.png"};
    private static final String[] JUMP_FRAMES = {
            "assets/jump_0.png", "assets/jump_1.png",
            "assets/jump_2.png", "assets/jump_3.png"};
    private static final String[] RUN_FRAMES = {
            "assets/run_0.png", "assets/run_1.png", "assets/run_2.png",
            "assets/run_3.png", "assets/run_4.png", "assets/run_5.png"};
    private static final float GROUND_MIN = -0.5f;

    // ==== fields ====
    private final AnimationRenderable idleAnimation;
    private final AnimationRenderable jumpAnimation;
    private final AnimationRenderable runAnimation;


    private AnimationRenderable currentRenderer;
    private final UserInputListener inputListener;

    private float energy = MAX_ENERGY;
    private boolean onGround = false;
    private boolean jumpWasPressedLastFrame = false;
    private boolean wasOnGroundLastFrame = false;
    private boolean jumpKeyHeldWithInsufficientEnergy = false;
    private int framesSinceJump = 100;
    private int framesSinceLastSuccessfulJump = 100;
    private Vector2 lastPosition;

    /**
     * Constructs the Avatar object with animations and input logic.
     */
    public Avatar(Vector2 topLeftCorner, UserInputListener inputListener, ImageReader imageReader) {
        super(topLeftCorner, AVATAR_SIZE, null);
        setTag(TAG);
        this.inputListener = inputListener;

        idleAnimation = new AnimationRenderable(IDLE_FRAMES, imageReader, true, FRAME_RATE);
        jumpAnimation = new AnimationRenderable(JUMP_FRAMES, imageReader, true, FRAME_RATE);
        runAnimation  = new AnimationRenderable(RUN_FRAMES,  imageReader, true, FRAME_RATE);

        currentRenderer = idleAnimation;
        renderer().setRenderable(currentRenderer);

        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(INITIAL_MASS);
        lastPosition = getTopLeftCorner();

        addComponent(dt -> setVelocity(getVelocity().add(Vector2.DOWN.mult(GRAVITY * dt))));
        addComponent(this::updateAvatarLogic);
    }

    /**
     * onCollisionEnter() prevents the avatar from falling through blocks.
     * @param other the other GameObject in the collision.
     * @param collision the Collision object.
     * */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (other.getTag().equals(GROUND_TAG)) {
            this.transform().setVelocityY(0);
        }
    }
    /**
     * onCollisionStay() updates the onGround field.
     * @param other the other GameObject in the collision.
     * @param collision the Collision object.
     * */
    @Override
    public void onCollisionStay(GameObject other, Collision collision) {
        if (GROUND_TAG.equals(other.getTag()) || TRUNK_TAG.equals(other.getTag())) {
            Vector2 normal = collision.getNormal();
            // If the normal points upward (negative Y), the avatar is standing on the object
            if (normal.y() < GROUND_MIN) {
                onGround = true;
            }
        }
    }

    /**
     * addEnergy() adds energy to the avatar.
     * @param amount the amount of energy to add.
     * */
    public void addEnergy(float amount) {
        changeEnergy(amount);
    }

    /**
     * energySupplier() returns a Supplier that returns the current energy of the avatar.
     * */
    public Supplier<Float> energySupplier() {
        return () -> energy;

    }

    /**
     * isOnGround() returns whether the avatar is on the ground.
     * @return true if the avatar is on the ground, false otherwise.
     * */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * is rain method check if the game should rain
     * @return true if the game should rain, false otherwise.
     * */
    public boolean isRain(){
        return inputListener.isKeyPressed(KeyEvent.VK_SPACE) && energy >= JUMP_ENERGY;
    }

    /**
     * updateAnimation() updates the animation of the avatar.
     * */
    private void updateAnimation() {
        float dx = getTopLeftCorner().x() - lastPosition.x();
        boolean movedHorizontally = Math.abs(dx) > MINIMAL_HORIZONTAL_MOVEMENT;
        AnimationRenderable next = idleAnimation;
        if (!isOnGround()) {
            next = movedHorizontally ? runAnimation : jumpAnimation;
        } else if (movedHorizontally) {
            next = runAnimation;
        }
        if (currentRenderer != next) {
            currentRenderer = next;
            renderer().setRenderable(currentRenderer);
        }
    }


    /**
     *update() updates the avatar's position, energy, and animation.
     * @param deltaTime the time since the last frame.`
     * */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        onGround = false; // reset every frame
    }

    /**
     * updateAvatarLogic() updates the avatar's position ,energy and animation based on user input.
     * @param deltaTime the time since the last frame.
     * */
    private void updateAvatarLogic(float deltaTime) {
        Vector2 velocity = getVelocity();
        float horizontalVelocity = 0;
        boolean moved = false;
        // Handle horizontal movement
        boolean right = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
        boolean left = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
        if(!(right&&left)){
            if (right && energy >= MOVEMENT_ENERGY ) {
                horizontalVelocity += AVATAR_VELOCITY;
                changeEnergy(-MOVEMENT_ENERGY);
                moved = true;
            }
            else if (left && energy >= MOVEMENT_ENERGY) {
                horizontalVelocity -= AVATAR_VELOCITY;
                changeEnergy(-MOVEMENT_ENERGY);
                moved = true;
            }
        }

        // Handle jump
        boolean jumpNow = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
        boolean jumpJustPressed = jumpNow && !jumpWasPressedLastFrame;
        boolean justLanded = isOnGround() && !wasOnGroundLastFrame;
        float verticalVelocity = velocity.y();

        // Track if jump key is held with insufficient energy
        if (jumpNow && isOnGround() && energy < JUMP_ENERGY) {
            jumpKeyHeldWithInsufficientEnergy = true;
        }

        // Reset the flag when jump key is released
        if (!jumpNow) {
            jumpKeyHeldWithInsufficientEnergy = false;
        }
        boolean canJumpDueToEnergyRecovery = jumpKeyHeldWithInsufficientEnergy && energy >= JUMP_ENERGY;

        if ((jumpJustPressed || (jumpNow && justLanded) || canJumpDueToEnergyRecovery)
                && isOnGround() && energy >= JUMP_ENERGY) {
            verticalVelocity = AVATAR_JUMP_VELOCITY;
            changeEnergy(-JUMP_ENERGY);
            framesSinceJump = 0;
            framesSinceLastSuccessfulJump = 0;
            jumpKeyHeldWithInsufficientEnergy = false; // Reset after successful jump
        } else {
            framesSinceJump++;
            framesSinceLastSuccessfulJump++;
        }
        jumpWasPressedLastFrame = jumpNow;
        wasOnGroundLastFrame = isOnGround();

        // Regenerate energy if idle (use framesSinceLastSuccessfulJump instead of framesSinceJump)
        if (isOnGround() && !moved && framesSinceLastSuccessfulJump > MINIMUM_FRAMES_TO_REGENERATE_ENERGY) {
            changeEnergy(1f);
        }
        setVelocity(new Vector2(horizontalVelocity, verticalVelocity));
        if (horizontalVelocity > 0) renderer().setIsFlippedHorizontally(false);
        else if (horizontalVelocity < 0) renderer().setIsFlippedHorizontally(true);
        updateAnimation();
        lastPosition = new Vector2(getTopLeftCorner());
    }
    /**
     * changeEnergy() changes the energy of the avatar.
     * @param delta the amount of energy to change by.
     * */
    private void changeEnergy(float delta) {
        energy = Math.max(0, Math.min(MAX_ENERGY, energy + delta));
    }
}