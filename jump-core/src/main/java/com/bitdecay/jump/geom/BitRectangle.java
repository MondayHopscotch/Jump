package com.bitdecay.jump.geom;

public class BitRectangle implements Projectable {
	public BitPoint xy;
	public float width;
	public float height;

	public BitRectangle() {
		// here for JSON
	}

	public BitRectangle(BitRectangle other) {
		this(other.xy.x, other.xy.y, other.width, other.height);
	}

	public BitRectangle(float x, float y, float width, float height) {
		xy = new BitPoint(x, y);
		this.width = width;
		this.height = height;

		if (width < 0) {
			xy.x += width;
			this.width *= -1;
		}

		if (height < 0) {
			xy.y += height;
			this.height *= -1;
		}
	}

	public BitRectangle(BitPointInt startPoint, BitPointInt endPoint) {
		this(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
	}

	public BitRectangle(BitPoint startPoint, BitPoint endPoint) {
		this(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
	}

	public BitRectangle(BitPoint center, float width, float height) {
		this(center.x - width/2, center.y - height/2, width, height);
	}

	public void set(BitRectangle other) {
		this.xy.set(other.xy);
		this.width = other.width;
		this.height = other.height;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	/**
	 * Translates and returns this BitRectangle for chaining
	 * @param point
	 * @return
	 */
	public BitRectangle translate(BitPoint point) {
		translate(point.x, point.y);
		return this;
	}

	/**
	 * Translates and returns this BitRectangle for chaining
	 * @param x
	 * @param y
	 * @return
	 */
	public BitRectangle translate(float x, float y) {
		xy.x += x;
		xy.y += y;
		return this;
	}

	public BitPoint center() {
		return new BitPoint(xy.x + width / 2, xy.y + height / 2);
	}

	public boolean contains(BitPointInt point) {
		return contains(point.x, point.y);
	}

	public boolean contains(BitPoint point) {
		return contains(point.x, point.y);
	}

	public boolean contains(float x, float y) {
		return x >= xy.x && x <= xy.x + width && y >= xy.y && y <= xy.y + height;
	}

	/**
	 * Tests if this rectangle contains another rectangle. A rectangle
	 * is considered 'contained' if all of its edges are on or within
	 * this rectangles edges.
	 * @param other
	 * @return true if the rectangle is contained within this rect, false otherwise
	 */
	public boolean contains(BitRectangle other) {
		return other.xy.x >= this.xy.x && other.xy.y >= this.xy.y && other.xy.x + other.width <= this.xy.x + this.width
				&& other.xy.y + other.height <= this.xy.y + this.height;
	}

	@Override
	public BitPoint[] getProjectionPoints() {
		return new BitPoint[] {xy,  xy.plus(width, 0), xy.plus(width, height), xy.plus(0, height)};
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BitRectangle other = (BitRectangle) obj;
		if (height != other.height) {
			return false;
		}
		if (width != other.width) {
			return false;
		}
		if (xy == null) {
			if (other.xy != null) {
				return false;
			}
		} else if (!xy.looseEquals(other.xy)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "(x: " + xy.x + ", y: " + xy.y + " - w: " + width + ", h: " + height + ")";
	}

	public BitRectangle copyOf() {
		return new BitRectangle(this);
	}
}
