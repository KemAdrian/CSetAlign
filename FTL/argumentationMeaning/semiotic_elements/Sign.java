package semiotic_elements;

import java.util.HashSet;
import java.util.Set;

import containers.ContrastSet;
import interfaces.Container;
import interfaces.SemioticElement;

/**
 * {@link Sign}s are the {@link SemioticElement} that represent the symbols used in the communication.
 * 
 * @author kemoadrian
 *
 */
public class Sign {
	
	public static int newSymbol = 0;	
	public String symbol;
	
	/**
	 * Creates a new {@link Sign} from a single {@link String}.
	 * @param pieceandcake the {@link String} refering to the {@link ContrastSet} from which the {@link Concept} of this {@link Sign} belongs followed by the individual name of this {@link Concept} as a {@link String}, separated by a colon.
	 */
	public Sign(String symlbol){
		this.symbol = symlbol;
	}
	
	public static String getNewSymbol(){
		String o = "newSign_"+newSymbol;
		newSymbol ++;
		return o;
	}
	
	public static void reset() {
		newSymbol = 0;
	}
	
	/* (non-Javadoc)
	 * @see interfaces.SemioticElement#getExtension(interfaces.Container)
	 */
	public Set<Example> getExtension(Container c){
		Set<Example> o = new HashSet<>();
		for(Concept cp : c.getAllConcepts()){
			if(cp.sign().equals(this.toString()))
				o.addAll(cp.extensional_definition());
		}
		return o;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return this.symbol;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Sign clone(){
		return new Sign(this.symbol);
	}

}
