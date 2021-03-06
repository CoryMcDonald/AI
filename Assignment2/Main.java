import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
public class Main {

    private static Coordinate[] perimeter;
    private static Coordinate[][] pieces;
    public static void main(String[] args) {

        perimeter = new Coordinate[]{ new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(2, 0), new Coordinate(3, 0), new Coordinate(4, 0), new Coordinate(5, 0), new Coordinate(6, 0), new Coordinate(7, 0), new Coordinate(8, 0), new Coordinate(9, 0), new Coordinate(0, 1), new Coordinate(1, 1), new Coordinate(2, 1), new Coordinate(7, 1), new Coordinate(8, 1), new Coordinate(9, 1), new Coordinate(0, 2), new Coordinate(1, 2), new Coordinate(8, 2), new Coordinate(9, 2), new Coordinate(0, 3), new Coordinate(4, 3), new Coordinate(9, 3), new Coordinate(0, 4), new Coordinate(3, 4), new Coordinate(4, 4), new Coordinate(9, 4), new Coordinate(0, 5), new Coordinate(9, 5), new Coordinate(0, 6), new Coordinate(9, 6), new Coordinate(0, 7), new Coordinate(1, 7), new Coordinate(8, 7), new Coordinate(9, 7), new Coordinate(0, 8), new Coordinate(1, 8), new Coordinate(2, 8), new Coordinate(7, 8), new Coordinate(8, 8), new Coordinate(9, 8), new Coordinate(0, 9), new Coordinate(1, 9), new Coordinate(2, 9), new Coordinate(3, 9), new Coordinate(4, 9), new Coordinate(5, 9), new Coordinate(6, 9), new Coordinate(7, 9), new Coordinate(8, 9), new Coordinate(9, 9) };

        pieces = new Coordinate[11][4];
        pieces[0] = new Coordinate[]{new Coordinate(1, 3), new Coordinate(2, 3), new Coordinate(1, 4), new Coordinate(2, 4)};
        pieces[1] = new Coordinate[]{new Coordinate(1, 5), new Coordinate(1, 6), new Coordinate(2, 6)};
        pieces[2] = new Coordinate[]{new Coordinate(2, 5), new Coordinate(3, 5), new Coordinate(3, 6)};
        pieces[3] = new Coordinate[]{new Coordinate(4, 7), new Coordinate(5, 7), new Coordinate(5, 8)};
        pieces[4] = new Coordinate[]{new Coordinate(6, 7), new Coordinate(7, 7), new Coordinate(6, 8)};
        pieces[5] = new Coordinate[]{new Coordinate(3, 7), new Coordinate(3, 8), new Coordinate(4, 8)};
        pieces[6] = new Coordinate[]{new Coordinate(5, 4), new Coordinate(4, 5), new Coordinate(5, 5), new Coordinate(5, 6)};
        pieces[7] = new Coordinate[]{new Coordinate(6, 4), new Coordinate(6, 5), new Coordinate(7, 5), new Coordinate(6, 6)};
        pieces[8] = new Coordinate[]{new Coordinate(8, 5), new Coordinate(7, 6), new Coordinate(8, 6)};
        pieces[9] = new Coordinate[]{new Coordinate(6, 2), new Coordinate(5, 3), new Coordinate(6, 3)};
        pieces[10] = new Coordinate[]{new Coordinate(5, 1), new Coordinate(6, 1), new Coordinate(5, 2)};

        Coordinate[] offset = new Coordinate[] { new Coordinate(0,0), new Coordinate(0,0), new Coordinate(0,0),
                new Coordinate(0,0), new Coordinate(0,0), new Coordinate(0,0), new Coordinate(0,0), new Coordinate(0,0),
                new Coordinate(0,0), new Coordinate(0,0), new Coordinate(0,0)};

        State origin = new State(null, 0, offset);

        search(origin, new Coordinate(4, -2), false);
        search(origin, new Coordinate(4, -2), true);

    }

