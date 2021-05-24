package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/*
 * TODO (in order of importance):
 * 
 * \ get basics of camera working (e.g. - figure out perspective)
 * 
 * \ create and implement placeholders for landscape and character models
 * 
 * \ figure out basic player controls
 * 
 * \ figure out how to make camera bound to player and loosely follow mouse on screen
 * 
 * \ figure out how to animate style of graphics
 * 
 * \ figure out proper resolution scaling between windowed and fullscreen
 * 
 * \ figure out enemy ai
 * 
 * \ figure out how to balance difficulty
 * 
 * \ figure out what the style of the ui will be
 * 
 * \ figure out what style of graphics im going to do
 * 
 * - figure out how to do proper landscapes and character models after placeholders
 * 
 * - work on ambience, music, and sound design (can be done concurrently)
 * 
 * \ convert all character movement to box2d
 * 
 * - assess quality and time left when this is all done, decide whether or not to
 *   implement more features like inventory, story, save mechanics, a polished
 *   transition between the main menu and game world, etc.
 *   
 * - or alternatively, just POLISH, POLISH, POLISH
 * 
 * - add a pause function
 * 
 * first wave of placeholders may just be rudimentary colored cubes for simplicity and
 * ease of time investment
 * 
 * DONT FORGET TO OPTIMIZE DISPOSAL OF MAIN MENU AND GAME WORLD, NO MEMORY LEAKS
 */

/*
 * You don't have to restrict the use of state machines to agents. The state design pattern is also useful 
 * for structuring the main components of your game flow. For example, you could have a menu state, a save 
 * state, a paused state, an options state, a run state, etc.
 */
public class GameWorld2D implements Screen, InputProcessor
{
	public static final int VELOCITY_ITERATIONS = 6;
	public static final int POSITION_ITERATIONS = 6;
	public static final float TIME_STEP = 1/60f;
	
