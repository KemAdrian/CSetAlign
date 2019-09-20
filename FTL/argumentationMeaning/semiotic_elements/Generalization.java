package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import identifiers.GenID;
import interfaces.Agent;
import interfaces.Container;
import interfaces.SemioticElement;
import tools.LPkg;

/**
 * The component of the intensional definition. It represents the formalized knowledge that an {@link Agent} has about a {@link Concept}.
 * 
 * @author kemoadrian
 *
 */
public class Generalization implements SemioticElement{
	
	public GenID id;
	public FeatureTerm generalization;
	
	/**
	 * Create a new {@link Generalization} from a {@link FeatureTerm}
	 * @param g a {@link FeatureTerm} that subsumes {@link Example}s' {@link FeatureTerm} from the extensional definition.
	 * @throws FeatureTermException
	 */
	public Generalization(FeatureTerm g){
		this.id = new GenID();
		if(LPkg.initialized()) {
			try {
				this.generalization = g.clone(LPkg.dm(), LPkg.ontology());
			} catch (FeatureTermException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Give the {@link FeatureTerm} related to this {@link Generalization}.
	 * @return the {@link FeatureTerm}.
	 */
	public FeatureTerm generalization(){
		return this.generalization;
	}
	
	/**
	 * Test if the {@link FeatureTerm} of an other {@link Generalization} is equivalent to this one's.
	 * This method uses the method {@link csic.iiia.ftl.base.core.FeatureTerm#equivalents(FeatureTerm)}.
	 * @param g the other {@link Generalization}.
	 * @return <tt>true</tt> if the {@link FeatureTerm} are equivalent.
	 * @throws FeatureTermException
	 */
	public boolean equals(Generalization g){
		boolean out = false;
		out = generalization.equivalents(g.generalization());
		return out;
	}
	
	/**
	 * Test if this {@link Generalization} generalizes the given {@link Example}.
	 * This method tries to subsume the {@link FeatureTerm} of the {@link Example} with the {@link FeatureTerm} of this instance.
	 * @param e the {@link Example} to test.
	 * @return <tt>true</tt> if the {@link Example}s is generalized by this instance.
	 * @throws FeatureTermException
	 */
	public boolean generalizes(Example e){
		boolean out = false;
		try {
			out = generalization().subsumes(e.representation());
		} catch (FeatureTermException e1) {
			e1.printStackTrace();
		}
		return out;
	}
	
	/**
	 * Test if this {@link Generalization} generalizes the given {@link Set} of {@link Example}.
	 * This method tries to subsume all the {@link FeatureTerm}s of the {@link Set} with the {@link FeatureTerm} of this instance.
	 * @param E the {@link Set} of {@link Example}s to test.
	 * @return <tt>true</tt> if all the {@link Example} are generalized by this instance.
	 * @throws FeatureTermException
	 */
	public boolean generalizes(Set<Example> E){
		for(Example e : E){
			try {
				if(!this.generalization().subsumes(e.representation()))
					return false;
			} catch (FeatureTermException e1) {
				e1.printStackTrace();
			}
		}
		return true;
	}
	
	public boolean generalizes(FeatureTerm f) {
		try {
			return this.generalization.subsumes(f);
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean generalizes(Generalization g) {
		return this.generalizes(g.generalization);
	}
	
	/* (non-Javadoc)
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
	public Set<Example> getExtension(Container c){
		HashSet<Example> o = new HashSet<Example>();
		for(Example e : c.getContext()){
			if(this.generalizes(e))
				o.add(e);
		}
		return o;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Generalization clone(){
		Generalization clone = new Generalization(this.generalization());
		clone.id = id;
		return clone;
	}

}
