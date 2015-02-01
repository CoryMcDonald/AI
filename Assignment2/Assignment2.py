from PIL import Image
from Queue import PriorityQueue
import time
import math
import copy
import heapq
import cProfile

ImageObject = None
perimeter = [(0, 0), (1, 0), (2, 0), (3, 0), (4, 0), (5, 0), (6, 0), (7, 0), (8, 0), (9, 0), (0, 1), (1, 1), (2, 1), (7, 1), (8, 1), (9, 1), (0, 2), (1, 2), (8, 2), (9, 2), (0, 3), (4, 3), (9, 3), (0, 4), (3, 4),
             (4, 4), (9, 4), (0, 5), (9, 5), (0, 6), (9, 6), (0, 7), (1, 7), (8, 7), (9, 7), (0, 8), (1, 8), (2, 8), (7, 8), (8, 8), (9, 8), (0, 9), (1, 9), (2, 9), (3, 9), (4, 9), (5, 9), (6, 9), (7, 9), (8, 9), (9, 9)]


class MyState:
    cost = None
    parent = None
    x = None
    y = None

    def __init__(self, cost, par, x, y):
        self.cost = cost
        self.parent = par
        self.x = x
        self.y = y

    def isEqual(self, state):
        if self.x == state.x and self.y == state.y:
            return True
        else:
            return False


class puzzleState:
    cost = None
    parent = None
    pieces = None

    def __init__(self, cost, par, pieces):
        self.cost = cost
        self.parent = par
        self.pieces = pieces

    def isEqual(self, state):
        if self.pieces == state.pieces:
            return True
        else:
            return False


def breadth_first_search(startState, goalState):
    q = []
    explored = dict()
    # append =
    heapq.heappush(q, (startState.cost, startState))
    pops = 0
    while len(q) > 0:
        pops = pops + 1
        s = heapq.heappop(q)[1]
        # if(i % 5000 < 1000):
        # setPixelGreen(s.x, s.y)
        # print pops
        # print s.pieces[0][0] == (5,1)
        if s.pieces[0][0] == (5,1):
            parentState = s
            saveImage(s.pieces)
            print 'POPS', pops
            return s;
        # if s.isEqual(goalState):
        #     parentState = s
        #     saveImage(s.pieces)
        #     print 'POPS', pops
        #     return s

        # setPixelGreen(s.x, s.y)
        print pops, s.cost
        if pops % 5000 == 0 or pops==1:
            saveImage(s.pieces)
        # time.sleep(2)
        validMoves = []

        pieces = s.pieces
        for piecesIndex,piece in enumerate(pieces):
            # Try and move piece up, down, left, right
            for x in range(-1,2):
                for y in range (-1, 2):
                    if not abs(x) == abs(y):
                        tempPiece = list(piece)
                        for i,coordinate in enumerate(piece):
                            tempPiece[i] = (piece[i][0]+x, piece[i][1]+y)
                        # print piece, tempPiece, x, y
                        temp = list(pieces)
                        temp[piecesIndex] = tempPiece
                        # print temp, pieces
                        if checkValid(temp):
                            validMoves.append(temp)
                            # print temp
        
        # print len(validMoves)
        for move in validMoves:
            # Here for speed purposes
            j = 0 
            used = [0 for x in range(100)]
            for xy in perimeter:
                used[10 * xy[0] + xy[1]] = 0
            for piece in move:
                for coordinate in piece:
                    used[10 * coordinate[0] + coordinate[1]] = j
                j = j +1

            strused = ''.join(map(str, used))
            if strused not in explored:
                c = puzzleState(s.cost+1, s, move)
                heapq.heappush(q, (c.cost, c))
                explored[strused] = True

        # time.sleep(5)
    raise Exception("There is no path to the goal")

def checkValid(thisPiece):
    used = [[False for x in range(10)] for x in range(10)]
    for xy in perimeter:
        used[xy[0]][xy[1]] = True
    for i, piece in enumerate(thisPiece):
        for coordinate in piece:
            if used[coordinate[0]][coordinate[1]] == True:
                return False
            used[coordinate[0]][coordinate[1]] = True
    return True

def getGreenColor(x, y):
    return ImageObject.getpixel((x, y))[2]


def setPixelRed(x, y):
    ImageObject.putpixel((x, y), (255, 0, 0, 255))


