import java.lang.*;
import edu.princeton.cs.algs4.*;

/**
 * Board.java
 * 
 * An immutable data type that stores a board described by a 2D int array.
 * 
 * Has various functionality to check the validity of a <code>Board</code>
 * object, and to calculate the priority of the object via one of two methods.
 * 
 * Compilation: javac-algs4 Board.java
 * Execution:   java-algs4 Board
 */

public class Board {
    private int[][] boardTiles;
    private int N;             // size of the array (N x N)   
    private int[] zeroCoord;   // stores the initial position of blank space
                               // stored as {row, col}
    
    public Board(int[][] tiles) {
        if (tiles == null) { throw new NullPointerException(); }

        N = tiles.length;
        boardTiles = new int[N][N];
        
        // makes defensive copy of input array
        for(int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                boardTiles[row][col] = tiles[row][col];
                // stores the position of blank space
                if (tiles[row][col] == 0) {
                    zeroCoord = new int[] {row, col};
                }
            }
        }
    }
    
    //return tile at row i, column j (or 0 if blank)
    public int tileAt(int i, int j) {
        if (i < 0 || i >= N) throw new IndexOutOfBoundsException();
        if (j < 0 || j >= N) throw new IndexOutOfBoundsException();
        
        return boardTiles[i][j];
    }
    
    // returns board size: N
    public int size() {
        return N;
    }
    /*************************************************************************/
    /**** Priority calculation functions *************************************/
    
    // returns the number of tiles out of natural (ascending) order
    public int hamming() {
        int result = 0;
        for(int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                // breaks to avoid applying normal logic to the blank space
                if (row == N-1 && col == N-1) break;
                if ((convertTo1D(col, row)+1) != tileAt(row, col)) {
                    result++;
                }
            }
        }
        // special check for the goal blank space
        if (tileAt(N-1, N-1) != 0) { result++; }
        
        return result;
    }
    
    //returns the sum of the Manhattan distances between current tiles and goal
    public int manhattan() {
        int result = 0;
        for(int row = 0; row < N; row++){
            for (int col = 0; col < N; col++) {
                int x = 0, y = 0;

                if (row == N-1 && col == N-1) { 
                    if (tileAt(N-1, N-1) == 0) { break; }
                }
                
                if ((convertTo1D(col, row)+1) != tileAt(row, col)) {
                    if (tileAt(row, col) == 0) continue;
                    
                    int[] coord2D = convertTo2D((tileAt(row, col)-1));
                
                //determine correct order for subtraction to ensure positives
                //calculate x displacement
                    if (coord2D[0] > row) {
                        x = coord2D[0]-row;
                    }
                    else if (coord2D[0] < row){
                        x = row - coord2D[0];
                    }
                //and y displacement
                    if (coord2D[1] > col) {
                        y = coord2D[1] - col;
                    }
                    else if (coord2D[1] < col) {
                        y = col - coord2D[1];
                    }
                }
                result = result + x + y;
            }
        }
        
        return result;
    }
    /*************************************************************************/
    /**** Boolean checks *****************************************************/
    
    // is this board the goal board?
    public boolean isGoal() {
       for(int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (row == N-1 && col == N-1) {
                    break;
                }
                if ((convertTo1D(col, row)+1) == tileAt(row, col)) {
                    continue;
                }
                
                return false;
            }
        }
        return (tileAt(N-1, N-1) == 0);
    }
    
    // Returns a boolean:`true` if the current Board object can be solved based
    // on a number of criteria, else `false`.
    public boolean isSolvable() {
        int inversions = 0;
        int blankRow = 0;
        //iterate through the 2D array
        //at each position, iterate completely through the array from that
           //point until the end
        
        for(int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                //iterates further up the array to compare for inversions
                int j = row, k = col;
                while(j < N){
                    //checks at the top if we've reached the end of the array
                        //end condition for the while-loop
                        if ((j == N-1) && (k == N-1)) break;
                        if (tileAt(j, k) == 0) {
                            blankRow = j;
                        }
                    //if the positions equals 0, don't count as an inversion    
                        if ((tileAt(j, k) != 0)
                            &&
                            tileAt(row, col) > tileAt(j, k)) {
                        inversions++;
                        }
                    
                    k = (k+1) % N;
                    if (k == 0) j++;
                }
            }
        }
        
        //differing boolean conditions based on the array's mathematical parity
        if (((N-1) % 2) == 0) {
            return ((inversions % 2) == 0);
        }
        else {
            return (((inversions + blankRow) % 2) == 0);
        }
    }
    
    /* Returns `true` if Object y is equal to `this` Object. */
    public boolean equals(Object y) {
        if (y == this) return true;
        if (y == null) return false;
        if (y.getClass() != this.getClass() ) return false;
        
        Board that = (Board) y;
        if(this.size() != that.size()) return false;
        
        //checks linearly through the array for equality
        for (int row = 0; row < N; row++){
            for(int col = 0; col < N; col++) {
                if (this.tileAt(row, col) != that.tileAt(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }
    /*************************************************************************/
    /*************************************************************************/
    /* Returns an Iterable object populated with neighbors to this Board.
     * 
     * Neighbors are defined as `Board`s identical to this except for the 
     * position of the blank space (0) swapped with an adjacent tile.
     */
    public Iterable<Board> neighbors() {
        
        int[][] newTiles = new int[N][N];
        Stack<Board> stack = new Stack<Board>();
        
        for (int i = 0; i < 4; i++) {
            int moveBlank = 0;
            //set up where it is possible for the blank space to move
            switch (i) {
                case 0:
                    if ((zeroCoord[0] - 1) >= 0) moveBlank = -1;
                    break;
                case 1:
                    if ((zeroCoord[0] + 1) < N) moveBlank = 1;
                    break;
                case 2:
                    if ((zeroCoord[1] - 1) >= 0) moveBlank = -1;
                    break;
                case 3:
                    if ((zeroCoord[1] + 1) < N) moveBlank = 1;
                    break;
                default:
                    break;
            };
            //skips over the rest, if the piece cannot move
            if (moveBlank == 0) continue;
            
            /*Copies the board's tiles in a new array for initialization in new 
            `Board` objects */
            for (int row = 0; row < N; row++){
                for(int col = 0; col < N; col++) {
                    newTiles[row][col] = boardTiles[row][col];
                }
            }
            
            //Swaps the blank space with an adjacent tile
            int temp;
            switch (i) {
                case 0:
                    temp =  newTiles[zeroCoord[0] + moveBlank][zeroCoord[1]];
                    newTiles[zeroCoord[0] + moveBlank][zeroCoord[1]] = 0;
                    newTiles[zeroCoord[0]][zeroCoord[1]] = temp;
                    break;
                case 1:
                    temp = newTiles[zeroCoord[0] + moveBlank][zeroCoord[1]];
                    newTiles[zeroCoord[0] + moveBlank][zeroCoord[1]] = 0;
                    newTiles[zeroCoord[0]][zeroCoord[1]] = temp;
                    break;
                case 2:
                    temp = newTiles[zeroCoord[0]][zeroCoord[1] + moveBlank];
                    newTiles[zeroCoord[0]][zeroCoord[1] + moveBlank] = 0;
                    newTiles[zeroCoord[0]][zeroCoord[1]] = temp;
                    break;
                case 3:
                    temp = newTiles[zeroCoord[0]][zeroCoord[1] + moveBlank];
                    newTiles[zeroCoord[0]][zeroCoord[1] + moveBlank] = 0;
                    newTiles[zeroCoord[0]][zeroCoord[1]] = temp;
                    break;
                default:
                    break;
            };
            
            //if control goes this far, push a new Board with `newTiles`
            stack.push(new Board(newTiles));
        }

        return stack;
    }
    /*************************************************************************/
    /* Returns a string representation of the `Board object: an output of the 
     * boardTiles array.
     * 
     * Makes use of class StringBuilder.
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(N + "\n");
        
        for (int row = 0; row < N; row++){
            for(int col = 0; col < N; col++) {
                str.append(String.format("%2d ", tileAt(row, col)));
            }
            str.append("\n");
        }
        
        return str.toString();
    }
    /*************************************************************************/
    /**** Private helpers ****************************************************/    
    //converts a 2D array coordinate pair into its 1D equivalent
        // used to compare array position vs goal-board tile value,
        // e.g. index 8 should hold value 9
    private int convertTo1D(int x, int y) {
        return ((y*N)+x);
    }
    
    //converts a 1D array index into a 2D array coordinate pair: {row, col}
    private int[] convertTo2D(int index) {
        int row = index % N;
        int col = (index - row) / N;
        int[] result = new int[] {col, row};
        return result;
    }
    /*************************************************************************/

    // unit testing 
    public static void main(String[] args) {
        int[][] iArr = new int[][] {
            {1, 2, 3},
            {0, 4, 6},
            {8, 5, 7}
        };
        int[][] iArr2 = new int[][] {
            {1, 2, 3},
            {4, 0, 5},
            {7, 8, 6}
        };
        int[][] iArr3 = new int[][]{
            {1, 2, 3, 4},
            {5, 6, 0, 8},
            {9, 10, 7, 11},
            {13, 14, 15, 12}
        };
        int[][] iArr4 = new int[][] {
            {1, 0, 2, 3},
            {5, 4, 7, 6},
            {8, 9, 10, 11},
            {12, 13, 14, 15}
        };
        
        Board b1 = new Board(iArr);
        Board b2 = new Board(iArr2);
        Board b3 = new Board(iArr3);
        Board b4 = new Board(iArr4);
        
        StdOut.println("Equivalency tests:");
        StdOut.println(b1.equals(b2));    
        StdOut.println(b2.equals(b1));    
        StdOut.println(b3.equals(b2));    
        StdOut.println(b2.equals(b3));    
        StdOut.println();
        StdOut.println("b1 is the goal: " + b1.isGoal());    
        StdOut.println("b2 is the goal: " + b2.isGoal());    
        StdOut.println("b3 is the goal: " + b3.isGoal());    
        StdOut.println();
        StdOut.println("b1 is solvable: " + b1.isSolvable());
        StdOut.println("b2 is solvable: " + b2.isSolvable());
        StdOut.println("b3 is solvable: " + b3.isSolvable());
        StdOut.println("b4 is solvable: " + b4.isSolvable());
        
        StdOut.println();
        
        StdOut.println("Priority function tests:");
        StdOut.println("b1's Hamming distance = " + b1.hamming());
        StdOut.println("b1's Manhattan distance = " + b1.manhattan());
        StdOut.println("b2's Hamming distance = " + b2.hamming());
        StdOut.println("b2's Manhattan distance = " + b2.manhattan());
        StdOut.println("b3's Hamming distance = " + b3.hamming());
        StdOut.println("b3's Manhattan distance = " + b3.manhattan());
        StdOut.println("b4's Hamming distance = " + b4.hamming());
        StdOut.println("b4's Manhattan distance = " + b4.manhattan());
        
        StdOut.println();
        StdOut.println("Board outputs:");
        StdOut.print("b1 "); StdOut.println(b1);
        StdOut.print("b2 "); StdOut.println(b2);
        StdOut.print("b3 "); StdOut.println(b3);
        StdOut.print("b4 "); StdOut.println(b4);
        
        StdOut.println();
        StdOut.println("Neighbor() testing for b1");
        Iterable<Board> st = b1.neighbors();
        
        for (Board b : st) {
            StdOut.println(b);
        }
        
        StdOut.println("Neighbor() testing for b4");
        Iterable<Board> st2 = b4.neighbors();
        
        for (Board b : st2) {
            StdOut.println(b);
        }
    }
}