package renderer;

import java.util.*;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */

public class EdgeList {
	int beginingY;
	int endfOfY;
	Map<Integer,Float> minX = new HashMap<>();
	Map<Integer,Float> maxX = new HashMap<>();
	Map<Integer,Float> minZ = new HashMap<>();
	Map<Integer,Float> maxZ = new HashMap<>();

	public EdgeList(int startY, int endY) {
		beginingY = startY;
		endfOfY = endY;

	}

	public int getStartY() {
		return beginingY;
	}

	public int getEndY() {
		return endfOfY;
	}

	//Get lowest x value for y
	public float getLeftX(int y) {
		 return minX.get(y);
	}

	//Get highest z value for y
	public float getRightX(int y) {
		return  maxX.get(y);
	}

	//Get lowest z value for y
	public float getLeftZ(int y) {
		return minZ.get(y);
	}

	//Get highest z value for y
	public float getRightZ(int y) {
		return maxZ.get(y);
	}

	//Add values to the left or right edge for y.

	public void setMinX (int key , float minValue){
		minX.put(key, minValue);
	}

	public void setMinZ (int key , float minValue){
		minZ.put(key, minValue);
	}

	public void setMaxX (int key , float minValue){
		maxX.put(key, minValue);
	}

	public void setMaxZ (int key , float minValue){
		maxZ.put(key, minValue);
	}

}

// code for comp261 assignments
