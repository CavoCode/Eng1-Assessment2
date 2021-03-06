package com.mygdx.pirategame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.*;
import com.mygdx.pirategame.entities.Player;
import com.mygdx.pirategame.interactive.WorldContactListener;
import com.mygdx.pirategame.interactive.WorldCreator;
import com.mygdx.pirategame.entities.AvailableSpawn;
import com.mygdx.pirategame.entities.Coin;
import com.mygdx.pirategame.entities.Powerup;
import com.mygdx.pirategame.entities.College;
import com.mygdx.pirategame.entities.EnemyShip;
import com.mygdx.pirategame.entities.Fire;
import com.mygdx.pirategame.hud.Hud;
import com.mygdx.pirategame.main.PirateGame;
import com.mygdx.pirategame.screens.MainMenu;

import java.util.Random;

import javax.print.event.PrintEvent;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Game Screen
 * Class to generate the various screens used to play the game.
 * Instantiates all screen types and displays current screen.
 *
 *@author Ethan Alabaster, Adam Crook, Joe Dickinson, Sam Pearson, Tom Perry, Edward Poulter
 *@version 1.0
 */
public class GameScreen implements Screen {
    private static float maxSpeed = 2.5f;
    private static float accel = 0.05f;
    private float stateTime;

    /**(Team 17)
    * Change to allow packages to work - made PirateGame game public to other packages
    */
    public static PirateGame game;
    private MainMenu mainMenu;
    private OrthographicCamera camera;
    private Viewport viewport;
    private final Stage stage;

    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private World world;
    private Box2DDebugRenderer b2dr;
    
    public static boolean headless;

    public static Player player; //Team 17 - to call it in PirateGame
    private static HashMap<String, College> colleges = new HashMap<>();
    private static ArrayList<EnemyShip> ships = new ArrayList<>();
    private static ArrayList<Coin> Coins = new ArrayList<>();
    private AvailableSpawn invalidSpawn = new AvailableSpawn();
    private Hud hud;
    
    //Team 17------
    private static ArrayList<Powerup> Powerups = new ArrayList<>();
    private static long powerupActivatedTime;  //Used as common timer for all powerup instances so just has get and set methods
    private static String powerupType;
    
    private static ArrayList<Fire> fires = new ArrayList<>();
    private long lastFire;
    private String difficulty;
    //------------

    public static final int GAME_RUNNING = 0;
    public static final int GAME_PAUSED = 1;
    private static int gameStatus;

    private Table pauseTable;
    private Table table;

    public Random rand = new Random();

