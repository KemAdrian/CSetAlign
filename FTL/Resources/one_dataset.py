# -*- coding: utf-8 -*-

# IMPORT LIBRARIES

import random

# DEFINE ENVIRONMENT VARIABLES

NB_SEAT = 900
MATERIAL = ['steel','wood','plastic','leather','synthetic','cloth']
LENGTH = ['short','medium','long']
COLOR = ['black','red','blue','green','brown','white','grey']
SIZE = ['small','medium','big']
WIDTH = ['thin','medium','large']
WHEEL_NUMBER = ['three-wheeled','four-wheeled','five-wheeled']
LEG_NUMBER = ['one-leg','three-legs','four-legs']
LEGS_OR_WHEELS = ['with-legs','with-wheels']

SEAT = ['stool','chair','armchair']

# FUNCTIONS FOR CREATING THE DIFFERENT LINES

def seat_case(id_seat,label):
	out = "(define (seat-case :id E"+str(id_seat)+") \n"
	out += "	(DESCRIPTION SEAT-"+str(id_seat)+") \n"
	out += "	(LABEL "+label+") \n"
	out += ") \n"
	return out

def seat_case_header(id_seat):
	out = "(define (seat :id SEAT-"+str(id_seat)+") \n"
	return out

def seat_case_back(label):
	if not label in SEAT:
		print("INVALID LABEL IN INPUT : LABEL IS "+str(label)+" AND MUST BE stool, chair OR armchair")
		return 0
	elif label == 'stool':
		out = "    (back (define (no-back))) \n"
	else :
		out = "    (back (define (with-back) \n"
		out += "        (material "+random.choice(MATERIAL)+") \n"
		out += "        (length "+LENGTH[random.randint(0,len(LENGTH)-1)]+") \n"
		out += "        (color "+COLOR[random.randint(0,len(COLOR)-1)]+") \n"
		out += "	)) \n"
	return out

def seat_case_arms(label):
	if not label in SEAT:
		print("INVALID LABEL IN INPUT : LABEL IS "+str(label)+" AND MUST BE stool, chair OR armchair")
		return 0
	elif not label == 'armchair':
		out = "    (arms (define (no-arm))) \n"
	else :
		out = "    (arms (define (arms) \n"
		out += "        (material "+MATERIAL[random.randint(0,len(MATERIAL)-1)]+") \n"
		out += "        (length "+LENGTH[random.randint(0,len(LENGTH)-1)]+") \n"
		out += "        (color "+COLOR[random.randint(0,len(COLOR)-1)]+") \n"
		out += "	)) \n"
	return out

def seat_case_surface():
	out = "    (surface (define (seat-surface) \n"
	out += "        (material "+MATERIAL[random.randint(0,len(MATERIAL)-1)]+") \n"
	out += "        (size "+SIZE[random.randint(0,len(SIZE)-1)]+") \n"
	out += "        (color "+COLOR[random.randint(0,len(COLOR)-1)]+") \n"
	out += "	)) \n"
	return out

def seat_case_support():
	out = "    (support (define (seat-support) \n"
	out += "        (material "+MATERIAL[random.randint(0,len(MATERIAL)-1)]+") \n"
	out += "        (color "+COLOR[random.randint(0,len(COLOR)-1)]+") \n"
	out += "        (width "+WIDTH[random.randint(0,len(WIDTH)-1)]+") \n"
	return out

def seat_case_legs(legs_or_wheels):
	if not legs_or_wheels in LEGS_OR_WHEELS:
		print("INVALID INPUT : "+str(legs_or_wheels)+" MUST BE EITHER with-wheels OR with-legs")
		return 0
	out = "        (legs (define ("+legs_or_wheels+") \n"
	out += "			(material "+MATERIAL[random.randint(0,len(MATERIAL)-1)]+") \n"
	out += "			(length "+LENGTH[random.randint(0,len(LENGTH)-1)]+") \n"
	out += "			(color "+COLOR[random.randint(0,len(COLOR)-1)]+") \n"
	if legs_or_wheels == 'with-legs':
		out += "			(leg-number "+LEG_NUMBER[random.randint(0,len(LEG_NUMBER)-1)]+") \n"
	elif legs_or_wheels == 'with-wheels':
		out += "			(wheel-number "+WHEEL_NUMBER[random.randint(0,len(WHEEL_NUMBER)-1)]+") \n"
	else :
		print("INVALID INPUT : "+str(legs_or_wheels)+" MUST BE EITHER with-wheels OR with-legs")
	out += "		)) \n"
	out += "	)) \n"
	out += ") \n"
	return out

# CREATE FILES

# SETTINGS FOR DE TWO LEARNING DATASETS CREATION
first_set = [[300,'armchair','armchair'],[300,'chair','chair'],[300,'stool','stool']]
second_set = [[300,'armchair','armchair'],[300,'chair','chair'],[300,'stool','stool']]

f1 = open("seat-cases-900.noos",'w')

#WRITE THE FIRST LEARNING SET
i = 0
for case in first_set:
	for n in range(0,case[0]):
		# DEFINE CLASS OF SEAT
		legs_or_wheels = random.choice(LEGS_OR_WHEELS)
		f1.write(seat_case_header(i))
		f1.write(seat_case_back(case[1]))
		f1.write(seat_case_arms(case[1]))
		f1.write(seat_case_surface())
		f1.write(seat_case_support())
		f1.write(seat_case_legs(legs_or_wheels))
		f1.write("\n")
		f1.write(seat_case(i,case[2]))
		f1.write("\n")
		i+=1

f1.close