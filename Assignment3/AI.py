from PIL import Image
from state import State
import random
import math
import time

maxNumberOfSteps = 5
image = Image.open("terrain.png")


def main():
    evolutionaryAlgorithm()

def evolutionaryAlgorithm():

    population = []
    for x in range(0, 30):
        # Initialize each population to start at (0,0)
        population.append([(0, 0)])

    for iteration in range(0, 30000):
        # Minor optimizations
        if iteration == 10000:
            maxNumberOfSteps = 2
        if iteration == 22000:
            maxNumberOfSteps = 5
        if iteration == 32000:
            maxNumberOfSteps = 2

        randPercent = random.random()
        if len(population) > 0:
            randomIndex = random.randint(0, len(population) - 1)
            #Mutates a random chromosome
            if randPercent < .1 or (iteration > 10000 and randPercent < .3):
                randomIndex2 = random.randint(0, len(population[randomIndex]) - 1)
                population[randomIndex][randomIndex2] = mutate(population[randomIndex][randomIndex2])
            #Append a random chromosome
            elif randPercent < .6 and iteration < 27000:
                population[randomIndex].append(addPair())
            # Remove a random chromosome
            elif randPercent < .7 and iteration < 27500:
                if len(population[randomIndex]) > 1:
                    population[randomIndex].pop(random.randint(0, len(population[randomIndex]) - 1))
            #Challenge mode
            elif len(population[randomIndex]) > 1:
                randomIndex2 = random.randint(0, len(population) - 1)
                # Don't want to have tournament with self, that would be dumb
                if randomIndex != randomIndex2:
                    toRemove = tournament(population[randomIndex], population[randomIndex2])
                    if population[randomIndex] == toRemove:
                        winningIndex = randomIndex2
                    else:
                        winningIndex = randomIndex
                    population.remove(toRemove)
                    # Breed with the alpha, becacuse that's how it works in real life
                    randomIndex = random.randint(0, len(population) - 1)
                    population.append(
                        breed(population[winningIndex - 1], population[randomIndex]))

            # Printing out the results for the graph
            if iteration % 100 == 0:
                lowestCost = 99999999
                for chromo in population:
                    cost = evaluateChromosome(chromo)
                    if cost < lowestCost:
                        lowestCost = cost
                print iteration, lowestCost

# Mutate one of the angles orone of step counts.
def mutate(chromosome):
    randomnum = random.random()
    if randomnum < .33:
        newChromosome = (random.randint(0, 3), chromosome[1])
    elif randomnum < .66:
        newChromosome = (chromosome[0], random.randint(0, maxNumberOfSteps))
    else:
        newChromosome = (random.randint(0, 3), random.randint(0, maxNumberOfSteps))
    return newChromosome

# Adds a random angle and a random number of steps between 1 and the maxNumber
def addPair():
    angle = random.randint(0, 3)
    steps = random.randint(1, maxNumberOfSteps)
    return (angle, steps)


def breed(chromosome1, chromosome2):
    randomnum = random.random()
    if len(chromosome1) > 0 and len(chromosome2) > 0:
        halfChromosome1 = len(chromosome1) / 2
        halfChromosome2 = len(chromosome2) / 2
        if randomnum < .5:
            return chromosome1[0:halfChromosome1] + chromosome2[halfChromosome2:len(chromosome2)]
        else:
            return chromosome2[0:halfChromosome2] + chromosome1[halfChromosome1:len(chromosome1)]


# Returns the chromosome to kill
def tournament(chromosome1, chromosome2):
    cost1 = evaluateChromosome(chromosome1)
    cost2 = evaluateChromosome(chromosome2)
    if cost1 < cost2:
        better = chromosome1
        worse = chromosome2
    else:
        better = chromosome2
        worse = chromosome1

    # Kill the worse chromosome 90% of the time
    randomnum = random.random()
    if randomnum < .9:
        return worse
    else:
        return better


def evaluateChromosome(chromosome):
    q = []
    s = None
    q.append(State(0, None, 100, 100))
    for gene in chromosome:
        for j in range(0, gene[1]):
            s = q[-1]
            if(gene[0] == 0):
                state = State(None, s, s.x - 1, s.y)
            elif(gene[0] == 1):
                state = State(None, s, s.x + 1, s.y)
            elif(gene[0] == 2):
                state = State(None, s, s.x, s.y - 1)
            elif(gene[0] == 3):
                state = State(None, s, s.x, s.y + 1)
            if not (state.x < 0 or state.x >= 500) and not (state.y < 0 or state.y >= 500):
                state.cost = s.cost + image.getpixel((state.x, state.y))[2]
                q.append(state)

    cost = q[-1].cost + 500 * (distance(q[-1].x, q[-1].y, 400, 400))

    return cost


def distance(x1, y1, x2, y2):
    return math.sqrt(math.pow((x2 - x1), 2) + math.pow((y2 - y1), 2))


if __name__ == "__main__":
    main()