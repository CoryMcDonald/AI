import timeit
import random
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

yea = ""
i = 0
perimeter = [(0, 0), (1, 0), (2, 0), (3, 0), (4, 0), (5, 0), (6, 0), (7, 0), (8, 0), (9, 0), (0, 1), (1, 1), (2, 1), (7, 1), (8, 1), (9, 1), (0, 2), (1, 2), (8, 2), (9, 2), (0, 3), (4, 3), (9, 3), (0, 4), (3, 4),
             (4, 4), (9, 4), (0, 5), (9, 5), (0, 6), (9, 6), (0, 7), (1, 7), (8, 7), (9, 7), (0, 8), (1, 8), (2, 8), (7, 8), (8, 8), (9, 8), (0, 9), (1, 9), (2, 9), (3, 9), (4, 9), (5, 9), (6, 9), (7, 9), (8, 9), (9, 9)]

def convertPiece():
	j =0
	used = [0 for x in range(100)]
	for xy in perimeter:
	    used[10 * xy[0] + xy[1]] = 0
	for i, piece in enumerate(pieces):
	    for coordinate in piece:
	        used[10 * coordinate[0] + coordinate[1]] = j
	    j = j +1

	return 


# print convertPiece()

def compare():
	array = [0 for x in range(100)]
	array == convertPiece()

print timeit.timeit('convertPiece()', setup="from __main__ import convertPiece", number =100000)