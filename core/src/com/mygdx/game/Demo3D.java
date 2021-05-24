package com.mygdx.game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.UBJsonReader;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/*
 * scrapped 3D base for the game world. i realized that i wouldn't be able to learn 
 * libGDX 3D, create the assets and program the minimum viable product all before
 * the project would be due, so now this just functions as a nice, extremely basic
 * demo of 3D with a premade character model and some notes on a small variety of
 * subjects that pertain to 3D
 */
public class Demo3D implements Screen, InputProcessor
{
	final Master game;
	private boolean difficulty;
	private PerspectiveCamera camera; // use perspective camera in 3D, orthogonal camera for 2D
	private Viewport viewport;
	private ModelBatch modelBatch; // just like a sprite batch, but for 3D models
	private ModelBuilder modelBuilder; // model builder makes it easy to make simple 3D shapes
	private Model cube, playerModel;
	// model instance contains the instance-specific data of a model like position, translation, etc.
	// the model is the blueprint, the instance is the product
	private ModelInstance cubeModelInstance, playerModelInstance;
	private Environment environment;
	private Vector3 pointOfRotation_Center, axesOfRotation_Y;
	private AnimationController controller;
	private UBJsonReader jsonReader;
	private G3dModelLoader modelLoader;
	
	
	// general constructor of the game world that calls subconstructors for more detailed portions of the construction process
	public Demo3D(final Master game, boolean difficulty) 
	{
		this.game = game;
		game.batch = new SpriteBatch();
		camera = new PerspectiveCamera(75, game.GAME_WORLD_WIDTH, game.GAME_WORLD_HEIGHT);
		camera.position.set(200f, 150f, 200f);
		camera.lookAt(0f, 100f, 0f);
		camera.near = 0.01f;
		camera.far = 2000f;
		
		pointOfRotation_Center = new Vector3(0f, 0f, 0f);
		axesOfRotation_Y = new Vector3(0f, 1f, 0f);
		
		// create 3d model loader that reads json-formatted files
		jsonReader = new UBJsonReader();
		modelLoader = new G3dModelLoader(jsonReader);
		playerModel = modelLoader.loadModel(Gdx.files.getFileHandle
				("Models\\Dummy\\Dummy.g3db", FileType.Internal));
		modelConstructor(game);
		
		Gdx.input.setInputProcessor(this);
	}
	
	
	// handles the constructor processes of all things related to models
	public void modelConstructor(Master game) 
	{
		modelBatch = new ModelBatch();
		modelBuilder = new ModelBuilder();
		// material is what the surface is covered in, diffuse is the color that is unaffected by light sources
		// ambient is lighting all around you without a direct source, reflection is the reflection from other 
		// surfaces, specular is the result of a light being directly shined on the surface, and emissive is 
		// to cast light
		//
		// the attributes we give the modelBuilder tell it what data is important (like position data)
		// for this box, what's important is position data (coords in 3D space) and normal data (direction 
		// that this object faces). 
		//
		// - normal decides in which direction something is drawn, which is important
		//   for things like landscapes that are invisible from the side they're not meant to be viewed from
		//   (think about glitching through the floor in games like Skyrim or Fallout). 
		//
		// the bitwise math used in the attribute field presented essentially allows us to tell the program
		// both about positional data AND normal data, instead of only being able to insert one
		//
		// the cube can be thought of as an archetype. it's a 3D model with lots of built-in information.
		// there are object methods for animation, materials, submeshes (a body built out of multiple
		// meshes like a torso, arms, hands, shoulders, etc), individual parts of meshes (like vertices,
		// every index, etc)
		//
		// this is an extremely complex class, but only has to be used once (an archetype for a brick would 
		// only have to be designed once, and then it could be reused for every brick instead of
		// having to make a new one for every time you make an instance of a brick
		cube = modelBuilder.createBox(.5f, .5f, .5f, new Material(ColorAttribute.createDiffuse(Color.BLUE)), 
				VertexAttributes.Usage.Position|VertexAttributes.Usage.Normal); // this is bitwise math
		cubeModelInstance = new ModelInstance(cube, 0, 0, 0); // create an instance of cube at the position
		playerModelInstance = new ModelInstance(playerModel, 1f, 1f, 1f);
		// the environment is part of making shaders used in the background; used to implement lighting, etc.
		environment = new Environment();
		// add light grey ambient lighting to the environment with the desired intensity (rgbi, not rgba)
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
		
		environment.add();
		controller = new AnimationController(playerModelInstance);
		controller.setAnimation("mixamo.com", -1, new AnimationController.AnimationListener() 
		{
			@Override
			public void onEnd(AnimationDesc animation) {}
			
			@Override
			public void onLoop(AnimationDesc animation) 
			{
				Gdx.app.log("INFO", "Animation Ended");
			}
		});
	}
	
	
	// general handler of all render activities and methods called each frame
	@Override
	public void render (float delta) 
	{
		renderGL(delta);
		renderCamera(delta);
		game.batch.begin();
		game.batch.end();
	}
	
	
	// handles all of the rendering work done by libGDX's usage of openGL
	public void renderGL(float delta) 
	{
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		// this is bitwise math. it allows us to put in one integer of space to encode multiple bits of data
		//
		// e.g. - a = 5 = 0101
		//      - b = 7 = 0111
		//
		// this allows us to optimize by bit packing, or bundling several true/false (a.k.a two-state, or binary
		// represented by a bit) values into a single data point for more efficient memory use.
		//
		//		- when using the bitwise OR ('|') operator, if EITHER of the bit values are 1, it returns 1,
		//        else it gives 0. 
		//		  e.g. - 'a|b = 0111 = 7'
		//
		//      - bitwise AND ('&') returns 1 if BOTH bits are 1; else, 0.
		//		  e.g. - 'a&b = 0101 = 5'
		//
		//      - bitwise XOR ('^') returns 1 if the compared bits are different, else 0 
		//        (so they can't be the same).
		//        e.g. - 'a^b = 0010 = 2'
		//
		// 		- bitwise NOT ('~') returns the opposite of the bit, so 0 if the bit is 1 and vice versa.
		//		  e.g. - '~a = 1010 = 10'
		//
		// shift operators shift the bits of a number left or right, thereby multiplying or dividing the number
		// by two, respectively. the general format is 'number SHIFT_OP numberOfPlacesShifted'.
		//
		//		- Signed Right shift ('>>') shifts the bits of a number to the right and fills the empty spaces with
		//		  bits representing the sign of the numer (1 for negative, 0 for positive). similar to dividing by
		//        a power of two. preserves the sign of a number
		//        e.g. - 'a = 10', therefore 'a>>1 = 5'
		//				'b = -10', therefore 'b>>1 = -5'
		//
		//		- Unsigned Right shift ('>>>') shifts the bits of a number to the left and fills the empty spaces
		//		  with 0. does NOT preserve the sign bit
		//		  e.g. - 'a = 10', therefore 'a>>>1 = 5'
		//				 'b = -10', therefore 'b>>>1 = 2147483643'
		//
		//		- Signed Left shift ('<<') shifts the number bit to the left and fills empty spaces with 0. similar
		//		  to multiplying by a power of two. preserves the sign of a number
		//		  e.g. - 'a = 0000 0101 = 5', therefore 'a<<1 = 0000 1010 = 10' and 'a<<2 = 0001 0100 = 20'
		//				 'b = 1111 0110 = -10', therefore 'b<<1 = 1110 1100 = -20' and 'b<<2 = 1101 1000 = -40'
		//
		// there is no Unsigned Left shift operator in Java because the logical operation of '<<' and
		//  '<<<' are identical
		//
		// bitwise math will typically be used in the case of this class to insert multiple values into a single
		// variables
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
		camera.update();
		controller.update(delta);
		modelBatch.begin(camera);
		modelBatch.render(cubeModelInstance, environment); // renders the instance of the box under the environment conditions
		// a model batch can also be passed any data container that iterates iterable (arraylists, vectors, etc). we'd 
		// generally be doing that instead of calling every individual model instance, just to make the code nicer to
		// look at
		modelBatch.render(playerModelInstance, environment);
		modelBatch.end();
	}
	
	
	// handles all of the rendering work done for the camera
	public void renderCamera(float delta) 
	{
		
	}
	
	
	@Override
	public void dispose () 
	{
		game.batch.dispose();
		modelBatch.dispose();
		cube.dispose();
		playerModel.dispose();
	}

	
	@Override
	public void show() {}
	@Override
	public void resize(int width, int height) {}
	@Override
	public void pause() {}
	@Override
	public void resume() {}
	@Override
	public void hide() {}
	@Override
	// modified keyDown to return true when some keys are pressed, used to test camera basics
	public boolean keyDown(int keycode) 
	{
		// for rotating around two or three axes at the same time, we'll use rotation units
		// called quaternions to avoid a phenomenon that happens when a camera rotates to
		// certain point using normal units, where the mathematics behind the camera become
		// very skewed and confusing, and the camera goes spinning off into space. a quaternion
		// itself is an extremely complicated mathematical equation that has a fourth dimension
		// that makes rotation like this possible
		if(keycode == Input.Keys.A) 
		{
			// the first value of this method is the point being rotated around, the second value
			// is the the axes to rotate around, and the last value is the degrees to rotate by. 
			// in this case we're telling it to rotate 1 degree around the y-axis at the center
			camera.rotateAround(pointOfRotation_Center, axesOfRotation_Y, 10f);
		}
		if(keycode == Input.Keys.D) 
		{
			// this tells the camera to rotate -1 degree around the y-axis at the center
			camera.rotateAround(pointOfRotation_Center, axesOfRotation_Y, -10f);
		}
		return true;
	}
	@Override
	public boolean keyUp(int keycode) {return false;}
	@Override
	public boolean keyTyped(char character) {return false;}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {return false;}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {return false;}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {return false;}
	@Override
	public boolean scrolled(float amountX, float amountY) {return false;}
}
