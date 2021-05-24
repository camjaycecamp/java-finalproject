package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;

/*
 * master class for the core of the game
 */
public class Master extends Game
{
	final short CATEGORY_ZOMBIE = 0x0001;
	final short CATEGORY_PLAYER = 0x0002;
	final short CATEGORY_GROUND = 0x0004;
	
	final float GAME_WORLD_WIDTH = 81.92f;
	final float GAME_WORLD_HEIGHT = 81.92f;
	SpriteBatch batch;
	BitmapFont font, fontLarge, fontEasy, fontHuge;
	float masterVolume = 1f;
	boolean fullscreen = false;
	int waveZombiesDead, totalZombiesDead;
	
	public void create() 
	{
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("Fonts\\Vastantonius Standard\\Vastantonius.fnt"));
		fontLarge = new BitmapFont(Gdx.files.internal("Fonts\\Vastantonius Standard\\Vastantonius.fnt"));
		fontEasy = new BitmapFont(Gdx.files.internal("Fonts\\Vastantonius Easy\\VastantoniusEasy.fnt"));
		fontHuge = new BitmapFont(Gdx.files.internal("Fonts\\Vastantonius Huge\\VastantoniusHuge.fnt"));
		fontLarge.getData().setScale(2.0f);
		fontEasy.getData().setScale(1.0f);
		waveZombiesDead = 0;
		totalZombiesDead = 0;
		this.setScreen(new TitleScreen(this));
	}
	
	// is called every second
	public void render() 
	{
		super.render();
	}
	
	public void dispose() 
	{
		batch.dispose();
		font.dispose();
		fontLarge.dispose();
	}
}
