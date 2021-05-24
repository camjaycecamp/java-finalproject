package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

/*
 * TODO: 
 * 
 * - MAYBE implement individually fading letters into titlecard
 * 
 * - implement Celtsoup splash screen into intro sequence
 */
public class TitleScreen implements Screen
{
	private final Master game;
	private Stage stage;
	private final float fadeInTime = 3f;
	private float introElapsed, exitElapsed, beginElapsed, titleFade,
	optionsFade, exitFade, difficultyFade;
	private OrthographicCamera camera;
	private Viewport viewport;
	private Sound hoverSound, selectSound, beginSound, cancelSound,
	negativeSound;
	private boolean introSequence, exitSequence, beginSequence, mainMenu, configMenu,
	difficultyMenu, chosenDifficulty;
	private TextButton menuBeginButton, menuConfigButton, menuExitButton,
	configVolUpButton, configVolDownButton, configFullscreenButton, configBackButton,
	difficultyExplorerButton, difficultyAshboundButton, difficultyBackButton;
	private TextButtonStyle buttonStyle, buttonStyleLarge;
	
	public TitleScreen(final Master game) 
	{
		this.game = game;
		game.font.setColor(1, 1, 1, 0);
		
		hoverSound = Gdx.audio.newSound(Gdx.files.internal("Sounds\\Option Hover.wav"));
		selectSound = Gdx.audio.newSound(Gdx.files.internal("Sounds\\Option Select.wav"));
		cancelSound = Gdx.audio.newSound(Gdx.files.internal("Sounds\\Option Cancel.wav"));
		beginSound = Gdx.audio.newSound(Gdx.files.internal("Sounds\\It Begins.wav"));
		negativeSound = Gdx.audio.newSound(Gdx.files.internal("Sounds\\Option Negative.wav"));
		
		camera = new OrthographicCamera();
		viewport = new FitViewport(1920, 1080, camera);
		stage = new Stage(viewport);
		// add an overridden InputListener to the stage, allowing a debug shortcut to jump
		// straight into the game world defaulted at Ashbound difficulty to be created
		stage.addListener(new InputListener() 
		{
			@Override
			public boolean keyDown(InputEvent event, int keycode) 
			{
				super.keyDown(event, keycode);
				if(introSequence == true || mainMenu == true) 
				{
					if(keycode == Input.Keys.F1) 
					{
						game.setScreen(new GameWorld2D(game, true));
						dispose();
					}
				}
				return true;
			}
		});
		Gdx.input.setInputProcessor(stage);
		
		buttonSetup();
		
		introElapsed = 0;
		exitElapsed = 0;
		beginElapsed = 0;
		introSequence = true;
		exitSequence = false;
		beginSequence = false;
		mainMenu = false;
		configMenu = false;
		difficultyMenu = false;
	}
	
	@Override
	public void show() {}

