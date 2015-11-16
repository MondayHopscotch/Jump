package com.bitdecay.jump.leveleditor.example.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bitdecay.jump.BitBody;
import com.bitdecay.jump.BodyType;
import com.bitdecay.jump.gdx.level.RenderableLevelObject;
import com.bitdecay.jump.geom.BitRectangle;
import com.bitdecay.jump.leveleditor.render.LevelEditor;

/**
 * Created by Monday on 11/13/2015.
 */
public class ShellLevelObject extends RenderableLevelObject {
    TextureRegion texture;

    public ShellLevelObject() {
        rect = new BitRectangle(0, 0, 16 ,16);
        this.texture = new TextureRegion(new Texture(Gdx.files.internal(LevelEditor.EDITOR_ASSETS_FOLDER + "/question.png")));
        texture.setRegionHeight(16);
        texture.setRegionWidth(16);
    }

    @Override
    public BitBody buildBody() {
        BitBody body = new BitBody();
        body.aabb = new BitRectangle(rect);
        body.bodyType = BodyType.DYNAMIC;
        body.props.gravitational = true;
        body.props.crushable = false;
        return body;
    }

    @Override
    public String name() {
        return "Shell";
    }

    @Override
    public TextureRegion texture() {
        return texture;
    }
}
