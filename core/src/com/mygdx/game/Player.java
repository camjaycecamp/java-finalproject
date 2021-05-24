package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/*
 * TODO:
 * 
 * - rework velocity (diagonal goes way too fast)
 * 
 * - implement hitbox
 * 
 * - implement collision
 */
public class Player extends Entity
{
	/*
	 * class-relevant fields, objects and variables
	 */
	final Master game;
	final World world;
	public boolean isDead;
	private TextureAtlas idleAtlas, attackOpenerAtlas;
	private Animation<TextureRegion> idleAnimation, attackOpenerAnimation;
	public Vector2 velocity, mouseInWorld, deathPosition;
	public float timePassed, maxSpeed, deceleration,
	attackCooldown, animationRotationOffset, damagedCooldown;
	// private floats related to animation properties
	private float idleToAOWidthRatio, idleToAOHeightRatio,
	idleWidth, idleHeight, AOWidth, AOHeight, 
	idleOffsetX, idleOffsetY, AOOffsetX, AOOffsetY;
	// private floats related to player object properties
	private float health, rotation, damage;
	public Sprite sprite;
	public StateMachine<Player, PlayerState> stateMachine;
	public BodyDef bodyDef;
	private PolygonShape bodyShape, weaponShape;
	public FixtureDef fixtureDef;
	public Body body;
	public Fixture bodyFixture, weaponFixture;
	
	
	/*
	 * constructor
	 */
	public Player(final Master game, final World world, boolean difficulty) 
	{
		this.game = game;
		this.world = world;
		isDead = false;
		timePassed = 0f;
		damagedCooldown = 0f;
		attackCooldown = 10f;
		idleAtlas = new TextureAtlas(Gdx.files.internal("Animations\\\\PlayerIdle\\\\PlayerIdle.atlas"));
		attackOpenerAtlas = new TextureAtlas(Gdx.files.internal("Animations\\PlayerAttackOpener\\AttackOpener.atlas"));
		idleAnimation = new Animation<TextureRegion>(1/60f, idleAtlas.getRegions());
		attackOpenerAnimation = new Animation<TextureRegion>(1/60f, attackOpenerAtlas.getRegions());
		sprite = new Sprite((idleAnimation.getKeyFrame(timePassed, true)), 0, 0,
				(idleAnimation.getKeyFrame(timePassed, true).getRegionWidth()), 
				(idleAnimation.getKeyFrame(timePassed, true).getRegionHeight()));
		velocity = new Vector2(0, 0);
		mouseInWorld = new Vector2(0, 0);
		deathPosition = new Vector2(0, 0);
		health = 100f;
		if (difficulty) damage = 35f;
		else damage = 50f;
		maxSpeed = 5f;
		deceleration = 0.27f;
		// these values allow the scaling process of animations to be streamlined. the less hardcoding, the better
		idleWidth = (float)idleAnimation.getKeyFrame(timePassed, true).getRegionWidth();
		AOWidth = (float)attackOpenerAnimation.getKeyFrame(timePassed, true).getRegionWidth();
		idleHeight = (float)idleAnimation.getKeyFrame(timePassed, true).getRegionHeight();
		AOHeight = (float)attackOpenerAnimation.getKeyFrame(timePassed, true).getRegionHeight();
		idleToAOWidthRatio = ((float)AOWidth/(float)idleWidth);
		idleToAOHeightRatio = ((float)AOHeight/(float)idleHeight);
		
		// due to the nature of the frame-based animation im stuck with, offsets for each frame
		// have to be hardcoded so that the position and rotation of each animation lines up and
		// is stable
		idleOffsetX = 37f;
		idleOffsetY = 62f;
		AOOffsetX = 88f;
		AOOffsetY = 63f;
		animationRotationOffset = 90f;
		
		stateMachine = new DefaultStateMachine<Player, PlayerState>(this, PlayerState.IDLE);
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(25.6f, 14.4f);
		bodyDef.angle = rotation;
		body = world.createBody(bodyDef);
		body.setLinearDamping(1f);
		body.setUserData(this);
		bodyShape = new PolygonShape();
		bodyShape.set(new float[] 
				{.36f, -.40f,
				.36f, .60f,
				-.64f, .60f, 
				-.64f, -.40f});
		fixtureDef = new FixtureDef();
		fixtureDef.shape = bodyShape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 1.0f;
		fixtureDef.restitution = 0.15f;
		fixtureDef.filter.categoryBits = game.CATEGORY_PLAYER;
		fixtureDef.filter.maskBits = game.CATEGORY_ZOMBIE;
		bodyFixture = body.createFixture(fixtureDef);
		bodyFixture.setUserData(this);
		
		weaponShape = new PolygonShape();
		weaponShape.set(new float[] 
				{-1.6f, -1.2f,
				-1.6f, 1.5f,
				-0.6f, 1.5f, 
				-0.6f, -1.2f});
		fixtureDef.shape = weaponShape;
		fixtureDef.isSensor = true;
		weaponFixture = body.createFixture(fixtureDef);
		weaponFixture.setUserData(this);
	}
	
	
	
