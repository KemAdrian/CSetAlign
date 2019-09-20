# Two-agent System Arguing about the Meaning in order to Build Contrast Sets over Heterogeneous NOOS Datasets

A framework based on the [FTL](https://github.com/santiontanon/fterm) library. Two agents build contrast sets (knowledge representations associated to a vocabulary) that is initially unable to guarantee mutual intelligibility. Through argumentation over the concepts that compose the contrast set, the agents build a new version of their initial contrast set. This final contrast set guarantees mutual intelligibility while providing the same expressiveness as their initial versions. The two agents share their representation language, namely the [NOOS](http://www2.iiia.csic.es/Projects/Noos.html).

Parts of the code is under copyright (see LICENCE.txt).

* [How to install it](https://github.com/keminus/ArgumentationOnMeaning#how-to-install-it)
* [What is inside](https://github.com/keminus/ArgumentationOnMeaning#what-is-inside)
* [What you can do with it](https://github.com/keminus/ArgumentationOnMeaning#what-you-can-do-with-it)

# How to install it

This project has been developed using [Eclipse](https://eclipse.org/). The repository is a standard eclipse project folder. Since the code is written in [Java](https://www.java.com/fr/), any other IDE and it can be executed from a terminal in any operating system that has Java installed. However, the code has only been tested in the Eclipse environment so we recommend you to use it.

## Install Java

Download the [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) version compatible with your OS and run the installation.

## Install Eclipse

You can download Eclipse [here](https://eclipse.org/downloads/) for a large panel of operating systems. A good tutorial for the installation is provided by the Eclipse [Wiki](https://wiki.eclipse.org/Eclipse/Installation). You should download the last Eclipse IDE for Java Developers version of the IDE.

## Import the repository as a new project

Copy your local repository folder in your current workspace. If this is the first time that you launch Eclipse, it will ask you to create a workspace. Note its direction and copy the local repository there.

Open Eclipse and clic on File > New > Project...

<p align="center"><div style="text-align:center"><img src="https://github.com/keminus/ArgumentationOnMeaning/blob/master/Resources/Pictures/new_project.png" width="75%"></div></p>

In the window that pops up, select Java Project and clic next.

<p align="center"><div style="text-align:center"><img src="https://github.com/keminus/ArgumentationOnMeaning/blob/master/Resources/Pictures/java_project.png" width="75%"></div></p>

In the Project name field, enter 'ArgumentationOnMeaning' (or the name of the local folder of this repository if you changed it).

<p align="center"><div style="text-align:center"><img src="https://github.com/keminus/ArgumentationOnMeaning/blob/master/Resources/Pictures/name_project.png" width="75%"></div></p>

Clic on Finish. The Package Explorer of Eclipse (left side by default) should look like this:

<p align="center"><div style="text-align:center"><img src="https://github.com/keminus/ArgumentationOnMeaning/blob/master/Resources/Pictures/default_project.png" width="75%"></div></p>

(with ArgumentationOnMeaning instead of FTL).

# What is inside

## Structure of the code

The project is divided between two source folders. The first one (src) contains a modified version of the FTL, having few new methods in the class ABUI allowing an easy creation for concepts in our formalism. The second one (argumentationMeaning) contains the last version of the multi-agent system and its communication and argumentation protocol.

You can find below a short presentation of argumentationMeaning's different packages. More information about the this source folder is available in the [Javadoc](https://keminus.github.io/ArgumentationOnMeaning/).

* agents : the different classes of agents.
* containers : the classes for collections of concepts
* enumerators : the custom enumerators in the code
* interfaces : the interfaces for agents, containers and semiotic elements (each of them has its own package)
* messages : the classes of message that agents can send to each other, with their abstract common class
* parametric_scripts : the scripts that can be called by a main in order to run different experiments (variations of contrast sets structure)
* scripts : the main scripts to run large experiments
* semiotic_elements : the class for the components of concepts and the class representing concepts.
* tools : various tools, from help with interfacing the learning with FTL to set manipulation.

## Main ideas behind the code

This Ph.D aims to provide a bridge between semiotics and A.I. You can find below some basic information about the transposition of element of semiotics into our computational model. 

### Semiotic elements

Context, Example and Extensional definition: The different elements that an agent has perceived in its environment are called examples. For example, a specific chair from an office is an example. They are noted ei. A context E = {e1...en} is a set of examples. An extensional definition on a context is a set of examples Ei ? E.

Generalisation and Intensional definition: The agents are using -terms as their representation language. An agent represents an example ei using a feature-term fi. A generalisation gj of a set of examples Ei is an other feature- term that verifies : for all ei in Ei, gj subsumes ei. An intensional definition Ii = {g1...gi} is a set of generalisations.

Sign and Concept A sign si is an abstract entity that exists only in the communication between two agents. A concept Ci = (si,Ii,Ei) is the triadic relation between a sign, an intensional definition and an extensional definition. The relation should verify: for all ei in Ei, exists gi in Ii as gi subsumes ei.

### Containers

Containers are a dyadic relation between a context E and a set of concepts {C1, ..., Cn}.

Hypothesis A hypothesis H = (E, {C1, ..., Cn}) is a container such that the set of examples {E1, ...,  En} is a subset of E and where the signs of the concepts are different:  for all Ci,Cj in {C1,...,Cn}, i is different  of j if si != sj.

Contrast Set A contrast set K = {C1,...,Cn} is a container such that the context E is equivalent to the set of examples {E1, ... , En}, the concepts are disjoint and the signs of the concepts are different. K is a partition of E.

### Relations between concepts

A central element of this model is the ability to compare concepts according to their respective extensional definitions. Agents are able to identify a relation from there point of view by using functions 'agree'. We identify 4 relations:

* True : means that the extensional definitions are equivalent
* False : means that the extensional definitions are disjoint
* Correct : means that one extensional definitions is included in the other (strict inclusion) 
* Incorrect : means that the intersection between the extensional definition is non-empty and neither the symmetric difference

# What you can do with it

Follow a little tutorial and start to work with your own data.

## Example: Argumentation over seats

Open the run.java script in the "scripts" package of "argumentationMeaning". Specify the SEAT_TEST dataset at the beginning of the code  and run it (white play arrow on a green button, top toolbar). Expend the console (bottom of the IDE).

<p align="center"><div style="text-align:center"><img src="https://github.com/keminus/ArgumentationOnMeaning/blob/master/Resources/Pictures/select_data.png" width="75%"></div></p>

 The script lists you the concepts of the contrast set of agent 1 and asks you if you want to merge some. Enter i to specific that you want to merge by giving the first concept's index. Then, enter the index of either "stool" or "armchair" (here, we chose stool).

```
The contrast set has 3 concepts:
   > 0 : label:chair
   > 1 : label:stool
   > 2 : label:armchair
Do you want to merge one ? (n = no, r = random, i = chose index of the merge)
i
```
The script will confirm which concept you selected.

```
The first concept is label:stool
```

Select "chair" as the second concept to merge with the same procedure:

```
With which other concept do you want to merge it ? (r = random, i = chose index of the merge)
i
   > Give an index:
0
The second concept is label:chair
Merged
```

Repeat the process for the second agent, merging chair with armchair (or with stool if you merged chair with armchair in the first agent's contrast set). First select the armchair index, then the chair index.

```
The contrast set has 3 concepts:
   > 0 : label:armchair
   > 1 : label:chair
   > 2 : label:stool
Do you want to merge one ? (n = no, r = random, i = chose index of the merge)
i
   > Give an index:
0
The first concept is label:armchair
With which other concept do you want to merge it ? (r = random, i = chose index of the merge)
i
   > Give an index:
1
The second concept is label:chair
Merged
```

The script displays the initial compatibility score between the two agents' contrast sets.

```
- - - - - - Display initial score :
   > 30.0%
   
```

The communication starts. The two agents send to each other their intensional definitions and signs.

```
Oracle : starts discussion,  agent boby in defense and agent adam in attack (Initial)
   > The concept label:chair has been sent to the attacker
   > The concept label:stool has been sent to the attacker

Oracle : switch roles, agent adam in defense and agent boby in attack (Initial)
   > The concept label:chair has been sent to the attacker
   > The concept label:armchair has been sent to the attacker
   
```

Each agent builds a hypothesis, with its concepts and the concepts that he created with the intensional definitions and signs from the other agent. He creates a table with the relations between its concepts and the concepts from the other agent. He marks its concepts' signs with * and the other agent concept's signs with °.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (BuildHypothesisState)
   > Concept for label:chair added to the other agent concepts in hypothesis
   > Concept for label:armchair added to the other agent concepts in hypothesis
   > Concept for label:chair added to our own concepts in hypothesis
   > Concept for label:stool added to our own concepts in hypothesis
   > Table of agreements has been created

Oracle : switch roles, agent adam in defense and agent boby in attack (BuildHypothesisState)
   > Concept for label:chair added to the other agent concepts in hypothesis
   > Concept for label:stool added to the other agent concepts in hypothesis
   > Concept for label:chair added to our own concepts in hypothesis
   > Concept for label:armchair added to our own concepts in hypothesis
   > Table of agreements has been created
   
```

Each agent then send a message to the other agent in order to inform him about the relation that he sees between each pair of concepts.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ExpressAgreementState)
   > The agreement : label:chair* is a label:armchair° is Correct - has been sent
   > The agreement : label:chair* is a label:chair° is Incorrect - has been sent
   > The agreement : label:stool* is a label:armchair° is False - has been sent
   > The agreement : label:stool* is a label:chair° is Correct - has been sent

Oracle : switch roles, agent adam in defense and agent boby in attack (ExpressAgreementState)
   > The agreement : label:chair* is a label:chair° is Incorrect - has been sent
   > The agreement : label:armchair* is a label:stool° is False - has been sent
   > The agreement : label:chair* is a label:stool° is Correct - has been sent
   > The agreement : label:armchair* is a label:chair° is Correct - has been sent
```

The agents might change the relation that they associate to the pairs of concepts according to the relation associated by the other, getting the overall relation between the concepts (the relation that would be detected if the agents were sharing their examples).

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ModifyAgreementState)
   > No agreement has been changed
{ [ label:chair* ] [ label:stool* ] }
{ [ label:chair° ] [ label:armchair° ] }

Oracle : switch roles, agent adam in defense and agent boby in attack (ModifyAgreementState)
   > No agreement has been changed
{ [ label:chair* ] [ label:armchair* ] }
{ [ label:chair° ] [ label:stool° ] }
```

A discussion is created about a relation (Incorrect occurs first)

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ArgumentationStartState)
   > The discussion's variables have been initialized
   > A new discussion about label:chair* and label:chair° has been created
   > The relation between the two concepts is seen as Incorrect

Oracle : switch roles, agent adam in defense and agent boby in attack (ArgumentationStartState)
   > A new discussion about label:chair* and label:chair° has been created
   > The relation between the two concepts is seen as Incorrect
   
```

The agents try to find a new intensional definition for the examples that are in both concepts from the discussed relation.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ArgumentationInitializeExtension)
label:chair*
label:chair°
   > The agents will try to find a new intensional definition for examples that are both label:chair* and label:chair°

Oracle : switch roles, agent adam in defense and agent boby in attack (ArgumentationInitializeExtension)
label:chair*
label:chair°
   > The agents will try to find a new intensional definition for examples that are both label:chair* and label:chair°
   
```

The agents use the protocol AMAIL from the FTL to argue about their definitions. They eventually agree on one.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ArgumentationCoreState)

AMAIL: agent Agent 1 has the token in round 0
AA of A5369 (by AAgent 2): 0.9 (Tree size: 1)
AA of A5370 (by AAgent 2): 0.93333334 (Tree size: 1)
AA of A5371 (by AAgent 2): 0.85714287 (Tree size: 1)
AMAIL: agent Agent 1 has to defend 0 roots
AMAIL: agent Agent 1 finds 0 arguments of the other agent unacceptable
beliefRevision: Agent 1
coverUncoveredExamples with 6 accepted arguments...
Uncovered examples 0 covered by 0 new rules.
beliefRevision: Agent 2

AMAIL: agent Agent 2 has the token in round 1
AA of A5366 (by AAgent 1): 0.8888889 (Tree size: 1)
AA of A5367 (by AAgent 1): 0.8888889 (Tree size: 1)
AA of A5368 (by AAgent 1): 0.9166667 (Tree size: 1)
AMAIL: agent Agent 2 has to defend 0 roots
AMAIL: agent Agent 2 finds 0 arguments of the other agent unacceptable
beliefRevision: Agent 1
beliefRevision: Agent 2
coverUncoveredExamples with 6 accepted arguments...
Uncovered examples 0 covered by 0 new rules.

```

The agents create a new concept with this intensional definition and add it to their hypothesis

```
Oracle : switch roles, agent boby in defense and agent adam in attack (WaitingAgreementState)
   > Concept label:temp_0* and Concept label:temp_0° has been added
{ [ label:chair* & label:armchair° = Correct ] [ label:temp_0* & label:armchair° = False ] [ label:temp_0* & label:chair° = Correct ] [ label:chair* & label:temp_0° = Correct ] [ label:temp_0* & label:temp_0° = True ] [ label:stool* & label:temp_0° = False ] [ label:stool* & label:armchair° = False ] [ label:stool* & label:chair° = Correct ] }
{ [ label:chair* ] [ label:temp_0* ] [ label:stool* ] }
{ [ label:temp_0° ] [ label:chair° ] [ label:armchair° ] }

Oracle : switch roles, agent adam in defense and agent boby in attack (WaitingAgreementState)
   > Concept label:temp_0* and Concept label:temp_0° has been added
{ [ label:armchair* & label:stool° = False ] [ label:temp_0* & label:temp_0° = True ] [ label:chair* & label:stool° = Correct ] [ label:armchair* & label:chair° = Correct ] [ label:chair* & label:temp_0° = Correct ] [ label:temp_0* & label:chair° = Correct ] [ label:temp_0* & label:stool° = False ] [ label:armchair* & label:temp_0° = False ] }
{ [ label:temp_0* ] [ label:chair* ] [ label:armchair* ] }
{ [ label:temp_0° ] [ label:chair° ] [ label:stool° ] }
```

The agents come back to the state where they discuss about the agreement of their pairs. They continue and create a new discussion about a Correct relation.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ArgumentationStartState)
   > The discussion's variables have been initialized
   > There is a "Correct" disagreement and the problem is seen as Hyponymy
   > A new discussion about label:stool* and label:chair° has been created
   > The relation between the two concepts is seen as Correct

Oracle : switch roles, agent adam in defense and agent boby in attack (ArgumentationStartState)
   > There is a "Correct" disagreement and the problem is seen as Hyperonymy
   > A new discussion about label:chair* and label:stool° has been created
   > The relation between the two concepts is seen as Correct

```
This time the agents try to create an intensional definition for the part of their extensional definition that is not subsumed by the hyponym. They have to both agree on which concept is the hypernym, or they change the nature of the relation.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ArgumentationInitializeExtension)
label:stool*
label:chair°
   > Agent considers its concept as the Hyponymy and the other as Hyperonymy
   > The agents will try to find a new intensional definition for examples that are label:chair° but not label:stool*

Oracle : switch roles, agent adam in defense and agent boby in attack (ArgumentationInitializeExtension)
label:chair*
label:stool°
   > Agent considers its concept as the Hyperonymy and the other as Hyponymy
   > The agents will try to find a new intensional definition for examples that are label:chair* but not label:stool°
   
```

The argumentation continue with the other Correct, True and False relation until all the relations have been examined. Then, the agents go back to their initial state and then vote for the sign of each concepts that have a True relation. They use the number of examples that initially had each label as their number of vote.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (VoteForSignState)
  > label:temp_2* should be named label:chair (13)
  > label:temp_0* should be named label:chair (8)

Oracle : switch roles, agent adam in defense and agent boby in attack (VoteForSignState)
  > label:temp_0* should be named label:chair (7)
  > label:temp_0* should be named label:armchair (7)
  > label:temp_2* should be named label:chair (7)
  > label:temp_2* should be named label:armchair (7)
  
```

The agents then change their signs accordingly to what they voted.

```
Oracle : switch roles, agent boby in defense and agent adam in attack (ChangeSignState)
   > A new vote started for label:temp_2* with answer label:chair (13)
   > A new vote started for label:temp_0* with answer label:chair (8)
   > The winner for the vote on label:temp_0 has not been updated  (7)
   > The winner for the vote on label:temp_0 has not been updated  (7)
   > The winner for the vote on label:temp_2 has not been updated  (7)
   > The winner for the vote on label:temp_2 has not been updated  (7)
   >label:temp_0* has been changed for label:chair*
   >label:temp_0° has been changed for label:chair°
   >label:temp_2* has been changed for label:chair*
   >label:temp_2° has been changed for label:chair°
   > Stop
{ }
{ [ label:chair* ] [ label:chair* ] [ label:stool* ] }
{ [ label:chair° ] [ label:stool° ] [ label:chair° ] }

Oracle : switch roles, agent adam in defense and agent boby in attack (ChangeSignState)
   > A new vote started for label:temp_2* with answer label:chair (13)
   > A new vote started for label:temp_0* with answer label:chair (8)
   > The winner for the vote on label:temp_0 has not been updated  (7)
   > The winner for the vote on label:temp_0 has not been updated  (7)
   > The winner for the vote on label:temp_2 has not been updated  (7)
   > The winner for the vote on label:temp_2 has not been updated  (7)
   >label:temp_0* has been changed for label:chair*
   >label:temp_0° has been changed for label:chair°
   >label:temp_2* has been changed for label:chair*
   >label:temp_2° has been changed for label:chair°
   > Stop
{ }
{ [ label:chair* ] [ label:stool* ] [ label:chair* ] }
{ [ label:chair° ] [ label:chair° ] [ label:stool° ] }
- - - - - - Display final score :
   > 100.0%

```

The communication stop, the concepts of the final contrast sets are displayed and the final compatibility score is given.

## Import your own data

Start by converting you own data into NOOS files, and place the three files (mydataset-ontology.noos, mydataset-dm.noos, mydatasetXcases.noos) into the Ressources/DATA/ folder.

In the code, open the source folder src and find csic.iiia.ftl.learning.core and edit the class TrainingSetUtils.java. After the line 470, add the index of your dataset, for example: public static final int MY_DATASET = 35;

<p align="center"><div style="text-align:center"><img src="https://github.com/keminus/ArgumentationOnMeaning/blob/master/Resources/Pictures/my_dataset.png" width="75%"></div></p>

In the method loadTrainingSet(), add the loading of your dataset in the switch. Specify the case of your dataset by indicating the path of your three files, give it a name and a problem sort and add a description and a solution path.

<p align="center"><div style="text-align:center"><img src="https://github.com/keminus/ArgumentationOnMeaning/blob/master/Resources/Pictures/add_switch.png" width="75%"></div></p>

Congratulation, you are now ready to test your dataset as you did in the short tutorial!
