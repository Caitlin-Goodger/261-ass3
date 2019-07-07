package renderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.AmbientLight;
import renderer.Scene.Polygon;

/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 * 
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {

	static Color[][] reUsableZbuffer = null;
	static float[][] reUsableZDepth = null;

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		if (poly.getnormal().z <= 0) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 * 
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from
	 *            the file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight) {
		Color shadeColour;
		Color reflect = poly.reflectance;
		Vector3D firstVec;
		Vector3D secondVec;
		Vector3D cross;
		int red;
		int green;
		int blue;
		double cos;

		Vector3D[]  allVec = poly.getVertices();

		//Caluculate the red, green and blue values
		firstVec = allVec[1].minus(allVec[0]);
		secondVec= allVec[2].minus(allVec[1]);
		cross= firstVec.crossProduct(secondVec);
		cos = cross.cosTheta(lightDirection);
		red = (int)((ambientLight.getRed()/255f) * reflect.getRed() + (lightColor.getRed()/255f) * reflect.getRed() * cos);
		green = (int)((ambientLight.getGreen()/255f) * reflect.getGreen() + (lightColor.getGreen()/255f) * reflect.getGreen() * cos);
		blue = (int)((ambientLight.getBlue()/255f) * reflect.getBlue()+ (lightColor.getBlue()/255f) * reflect.getBlue() * cos);

		//If it is too big or too small then set to corrent values
		if(red<0) {
			red=ambientLight.getRed();
		}
		if(green<0) {
			green = ambientLight.getGreen();
		}
		if(blue<0) {
			blue = ambientLight.getBlue();
		}

		if(red>255) {
			red = 255;
		}
		if(green>255) {
			green = 255;
		}
		if(blue>255) {
			blue = 255;
		}
		shadeColour =  new Color( red,  green,  blue);
		return shadeColour;
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 * 
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		//Check that scene is not null
		if(scene == null) {
			return null;
		}

		//Calculate transforms for the new rotations
		Transform tX = Transform.newXRotation(xRot);
		Transform tY = Transform.newYRotation(yRot);

		//Multiply all the points by the transforms
		ArrayList<Polygon> newPoly = new ArrayList<>(scene.getPolygons());
		for(Polygon p : newPoly) {
			for(int i =0; i<p.getVertices().length;i++) {
				if(xRot != 0.0f) {
					p.getVertices()[i] = tX.multiply(p.getVertices()[i]);

				}
				if(yRot != 0.0f) {
					p.getVertices()[i] = tY.multiply(p.getVertices()[i]);

				}
			}
		}

		return new Scene(newPoly,scene.getLight());
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene) {
		//Bounding box is the area that the polygon takes up
		Rectangle boundingBox = boundingBox(scene.getPolygons());
		float xDiff = -boundingBox.x;
		float yDiff = -boundingBox.y;
		//Calculate the transform for the amount it has to be translated then apply it to the points
		Transform t = Transform.newTranslation(new Vector3D(xDiff,yDiff,0));
		for(Polygon p : scene.getPolygons()) {
			for(int i =0;i<p.getVertices().length;i++) {
				p.getVertices()[i] = t.multiply(p.getVertices()[i]);
			}
		}
		return new Scene(scene.getPolygons(),scene.getLight());
	}

	/**
	 * This should scale the scene.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene) {
		//Bounding box is the area that the polygon takes up
		Rectangle boundingBox = boundingBox(scene.getPolygons());
		float width = (float) boundingBox.width;
		float height = (float) boundingBox.height;

		float scale = 1;
		//Find out if the width or the height is further off and work with that one
		boolean widthLong = (width-GUI.CANVAS_WIDTH > height-GUI.CANVAS_HEIGHT);

		if (width > GUI.CANVAS_WIDTH && widthLong) {
			scale = GUI.CANVAS_WIDTH / width;
		}
		if (height > GUI.CANVAS_HEIGHT && !widthLong) {
			scale = GUI.CANVAS_HEIGHT / height;
		}
		//If the scale is one then no scaling needs to occur
		if (scale == 1.0f) {
			return scene;
		}
		//Calculate the transform for the amount it has to be scale then apply it to the points
		Transform t = Transform.newScale(scale,scale,scale);
		for (Scene.Polygon p : scene.getPolygons()) {
			for (int i=0; i<p.getVertices().length; i++) {
				p.getVertices()[i] = t.multiply(p.getVertices()[i]);
			}
		}
		return new Scene(scene.getPolygons(),scene.getLight());
	}

	/**
	 * Calculate the bounding boxes of the polygons
	 * Finds out the area that they take up
	 * @param polygons
	 * @return
	 */
	public static Rectangle boundingBox(List<Polygon> polygons) {
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;

		float minX = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;

		for (Scene.Polygon poly : polygons) {
			Vector3D[] vectors = Arrays.copyOf(poly.getVertices(), 3);

			for (Vector3D v : vectors) {
				minY = Math.min(minY, v.y);
				maxY = Math.max(maxY, v.y);
				minX = Math.min(minX, v.x);
				maxX = Math.max(maxX, v.x);
			}
		}

		return new Rectangle(Math.round(minX), Math.round(minY), Math.round(maxX - minX), Math.round(maxY - minY));
	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		//Get copy polygon vertices
		Vector3D[] vectors = Arrays.copyOf(poly.getVertices(), 3);
		int minY = Integer.MAX_VALUE;
		int maxY = -Integer.MAX_VALUE;
		//Calculate the largest and smallest value
		for (Vector3D v : vectors) {
			if (v.y > maxY) {
				maxY = Math.round(v.y);
			}
			if (v.y < minY) {
				minY = Math.round(v.y);
			}
		}
		//Create new edgelist between the min and max values
		EdgeList edgeList = new EdgeList(minY, maxY);
		Vector3D a;
		Vector3D b;
		for (int i = 0; i < 3; i++) {
			a = vectors[i];
			b = vectors[(i + 1) % 3];
			//Calculate the slope of the x and z values.
			float slopeX = (b.x - a.x) / (b.y - a.y);
			float slopeZ = (b.z - a.z) / (b.y - a.y);
			float x = a.x;
			int y = Math.round(a.y);
			float z = a.z;
			if (a.y < b.y) {
				while (y <= Math.round(b.y)) {
					edgeList.setMinX(y, x);
					edgeList.setMinZ(y, z);
					x += slopeX;
					z += slopeZ;
					y++;
				}
			} else {
				while (y >= Math.round(b.y)) {
					edgeList.setMaxX(y, x);
					edgeList.setMaxZ(y, z);
					x -= slopeX;
					z -= slopeZ;
					y--;
				}
			}
		}
		return edgeList;
	}


	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 * 
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 * 
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		//Calculate between the min and max y values
		for (int y = polyEdgeList.getStartY(); y < polyEdgeList.getEndY(); y++) {
			//Calculate the slope of the line
			float slope = (polyEdgeList.getRightZ(y) - polyEdgeList.getLeftZ(y))
					/ (polyEdgeList.getRightX(y) - polyEdgeList.getLeftZ(y));

			float z = polyEdgeList.getLeftZ(y);
			int x = Math.round(polyEdgeList.getLeftX(y));
			while (x <= Math.round(polyEdgeList.getRightX(y)) - 1) {
				if (y >= 0 && x >= 0 && y < GUI.CANVAS_HEIGHT && x < GUI.CANVAS_WIDTH && z < zdepth[x][y]) {
					zbuffer[x][y] = polyColor;
					zdepth[x][y] = z;
				}
				z += slope;
				x++;
			}
		}
		reUsableZDepth = zdepth;
		reUsableZbuffer = zbuffer;


	}

	/**
	 * Get ZBuffer
	 * @return
	 */
	public Color[][] getReUsableZbuffer() { return reUsableZbuffer; }

	/**
	 * Get ZDepth
	 * @return
	 */
	public float[][] getReUsableZDepth() { return reUsableZDepth;	}

}

// code for comp261 assignments