def h(x, y):
    return 14 * (abs(400 - x) + abs(400 - y))


def setPixelGreen(x, y):
    ImageObject.putpixel((x, y), (0, 255, 0, 255))


def saveImage(pieces):
    used = [[False for x in range(10)] for x in range(10)]
    for xy in perimeter:
        used[xy[0]][xy[1]] = True
    ImageObject = Image.new(mode='RGB', size=(300, 300), color=0)
    pixelSize = 30
    for i, row in enumerate(used):
        for j, col in enumerate(row):
            x = i * pixelSize
            y = j * pixelSize
            if used[i][j] == False:
                for w in range(0, pixelSize):
                    for z in range(0, pixelSize):
                        ImageObject.putpixel(
                            (x + w, y + z), (255,248,225, 255))
            else:
                for w in range(0, pixelSize):
                    for z in range(0, pixelSize):
                        ImageObject.putpixel((x + w, y + z), (0, 0, 0, 255))

    for i, piece in enumerate(pieces):
        for coordinate in piece:
            x = coordinate[0] * pixelSize
            y = coordinate[1] * pixelSize
            for w in range(0, pixelSize):
                for z in range(0, pixelSize):
                    ImageObject.putpixel((x + w, y + z), colors[i])
            used[coordinate[0]][coordinate[1]] = True
    ImageObject.save('path.png', None)

# Initalizing Board
used = [[False for x in range(10)] for x in range(10)]
pieces = [None] * 11  # Initializing
pieces[0] = [(1, 3), (2, 3), (1, 4), (2, 4)]  # (red)
pieces[1] = [(1, 5), (1, 6), (2, 6)]  # (light,green
pieces[2] = [(2, 5), (3, 5), (3, 6)]  # (lavender)
pieces[3] = [(4, 7), (5, 7), (5, 8)]  # (yellow)
pieces[4] = [(6, 7), (7, 7), (6, 8)]  # (brown)
pieces[5] = [(3, 7), (3, 8), (4, 8)]  # (pink)
pieces[6] = [(5, 4), (4, 5), (5, 5), (5, 6)]  # (darkcyan
pieces[7] = [(6, 4), (6, 5), (7, 5), (6, 6)]  # (darkgreen
pieces[8] = [(8, 5), (7, 6), (8, 6)]  # (lightcyan
pieces[9] = [(6, 2), (5, 3), (6, 3)]  # (blue)
pieces[10] = [(5, 1), (6, 1), (5, 2)]  # (orange)

for xy in perimeter:
    used[xy[0]][xy[1]] = True

colors = [(244, 67, 54, 255), (139, 195, 74, 255), (156, 39, 176, 255), (255, 235, 59, 255), (121, 85, 72, 255),
          (233, 30, 99, 255), (0, 151, 167, 255), (76, 175, 80, 255), (178, 235, 242, 255), (33, 150, 243, 255), (255, 152, 0, 255)]


# saveImage(pieces)

goalPiece = [None] * 11  # Initializing
goalPiece[0] = [(1, 3), (2, 3), (1, 4), (2, 4)]  # (red)
goalPiece[1] = [(1, 5), (1, 6), (2, 6)]  # (light,green
goalPiece[2] = [(2, 5), (3, 5), (3, 6)]  # (lavender)
goalPiece[3] = [(4, 7), (5, 7), (5, 8)]  # (yellow)
goalPiece[4] = [(6, 7), (7, 7), (6, 8)]  # (brown)
goalPiece[5] = [(3, 7), (3, 8), (4, 8)]  # (pink)
goalPiece[6] = [(5, 4), (4, 5), (5, 5), (5, 6)]  # (darkcyan
goalPiece[7] = [(6, 4), (6, 5), (7, 5), (6, 6)]  # (darkgreen
goalPiece[8] = [(8, 5), (7, 6), (8, 6)]  # (lightcyan
goalPiece[9] = [(6, 2), (5, 3), (6, 3)]  # (blue)
goalPiece[10] = [(4, 1), (5, 1), (4, 2)]  # (orange)


saveImage(pieces)

origin = puzzleState(0.0, None, pieces)
goal = puzzleState(0.0, None, goalPiece)

print breadth_first_search(origin, goal).cost
