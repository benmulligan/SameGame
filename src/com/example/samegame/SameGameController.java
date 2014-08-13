package com.example.samegame;

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

	public void HandleTouchUpOnCell(int row, int col)
	{
		if (this.model.CellHasAdjacentSameColourCell(row, col))
		{
			// condition is met for destroying stuff. DESTROY!!!
			
			this.model.DestroyAdjacentSameColourCells(row, col);
			this.model.Collapse();
			
			// detect empty grid
			if (this.model.GridIsEmpty())
			{
				System.out.println("Grid is empty: Increasing Difficulty");
				this.model.IncreaseDifficulty();
				this.model.RandomizeGrid(this.model.GetCurrentDifficulty());

			}
			
			// detect "no moves"
			if (this.model.NoMovesAvailable())
			{
				System.out.println("No moves: Resetting grid at current difficulty");
				this.model.RandomizeGrid(this.model.GetCurrentDifficulty());
			}
			
			// notify view of updates
			this.view.invalidate();
		}
	}
	
}