    /**
     * Initialises the Game Screen,
     * generates the world data and data for entities that exist upon it,
     * @param game passes game data to current class,
     * @param headless 
     */
    public GameScreen(PirateGame game, boolean headless){
        gameStatus = GAME_RUNNING;
        this.game = game;
        this.headless = headless;
        // Initialising camera and extendable viewport for viewing game
        camera = new OrthographicCamera();
        camera.zoom = 0.0155f;
        viewport = new ScreenViewport(camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        
        // Initialize difficulty
        difficulty = MainMenu.getDifficulty();
        
        // making the Tiled tmx file render as a map
        maploader = new TmxMapLoader();
        map = maploader.load("map/map.tmx");
        
        if (!headless) {
        	// Initialize a hud
            hud = new Hud(game.batch);
            b2dr = new Box2DDebugRenderer();
         
            renderer = new OrthogonalTiledMapRenderer(map, 1 / PirateGame.PPM);
        }
        

        // Initialising box2d physics
        world = new World(new Vector2(0,0), true);
        
        player = new Player(this);

        
        
        new WorldCreator(this);

        // Setting up contact listener for collisions
        world.setContactListener(new WorldContactListener());

        // Spawning enemy ship and coin. x and y is spawn location
        colleges = new HashMap<>();
        colleges.put("Alcuin", new College(this, "Alcuin", 1900 / PirateGame.PPM, 2100 / PirateGame.PPM,
                "ships&colleges/alcuin_flag.png", "ships&colleges/alcuin_ship.png", 0, invalidSpawn, difficulty));
        colleges.put("Anne Lister", new College(this, "Anne Lister", 6304 / PirateGame.PPM, 1199 / PirateGame.PPM,
                "ships&colleges/anne_lister_flag.png", "ships&colleges/anne_lister_ship.png", 8, invalidSpawn, difficulty));
        colleges.put("Constantine", new College(this, "Constantine", 6240 / PirateGame.PPM, 6703 / PirateGame.PPM,
                "ships&colleges/constantine_flag.png", "ships&colleges/constantine_ship.png", 8, invalidSpawn, difficulty));
        colleges.put("Goodricke", new College(this, "Goodricke", 1760 / PirateGame.PPM, 6767 / PirateGame.PPM,
                "ships&colleges/goodricke_flag.png", "ships&colleges/goodricke_ship.png", 8, invalidSpawn, difficulty));
        ships = new ArrayList<>();
        ships.addAll(colleges.get("Alcuin").fleet);
        ships.addAll(colleges.get("Anne Lister").fleet);
        ships.addAll(colleges.get("Constantine").fleet);
        ships.addAll(colleges.get("Goodricke").fleet);

        //Random ships
        Boolean validLoc;
        int a = 0;
        int b = 0;
        for (int i = 0; i < 20; i++) {
            validLoc = false;
            while (!validLoc) {
                //Get random x and y coords
                a = rand.nextInt(AvailableSpawn.xCap - AvailableSpawn.xBase) + AvailableSpawn.xBase;
                b = rand.nextInt(AvailableSpawn.yCap - AvailableSpawn.yBase) + AvailableSpawn.yBase;
                //Check if valid
                validLoc = checkGenPos(a, b);
            }
            //Add a ship at the random coords
            ships.add(new EnemyShip(this, a, b, "ships&colleges/unaligned_ship.png", "Unaligned", difficulty));
        }

        //Random coins
        Coins = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            validLoc = false;
            while (!validLoc) {
                //Get random x and y coords
                a = rand.nextInt(AvailableSpawn.xCap - AvailableSpawn.xBase) + AvailableSpawn.xBase;
                b = rand.nextInt(AvailableSpawn.yCap - AvailableSpawn.yBase) + AvailableSpawn.yBase;
                validLoc = checkGenPos(a, b);
            }
            //Team 17------------
            //Weather submerged checker
            if (i >= 100){
                //Add a submerge-able coin at the random coords
                Coin coinTemp = new Coin(this, a, b, true);
                coinTemp.setVisible(false);
                Coins.add(coinTemp);
            }
            else{
                //Add a normal coin at the random coords
                Coins.add(new Coin(this, a, b, false));
            }
            //------------------
        }
        
        //-------Team-17--------
        //Random powerups
        Powerups = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            validLoc = false;
            while (!validLoc) {
                //Get random x and y coords
                a = rand.nextInt(AvailableSpawn.xCap - AvailableSpawn.xBase) + AvailableSpawn.xBase;
                b = rand.nextInt(AvailableSpawn.yCap - AvailableSpawn.yBase) + AvailableSpawn.yBase;
                validLoc = checkGenPos(a, b);
            }
            
            if (i >= 20){
                //Add a submerge-able powerup at the random coords
                Powerup powerupTemp = new Powerup(this, a, b,true);
                powerupTemp.setVisible(false);
                Powerups.add(powerupTemp);
            }
            else{
                //Add a normal powerup at the random coords
                Powerups.add(new Powerup(this, a, b,false));
            }
        }
        //----------------------
        
