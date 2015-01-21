from Queue import PriorityQueue
import time
import copy
import heapq


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
    heapq.heappush(q, (startState.cost, startState))
    i = 0
    while len(q) > 0:
        i = i + 1
        s = heapq.heappop(q)[1]
        # if(i % 5000 < 1000):
        #     setPixelGreen(s.x, s.y)

        if s.isEqual(goalState):
            parentState = s
            while parentState.parent is not None:
                setPixelRed(parentState.x, parentState.y)
                parentState = parentState.parent
            return s

        up = MyState(None, s, s.x, s.y - 1)
        down = MyState(None, s, s.x, s.y + 1)
        left = MyState(None, s, s.x - 1, s.y)
        right = MyState(None, s, s.x + 1, s.y)

        borders = [up, down, left, right]
        for c in borders:
            if not used[c.x][c.y] and not (c.x <= 0 or c.x >= ImageObject.size[0]) and not (c.y <= 0 or c.y >= ImageObject.size[1]):
                used[c.x][c.y] = True
                c.parent = s
                c.cost = s.cost + getGreenColor(c.x, c.y)
                heapq.heappush(q, (c.cost, c))

    raise Exception("There is no path to the goal")

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

puzzle = 	'##########' + \
		    '###  aa###' + \
		    '##   a9 ##' + \
		    '#00 #99  #' + \
		    '#00##67  #' + \
		    '#12266778#' + \
		    '#112 6788#' + \
		    '## 53344##' + \
		    '###5534###' + \
		    '##########'