	// ran every frame
	@Override
	public void render(float delta) 
	{
		ScreenUtils.clear(0, 0, 0, 1);
		viewport.apply();
		// bounds batch to the camera's coords
		game.batch.setProjectionMatrix(camera.combined);
		delta = Gdx.graphics.getDeltaTime();
		stage.draw();
		stage.act();
		
		introRender(delta);
		exitTitle(delta);
		mainMenuRender(delta);
		configMenuRender(delta);
		difficultyMenuRender(delta);
		beginGame(delta);
	}
	
	
	// handles the temporary rendering of the intro sequence
	public void introRender(float delta) 
	{
		if(introSequence == true)
		{
			game.batch.begin();
			introElapsed += delta;
			titleFade = Interpolation.fade.apply((introElapsed - 1f) / fadeInTime);
			optionsFade = Interpolation.fade.apply((introElapsed - 3.0f) / fadeInTime);
			game.font.getData().setScale(3.5f);
			game.font.setColor(0.6f, 0.6f, 0.6f, titleFade);
			game.font.draw(game.batch, "ASHBOUND", 200, 800);
			game.font.getData().setScale(1.2f);
			game.font.setColor(1, 1, 1, optionsFade);
			game.font.draw(game.batch, "begin", 200, 600);
			game.font.draw(game.batch, "configuration", 200, 500);
			game.font.draw(game.batch, "exit", 200, 400);
			game.batch.end();
			
			if(introElapsed >= 5.0f) 
			{
				introSequence = false;
				mainMenu = true;
			}
		}
	}
	
	
	// handles the rendering of the main menu
	public void mainMenuRender(float delta) 
	{
		if(mainMenu == true)
		{
			game.batch.begin();
			game.font.getData().setScale(3.5f);
			game.font.setColor(0.6f, 0.6f, 0.6f, titleFade);
			game.font.draw(game.batch, "ASHBOUND", 200, 800);
			game.font.getData().setScale(1.2f);
			game.font.setColor(1, 1, 1, optionsFade);
			if(menuBeginButton.getClickListener().isOver()) 
			{
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "begin", 200, 602);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "begin", 200, 600);
			}
			if(menuConfigButton.getClickListener().isOver()) 
			{
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "configuration", 200, 502);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "configuration", 200, 500);
			}
			if(menuExitButton.getClickListener().isOver()) 
			{
				game.font.setColor(0.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "exit", 200, 402);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "exit", 200, 400);
			}
			game.batch.end();
		}
	}
	
	
	// handles the rendering of the configuration menu
	public void configMenuRender(float delta) 
	{
		if(configMenu == true)
		{
			game.batch.begin();
			game.font.getData().setScale(3.5f);
			game.font.setColor(0.6f, 0.6f, 0.6f, titleFade);
			game.font.draw(game.batch, "ASHBOUND", 200, 800);
			game.font.getData().setScale(1.2f);
			game.font.setColor(1, 1, 1, optionsFade);
			game.font.draw(game.batch, "begin", 200, 600);
			game.font.draw(game.batch, "configuration", 200, 500);
			game.font.draw(game.batch, "exit", 200, 400);
			if(configFullscreenButton.getClickListener().isOver()) 
			{
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "FULLSCREEN (EXPERIMENTAL)", 657, 602);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "FULLSCREEN (EXPERIMENTAL)", 657, 600);
			}
			if(configVolUpButton.getClickListener().isOver()) 
			{
				game.font.getData().setScale(2.0f);
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "+", 660, 522);
			}
			else 
			{
				game.font.getData().setScale(2.0f);
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "+", 660, 520);
			}
			if(configVolDownButton.getClickListener().isOver()) 
			{
				game.font.getData().setScale(2.0f);
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "-", 1200, 522);
			}
			else 
			{
				game.font.getData().setScale(2.0f);
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "-", 1200, 520);
			}
			game.font.getData().setScale(1.2f);
			game.font.setColor(1, 1, 1, optionsFade);
			game.font.draw(game.batch, "VOLUME: " + (int)(game.masterVolume * 100), 820, 500);
			if(configBackButton.getClickListener().isOver()) 
			{
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "back", 1126, 402);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "back", 1126, 400);
			}
			game.batch.end();
		}
	}
	
	
	// handles the rendering of the difficulty menu
	public void difficultyMenuRender(float delta) 
	{
		if(difficultyMenu == true)
		{
			game.batch.begin();
			game.font.getData().setScale(3.5f);
			game.font.setColor(0.6f, 0.6f, 0.6f, titleFade);
			game.font.draw(game.batch, "ASHBOUND", 200, 800);
			game.font.getData().setScale(1.2f);
			game.font.setColor(1, 1, 1, optionsFade);
			game.font.draw(game.batch, "begin", 200, 600);
			game.font.draw(game.batch, "configuration", 200, 500);
			game.font.draw(game.batch, "exit", 200, 400);
			game.font.draw(game.batch, "difficulty:\n------------", 657, 602);
			if(difficultyAshboundButton.getClickListener().isOver()) 
			{
				game.font.setColor(.8f, 0, 0, 2);
				game.font.draw(game.batch, "ASHBOUND", 680, 502);
				game.fontEasy.setColor(1, 1, 1, 1);
				game.fontEasy.draw(game.batch, "ASH STORMS ARE UNYIELDING, COMBAT "
						+ "\nIS DESPARATE, RESOURCES ARE PRECIOUS; "
						+ "\nDIFFICULT CHOICES WILL BE MADE, AND YOU "
						+ "\nWILL LEARN TO FEAR ENCOUNTERS WITH "
						+ "\nOTHERS ON THE FRONTIER. THIS IS THE"
						+ "\nWAY ASHBOUND IS MEANT TO BE PLAYED.", 1000, 552);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "ASHBOUND", 680, 500);
			}
			if(difficultyExplorerButton.getClickListener().isOver()) 
			{
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "EXPLORER", 715, 432);
				game.fontEasy.setColor(1, 1, 1, 1);
				game.fontEasy.draw(game.batch, "THE WORLD IS A MORE FORGIVING PLACE; "
						+ "\nDIFFICULT CHOICES CAN BE AVOIDED WITH "
						+ "\nTHE RIGHT PLANNING, AND YOU WILL FIND "
						+ "\nTRIUMPH OVER ALL ENEMIES THROUGH ENOUGH "
						+ "\nEFFORT. FOR THOSE WHO SIMPLY WANT TO"
						+ "\nEXPERIENCE THE STORY OF ASHBOUND.", 1000, 552);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "EXPLORER", 715, 430);
			}
			if(difficultyBackButton.getClickListener().isOver()) 
			{
				game.font.setColor(.8f, 0, 0, optionsFade);
				game.font.draw(game.batch, "back", 800, 352);
			}
			else 
			{
				game.font.setColor(1, 1, 1, optionsFade);
				game.font.draw(game.batch, "back", 800, 350);
			}
			game.batch.end();
		}
	}
	
	
	// handles the setup of each button in the constructor
	public void buttonSetup() 
	{
		// button style setup
		buttonStyle = new TextButtonStyle();
		buttonStyleLarge = new TextButtonStyle();
		game.font.getData().setScale(1.2f);
		buttonStyle.font = game.font;
		buttonStyleLarge.font = game.fontLarge;
		
		
		//////////////////////////// main menu buttons \\\\\\\\\\\\\\\\\\\\\\\\\\\
		
		// begin button setup
		menuBeginButton = new TextButton("", buttonStyle);
		menuBeginButton.setPosition(200, 570);
		menuBeginButton.setSize(125, 35);
		menuBeginButton.addCaptureListener(new ClickListener() 
		{
			private boolean playing = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(mainMenu == true) 
				{
					selectSound.play(game.masterVolume);
					mainMenu = false;
					difficultyMenu = true;
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(mainMenu == true) 
				{
					if(!playing) 
					{
						hoverSound.play(game.masterVolume);
						playing = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				playing = false;
			}
		});
		
		
		// configuration button setup
		menuConfigButton = new TextButton("", buttonStyle);
		menuConfigButton.setPosition(200, 470);
		menuConfigButton.setSize(358, 35);
		menuConfigButton.addCaptureListener(new ClickListener() 
		{
			private boolean playing = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(mainMenu == true) 
				{
					selectSound.play(game.masterVolume);
					mainMenu = false;
					configMenu = true;
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(mainMenu == true) 
				{
					if(!playing) 
					{
						hoverSound.play(game.masterVolume);
						playing = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				playing = false;
			}
		});
		
		
		// exit button setup
		menuExitButton = new TextButton("", buttonStyle);
		menuExitButton.setPosition(200, 370);
		menuExitButton.setSize(87, 35);
		menuExitButton.addCaptureListener(new ClickListener() 
		{
			private boolean hoverPlaying = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(mainMenu == true) 
				{
					cancelSound.play(game.masterVolume);
					exitSequence = true;
					mainMenu = false;
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(mainMenu == true) 
				{
					if(!hoverPlaying) 
					{
						hoverSound.play(game.masterVolume);
						hoverPlaying = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				hoverPlaying = false;
			}
		});
		
		
		//////////////////////////// config menu buttons \\\\\\\\\\\\\\\\\\\\\\\\\\
		
		// volume up button setup
		configVolUpButton = new TextButton("", buttonStyle);
		configVolUpButton.setPosition(660, 490);
		configVolUpButton.setSize(28, 0);
		configVolUpButton.addCaptureListener(new ClickListener() 
		{
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(configMenu == true) 
				{
					if(game.masterVolume < 1f) 
					{
						game.masterVolume += 1/20f;
						hoverSound.play(game.masterVolume);
					}
					else 
					{
						negativeSound.play(1f);
					}
				}
			}
		});
		
		
		// volume down button setup
		configVolDownButton = new TextButton("", buttonStyle);
		configVolDownButton.setPosition(1200, 490);
		configVolDownButton.setSize(28, 0);
		configVolDownButton.addCaptureListener(new ClickListener() 
		{
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(configMenu == true) 
				{
					if(game.masterVolume > 0f) 
					{
						game.masterVolume -= 1/20f;
						hoverSound.play(game.masterVolume);
					}
					else if (game.masterVolume <= 0f)
					{
						game.masterVolume = 0f;
					}
					else 
					{
						negativeSound.play(1f);
					}
				}
			}
		});
		
		
		// fullscreen button setup
		configFullscreenButton = new TextButton("", buttonStyle);
		configFullscreenButton.setPosition(658, 575);
		configFullscreenButton.setSize(575, 30);
		configFullscreenButton.addCaptureListener(new ClickListener() 
		{
			boolean playing = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(configMenu == true) 
				{
					if(game.fullscreen == false) 
					{
						selectSound.play();
						game.fullscreen = true;
						Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
						viewport.update(1920, 1080);
					}
					else if (game.fullscreen == true) 
					{
						selectSound.play();
						game.fullscreen = false;
						Gdx.graphics.setWindowedMode(1920, 1080);
						viewport.update(1920, 1080);
					}
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(configMenu == true) 
				{
					if(!playing) 
					{
						hoverSound.play(game.masterVolume);
						playing = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				playing = false;
			}
		});
		
		
		// back button setup
		configBackButton = new TextButton("", buttonStyle);
		configBackButton.setPosition(1126, 370);
		configBackButton.setSize(108, 15);
		configBackButton.addCaptureListener(new ClickListener() 
		{
			boolean playing = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(configMenu == true) 
				{
					configMenu = false;
					mainMenu = true;
					cancelSound.play(game.masterVolume);
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(configMenu == true) 
				{
					if(!playing) 
					{
						hoverSound.play(game.masterVolume);
						playing = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				playing = false;
			}
		});
		
		
		///////////////////////// difficulty menu buttons \\\\\\\\\\\\\\\\\\\\\\\\\
		
		// Ashbound difficulty button
		difficultyAshboundButton = new TextButton("", buttonStyle);
		difficultyAshboundButton.setPosition(680, 470);
		difficultyAshboundButton.setSize(223, 25);
		difficultyAshboundButton.addCaptureListener(new ClickListener() 
		{
			boolean playing = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(difficultyMenu == true) 
				{
					beginSound.play(game.masterVolume);
					difficultyMenu = false;
					chosenDifficulty = true;
					beginSequence = true;
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(difficultyMenu == true) 
				{
					if(!playing) 
					{
						hoverSound.play(game.masterVolume);
						playing = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				playing = false;
			}
		});
		
		
		// Explorer difficulty button
		difficultyExplorerButton = new TextButton("", buttonStyle);
		difficultyExplorerButton.setPosition(715, 400);
		difficultyExplorerButton.setSize(189, 25);
		difficultyExplorerButton.addCaptureListener(new ClickListener() 
		{
			boolean playing = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(difficultyMenu == true) 
				{
					beginSound.play(game.masterVolume);
					difficultyMenu = false;
					chosenDifficulty = false;
					beginSequence = true;
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(difficultyMenu == true) 
				{
					if(!playing) 
					{
						hoverSound.play(game.masterVolume);
						playing = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				playing = false;
			}
		});
		
		
		// back button setup
		difficultyBackButton = new TextButton("", buttonStyle);
		difficultyBackButton.setPosition(800, 320);
		difficultyBackButton.setSize(108, 25);
		difficultyBackButton.addCaptureListener(new ClickListener() 
		{
			boolean playing = false;
			@Override
			public void clicked(InputEvent event, float x, float y) 
			{
				super.clicked(event, x, y);
				if(difficultyMenu == true) 
				{
					cancelSound.play(game.masterVolume);
					difficultyMenu = false;
					mainMenu = true;
				}
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) 
			{
				super.enter(event, x, y, pointer, fromActor);
				if(difficultyMenu == true) 
				{
					if(!playing) 
					{
						hoverSound.play(game.masterVolume);
						playing = true;
					}
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) 
			{
				super.enter(event, x, y, pointer, toActor);
				playing = false;
			}
		});
		
		
		/////////////////////////////// final setup \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		
		//add all buttons to the stage
		stage.addActor(menuBeginButton);
		stage.addActor(menuConfigButton);
		stage.addActor(menuExitButton);
		stage.addActor(configVolUpButton);
		stage.addActor(configVolDownButton);
		stage.addActor(configFullscreenButton);
		stage.addActor(configBackButton);
		stage.addActor(difficultyAshboundButton);
		stage.addActor(difficultyExplorerButton);
		stage.addActor(difficultyBackButton);
	}
	
	
	// begins the game closing process when the exit button is selected
	public void exitTitle(float delta) 
	{
		if(exitSequence == true) 
		{
			game.batch.begin();
			exitElapsed += delta;
			exitFade = Interpolation.fade.apply(2.0f - (exitElapsed));
			game.font.getData().setScale(3.5f);
			game.font.setColor(1, 1, 1, exitFade);
			game.font.draw(game.batch, "ASHBOUND", 200, 800);
			game.font.getData().setScale(1.2f);
			game.font.setColor(1, 1, 1, exitFade);
			game.font.draw(game.batch, "begin", 200, 600);
			game.font.draw(game.batch, "configuration", 200, 500);
			game.font.draw(game.batch, "exit", 200, 400);
			game.batch.end();
		}
		if(exitFade < -2.0f) 
		{
			this.dispose();
			Gdx.app.exit();
		}
	}
	
	
	// begins the game with the selected difficulty
	public void beginGame(float delta) 
	{
		if (beginSequence == true) 
		{
			game.batch.begin();
			beginElapsed += delta;
			difficultyFade = Interpolation.fade.apply(2.5f - (beginElapsed));
			optionsFade = Interpolation.fade.apply(3.5f - (beginElapsed));
			titleFade = Interpolation.fade.apply(5.0f - (beginElapsed));
			game.font.getData().setScale(3.5f);
			game.font.setColor(0.6f, 0.6f, 0.6f, titleFade);
			game.font.draw(game.batch, "ASHBOUND", 200, 800);
			game.font.getData().setScale(1.2f);
			game.font.setColor(1, 1, 1, optionsFade);
			game.font.draw(game.batch, "begin", 200, 600);
			game.font.draw(game.batch, "configuration", 200, 500);
			game.font.draw(game.batch, "exit", 200, 400);
			game.font.setColor(1, 1, 1, difficultyFade);
			game.font.draw(game.batch, "difficulty:\n------------", 657, 602);
			if(chosenDifficulty) 
			{
				game.font.setColor(0.8f, 0, 0, difficultyFade);
				game.font.draw(game.batch, "ASHBOUND", 680, 502);
				game.fontEasy.setColor(1, 1, 1, difficultyFade);
				game.fontEasy.draw(game.batch, "ASH STORMS ARE UNYIELDING, COMBAT "
						+ "\nIS DESPARATE, RESOURCES ARE PRECIOUS; "
						+ "\nDIFFICULT CHOICES WILL BE MADE, AND YOU "
						+ "\nWILL LEARN TO FEAR ENCOUNTERS WITH "
						+ "\nOTHERS ON THE FRONTIER. THIS IS THE"
						+ "\nWAY ASHBOUND IS MEANT TO BE PLAYED.", 1000, 552);
			}
			else if(!chosenDifficulty) 
			{
				game.font.setColor(0.8f, 0, 0, difficultyFade);
				game.font.draw(game.batch, "EXPLORER", 715, 432);
				game.fontEasy.setColor(1, 1, 1, difficultyFade);
				game.fontEasy.draw(game.batch, "THE WORLD IS A MORE FORGIVING PLACE; "
						+ "\nDIFFICULT CHOICES CAN BE AVOIDED WITH "
						+ "\nTHE RIGHT PLANNING, AND YOU WILL FIND "
						+ "\nTRIUMPH OVER ALL ENEMIES THROUGH ENOUGH "
						+ "\nEFFORT. FOR THOSE WHO SIMPLY WANT TO"
						+ "\nEXPERIENCE THE STORY OF ASHBOUND.", 1000, 552);
			}
			game.batch.end();
			
			if(beginElapsed >= 6.0f) 
			{
				game.setScreen(new GameWorld2D(game, chosenDifficulty));
				dispose();
			}
		}
	}
	
	
	@Override
	public void resize(int width, int height) 
	{
		viewport.update(width, height);
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void dispose() 
	{
		hoverSound.dispose();
		selectSound.dispose();
		beginSound.dispose();
		cancelSound.dispose();
		negativeSound.dispose();
		stage.dispose();
	}
}
