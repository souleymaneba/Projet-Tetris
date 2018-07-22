import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a Tetris board -- essentially a 2-d grid of booleans. Supports
 * tetris pieces and row clearing. Has an "undo" feature that allows clients to
 * add and remove pieces efficiently. Does not do any drawing or have any idea
 * of pixels. Instead, just represents the abstract 2-d board.
 */
public class Board {
	
	private int width;
	private int height;
	
	// The abstract representation of the 2-d board is stored via grid and gridBackup
	protected boolean[][] grid;
	private boolean[][] backupGrid;
	
	// wArray and wArrayBackup store information regarding how filled given rows are
	private int widths[];
	private int backupWidths[];
	
	// hArray and hArrayBackup store information regarding how filled given columns are
	protected int heights[];
	private int backupHeights[];
	
	private boolean DEBUG = true;
	private boolean committed;
	
	/**
	 * Creates an empty board of the given width and height measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;

		this.grid = new boolean[width][height];
		this.backupGrid = new boolean[width][height];
		
		this.widths = new int[height];
		this.backupWidths = new int[height];
		
		this.heights = new int[width];
		this.backupHeights = new int[width];

		this.committed = true;
	}
	
	//copie de la classe board
	
	public Board(Board board) {
		
		this.width=board.width;
		this.height = board.height;
		this.grid = this.copieArray(this.grid);
		this.backupGrid = this.copieArray(this.backupGrid);
		this.widths = copieArray1(this.widths,this.height);
		this.backupWidths = copieArray1(this.heights,this.width);
		this.heights = copieArray1(this.heights,this.width);
		this.backupHeights = copieArray1(this.widths,this.height);
		this.committed = true;
		
 	}
	//copie d'un array
	private boolean[][] copieArray(boolean b[][]){
		boolean res[][] = new boolean[this.width][this.height];
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b.length; j++){
				res[i][j] = b[i][j];
			}
		}
		return res;
	}
	
	//copie
	@SuppressWarnings("unused")
	private int[] copieArray1(int tab1[],int taille){
		int res[] = new int[taille];
		for(int i = 0; i < taille; i++){
				res[i] = tab1[i];
		}
		return res;
	}
	
	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns the max column height present in the board. For an empty board
	 * this is 0.
	 */
	public int getMaxHeight() {
	    // Recherche de maximum sur heights
		int maxH = heights[0];
		for(int i : heights){
			if(i > maxH){
				maxH = i;
			}
		}
		return maxH;
	}
	
	/* Method: printBoardState
	 * -----------------------
	 * A useful testing method to print the grid formatted as a board
	 */
	public void printBoardState() {
		System.out.println("PRINT BOARD");
		if (DEBUG) {
			System.out.println("Height " + height);
			System.out.println("Width " + width);
			
			for (int y = height-1; y >= 0; y--) {
				System.out.println("-----------------");
				for(int x = 0; x < width; x++) {
					System.out.print(" | " + grid[x][y] + " | ");
				}	
				System.out.println();
			}
		}
	}
	
	/**
	 * Given a piece and an x, returns the y value where the piece would come to
	 * rest if it were dropped straight down at that x.
	 * 
	 * <p>
	 * Implementation: use the skirt and the col heights to compute this fast --
	 * O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {
		
		if (x < 0 || x >= width) throw new RuntimeException("Cannot drop piece out of bounds");
		
		int firstStop = 0;
		List<Integer> skirtVals = piece.getSkirt();
		
		for(int currPieceX = 0; currPieceX < piece.getWidth(); currPieceX++) {
			int stop = getColumnHeight(x+currPieceX) - skirtVals.get(currPieceX);
			if (stop > firstStop) firstStop = stop;
		}
		
		return firstStop;
	}

	/**
	 * Returns the height of the given column -- i.e. the y value of the highest
	 * block + 1. The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
	    return heights[x];
	}

	/**
	 * Returns the number of filled blocks in the given row.
	 */
	public int getRowWidth(int y) {
	    return widths[y];
	}

	/**
	 * Returns true if the given block is filled in the board. Blocks outside of
	 * the valid width/height area always return true.
	 */
	public boolean getGrid(int x, int y) {
	    if(x < 0 || x>= this.width || y < 0 || y >= this.height){
	    	return true;
	    }
	    return grid[x][y];
	}

	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 * Attempts to add the body of a piece to the board. Copies the piece blocks
	 * into the board grid. Returns PLACE_OK for a regular placement, or
	 * PLACE_ROW_FILLED for a regular placement that causes at least one row to
	 * be filled.
	 * 
	 * <p>
	 * Error cases: A placement may fail in two ways. First, if part of the
	 * piece may falls out of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 * Or the placement may collide with existing blocks in the grid in which
	 * case PLACE_BAD is returned. In both error cases, the board may be left in
	 * an invalid state. The client can use undo(), to recover the valid,
	 * pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
	    if (!this.committed) {
	    	throw new RuntimeException("can only place object if the board has been commited");
	    }
	    this.committed = false;
	    
	    updateBackups();
	    
	    int pieceW = piece.getWidth();
	    int pieceH = piece.getHeight();

	    if(!inBounds(x, y, pieceW, pieceH)){
	    	return PLACE_OUT_BOUNDS;
	    }
	    
