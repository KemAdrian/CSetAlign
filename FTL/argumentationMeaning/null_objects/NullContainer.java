package null_objects;

import java.util.HashSet;
import java.util.Set;

import interfaces.Container;
import semiotic_elements.Concept;
import semiotic_elements.Example;

public class NullContainer implements Container{
	
	public NullContainer() {
		// Do nothing
	}

	@Override
	public Set<Example> getContext() {
		return new HashSet<>();
	}

	@Override
	public Concept removeConcept(Concept c) {
		return new NullConcept();
	}

	@Override
	public Set<Concept> getAllConcepts() {
		return new HashSet<>();
	}

}
