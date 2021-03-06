package com.mygdx.pirategame.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.pirategame.entities.CannonFire;
import com.mygdx.pirategame.hud.Hud;
import com.mygdx.pirategame.main.PirateGame;
import com.mygdx.pirategame.screens.GameScreen;

/**
 * Creates the class of the player. Everything that involves actions coming from the player boat
 * @author Ethan Alabaster, Edward Poulter, Zac Spooner
 * @version 1.0
 */
public class Player extends Sprite {
    private final GameScreen screen;
    private Texture ship;
    private Texture ghostShip;
    public World world;
    public Body b2body;
    private Sound breakSound;
    private Array<CannonFire> cannonBalls;
    private static float dragFactor = 1.0f;
    private static float maxSpeed = 5.0f + dragFactor;
    private static float accel = 0.08f;
    private static float accelMul = 1f; //Team 17 - percentage multiplier for acceleration
    private float angle;
    private Vector2 astralPos;
    public static boolean astral;
    public static boolean rubber;
    public static boolean soup;

    /**
     * Instantiates a new Player. Constructor only called once per game
     *
     * @param screen visual data
     */
    public Player(GameScreen screen) {
        // Retrieves world data and creates ship texture
        this.screen = screen;
        ship = new Texture("ships&colleges/player_ship.png");
        ghostShip = new Texture("ships&colleges/player_ship_ghost.png"); //Team 17 - for astral body func
        this.world = screen.getWorld();

        // Defines a player, and the players position on screen and world
        definePlayer();
        setBounds(0,0,64 / PirateGame.PPM, 110 / PirateGame.PPM);
        setRegion(ship);
        setOrigin(32 / PirateGame.PPM,55 / PirateGame.PPM);

        // Sound effect for damage
        breakSound = Gdx.audio.newSound(Gdx.files.internal("sounds/wood-bump.mp3"));

        // Sets cannonball array
        setCannonBalls(new Array<CannonFire>());
    }

    /**
     * Update the position of the player. Also updates any cannon balls the player generates
     *
     * @param dt Delta Time
     */
    public void update(float dt) {
        // Updates position and orientation of player
        setPosition(b2body.getPosition().x - getWidth() / 2f, b2body.getPosition().y - getHeight() / 2f);
        //float angle = (float) Math.atan2(b2body.getLinearVelocity().y, b2body.getLinearVelocity().x);
        b2body.setTransform(b2body.getWorldCenter(), angle - ((float)Math.PI) / 2.0f);
        setRotation((float) (b2body.getAngle() * 180 / Math.PI));

        // Updates cannonball data
        for(CannonFire ball : getCannonBalls()) {
            ball.update(dt);
            if(ball.isDestroyed())
                getCannonBalls().removeValue(ball, true);
        }
    }

    /**
     * Plays the break sound when a boat takes damage
     */
    public void playBreakSound() {
        // Plays damage sound effect
        if (screen.game.getPreferences().isEffectsEnabled()) {
            breakSound.play(screen.game.getPreferences().getEffectsVolume());
        }
    }

    /**
     * Defines all the parts of the player's physical model. Sets it up for collisons
     */
    private void definePlayer() {
        // Defines a players position
        BodyDef bdef = new BodyDef();
        bdef.position.set(1200  / PirateGame.PPM, 2500 / PirateGame.PPM); // Default Pos: 1800,2500
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        // Defines a player's shape and contact borders
        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(55 / PirateGame.PPM);

        // setting BIT identifier
        fdef.filter.categoryBits = PirateGame.PLAYER_BIT;

        // determining what this BIT can collide with
        fdef.filter.maskBits = PirateGame.DEFAULT_BIT | PirateGame.COIN_BIT | PirateGame.ENEMY_BIT | PirateGame.COLLEGE_BIT | PirateGame.COLLEGESENSOR_BIT | PirateGame.COLLEGEFIRE_BIT | PirateGame.POWERUP_BIT;
        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
    }
    

    /**
     * Called when E is pushed. Causes 1 cannon ball to spawn on both sides of the ships with their relative velocity
     */
    public void fire() {
        // Fires cannons
        getCannonBalls().add(new CannonFire(screen, b2body.getPosition().x, b2body.getPosition().y, b2body, 5));
        getCannonBalls().add(new CannonFire(screen, b2body.getPosition().x, b2body.getPosition().y, b2body, -5));

        // Cone fire below
        /*cannonBalls.add(new CannonFire(screen, b2body.getPosition().x, b2body.getPosition().y, (float) (b2body.getAngle() - Math.PI / 6), -5, b2body.getLinearVelocity()));
        cannonBalls.add(new CannonFire(screen, b2body.getPosition().x, b2body.getPosition().y, (float) (b2body.getAngle() - Math.PI / 6), 5, b2body.getLinearVelocity()));
        cannonBalls.add(new CannonFire(screen, b2body.getPosition().x, b2body.getPosition().y, (float) (b2body.getAngle() + Math.PI / 6), -5, b2body.getLinearVelocity()));
        cannonBalls.add(new CannonFire(screen, b2body.getPosition().x, b2body.getPosition().y, (float) (b2body.getAngle() + Math.PI / 6), 5, b2body.getLinearVelocity()));
        }
         */
    }
    
