package semiotic_elements;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import identifiers.ExID;
import interfaces.Agent;
import interfaces.Container;
import interfaces.SemioticElement;
import tools.LPkg;

/**
 * The representation that an {@link Agent} has about objects from its environement.
 * 
 * @author kemoadrian
 *
 */
public class Example implements SemioticElement{
	
	public ExID example;
	public FeatureTerm featureterm;
	
	/**
	 * Create a new {@link Example} from a given {@link FeatureTerm} that describes it.
	 * @param f the {@link FeatureTerm}.
	 */
	public Example(FeatureTerm f) {
		this.example = new ExID();
		if(LPkg.initialized())
			try {
				this.featureterm = f.clone(LPkg.dm(), LPkg.ontology());
			} catch (FeatureTermException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	/**
	 * Test if the {@link FeatureTerm} of this {@link Example} is equivalent to the {@link FeatureTerm} of a given {@link Example}.
	 * Two different instances of {@link FeatureTerm} can be equivalent if they are 
	 * @param e the other {@link Example}.
	 * @return <tt>true</tt> if the two {@link FeatureTerm} are equivalent.
	 */
	public boolean equivalent(Example e) {
		return this.featureterm.equivalents(e.representation());
	}
	
	/**
	 * Give the unique {@link UUID} identifier of this example. 
	 * @return the {@link UUID}.
	 */
	public ExID example(){
		return this.example;
	}
	
	/**
	 * Give the {@link FeatureTerm} that represents this {@link Example}.
	 * @return the {@link FeatureTerm}.
	 */
	public FeatureTerm representation(){
		return this.featureterm;
	}
	
	/* (non-Javadoc)
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
	public Set<Example> getExtension(Container c){
		Set<Example> o = new HashSet<>();
		for(Example e : c.getContext()){
			if(e.equals(this)){
				o.add(this);
				return o;
			}	
		}
		return o;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
	    if (o == null) return false;
	    if (getClass() != o.getClass()) return false;
	    return equals((Example) o);
	}
	
	public boolean equals(Example e) {
		if (e == this) return true;
	    if (e == null) return false;
		return this.equivalent(e);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Example clone(){
		Example out = new Example(this.representation());
		out.example = this.example;
		return out;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "example_"+example.toString();
	}

}