	final Master game;
	final Stage stage;
	final World world;
	private boolean difficulty, showWorldCoords,
	debugMode;
	private int numberOfZombies;
	private Texture landscape;
	private OrthographicCamera camera;
	private Viewport viewport;
	private Vector2 mouseInWorld2D;
	private Vector3 mouseInWorld3D;
	private Pixmap pm;
	private int xHotSpot, yHotSpot;
	private Player player;
	private Zombie[] zombies;
	private Box2DDebugRenderer debugRenderer;
	public FrictionJointDef jointDef;
	private float accumulator = 0f;
	public ShapeRenderer shapeRenderer;
	public Color healthColor;
	
	
	// general constructor of the game world that calls subconstructors for more detailed portions of the construction process
	public GameWorld2D(final Master game, boolean difficulty) 
	{	
		world = new World(new Vector2(0, 0), true);
		debugRenderer = new Box2DDebugRenderer();
		pm = new Pixmap(Gdx.files.internal("crosshair.png"));
		xHotSpot = pm.getWidth()/2;
		yHotSpot = pm.getHeight()/2;
		Cursor cursor = Gdx.graphics.newCursor(pm, xHotSpot, yHotSpot);
		Gdx.graphics.setCursor(cursor);
		landscape = new Texture(Gdx.files.internal("Textures\\TestingRoomLarge.png"));
		mouseInWorld3D = new Vector3(0, 0, 0);
		mouseInWorld2D = new Vector2(0, 0);
		debugMode = false;
		
		this.game = game;
		game.batch = new SpriteBatch();
		camera = new OrthographicCamera();
		viewport = new FitViewport(51.2f, 28.8f, camera);
		stage = new Stage(viewport);
		stage.addListener(new InputListener());
		
		this.difficulty = difficulty;
		player = new Player(game, this.world, difficulty);
		if(difficulty) numberOfZombies = 16;
		else numberOfZombies = 10;
		zombies = new Zombie[numberOfZombies];
		for(int i = 0; i < zombies.length; i++) 
		{
			zombies[i] = new Zombie(game, this.world, difficulty);
		}
		
		Gdx.input.setInputProcessor(stage);
		
		world.setContactListener(new ContactListener() 
		{
			@Override
			public void endContact(Contact contact) 
			{
				Fixture fixtureA = contact.getFixtureA();
				Fixture fixtureB = contact.getFixtureB();
				/*Gdx.app.log("endContact", "between " + fixtureA.getFilterData().categoryBits
				+ " and " + fixtureB.getFilterData().categoryBits);*/
				if(fixtureA.getBody().getUserData().getClass().equals(Player.class))
				{
					if(fixtureB.getBody().getUserData().getClass().equals(Zombie.class))
					{
						if((fixtureA == ((Player)fixtureA.getBody().getUserData()).bodyFixture)
							&& fixtureB == ((Zombie)fixtureB.getBody().getUserData()).armsFixture) 
						{
							for(Zombie zombie : zombies) 
							{
								if(fixtureB.getBody().getUserData().equals(zombie)) 
								{
									zombie.hitPlayer = false;
								}
							}
						}
						if ((fixtureA == ((Player)fixtureA.getBody().getUserData()).weaponFixture)
								&& (fixtureB == ((Zombie)fixtureB.getBody().getUserData()).armsFixture
								|| fixtureB == ((Zombie)fixtureB.getBody().getUserData()).torsoFixture)) 
						{
							for(Zombie zombie : zombies) 
							{
								if(fixtureB.getBody().getUserData().equals(zombie)) 
								{
									zombie.hitByPlayer = false;
								}
							}
						}
					}
				}
			}

			@Override
			public void beginContact(Contact contact) 
			{
				Fixture fixtureA = contact.getFixtureA();
				Fixture fixtureB = contact.getFixtureB();
				Gdx.app.log("beginContact", "between " + fixtureA.getFilterData().categoryBits
				+ " and " + fixtureB.getFilterData().categoryBits);
				if(fixtureA.getBody().getUserData().getClass().equals(Player.class))
				{
					if(fixtureB.getBody().getUserData().getClass().equals(Zombie.class))
					{
						if((fixtureA == ((Player)fixtureA.getBody().getUserData()).bodyFixture)
							&& fixtureB == ((Zombie)fixtureB.getBody().getUserData()).armsFixture) 
						{
							for(Zombie zombie : zombies) 
							{
								if(fixtureB.getBody().getUserData().equals(zombie)) 
								{
									zombie.hitPlayer = true;
								}
							}
						}
						if ((fixtureA == ((Player)fixtureA.getBody().getUserData()).weaponFixture)
								&& (fixtureB == ((Zombie)fixtureB.getBody().getUserData()).armsFixture
								|| fixtureB == ((Zombie)fixtureB.getBody().getUserData()).torsoFixture)) 
						{
							for(Zombie zombie : zombies) 
							{
								if(fixtureB.getBody().getUserData().equals(zombie)) 
								{
									zombie.hitByPlayer = true;
								}
							}
						}
					}
				}
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {}
		});
		
		shapeRenderer = new ShapeRenderer();
	}
	
	
	// general handler of all render activities and methods called each frame
	@Override
	public void render (float delta) 
	{
		if(game.waveZombiesDead == zombies.length) 
		{
			game.waveZombiesDead = 0;
			zombies = new Zombie[numberOfZombies];
			for(int i = 0; i < zombies.length; i++) 
			{
				zombies[i] = new Zombie(game, this.world, difficulty);
			}
		}
		for(Zombie zombie : zombies) zombie.playerPosition = player.body.getPosition();
		game.batch.setProjectionMatrix(viewport.getCamera().combined);
		renderGL(delta);
		renderHotKeys(delta);
		renderCamera(delta);
		game.batch.begin();
		game.batch.draw(landscape, 0, 0, landscape.getWidth()/50, landscape.getHeight()/50);
		for(Zombie zombie : zombies) zombie.timePassed += delta;
		for(Zombie zombie : zombies) zombie.update(delta);
		if(player.isDead) 
		{
			System.out.println();
			for(Zombie zombie : zombies) {zombie.setDetectionRadius(0); zombie.setAttackRadius(0);}
			game.fontEasy.getData().setScale(0.07f);
			game.fontEasy.setUseIntegerPositions(false);
			game.fontEasy.draw(game.batch, ("ZOMBIES KILLED: " + game.totalZombiesDead), 
					player.body.getPosition().x-10f, player.body.getPosition().y+2f);
			System.out.println("Zombies Killed: " + game.totalZombiesDead);
			player.body.setAngularVelocity(0);
			player.body.setLinearVelocity(0, 0);
		}
		else 
		{
			player.timePassed += delta;
			player.update(delta);
		}
		renderUI(delta);
		game.batch.end();
		doPhysicsStep(delta);
		handleContact();
		
		// makes sure that the method does not execute the rest of the debug code if not true
		if(!debugMode) return;
		
		debugRenderer.render(world, viewport.getCamera().combined);
	}
	
	
	// variable timestep method for consistent physics updates between inconsistent framerates
	public void doPhysicsStep(float delta) 
	{
		float frameTime = Math.min(delta, 0.25f);
		accumulator += frameTime;
		while (accumulator >= TIME_STEP) 
		{
			world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
			accumulator -= TIME_STEP;
		}
	}
	
	
	// handles all of the rendering work done by libGDX's usage of openGL
	public void renderGL(float delta) 
	{
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
	}
	
	
	// handles rendering of the UI
	public void renderUI(float delta) 
	{
		game.batch.end();
		shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
		shapeRenderer.begin(ShapeType.Filled);
		if(player.getHealth() > 0) 
		{
			if (player.getHealth() > 25) healthColor = Color.WHITE;
			else healthColor = Color.RED;
			shapeRenderer.rect(player.body.getPosition().x-0.75f, player.body.getPosition().y+1.35f, 
					0, 0.5f, .015f*(player.getHealth()), 0.25f, 1, 1, 0, 
					healthColor, healthColor, healthColor, healthColor);
		}
		for(Zombie zombie : zombies) 
		{
			if(zombie.getHealth() > 0) 
			{
				if (zombie.getHealth() > 25) healthColor = Color.WHITE;
				else healthColor = Color.RED;
				shapeRenderer.rect(zombie.body.getPosition().x-0.75f, zombie.body.getPosition().y+1.35f, 
						0, 0.5f, .015f*(zombie.getHealth()), 0.25f, 1, 1, 0, 
						healthColor, healthColor, healthColor, healthColor);
			}
		}
		shapeRenderer.end();
		game.batch.begin();
	}
	
	
	// handles all of the rendering work done for the camera
	public void renderCamera(float delta) 
	{
		mouseInWorld3D.x = viewport.getScreenWidth()/2 - Gdx.input.getX();
		mouseInWorld3D.y = viewport.getScreenHeight()/2 - Gdx.input.getY();
		mouseInWorld3D.z = 0;
		// viewport.unproject(mouseInWorld3D);
		mouseInWorld2D.x = mouseInWorld3D.x;
		mouseInWorld2D.y = -mouseInWorld3D.y;
		player.mouseInWorld = mouseInWorld2D;
		viewport.getCamera().position.set(player.body.getPosition().x, player.body.getPosition().y, 1);
		viewport.getCamera().update();
		viewport.apply();
	}
	
	
	// handles the rendering of debug hotkey shortcuts for the game world
	public void renderHotKeys(float delta) 
	{
		// world coords shortcut if-block
		if(Gdx.input.isKeyJustPressed(Keys.F2))
		{
			if(showWorldCoords == false) 
			{
				showWorldCoords = true;
			}
			else if (showWorldCoords == true) 
			{
				showWorldCoords = false;
			}
		}
		
		// hitbox debug shortcut if-block
		if(Gdx.input.isKeyJustPressed(Keys.F3)) 
		{
			if(debugMode) 
			{
				debugMode = false;
				System.out.println("Debug deactivated");
			}
			else if(!debugMode) 
			{
				debugMode = true;
				System.out.println("Debug activated");
			}
		}
		
		// fullscreen shortcut if-block
		if(Gdx.input.isKeyJustPressed(Keys.F5))
		{
			if(game.fullscreen == false) 
			{
				game.fullscreen = true;
				Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
				resize(2560, 1440);
			}
			else if (game.fullscreen == true) 
			{
				game.fullscreen = false;
				Gdx.graphics.setWindowedMode(1920, 1080);
				resize(1920, 1080);
			}
		}
	}
	
	
	// handle contact between player and enemies in various possible scenarios
	public void handleContact() 
	{
		for(Zombie zombie : zombies) 
		{
			if(zombie.hitPlayer) 
			{
				if(zombie.stateMachine.isInState(ZombieState.ATTACK)
				&& zombie.timePassed > 0.9f 
				&& zombie.timePassed < 1.33f
				&& player.damagedCooldown > 0.6f
				&& zombie.getAttackOpenerAnimation().isAnimationFinished(zombie.timePassed) == false) 
				{
					player.damagedCooldown = 0f;
					zombie.hitPlayer = false;
					player.setHealth(player.getHealth()-zombie.getDamage());
				}
			}
			if(zombie.hitByPlayer) 
			{
				if(player.stateMachine.isInState(PlayerState.ATTACK)
				&& player.timePassed < 0.22f
				&& zombie.damagedCooldown > 0.3f
				&& player.getAttackOpenerAnimation().isAnimationFinished(player.timePassed) == false) 
				{
					zombie.damagedCooldown = 0f;
					zombie.hitByPlayer = false;
					zombie.setHealth(zombie.getHealth()-player.getDamage());
				}
			}
		}
	}
	
	
	@Override
	public void dispose () 
	{
		player.dispose();
		for(Zombie zombie : zombies) zombie.dispose();
		landscape.dispose();
		pm.dispose();
		shapeRenderer.dispose();
		stage.dispose();
		world.dispose();
		game.dispose();
	}
	
	
	@Override
	public void show() {}
	
	@Override
	public void resize(int width, int height) 
	{
		stage.getViewport().update(width, height);
	}
	
	@Override
	public void pause() {}
	@Override
	public void resume() {}
	@Override
	public void hide() {}
	@Override
	public boolean keyDown(int keycode) {return true;}
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
