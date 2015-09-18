package com.bitdecay.jump.leveleditor.render.mouse;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bitdecay.jump.BitBody;
import com.bitdecay.jump.BitBodyProps;
import com.bitdecay.jump.BitWorld;
import com.bitdecay.jump.JumperProps;
import com.bitdecay.jump.geom.BitPointInt;
import com.bitdecay.jump.geom.GeomUtils;
import com.bitdecay.jump.level.LevelBuilder;
import com.bitdecay.jump.leveleditor.input.ControlMap;
import com.bitdecay.jump.leveleditor.input.PlayerInputHandler;
import com.bitdecay.jump.state.JumperStateWatcher;

public class SetPlayerMouseMode extends BaseMouseMode {

    private BitWorld world;
    private JumperProps props;

    public BitBody lastPlayer;
    private PlayerInputHandler playerController;
    private ControlMap controls;

    public SetPlayerMouseMode(LevelBuilder builder, BitWorld world, PlayerInputHandler playerController, JumperProps props) {
        super(builder);
        this.world = world;
        this.playerController = playerController;
        this.props = props;
        this.controls = ControlMap.defaultMapping;
    }

    @Override
    protected void mouseUpLogic(BitPointInt point) {
        if (startPoint.x != endPoint.x && startPoint.y != endPoint.y) {
            BitBodyProps props = this.props;
            if (lastPlayer != null) {
                world.removeBody(lastPlayer);
            }
            // TODO: figure out why this isn't bringing over the jumperprops values
            lastPlayer = world.createBody(GeomUtils.makeRect(startPoint, endPoint), props);
            lastPlayer.stateWatcher = new JumperStateWatcher();
            playerController.setBody(lastPlayer, controls);
        }
    }

    @Override
    public void render(ShapeRenderer shaper, SpriteBatch spriteBatch) {
        if (startPoint != null && endPoint != null) {
            shaper.setColor(Color.ORANGE);
            shaper.rect(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
        }
    }

    @Override
    public String getToolTip() {
        return "Drop a player object into the level";
    }
}