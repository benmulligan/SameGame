package com.example.samegame;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class SameGame extends Activity {

	
	
    private SameGameView sgView;
    private SameGameModel sgModel;
    private SameGameController sgController;
    
    //private static String ICICLE_KEY = "sg-view";

    /**
     * Called when Activity is first created. Turns off the title bar, sets up
     * the content views, and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);


        
        this.hideStatusBar();
        setContentView(R.layout.activity_same_game);
        
        sgView = (SameGameView) findViewById(R.id.samegame); // the view should know about the model...
        this.initializeModel();
        sgController = new SameGameController(sgModel, sgView);
        
        sgView.setController(sgController); // TODO: shouldn't be like this, controller should be observer of view
        
        // set sgView as an observer of the model
        sgModel.registerBoardObserver(sgView);
        sgView.setBoard(sgModel);
        
        
        // TODO: to be a bit more purist, i'd like the controller handling the touch events directly
        sgView.setOnTouchListener(sgView);
        

        
        
        //sgView.setTextView((TextView) findViewById(R.id.text));

        
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

    private void hideStatusBar()
    {
    	
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        
    }
    @SuppressWarnings("deprecation")
	private void initializeModel()
    {
        // determine an appropriate board size.
        // this information will be used to inform how we should initialize the model.
    	
    	int screenHeight = 0;
    	int screenWidth = 0;
    	WindowManager w = getWindowManager();

/*    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
    	{
        	Point size = new Point();
    	    w.getDefaultDisplay().getSize(size);

    	    screenWidth = size.x;
    	    screenHeight = size.y;
    	}
    	else*/
    	{
    	    Display d = w.getDefaultDisplay();
    	    screenWidth = d.getWidth();
    	    screenHeight = d.getHeight();
    	}

    	int boardHeight = screenHeight - 60; // 60 is the height that the bar area will have. not exactly sure how to get it correctly before it is drawn

    	sgView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, boardHeight ));

    	

    	
    	// sgView has not been layed out at this point, so we can't use its dimensions.
        //int screenHeight = sgView.getHeight();
        //int screenWidth = sgView.getWidth();
        
        Log.w("SameGame", "Dimensions: [" + screenHeight + " , " + screenWidth + "]");
        
        int blockSize = sgView.getCellHeight(); // we're assuming squares here
        
        int maxCols  = screenWidth/blockSize - 1;
        int maxRows = boardHeight/blockSize - 1;
        
        Log.i("SameGame", " + Initializing gameboard [" + maxRows + " , " + maxCols + "]");
        
        sgModel = new SameGameModel(maxCols, maxRows); 
    }

    
    // button handlers
    public void onClickSkip(View v)
    {
    	sgController.nextLevel();
    }
    
    public void onClickReset(View v)
    {
    	sgController.resetLevel();
    }
    
    public void onClickBomb(View v)
    {
    	sgController.setClickMode(SameGameController.ClickMode.BOMB);
    }
    
    public void onClickRowDestroy(View v)
    {
    	sgController.setClickMode(SameGameController.ClickMode.ROW_DESTROY);
    }
    
    public void onClickColDestroy(View v)
    {
    	sgController.setClickMode(SameGameController.ClickMode.COL_DESTROY);
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
