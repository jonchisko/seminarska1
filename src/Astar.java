

// imports
import Rules.Chessboard;
import Rules.Rules;
import Rules.Move;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.UnsupportedOperationException;
import java.security.SecureRandom;
import java.util.*;


public class Astar {
    /*

    A* algorithm implementation with all of its necessary components.

    pseudocode:
    -> given start position find the optimal path from start to goal
    1. at start position generate all possible moves from start to "next level"
    2. evaluate children (with a heuristict, cost(state) = path to state + h(state)
        and put them in some sort of priority queue
    3. pick the lowest scoring child and before expanding check if goal node
        if goal: end
        else: repeat expansion and 2,3
    -> dont forget to remember the path
     */


    private static String getPath() throws java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("Not implemented");
    }


    private static String findGoal(Chessboard initialState) throws java.lang.UnsupportedOperationException {
        //throw new java.lang.UnsupportedOperationException("Not implemented");

        // Chess state comparator
        Comparator<Chessboard> chessStateComparator = (chess1, chess2) -> {
            return Astar.getF(chess1) - Astar.getF(chess2);
        };

        // heap where evaluated children are stored, waiting to be visited
        PriorityQueue<Chessboard> heap = new PriorityQueue<>(chessStateComparator);
        // it gives a move that was X state with MOVE to given STATE
        HashMap<Chessboard, Move> stateMove = new HashMap<>();

        heap.add(initialState);

        // game status
        int gameStatus;
        // legal moves
        ArrayList<Move> legalMoves;
        // g path
        int g;
        // heuristic path
        int h;
        // do this while until heap is empty or number of moves reached 0
        while(!heap.isEmpty()){
            // pop minimum evaluated state
            Chessboard bestNode = heap.poll();
            gameStatus = bestNode.getGameStatus();
            // the state that was chosen for expansion -> check if goal - check mate, the number of moves left must be 0
            if(gameStatus == 2 && bestNode.getMovesLeft() == 0){
                // it is goal
            }
            // if node is "remi", dont expand or if no moves left
            if(gameStatus == 3 || bestNode.getMovesLeft() == 0){
                continue;
            }
            // if we made it to here, expand node - get children
            // get legal moves
            legalMoves = bestNode.getMoves();
            // make new states with legal moves
            for(Move ele : legalMoves){
                // make move
                bestNode.makeMove(ele);
                // copy this chess state to child
                Chessboard child = bestNode.copy();
                // compute value of f function for created child
                // TODO:look above
                // add child to heap
                heap.add(child);
                // add child and move to hashMap, add to hashmap states as key and a move that made it
                stateMove.put(child, ele);
                // reverse move back to initial bestNode
                bestNode.reverseMove(ele);
            }

        }


        return "";


    }


    private static int getG(Chessboard state) throws java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("Not implemented");
    }

    private static int getH(Chessboard state) throws java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("Not implemented");
    }

    private static int getF(Chessboard state) throws java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("Not implemented");
    }

    // transpositions, you want to know if state was already  searched
    // use some hashing that is faster than just hashing fen notation
    private static long[][] initiZobrist(){
        SecureRandom sr = new SecureRandom(); // I cannot use Random, because it uses 48 bit seed ...
        long[][] table = new long[64][12];
        for(int i = 0; i < 64; i++){
            for(int j = 0; j < 12; j++){
                // set bytes to random
                // table[i][j] = random_bytes;
                table[i][j] = sr.nextLong();
            }
        }
        return table;
    }

    private static long valueZobrist(Chessboard state, long[][] table){
        long hash = 0;
        int[][] board = state.getBoard();
        int globalMove = 0;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                // if diffrent than 0, chess figure is in this field
                if(board[i][j] != 0){
                    globalMove = i*8 + j;
                    // figures go from -6 to -1 for one player
                    // for the other from 1 to 5
                    // i have to move them from 0 to 5 and from 6 to 11
                    // because the Zobrist table is from 0...11 or size of 12
                    int figureIndex = board[i][j] < 0 ? board[i][j] + 6 : board[i][j] + 5;
                    hash = hash ^ table[globalMove][figureIndex];
                }
            }
        }
        return hash;
    }

    /*private static long updateZobrist(long hash, Move m, Chessboard state, long[][] table){
        String[] move = m.toString().split("-");
        int z = state.getBoard();
        int posI = Character.getNumericValue(move[0].charAt(1));
        int posJ = Character.getNumericValue(move[1].charAt(1));
        hash = hash ^ table[posI][]
    }*/

    // define different heuristic functions ...



    // to suppress potential warnings on standard out
    @SuppressWarnings("unchecked")
    public static void main(String[] args){

        // file were the beginning state is written
        // String fileName = args[0];
        if (args.length == 0){
            throw new Error("No args");
        }
        File f = new File(args[0]);
        // read the state from file
        String currentBoardFEN;
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            currentBoardFEN = br.readLine();
            // if it fails, it closes br, no need for finally
        }catch(java.io.FileNotFoundException e){
            System.out.println("File not found");
            System.out.println("Error: " + e.getMessage());
            throw new Error("No state");
        }catch(Exception e){
            System.out.println("Some error: " +e.getMessage());
            throw new Error("No state");
        }

        // initialize chessboard
        Chessboard currentState = Chessboard.getChessboardFromFEN(currentBoardFEN);


    }

}
