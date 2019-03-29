

// imports
import Rules.Chessboard;
import Rules.Rules;
import Rules.Move;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private Chessboard startState;
    private ArrayList<int[]> enemyKing;
    private HashMap<String, Integer> fScores = new HashMap<>();
    private HashMap<Long, String> zobrist2hash = new HashMap<>();

    public Astar(Chessboard initialState){
        this.startState = initialState;
        this.enemyKing = Astar.enemyKing(initialState);
    }

    private static String getPath(HashMap<String, Move> hashMoves, Chessboard endNode, Chessboard startNode)  {
        // returns moves
        // Chessboard currNode = endNode;
        StringBuilder sb = new StringBuilder();
        Move move;
        String startFEN = startNode.getFEN();
        String endFEN = endNode.getFEN();
        while(!endFEN.equals(startFEN)){
            move = hashMoves.get(endFEN);
            sb.insert(0, hashMoves.get(endFEN).toString()+";");
            //sb.append(";");
            endNode.reverseMove(move);
            endFEN = endNode.getFEN();
        }
        return sb.length() == 0 ? "" : sb.toString().substring(0, sb.length()-1);
    }


    private String findGoal(Chessboard initialState) {
        long[][] init_zobrist = initiZobrist();

        // Chess state comparator
        Comparator<Chessboard> chessStateComparator = (chess1, chess2) -> {
            return this.getF(chess1) - this.getF(chess2);
        };

        // heap where evaluated children are stored, waiting to be visited
        PriorityQueue<Chessboard> heap = new PriorityQueue<>(chessStateComparator);
        // it gives a move that was X state with MOVE to given STATE
        HashMap<String, Move> stateMove = new HashMap<>();
        // already seen elements
        //HashSet<String> seen = new HashSet<>();

        heap.add(initialState);
        Chessboard endState = null;
        // legal moves
        ArrayList<Move> legalMoves;
        // do this while until heap is empty or number of moves reached 0
        while(!heap.isEmpty()) {
            // pop minimum evaluated state
            Chessboard bestNode = heap.poll();
            // if we made it to here, expand node - get children
            // get legal moves
            legalMoves = bestNode.getMoves();
            // make new states with legal moves
            for (Move ele : legalMoves) {
                // make move
                bestNode.makeMove(ele);
                // copy this chess state to child
                //Chessboard child = bestNode.copy();
                //System.out.println(bestNode.getFEN() + " " + bestNode.getGameStatus());
                String hashed = bestNode.getFEN(); //valueZobrist(bestNode, init_zobrist);
                if(bestNode.getGameStatus() == 1 ||
                        bestNode.getGameStatus() == 2 && bestNode.getMovesLeft() != 0||
                        bestNode.getGameStatus() == 3 ||
                        stateMove.containsKey(hashed)){
                    // leave this node
                    //seen.add(hashed); // if not yet in
                    bestNode.reverseMove(ele);
                    //System.out.println("Nope");
                    continue;
                }
                if(bestNode.getGameStatus() == 2 && bestNode.getMovesLeft() == 0){
                    // if checkmate and no moves, this is gut child
                    stateMove.put(bestNode.getFEN(), ele);
                    endState = bestNode;
                    break;
                }
                // compute value of f function for created child
                // TODO:look above
                // add child to heap
                if(!stateMove.containsKey(hashed)) {
                    heap.add(bestNode.copy());
                    // add child and move to hashMap, add to hashmap states as key and a move that made it
                    stateMove.put(bestNode.getFEN(), ele);
                    // reverse move back to initial bestNode
                    //seen.add(hashed);
                }
                bestNode.reverseMove(ele);
            }
            if(endState != null){
                break; // the loop
            }

        }
        if(endState != null){
            //System.out.println(endState.getFEN());
            return Astar.getPath(stateMove, endState ,initialState);
        }
        return "No Solution Found";
    }

    private static ArrayList<int[]> enemyKing(Chessboard state){
        int player = state.getColor();
        // you are searching for the opposite king, so opposite colour
        int king = -6;
        if(player < 0){
            king = 6;
        }
        int[][] board = state.getBoard();
        ArrayList<int[]> kingPos = new ArrayList<>();
        int x, y;
        for(int indeks = 0; indeks < board.length*board.length; indeks++){
            y = indeks / board.length;
            x = indeks % board.length;
            if(board[y][x] == king) {
                kingPos.add(new int[]{y-1,x-1});
                kingPos.add(new int[]{y-1,x});
                kingPos.add(new int[]{y-1,x+1});
                kingPos.add(new int[]{y,x-1});
                kingPos.add(new int[]{y,x+1});
                kingPos.add(new int[]{y+1,x-1});
                kingPos.add(new int[]{y+1,x});
                kingPos.add(new int[]{y+1,x+1});
                break;
            }
        }
        return kingPos;
    }


    private int pokritost(Chessboard state){
        int score = 0;
        for(int[] field:this.enemyKing){
            if(field[0] >= 0 && field[1] >= 0){
                score  += Rules.isCovered(state.getBoard(), field[0], field[1], state.getColor()) ? 1 : 0;
            }
        }
        return -score;
    }

    private static int manhattanDistance(int[] pos1, int[] pos2){
        int xDif = Math.abs(pos1[0] - pos2[0]);
        int yDif = Math.abs(pos1[1] - pos2[1]);
        return xDif + yDif;
    }

    private int getMin(int[] pos){
        int min = 1000;
        int d;
        for(int[] field:this.enemyKing){
            d = Astar.manhattanDistance(field, pos);
            if(min > d){
                min = d;
            }
        }
        return min;
    }

    private int manhattanHeuristic(Chessboard state){
        int player = state.getColor();
        int[][] board = state.getBoard();
        int score = 0;
        int fig;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                fig = board[i][j];
                if(player < 0 && fig < 0){
                    score += this.getMin(new int[]{i, j});
                }
                if(player > 0 && fig > 0){
                    score += this.getMin(new int[]{i, j});
                }
            }
        }
        return score;
    }

    private int getPromotion(Chessboard state){
        return 0;
    }


    private int getH(Chessboard state) {
        return this.pokritost(state);
    }

    private int getF(Chessboard state) {
        int fscore;
        String fen = state.getFEN();
        if(this.fScores.containsKey(fen)){
            fscore = this.fScores.get(fen);
        }else{
            fscore = this.getH(state);
            this.fScores.put(fen, fscore);
        }
        return fscore + 1*state.getMovesLeft(); //+ this.manhattanHeuristic(state);
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
        Astar alg = new Astar(currentState);
        String solution = alg.findGoal(currentState);
        System.out.println(solution);

        /*long t1,t2;
        for(int i = 1; i <= 60; i++){
            String fileName = String.valueOf(i)+".txt";
            f = new File("/Users/jonskoberne/Downloads/progressive_checkmates/"+fileName);
            try(BufferedReader br = new BufferedReader(new FileReader(f))){
                System.out.println("File: "+i);
                currentState = Chessboard.getChessboardFromFEN(br.readLine());
                alg = new Astar(currentState);
                t1 = System.nanoTime();
                System.out.println(alg.findGoal(currentState));
                System.out.println("Time: "+ (System.nanoTime()-t1)/1000000000);
            }catch(java.io.FileNotFoundException e){
                System.out.println(e.getMessage());
            }catch(Exception e){
                System.out.println("lol"+e.getMessage());
            }
        }*/


    }

}
