# -*- coding: utf-8 -*-

# IMPORT LIBRARIES

import random
from math import *

# DEFINE ENVIRONMENT VARIABLES

DEBUG = 1
EXAMPLES = 0
SORTS = 0
FEATURES = 0
SORT_SYMBOLS = 0
FEATURE_SYMBOLS = 0
SIMPLE_VALUES = 0
SIMPLE_VARIABLES = 0

# DEFINE ENVIRONMENT CONSTENTS

DEF_DOMAIN_MODEL = "Seat-DM"
DEF_ROOTNAME = "SEAT"
DEF_CREATOR = "Kemo Adrian"
DEF_DESCRIPTION = "Domain model for the Seat Data Set"

VOYELS = "aeiou"
CONSONS = "zrtpsdfgklmxcvbn"

# DEFINE CLASSES

class Feature:

	def __init__(self, feature_name, featureterm):
		if DEBUG == 1: 
			print "SYSTEM creating new Feature "
		self.name = feature_name
		self.term = featureterm
		if DEBUG == 1: 
			print "SYSTEM new Feature created"		

class FeatureTerm:
	
	def __init__(self, root, features):
		if DEBUG == 1: 
			print "SYSTEM creating new FeatureTerm "
		self.root = root
		self.features = features
		if DEBUG == 1: 
			print "SYSTEM new FeatureTerm created"