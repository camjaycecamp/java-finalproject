package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;

/*
 * TODO:
 * 
 *  - rewrite pathing algorithm for zombie wander state
 *  
 *  - write a rotation speed algorithm so zombie doesn't just instantly rotate
 *  
 *  - adjust speed values for zombie wander state to prevent zoombie
 *  
 *  - implement collision box to prevent zombie from going inside player
 *  
 *  - improve deceleration algorithm to reduce weird slide-y stops
 *  
 *  prioritize restructuring player class and implementing player hitbox 
 *  before doing any of this
 */
public class Zombie extends Entity
{
	/*
	 * class-relevant fields, objects and variables
	 */
	final Master game;
	final World world;
	public boolean  wanderOnCooldown, hitPlayer, hitByPlayer, isDead;
	private TextureAtlas idleAtlas, attackOpenerAtlas;
	private Animation<TextureRegion> idleAnimation, attackOpenerAnimation;
	public Vector2 velocity, playerPosition, deceleration,
	playerDirection, playerDistance, wanderDirection,
	wanderPosition;
	public float maxSpeed, timePassed, wanderTimer, 
	randomWanderCooldown, randomWanderDuration, damagedCooldown;
	// private floats related to animation properties
	private float idleToAOWidthRatio, idleToAOHeightRatio,
	idleWidth, idleHeight, AOWidth, AOHeight, 
	idleOffsetX, idleOffsetY, AOOffsetX, AOOffsetY,
	animationRotationOffset;
	// private floats related to zombie stat properties
	private float health, damage;
	// private floats related to zombie object properties
	private float rotation, detectionRadius, attackRadius, 
	playerAngle, pursueSpeed, wanderSpeed, attackRotation;
	public Sprite sprite;
	public StateMachine<Zombie, ZombieState> stateMachine;
	public BodyDef bodyDef;
	private PolygonShape shape;
	public FixtureDef torsoFixtureDef, armsFixtureDef;
	public Body body;
	public Fixture torsoFixture, armsFixture;
	
	
	/*
	 * constructor
	 */
	public Zombie(final Master game, final World world, boolean difficulty) 
	{
		this.game = game;
		this.world = world;
		game.batch = new SpriteBatch();
		wanderOnCooldown = true;
		hitPlayer = false;
		hitByPlayer = false;
		isDead = false;
		health = 100f;
		if(difficulty) damage = 40f;
		else damage = 25f;
		timePassed = 0f;
		wanderTimer = 0f;
		damagedCooldown = 0f;
		randomWanderCooldown = (MathUtils.random() * (3f) + 4f);
		randomWanderDuration = randomWanderCooldown * (MathUtils.random() * (0.5f) + 0.25f);
		idleAtlas = new TextureAtlas(Gdx.files.internal("Animations\\ZombieIdle\\ZombieIdle.atlas"));
		attackOpenerAtlas = new TextureAtlas(Gdx.files.internal("Animations\\ZombieAttackOpener\\ZombieAttackOpener.atlas"));
		idleAnimation = new Animation<TextureRegion>(1/60f, idleAtlas.getRegions());
		attackOpenerAnimation = new Animation<TextureRegion>(1/60f, attackOpenerAtlas.getRegions());
		sprite = new Sprite((idleAnimation.getKeyFrame(timePassed, true)), 0, 0,
				(idleAnimation.getKeyFrame(timePassed, true).getRegionWidth()), 
				(idleAnimation.getKeyFrame(timePassed, true).getRegionHeight()));
		velocity = new Vector2(0, 0);
		wanderDirection = new Vector2();
		playerDirection = new Vector2(0, 0);
		rotation = 0f;
		pursueSpeed = 3.85f;
		wanderSpeed = 2f;
		maxSpeed = 1.2f;
		playerAngle = 0f;
		deceleration = new Vector2(.2f, .2f);
		detectionRadius = 14f;
		attackRadius = 4f;
		// these values allow the scaling process of animations to be streamlined. the less hardcoding, the better
		idleWidth = (float)idleAnimation.getKeyFrame(timePassed, true).getRegionWidth();
		AOWidth = (float)attackOpenerAnimation.getKeyFrame(timePassed, true).getRegionWidth();
		idleHeight = (float)idleAnimation.getKeyFrame(timePassed, true).getRegionHeight();
		AOHeight = (float)attackOpenerAnimation.getKeyFrame(timePassed, true).getRegionHeight();
		idleToAOWidthRatio = ((float)AOWidth/(float)idleWidth);
		idleToAOHeightRatio = ((float)AOHeight/(float)idleHeight);
		animationRotationOffset = 90f;
		
		// due to the nature of the frame-based animation im stuck with, offsets for each frame
		// have to be hardcoded so that the position and rotation of each animation lines up and
		// is stable
		idleOffsetX = 34f;
		idleOffsetY = 60f;
		AOOffsetX = 52f;
		AOOffsetY = 75f;
		
		stateMachine = new DefaultStateMachine<Zombie, ZombieState>(this, ZombieState.WANDER);
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(MathUtils.random()*(50f), MathUtils.random()*(50f));
		bodyDef.angle = rotation;
		body = world.createBody(bodyDef);
		body.setUserData(this);
		body.setLinearDamping(1f);
		shape = new PolygonShape();
		shape.set(new float[]
				{-0.54f, -0.54f, 
				0.44f, -0.54f,
				0.44f, 0.54f,
				-0.54f, 0.54f});
		torsoFixtureDef = new FixtureDef();
		torsoFixtureDef.shape = shape;
		torsoFixtureDef.density = 1.0f;
		torsoFixtureDef.friction = 1.0f;
		torsoFixtureDef.restitution = 0.05f;
		torsoFixtureDef.filter.categoryBits = game.CATEGORY_ZOMBIE; // is a 
		torsoFixtureDef.filter.maskBits = (short)(game.CATEGORY_PLAYER | game.CATEGORY_ZOMBIE); // collides with
		torsoFixture = body.createFixture(torsoFixtureDef);
		
		shape.set(new float[]
				{0.44f, -0.54f, 
				1.34f, -0.54f,
				1.34f, 0.54f,
				0.44f, 0.54f});
		armsFixtureDef = new FixtureDef();
		armsFixtureDef.shape = shape;
		armsFixtureDef.density = 1.0f;
		armsFixtureDef.friction = 1.0f;
		armsFixtureDef.restitution = 0.15f;
		armsFixtureDef.isSensor = true;
		armsFixtureDef.filter.categoryBits = game.CATEGORY_ZOMBIE;
		armsFixtureDef.filter.maskBits = game.CATEGORY_PLAYER;
		armsFixture = body.createFixture(armsFixtureDef);
		
		torsoFixture.setUserData(this);
		armsFixture.setUserData(this);
		
		wanderPosition = new Vector2(body.getPosition().x + (MathUtils.random()*(21f)-10f), 
				(body.getPosition().y + MathUtils.random()*(21f)-10f));
	}
	
	
	
