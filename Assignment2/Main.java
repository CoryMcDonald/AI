import java.util.*;

public class Main {

    private static Coordinate[] perimeter;

    public static void main(String[] args) {
        perimeter = new Coordinate[]{ new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(2, 0), new Coordinate(3, 0), new Coordinate(4, 0), new Coordinate(5, 0), new Coordinate(6, 0), new Coordinate(7, 0), new Coordinate(8, 0), new Coordinate(9, 0), new Coordinate(0, 1), new Coordinate(1, 1), new Coordinate(2, 1), new Coordinate(7, 1), new Coordinate(8, 1), new Coordinate(9, 1), new Coordinate(0, 2), new Coordinate(1, 2), new Coordinate(8, 2), new Coordinate(9, 2), new Coordinate(0, 3), new Coordinate(4, 3), new Coordinate(9, 3), new Coordinate(0, 4), new Coordinate(3, 4), new Coordinate(4, 4), new Coordinate(9, 4), new Coordinate(0, 5), new Coordinate(9, 5), new Coordinate(0, 6), new Coordinate(9, 6), new Coordinate(0, 7), new Coordinate(1, 7), new Coordinate(8, 7), new Coordinate(9, 7), new Coordinate(0, 8), new Coordinate(1, 8), new Coordinate(2, 8), new Coordinate(7, 8), new Coordinate(8, 8), new Coordinate(9, 8), new Coordinate(0, 9), new Coordinate(1, 9), new Coordinate(2, 9), new Coordinate(3, 9), new Coordinate(4, 9), new Coordinate(5, 9), new Coordinate(6, 9), new Coordinate(7, 9), new Coordinate(8, 9), new Coordinate(9, 9) };

        Coordinate[][] pieces = new Coordinate[11][4];
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

        State origin = new State(null, 0, pieces);
        System.out.println(search(origin).cost);
    }
    private static State search(State origin)
    {
        Queue<State> queue = new LinkedList<State>();
        Hashtable<String, Coordinate[][]> used = new Hashtable<String, Coordinate[][]>();
        Coordinate goal = new Coordinate(5,1);

        queue.add(origin);
        while(queue.size() > 0) {

            State s = queue.remove();
            if (s.pieces[0][0].x  == goal.x && s.pieces[0][0].y == goal.y) {
                return s;
            }

            ArrayList<Coordinate[][]> validMoves = new ArrayList<Coordinate[][]>();

            for (int i = 0; i < s.pieces.length; i++) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (Math.abs(x) != Math.abs(y)) {
                            Coordinate[] tempPiece = new Coordinate[s.pieces[i].length];
                            for (int j = 0; j < s.pieces[i].length; j++) {
                                tempPiece[j] = new Coordinate(s.pieces[i][j].x + x, s.pieces[i][j].y + y);
                            }
                            if(checkValid(s.pieces, i, tempPiece))
                            {
                                Coordinate[][] copy = new Coordinate[11][4];
                                System.arraycopy(s.pieces, 0, copy, 0, s.pieces.length);
                                copy[i] = tempPiece;

                                validMoves.add(copy);
                            }
//                            System.out.println();
                        }
                    }
                }
            }
            if(s.cost % 10 == 0)
                System.out.println(s.cost);
            for (Coordinate[][] move : validMoves)
            {
                StringBuilder moveString = new StringBuilder();
                for(Coordinate[] coordinateArray : move)
                {
                    for(Coordinate coordinate : coordinateArray)
                    {
                        moveString.append(coordinate.x + " " + coordinate.y);
                    }
                }
                if(!used.containsKey(moveString.toString()))
                {
                    queue.add(new State(s, s.cost + 1, move));
                    used.put(moveString.toString(), move);
                }
            }
//            break;

        }

        return new State(null,-1, new Coordinate[][] { new Coordinate[] { new Coordinate(0,0 )}});
    }
    public static boolean checkValid(Coordinate[][] pieces, int currentPieceIndex, Coordinate[] pieceToCheck)
    {
        for(Coordinate coordinate : pieceToCheck)
        {
            for(int i =0; i<pieces.length; i++) {
                if (i != currentPieceIndex) {
                    for(int j =0; j<pieces[i].length; j++) {
                        if (pieces[i][j].x == coordinate.x && pieces[i][j].y == coordinate.y)
                            return false;
                    }
                    for(int j =0; j< perimeter.length; j++)
                    {
                        if(perimeter[j].x == coordinate.x && perimeter[j].y == coordinate.y)
                            return false;
                    }
                }
            }

        }
//        System.out.println("Returning true");
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
}
class State
{
    int cost;
    State parent;
    Coordinate[][] pieces;

    public State(State parent, int cost, Coordinate[][] pieces)
    {
        this.cost = cost;
        this.parent = parent;
        this.pieces = pieces;
    }

}