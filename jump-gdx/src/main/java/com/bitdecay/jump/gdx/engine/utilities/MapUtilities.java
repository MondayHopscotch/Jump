package com.bitdecay.jump.gdx.engine.utilities;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.math.*;

/**
 * A utility class that makes various actions dealing with maps and MapObjects
 *
 * @author Monday
 * @version 0.1 (March 2014)
 */
public class MapUtilities {

    /**
     * A helper method to convert a pixel location into a map location based on
     * the game tile size
     *
     * @param obj the map object to pull a location from
     * @return a BitPoint corresponding to what tile position the object is in
     */
    public static Vector2 getObjectsLocation(MapObject obj) {
        Vector2 location;
        if (obj instanceof EllipseMapObject) {
            Ellipse ellipse = ((EllipseMapObject) obj).getEllipse();
            location = new Vector2((int) (ellipse.x + ellipse.width / 2), (int) (ellipse.y + ellipse.width / 2));
        } else if (obj instanceof CircleMapObject) {
            Circle circle = ((CircleMapObject) obj).getCircle();
            location = new Vector2((int) (circle.x + circle.radius / 2), (int) (circle.y + circle.radius / 2));
        } else if (obj instanceof PolygonMapObject) {
            Polygon polygon = ((PolygonMapObject) obj).getPolygon();
            location = new Vector2((int) polygon.getX(), (int) polygon.getY());
        } else if (obj instanceof PolylineMapObject) {
            Polyline polyline = ((PolylineMapObject) obj).getPolyline();
            location = new Vector2((int) polyline.getX(), (int) polyline.getY());
        } else if (obj instanceof RectangleMapObject) {
            com.badlogic.gdx.math.Rectangle rectangle = ((RectangleMapObject) obj).getRectangle();
            location = new Vector2((int) (rectangle.x + rectangle.getWidth() / 2), (int) (rectangle.y + rectangle.getWidth() / 2));
        } else if (obj instanceof TextureMapObject) {
            TextureMapObject textureMapObject = (TextureMapObject) obj;
            location = new Vector2((int) textureMapObject.getX(), (int) textureMapObject.getY());
        } else {
            throw new IllegalStateException("Unexpected subclass '" + obj.getClass().getName() + "' of MapObject");
        }
        location.x /= 32;
        location.y /= 32;
        return location;
    }
}
