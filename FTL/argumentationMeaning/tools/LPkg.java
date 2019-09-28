package tools;

import csic.iiia.ftl.base.core.*;
import csic.iiia.ftl.base.utils.FeatureTermException;
import semiotic_elements.Sign;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LPkg {
	
	private static Ontology o;
	private static FTKBase dm;
	private static Path dp,sp;
	private static FeatureTerm generic;
	private static Set<FeatureTerm> different_solutions;
	private static boolean initialized = false;
	
	public static void initialize(FeatureTerm g, Ontology o, FTKBase dm, Path dp, Path sp, Set<FeatureTerm> ds){
		// Ontology information
		LPkg.o = o;
		LPkg.dm = dm;
		LPkg.dp = dp;
		LPkg.sp = sp;
		LPkg.generic = g;
		LPkg.different_solutions = ds;
		LPkg.initialized = true;
		
		// New binary levels for discussion
		Sign.reset();
	}
	
	public static FeatureTerm generic(){
		if(generic == null)
			return null;
		try {
			return generic.clone(dm, o);
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Ontology ontology(){
		return LPkg.o;
	}
	
	public static FTKBase dm(){
		return LPkg.dm;
	}
	
	public static Path description_path(){
		return LPkg.dp;
	}
	
	public static Path solution_path(){
		return LPkg.sp;
	}
	
	public static Sort description_sort(){
		try {
			return o.getSort(description_path().getEnd());
		} catch (FeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Sort solution_sort(){
		try {
			return o.getSort(solution_path().getEnd());
		} catch (FeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Set<FeatureTerm> different_solutions(){
		return LPkg.different_solutions;
	}
	
	public static void set_different_solutions(Collection<FeatureTerm> d_s) {
		LPkg.different_solutions = new HashSet<>(d_s);
	}

	static void set_paths(Path d_path, Path s_path){
		dp = d_path;
		sp = s_path;
	}

	static void set_generic(FeatureTerm g){
		generic = g;
	}
	
	public static FeatureTerm createFeature(FeatureTerm d, FeatureTerm s){
		TermFeatureTerm f = (TermFeatureTerm) LPkg.generic();
		try {
			assert f != null;
			f.setName(new Symbol(UUID.randomUUID().toString()));
			f.defineFeatureValue(dp, d);
			f.defineFeatureValue(sp, s);
		} catch (FeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
	
	public static boolean initialized(){
		return LPkg.initialized;
	}

}
