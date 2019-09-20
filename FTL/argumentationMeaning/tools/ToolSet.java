package tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import interfaces.Agent;
import semiotic_elements.Example;
import semiotic_elements.Generalization;

public class ToolSet {
	
	public static int THRESHOLD = 0;
	public static Random rand = new Random();
	
	
	public static boolean contains(Collection<Example> E, Example e) {
		for(Example e1 : E)
			if(e1.equivalent(e))
				return true;
		return false;
	}
	
	public static Collection<Example> union(Collection<Example> E1, Collection<Example> E2){
		Collection<Example> U = new HashSet<>();
		U.addAll(E1);
		U.addAll(E2);
		U = ToolSet.cleanDuplicates(U);
		return U;
	}
	
	public static Collection<Example> intersection(Collection<Example> E1, Collection<Example> E2){
		Collection<Example> I = new HashSet<>();
		for(Example e1: E1){
			boolean add = false;
			for(Example e2 : E2){
				if(e1.equivalent(e2)){
					add = true;
					break;
				}
			}
			if(add)
				I.add(e1);
		}
		return I;
	}
	
	public static Collection<Example> extrusion(Collection<Example> E1, Collection<Example> E2){
		Collection<Example> E = new HashSet<>();
		Collection<Example> O = union(E1, E2);
		Collection<Example> I = intersection(E1, E2);
		for(Example e1 : O){
			boolean add = true;
			for(Example e2 : I){
				if(e1.equivalent(e2)){
					add = false;
					break;
				}
			}
			if(add)
				E.add(e1);
		}
		return E;
	}
	
	/**
	 * Return the {@link Example}s from a {@link Set} that are not present in another {@link Set}.
	 * @param E The {@link Set} with the {@link Example}s to substract from
	 * @param substracted The {@link Set} with the {@link Example} to be substracted
	 * @return The {@link Example}s from the {@link Set} E without the {@link Example}s from the {@link Set} substracted
	 */
	public static Collection<Example> substract(Collection<Example> E, Collection<Example> substracted){
		Collection<Example> S = new HashSet<>();
		for(Example e1 : E){
			boolean add = true;
			for(Example e2 : substracted){
				if(e1.equivalent(e2)){
					add = false;
					break;
				}
			}
			if(add)
				S.add(e1);
		}
		return S;
	}
	
	public static boolean emptySet(Collection<Example> E){
		return !(E.size() >= THRESHOLD);
	}
	
	public static boolean equivalent(Collection<Example> E1, Collection<Example> E2){
		return emptySet(substract(E1, E2)) && emptySet(substract(E2, E1));
	}
	
	public static boolean disjoint(Collection<Example> E1, Collection<Example> E2){
		return (!equivalent(E1, E2) && emptySet(intersection(E1, E2)));
	}
	
	public static boolean contains(Collection<Example> E1, Collection<Example> E2){
		return (!disjoint(E1, E2) && emptySet(extrusion(E2, intersection(E1, E2)))); 
	}
	
	public static boolean included(Collection<Example> E1, Collection<Example> E2){
		return (contains(E1, E2) || contains(E2, E1));
	}
	
	public static boolean overlap(Collection<Example> E1, Collection<Example> E2){
		return(!disjoint(E1, E2) && !included(E1, E2));
	}
	
	public static Set<Example> adjunctSet(Generalization g, Set<Example> context) {
		Set<Example> out = new HashSet<>();
		for (Example e : context)
			if (g.generalizes(e))
				out.add(e);
		return out;
	}
	
	public static Set<Example> adjunctSet(Set<Generalization> I, Set<Example> context){
		Set<Example> out = new HashSet<>();
		for(Example e : context)
			for(Generalization g : I)
				if(g.generalizes(e))
					out.add(e);
		return out;
	}
	
	public static Set<Example> duplicate(Set<Example> to_copy){
		Set<Example> output = new HashSet<>();
		for(Example e : to_copy)
			output.add(e.clone());
		return output;
	}
	
	public static Collection<Example> cleanDuplicates(Collection<Example> E){
		Collection<Example> clean = new HashSet<>();
		for(Example e1 : E){
			boolean add = true;
			for(Example e2 : clean){
				if(e1.equivalent(e2)){
					add = false;
					break;
				}
			}
			if(add)
				clean.add(e1);
		}
		return clean;
	}
	
	public static Collection<Example> optiRandomSubset(Agent agent, Collection<Example> set, int size){
		Collection<Example> out = new HashSet<Example>();
		ArrayList<Example> candidates = new ArrayList<>(set);
		ArrayList<Example> exchanged = new ArrayList<>(agent.e_exchanged());
		ArrayList<Example> high_priority = new ArrayList<>(ToolSet.intersection(candidates, exchanged));
		ArrayList<Example> low_priority = new ArrayList<>(ToolSet.substract(candidates, high_priority));
		int min = (set.size() < size)? set.size() : size;
		if(high_priority.size() == min)
			return high_priority;
		else if(high_priority.size() > min) {
			while(out.size() < min)
				out.add(high_priority.remove(rand.nextInt(high_priority.size())));
			return out;
		}
		else {
			out.addAll(high_priority);
			while(out.size() < min)
				out.add(low_priority.remove(rand.nextInt(low_priority.size())));
			return out;
		}
	}

}