	/*
	 * methods
	 */
	// render idle animation
	public void renderIdleAnimation() 
	{
		
		// idle is treated as the normal scale, so no special scaling variable is needed
		sprite.setScale(.02f);
		sprite.setRegion(idleAnimation.getKeyFrame(timePassed, true));
		sprite.setOrigin(
				(idleAnimation.getKeyFrame(timePassed, true).getRegionWidth())/(idleWidth/idleOffsetX), 
				(idleAnimation.getKeyFrame(timePassed, true).getRegionHeight())/(idleHeight/idleOffsetY));
		sprite.setPosition(body.getPosition().x-37, body.getPosition().y-62);
		rotation = mouseInWorld.angleDeg();
		body.setTransform(body.getPosition(), rotation/57.4f);
		sprite.setRotation(rotation + animationRotationOffset);
		sprite.draw(game.batch);
	}
	
	
	// render attack animation
	public void renderAttackAnimation() 
	{
		/*
		 * equations for animation hotswap offset:
		 * 
		 * W - (old X/sprite scale X) = animation hotswap rotation offset X
		 * 
		 * H - (old Y/sprite scale Y) = animation hotswap rotation offset Y
		 * 
		 * just adjust the position of the sprite once rotation is stable
		 */
		sprite.setScale(idleToAOWidthRatio/50, idleToAOHeightRatio/50);
		sprite.setOrigin(AOWidth-(AOWidth-(AOOffsetX/idleToAOWidthRatio)),
				AOHeight-(AOHeight-(AOOffsetY/idleToAOHeightRatio)));
		sprite.setRegion(attackOpenerAnimation.getKeyFrame(timePassed, false));
		sprite.setPosition(body.getPosition().x-65.8f, body.getPosition().y-46.225f); // these also have to be hardcoded
		rotation = mouseInWorld.angleDeg();
		body.setTransform(body.getPosition(), rotation/57.4f);
		sprite.setRotation(rotation + animationRotationOffset);
		sprite.draw(game.batch);
	}
	
	
	// render player character controls
	public void renderControls() 
	{	
		// left click attack if-block with cooldown for attack
		if(Gdx.input.isButtonJustPressed(Buttons.LEFT) && attackCooldown > 1.6f)
		{
			timePassed = 0f;
			attackCooldown = 0f;
			stateMachine.changeState(PlayerState.ATTACK);
		}
		
		// deceleration if-blocks with stabilizers
		if(velocity.x > 0 && !(Gdx.input.isKeyPressed(Keys.D))) 
		{
			if(velocity.x < 1) 
			{
				velocity.x = 0;
			}
			else 
			{
				velocity.x -= (deceleration);
			}
		}
		else if (velocity.x < 0 && !(Gdx.input.isKeyPressed(Keys.A))) 
		{
			if(velocity.x > -1) 
			{
				velocity.x = 0;
			}
			else 
			{
				velocity.x += (deceleration);
			}
		}
		if(velocity.y > 0 && !(Gdx.input.isKeyPressed(Keys.W))) 
		{
			if(velocity.y < 1) 
			{
				velocity.y = 0;
			}
			else 
			{
				velocity.y -= (deceleration);
			}
		}
		else if (velocity.y < 0 && !(Gdx.input.isKeyPressed(Keys.S))) 
		{
			if(velocity.y > -1) 
			{
				velocity.y = 0;
			}
			else 
			{
				velocity.y += (deceleration);
			}
		}
		if(body.getAngularVelocity() != 0) 
		{
			body.setAngularVelocity(0);
		}
		
		// acceleration if-blocks that also limit diagonal speed
		if(Gdx.input.isKeyPressed(Keys.W) && velocity.y < maxSpeed) 
		{
			if(Gdx.input.isKeyPressed(Keys.A) && velocity.x > -maxSpeed) 
			{
				velocity.y += 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x -= 2.236f * Gdx.graphics.getDeltaTime();
			}
			else if(Gdx.input.isKeyPressed(Keys.D) && velocity.x < maxSpeed) 
			{
				velocity.y += 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x += 2.236f * Gdx.graphics.getDeltaTime();
			}
			else 
			{
				velocity.y += 10 * Gdx.graphics.getDeltaTime();
			}
		}
		else if(Gdx.input.isKeyPressed(Keys.S) && velocity.y > -maxSpeed) 
		{
			if(Gdx.input.isKeyPressed(Keys.A) && velocity.x > -maxSpeed) 
			{
				velocity.y -= 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x -= 2.236f * Gdx.graphics.getDeltaTime();
			}
			else if(Gdx.input.isKeyPressed(Keys.D) && velocity.x < maxSpeed) 
			{
				velocity.y -= 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x += 2.236f * Gdx.graphics.getDeltaTime();
			}
			else 
			{
				velocity.y -= 10 * Gdx.graphics.getDeltaTime();
			}
		}
		else if(Gdx.input.isKeyPressed(Keys.A) && velocity.x > -maxSpeed) 
		{
			if(Gdx.input.isKeyPressed(Keys.W) && velocity.y < maxSpeed) 
			{
				velocity.y += 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x -= 2.236f * Gdx.graphics.getDeltaTime();
			}
			else if(Gdx.input.isKeyPressed(Keys.S) && velocity.y > -maxSpeed) 
			{
				velocity.y -= 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x -= 2.236f * Gdx.graphics.getDeltaTime();
			}
			else 
			{
				velocity.x -= 10 * Gdx.graphics.getDeltaTime();
			}
		}
		else if(Gdx.input.isKeyPressed(Keys.D) && velocity.x < maxSpeed) 
		{
			if(Gdx.input.isKeyPressed(Keys.W) && velocity.y < maxSpeed) 
			{
				velocity.y += 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x += 2.236f * Gdx.graphics.getDeltaTime();
			}
			else if(Gdx.input.isKeyPressed(Keys.S) && velocity.y > -maxSpeed) 
			{
				velocity.y -= 2.236f * Gdx.graphics.getDeltaTime();
				velocity.x += 2.236f * Gdx.graphics.getDeltaTime();
			}
			else 
			{
				velocity.x += 10 * Gdx.graphics.getDeltaTime();
			}
		}
		
		// update player position for the next render loop
		body.setLinearVelocity(velocity);
		
		// if-else blocks to keep player within map bounds, adjusted for inconsistencies
		if(body.getPosition().x < 0.4f) 
		{
			body.setTransform(0.4f, body.getPosition().y, rotation);
		}
		else if(body.getPosition().x > 80.4f) 
		{
			body.setTransform(80.4f, body.getPosition().y, rotation);
		}
		if(body.getPosition().y < 0.4f) 
		{
			body.setTransform(body.getPosition().x, 0.4f, rotation);
		}
		else if(body.getPosition().y > 80.4f) 
		{
			body.setTransform(body.getPosition().x, 80.4f, rotation);
		}
	}
	
	
	
