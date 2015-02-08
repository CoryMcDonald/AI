import pdb
from PIL import Image
from Queue import PriorityQueue
import time
import math
import copy
import heapq
import cProfile

ImageObject = None
perimeter = [[0, 0], [1, 0], [2, 0], [3, 0], [4, 0], [5, 0], [6, 0], [7, 0], [8, 0], [9, 0], [0, 1], [1, 1], [2, 1], [7, 1], [8, 1], [9, 1], [0, 2], [1, 2], [8, 2], [9, 2], [0, 3], [4, 3], [9, 3], [0, 4], [3, 4],
             [4, 4], [9, 4], [0, 5], [9, 5], [0, 6], [9, 6], [0, 7], [1, 7], [8, 7], [9, 7], [0, 8], [1, 8], [2, 8], [7, 8], [8, 8], [9, 8], [0, 9], [1, 9], [2, 9], [3, 9], [4, 9], [5, 9], [6, 9], [7, 9], [8, 9], [9, 9]]


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
    heapq.heappush(q, (startState.cost, startState))
    pops = 0
    while len(q) > 0:
        validBoards=0
        pops = pops + 1
        s = heapq.heappop(q)[1]
        # print s.pieces[0][0] == (5,1)
        if s.pieces[0] == [4,-2]:
            parentState = s
            saveImage(s.pieces)
            print 'POPS', pops
            return s;

        validMoves = []

        pieces = s.pieces
        # print s.pieces

        # saveImage(pieces)
        # time.sleep(3)
        # 10 times
        for piecesIndex,piece in enumerate(pieces):
            for x in range(-1,2):
                for y in range (-1, 2):
                    if not abs(x) == abs(y):
                        # print ('Moving piece' + )
                        tempPieceOffset = list(pieces)
                        tempPieceOffset[piecesIndex] = [tempPieceOffset[piecesIndex][0]+x, tempPieceOffset[piecesIndex][1]+y]
                        if checkValid(tempPieceOffset):
                            validBoards = validBoards+1
                            validMoves.append(tempPieceOffset)

        for move in validMoves:
            j = 1
            strused = ''.join(map(str, move))
            if strused not in explored:
                c = puzzleState(s.cost+1, s, move)
                heapq.heappush(q, (c.cost, c))
                explored[strused] = True

        print pops,s.cost

    raise Exception("There is no path to the goal")

def checkValid(piecesOffset):
    # print piecesOffset
    used = [[False for x in range(10)] for x in range(10)]
    for xy in perimeter:
        used[xy[0]][xy[1]] = True
    for i, row in enumerate(pieces):
        for j, col in enumerate(row):
            x = pieces[i][j][0] + piecesOffset[i][0]
            y = pieces[i][j][1] + piecesOffset[i][1]
            if(x >= 0 and y >= 0 and x<=10 and y<=10 and not used[x][y] == True):
                used[x][y] = True
            else:
                return False
    return True

    # usedBefore = []
    # for i, piece in enumerate(thisPiece):
    #     for coordinate in piece:
    #         if coordinate in perimeter or coordinate in usedBefore:
    #             return False22
    #         usedBefore.append(coordinate)
    # return True

def getGreenColor(x, y):
    return ImageObject.getpixel((x, y))[2]


def setPixelRed(x, y):
    ImageObject.putpixel((x, y), (255, 0, 0, 255))


def h(x, y):
    return 14 * (abs(400 - x) + abs(400 - y))


def setPixelGreen(x, y):
    ImageObject.putpixel((x, y), (0, 255, 0, 255))


def saveImage(piecesOffset):
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
                        ImageObject.putpixel((x + w, y + z), (255,248,225, 255))
            else:
                for w in range(0, pixelSize):
                    for z in range(0, pixelSize):
                        ImageObject.putpixel((x + w, y + z), (0, 0, 0, 255))

    for i, piece in enumerate(pieces):
        for coordinate in piece:
            x = (coordinate[0]+piecesOffset[i][0]) * pixelSize
            y = (coordinate[1]+piecesOffset[i][1]) * pixelSize
            for w in range(0, pixelSize):
                for z in range(0, pixelSize):
                    ImageObject.putpixel((x + w , y + z), colors[i])
            used[coordinate[0]][coordinate[1]] = True
    ImageObject.save('path.png', None)

# Initalizing Board
used = [[False for x in range(10)] for x in range(10)]
pieces = [None] * 11  # Initializing
pieces[0] = [[1, 3], [2, 3], [1, 4], [2, 4]]  # red
pieces[1] = [[1, 5], [1, 6], [2, 6]]          # light,green
pieces[2] = [[2, 5], [3, 5], [3, 6]]          # lavender
pieces[3] = [[4, 7], [5, 7], [5, 8]]          # yellow
pieces[4] = [[6, 7], [7, 7], [6, 8]]          # brown
pieces[5] = [[3, 7], [3, 8], [4, 8]]          # pink
pieces[6] = [[5, 4], [4, 5], [5, 5], [5, 6]]  # darkcyan
pieces[7] = [[6, 4], [6, 5], [7, 5], [6, 6]]  # darkgreen
pieces[8] = [[8, 5], [7, 6], [8, 6]]          # lightcyan
pieces[9] = [[6, 2], [5, 3], [6, 3]]          # blue
pieces[10] = [[5, 1], [6, 1], [5, 2]]         # orange

piecesOffset = [[0,0]] * 11


# for xy in perimeter:
    # used[xy[0]][xy[1]] = True

colors = [(244, 67, 54, 255), (139, 195, 74, 255), (156, 39, 176, 255), (255, 235, 59, 255), (121, 85, 72, 255),
          (233, 30, 99, 255), (0, 151, 167, 255), (76, 175, 80, 255), (178, 235, 242, 255), (33, 150, 243, 255), (255, 152, 0, 255)]


# saveImage(pieces)

goalPiece = [None] * 11  # Initializing
goalPiece[0] = [(1, 3), (2, 3), (1, 4), (2, 4)]  # (red)


# saveImage(pieces)

origin = puzzleState(0.0, None, piecesOffset)
goal = puzzleState(0.0, None, goalPiece)

print breadth_first_search(origin, goal).cost
