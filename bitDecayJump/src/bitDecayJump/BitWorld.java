package bitDecayJump;

import java.util.*;

import bitDecayJump.geom.*;
import bitDecayJump.level.*;

/**
 * A Pseudo-Physics simulation world. Will step according to all body's
 * properties, but properties are publicly accessible to allow for total
 * control.
 * 
 * @author Monday
 *
 */
public class BitWorld {

	private List<BitBody> bodies;

	private Level level;
	private Map<LevelObject, BitBody> levelBodies;

	private BitPoint gravity = new BitPoint(0, 0);

	private List<BitBody> pendingAdds;
	private List<BitBody> pendingRemoves;

	public BitWorld() {
		bodies = new ArrayList<BitBody>();
		levelBodies = new HashMap<LevelObject, BitBody>();
		pendingAdds = new ArrayList<BitBody>();
		pendingRemoves = new ArrayList<BitBody>();
	}

	public void setGravity(float x, float y) {
		this.gravity.x = x;
		this.gravity.y = y;
	}

	public void addBody(BitBody body) {
		pendingAdds.add(body);
	}

	public void removeBody(BitBody body) {
		pendingRemoves.add(body);
	}

	public void step(float delta) {
		// make sure world contains everything it should
		bodies.addAll(pendingAdds);
		pendingAdds.clear();
		bodies.removeAll(pendingRemoves);
		pendingRemoves.clear();

		// apply gravity to DYNAMIC bodies
		bodies.parallelStream().filter(body -> BodyType.DYNAMIC == body.props.bodyType).forEach(body -> body.velocity.add(gravity));

		// move all of our bodies
		bodies.parallelStream().filter(body -> BodyType.STATIC != body.props.bodyType).forEach(body -> body.aabb.translate(body.velocity.getScaled(delta)));

		// resolve collisions for DYNAMIC bodies against Level bodies
		bodies.stream().filter(body -> BodyType.DYNAMIC == body.props.bodyType).forEach(body -> resolveLevelCollisions(body));
	}

	private void resolveLevelCollisions(BitBody body) {
		boolean collisionsDetected = false;

		// 1. determine tile that x,y lives in
		BitPointInt startCell = body.aabb.xy.divideBy(level.tileSize, level.tileSize).minus(level.gridOffset);

		// 2. determine width/height in tiles
		int endX = startCell.x + (int) Math.ceil(1.0 * body.aabb.width / level.tileSize);
		int endY = startCell.y + (int) Math.ceil(1.0 * body.aabb.height / level.tileSize);

		// 3. loop over those all occupied tiles
		// - find all tiles that the body occupies full width or height
		// - add up resolution via:
		// if (width of interstRect > height) -> resolve up/down else resolve
		// left/right
		// - move body the cumulative resolution amount
		BitPointInt resolution = new BitPointInt(0, 0);
		for (int x = startCell.x; x <= endX; x++) {
			for (int y = startCell.y; y <= endY; y++) {
				if (ArrayUtilities.onGrid(level.objects, x, y) && level.objects[x][y] != null) {
					LevelObject checkObj = level.objects[x][y];
					BitRectangle insec = GeomUtils.intersection(body.aabb, level.objects[x][y].rect);
					if (insec != null) {
						collisionsDetected = true;
						if ((checkObj.nValue & Neighbor.UP) == 0 && insec.xy.y == body.aabb.xy.y && insec.height <= insec.width) {
							// bottom side
							resolution.y = Math.max(resolution.y, insec.height);
						} else if ((checkObj.nValue & Neighbor.DOWN) == 0 && insec.xy.y + insec.height == body.aabb.xy.y + body.aabb.height
								&& insec.height <= insec.width) {
							// top side
							resolution.y = Math.min(resolution.y, -insec.height);
						} else if ((checkObj.nValue & Neighbor.RIGHT) == 0 && insec.xy.x == body.aabb.xy.x && insec.width <= insec.height) {
							// left side
							resolution.x = Math.max(resolution.x, insec.width);
						} else if ((checkObj.nValue & Neighbor.LEFT) == 0 && insec.xy.x + insec.width == body.aabb.xy.x + body.aabb.width
								&& insec.width <= insec.height) {
							// right side
							resolution.x = Math.min(resolution.x, -insec.width);
						}
					}
				}
			}
		}

		if (collisionsDetected) {
			body.aabb.xy.add(resolution.x, resolution.y);
			System.out.println(body.aabb + " resolved with " + resolution);
			// NOTE: need to keep track of grounded objects in some fashion.
		}
	}

	private BitBody createBody(BitRectangle rect, BitBodyProps props) {
		return createBody(rect.xy.x, rect.xy.y, rect.width, rect.height, props);
	}

	public BitBody createBody(int x, int y, int width, int height, BitBodyProps props) {
		BitBody body = new BitBody();
		body.aabb = new BitRectangle(x, y, width, height);
		body.props = new BitBodyProps(props);
		addBody(body);
		return body;
	}

	public List<BitBody> getBodies() {
		return Collections.unmodifiableList(bodies);
	}

	public void setLevel(Level level) {
		this.level = level;
		bodies.removeAll(levelBodies.values());
		levelBodies.clear();

		BitBodyProps props = new BitBodyProps();
		props.bodyType = BodyType.STATIC;
		for (LevelObject object : level.getObjects()) {
			createBody(object.rect, props);
		}
		bodies.addAll(pendingAdds);
		pendingAdds.clear();
	}
}