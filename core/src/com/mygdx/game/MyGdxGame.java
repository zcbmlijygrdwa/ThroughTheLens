package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
//import com.mygdx.game.FirstOrderInputProcessor;

import java.util.ArrayList;


public class MyGdxGame extends ApplicationAdapter{
	SpriteBatch batch;
	Texture img;
	private BitmapFont font;




	public Environment environment;
	public PerspectiveCamera cam;

	public ModelBatch modelBatch;

	public ModelInstance instance;
	public ModelInstance instanceSphere;

	public ArrayList<ModelInstance> instanceSkeleton = new ArrayList<ModelInstance>();

	public Model model;
	public Model modelSphere;

	float testRed = 0;

	int frameCount = 0;

	float distance = 1;

	float yaw = 0;

	float pitch = 0;

	boolean isTouching = false;
	float x_start = 0;
	float y_start = 0;

	float[] data = new float[42];


	@Override
	public void create () {


		// TODO: create completely new batches for sprites and models
		modelBatch = new ModelBatch();

		// TODO: create a new environment
		// set a new color attribute for ambient light in the environment
		// add a new directional light to the environment
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 0.4f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -.4f, -.4f, -.4f));


		// TODO: create a new perspective camera with a field-of-view of around 70,
		//  and the width and height found in the Gdx.graphics class
		// set the position of the camera to (100, 100, 100)
		// set the camera to look at the origin point (0, 0, 0)
		// set the near and far planes of the camera to 1 and 300
		// update the camera
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(80f, 80f, 80f);
		cam.lookAt(0,0,0);
		cam.near  = 1f;
		cam.far = 300f;
		cam.update();

		// create a new model loader
		final ModelLoader modelLoader = new ObjLoader();

		model = modelLoader.loadModel(Gdx.files.internal("stormtrooper.obj"));
		modelSphere = modelLoader.loadModel(Gdx.files.internal("sphere.obj"));

//
//
//
//		instance = new ModelInstance(model);
//		instance.transform.scale(0.2f,0.2f,0.2f);
//
//		for(int i = 0; i<14;i++){
//			instanceSphere = new ModelInstance(modelSphere);
//			instanceSphere.transform.scale(1f,1f,1f);
//			instanceSkeleton.add(instanceSphere);
//		}
//
//		for(int i = 0; i<14;i++){
//			instanceSkeleton.get(i).transform.translate(2*(float)data[frameCount*42+i],0,0);
//		}
//
//		for(int i = 14; i<28;i++){
//			instanceSkeleton.get(i-14).transform.translate(0,-2*(float)data[frameCount*42+i],0);
//		}
//
//		for(int i = 28; i<42;i++){
//			instanceSkeleton.get(i-28).transform.translate(0,0,2*(float)data[frameCount*42+i]);
//		}
//
//		if(frameCount<=50){
//			frameCount++;
//		}
//		else{
//			frameCount = 0;
//		}

		//instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLACK));

		// set the input processor to work with our custom input:
		//  clicking the image in the lower right should change the colors of the helmets
		//  bonus points: implement your own GestureDetector and an input processor based on it
		CameraInputController cameraInputController = new CameraInputController(cam);
		cameraInputController.pinchZoomFactor = 80f;
        Gdx.input.setInputProcessor(cameraInputController);


//		batch = new SpriteBatch();
//		img = new Texture("badlogic.jpg");
//		font = new BitmapFont();
//		font.setColor(Color.BLUE);








	}

	@Override
	public void render () {


		float translationScale = 12f;
		for(int i = 0; i<14;i++){
			instanceSphere = new ModelInstance(modelSphere);
			//instanceSphere.transform.scale(1f,1f,1f);
			instanceSkeleton.add(instanceSphere);
		}

		for(int i = 0; i<14;i++){
			instanceSkeleton.get(i).transform.translate(translationScale*(float)data[i],-translationScale*(float)data[i+14],translationScale * (float) data[i+28]);
		}


		//Gdx.gl.glLineWidth(32);
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("line", 1, 3, new Material());
		builder.setColor(Color.RED);

		//add lines
		int start = 0;
		int end = 1;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 1;
		end = 2;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 3;
		end = 4;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 4;
		end = 5;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 6;
		end = 3;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 6;
		end = 7;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 6;
		end = 8;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 8;
		end = 9;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 9;
		end = 10;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 6;
		end = 11;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 11;
		end = 12;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 12;
		end = 13;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		start = 6;
		end = 0;
		builder.line(translationScale*(float)data[start], -translationScale*(float)data[start+14], translationScale * (float) data[start+28], translationScale*(float)data[end], -translationScale*(float)data[end+14], translationScale * (float) data[end+28]);

		Model lineModel = modelBuilder.end();
		ModelInstance lineInstance = new ModelInstance(lineModel);
		//lineInstance.transform.scale(1f,1f,1f);


		//Gdx.gl.glClearColor(testRed, 0, 100, 100);
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 100);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		//System.out.println("cam.position = "+cam.position.toString());
		//System.out.println("cam.direction = "+cam.direction.toString());
		float x = cam.direction.x;
		float y = cam.direction.y;
		float z = cam.direction.z;

		double a1 = Math.atan(y/ Vector2.len(x,z));
		double a2 = Math.atan(z/ x);
		//System.out.println("a1 = "+a1+", a2 = "+a2);


		//System.out.println("cam.radius = "+cam.position.len());


		modelBatch.begin(cam);
		//modelBatch.render(instance, environment);
		modelBatch.render(instanceSkeleton, environment);
		modelBatch.render(lineInstance,environment);
		modelBatch.end();


		instanceSkeleton.clear();

//		batch.begin();
//
//		//batch.draw(img, 0, 0);
//		font.getData().setScale(6.0f);
//		font.draw(batch, "Hello World from libgdx running in a fragment! :)", 100, 300);
//
//		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		modelBatch.dispose();
	}

	public float[] getLocalization(){
		float[] location = new float[3];
		location[0] = cam.position.x;
		location[1] = cam.position.y;
		location[2] = cam.position.z;

		return location;
	}

	public void setData(float[] skeleton){
		data = skeleton;
	}

	public boolean check(){
		return true;
	}

	//Change the cameras coordinates and update the view

}
