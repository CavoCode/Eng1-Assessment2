package com.mygdx.pirategame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.pirategame.main.PirateGame;

/**
 * Main menu is the first screen the player sees. Allows them to navigate where they want to go to
 * @author Sam Pearson
 * @version 1.0
 */
public class MainMenu implements Screen {

    private final PirateGame parent;
    private final Stage stage;
    private ShapeRenderer shapeRenderer;

    /**
     * Instantiates a new Main menu.
     *
     * @param PirateGame the main starting body of the game. Where screen swapping is carried out.
     */
    public MainMenu(PirateGame PirateGame){
        parent = PirateGame;
        stage = new Stage(new ScreenViewport());
    }

    /**
     * What should be displayed on the options screen
     *
     */
    @Override
    public void show() {
        //Set the input processor
        Gdx.input.setInputProcessor(stage);
        // Create a table for the buttons
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        //The skin for the actors
        Skin skin = new Skin(Gdx.files.internal("skin\\uiskin.json"));

        //create buttons
        TextButton newGame = new TextButton("New Game", skin);
        TextButton help = new TextButton("Help", skin);
        TextButton options = new TextButton("Options", skin);
        TextButton exit = new TextButton("Exit", skin);

        //add buttons to table
        table.add(newGame).fillX().uniformX();
        table.row().pad(10, 0, 10, 0);
        table.add(help).fillX().uniformX();
        table.row();
        table.add(options).fillX().uniformX();
        table.row();
        table.add(exit).fillX().uniformX();

        //add listeners to the buttons

        //Start a game
        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                parent.changeScreen(PirateGame.GAME);
            }
        });
        //Help Screen
        help.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                parent.changeScreen(PirateGame.HELP);
            }
        });

        //Go to edit options
        options.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor){
                parent.setScreen(new Options(parent,parent.getScreen()));
            }
        });


        //Quit game
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        shapeRenderer = new ShapeRenderer();
    }

    /**
     * Renders the visual data for all objects
     * @param delta Delta Time
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        /**
         * Here is a test code of making an opaque overlay which may be used to 
         * implement weather dynamically across the whole map
         */
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0,1,0,1));
        shapeRenderer.rect(50,50, 50, 50);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.3f));
        shapeRenderer.rect(0, 0, stage.getWidth(), stage.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    /**
     * Changes the camera size, Scales the hud to match the camera
     *
     * @param width the width of the viewable area
     * @param height the height of the viewable area
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * (Not Used)
     * Pauses game
     */
    @Override
    public void pause() {
    }

    /**
     * (Not Used)
     * Resumes game
     */
    @Override
    public void resume() {
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
        stage.dispose();
    }
}




