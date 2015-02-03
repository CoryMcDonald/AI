import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit




file = open('Data.csv', 'r')

x = []
y = []
for line in file:
    temp = line.split(',')
    x.append(float(temp[0].strip()))
    y.append(float(temp[1].strip()))

print len(x)
print len(y)


# yn = y + 0.2*np.random.normal(size=len(x))

def func(x, a, b, c):
    return a * np.exp(-b * x) + c


popt, pcov = curve_fit(func, x, y)

# plt.plot(x, y, 'ro')
# plt.axis([0, len(x), 0, 60])
# plt.show()
plt.figure()
plt.plot(x, y, 'ro', label="Original Noised Data")
# plt.plot(x, func(x, *popt), 'r-', label="Fitted Curve")
# plt.legend()
plt.show()