	/*
	 * render methods
	 */
	// render idle animation (wandering or pursuing)
	public void renderIdleAnimation() 
	{	
		// idle is treated as the normal scale, so no special scaling variable is needed
		sprite.setScale(0.02f);
		sprite.setRegion(idleAnimation.getKeyFrame(timePassed, true));
		sprite.setOrigin(
				(idleAnimation.getKeyFrame(timePassed, true).getRegionWidth())/(idleWidth/idleOffsetX), 
				(idleAnimation.getKeyFrame(timePassed, true).getRegionHeight())/(idleHeight/idleOffsetY));
		sprite.setPosition(body.getPosition().x-34, body.getPosition().y-60);
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
		sprite.setPosition(body.getPosition().x-34.35f, body.getPosition().y-63.15f); // these also have to be hardcoded
		body.setTransform(body.getPosition(), rotation/57.4f);
		sprite.setRotation(attackRotation + animationRotationOffset);
		sprite.draw(game.batch);
	}
	
	
	
	/*
	 * methods for the state machine's states, allowing the zombie to function as an AI
	 */
	// update method for zombie object's state machine and its relevant objects and variables
	public void update(float delta) 
	{
		if (isDead) return;
		if (stateMachine.isInState(ZombieState.WANDER) 
				|| stateMachine.isInState(ZombieState.PURSUE)) 
		{
			renderIdleAnimation();
		}
		else if (stateMachine.isInState(ZombieState.ATTACK)) 
		{
			renderAttackAnimation();
		}
		damagedCooldown += Gdx.graphics.getDeltaTime();
		stateMachine.update();
		body.setLinearVelocity(velocity);
		correctPosition();
	}
	
