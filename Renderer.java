package renderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Renderer extends GUI {
	private Scene currentScene = null;

	@Override
	protected void onLoad(File file) {
		String currentString = "";
		boolean skipOnce = true;//Skip line is to skip the first line with only one value in it
		String splitString [];
		float values[];
		Color currentColor;
		Vector3D vecA;
		Vector3D vecB;
		Vector3D vecC;
		Vector3D lightPosition = null;
		Scene.Polygon currentPolygon;
		ArrayList<Scene.Polygon> listOfPolygons = new ArrayList<>();

		try{
			BufferedReader input = new BufferedReader(new FileReader(file));

			while (currentString != null){
				currentString = input.readLine();

				if (currentString != null && skipOnce == false){
					splitString = currentString.split(",");

					//I assign all the splitStrings into a values so that they can all be converted to floats
					values = new float[splitString.length];
					for (int i = 0; i < splitString.length; i++){
						values[i] = Float.valueOf(splitString[i]);
					}
					if (values.length > 3){// determine if this is a polygon
						currentColor = new Color((int)values[0],(int)values[1],(int)values[2]);
						vecA = new Vector3D(values[3], values[4], values[5]);
						vecB = new Vector3D(values[6], values[7], values[8]);
						vecC = new Vector3D(values[9], values[10], values[11]);

						currentPolygon = new Scene.Polygon(vecA, vecB, vecC, currentColor);
						listOfPolygons.add(currentPolygon);// add the polygon to the list
					}
					else {// determine the light position
						lightPosition = new Vector3D(values[0], values[1], values[2]);
					}

				}
				else {
					skipOnce= false;
				}
			}

			if (lightPosition != null || listOfPolygons.size() != 0 ){
				this.currentScene = new Scene(listOfPolygons, lightPosition);
			}
			else {
				System.out.println("missing info");
			}

		}catch (Exception e){System.out.println(e);}

		/*
		 * This method should parse the given file into a Scene object, which
		 * you store and use to render an image.
		 */
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
		/*
		 * This method should be used to rotate the user's viewpoint in the direction of the key pressed.
		 * WASD could be used as well
		 */
		if(ev.getKeyCode() == KeyEvent.VK_LEFT || ev.getKeyCode() == KeyEvent.VK_A){
			currentScene = Pipeline.rotateScene(currentScene, 0,(float) (-0.1*Math.PI));

		}else if(ev.getKeyCode() == KeyEvent.VK_RIGHT || ev.getKeyCode() == KeyEvent.VK_D){
			currentScene = Pipeline.rotateScene(currentScene, 0,(float) (0.1*Math.PI));

		}else if(ev.getKeyCode() == KeyEvent.VK_UP|| ev.getKeyCode() == KeyEvent.VK_W){
			currentScene = Pipeline.rotateScene(currentScene, (float) (0.1*Math.PI), 0);

		}else if(ev.getKeyCode() == KeyEvent.VK_DOWN || ev.getKeyCode() == KeyEvent.VK_S){
			currentScene = Pipeline.rotateScene(currentScene, (float) (-0.1*Math.PI), 0);
		}
	}

	@Override
	protected BufferedImage render() {
		//Check that the currentScene isn't null
		if(currentScene == null) {
			return null;
		}
		//Translate and scale the scene so that it is on the screen
		currentScene = Pipeline.translateScene(currentScene);
		currentScene = Pipeline.scaleScene(currentScene);
		BufferedImage returnValue = null;
		Pipeline currentPipeline = new Pipeline();
		EdgeList currentEdgeList;
		Color currentColor;

		//Create an empty zBuffer and zDepth 2D array of the size of the canvas
		Color[][] zBuffer = new Color[GUI.CANVAS_HEIGHT][GUI.CANVAS_WIDTH];
		float[][] zDepth = new float[GUI.CANVAS_HEIGHT][GUI.CANVAS_WIDTH];

		//Calculate ambient light and light colour
		Color ambientLight = new Color(getAmbientLight()[0], getAmbientLight()[1],getAmbientLight()[2]);
		Color lightColor = new Color(255,255,255);

		//Set all the values to white so that the background will be white
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				zBuffer[x][y] = Color.white;
				zDepth[x][y] = Float.POSITIVE_INFINITY;
			}
		}

		if (this.currentScene != null){
			//For all the polygons
			for (int i = 0 ; i < currentScene.getPolygons().size(); i++){
				//Check that the polygon isn't hidden before calculating the zBuffer and zDepth
				if (!currentPipeline.isHidden(currentScene.getPolygons().get(i))){
					currentEdgeList = currentPipeline.computeEdgeList(currentScene.getPolygons().get(i));
					currentColor = currentPipeline.getShading(currentScene.getPolygons().get(i), currentScene.getLight(), lightColor, ambientLight);
					currentPipeline.computeZBuffer(zBuffer, zDepth, currentEdgeList, currentColor);
					zBuffer = currentPipeline.getReUsableZbuffer();
					zDepth = currentPipeline.getReUsableZDepth();
				}
			}
			returnValue = convertBitmapToImage(zBuffer);
		}


		/*
		 * This method should put together the pieces of your renderer, as
		 * described in the lecture. This will involve calling each of the
		 * static method stubs in the Pipeline class, which you also need to
		 * fill in.
		 */
		return returnValue;
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				if (bitmap[x][y] != null) image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments
