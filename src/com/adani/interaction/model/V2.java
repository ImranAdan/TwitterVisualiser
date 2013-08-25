package com.adani.interaction.model;

/**
 * Class used to represent a vector.
 * @author Imran
 * 
 */
public final class V2 {

	// x and y values of the vector
	public float x;
	public float y;

	/**
	 * Create a new Vector from x, y positions.
	 * @param x X
	 * @param y Y
	 */
	public V2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Subtract this vector form another
	 * @param v The other vector.
	 * @return The difference vector.
	 */
	public V2 subtract(V2 v) {
		return new V2(this.x - v.x, this.y - v.y);
	}

	/**
	 * @return The length of this vector.
	 */
	public float length() {
		return (float) Math.sqrt(this.x * this.x + this.y * this.y);
	}

	/**
	 * Normalise this vector.
	 */
	public void normalise() {
		float l = this.length();
		this.x = this.x / l;
		this.y = this.y / l;
	}
}