    private static State search(State origin, Coordinate goal, boolean useHeuristic)
    {
        PriorityQueue<State> queue = new PriorityQueue<State>();
        HashMap<String, State> used = new HashMap<String, State>();

        queue.add(origin);
        int pops =0;
        while(queue.size() > 0) {
            State s = queue.remove();
            pops++;

            if (s.pieceOffset[0].equals(goal)) {
                if(useHeuristic == true) {
                    System.out.println("astar2=" + pops);
                }else
                {
                    System.out.println("bfs2=" + pops);
                }
                return s;
            }

            ArrayList<Coordinate[]> validMoves = new ArrayList<Coordinate[]>();

            for (int i = 0; i < s.pieceOffset.length; i++) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (Math.abs(x) != Math.abs(y))
                        {
                            Coordinate[] copyOfPieceOffset = new Coordinate[s.pieceOffset.length];
                            System.arraycopy(s.pieceOffset, 0, copyOfPieceOffset, 0, copyOfPieceOffset.length);
                            copyOfPieceOffset[i] = new Coordinate(copyOfPieceOffset[i].x+x,copyOfPieceOffset[i].y+y);

                            if(checkValid(copyOfPieceOffset))
                            {
                                validMoves.add(copyOfPieceOffset);
                            }
                        }
                    }
                }
            }
            for (Coordinate[] moves : validMoves)
            {
                String string = "";
                for(Coordinate m : moves)
                    string += m.x + " " + m.y + " ";

                int cost = s.cost + 1;
                State mystate;
                if(used.containsKey(string))
                {
                    if(cost < (mystate=used.get(string)).cost)
                    {
                        mystate.cost = cost;
                        if(useHeuristic)
                            mystate.heuristicAndCost = cost +  heuristic(mystate.pieceOffset[0], goal);
                        else
                            mystate.heuristicAndCost = cost;

                        mystate.parent = s;
                        queue.add(mystate);
                        used.put(string, mystate);
                    }
                }else
                {
                    mystate = new State(s, s.cost + 1, moves);
                    if(useHeuristic)
                        mystate.heuristicAndCost = cost +  heuristic(mystate.pieceOffset[0], goal);
                    else
                        mystate.heuristicAndCost = cost;
                    queue.add(mystate);
                    used.put(string, mystate);
                }
            }
        }

        return new State(null,-1, new Coordinate[] { new Coordinate(0,0 )});
    }

    private static int heuristic(Coordinate piece, Coordinate Goal) {
        return (Math.abs(Goal.x-piece.x) + Math.abs(Goal.y-piece.y));
    }

    public static boolean checkValid(Coordinate[] pieceOffset)
    {
        boolean used[][] = new boolean[10][10];
        for(Coordinate perim : perimeter)
        {
            used[perim.x][perim.y] = true;
        }
        for(int i =0; i<pieces.length; i++) {
            for(int j=0;j< pieces[i].length; j++) {
                int x = pieces[i][j].x + pieceOffset[i].x;
                int y = pieces[i][j].y + pieceOffset[i].y;
                if(x >= 0 && y >= 0 && x<=10 && y<=10 &&  !used[x][y])
                {
                    used[x][y] = true;
                }else
                {
                    return false;
                }
            }
        }
        return true;
    }
}
class Coordinate
{
    public int x;
    public int y;
    //    public String Color;
    public Coordinate(int x, int y)
    {
        this.x = x;
        this.y = y;
//        this.Color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;

        Coordinate that = (Coordinate) o;

        if (x != that.x) return false;
        if (y != that.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "(" +
                "" + x +
                ", " + y +
                ')';
    }
}
class State implements Comparable<State>
{
    int cost;
    int heuristicAndCost;
    State parent;
    Coordinate[] pieceOffset;

    public State(State parent, int cost, Coordinate[] pieces)
    {
        this.cost = cost;
        this.parent = parent;
        this.pieceOffset = pieces;
    }

    @Override
    public int compareTo(State state) {
        if(heuristicAndCost > state.heuristicAndCost) {
            return 1;
        }
        else if(heuristicAndCost == state.heuristicAndCost)
            return 0;

        return -1;
    }
}