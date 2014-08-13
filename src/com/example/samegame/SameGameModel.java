package com.example.samegame;

import java.util.Random;

public class SameGameModel 
{

	// represents game state
	// view looks at this for display info
	// controller updates this
	
	Random rand; // RNG
	
	private int[][] grid; // grid data
	
	private int numColumns;
	private int numRows;
	
	private int numColours; // current difficulty
	
	private int maxDifficulty = 9; // TODO: static const
	private int minDifficulty = 3;
	
	private int emptyCellValue = 0; // TODO: look up how to do static const in java
	
	public SameGameModel(int numCols, int numRows)
	{
		this.numColumns = numCols;
		this.numRows = numRows;
		
		this.rand = new Random(); // initialize RNG
		
		this.grid = new int[this.numRows][this.numColumns]; // initialize grid
		
		this.numColours = minDifficulty;
		this.RandomizeGrid(this.numColours);
	}
	
	public int GetCurrentDifficulty()
	{
		return this.numColours;
	}
	
	public void IncreaseDifficulty()
	{
		if (this.numColours < this.maxDifficulty)
			this.numColours++;
		else
			this.numColours = this.minDifficulty; // wrap back around to easy
	}
	
	public void RandomizeGrid(int numColours)
	{
		// set all cells in the grid to a random non-empty value
		
		for (int r = 0; r < this.numRows; r++)
		{
			for (int c = 0; c < this.numColumns; c++)
			{
				int colour = rand.nextInt(numColours - 1) + 1; // this little bit actually requires knowing that emptyCellValue == 0, so it kind of sucks.
				this.SetValueForCell(r, c, colour);
			}
		}
	}
	
	public int GetNumRows()
	{
		return this.numRows;
	}
	
	public int GetNumColumns()
	{
		return this.numColumns;
	}
	
	// this function should always be used instead of direct access to the grid so that I can easily change the implementation later if I want to for optimizations
	// the function call should be optimized away by the compiler I imagine.
	public int GetValueForCell(int row, int col)
	{
		// TODO: this should throw an error for invalid row/col combinations
		if (row < 0 || row >= this.numRows || col < 0 || row >= this.numRows) 
			return 0;

		return this.grid[row][col];
	}
	
	private void SetValueForCell(int row, int col, int value)
	{
		// TODO: this should throw an error for invalid row/col combinations
		if (row < 0 || row >= this.numRows || col < 0 || row >= this.numRows) return;
		
		this.grid[row][col] = value;
	}
	
	private boolean CellIsEmpty(int row, int col)
	{
		return GetValueForCell(row, col) == emptyCellValue;
	}

	/* COLLAPSING FUNCTIONS */
	
	private void CollapseColumnVertically(int col, int startingRow)
	{
		if (startingRow >= this.numRows)
			return;
		
		if (GetValueForCell(startingRow, col) != emptyCellValue)
		{
			CollapseColumnVertically(col, startingRow + 1); // this cell is not empty. continue with the cell above it
			return;
		}
		
		for (int currRow = startingRow + 1; currRow < this.numRows; currRow++)
		{
			// find a non empty cell above this one
			int cellVal = GetValueForCell(currRow, col);
			if (cellVal != emptyCellValue)
			{
				// stick the value from that cell into this one
				SetValueForCell(startingRow, col, cellVal);
				SetValueForCell(currRow, col, emptyCellValue);
				
				CollapseColumnVertically(col, startingRow); // recurse
				return;
			}
		}
	}
	
	private void CollapseRows()
	{
		for (int i = 1; i <= this.numColumns; i++)
		{
			this.CollapseColumnVertically(this.numColumns - i, 0);
		}
		
	}
	
	private boolean ColumnIsEmpty(int col)
	{
		// foolproof way is to iterate the entire column and check all cells.
		// i know however this will only be called after collapseColumns, so if the cell in the bottom row is empty, the entire col is empty
		
		return CellIsEmpty(0, col);
	}