	// method to check if zombie can detect player
	public boolean detectsPlayer() 
	{
		if (playerPosition.dst(body.getPosition()) < detectionRadius)
		{
			return true;
		}
		else 
		{
			return false;
		}
	}
	
	// method to check if zombie is within attack range
	public boolean inAttackRange() 
	{
		if (playerPosition.dst(body.getPosition()) < attackRadius)
		{
			return true;
		}
		else 
		{
			return false;
		}
	}
	
	// method to wander aimlessly
	public void wander() 
	{
		wanderTimer += Gdx.graphics.getDeltaTime();
		
		// stabilize velocity when not wandering
		if (wanderOnCooldown) 
		{
			stabilizeVelocityLength(0.2f);
		}
		
		// if enough time has passed for the zombie to randomly wander again
		if (wanderTimer >= randomWanderCooldown)
		{
			wanderOnCooldown = false;
			// if the zombie isn't within the deadzone of its wander destination
			if (!(body.getPosition().x < wanderPosition.x+0.2f
					&& body.getPosition().x > wanderPosition.x-0.2f)
					&& !(body.getPosition().y < wanderPosition.y+0.2f
					&& body.getPosition().y > wanderPosition.y-0.2f)) 
			{
				wanderDirection.set(wanderPosition.x - body.getPosition().x,
						wanderPosition.y - body.getPosition().y);
				wanderDirection.nor();
				rotation = MathUtils.atan2(wanderDirection.y, wanderDirection.x) 
						* MathUtils.radiansToDegrees;
				if(velocity.x < maxSpeed 
						&& velocity.y < maxSpeed) 
				{
					velocity = wanderDirection.setLength(wanderSpeed);
				}
			}
			// otherwise, let the zombie slow down until it's at zero velocity, then reset timers
			// once stabilized
			else
			{
				if (velocity.x != 0 || velocity.y != 0) 
				{
					stabilizeVelocityLength(0.2f);
				}
				else 
				{
					wanderTimer = 0f;
					wanderPosition.set(body.getPosition().x + (MathUtils.random()*(21f)-10f), 
							(body.getPosition().y + MathUtils.random()*(21f)-10f));
					randomWanderCooldown = (MathUtils.random() * (3f) + 4f);
				}
			}
		}
		else 
		{
			wanderOnCooldown = true;
		}
		/*System.out.println("Wander Direction: " + wanderDirection + " " 
		+ "\nWander Position: " + wanderPosition
		+ "\nVelocity: " + velocity + "\n" +
		(!(body.getPosition().x < wanderPosition.x+0.2f
				&& body.getPosition().x > wanderPosition.x-0.2f)
				&& !(body.getPosition().y < wanderPosition.y+0.2f
				&& body.getPosition().y > wanderPosition.y-0.2f)) 
		+ "\nZombie Position: " + body.getPosition()
		+ "\nZombie Rotation: " + rotation + "\n");*/
	}
	
	// method to pursue the player
	public void pursue() 
	{
		// rotate zombie towards player
		playerDirection.set(playerPosition.x - body.getPosition().x, 
				playerPosition.y - body.getPosition().y);
		playerDirection.nor();
		playerAngle = MathUtils.atan2(playerDirection.y, playerDirection.x) * MathUtils.radiansToDegrees;
		rotation = playerAngle;
		if(velocity.x < maxSpeed 
				&& velocity.y < maxSpeed) 
		{
			velocity = playerDirection.setLength(pursueSpeed);
		}
		/*System.out.println("Wander Direction: " + playerDirection + " " 
				+ "\nWander Position: " + playerPosition
				+ "\nVelocity: " + velocity
				+ "\nZombie Position: " + body.getPosition()
				+ "\nZombie Rotation: " + rotation + "\n");*/
	}
	
	// method to attack player
	public void attack() 
	{
		/*
		 * once in attack range, begin attack animation and stabilize velocity. don't switch to another
		 * state until the animation is finished, even if the player leaves the attack range. once the attack
		 * animation is finished, return to pursue until the player enters the attack range again.
		 */
		stabilizeVelocity(0.2f);
		renderAttackAnimation();
	}
	
