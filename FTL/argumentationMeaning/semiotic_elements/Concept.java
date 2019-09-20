package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.base.utils.FeatureTermException;
import identifiers.ConID;
import interfaces.Container;
import interfaces.SemioticElement;
import null_objects.NullConID;
import tools.ToolSet;

/**
 * A {@link Concept} is a triadic relation between the three other
 * {@link SemioticElement}s. It represents a unity of meaning in our model.
 * 
 * @author kemoadrian
 *
 */
public class Concept implements SemioticElement {

	public ConID id;
	public Sign sign;
	public Set<Generalization> intensional_definition;
	public Set<Example> extensional_definition;

	public Concept() {
		this.id = new NullConID();
		this.sign = new Sign("nullConcept");
		this.intensional_definition = new HashSet<>();
		this.extensional_definition = new HashSet<>();
	}

	/**
	 * Create a new {@link Concept} from three {@link SemioticElement}.
	 * 
	 * @param s the first {@link SemioticElement}, the {@link Sign}.
	 * @param I the second {@link SemioticElement}, the intentional definition as a
	 *          {@link Set} of {@link Generalization}.
	 * @param E the third {@link SemioticElement}, the extensional definition as a
	 *          {@link Set} of {@link Example}.
	 */
	public Concept(Sign s, Set<Generalization> I, Set<Example> E) {
		this.id = new ConID();
		this.sign = s.clone();
		this.intensional_definition = new HashSet<Generalization>();
		this.extensional_definition = new HashSet<Example>();

		for (Generalization g : I)
			this.intensional_definition.add(g.clone());
		for (Example e : E)
			this.extensional_definition.add(e.clone());
	}

	public Concept(ConID id, Sign s, Set<Generalization> I, Set<Example> E) {
		this.id = id;
		this.sign = s.clone();
		this.intensional_definition = new HashSet<Generalization>();
		this.extensional_definition = new HashSet<Example>();

		for (Generalization g : I)
			this.intensional_definition.add(g.clone());
		for (Example e : E)
			this.extensional_definition.add(e.clone());
	}

	// Get Informations

	/**
	 * Give the {@link Sign} of the {@link Concept}.
	 * 
	 * @return the {@link Sign}.
	 */
	public String sign() {
		return sign.toString();
	}

	/**
	 * Give the intensional definition.
	 * 
	 * @return the {@link Set} of {@link Generalization} that composes the
	 *         intensional definition.
	 */
	public Set<Generalization> intensional_definition() {
		return this.intensional_definition;
	}

	/**
	 * Give the extensional definition
	 * 
	 * @return the {@link Set} of {@link Example} that composes the extensional
	 *         definition
	 */
	public Set<Example> extensional_definition() {
		return this.extensional_definition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
	public Set<Example> getExtension(Container c) {
		Set<Example> out = new HashSet<>();
		for (Generalization g : intensional_definition) {
			out.addAll(g.getExtension(c));
		}
		return new HashSet<>(ToolSet.cleanDuplicates(out));
	}

	/**
	 * Give the {@link Generalization} from this {@link Concept}'s intensional
	 * definition that can generalize at least one {@link Example} from the given
	 * {@link SemioticElement} extension in the context of a {@link Container}.
	 * 
	 * @param e the {@link SemioticElement}.
	 * @param c the {@link Container}.
	 * @return the {@link Set} of {@link Generalization} that have a common
	 *         extension in the context of the {@link Container}.
	 */
	public Set<Generalization> Generalizes(SemioticElement e, Container c) {
		Set<Generalization> o = new HashSet<Generalization>();
		for (Generalization g : this.intensional_definition) {
			if (!ToolSet.disjoint(g.getExtension(c), e.getExtension(c)))
				o.add(g);
		}
		return o;
	}

	// Duplicate
	public Set<Generalization> copy_intensional_definition() {
		HashSet<Generalization> o = new HashSet<Generalization>();
		for (Generalization g : this.intensional_definition)
			o.add(g.clone());
		return o;
	}

	public Set<Example> copy_extensional_definition() {
		HashSet<Example> o = new HashSet<Example>();
		for (Example e : this.extensional_definition)
			o.add(e.clone());
		return o;
	}

	// Add elements
	public boolean addExamples(Set<Example> E) {
		HashSet<Example> to_add = new HashSet<Example>();
		for (Example e1 : this.extensional_definition) {
			for (Example e2 : E) {
				if (!e1.equivalent(e2))
					to_add.add(e1);
			}
		}
		return this.extensional_definition.addAll(to_add);

	}

	public boolean addGeneralizations(Set<Generalization> I) {
		HashSet<Generalization> to_add = new HashSet<Generalization>();
		for (Generalization g1 : this.intensional_definition) {
			for (Generalization g2 : I) {
				if (!g1.generalization().equivalents(g2.generalization()))
					to_add.add(g1);
			}
		}
		return this.intensional_definition.addAll(to_add);
	}

	// Delete elements
	public boolean removeExamples(Set<Example> E) {
		HashSet<Example> to_remove = new HashSet<Example>();
		for (Example e1 : this.extensional_definition) {
			for (Example e2 : E) {
				if (e1.equivalent(e2))
					to_remove.add(e1);
			}
		}
		return this.extensional_definition.removeAll(to_remove);
	}

	public boolean removeGeneralizations(Set<Generalization> I) {
		HashSet<Generalization> to_remove = new HashSet<Generalization>();
		for (Generalization g1 : this.intensional_definition) {
			for (Generalization g2 : I) {
				if (g1.generalization().equivalents(g2.generalization()))
					to_remove.add(g1);
			}
		}
		return this.intensional_definition.removeAll(to_remove);
	}

	// Test

	/**
	 * Test if the intensional definition of this {@link Concept} covers a given
	 * {@link Example}.
	 * 
	 * @param e the {@link Example}.
	 * @return <tt>true</tt> if the {@link Example} is covered by the intensional
	 *         definition.
	 * @throws FeatureTermException
	 */
	public boolean covers(Example e) {
		for (Generalization g : this.intensional_definition) {
			if (g.generalizes(e))
				return true;
		}
		return false;
	}

	public Concept clone() {
		return new Concept(this.id, sign.clone(), copy_intensional_definition(), copy_extensional_definition());
	}

	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		return equals((Concept) o);
	}

	public boolean equals(Concept id) {
		if (id == this)
			return true;
		if (id == null)
			return false;
		return this.id.equals(id.id);
	}

	/**
	 * Display in the consle the {@link Sign} of the {@link Concept}, the
	 * {@link Set} of its {@link Generalization} and the number of {@link Example}s
	 * from its extensional definition.
	 */
	public void display() {
		System.out.println("sign : " + this.sign());
		System.out.println("iDef : " + this.intensional_definition.size());
		System.out.println("eDef : " + this.extensional_definition.size());
	}

	/**
	 * Display in the consle the {@link Sign} of the {@link Concept}, the
	 * {@link Set} of its {@link Generalization} and the number of {@link Example}s
	 * from its extensional definition and the size of its extension in the context
	 * of a {@link Container}.
	 * 
	 * @param k the {@link Container}.
	 */
	public void display(Container k) {
		System.out.println("sign : " + this.sign());
		System.out.println("iDef : " + this.intensional_definition);
		System.out.println("eDef : " + this.extensional_definition.size());
		System.out.println(this.getExtension(k).size());
	}

	public String toString() {
		return this.sign() + "(" + this.id + ")";
	}

	public boolean isNull() {
		return false;
	}
}
