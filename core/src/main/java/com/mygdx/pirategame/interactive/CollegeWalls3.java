package com.mygdx.pirategame.interactive;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.pirategame.interactive.InteractiveTileObject;
import com.mygdx.pirategame.main.PirateGame;
import com.mygdx.pirategame.screens.GameScreen;

/**
 * College Walls (Constantine)
 * Checks interaction with walls from map
 *
 *@author Ethan Alabaster, Sam Pearson
 *@version 1.0
 */
public class CollegeWalls3 extends InteractiveTileObject {
    private GameScreen screen;

    /**
     * Sets bounds of college walls
     *
     * @param screen Visual data
     * @param bounds Wall bounds
     */
    public CollegeWalls3(GameScreen screen, Rectangle bounds) {
        super(screen, bounds);
        this.screen = screen;
        fixture.setUserData(this);
        //Set the category bit
        setCategoryFilter(PirateGame.COLLEGE_BIT);
    }

    /**
     * Checks for contact with cannonball
     */
    @Override
    public void onContact() {
        Gdx.app.log("wall", "collision");
        //Deal damage to the assigned college
        screen.getCollege("Constantine").onContact();
    }
}