	    for (TPoint p : piece.getBody())
	    {
	    	if (this.grid[x+p.x][y+p.y])
	    		return PLACE_BAD;
	    }
	    
	    for(TPoint pt : piece.getBody()){
	    	int i = x + pt.x;
	    	int j = y + pt.y;
	    	
	    	this.grid[i][j] = true;
	    	if(j+1 > heights[i]){
	    		heights[i] = j + 1;
	    	}
	    	widths[j]++;
	    }
	    
	    int maxW = widths[0];
	    for(int i : widths){
	    	if(i > maxW){
	    		maxW = i;
	    	}
	    }
	    
	    return (maxW == this.width)? PLACE_ROW_FILLED : PLACE_OK;
	}

	/* Method: sanityCheck
	 * -------------------
	 * Checks the board for internal consistency -- used
	 * for debugging.
	 */
	public void sanityCheck() {
		if (DEBUG) {
			int max = 0;
		
			// check if the height array matches the board state
			for (int curCol = 0; curCol < width; curCol++) {
				int colHeight = height-1;
				while(colHeight >= 0 && !grid[curCol][colHeight]) colHeight--;
				colHeight++;
				if (colHeight > max) max = colHeight;
				//if (heights[curCol] != colHeight) throw new RuntimeException("heights[" + curCol + "] incorrect");
			}
		
			// check if the maxHeight matches the board's max height
			//if (max != maxHeight) throw new RuntimeException("maxHeight incorrect");
		
			// check if the width array matches the board state
			for (int curRow = 0; curRow < height; curRow++) {
				int numBlocks = 0;
				for(int curCol = 0; curCol < width; curCol++) {
					if (grid[curCol][curRow]) numBlocks++;
				}
				//if (numBlocks != widths[curRow]) throw new RuntimeException("widths[" + curRow + "] incorrect");
			}
		}
	}

	private boolean inBounds(int x, int y, int pW, int pH) {
		
		return (x >= 0 && x+pW <= width && y >= 0 && y+pH <= height);
	}

	private void updateBackups() {
		
		System.arraycopy(widths, 0, backupWidths, 0, height);
		System.arraycopy(heights, 0, backupHeights, 0, width);
		
		for (int curCol = 0; curCol < this.width; curCol++) {
			System.arraycopy(grid[curCol], 0, backupGrid[curCol], 0, this.height);
		}
		
	}

	/**
	 * Deletes rows that are filled all the way across, moving things above
	 * down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		int rowsCleared = 0;
		if (committed) updateBackups();
		committed = false;

		// iterates through the rows shifting each row down based on how many rows have been cleared
		for(int rowNum = 0; rowNum < height; rowNum++) {
			if(widths[rowNum] == width) {
				rowsCleared++; 
				
			} else if (rowsCleared > 0) {
				shiftRow(rowNum, rowsCleared);
			}
			
		}
		
		for (int i = height - rowsCleared; i < height; i++){
			//
			for(int j = 0; j < width; j++){
				grid[j][i] = false;
			}
			widths[i] = 0;
		}
		
		for (int y = 0; y < heights.length; y++){
			heights[y] -= rowsCleared;
		}
			
		return rowsCleared;
	}


	private void shiftRow(int rowNum, int i) {
		for (int colNum = 0; colNum < width; colNum++) {
			grid[colNum][rowNum-i] = grid[colNum][rowNum];
			grid[colNum][rowNum] = false;
		}
		widths[rowNum-i] = widths[rowNum];
		widths[rowNum] = 0;
	}

	/**
	 * Reverts the board to its state before up to one place and one
	 * clearRows(); If the conditions for undo() are not met, such as calling
	 * undo() twice in a row, then the second undo() does nothing. See the
	 * overview docs.
	 */
	public void undo() {
		if (!committed) {
			committed = true;
			revertToBackups();
			sanityCheck();
		}
	}

	/* Method: revertToBackups
	 * -----------------------
	 * Revert the current board data structures to the backup states.
	 */ 
	private void revertToBackups() {
		
		System.arraycopy(backupWidths, 0, widths, 0, height);
		System.arraycopy(backupHeights, 0, heights, 0, width);
		
		for(int curCol = 0; curCol < width; curCol++) {
			System.arraycopy(backupGrid[curCol], 0, grid[curCol], 0, height);
		}
	}

	/**
	 * Puts the board in the committed state.
	 */
	public void commit() {
		
	    this.committed = true;
	}

	/*
	 * Renders the board state as a big String, suitable for printing. This is
	 * the sort of print-obj-state utility that can help see complex state
	 * change over time. (provided debugging utility)
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = this.height - 1; y >= 0; y--) {
			buff.append('|');
			for (int x = 0; x < this.width; x++) {
				if (getGrid(x, y))
					buff.append('+');
				else
					buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x = 0; x < this.width + 2; x++)
			buff.append('-');
		return buff.toString();
	}

	// Only for unit tests
	protected void updateWidthsHeights() {
		Arrays.fill(this.widths, 0);

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				if (this.grid[i][j]) {
					this.widths[j] += 1;
					this.heights[i] = Math.max(j + 1, this.heights[i]);
				}
			}
		}
	}
	
}
