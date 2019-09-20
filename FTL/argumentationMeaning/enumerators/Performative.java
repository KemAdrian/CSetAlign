package enumerators;

import interfaces.Agent;

/**
 * The possible performatives of the language acts that an {@link Agent} can produce.
 * 
 * @author kemoadrian 
 */
public enum Performative {
	
	// Send semiotic elements out of an argumentation
	Baptise,  Assert, SendExamples,
	// Has created a complete set of connected disagreements through the use of Asert
	ArgumentationReady,
	// Present a new example to be named (used by experimenter only)
	Present,
	// Name a presented example
	Name,
	// Send meta-information on elements
	ExtSize, Intransitive,
	// Send information about the relation between two concepts
	Evaluation, Relation,
	// Request self evaluations
	CheckSelf,
	// Ask for an argumentation on a disagreement
	Debate,
	// Take charge of one step instead of the other agent
	Seize,
	// Send arguments
	Belief, Attack,
	// Accept arguments
	AcceptBelief, AcceptAttack,
	// Ask to remove or replace a concept
	Remove, Replace,
	// Reassign vocabulary for clarity
	Elect;

}
