package interfaces;

import java.util.Set;

import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;

/**
 * A {@link SemioticElement} is a constituent of the meaning.
 * Three of them, the {@link Example}, the {@link Generalization} and the {@link Sign}, are associated to make the fourth one, the {@link Concept}.
 * 
 * @author kemoadrian
 *
 */
public interface SemioticElement {
	
	/**
	 * Clone the {@link SemioticElement}. Its attributes are also cloned.
	 * @return a copy of the {@link SemioticElement}
	 */
	public SemioticElement clone();
	
	/** Return the {@link Set} of {@link Example} related to this element among a {@link Container}
	 * @param c the {@link Container}
	 * @return the {@link Set} of {@link Example}
	 */
	public Set<Example> getExtension(Container c);

}