	private void MoveColumnXToColumnY(int x, int y)
	{
		for (int row = 0; row < this.numRows; row++)
		{
			SetValueForCell(row, x, GetValueForCell(row, y));
			SetValueForCell(row, y, this.emptyCellValue);
		}
	}
	
	private void CollapseColumnHorizontally(int col)
	{
		// if this column is empty, move all columns to the right of it over one column to the left
		
		if (col + 1 >= this.numColumns || col < 0) // column is invalid or furthest right -- can't move values to it
			return;
		else if (!ColumnIsEmpty(col)) // column isn't empty, don't shift columns into it
		{
			// advance to next column
			this.CollapseColumnHorizontally(col + 1);
			return;
		}
		else
		{
			int firstNonEmptyCol = -1;
			
			for (int i = col + 1; i < this.numColumns; i++)
			{
				if (!this.ColumnIsEmpty(i))
				{
					firstNonEmptyCol = i;
					break;
				}
			}
			
			if (firstNonEmptyCol == -1)
				return; // no non empty cols to copy here, done.
			
			this.MoveColumnXToColumnY(col, firstNonEmptyCol);
			this.CollapseColumnHorizontally(col);
			return;
		}
	}
	
	private void CollapseColumns()
	{
		// the thing this calls will recurse and deal with everything
		this.CollapseColumnHorizontally(0);
	}
	
	public void Collapse()
	{
		this.CollapseRows();
		this.CollapseColumns();
	}
	
	
	public boolean NoMovesAvailable()
	{
		for (int c = 0; c < this.numColumns; c++)
		{
			for (int r = 0; r < this.numRows; r++)
			{
				if (this.CellHasAdjacentSameColourCell(r, c))
					return false;
			}
		}
		return true;
	}
	
	public boolean GridIsEmpty()
	{
		for (int c = 0; c < this.numColumns; c++)
		{
			if (!this.ColumnIsEmpty(c))
				return false;
		}
		return true;
	}
	
	/* CELL DESTROYING FUNCTIONS */
	
	public boolean CellHasAdjacentSameColourCell(int row, int col)
	{
		int cellVal = this.GetValueForCell(row, col);
		
		// cell is empty. don't really want to know if adjacent cells are same colour. guess the function is named poorly!
		if (cellVal == this.emptyCellValue) 
			return false;
		
		// cell above
		if (row + 1 < this.numRows)
		{
			if (cellVal == this.GetValueForCell(row + 1, col))
				return true;
		}
		
		// cell below
		if (row > 0)
		{
			if (cellVal == this.GetValueForCell(row - 1, col))
				return true;
		}
		
		// cell to right
		if (col + 1 < this.numColumns)
		{
			if (cellVal == this.GetValueForCell(row, col + 1))
				return true;	
		}
		
		// cell to left
		if (col > 0)
		{
			if (cellVal == this.GetValueForCell(row, col - 1))
				return true;	
		}
		
		return false;
	}
	
	public void DestroyAdjacentSameColourCells(int row, int col)
	{
		// this function recursively searches out from the specified cell, clearing all adjacent cells of that colour
		// this combined with the "Collapse" function form the meat of the game
		
		int cellVal = this.GetValueForCell(row, col);
		this.SetValueForCell(row, col, this.emptyCellValue);	
		
		// this can be brutal on the stack as it is recursing and splitting in 4 at once. thought the compiler would optimize the recursion but java sucks
		
		// cell above
		if (row + 1 < this.numRows)
		{
			if (cellVal == this.GetValueForCell(row + 1, col))
				DestroyAdjacentSameColourCells(row + 1, col);
		}
		
		// cell below
		if (row > 0)
		{
			if (cellVal == this.GetValueForCell(row - 1, col))
				DestroyAdjacentSameColourCells(row - 1, col);
		}
		
		// cell to right
		if (col + 1 < this.numColumns)
		{
			if (cellVal == this.GetValueForCell(row, col + 1))
				DestroyAdjacentSameColourCells(row, col + 1);
		}
		
		// cell to left
		if (col > 0)
		{
			if (cellVal == this.GetValueForCell(row, col - 1))
				DestroyAdjacentSameColourCells(row, col - 1);	
		}
	}

}
