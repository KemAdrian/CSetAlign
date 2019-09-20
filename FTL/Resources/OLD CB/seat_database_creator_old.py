# -*- coding: utf-8 -*-

filepath = ""

print "<<<< Welcome in the seat database creator >>>>"

print "Do you want to create an other file or use an old one?"
save = int(input("1-new 2-old \n"))

def new_seat():

	answers = [0];
	nb_answer = 0;

	print "Which kind of seat will it be?"
	answers[nb_answer] = string.lower(raw_input("1-Stool 2-Chair 3-Armchair \n"))
	nb_answer += 1

	print "Does it have a back?"
	answers[nb_answer] = string.lower(raw_input("1-Yes 2-No \n"))
	nb_answer += 1

	if answers[nb_answer-1] == 1:

		print "Which maretial?"
		answers[nb_answer] = string.lower(raw_input("1-Steel 2-Wood 3-Plastic 4-Leather 5-Synthetic 6-Cloth \n"))
		nb_answer += 1

		print "Which length?"
		answers[nb_answer] = string.lower(raw_input("1-Short 2-Medium 3-Long \n"))
		nb_answer += 1

		print "Which color?"
		answers[nb_answer] = string.lower(raw_input("1-Black 2-Red 3-Blue 4-Green 5-Brown 6-White 7-Grey \n"))
		nb_answer += 1

	else :

		nb_answer += 3

	print "Does it have arms?"
	answers[nb_answer] = string.lower(raw_input("1-Yes 2-No \n"))
	nb_answer += 1

	if answers[nb_answer-1] == 1:

		print "Which maretial?"
		answers[nb_answer] = string.lower(raw_input("1-Steel 2-Wood 3-Plastic 4-Leather 5-Synthetic 6-Cloth \n")) 
		nb_answer += 1

		print "Which length?"
		answers[nb_answer] = string.lower(raw_input("1-Short 2-Medium 3-Long \n"))
		nb_answer += 1

		print "Which color?"
		answers[nb_answer] = string.lower(raw_input("1-Black 2-Red 3-Blue 4-Green 5-Brown 6-White 7-Grey \n"))
		nb_answer += 1

	else :

		nb_answer += 3

	print "For the surface, "
	print "Which maretial?"
	answers[nb_answer] = string.lower(raw_input("1-Steel 2-Wood 3-Plastic 4-Leather 5-Synthetic 6-Cloth \n")) 
	nb_answer += 1

	print "Which size?"
	answers[nb_answer] = string.lower(raw_input("1-Small 2-Medium 3-Big \n"))
	nb_answer += 1

	print "Which color?"
	answers[nb_answer] = string.lower(raw_input("1-Black 2-Red 3-Blue 4-Green 5-Brown 6-White 7-Grey \n")) 
	nb_answer += 1

	print "For the support, "
	print "Which maretial?"
	answers[nb_answer] = string.lower(raw_input("1-Steel 2-Wood 3-Plastic 4-Leather 5-Synthetic 6-Cloth \n")) 
	nb_answer += 1

	print "Which color?"
	answers[nb_answer] = string.lower(raw_input("1-Black 2-Red 3-Blue 4-Green 5-Brown 6-White 7-Grey \n")) 
	nb_answer += 1

	print "Which width?"
	answers[nb_answer] = string.lower(raw_input("1-Thin 2-Medium 3-Large \n")) 
	nb_answer += 1

	print "Which kind of legs?"
	answers[nb_answer] = string.lower(raw_input("1-Legs 2-Wheels \n")) 
	nb_answer += 1

	print "Which maretial?"
	answers[nb_answer] = string.lower(raw_input("1-Steel 2-Wood 3-Plastic 4-Leather 5-Synthetic 6-Cloth \n")) 
	nb_answer += 1

	print "Which length?"
	answers[nb_answer] = string.lower(raw_input("1-Short 2-Medium 3-Long \n")) 
	nb_answer += 1

	print "Which color?"
	answers[nb_answer] = string.lower(raw_input("1-Black 2-Red 3-Blue 4-Green 5-Brown 6-White 7-Grey \n")) 
	nb_answer += 1

	print "Wich number of wheel(W) / leg(L)?"
	answers[nb_answer] = string.lower(raw_input("1- 3W or 1L 2- 4W or 3L 3- 5W or 4L"))

	return answers

def save_input(answers,path,n):
	f = open(path,'a')

	f.write("define (seat :ID SEAT-"+str(n)+")\n")
	f.write("	(back (define (")
	if(answers[1] == 1)
		f.write("with-back) \n")
		f.write("		(material ")

	else:
		f.write("no-back))) \n")