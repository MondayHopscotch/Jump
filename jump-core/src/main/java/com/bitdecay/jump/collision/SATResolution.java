package com.bitdecay.jump.collision;

import com.bitdecay.jump.BitBody;
import com.bitdecay.jump.geom.BitPoint;
import com.bitdecay.jump.geom.BitRectangle;
import com.bitdecay.jump.geom.GeomUtils;
import com.bitdecay.jump.geom.MathUtils;
import com.bitdecay.jump.level.Direction;
import com.bitdecay.jump.level.TileBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monday on 9/6/2015.
 */
public class SATResolution {
    public BitPoint axis;
    public float distance;
    public BitPoint result;

    private List<AxisOverlap> axes = new ArrayList<>();

    @Override
    public String toString() {
        return axis.toString() + " * " + distance;
    }

    public void addAxis(BitPoint axis, float overlap) {
        if (distance < 0) {
            distance *= -1;
            axis.x *= -1;
            axis.y *= -1;
        }
        axes.add(new AxisOverlap(axis, overlap));
    }

    /**
     * This method finds what axis should be used for resolution. This method takes into account the relative
     * speeds of the objects being resolved. A resolution will only be applied to an object that is AGAINST
     * the relative movement between it and what it has collided against.
     *
     * This logic is here to ensure corners feel crisp when a body is landing on the edge of a platform.
     *
     * <b>NOTE:</b> Due to logic to make sure the feel is correct, there are situations (such as one-way
     * platforms) that can result in NO axis (aka the Zero-axis) being set on the resolution.
     *
     *  @param body the body being resolved
     * @param otherBody the other body that participated in the collision
     * @param resolvedPosition the current partially built resolution
     */
    public void compute(BitBody body, BitBody otherBody, BitRectangle resolvedPosition) {
        axes.sort((o1, o2) -> Float.compare(Math.abs(o1.distance), Math.abs(o2.distance)));
        // this line is just taking where the body tried to move and the partially resolved position into account to
        // figure out the relative momentum.
        BitPoint bodyAttempt = body.resolutionLocked ? new BitPoint(0, 0) : body.currentAttempt;
        BitPoint otherBodyAttempt = otherBody.resolutionLocked ? new BitPoint(0, 0) : otherBody.currentAttempt;
        BitPoint relativeMovement = bodyAttempt.plus(resolvedPosition.xy.minus(body.aabb.xy)).minus(otherBodyAttempt);
        float dotProd;
        for (AxisOverlap axisOver : axes) {
            dotProd = relativeMovement.dot(axisOver.axis);
            if (dotProd != 0 && !MathUtils.sameSign(dotProd, axisOver.distance)) {
                if (otherBody instanceof TileBody) {
                    if (!axisValidForNValue(axisOver, (TileBody) otherBody)) {
                        continue;
                    }
                    // confirm that body came from past this thing
                    float resolutionPosition = resolvedPosition.xy.plus(axisOver.axis.times(axisOver.distance)).dot(axisOver.axis);
                    float lastPosition = body.lastPosition.dot(axisOver.axis);

                    if (MathUtils.opposing(resolutionPosition, lastPosition)) {
                        // all collisions should push a body backwards according to the
                        // relative movement. If it's not doing so, it's not a valid case.
                        continue;
                    }

                    if (axisOver.distance < 0 && (lastPosition > resolutionPosition)) {
                        continue;
                    }

                    if (axisOver.distance > 0 && lastPosition < resolutionPosition) {
                        continue;
                    }

                    if (((TileBody) otherBody).collisionAxis != null) {
                        if (axisOver.axis.equals(((TileBody) otherBody).collisionAxis)) {
                            if (axisOver.distance < 0) {
                                continue;
                            }
                        } else if (axisOver.axis.equals(((TileBody) otherBody).collisionAxis.times(-1))) {
                            if (axisOver.distance > 0) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                axis = axisOver.axis;
                distance = axisOver.distance;
                result = axis.scale(distance);
                return;
            }
        }
        /*
         * This is a fallback, I can't think of a scenario where we would get here, but better to do
         * something than to leave ourselves open to an NPE
         */
        axis = new BitPoint(0, 0);
        distance = 0;
        result = axis.scale(distance);
    }

    private boolean axisValidForNValue(AxisOverlap axisOver, TileBody body) {
        if (axisOver.axis.equals(GeomUtils.X_AXIS)) {
            if (axisOver.distance > 0 && (body.nValue & Direction.RIGHT) == 0) {
                return true;
            } else if (axisOver.distance < 0 && (body.nValue & Direction.LEFT) == 0) {
                return true;
            }
        } else if (axisOver.axis.equals(GeomUtils.Y_AXIS)) {
            if (axisOver.distance > 0 && (body.nValue & Direction.UP) == 0) {
                return true;
            } else if (axisOver.distance < 0 && (body.nValue & Direction.DOWN) == 0) {
                return true;
            }
        }
        return false;
    }

    private class AxisOverlap {
        public BitPoint axis;
        public float distance;

        public AxisOverlap(BitPoint axis, float distance) {
            this.axis = axis;
            this.distance = distance;
        }
    }
}

