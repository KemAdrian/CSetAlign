package tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import csic.iiia.ftl.base.core.FeatureTerm;
import semiotic_elements.Example;

public class FTConv {
	
	public static Set<Example> ftToEx(Collection<FeatureTerm> fts){
		Set<Example> out = new HashSet<>();
		if(!LPkg.initialized()) {
			System.out.println("   > PROBLEM: Initialize Learning Package first!");
			return out;
		}
		for(FeatureTerm ft : fts) {
			out.add(new Example(ft.readPath(LPkg.description_path())));
		}
		return out;
	}
	
	public static Set<FeatureTerm> exToft(Collection<Example> exs, FeatureTerm label){
		Set<FeatureTerm> out = new HashSet<>();
		if(!LPkg.initialized()) {
			System.out.println("   > PROBLEM: Initialize Learning Package first!");
			return out;
		}
		for(Example ex : exs) {
			out.add(LPkg.createFeature(ex.featureterm, label));
		}
		return out;
	}
	
	public static Set<FeatureTerm> contextToLearningSet(Map<FeatureTerm, Set<Example>> context){
		Set<FeatureTerm> out = new HashSet<>();
		if(!LPkg.initialized()) {
			System.out.println("   > PROBLEM: Initialize Learning Package first!");
			return out;
		}
		for(FeatureTerm label : context.keySet())
			for(Example ex : context.get(label))
				out.add(LPkg.createFeature(ex.featureterm, label));
		return out;
	}

}
