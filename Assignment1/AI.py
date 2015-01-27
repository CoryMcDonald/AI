from PIL import Image
from Queue import PriorityQueue
import time
import copy
import heapq

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


def breadth_first_search(startState, goalState):
    q = []
    explored = set()
    heapq.heappush(q, (startState.cost, startState))
    i = 0
    while len(q) > 0:
        i = i + 1
        s = heapq.heappop(q)[1]
        if(i % 5000 < 1000):
            setPixelGreen(s.x, s.y)

        if s.isEqual(goalState):
            parentState = s
            while parentState.parent is not None:
                setPixelRed(parentState.x, parentState.y)
                parentState = parentState.parent
            print 'Iterations',i
            return s

        up = MyState(None, s, s.x, s.y - 1)
        down = MyState(None, s, s.x, s.y + 1)
        left = MyState(None, s, s.x - 1, s.y)
        right = MyState(None, s, s.x + 1, s.y)

        borders = [up, down, left, right]

        for c in borders:
            if not (c.x < 0 or c.x >= 500) and not (c.y < 0 or c.y >= 500):
                if (c.x, c.y) not in explored:
                    c.parent = s
                    c.cost = s.cost + getGreenColor(c.x, c.y)
                    heapq.heappush(q, (c.cost, c))
                    explored.add((c.x, c.y))
        # time.sleep(5)
    raise Exception("There is no path to the goal")


def getGreenColor(x, y):
    return ImageObject.getpixel((x, y))[2]

def setPixelRed(x, y):
    ImageObject.putpixel((x, y), (255, 0, 0, 255))


def setPixelGreen(x, y):
    ImageObject.putpixel((x, y), (0, 255, 0, 255))

# Maze2 - 120,241 ; 120, 0
# Maze3 580, 1115 580

ImageObject = Image.open("terrain.png")
used = [[False for i in range(ImageObject.size[0] + 1)]
        for j in range(ImageObject.size[1] + 1)]
origin = MyState(0.0, None, 100, 100)
goal = MyState(0.0, None, 400, 400)
print breadth_first_search(origin, goal).cost
# ImageObject.resize((ImageObject.size[0]/2, ImageObject.size[1]/2)).save('maze3_solved.png', None)
ImageObject.save('path.png', None)
