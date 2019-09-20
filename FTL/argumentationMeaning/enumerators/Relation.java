package enumerators;

import interfaces.SemioticElement;
import semiotic_elements.Concept;

/**
 * @author kemoadrian
 *	{@link Relation}s are the four basic relations that two {@link Concept}s can have together. They are based on their extensional definitions (see {@link SemioticElement}).
 */
public enum Relation {
	
	// The four basic type of relations
	Overlap, Inclusion, Equivalence, Disjunction,
	// Signifies that no relation can be assigned between the concepts
	Blind,
	// The concept is without equivalent in the other agent's contrast set
	Untrans,
}
