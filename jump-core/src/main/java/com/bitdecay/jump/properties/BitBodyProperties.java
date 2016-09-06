package com.bitdecay.jump.properties;

import com.bitdecay.jump.annotation.ValueRange;

/**
 * Property object defining the configuration of a given body.
 * Created by Monday on 10/25/2015.
 */
public class BitBodyProperties {

    /**
     * Flag to specify whether or not this body participates in collision resolution
     */
    public boolean collides = true;

    /**
     * Flag to specify whether or not this body should fire contact events
     */
    public boolean firesListenerEvents = true;

    /**
     * Flag dictating if conflicting resolutions will deactive the body
     */
    public boolean crushable = true;

    /**
     * The acceleration of the body while on the ground
     */
    @ValueRange(min = 0, max = 5000)
    public int acceleration = 0;

    /**
     * The Deceleration of the body while in the air
     */
    @ValueRange(min = 0, max = 5000)
    public int airAcceleration = 0;

    /**
     * The decelleration of the body on while on the ground
     */
    @ValueRange(min = 0, max = 5000)
    public int deceleration = 0;

    /**
     * The Acceleration of the body while in the air
     */
    @ValueRange(min = 0, max = 5000)
    public int airDeceleration = 0;

    /**
     * The max 'running' speed of the body
     */
    public int maxVoluntarySpeed = 300;

    /**
     * A flag for whether or not gravity should affect this body
     */
    public boolean gravitational = true;

    @ValueRange(min = 0, max = 10)
    public float gravityModifier = 1.0f;
}