    //-------Team-17--------
    /**
     * Applies a force to player b2body according to x and y components.
     * May result in faster speeds diagonally than orthogonally as drag works on scalars but we have to use velocity components
     * @param x The x component of the force should be -1,0 or 1 but can be any integer
     * @param y The y component of the force should be -1,0 or 1 but can be any integer
     */
    public void applyImpuse(int x,int y) {
    	
    	//finds current x and y components
    	float linx = b2body.getLinearVelocity().x;
    	float liny = b2body.getLinearVelocity().y;
    	
    	//finds pre-drag velocity for x and y components
    	linx += (x * accel*accelMul);
    	liny += (y * accel*accelMul);
    	
    	//applies drag velocity penalty
    	linx = calcDrag(linx);
    	liny = calcDrag(liny);
    	
    	b2body.setLinearVelocity(linx, liny);
    	
    	if (x == 0 && y == 0) {
    		//doesn't calculate a new angle so the ship stays pointing in the same direction when slowing down
    		//technically not pointing in the same direction as movement but speeds and difference in angle are so small no-one can tell
    	}
    	else {
    		//calculates the angle that the ship is pointing in
    		angle = (float) Math.atan2(b2body.getLinearVelocity().y, b2body.getLinearVelocity().x);
    	}
    }
    
    /**
     * Calculates the speed after drag has been applied to it.
     * @param speed The pre-drag speed
     * @return The new speed (should be lower the original or 0)
     */
    private float calcDrag(float speed) {
    	//fairly simple drag simulation by getting a proportion of max speed but with correct proportionality
    	double drag = (Math.pow(speed, 2) / Math.pow(maxSpeed, 3));
    	//adds dragFactor constant so it still slows down at low speeds 
    	drag = drag * dragFactor + dragFactor/1001;
    	//stops weird movement caused by constant dragFactor by flooring speed
    	if (drag < dragFactor/1000 && drag > -dragFactor/1000) {
    		speed = 0;
    		drag = 0;
    	}
    	//takes magnitude away from negative speeds rather than adding it
    	if (speed > 0) {
    		return (float) (speed - drag);
    	}
    	else {
    		return (float) (speed + drag);
    	}
    }
    
    /**
     * toggles on the astral body power by making a new fixture without collisions
     */
    public void turnOnAstral() {
    	if (!astral) {
    		astral = true;
	    	astralPos = b2body.getPosition();
	    	Vector2 vel = b2body.getLinearVelocity();
	    	
	    	Array<Fixture> fix = b2body.getFixtureList();
	    	b2body.destroyFixture(fix.first());

	    	// Defines a players position
	        BodyDef bdef = new BodyDef();
	        bdef.position.set(astralPos.x, astralPos.y);
	        bdef.type = BodyDef.BodyType.DynamicBody;
	        b2body = world.createBody(bdef);
	
	        // Defines a player's shape and contact borders
	        FixtureDef fdef = new FixtureDef();
	        CircleShape shape = new CircleShape();
	        shape.setRadius(55 / PirateGame.PPM);
	
	        // setting BIT identifier
	        fdef.filter.categoryBits = PirateGame.PLAYER_BIT;
	
	        // determining what this BIT can collide with
	        fdef.filter.maskBits = PirateGame.COIN_BIT	| PirateGame.POWERUP_BIT;
	        fdef.shape = shape;
	        b2body.createFixture(fdef).setUserData(this);
	        b2body.setLinearVelocity(vel);

            //Change texture of ship
            setRegion(ghostShip);
    	}
    }
    
    /**
     * toggles off the astral body powerup returning the player to original position
     */
    public void turnOffAstral() {
    	if (astral) {
    		astral = false;
        	Vector2 vel = b2body.getLinearVelocity();
        	
	    	Array<Fixture> fix = b2body.getFixtureList();
	    	b2body.destroyFixture(fix.first());
        	
        	// Defines a players position
            BodyDef bdef = new BodyDef();
            bdef.position.set(astralPos.x , astralPos.y);
            bdef.type = BodyDef.BodyType.DynamicBody;
            b2body = world.createBody(bdef);

            // Defines a player's shape and contact borders
            FixtureDef fdef = new FixtureDef();
            CircleShape shape = new CircleShape();
            shape.setRadius(55 / PirateGame.PPM);

            // setting BIT identifier
            fdef.filter.categoryBits = PirateGame.PLAYER_BIT;

            // determining what this BIT can collide with
            fdef.filter.maskBits = PirateGame.DEFAULT_BIT | PirateGame.COIN_BIT | PirateGame.ENEMY_BIT | PirateGame.COLLEGE_BIT | PirateGame.COLLEGESENSOR_BIT | PirateGame.COLLEGEFIRE_BIT | PirateGame.POWERUP_BIT;
            fdef.shape = shape;
            b2body.createFixture(fdef).setUserData(this);
            b2body.setLinearVelocity(vel);

            //Change texture of ship back to normal
            setRegion(ship);
    	}
    }
    
