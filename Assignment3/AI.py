from PIL import Image
import Queue
import time
import copy
import heapq
import numpy as np
import random
import math
import pylab as plt
import numpy as np
import time

plt.ion()
im = plt.imread('terrain.png')
plotX = []
plotY = []
maxNumberOfSteps = 1

class State:
    cost = None
    parent = None
    x = None
    y = None
    direction = None
    numOfSteps = None

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


def genetic(origin, goal):

    plan = []
    plan.append((0,0))   
    for x in range(0,1000):
        plan.append(addPair())

    print len(plan)

    for iteration in range(0,3000):
        randomnum = random.random();
        if len(plan) > 0:
            if randomnum < .4:
                # Mutate
                randomIndex = random.randint(0,len(plan)-1)
                plan[randomIndex] = mutate(plan[randomIndex])
            elif randomnum < .5:
                plan.append(addPair())
            elif randomnum < .6:
                # Remove a random chromosome
                plan.pop(random.randint(0,len(plan)-1))
            elif len(plan) > 1:
                randomIndex1 = random.randint(0,len(plan)-1)
                randomIndex2 = random.randint(0,len(plan)-1)
                toRemove = tournament(plan, plan[randomIndex1],plan[randomIndex2],randomIndex1, randomIndex2)
                # print len(plan)
                # print plan[randomIndex1], round(evaluateChromosome(plan[0:randomIndex1], False),2)
                # print plan[randomIndex2], round(evaluateChromosome(plan[0:randomIndex2], False),2)
                # print 'Comparing', plan[randomIndex1], 'and', plan[randomIndex2], 'Removing:', toRemove
                plan.remove(toRemove)
                randomIndex1 = random.randint(0,len(plan)-1)
                randomIndex2 = random.randint(0,len(plan)-1)
                plan.append(breed(plan[randomIndex1],plan[randomIndex2]))
                # print len(plan)

    print len(plan)
    print evaluateChromosome(plan, True)

def breed(chromosome1,chromosome2):
    randomnum = random.random()
    if randomnum < .5:
        return (chromosome1[0], chromosome2[1])
    else:
        return (chromosome2[0], chromosome1[1])

# Returns the chromosome to kill
def tournament(plan, chromosome1, chromosome2):
    cost1 = evaluateChromosome(plan[0:index1], False)
    cost2 = evaluateChromosome(plan[0:index2], False)
    randomnum = random.random()
    if cost1 < cost2:
        better = chromosome1
        worse = chromosome2
    else:
        better = chromosome2
        worse = chromosome2

    # Kill the worse chromosome 90% of the time
    if randomnum < .9:
        return worse
    else:
        return better

# Mutate one of the angles.
# Mutate one of step counts.
def mutate(chromosome):
    randomnum = random.random()
    # print chromosome
    if randomnum < .33:
        newChromosome = (random.randint(0,3), chromosome[1])
    elif randomnum < .66:
        newChromosome = (chromosome[0],random.randint(0,maxNumberOfSteps))
    else:
        newChromosome = (random.randint(0,3),random.randint(0,maxNumberOfSteps))
    return newChromosome

def evaluateChromosome(plan, final):
    q = []
    q.append(State(0,None, 100,100))
    for i,gene in enumerate(plan):
        # print gene
        for j in range(0,gene[1]):
            s = State(None, None, q[len(q)-1].x, q[len(q)-1].y)
            if(gene[0]==0):
                state = State(None, s, s.x-1, s.y)
            elif(gene[0]==1):
                state = State(None, s, s.x+1, s.y)
            elif(gene[0]==2):
                state = State(None, s, s.x, s.y-1)
            elif(gene[0]==3):
                state = State(None, s, s.x, s.y+1)
            if not (state.x < 0 or state.x >= 500) and not (state.y < 0 or state.y >= 500):
                state.cost = getGreenColor(state.x,state.y)
                q.append(state)
    
    plotX = []
    plotY = []
    for gene in q:
        plotX.append(gene.x)
        plotY.append(gene.y)

    plt.imshow(im)
    plt.plot(plotX ,plotY, "x", color="red",marker=',')
    plt.draw()
    # plt.pause(1)
    plt.clf()

    if final == True:
        for gene in q:
            setPixelRed(gene.x, gene.y)

    
    return q[len(q)-1].cost + 500*(distance(q[len(q)-1].x,q[len(q)-1].y, 400, 400))

def addPair():
    angle = random.randint(0,3)
    steps = random.randint(1,maxNumberOfSteps)
    return (angle,steps)

def distance(x1,y1,x2,y2):
    return math.sqrt(math.pow((x2-x1),2)+math.pow((y2-y1),2))


def getGreenColor(x, y):
    return image.getpixel((x, y))[2]


def setPixelRed(x, y):
    image.putpixel((x, y), (255, 0, 0, 255))


def setPixelGreen(x, y):
    image.putpixel((x, y), (0, 255, 0, 255))


image = None
# for i in range(0,10):
image = Image.open("terrain.png")
origin = State(0,None, 100,100)
goal = State(0,None, 400,400)
genetic(origin,goal)
image.save('path.png', None)