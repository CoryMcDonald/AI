import pylab as plt
import numpy as np
import time

plt.ion()
im = plt.imread('terrain.png')
# implot = plt.imshow(im)

plotX = []
plotY = []

for i in range(0,100):
    plotX.append(i)
    plotY.append(i)

# for i in range(0,3):
plt.imshow(im)
plt.plot(plotX ,plotY, "x", color="red",marker=',')
plt.draw()
plt.pause(1)
plt.clf()
    
