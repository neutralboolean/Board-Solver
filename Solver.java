import java.lang.*;
import edu.princeton.cs.algs4.*;

/**
 * Solver.java
 * 
 * An immutable data type to solve, if possible, a given <code>Board</code>
 * object supplied at construction. Enables the output of the sequence of moves
 * necessary to convert the initial board into the goal board, else outputs
 * "unsolvable puzzle" if the <code>Board</code> cannot be solved.
 * 
 * Compilation: javac-algs4 Solver.java
 * Execution:   java-algs4 Solver file.txt
 */
public class Solver {
    private SearchNode currentNode;
    
    private class SearchNode implements Comparable<SearchNode>{
        private Board board;
        private int movesMade;
        private SearchNode prev;
        private int priority;
        
        SearchNode(Board b, int m, SearchNode p, int pr) {
            board = b;
            movesMade = m;
            prev = p;
            priority = pr;
        }
        
        // default comparison of the node is based on the node's priority
        public int compareTo(SearchNode sn) {
            if (this.priority > sn.priority) return 1;
            else if (this.priority < sn.priority) return -1;
            else return 0;
        }
    }
    
    /**************************************************************/
    
    // constructor for Solver class
    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial) {
        if (initial == null) throw new NullPointerException();
        if (!initial.isSolvable()) throw new IllegalArgumentException();
        
        currentNode = new SearchNode(initial, 0, null, initial.hamming());
        
        MinPQ<SearchNode> mpq = new MinPQ<SearchNode>();
        mpq.insert(currentNode);
        
        /* Determines a minimal path to the goal using MinPQ */
        while (!currentNode.board.isGoal()) {
            currentNode = mpq.delMin();
            Iterable<Board> iterBoard = currentNode.board.neighbors();
            
            for (Board b : iterBoard) {
                // attempts to weed out copies of previous boards from being
                // reinserted into the priority queue
                if (currentNode.prev != null
                        &&
                b.equals(currentNode.prev.board)) {
                    continue;
                }
                mpq.insert(new SearchNode(b,
                                     (currentNode.movesMade+1), currentNode, 
                                     (currentNode.movesMade + b.manhattan())));
            }
        }
    }
    
    /**************************************************************/
    
    /*Returns the minimum number of moves to solve initial board as determined
      while solving in the constructor. */
    public int moves() {
        return currentNode.movesMade;
    }
    
    /**************************************************************/
    
    /*Returns an Iterable object with all of the Boards from the initial board
      to the goal board. */
    public Iterable<Board> solution() {
        Stack<Board> stack = new Stack<Board>();
        for (SearchNode s = currentNode; s != null; s = s.prev) {
                 stack.push(s.board);
        }
        
        return stack;
    }
    
    /**************************************************************/
    // unit testing
    public static void main(String[] args) {

    // create initial board from file
    In in = new In(args[0]);
    int N = in.readInt();
    int[][] tiles = new int[N][N];
    for (int i = 0; i < N; i++)
        for (int j = 0; j < N; j++)
            tiles[i][j] = in.readInt();
    
    Board initial = new Board(tiles);

    // check if puzzle is solvable; if so, solve it and output solution
    if (initial.isSolvable()) {
        
        Solver solver = new Solver(initial);
        StdOut.println("Minimum number of moves = " + solver.moves());
        for (Board board : solver.solution())
            StdOut.println(board);
    }

    // if not, report unsolvable
    else {
        StdOut.println("Unsolvable puzzle");
    }
}
}