from PIL import Image
from Queue import PriorityQueue
import time
import math
import copy
import heapq
import cProfile

ImageObject = None
used = []

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


def breadth_first_search(startState, goalState, heuristic):
    q = []
    explored = {}
    heapq.heappush(q, (startState.cost, startState))
    i = 0
    while len(q) > 0:
        i = i + 1
        s = heapq.heappop(q)[1]
        # if(i % 5000 < 1000):
            # setPixelGreen(s.x, s.y)

        if s.isEqual(goalState):
            parentState = s
            # while parentState.parent is not None:
            #     setPixelRed(parentState.x, parentState.y)
            #     parentState = parentState.parent
            if heuristic == False:
                print 'bfs1=' + str(i)
            else:
                print 'astar1=' + str(i)
            return s

        up = MyState(None, s, s.x, s.y - 1)
        down = MyState(None, s, s.x, s.y + 1)
        left = MyState(None, s, s.x - 1, s.y)
        right = MyState(None, s, s.x + 1, s.y)

        borders = [up, down, left, right]

        for c in borders:
            if not (c.x < 0 or c.x >= 500) and not (c.y < 0 or c.y >= 500):
                new_cost = s.cost + getGreenColor(c.x, c.y) 
                if (c.x, c.y) in explored:
                    # Reduces interations but increases 
                    if ((c.x,c.y) in explored and new_cost < explored[(c.x,c.y)].cost):
                        for j, item in enumerate(q):
                            if item[1].x == c.x and item[1].y == c.y:
                                q[j][1].cost = new_cost
                                break
                else:
                    c.parent = s
                    c.cost = new_cost
                    if heuristic == True:
                        heapq.heappush(q, (c.cost+h(c.x,c.y), c))
                    else:
                        heapq.heappush(q, (c.cost, c))
                    explored[(c.x, c.y)] = c;
               
        # time.sleep(5)
    raise Exception("There is no path to the goal")


def getGreenColor(x, y):
    return ImageObject.getpixel((x, y))[2]

def setPixelRed(x, y):
    ImageObject.putpixel((x, y), (255, 0, 0, 255))

def h(x,y):
    return  14 * (abs(400-x) + abs(400-y))

def setPixelGreen(x, y):
    ImageObject.putpixel((x, y), (0, 255, 0, 255))



ImageObject = Image.open("terrain.png")
used = [[False for i in range(ImageObject.size[0] + 1)]
        for j in range(ImageObject.size[1] + 1)]
origin = MyState(0.0, None, 100, 100)
goal = MyState(0.0, None, 400, 400)

breadth_first_search(origin, goal, False)
breadth_first_search(origin, goal, True)
ImageObject.save('path.png', None)