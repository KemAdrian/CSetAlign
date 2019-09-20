package null_objects;

import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.base.utils.FeatureTermException;
import interfaces.Container;
import interfaces.SemioticElement;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;

/**
 * A {@link NullConcept} is a triadic relation between the three other {@link SemioticElement}s. It represents a unity of meaning in our model.
 * @author kemoadrian
 *
 */
public class NullConcept extends Concept {
	
	// Get Informations
	
	/**
	 * Give the {@link Sign} of the {@link NullConcept}.
	 * @return the {@link Sign}.
	 */
	public String sign(){
		return sign.toString();
	}
	
	/**
	 * Give the intensional definition.
	 * @return the {@link Set} of {@link Generalization} that composes the intensional definition.
	 */
	public Set<Generalization> intensional_definition(){
		return this.intensional_definition;
	} 
	
	/**
	 * Give the extensional definition
	 * @return the {@link Set} of {@link Example} that composes the extensional definition
	 */
	public Set<Example> extensional_definition(){
		return this.extensional_definition;
	}
	
	/* (non-Javadoc)
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
	public Set<Example> getExtension(Container c){
		return new HashSet<>();
	}
	
	/**
	 * Give the {@link Generalization} from this {@link NullConcept}'s intensional definition that can generalize at least one {@link Example} from the given {@link SemioticElement} extension in the context of a {@link Container}.
	 * @param e the {@link SemioticElement}.
	 * @param c the {@link Container}.
	 * @return the {@link Set} of {@link Generalization} that have a common extension in the context of the {@link Container}.
	 */
	public Set<Generalization> Generalizes(SemioticElement e, Container c){
		return new HashSet<>();
	}
	
	// Duplicate
	public Set<Generalization> copy_intensional_definition(){
		return new HashSet<>();
	}
	
	public Set<Example> copy_extensional_definition(){
		return new HashSet<>();
	}
	
	// Add elements
	public boolean addExamples(Set<Example> E){
		return false;
	}
	
	public boolean addGeneralizations(Set<Generalization> I){
		return false;
	}
	
	// Delete elements
	public boolean removeExamples(Set<Example> E){
		return false;
	}
	
	public boolean removeGeneralizations(Set<Generalization> I){
		return false;
	}
	
	// Test
	
	/**
	 * Test if the intensional definition of this {@link NullConcept} covers a given {@link Example}.
	 * @param e the {@link Example}.
	 * @return <tt>true</tt> if the {@link Example} is covered by the intensional definition.
	 * @throws FeatureTermException
	 */
	public boolean covers(Example e){
		return false;
	}
	
	public NullConcept clone(){
		return new NullConcept();
	}
	
	public boolean equals(Object o) {
		return false;
	}
	
	public boolean isNull() {
		return true;
	}
	
}