	// method to die when enough damage has been sustained
	public void die() 
	{
		isDead = true;
	}
	
	// method to correct the position of the zombie should it travel outside the bounds of the map
	private void correctPosition() 
	{
		if (body.getPosition().x < 0.4f) 
		{
			body.setTransform(0.4f, body.getPosition().y, rotation);
		}
		else if (body.getPosition().x > 80.4f) 
		{
			body.setTransform(80.4f, body.getPosition().y, rotation);
		}
		if (body.getPosition().y < 0.4f) 
		{
			body.setTransform(body.getPosition().x, 0.4f, rotation);
		}
		else if (body.getPosition().y > 80.4f) 
		{
			body.setTransform(body.getPosition().x, 80.4f, rotation);
		}
	}
	
	// method to stabilize velocity to 0 when the zombie when it's not moving
	// decelerationScale affects the speed of deceleration, useful for making the
	// zombie appear as if it's stumbling or lunging
	private void stabilizeVelocityLength(float decelerationScale)
	{
		
		if(velocity.x == 0 && velocity.y == 0 && body.getAngularVelocity() != 0) 
		{
			body.setAngularVelocity(0);
		}
		if(velocity.x > 0 && velocity.x < 0.1f) 
		{
			velocity.x = 0f;
		}
		else if(velocity.x < 0 && velocity.x > -0.1f) 
		{
			velocity.x = 0f;
		}
		if(velocity.y > 0 && velocity.y < 0.1f) 
		{
			velocity.y = 0f;
		}
		else if(velocity.y < 0 && velocity.y > -0.1f) 
		{
			velocity.y = 0f;
		}
		velocity.setLength(velocity.len()*decelerationScale);
	}
	
	// method to stabilize velocity to 0 when the zombie when it's not moving
	// decelerationScale affects the speed of deceleration, useful for making the
	// zombie appear as if it's stumbling or lunging
	private void stabilizeVelocity(float decelerationScale)
	{
		
		if(body.getAngularVelocity() != 0) 
		{
			body.setAngularVelocity(0);
		}
		if(velocity.x > 0 && velocity.x < 0.1f) 
		{
			velocity.x = 0f;
		}
		else if(velocity.x < 0 && velocity.x > -0.1f) 
		{
			velocity.x = 0f;
		}
		if(velocity.y > 0 && velocity.y < 0.1f) 
		{
			velocity.y = 0f;
		}
		else if(velocity.y < 0 && velocity.y > -0.1f) 
		{
			velocity.y = 0f;
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
				velocity.x -= (deceleration.x * decelerationScale);
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
				velocity.x += (deceleration.x * decelerationScale);
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
				velocity.y -= (deceleration.y * decelerationScale);
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
				velocity.y += (deceleration.y * decelerationScale);
			}
		}
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

	public float getHealth() {return health;}

	public void setHealth(float health) {this.health = health;}

	public float getRotation() {return rotation;}

	public void setRotation(float rotation) {this.rotation = rotation;}

	public float getAttackRotation() {return attackRotation;}

	public void setAttackRotation(float attackRotation) {this.attackRotation = attackRotation;}

	public float getDamage() {return damage;}

	public void setDamage(float damage) {this.damage = damage;}

	public boolean isHitPlayer() {return hitPlayer;}

	public void setHitPlayer(boolean hitPlayer) {this.hitPlayer = hitPlayer;}

	public boolean isHitByPlayer() {return hitByPlayer;}

	public void setHitByPlayer(boolean hitByPlayer) {this.hitByPlayer = hitByPlayer;}

	public float getDetectionRadius() {return detectionRadius;}

	public void setDetectionRadius(float detectionRadius) {this.detectionRadius = detectionRadius;}

	public float getAttackRadius() {return attackRadius;}

	public void setAttackRadius(float attackRadius) {this.attackRadius = attackRadius;}
	
	// method to dispose of all class stuff when the program is done running
	public void dispose() 
	{
		shape.dispose();
		idleAtlas.dispose();
		attackOpenerAtlas.dispose();
	}
}
