package com.bitdecay.jump.leveleditor.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.bitdecay.jump.collision.BitWorld;
import com.bitdecay.jump.gdx.level.RenderableLevelObject;
import com.bitdecay.jump.geom.BitPoint;
import com.bitdecay.jump.geom.BitPointInt;
import com.bitdecay.jump.geom.GeomUtils;
import com.bitdecay.jump.level.*;
import com.bitdecay.jump.level.builder.LevelLayersBuilder;
import com.bitdecay.jump.leveleditor.EditorHook;
import com.bitdecay.jump.leveleditor.render.mouse.*;
import com.bitdecay.jump.leveleditor.tools.BitColors;
import com.bitdecay.jump.leveleditor.ui.OptionsMode;
import com.bitdecay.jump.leveleditor.ui.OptionsUICallback;
import com.bitdecay.jump.leveleditor.ui.menus.EditorMenus;
import com.bitdecay.jump.leveleditor.utils.EditorKeys;
import com.bitdecay.jump.leveleditor.utils.LevelUtilities;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LevelEditor extends InputAdapter implements Screen, OptionsUICallback {
    public static String ASSETS_FOLDER = "assets/";
    public static String EDITOR_ASSETS_FOLDER = "editorAssets";

    /** This should be called with the assets folder for the editor from any game
     *  to prevent errors finding things like fonts.
     * @param  assetsPath the path to the editor assets
     */
    public static void setAssetsFolder(String assetsPath) {
        ASSETS_FOLDER = assetsPath.replaceAll("[/\\\\]$", "") + "/";
        EDITOR_ASSETS_FOLDER = ASSETS_FOLDER + "editorAssets";
    }

    public static int TILE_SIZE = 16;

    private EditorMenus menus;

    private static final int CAM_MOVE_SPEED = 5;
    private static final float CAM_MAX_ZOOM = 20;
    private static final float CAM_MIN_ZOOM= .2f;
    private static final float CAM_ZOOM_SPEED_SLOW = .05f;
    private static final float CAM_ZOOM_SPEED_FAST = .2f;
    private static final int ZOOM_THRESHOLD = 5;

    public BitmapFont font = new BitmapFont(Gdx.files.internal(ASSETS_FOLDER + "fonts/test2.fnt"), Gdx.files.internal(ASSETS_FOLDER + "fonts/test2.png"), false);

    public Texture playIcon = new Texture(ASSETS_FOLDER + "icons/play.png");
    public Texture pauseIcon = new Texture(ASSETS_FOLDER + "icons/pause.png");

    private String jumpVersion = "Jump v" + BitWorld.VERSION;
    private GlyphLayout jumpVersionGlyphLayout = new GlyphLayout(font, jumpVersion);
    private BitPoint jumpSize = new BitPoint(jumpVersionGlyphLayout.width, jumpVersionGlyphLayout.height);

    private String renderVersion = "Render v" + BitWorld.VERSION;
    private GlyphLayout renderVersionGlyphLayout = new GlyphLayout(font, renderVersion);
    private BitPoint renderSize = new BitPoint(renderVersionGlyphLayout.width, renderVersionGlyphLayout.height);

    private String unsavedChangeWarning = "* There are unsaved changes *";
    private GlyphLayout unsavedChangeWarningGlyphLayout = new GlyphLayout(font, unsavedChangeWarning);
    private BitPoint unsavedWarningSize = new BitPoint(unsavedChangeWarningGlyphLayout.width, unsavedChangeWarningGlyphLayout.height);

    private static Map<RenderLayer, Map<BitPoint, String>> extraStrings = new HashMap<>();

    public SpriteBatch spriteBatch;
    public SpriteBatch uiBatch;

    public LevelLayersBuilder curLevelBuilder;
    public String currentFile = "";

    private boolean renderAsGame = false;
    private OrthographicCamera camera;
    private LibGDXWorldRenderer worldRenderer;
    private LibGDXLevelRenderer levelRenderer;
    private ShapeRenderer shaper;

    private Map<OptionsMode, MouseMode> mouseModes;
    private MouseMode mouseMode;
    private final NoOpMouseMode noOpMouseMode = new NoOpMouseMode();

    // a flags to control whether we are moving the world forward or not
    private boolean stepWorld = true;
    private boolean singleStep = false;
    private long editorUpdates = 0;

    private EditorHook hooker;

    /**
     * Since some calls to refresh can come from Swing threads, we need to fire listeners from
     * the GDX thread and this is the cheapest way I can think of to achieve that.
     */
    private AtomicBoolean refresh = new AtomicBoolean(false);

    public LevelEditor(EditorHook hooker) {
        this.hooker = hooker;
        setUpMenus();
        spriteBatch = new SpriteBatch();
        uiBatch = new SpriteBatch();
        shaper = new ShapeRenderer();

        font.setColor(BitColors.UI_TEXT);

        curLevelBuilder = new LevelLayersBuilder(TILE_SIZE);
        curLevelBuilder.addListener(hooker);

        camera = new OrthographicCamera(1600, 900);
        setCamToOrigin();

        worldRenderer = new LibGDXWorldRenderer();
        levelRenderer = new LibGDXLevelRenderer();

        mouseModes = new HashMap<>();
        mouseModes.put(OptionsMode.SELECT, new SelectMouseMode(curLevelBuilder));
        mouseModes.put(OptionsMode.CREATE, new CreateMouseMode(curLevelBuilder));
        mouseModes.put(OptionsMode.ONEWAY, new CreateOneWayMouseMode(curLevelBuilder));
        mouseModes.put(OptionsMode.MOVING_PLATFORM, new MovingPlatformMouseMode(curLevelBuilder));
        mouseModes.put(OptionsMode.DELETE, new DeleteMouseMode(curLevelBuilder));
        mouseModes.put(OptionsMode.TRIGGERS, new TriggerMouseMode(curLevelBuilder));
        mouseModes.put(OptionsMode.SET_SPAWN, new SpawnMouseMode(curLevelBuilder));
        mouseModes.put(OptionsMode.DROP_OBJECT, new DropObjectMode(curLevelBuilder, this));
        mouseModes.put(OptionsMode.DROP_SIZABLE_OBJECT, new DropSizedObjectMode(curLevelBuilder));
        mouseModes.put(OptionsMode.PROPERTY_INSPECT, new PropertyInspectMode(curLevelBuilder, this));

        mouseMode = mouseModes.get(OptionsMode.SELECT);
    }

    private void setUpMenus() {
        menus = new EditorMenus(this, hooker);
        InputMultiplexer inputMux = new InputMultiplexer();
        inputMux.addProcessor(menus.getStage());
        inputMux.addProcessor(this);
        Gdx.input.setInputProcessor(inputMux);
    }

    private void setCamToOrigin() {
        int tileSize = curLevelBuilder.getCellSize();
        float width = curLevelBuilder.getLevel().layers.getLayer(0).grid.length * tileSize;
        float height = curLevelBuilder.getLevel().layers.getLayer(0).grid[0].length * tileSize;

        BitPoint center = new BitPoint(0, 0).plus(curLevelBuilder.getLevel().layers.gridOffset.x * tileSize, curLevelBuilder.getLevel().layers.gridOffset.y * tileSize);
        center.add(width / 2, height / 2);
        camera.position.set(center.x, center.y, 0);

        float widthZoom = width / Gdx.graphics.getWidth();
        float heightZoom = height / Gdx.graphics.getHeight();
        camera.zoom = Math.max(widthZoom, heightZoom) + .25f;
        camera.update();
    }

    @Override
    public void render(float delta) {
        editorUpdates++;
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (refresh.get()) {
            refresh.set(false);
            curLevelBuilder.fireToListeners();
        }

        handleInput();
        camera.update();

        OrthographicCamera renderingCamera;
        if (renderAsGame) {
            renderingCamera = hooker.getCamera();
        } else {
            renderingCamera = camera;
        }

        spriteBatch.setProjectionMatrix(renderingCamera.combined);
        shaper.setProjectionMatrix(renderingCamera.combined);

        if (singleStep) {
            singleStep = false;
            hooker.update(BitWorld.STEP_SIZE);
        } else if (stepWorld) {
            hooker.update(delta);
        } else {
            hooker.update(0);
        }
        if (RenderLayer.GAME.enabled) {
            hooker.render(renderingCamera);
        }
        if (RenderLayer.GRID.enabled) {
            drawGrid(renderingCamera);
        }
        if (RenderLayer.LEVEL_OBJECTS.enabled) {
            levelRenderer.render(curLevelBuilder, renderingCamera);
        }
        worldRenderer.render(hooker.getWorld(), renderingCamera);
        debugRender();
        drawOrigin(renderingCamera);

        spriteBatch.begin();
        shaper.begin(ShapeType.Line);
        mouseMode.render(shaper, spriteBatch);
        spriteBatch.end();
        shaper.end();

        uiBatch.begin();
        renderExtraUIHints();
        renderStrings(renderingCamera);
        renderVersion();
        renderSpecial();
        uiBatch.end();


        menus.updateAndDraw();
    }

    private void renderSpecial() {
        if (stepWorld) {
            uiBatch.draw(playIcon, 20, 20);
        } else {
            uiBatch.draw(pauseIcon, 20, 20);
        }

        if (currentFile != null && !currentFile.isEmpty()) {
            font.draw(uiBatch, currentFile, Gdx.graphics.getWidth() - unsavedWarningSize.x - 10 , Gdx.graphics.getHeight() - 10);
        }
        if (curLevelBuilder.hasChanges()) {
            font.draw(uiBatch, unsavedChangeWarning, Gdx.graphics.getWidth() - unsavedWarningSize.x - 10 , Gdx.graphics.getHeight() - 20);
        }
    }

    private void debugRender() {
        shaper.begin(ShapeType.Line);
        shaper.setColor(BitColors.SELECTION);
        for (LevelObject obj : curLevelBuilder.selection) {
            if (obj instanceof TriggerObject) {
                shaper.circle(obj.rect.center().x, obj.rect.center().y, 10);
            } else if (obj instanceof DebugSpawnObject) {
                shaper.circle(obj.rect.xy.x, obj.rect.xy.y, DebugSpawnObject.OUTER_DIAMETER + DebugSpawnObject.INNER_DIAMETER);
            } else {
                shaper.rect(obj.rect.xy.x, obj.rect.xy.y, obj.rect.width, obj.rect.height);
            }
        }
        if (curLevelBuilder.getLevel().debugSpawn != null) {
            shaper.setColor(BitColors.SPAWN_OUTER);
            shaper.circle(curLevelBuilder.getLevel().debugSpawn.rect.xy.x, curLevelBuilder.getLevel().debugSpawn.rect.xy.y, DebugSpawnObject.OUTER_DIAMETER);
            shaper.setColor(BitColors.SPAWN);
            shaper.circle(curLevelBuilder.getLevel().debugSpawn.rect.xy.x, curLevelBuilder.getLevel().debugSpawn.rect.xy.y, DebugSpawnObject.INNER_DIAMETER);
        }
        shaper.setColor(BitColors.GRID_SIZE);

        TileObject[][] grid = curLevelBuilder.getLevel().layers.getLayer(0).grid;
        shaper.rect(curLevelBuilder.getLevel().layers.gridOffset.x * curLevelBuilder.getCellSize(), curLevelBuilder.getLevel().layers.gridOffset.y * curLevelBuilder.getCellSize(),
                grid.length * curLevelBuilder.getCellSize(), grid[0].length * curLevelBuilder.getCellSize());
        shaper.end();


    }

    private void renderStrings(OrthographicCamera camera) {
        for (RenderLayer layer : extraStrings.keySet()) {
            if (layer.enabled) {
                for (Map.Entry<BitPoint, String> entry : extraStrings.get(layer).entrySet()) {
                    Vector3 screenCoords = camera.project(new Vector3(entry.getKey().x, entry.getKey().y, 0));
                    font.draw(uiBatch, entry.getValue(), screenCoords.x, screenCoords.y);
                }
            }
            extraStrings.get(layer).clear();
        }
    }

    private void renderExtraUIHints() {
        if (RenderLayer.UI_STRINGS.enabled) {
            font.draw(uiBatch, String.format("World time: %.2f", hooker.getWorld().getTimePassed()), 20, 20);
            font.draw(uiBatch, "Mouse Coordinates: " + getMouseCoords().toString(), 200, 20);
            font.draw(uiBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 500, 20);
            font.draw(uiBatch, EditorKeys.HELP.getHelp(), 10, Gdx.graphics.getHeight() - 100);
        }

        if (EditorKeys.HELP.isPressed()) {
            int curY = Gdx.graphics.getHeight() - 200;
            for (EditorKeys editorKey : EditorKeys.values()) {
                font.draw(uiBatch, editorKey.getHelp(), 10, curY);
                curY -= 20;
            }

        }
    }

    private void renderVersion() {
        if (RenderLayer.UI_STRINGS.enabled) {
            font.draw(uiBatch, jumpVersion, Gdx.graphics.getWidth() - jumpSize.x, jumpSize.y);
            font.draw(uiBatch, renderVersion, Gdx.graphics.getWidth() - renderSize.x, jumpSize.y + renderSize.y);
        }
    }

    /**
     * Queues a string to be rendered next frame
     * @param text
     * @param location
     * @param type
     */
    public static void addStringForRender(String text, BitPoint location, RenderLayer type) {
        if (!extraStrings.containsKey(type)) {
            extraStrings.put(type, new HashMap<>());
        }
        extraStrings.get(type).put(location, text);
    }

    private void drawGrid(OrthographicCamera camera) {
        if (camera.zoom >= ZOOM_THRESHOLD) {
            return;
        }
        shaper.begin(ShapeType.Line);
        shaper.setColor(BitColors.GRID_LINES);
        Vector3 topLeft = camera.unproject(new Vector3(-curLevelBuilder.getCellSize(), -curLevelBuilder.getCellSize(), 0));
        BitPointInt snapTopLeft = GeomUtils.snap(new BitPointInt((int) topLeft.x, (int) topLeft.y), curLevelBuilder.getCellSize());
        Vector3 bottomRight = camera.unproject(new Vector3(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() + 2 * curLevelBuilder.getCellSize(), 0));
        BitPointInt snapBottomRight = GeomUtils.snap(new BitPointInt((int) bottomRight.x, (int) bottomRight.y), curLevelBuilder.getCellSize());

        for (float x = snapTopLeft.x; x <= snapBottomRight.x; x += curLevelBuilder.getCellSize()) {
            shaper.line(x, snapTopLeft.y, x, snapBottomRight.y);
        }
        for (float y = snapBottomRight.y; y <= snapTopLeft.y; y += curLevelBuilder.getCellSize()) {
            shaper.line(snapTopLeft.x, y, snapBottomRight.x, y);
        }
        shaper.end();
    }

    private void drawOrigin(OrthographicCamera camera) {
        shaper.setColor(BitColors.ORIGIN);
        shaper.begin(ShapeType.Filled);
        shaper.circle(0, 0, camera.zoom * (curLevelBuilder.getCellSize() / 3));
        shaper.end();
    }

    public BitPointInt unproject(int x, int y) {
        Vector3 unproj = camera.unproject(new Vector3(x, y, 0));
        return new BitPointInt((int) unproj.x, (int) unproj.y);
    }

    private void handleInput() {
        if (EditorKeys.TOGGLE_GAME.isJustPressed()) {
            renderAsGame = !renderAsGame;
        }

        if (EditorKeys.PAUSE.isJustPressed()) {
            stepWorld = !stepWorld;
        }
        if (!stepWorld && EditorKeys.STEP_WORLD.isJustPressed()) {
            singleStep = true;
        }

        if (EditorKeys.ROLL_WORLD.isPressed()) {
            stepWorld = false;
            if (editorUpdates % 4 == 0) {
                singleStep = true;
            }
        }

        if (renderAsGame) {
            // don't handle the rest of the input
            return;
        }

        if (EditorKeys.PAN_LEFT.isPressed()) {
            camera.translate(-CAM_MOVE_SPEED * camera.zoom, 0);
        } else if (EditorKeys.PAN_RIGHT.isPressed()) {
            camera.translate(CAM_MOVE_SPEED * camera.zoom, 0);
        }
        if (EditorKeys.PAN_UP.isPressed()) {
            camera.translate(0, CAM_MOVE_SPEED * camera.zoom);
        } else if (EditorKeys.PAN_DOWN.isPressed()) {
            camera.translate(0, -CAM_MOVE_SPEED * camera.zoom);
        }

        if (EditorKeys.ZOOM_IN.isPressed()) {
            if (camera.zoom >= ZOOM_THRESHOLD) {
                adjustCamZoom(-CAM_ZOOM_SPEED_FAST);
            } else if (camera.zoom > CAM_MIN_ZOOM) {
                adjustCamZoom(-CAM_ZOOM_SPEED_SLOW);
            }
        } else if (EditorKeys.ZOOM_OUT.isPressed()) {
            if (camera.zoom < ZOOM_THRESHOLD) {
                adjustCamZoom(CAM_ZOOM_SPEED_SLOW);
            } else if (camera.zoom < CAM_MAX_ZOOM) {
                adjustCamZoom(CAM_ZOOM_SPEED_FAST);
            }
        }

        if (EditorKeys.DELETE_SELECTED.isJustPressed()) {
            curLevelBuilder.deleteSelected();
        }

        // check mode hotkeys
        for (OptionsMode mode : OptionsMode.values()) {
            if (mode.hotkey != null && mode.hotkey.isJustPressed()) {
                setMode(mode);
            }
        }
    }

    private void adjustCamZoom(float change) {
        BitPointInt mouseCoordinates = getMouseCoords();
        camera.zoom += change;
        camera.update();
        mouseCoordinates = mouseCoordinates.minus(getMouseCoords());
        camera.translate(mouseCoordinates.x, mouseCoordinates.y);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (renderAsGame) {
            return false;
        }
        mouseMode.mouseDown(getMouseCoords(), MouseButton.getButton(button));
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (renderAsGame) {
            return false;
        }
        mouseMode.mouseUp(getMouseCoords(), MouseButton.getButton(button));
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (renderAsGame) {
            return false;
        }
        mouseMode.mouseDragged(getMouseCoords());
        return super.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (renderAsGame) {
            return false;
        }
        mouseMode.mouseMoved(getMouseCoords());
        return super.mouseMoved(screenX, screenY);
    }

    private BitPointInt getMouseCoords() {
        return unproject(Gdx.input.getX(), Gdx.input.getY());
    }

    @Override
    public void dispose() {
        saveIfChanges();
    }

    @Override
    public void hide() {
        saveIfChanges();
    }

    private void saveIfChanges() {
        if (curLevelBuilder.hasChanges()) {
            JOptionPane.showMessageDialog(null, "Some changes have not been saved");
            LevelUtilities.saveLevel(curLevelBuilder, false);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resize(int arg0, int arg1) {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void setMode(OptionsMode mode) {
        if (mouseModes.containsKey(mode)) {
            mouseMode = mouseModes.get(mode);
        } else if (OptionsMode.UNDO.equals(mode)) {
            curLevelBuilder.undo();
        } else if (OptionsMode.REDO.equals(mode)) {
            curLevelBuilder.redo();
        } else if (OptionsMode.SAVE_LEVEL.equals(mode)) {
            Level savedLevel = LevelUtilities.saveLevel(curLevelBuilder, true);
            if (savedLevel != null) {
                currentFile = FileUtils.lastTouchedFileName;
                setLevelBuilder(savedLevel);
            }
        } else if (OptionsMode.LOAD_LEVEL.equals(mode)) {
            Level loadLevel = LevelUtilities.loadLevel();
            if (loadLevel != null) {
                currentFile = FileUtils.lastTouchedFileName;
                setLevelBuilder(loadLevel);
                setCamToOrigin();
            }
        } else if (OptionsMode.REFRESH.equals(mode)) {
            curLevelBuilder.fireToListeners();
        } else {
            mouseMode = noOpMouseMode;
        }
    }

    private void setLevelBuilder(Level level) {
        if (level == null) {
            curLevelBuilder.newLevel(16);
        } else {
            curLevelBuilder.setLevel(level);
        }
    }

    public void dropObject(RenderableLevelObject modelInstance) {
        if (UserSizedLevelObject.class.isAssignableFrom(modelInstance.getClass())) {
            ((DropSizedObjectMode)mouseModes.get(OptionsMode.DROP_SIZABLE_OBJECT)).setObject(modelInstance);
            setMode(OptionsMode.DROP_SIZABLE_OBJECT);
        } else {
            ((DropObjectMode) mouseModes.get(OptionsMode.DROP_OBJECT)).setObject(modelInstance);
            setMode(OptionsMode.DROP_OBJECT);
        }
    }

    public void setMaterial(int tileset) {
        ((CreateMouseMode)mouseModes.get(OptionsMode.CREATE)).setMaterial(tileset);
        ((CreateOneWayMouseMode)mouseModes.get(OptionsMode.ONEWAY)).setMaterial(tileset);
    }

    public void queueReload() {
        refresh.set(true);
    }

    public void setActiveLayer(int layerNumber) {
        curLevelBuilder.setActiveLayer(layerNumber);
    }
}
