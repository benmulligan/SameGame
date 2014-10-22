package com.example.samegame;

public interface IBoardModel
{
	public int getNumRows();
	public int getNumColumns();
	public int getValueForCell(int row, int col);	
}
