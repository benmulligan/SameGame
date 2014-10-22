package com.example.samegame;

import com.example.samegame.SameGameView.EHighlightMode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;



public class SameGameController {
	
	public enum ClickMode { NORMAL, BOMB, ROW_DESTROY, COL_DESTROY } ; 
	
	// registered with view for touch events
	// updates model after touch events
	// tells view to update when model changed (maybe no point, maybe view just updates every frame instead)
	
	private SameGameModel model;
	private SameGameView view;
	

	
	private ClickMode currentClickMode;
	
	public SameGameController(SameGameModel model, SameGameView view)
	{
		this.model = model;
		this.view = view;
		
		this.setClickMode(ClickMode.NORMAL);
	}

	public void resetLevel()
	{
		Log.i("SameGame", " > Resetting grid at current difficulty");
		this.model.randomizeGrid(this.model.getCurrentDifficulty());
		this.view.invalidate();
	}
	
	public void nextLevel()
	{
		Log.i("SameGame", " > Increasing Difficulty");

		this.model.increaseDifficulty();
		this.model.randomizeGrid(this.model.getCurrentDifficulty());
		this.view.invalidate();
	}
	
	
	public void setClickMode(ClickMode mode)
	{
		this.currentClickMode = mode;
		
		switch (this.currentClickMode) // this is a 1-1 mapping always. they should probably just use the same enum
		{
			case BOMB:
				this.view.setTouchDownHighlightMode(EHighlightMode.BOMB);
				break;
			case ROW_DESTROY:
				this.view.setTouchDownHighlightMode(EHighlightMode.ROW);
				break;
			case COL_DESTROY:
				this.view.setTouchDownHighlightMode(EHighlightMode.COL);
				break;
			case NORMAL:
			default:
				this.view.setTouchDownHighlightMode(EHighlightMode.NORMAL);
				break;
		}
	}

	// handle the touch down
	public void handleNormalClick(int row, int col)
	{
		if (this.model.cellHasAdjacentSameColourCell(row, col))
		{
			// condition is met for destroying stuff. DESTROY!!!
			
			this.model.destroyAdjacentSameColourCells(row, col);
			this.model.collapse();
		}
	}
	
	public void handleBombClick(int row, int col)
	{
		this.model.destroy9x9Square(row, col);
		this.model.collapse();

		this.setClickMode(ClickMode.NORMAL);
	}
	
	public void handleRowDestroyClick(int row, int col)
	{
		this.model.destroyRow(row);
		this.model.collapse();
		
		this.setClickMode(ClickMode.NORMAL);		
	}
	
	public void handleColDestroyClick(int row, int col)
	{
		this.model.destroyCol(col);
		this.model.collapse();
		
		this.setClickMode(ClickMode.NORMAL);
	}
	
	public void handleTouchUpOnCell(int row, int col)
	{
		if (this.model.cellIsEmpty(row, col))
			return;
		
		switch(this.currentClickMode)
		{
			case BOMB:
				this.handleBombClick(row, col);
				break;
			case ROW_DESTROY:
				this.handleRowDestroyClick(row, col);
				break;
			case COL_DESTROY:
				this.handleColDestroyClick(row, col);
				break;
			
			case NORMAL:
			default:
				this.handleNormalClick(row, col);
				break;
			
		}
		
			
		//Log.w("SameGame", " > [" + this.model.getNumCellsRemaining() + "] Cells remaining");
		//this.model.writeGridToLog();			
			
		// detect empty grid
		if (this.model.gridIsEmpty())
		{
			Log.i("SameGame", " + LEVEL COMPLETE +");

			Dialog d = this.onLevelCompleteDialog();
			d.show();
		}
			
		// detect "no moves"
		else if (this.model.noMovesAvailable())
		{
			Log.i("SameGame", " - NO MORE MOVES -");

			Dialog d = this.onLevelFailedDialog();
			d.show();
		}
			
		this.model.notifyObservers(); // the model has changed, notify any observers. (TODO: the model itself should trigger this)
	}
	
	// dialogs
	// TODO: I don't really like the controller pushing dialogs to the UI
	public Dialog onLevelFailedDialog() 
	{
        AlertDialog.Builder builder = new AlertDialog.Builder(this.view.getContext());
        builder.setMessage("Level Failed!")
               .setPositiveButton("Retry", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                	   resetLevel();
                   }               
               });
        return builder.create();
    }
	
	public Dialog onLevelCompleteDialog() 
	{
        AlertDialog.Builder builder = new AlertDialog.Builder(this.view.getContext());
        builder.setMessage("Level Complete!")
               .setPositiveButton("Next Level", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                	   nextLevel();
                   }               
               });
        return builder.create();
    }


}