	/*
	 *  methods for the state machine's states, allowing the player to switch between different
	 *  functions
	 */
	// update method for player object's state machine and its relevant objects and variables
	public void update(float delta) 
	{
		if(isDead) return;
		// increment attack cooldown
		attackCooldown += Gdx.graphics.getDeltaTime();
		renderControls();
		// render animation dependent on the state the player is in
		if(stateMachine.isInState(PlayerState.IDLE)) 
		{
			renderIdleAnimation();
		}
		else if(stateMachine.isInState(PlayerState.ATTACK))
		{
			renderAttackAnimation();
		}
		damagedCooldown += Gdx.graphics.getDeltaTime();
		stateMachine.update();
	}
	
	// method to act idly, whether still or moving
	public void idle() 
	{
		
	}
	
	// method to use the player's attacks
	public void attack() 
	{
		
	}
	
	// method to die when enough damage has been sustained
	public void die() 
	{
		isDead = true;
	}
	
	
	
	/*
	 * getters and setters for every variable and final dispose method
	 */
	public TextureAtlas getIdleAtlas() {return idleAtlas;}

	public void setIdleAtlas(TextureAtlas idleAtlas) {this.idleAtlas = idleAtlas;}

	public TextureAtlas getAttackOpenerAtlas() {return attackOpenerAtlas;}

	public void setAttackOpenerAtlas(TextureAtlas attackOpenerAtlas) {this.attackOpenerAtlas = attackOpenerAtlas;}

	public Animation<TextureRegion> getIdleAnimation() {return idleAnimation;}

	public void setIdleAnimation(Animation<TextureRegion> idleAnimation) {this.idleAnimation = idleAnimation;}

	public Animation<TextureRegion> getAttackOpenerAnimation() {return attackOpenerAnimation;}

	public void setAttackOpenerAnimation(Animation<TextureRegion> attackOpenerAnimation) {this.attackOpenerAnimation = attackOpenerAnimation;}

	public float getMaxSpeed() {return maxSpeed;}

	public void setMaxSpeed(float maxSpeed) {this.maxSpeed = maxSpeed;}

	public float getDeceleration() {return deceleration;}

	public void setDeceleration(float deceleration) {this.deceleration = deceleration;}

	public float getHealth() {return health;}

	public void setHealth(float health) {this.health = health;}

	public float getDamage() {return damage;}

	public void setDamage(float damage) {this.damage = damage;}

	public float getRotation() {return rotation;}

	public void setRotation(float rotation) {this.rotation = rotation;}
	
	// method to dispose of all class stuff when the program is done running
	public void dispose() 
	{
		bodyShape.dispose();
		weaponShape.dispose();
		idleAtlas.dispose();
		attackOpenerAtlas.dispose();
		bodyShape.dispose();
		weaponShape.dispose();
	}
}
