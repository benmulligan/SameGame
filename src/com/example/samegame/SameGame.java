package com.example.samegame;

import android.app.Activity;
import android.os.Bundle;

public class SameGame extends Activity {

	
	
    private SameGameView sgView;
    private SameGameModel sgModel;
    private SameGameController sgController;
    
    // No idea what this is yet, copied from sample game
    private static String ICICLE_KEY = "sg-view";

    /**
     * Called when Activity is first created. Turns off the title bar, sets up
     * the content views, and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_same_game);


        sgModel = new SameGameModel(35, 20);
        sgView = (SameGameView) findViewById(R.id.samegame); // the view should know about the model...
        sgController = new SameGameController(sgModel, sgView);
        
        sgView.SetController(sgController); // shouldn't be like this, controller should be observer of view
        sgView.SetModel(sgModel);
        
        //sgView.setTextView((TextView) findViewById(R.id.text));

        sgView.setOnTouchListener(sgView);
        
        // don't care about this kind of stuff yet
        /*
        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
        	sgView.setMode(SameGameView.READY);
        } else {
            // We are being restored
            Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
            	sgView.restoreState(map);
            } else {
            	sgView.setMode(SameGameView.PAUSE);
            }
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        
        // TODO: implement this stuff
        //sgView.setMode(SameGameView.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
    	
    	// TODO: implement this stuff
        //outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

}
