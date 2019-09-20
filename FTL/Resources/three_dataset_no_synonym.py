# -*- coding: utf-8 -*-

# IMPORT LIBRARIES

import random

# DEFINE ENVIRONMENT VARIABLES

SAME_FILE = True
NB_SEAT = 50
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
		print "INVALID LABEL IN INPUT : LABEL IS "+str(label)+" AND MUST BE stool, chair OR armchair"
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
		print "INVALID LABEL IN INPUT : LABEL IS "+str(label)+" AND MUST BE stool, chair OR armchair"
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
		print "INVALID INPUT : "+str(legs_or_wheels)+" MUST BE EITHER with-wheels OR with-legs"
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
		print "INVALID INPUT : "+str(legs_or_wheels)+" MUST BE EITHER with-wheels OR with-legs"
	out += "		)) \n"
	out += "	)) \n"
	out += ") \n"
	return out

# CREATE FILES

# SETTINGS FOR DE TWO LEARNING DATASETS CREATION
first_set = [[20,'armchair','armchair'],[20,'chair','chair'],[10,'stool','chair']]
second_set = [[20,'chair','chair'],[20, 'stool', 'stool'],[10,'armchair','chair']]

if not SAME_FILE :
	f1 = open("seat-cases-learn-1.noos",'w')

else :
	f1 = open("seat-cases-all.noos",'w')

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

if not SAME_FILE :
	f1.close()
	f2 = open("seat-cases-learn-2.noos",'w')

else :
	f2 = f1

#WRITE THE SECOND LEARNING SET
if not SAME_FILE :
	i = 0
for case in second_set:
	for n in range(0,case[0]):
		# DEFINE CLASS OF SEAT
		legs_or_wheels = random.choice(LEGS_OR_WHEELS)
		f2.write(seat_case_header(i))
		f2.write(seat_case_back(case[1]))
		f2.write(seat_case_arms(case[1]))
		f2.write(seat_case_surface())
		f2.write(seat_case_support())
		f2.write(seat_case_legs(legs_or_wheels))
		f2.write("\n")
		f2.write(seat_case(i,case[2]))
		f2.write("\n")
		i+=1

if not SAME_FILE :
	f2.close()
	f3 = open("seat-cases-test.noos",'w')

else :
	f3 = f1

# WRITE NB_SEAT EXAMPLES IN THE LAST FILE
for n in range(0,NB_SEAT):

	if not SAME_FILE :
		i = n
	# DEFINE CLASS OF SEAT
	label = random.choice(SEAT)
	legs_or_wheels = random.choice(LEGS_OR_WHEELS)
	f3.write(seat_case_header(i))
	f3.write(seat_case_back(label))
	f3.write(seat_case_arms(label))
	f3.write(seat_case_surface())
	f3.write(seat_case_support())
	f3.write(seat_case_legs(legs_or_wheels))
	f3.write("\n")
	f3.write(seat_case(i,label))
	f3.write("\n")
	i+=1

if not SAME_FILE :
	f3.close()

else :
	f1.close