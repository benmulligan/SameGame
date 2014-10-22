package com.example.samegame;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;

public class SameGameModel implements IBoardModel
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

	private ArrayList<IBoardObserver> observerList;
	
	public SameGameModel(int numCols, int numRows)
	{
		this.observerList = new ArrayList<IBoardObserver>();
		
		this.numColumns = numCols;
		this.numRows = numRows;
		
		this.rand = new Random(); // initialize RNG
		
		this.grid = new int[this.numRows][this.numColumns]; // initialize grid
		
		this.numColours = minDifficulty;
		this.randomizeGrid(this.numColours);
	}
	
	// Use observer pattern for notifying anyone interested of state updates
	//
    public void registerBoardObserver(IBoardObserver obj) 
    {
    	// TODO: verify it doesnt exist already in the list?
    	this.observerList.add(obj);
	}
    
    public void unregisterBoardObserver(IBoardObserver obj) 
    {
    	this.observerList.remove(obj);
    }
    
    //method to notify observers of change
    public void notifyObservers() 
    {
    	for (int i = 0; i < this.observerList.size(); i++)
    	{
    		this.observerList.get(i).update();
    	}
    }	
	
	public int getCurrentDifficulty()
	{
		return this.numColours;
	}
	
	public void increaseDifficulty()
	{
		if (this.numColours < this.maxDifficulty)
			this.numColours++;
		else
			this.numColours = this.minDifficulty; // wrap back around to easy
	}
	
	public void randomizeGrid(int numColours)
	{
		// set all cells in the grid to a random non-empty value
		
		Log.i("SameGame", " > Rebuilding grid with [" + numColours + "] colours");
		
		for (int r = 0; r < this.numRows; r++)
		{
			for (int c = 0; c < this.numColumns; c++)
			{
				int colour = rand.nextInt(numColours - 1) + 1; // this little bit actually requires knowing that emptyCellValue == 0, so it kind of sucks.
				this.setValueForCell(r, c, colour);
			}
		}
	}
	
	public int getNumRows()
	{
		return this.numRows;
	}
	
	public int getNumColumns()
	{
		return this.numColumns;
	}
	
	public int getNumCellsRemaining()
	{
		// debugging function
		int cellsRemaining = 0;
		
		for (int c = 0; c < this.numColumns; c++)
		{
			for (int r = 0; r < this.numRows; r++)
			{
				if (!this.cellIsEmpty(r,c))
					cellsRemaining++;
			}
		}
		
		return cellsRemaining;
	}
	
	public void writeGridToLog()
	{
		for (int r = this.numRows-1; r >= 0; r--)
		{
			StringBuilder rowString = new StringBuilder();
			for (int c = 0; c < this.numColumns; c++)
			{
				rowString.append(this.grid[r][c]);
			}
			Log.w("SameGame", rowString.toString());
		}
	}
	
	// this function should always be used instead of direct access to the grid so that I can easily change the implementation later if I want to for optimizations
	// the function call should be optimized away by the compiler I imagine.
	public int getValueForCell(int row, int col)
	{
		// TODO: this should throw an error for invalid row/col combinations
		if (row < 0 || row >= this.numRows || col < 0 || col >= this.numColumns) 
			return 0;

		return this.grid[row][col];
	}
	
	private void setValueForCell(int row, int col, int value)
	{
		
		// TODO: this should throw an error for invalid row/col combinations
		if (row < 0 || row >= this.numRows || col < 0 || col >= this.numColumns) return;
		
		this.grid[row][col] = value;
	}
	
	public boolean cellIsEmpty(int row, int col)
	{
		return getValueForCell(row, col) == emptyCellValue;
	}

	/* COLLAPSING FUNCTIONS */
	
	private void collapseColumnVertically(int col, int startingRow)
	{
		if (startingRow >= this.numRows)
			return;
		
		if (this.getValueForCell(startingRow, col) != emptyCellValue)
		{
			this.collapseColumnVertically(col, startingRow + 1); // this cell is not empty. continue with the cell above it
			return;
		}
		
		for (int currRow = startingRow + 1; currRow < this.numRows; currRow++)
		{
			// find a non empty cell above this one
			int cellVal = this.getValueForCell(currRow, col);
			if (cellVal != emptyCellValue)
			{
				// stick the value from that cell into this one
				this.setValueForCell(startingRow, col, cellVal);
				this.setValueForCell(currRow, col, emptyCellValue);
				
				this.collapseColumnVertically(col, startingRow); // recurse
				return;
			}
		}
	}
	
	private void collapseRows()
	{
		for (int i = 1; i <= this.numColumns; i++)
		{
			this.collapseColumnVertically(this.numColumns - i, 0);
		}
		
	}
	
	private boolean columnIsEmpty(int col)
	{
		// foolproof way is to iterate the entire column and check all cells.
		// i know however this will only be called after collapseColumns, so if the cell in the bottom row is empty, the entire col is empty
		
		return this.cellIsEmpty(0, col);
	}

	private void moveColumnXToColumnY(int x, int y)
	{
		for (int row = 0; row < this.numRows; row++)
		{
			this.setValueForCell(row, x, this.getValueForCell(row, y));
			this.setValueForCell(row, y, this.emptyCellValue);
		}
	}
	
	private void collapseColumnHorizontally(int col)
	{
		// if this column is empty, move all columns to the right of it over one column to the left
		
		if (col + 1 >= this.numColumns || col < 0) // column is invalid or furthest right -- can't move values to it
			return;
		else if (!this.columnIsEmpty(col)) // column isn't empty, don't shift columns into it
		{
			// advance to next column
			this.collapseColumnHorizontally(col + 1);
			return;
		}
		else
		{
			int firstNonEmptyCol = -1;
			
			for (int i = col + 1; i < this.numColumns; i++)
			{
				if (!this.columnIsEmpty(i))
				{
					firstNonEmptyCol = i;
					break;
				}
			}
			
			if (firstNonEmptyCol == -1)
				return; // no non empty cols to copy here, done.
			
			this.moveColumnXToColumnY(col, firstNonEmptyCol);
			this.collapseColumnHorizontally(col);
			return;
		}
	}
	
	private void collapseColumns()
	{
		// the thing this calls will recurse and deal with everything
		this.collapseColumnHorizontally(0);
	}
	
	public void collapse()
	{
		this.collapseRows();
		this.collapseColumns();
	}
	
	
	public boolean noMovesAvailable()
	{
		for (int c = 0; c < this.numColumns; c++)
		{
			for (int r = 0; r < this.numRows; r++)
			{
				if (this.cellHasAdjacentSameColourCell(r, c))
					return false;
			}
		}
		return true;
	}
	
	public boolean gridIsEmpty()
	{
		for (int c = 0; c < this.numColumns; c++)
		{
			if (!this.columnIsEmpty(c))
				return false;
		}
		return true;
	}
	
	/* CELL DESTROYING FUNCTIONS */
	
	public boolean cellHasAdjacentSameColourCell(int row, int col)
	{
		int cellVal = this.getValueForCell(row, col);
		
		// cell is empty. don't really want to know if adjacent cells are same colour. guess the function is named poorly!
		if (cellVal == this.emptyCellValue) 
			return false;
		
		// cell above
		if (row + 1 < this.numRows)
		{
			if (cellVal == this.getValueForCell(row + 1, col))
				return true;
		}
		
		// cell below
		if (row > 0)
		{
			if (cellVal == this.getValueForCell(row - 1, col))
				return true;
		}
		
		// cell to right
		if (col + 1 < this.numColumns)
		{
			if (cellVal == this.getValueForCell(row, col + 1))
				return true;	
		}
		
		// cell to left
		if (col > 0)
		{
			if (cellVal == this.getValueForCell(row, col - 1))
				return true;	
		}
		
		return false;
	}
	
	public void destroyAdjacentSameColourCells(int row, int col)
	{
		// this function recursively searches out from the specified cell, clearing all adjacent cells of that colour
		// this combined with the "Collapse" function form the meat of the game
		
		int cellVal = this.getValueForCell(row, col);
		this.setValueForCell(row, col, this.emptyCellValue);	
		
		// this can be brutal on the stack as it is recursing and splitting in 4 at once. thought the compiler would optimize the recursion but java sucks
		
		// cell above
		if (row + 1 < this.numRows)
		{
			if (cellVal == this.getValueForCell(row + 1, col))
				this.destroyAdjacentSameColourCells(row + 1, col);
		}
		
		// cell below
		if (row > 0)
		{
			if (cellVal == this.getValueForCell(row - 1, col))
				this.destroyAdjacentSameColourCells(row - 1, col);
		}
		
		// cell to right
		if (col + 1 < this.numColumns)
		{
			if (cellVal == this.getValueForCell(row, col + 1))
				this.destroyAdjacentSameColourCells(row, col + 1);
		}
		
		// cell to left
		if (col > 0)
		{
			if (cellVal == this.getValueForCell(row, col - 1))
				this.destroyAdjacentSameColourCells(row, col - 1);	
		}
	}

	public void destroyRow(int row)
	{
		for (int c = 0; c < this.numColumns; c++)
		{
			this.setValueForCell(row, c, this.emptyCellValue);
		}	
	}
	
	public void destroyCol(int col)
	{
		for (int r = 0; r < this.numRows; r++)
		{
			this.setValueForCell(r, col, this.emptyCellValue);
		}
	}
	
	public void destroy9x9Square(int centerRow, int centerCol)
	{	
		this.setValueForCell(centerRow, centerCol, this.emptyCellValue);
		this.setValueForCell(centerRow, centerCol+1, this.emptyCellValue);
		this.setValueForCell(centerRow, centerCol-1, this.emptyCellValue);
		this.setValueForCell(centerRow+1, centerCol, this.emptyCellValue);
		this.setValueForCell(centerRow+1, centerCol+1, this.emptyCellValue);
		this.setValueForCell(centerRow+1, centerCol-1, this.emptyCellValue);
		this.setValueForCell(centerRow-1, centerCol, this.emptyCellValue);
		this.setValueForCell(centerRow-1, centerCol+1, this.emptyCellValue);
		this.setValueForCell(centerRow-1, centerCol-1, this.emptyCellValue);
	}
	
}
