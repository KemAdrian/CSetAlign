package containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import csic.iiia.ftl.learning.lazymethods.similarity.AUDistance;
import csic.iiia.ftl.learning.lazymethods.similarity.Distance;
import identifiers.ConID;
import interfaces.Container;
import null_objects.NullConcept;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;
import tools.LPkg;
import tools.ToolSet;

/**
 * A {@link ContrastSet} is a {@link Container} where the set of
 * {@link Concept}s makes a partition of the context.
 * 
 * @author kemoadrian
 *
 */
public class ContrastSet implements Container {

	public static int custom_name = 0;
	public Distance distance = new AUDistance();
	public Map<ConID, Concept> map;
	public Set<Example> context;

	/**
	 * A {@link ContrastSet} is a {@link Container} where the set of
	 * {@link Concept}s makes a partition of the context.
	 * 
	 * @param concepts The set of {@link Concept}s partitioning the context
	 * @param context  The set of {@link Example}s representing the context
	 */
	public ContrastSet(Set<Concept> concepts, Set<Example> context) {
		this.map = new HashMap<>();
		for (Concept c : concepts) {
			this.map.put(c.id, c.clone());
		}
		this.context = context;
	}

	public Concept getConcept(ConID id) {
		Concept out = map.get(id);
		if (out == null)
			return new NullConcept();
		return out;
	}

	public Concept getConcept(String sign) {
		for (Concept c : getAllConcepts()) {
			if (c.sign().equals(sign))
				return c;
		}
		return new NullConcept();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.Container#getAllConcepts()
	 */
	public Set<Concept> getAllConcepts() {
		return new HashSet<>(this.map.values());
	}

	public boolean contains(Concept c) {
		return map.keySet().contains(c.id);
	}

	public Concept addConcept(Concept c) {
		return this.map.put(c.id, c.clone());
	}

	/**
	 * Remove the first {@link Concept} that has the {@link String} passed as a
	 * parameter as its {@link Sign} from the {@link ContrastSet}.
	 * 
	 * @param s the {@link String} that should be identical to the expected
	 *          {@link Concept} {@link Sign}'s {@link String}.
	 */
	public Concept removeConcept(Concept c) {
		if (c.isNull())
			return new NullConcept();
		Concept out = this.map.remove(c.id);
		if (out == null)
			return new NullConcept();
		return out;
	}

	/**
	 * Add a set of {@link Example}s to the {@link ContrastSet}'s context and put
	 * them into the extensional definiton of the {@link Concept}s that cover them.
	 * 
	 * @param examples the {@link Example}s to add.
	 */
	public void addExamples(Collection<Example> examples) {
		Set<Example> toAdd = new HashSet<>(ToolSet.substract(examples, context));
		for (Example e : toAdd) {
			this.context.add(e);
			for (Concept c : this.getAllConcepts()) {
				if (c.covers(e)) {
					c.extensional_definition.add(e);
				}
			}
		}
	}

	public void leftPathConsistent() {
		int removed = 0;
		Map<Concept, Set<Example>> toRemove = new HashMap<>();
		for(Concept c : map.values())
			toRemove.put(c, new HashSet<>());
		// Check all the examples from the context
		System.out.println("   > Looking for examples subsumed by more than one concept...");
		for (Example e : context) {
			// Check all the concepts that are subsuming it
			List<Concept> covering = new ArrayList<>();
			for (Concept c : map.values()) {
				if (ToolSet.contains(c.extensional_definition, e))
					covering.add(c);
			}
			// Check if more than one concept is subsuming it
			if (covering.size() > 1) {
				System.out.println("     > Example covered by more than one concept, removing...");
				// Check the concept that is the closest
				Concept closest = closestConcept(covering, e);
				covering.remove(closest);
				for(Concept c : covering)
					toRemove.get(c).add(e);
				removed ++;
			}
		}
		for(Entry<Concept,Set<Example>> entry : toRemove.entrySet()) {
			entry.getKey().removeExamples(entry.getValue());
		}
		System.out.println("   > Found "+removed+" examples!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.Container#getContext()
	 */
	public Set<Example> getContext() {
		return this.context;
	}

	public ContrastSet clone() {
		ContrastSet clone = new ContrastSet(new HashSet<>(this.getAllConcepts()), this.context);
		return clone;
	}

	// Gives the closest belief to an example
	public Concept closestConcept(List<Concept> concepts, Example e) {
		Map<Concept,Double> distances = new HashMap<Concept,Double>();
		for(Concept c : concepts)
			distances.put(c, e_i_distance(c.intensional_definition, e));
		// Choose smallest distance
		Entry<Concept,Double> min = null;
		for(Entry<Concept,Double> entry : distances.entrySet()) {
			if(min == null)
				min = entry;
			else
				min = (entry.getValue() <= min.getValue())? entry : min;
		}
		return min.getKey();
	}

	// Gives the distance between a set of generalizations and an example
	public double e_i_distance(Set<Generalization> idef, Example e) {
		double num = 0;
		double denom = 0;
		for (Generalization g : idef) {
			try {
				num += distance.distance(g.generalization, e.featureterm, LPkg.ontology(), LPkg.dm());
				denom++;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return num / denom;
	}

}