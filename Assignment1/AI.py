from PIL import Image
from Queue import PriorityQueue
import time

ImageObject = None
distance = {}


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

    def has_already_been_reached(self):
        checkParent = self.parent
        while checkParent is not None:
            if checkParent.x == self.x and checkParent.y == self.y or self.x < 0 or self.y < 0 or self.x >= 500 or self.y >= 500:
                return True
            else:
                checkParent = checkParent.parent
        return False


def transition_cost(s, c):
    return getGreenColor(c.x, c.y)

def exists(q, item):
    while not q.empty():
        prio, s = q.get()
        if(s == item): 
            return True
    return False
def breadth_first_search(startState, goalState):
    q = PriorityQueue(0)
    q.put((startState.cost, startState))
    i = 0
    while not q.empty():
        i = i + 1
        prio, s = q.get()
        if(i % 5000 < 1000):
            setPixelGreen(s.x, s.y)

        if s.parent and s.parent.x and s.parent.y:
            print('currently on ' +  str(s.x) + ',' + str(s.y) + '. parent is ' + str(s.parent.x) + ',' + str(s.parent.y) + ' cost is ' + str(s.cost))

        if s.isEqual(goalState):
            return s

        up = MyState(None, s, s.x, s.y - 1)
        down = MyState(None, s, s.x, s.y + 1)
        left = MyState(None, s, s.x - 1, s.y)
        right = MyState(None, s, s.x + 1, s.y)

        borders = [up, down, left, right]
        for c in borders:
            if not c.has_already_been_reached():
                c.parent = s
                c.cost = s.cost + transition_cost(s, c)
                q.put((c.cost, c))
            else:
                print (str(c.x) + ',' + str(c.y) + ' - has been used')
        # time.sleep(1)
    raise Exception("There is no path to the goal")


def getGreenColor(x, y):
    return ImageObject.getpixel((x, y))[2]


def setPixelGreen(x, y):
    currentPixel = list(ImageObject.getpixel((x, y)))
    currentPixel[0] = 0
    currentPixel[1] = 255
    currentPixel[2] = 0
    ImageObject.putpixel((x, y), tuple(currentPixel))
    # ImageObject.close();
    # 0=alpha, 1=blue, 2=green, 3=red


ImageObject = Image.open("terrain.png")
origin = MyState(0.0, None, 100, 100)
goal = MyState(0.0, None, 102, 102)
print breadth_first_search(origin, goal).cost
ImageObject.save('path.png', None)


# for x in range(0, 300):
#     setPixelGreen(200, x)
#     for y in range(x, 200):
#       setPixelGreen(x,y)
