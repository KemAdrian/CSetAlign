package interfaces;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.base.core.FeatureTerm;
import semiotic_elements.Example;
import semiotic_elements.Generalization;

public interface ArgNode extends Node{
 	
	public Collection<Generalization> getValidPart(Collection<Argument> arguments, Map<FeatureTerm, Set<Example>> context);
	
	public Collection<Generalization> getInvalidPart(Collection<Argument> arguments, Map<FeatureTerm, Set<Example>> context);
	
	public Set<Generalization> generalizations();

}
