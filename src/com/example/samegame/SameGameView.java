package com.example.samegame;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SameGameView extends View implements OnTouchListener
{
	// displays current game state
	// receives touch events (relays to controller for handling)
	// deals with rendering
	
	private SameGameModel model;
	private SameGameController controller;
	
    private int screenHeight;
    private int screenWidth; 
    
	private Paint paint;
	
	// TODO: write code that deals with possibly too small screens. use different grid sizes based on screen sizes
	private int cellWidth = 30; // TODO: static const
	private int cellHeight = 30; // TODO: static const
	
	// offset of the touch area from bottom left corner
	private int boardOffsetX = 0;
	private int boardOffsetY = 0;
	
	private int colours[] = { 
			Color.BLACK, // No cell colour
			Color.RED,
			Color.BLUE,
			Color.YELLOW,
			Color.GREEN,
			Color.MAGENTA,
			Color.CYAN,
			Color.LTGRAY}; // the size of this array is tied directly to the value of model.maxDifficulty, and so should not be defined here
	
	
    /**
     * Constructs a SameGameView based on inflation from XML
     * 
     * @param context
     * @param attrs
     */
    public SameGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSameGameView();
   }

    public SameGameView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	initSameGameView();
    }
    
    private void initSameGameView()
    {
		this.paint = new Paint();
		this.paint.setStrokeWidth(30.0f);
    }
	
    public void setModel(SameGameModel model)
    {
    	this.model = model;
    }
    
    public void setController(SameGameController controller)
    {
    	this.controller = controller;
    }
    
    public Rect setScreenRectForCell(int r, int c)
    {
    	Rect rect = new Rect();
  
		rect.left = c * this.cellWidth + this.boardOffsetX;
		rect.bottom = this.screenHeight - ( (r + 1) * this.cellHeight + this.boardOffsetY);
		rect.right = (c + 1) * this.cellWidth + this.boardOffsetX;  	
		rect.top = this.screenHeight - ( r * this.cellHeight + this.boardOffsetY);	

    	return rect;
    }

    public int getCellHeight()
    {
    	return this.cellHeight;
    }
    
    @Override
    public void onDraw(Canvas canvas) 
    {
        super.onDraw(canvas);

        
        // this height/width and offset stuff ideally should not be done every draw.
        // instead, this should only happen whenever the screen dims change (rotations, etc)
        this.screenHeight = this.getHeight();
        this.screenWidth = this.getWidth();
        
        //Log.w("SameGame", "ondraw: [" + screenHeight + " , " + screenWidth + "]");

        int numColumns = this.model.getNumColumns();
        int numRows = this.model.getNumRows();
    
        this.boardOffsetX = this.screenWidth / 2 - (numColumns * this.cellWidth / 2);
        this.boardOffsetY = this.screenHeight / 2 - (numRows * this.cellHeight / 2);

        if (this.boardOffsetX < 0 || this.boardOffsetY < 0)
        	Log.e("SameGame", "ERROR: BOARD EXCEEDING SCREEN BOUNDARIES");
        
        // draw every individual tile
        
        for (int r = 0; r < numRows; r++)
        {
        	for (int c = 0; c < numColumns; c++)
        	{
        		int cellColourIndex = this.model.getValueForCell(r,c);
        		
        		// TODO: throw error if cell colour is invalid
        		int cellColour = this.colours[0];
        		if (cellColourIndex >= 0 && cellColourIndex < colours.length)
        			cellColour = this.colours[cellColourIndex];
        			
        		this.paint.setColor(cellColour);

        		Rect rect = this.setScreenRectForCell(r, c);
        		
        		canvas.drawRect(rect, paint);
        		/*
        		(c * this.cellWidth + this.boardOffsetX, // LEFT
        				(r + 1) * this.cellHeight + this.boardOffsetY,  // TOP
        				(c + 1) * this.cellWidth + this.boardOffsetX,  	// RIGHT
        				r * this.cellHeight + this.boardOffsetY,		// BOTTOM
        				paint);
        		*/
        	}
        }
    }

    private boolean touchIsOnBoard(int xPos, int yPos)
    {
    	return xPos > boardOffsetX && 
    			xPos < ((this.model.getNumColumns() * this.cellWidth) + boardOffsetX) &&
    			yPos < this.screenHeight - boardOffsetY &&
    			yPos > this.screenHeight - ((this.model.getNumColumns() * this.cellHeight) + boardOffsetY);
    }
    
    private int convertYPosToRow(int yPos)
    {
    	// TODO: does java handle converting doubles to ints correctly? i know in C++ this is an issue due to (usually) converting 64-bit to 32-bit
    	return (int) Math.floor((this.screenHeight - (yPos + boardOffsetY)) / cellHeight);
    }

    
    private int convertXPosToColumn(int xPos)
    {
    	// TODO: does java handle converting doubles to ints correctly? i know in C++ this is an issue due to (usually) converting 64-bit to 32-bit
    	return (int) Math.floor((xPos - boardOffsetX) / cellWidth);
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		// only pass the touch up events along.
		// TODO: maybe change this so that the touch down and touch up has to occur on the same object for the click to go through
        if (event.getAction()==MotionEvent.ACTION_DOWN)
        {
        	int xPos = (int) event.getX();
        	int yPos = (int) event.getY();

        	if (!touchIsOnBoard(xPos, yPos))
        	{
        		System.out.println("Touch not on board!");
        		return false;
        	}
        	
    		int c = this.convertXPosToColumn(xPos);
    		int r = this.convertYPosToRow(yPos);
    		
    		this.controller.handleTouchUpOnCell(r, c);
    		
    		return true;
        }

		return false;
	}
}