    /**
     * toggles on rubber coating powerup by making a new fixture with high restitution
     */
    public void turnOnRubber() {
    	if (!rubber) {
    		rubber = true;
    		
    		Vector2 vel = b2body.getLinearVelocity();
        	Vector2 pos = b2body.getPosition();
        	
	    	Array<Fixture> fix = b2body.getFixtureList();
	    	b2body.destroyFixture(fix.first());
    		
        	// Defines a players position
            BodyDef bdef = new BodyDef();
            bdef.position.set(pos.x , pos.y);
            bdef.type = BodyDef.BodyType.DynamicBody;
            b2body = world.createBody(bdef);

            // Defines a player's shape and contact borders
            FixtureDef fdef = new FixtureDef();
            fdef.restitution = 3f;
            CircleShape shape = new CircleShape();
            shape.setRadius(55 / PirateGame.PPM);

            // setting BIT identifier
            fdef.filter.categoryBits = PirateGame.PLAYER_BIT;

            // determining what this BIT can collide with
            fdef.filter.maskBits = PirateGame.DEFAULT_BIT | PirateGame.COIN_BIT | PirateGame.ENEMY_BIT | PirateGame.COLLEGE_BIT | PirateGame.COLLEGESENSOR_BIT | PirateGame.COLLEGEFIRE_BIT | PirateGame.POWERUP_BIT;
            fdef.shape = shape;
            b2body.createFixture(fdef).setUserData(this);
            b2body.setLinearVelocity(vel);

            //Speed buff
            dragFactor = 2.0f;
    	    maxSpeed = 10.0f + dragFactor;
    	    accel = 0.2f;
    	}
    }
    
    /**
     * toggles off the rubber coating powerup
     */
    public void turnOffRubber() {
    	if (rubber) {
    		rubber = false;

    		Vector2 vel = b2body.getLinearVelocity();
        	Vector2 pos = b2body.getPosition();
        	
	    	Array<Fixture> fix = b2body.getFixtureList();
	    	b2body.destroyFixture(fix.first());
    		
        	// Defines a players position
            BodyDef bdef = new BodyDef();
            bdef.position.set(pos.x , pos.y);
            bdef.type = BodyDef.BodyType.DynamicBody;
            b2body = world.createBody(bdef);

            // Defines a player's shape and contact borders
            FixtureDef fdef = new FixtureDef();
            CircleShape shape = new CircleShape();
            shape.setRadius(55 / PirateGame.PPM);

            // setting BIT identifier
            fdef.filter.categoryBits = PirateGame.PLAYER_BIT;

            // determining what this BIT can collide with
            fdef.filter.maskBits = PirateGame.DEFAULT_BIT | PirateGame.COIN_BIT | PirateGame.ENEMY_BIT | PirateGame.COLLEGE_BIT | PirateGame.COLLEGESENSOR_BIT | PirateGame.COLLEGEFIRE_BIT | PirateGame.POWERUP_BIT;
            fdef.shape = shape;
            b2body.createFixture(fdef).setUserData(this);
            b2body.setLinearVelocity(vel);

            //Put the speed back to normal
            speedNormal();
    	}
    }

    /** 
     * toggles on the soup powerup
     * (the regen is done in the hud)
     */
    public void turnOnSoup() {
    	if (!soup) {
    		soup = true;
    		dragFactor = 2.0f;
    	    maxSpeed = 7.0f + dragFactor;
    	    accel = 0.12f;
    	}
    }
    
    /**
     * toggles off the soup powerup
     */
    public void turnOffSoup() {
    	if (soup) {
    		soup = false;
    	    speedNormal();
    	}
    }
    
    /**
     * Sets the speed to normal
     */
    public void speedNormal(){
    	dragFactor = 1.0f;
    	maxSpeed = 5.0f + dragFactor;
    	accel = 0.08f;
    }

    /**
     * Slows down the ship (bad weather only)
     */
    public void weatherDebuff(){
        dragFactor = 1.0f;
    	maxSpeed = 4.0f + dragFactor;
    	accel = 0.05f;
    }

    /**
     * increases the multiplier with simple interest
     * @param percent the Acceleration percentage increase
     */
    public static void accelPercentInc(Integer percent){
        accelMul = accelMul + percent/100;

    }
    
    /**
     * Returns the acceleration
     * @return returns the acceleration as a float
     */
    public static float getAcceleration() {
    	float temp = accel;
    	return temp;
    }
    
    /**
     * Changes the max speed by a percentage increase
     * @param percentage
     */
    public static void setMaxSpeed(float percentage) {
    	maxSpeed = maxSpeed * (1 +(percentage/100));
    }
    
    
    //----------------------
    
   
    /**
     * Draws the player using batch
     * Draws cannonballs using batch
     *
     * @param batch The batch of the program
     */
    public void draw(Batch batch){
        // Draws player and cannonballs
        super.draw(batch);
        for(CannonFire ball : getCannonBalls()){
            ball.draw(batch);
        }
    }

	public Array<CannonFire> getCannonBalls() {
		return cannonBalls;
	}

	public void setCannonBalls(Array<CannonFire> cannonBalls) {
		this.cannonBalls = cannonBalls;
	}
}
