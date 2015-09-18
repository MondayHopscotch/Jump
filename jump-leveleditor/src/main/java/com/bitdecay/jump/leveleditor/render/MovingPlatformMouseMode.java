package com.bitdecay.jump.leveleditor.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bitdecay.jump.geom.BitPointInt;
import com.bitdecay.jump.geom.GeomUtils;
import com.bitdecay.jump.level.Direction;
import com.bitdecay.jump.level.LevelBuilder;
import com.bitdecay.jump.leveleditor.render.mouse.BaseMouseMode;

public class MovingPlatformMouseMode extends BaseMouseMode {
    private LevelBuilder builder;
    public static int direction = Direction.UP;

    public MovingPlatformMouseMode(LevelBuilder builder) {
        super(builder);
        this.builder = builder;
    }

    @Override
    public void mouseDown(BitPointInt point) {
        startPoint = GeomUtils.snap(point, builder.tileSize);
    }

    @Override
    public void mouseDragged(BitPointInt point) {
        super.mouseDragged(point);
        endPoint = GeomUtils.snap(point, builder.tileSize);
    }

    @Override
    public void mouseUpLogic(BitPointInt point) {
        endPoint = GeomUtils.snap(point, builder.tileSize);
        if (startPoint.x != endPoint.x && startPoint.y != endPoint.y) {
            builder.createKineticObject(startPoint, endPoint, direction, 20);
        }
    }

    @Override
    public void render(ShapeRenderer shaper, SpriteBatch spriteBatch) {
        if (startPoint != null && endPoint != null) {
            shaper.setColor(Color.RED);
            shaper.rect(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
        }
    }

    @Override
    public String getToolTip() {
        return "Create a kinetic object that follows a path";
    }
}