        if (!headless) {
        	//Setting stage
            stage = new Stage(new ScreenViewport());
        }
        else {
        	stage = null;
        }
    }

    /**
     * Makes this the current screen for the game.
     * Generates the buttons to be able to interact with what screen is being displayed.
     * Creates the escape menu and pause button
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Skin skin = new Skin(Gdx.files.internal("skin\\uiskin.json"));

        //GAME BUTTONS
        final TextButton pauseButton = new TextButton("Pause",skin);
        final TextButton skill = new TextButton("Skill Tree", skin);

        //PAUSE MENU BUTTONS
        final TextButton start = new TextButton("Resume", skin);
        final TextButton options = new TextButton("Options", skin);
        TextButton exit = new TextButton("Exit", skin);


        //Create main table and pause tables
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        pauseTable = new Table();
        pauseTable.setFillParent(true);
        stage.addActor(pauseTable);


        //Set the visability of the tables. Particuarly used when coming back from options or skillTree
        if (gameStatus == GAME_PAUSED){
            table.setVisible(false);
            pauseTable.setVisible(true);
        }
        else{
            pauseTable.setVisible(false);
            table.setVisible(true);
        }

        //ADD TO TABLES
        table.add(pauseButton);
        table.row().pad(10, 0, 10, 0);
        table.left().top();

        pauseTable.add(start).fillX().uniformX();
        pauseTable.row().pad(20, 0, 10, 0);
        pauseTable.add(skill).fillX().uniformX();
        pauseTable.row().pad(20, 0, 10, 0);
        pauseTable.add(options).fillX().uniformX();
        pauseTable.row().pad(20, 0, 10, 0);
        pauseTable.add(exit).fillX().uniformX();
        pauseTable.center();


        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                table.setVisible(false);
                pauseTable.setVisible(true);
                pause();

            }
        });
        skill.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                pauseTable.setVisible(false);
                game.changeScreen(PirateGame.SKILL);
            }
        });
        start.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                pauseTable.setVisible(false);
                table.setVisible(true);
                resume();
            }
        });
        options.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor){
                pauseTable.setVisible(false);
                game.setScreen(new Options(game,game.getScreen()));
            }
        });
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    /**
     * Checks for input and performs an action
     * Applies to keys "W" "A" "S" "D" "E" "Esc"
     *
     * Caps player velocity
     *
     * @param dt Delta time (elapsed time since last game tick)
     */
    public void handleInput(float dt) {
        if (gameStatus == GAME_RUNNING) {
        	//-------Team-17--------
            // Left physics impulse on 'A'
        	int impulseX = 0;
        	int impulseY = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                //player.b2body.applyLinearImpulse(new Vector2(-accel, 0), player.b2body.getWorldCenter(), true);
            	impulseX -= 1;
            }
            // Right physics impulse on 'D'
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                //player.b2body.applyLinearImpulse(new Vector2(accel, 0), player.b2body.getWorldCenter(), true);
            	impulseX += 1;
            }
            // Up physics impulse on 'W'
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                //player.b2body.applyLinearImpulse(new Vector2(0, accel), player.b2body.getWorldCenter(), true);
            	impulseY += 1;
            }
            // Down physics impulse on 'S'
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                //player.b2body.applyLinearImpulse(new Vector2(0, -accel), player.b2body.getWorldCenter(), true);
            	impulseY -= 1;	
            }
            player.applyImpuse(impulseX, impulseY);
            // Cannon fire on 'Space bar'
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                player.fire();
            }
            //----------------------

            //TEST INPUTS KEY TEAM 17-------
            /**
            //Sinks any sinkable objects
            if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                for (int i = 0; i < Coins.size(); i++){
                    Hud.weatherTimer = -1;
                }
            }
            //Rises any risable objects
            if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                for (int i = 0; i < Coins.size(); i++){
                    Hud.weatherTimer = 101;
                }
            }
            */
            //------------------------------

        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if(gameStatus == GAME_PAUSED) {
                resume();
                table.setVisible(true);
                pauseTable.setVisible(false);
            }
            else {
                table.setVisible(false);
                pauseTable.setVisible(true);
                pause();
            }
        }
    }
    
    /**
     * Updates the state of each object with delta time
     *
     * @param dt Delta time (elapsed time since last game tick)
     */
    public void update(float dt) {
        stateTime += dt;
        handleInput(dt);
        // Stepping the physics engine by time of 1 frame
        world.step(1 / 60f, 6, 2);

        // Update all players and entities
        player.update(dt);
        colleges.get("Alcuin").update(dt);
        colleges.get("Anne Lister").update(dt);
        colleges.get("Constantine").update(dt);
        colleges.get("Goodricke").update(dt);

        //Update ships
        for (int i = 0; i < ships.size(); i++) {
            ships.get(i).update(dt);
        }

        //Updates coins
        for (int i = 0; i < Coins.size(); i++) {
            Coins.get(i).update();
        }    
        //-------Team-17--------
        //Updates powerups
        for (int i = 0; i < Powerups.size(); i++) {
            Powerups.get(i).update();
        }
        
        //Updates fires
        for (int i = 0; i < fires.size(); i++) {
        	fires.get(i).update();
        }
        
        keepPowerupEffects();
        //----------------------
        
        //After a delay check if a college is destroyed. If not, if can fire

        if (stateTime > 1) {
            if (!colleges.get("Anne Lister").destroyed) {
                colleges.get("Anne Lister").fire();
            }
            if (!colleges.get("Constantine").destroyed) {
                colleges.get("Constantine").fire();
            }
            if (!colleges.get("Goodricke").destroyed) {
                colleges.get("Goodricke").fire();
        }
        stateTime = 0;
    }
        
        if (!headless) {
        	hud.update(dt);
        	
        	// Centre camera on player boat
            camera.position.x = player.b2body.getPosition().x;
            camera.position.y = player.b2body.getPosition().y;
            camera.update();
            renderer.setView(camera);
        }
    }

    /**
     * Renders the visual data for all objects
     * Changes and renders new visual data for ships
     *
     * @param dt Delta time (elapsed time since last game tick)
     */
    @Override
    public void render(float dt) {
        if (gameStatus == GAME_RUNNING) {
            update(dt);
        }
        else{handleInput(dt);}

        Gdx.gl.glClearColor(46/255f, 204/255f, 113/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.render();
        // b2dr is the hitbox shapes, can be commented out to hide
        //b2dr.render(world, camera.combined);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        // Order determines layering

        //Renders coins
        for(int i=0;i<Coins.size();i++) {
            Coins.get(i).draw(game.batch);
        }
        
      //-------Team-17--------
        //Renders powerups
        for(int i=0;i<Powerups.size();i++) {
        	Powerups.get(i).draw(game.batch);
        }
        
      //Renders fires
        for(int i=0;i<fires.size();i++) {
        	fires.get(i).draw(game.batch);
        }
      //----------------------
        //Renders colleges
        player.draw(game.batch);
        colleges.get("Alcuin").draw(game.batch);
        colleges.get("Anne Lister").draw(game.batch);
        colleges.get("Constantine").draw(game.batch);
        colleges.get("Goodricke").draw(game.batch);

        //Updates all ships
        for (int i = 0; i < ships.size(); i++){
            if (ships.get(i).college != "Unaligned") {
                //Flips a colleges allegence if their college is destroyed
                if (colleges.get(ships.get(i).college).destroyed) {

                    ships.get(i).updateTexture("Alcuin", "ships&colleges/alcuin_ship.png");
                }
            }
            ships.get(i).draw(game.batch);
        }
        game.batch.end();
        Hud.stage.draw();
        stage.act();
        stage.draw();
        //Checks game over conditions
        gameOverCheck();
    }

    /**
     * Changes the camera size, Scales the hud to match the camera
     *
     * @param width the width of the viewable area
     * @param height the height of the viewable area
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        stage.getViewport().update(width, height, true);
        Hud.resize(width, height);
        camera.update();
        renderer.setView(camera);
    }

    /**
     * Returns the map
     *
     * @return map : returns the world map
     */
    public TiledMap getMap() {
        return map;
    }

    /**
     * Returns the world (map and objects)
     *
     * @return world : returns the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the college from the colleges hashmap
     *
     * @param collegeName uses a college name as an index
     * @return college : returns the college fetched from colleges
     */
    public College getCollege(String collegeName) {
        return colleges.get(collegeName);
    }

    /**
     * Checks if the game is over
     * i.e. goal reached (all colleges bar "Alcuin" are destroyed)
     */
    public void gameOverCheck(){
        //Lose game if ship on 0 health or Alcuin is destroyed
        if (Hud.getHealth() <= 0 || colleges.get("Alcuin").destroyed) {
            game.changeScreen(PirateGame.DEATH);
            game.killGame();
        }
        //Win game if all colleges destroyed
        else if (colleges.get("Anne Lister").destroyed && colleges.get("Constantine").destroyed && colleges.get("Goodricke").destroyed){
            game.changeScreen(PirateGame.VICTORY);
            game.killGame();
        }
    }

    /**
     * Fetches the player's current position
     *
     * @return position vector : returns the position of the player
     */
    public Vector2 getPlayerPos(){
        return new Vector2(player.b2body.getPosition().x, player.b2body.getPosition().y);
    }

    /**
     * not used
     * Updates player acceleration by a given percentage. Accessed by skill tree
     *
     * @param percentage percentage increase
     */
    public static void changeAcceleration(Integer percentage){
        Player.accelPercentInc(percentage);
    }

    /**
     * Updates player max speed by a given percentage. Accessed by skill tree
     *
     * @param percentage percentage increase
     */
    public static void changeMaxSpeed(float percentage){
    	Player.setMaxSpeed(percentage);
    }
    
    //-------Team-17--------
    /**
     * Sets when the last powerup was activated
     * @param time the time the last powerup was activated
     */
    public static void setPowerupActivatedTime(long time) {
    	powerupActivatedTime = time;
    }
    
    /**
     * returns when the last powerup was activated
     * @return the time the last powerup was activated
     */
    public static long getPowerupActivatedTime() {
    	return powerupActivatedTime;
    }
    
    /**
     * Sets the type of powerup, only certain inputs will have an effect
     * @param type the type of powerup
     */
    public static void setPowerupType(String type) {
    	powerupType = type;
    }
    
    public static String getPowerupType() {
    	return(powerupType);
    }
    
    /**
     * Controls what powerups are active/unactive
     * should be called every update
     */
    public void keepPowerupEffects() {
    	switch(powerupType) {
    	case "Auto Reload":
    		Hud.setPowerupType("Auto Reload");
    		player.turnOffAstral();
    		player.turnOffRubber();
    		player.turnOffSoup();
    		player.fire();
    		break;
    	case "Astral Body":
    		Hud.setPowerupType("Astral Body");
    		player.turnOffRubber();
    		player.turnOffSoup();
    		player.turnOnAstral();
    		break;
    	case "Oil Spill":
    		Hud.setPowerupType("Oil Spill");
    		player.turnOffAstral();
    		player.turnOffRubber();
    		player.turnOffSoup();
    		if (TimeUtils.timeSinceMillis(lastFire) > 100) {
    			float x = player.getX() + player.getWidth()/2;
        		float y = player.getY() + player.getWidth();
        		fires.add(new Fire(this, x, y));
        		lastFire = TimeUtils.millis();
    		}
    		break;
    	case "Rubber Coating":
    		Hud.setPowerupType("Rubber Coating");
    		player.turnOffAstral();
    		player.turnOffSoup();
    		player.turnOnRubber();
    		break;
    	case "Soup":
    		Hud.setPowerupType("Soup");
    		player.turnOffAstral();
    		player.turnOffRubber();
    		player.turnOnSoup();
    		break;
    	case "":
    		Hud.setPowerupType("");
    		player.turnOffAstral();
    		player.turnOffRubber();
    		player.turnOffSoup();
    	}
    }
    
    //----------------------

    /**
     * Changes the amount of damage done by each hit. Accessed by skill tree
     *
     * @param value damage dealt
     */
    public static void changeDamage(int value){

        for (int i = 0; i < ships.size(); i++){
            ships.get(i).changeDamageReceived(value);
        }
        colleges.get("Anne Lister").changeDamageReceived(value);
        colleges.get("Constantine").changeDamageReceived(value);
        colleges.get("Goodricke").changeDamageReceived(value);

    }

    /**
     * Tests validity of randomly generated position
     *
     * @param x random x value
     * @param y random y value
     */
    public Boolean checkGenPos(int x, int y){
        if (invalidSpawn.tileBlocked.containsKey(x)){
            ArrayList<Integer> yTest = invalidSpawn.tileBlocked.get(x);
            if (yTest.contains(y)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pauses game
     */
    @Override
    public void pause() {
        gameStatus = GAME_PAUSED;
    }

    /**
     * Resumes game
     */
    @Override
    public void resume() {
        gameStatus = GAME_RUNNING;
    }

    /**
     * (Not Used)
     * Hides game
     */
    @Override
    public void hide() {

    }

    /**
     * Disposes game data
     */
    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
        stage.dispose();
    }

    /**
     * Activates the weather event functions in other classes
     * Used only by Hud.update()
     */
    public static void weather(Boolean x) {
        if (x){
            //Turns on all coins and powerups
            for (int i = 0; i < Coins.size(); i++){
                Coins.get(i).setVisible(true);
            }
            for (int i = 0; i < Powerups.size(); i++){
                Powerups.get(i).setVisible(true);
            }

            //Player De-buffs (speed and accel)
            player.weatherDebuff();
            
            //Slows enemy ship speed
            for (int i = 0; i < ships.size(); i++){
                ships.get(i).changeSpeed(0.7F);
            }
        }
        else{
            //Turns off all coins and powerups
            for (int i = 0; i < Coins.size(); i++){
                Coins.get(i).setVisible(false);
            }
            for (int i = 0; i < Powerups.size(); i++){
                Powerups.get(i).setVisible(false);
            }

            //Player has normal speed
            player.speedNormal();

            //Changes enemy ship speed back to normal
            for (int i = 0; i < ships.size(); i++){
                ships.get(i).changeSpeed(1F);
            }
        }
    }
}
