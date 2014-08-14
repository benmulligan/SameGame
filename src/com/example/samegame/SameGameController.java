package com.example.samegame;

import android.util.Log;

public class SameGameController {

	// registered with view for touch events
	// updates model after touch events
	// tells view to update when model changed (maybe no point, maybe view just updates every frame instead)
	
	private SameGameModel model;
	private SameGameView view;
	
	
	public SameGameController(SameGameModel model, SameGameView view)
	{
		this.model = model;
		this.view = view;
	}

	public void handleTouchUpOnCell(int row, int col)
	{
		if (this.model.cellHasAdjacentSameColourCell(row, col))
		{
			// condition is met for destroying stuff. DESTROY!!!
			
			this.model.destroyAdjacentSameColourCells(row, col);
			this.model.collapse();
			
			//Log.w("SameGame", " > [" + this.model.getNumCellsRemaining() + "] Cells remaining");
			//this.model.writeGridToLog();			
			
			// detect empty grid
			if (this.model.gridIsEmpty())
			{
				Log.i("SameGame", "Grid is empty: Increasing Difficulty");
				this.model.increaseDifficulty();
				this.model.randomizeGrid(this.model.getCurrentDifficulty());

			}
			
			// detect "no moves"
			if (this.model.noMovesAvailable())
			{
				Log.i("SameGame", "No moves: Resetting grid at current difficulty");
				this.model.randomizeGrid(this.model.getCurrentDifficulty());
			}
			
			// notify view of updates
			this.view.invalidate();
		}
	}
	
}
