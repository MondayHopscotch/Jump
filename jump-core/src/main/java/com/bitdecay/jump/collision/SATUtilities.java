package com.bitdecay.jump.collision;

import com.bitdecay.jump.BitBody;
import com.bitdecay.jump.geom.BitPoint;
import com.bitdecay.jump.geom.GeomUtils;
import com.bitdecay.jump.geom.Projectable;
import com.bitdecay.jump.level.Direction;
import com.bitdecay.jump.level.TileBody;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Monday on 9/4/2015.
 */
public class SATUtilities {

    /**
     * Builds a resolution to move p1 out of p2 if necessary
     *
     * @param p1 the shape to be resolved
     * @param p2 the shape to resolve against
     * @return the resolution strategy, or null if the shapes do not intersect
     */
    public static ManifoldBundle getCollision(Projectable p1, Projectable p2) {
        BitPoint[] points1 = p1.getProjectionPoints();
        BitPoint[] points2 = p2.getProjectionPoints();

        Set<BitPoint> perpendicularAxes = new HashSet<>();

        perpendicularAxes.addAll(buildAxes(points1));
        perpendicularAxes.addAll(buildAxes(points2));

        return maybeBuildCollision(points1, points2, perpendicularAxes);
    }

    private static ManifoldBundle maybeBuildCollision(BitPoint[] points1, BitPoint[] points2, Set<BitPoint> perpendicularAxes) {
        ManifoldBundle res = null;
        for (BitPoint axis : perpendicularAxes) {
            BitPoint line1 = project(axis, points1);
            BitPoint line2 = project(axis, points2);
            Float overlap = getLinearOverlap(line1, line2);
            if (overlap != null) {
                if (res == null) {
                    // only instantiate the resolution if we need to.
                    res = new ManifoldBundle();
                }
                res.addCandidate(new Manifold(axis, overlap));
            } else {
                // if any axis has no overlap, then the shapes do not intersect
                return null;
            }
        }
        return res;
    }

    public static Manifold getCollisionSolution(BitBody body, BitBody against, BitPoint cumulativeResolution) {
        ManifoldBundle bundle = SATUtilities.getCollision(body.aabb.copyOf().translate(cumulativeResolution), against.aabb);
        if (bundle == null) {
            return GeomUtils.ZERO_MANIFOLD;
        } else {
            return CollisionUtilities.solve(bundle, body, against, cumulativeResolution);
        }
    }


    /**
     * Builds all perpendicular axes. Intentionally creates them all as unit vectors in the first and second cartesian quardrants.
     *
     * @param points The points to build perpendiculars for
     */
    private static Set<BitPoint> buildAxes(BitPoint[] points) {
        Set<BitPoint> perpendicularAxes = new HashSet<>();
        BitPoint firstPoint;
        BitPoint secondPoint;
        for (int i = 0; i < points.length; i++) {
            firstPoint = points[i];
            secondPoint = points[(i + 1) % points.length];

            float run = secondPoint.x - firstPoint.x;
            float rise = secondPoint.y - firstPoint.y;
            if (run == 0) {
                // vertical line
                perpendicularAxes.add(new BitPoint(0, 1));
            } else if (rise == 0) {
                perpendicularAxes.add(new BitPoint(1, 0));
            } else {
                float perpSlope = -run / rise;
                BitPoint perpAxis = new BitPoint(1, perpSlope).normalize();
                perpendicularAxes.add(perpAxis);
            }
        }
        return perpendicularAxes;
    }

    public static BitPoint project(BitPoint slope, BitPoint... points) {
        BitPoint axis = new BitPoint(slope.x, slope.y).normalize();

        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        float value;
        for (BitPoint point : points) {
            value = axis.dot(point.x, point.y);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        return new BitPoint(min, max);
    }

    public static Float getLinearOverlap(BitPoint l1, BitPoint l2) {
        // knowing which ends are closer will tell us which way the intersection came from.
        float minEnd = Math.min(l1.y, l2.y);
        float maxStart = Math.max(l1.x, l2.x);
        float overlap = minEnd - maxStart;

        if (overlap > 0) {
            float diff1 = Math.abs(l1.x - l2.y);
            float diff2 = Math.abs(l2.x - l1.y);

            if (diff2 < diff1) {
                // resolve left
                overlap *= -1;
            }
            return overlap;
        } else {
            return null;
        }
    }

    public static boolean axisValidForNValue(Manifold axisOver, TileBody body) {
        if (axisOver.axis.equals(GeomUtils.X_AXIS) && (body.nValue & Direction.RIGHT) == 0) {
            return true;
        } else if (axisOver.axis.equals(GeomUtils.NEG_X_AXIS) && (body.nValue & Direction.LEFT) == 0) {
            return true;
        } else if (axisOver.axis.equals(GeomUtils.Y_AXIS) && (body.nValue & Direction.UP) == 0) {
            return true;
        } else if (axisOver.axis.equals(GeomUtils.NEG_Y_AXIS) && (body.nValue & Direction.DOWN) == 0) {
            return true;
        } else {
            return false;
        }
    }

}