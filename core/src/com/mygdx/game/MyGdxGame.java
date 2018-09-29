package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.FirstOrderInputProcessor;

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
		cam.position.set(100f, 100f, 100f);
		cam.lookAt(0,0,0);
		cam.near  = 1f;
		cam.far = 300f;
		cam.update();

		// create a new model loader
		final ModelLoader modelLoader = new ObjLoader();

		model = modelLoader.loadModel(Gdx.files.internal("stormtrooper.obj"));
		modelSphere = modelLoader.loadModel(Gdx.files.internal("sphere.obj"));




		instance = new ModelInstance(model);
		instance.transform.scale(0.2f,0.2f,0.2f);

		for(int i = 0; i<14;i++){
			instanceSphere = new ModelInstance(modelSphere);
			instanceSphere.transform.scale(1f,1f,1f);
			instanceSkeleton.add(instanceSphere);
		}

		for(int i = 0; i<14;i++){
			instanceSkeleton.get(i).transform.translate(2*(float)data[frameCount*42+i],0,0);
		}

		for(int i = 14; i<28;i++){
			instanceSkeleton.get(i-14).transform.translate(0,-2*(float)data[frameCount*42+i],0);
		}

		for(int i = 28; i<42;i++){
			instanceSkeleton.get(i-28).transform.translate(0,0,2*(float)data[frameCount*42+i]);
		}

		if(frameCount<=50){
			frameCount++;
		}
		else{
			frameCount = 0;
		}

		//instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLACK));

		// set the input processor to work with our custom input:
		//  clicking the image in the lower right should change the colors of the helmets
		//  bonus points: implement your own GestureDetector and an input processor based on it
        Gdx.input.setInputProcessor(new FirstOrderInputProcessor(cam, new Runnable() {
            public void run() {
                // TODO: change the helmet details material to a new diffuse random color

                // bonus points:
                //  randomly change the material of the helmet base to a texture
                //  from the files aloha.png and camouflage.png (or add your own!)
            }
        }));


//		batch = new SpriteBatch();
//		img = new Texture("badlogic.jpg");
//		font = new BitmapFont();
//		font.setColor(Color.BLUE);








	}

	@Override
	public void render () {


		for(int i = 0; i<14;i++){
			instanceSphere = new ModelInstance(modelSphere);
			instanceSphere.transform.scale(4f,4f,4f);
			instanceSkeleton.add(instanceSphere);
		}

		for(int i = 0; i<14;i++){
			instanceSkeleton.get(i).transform.translate(2*(float)data[i],0,0);
		}

		for(int i = 14; i<28;i++){
			instanceSkeleton.get(i-14).transform.translate(0,-2*(float)data[i],0);
		}

		for(int i = 28; i<42;i++){
			instanceSkeleton.get(i-28).transform.translate(0,0,2*(float)data[i]);
		}

//        if(frameCount<=50){
//            frameCount++;
//        }
//        else{
//            frameCount = 0;
//        }






		//Gdx.gl.glClearColor(testRed, 0, 100, 100);
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 100);
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
