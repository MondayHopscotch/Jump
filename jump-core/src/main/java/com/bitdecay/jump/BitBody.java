package com.bitdecay.jump;

import com.bitdecay.jump.collision.ContactListener;
import com.bitdecay.jump.control.BitBodyController;
import com.bitdecay.jump.exception.BitBodySerializeException;
import com.bitdecay.jump.geom.BitPoint;
import com.bitdecay.jump.geom.BitRectangle;
import com.bitdecay.jump.properties.BitBodyProperties;
import com.bitdecay.jump.render.BitBodyStateWatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A basic body in the world. Holds all stateful information as well as a properties
 * object defining the limits of the state.
 */
public class BitBody {

    /**
     * A generic object we can attach to a body. Useful for accessing game objects
     * during collisions.
     */
    public Object userObject;

    /**
     * Basic properties defining the configurable behavior of this body
     */
    public BitBodyProperties props = new BitBodyProperties();

    /**
     * Contains the position and size data for this body
     */
	public BitRectangle aabb = new BitRectangle(1, 1, 1, 1);

    /**
     * Reference to the optional parents object
     */
	public Set<BitBody> parents = new HashSet<>();

	/**
	 * Holds children bodies of this body. Allows for 'attaching' bodies to each
	 * other
	 */
	public Set<BitBody> children = new HashSet<>();

	/**
	 * Simple flag to determine if a body is grounded. Specific to platformer
	 * style physics/movement
	 */
	public boolean grounded = false;

	/**
	 * Simple flag to distinguish which direction the body is facing. This does
	 * not necessarily correlate with what direction the body is <i>moving</i>.
	 */
	public Facing facing = Facing.RIGHT;

	/**
	 * Optional controller to control movement/behavior of the body
	 */
	public BitBodyController controller = null;

	/**
	 * Optional render manager to automatically handle what render an object is in
	 * relative to the physics world
	 */
	public BitBodyStateWatcher renderStateWatcher = null;

    /**
     * Tracks the last position the body was. Generally this is the same thing as
     * the position the body was at when the world step began.
     */
    public BitPoint lastPosition = new BitPoint(0, 0);

	/**
	 * Tracks the <b>attempted</b> movement the body <b>this step</b> of the world. This does
	 * not mean the body did move.
	 */
	public BitPoint currentAttempt = new BitPoint(0, 0);

    /**
     * Tracks the <b>previous</b> attempted movement the body <b>tried</b> to move last step.
     * This is not how far it actually moved. To determine that, sum with lastResolution.
     */
    public BitPoint previousAttempt = new BitPoint(0, 0);

	/**
	 * Tracks what the physics world <b>last</b> did to resolve any collisions
	 * that may have happened
	 */
	public BitPoint lastResolution = new BitPoint(0, 0);

    /**
     * Flag that tells the engine not to let this body more any more during the current world step
     */
    public boolean resolutionLocked;

	/**
	 * Only active bodies are updated in the world
	 */
	public boolean active = true;

    /**
     * Determines how the body acts in the physics world
     */
    public BodyType bodyType = BodyType.STATIC;

    /**
     * The velocity of the body
     */
    public BitPoint velocity = new BitPoint(0, 0);

    /**
     * A list of subscribed listeners for contact information as it happens
     */
    private List<ContactListener> contactListeners = new ArrayList<>();

    public BitBody(){}

    public void set(String prop, Object value) throws BitBodySerializeException {
        try {
            this.getClass().getDeclaredField(prop).set(this, value);
        } catch (NoSuchFieldException nsfe){
            throw new BitBodySerializeException("There was no such field with the name: " + prop, nsfe);
        } catch (IllegalAccessException iae){
            throw new BitBodySerializeException("The property: " + prop + " is not marked as public", iae);
        }
    }

    public void addContactListener(ContactListener listener) {
        contactListeners.add(listener);
    }

    public void removeContactListener(ContactListener listener) {
        contactListeners.remove(listener);
    }

    public List<ContactListener> getContactListeners() {
        return contactListeners;
    }
}
