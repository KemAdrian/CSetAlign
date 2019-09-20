package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Path;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.TrainingSetProperties;

public class FTGen {
	
	// Saved training sets
	public static List<TrainingSetProperties> saved = new ArrayList<TrainingSetProperties>();
	public static Map<TrainingSetProperties, Integer> ft_shares = new HashMap<>();
	public static Map<TrainingSetProperties, List<List<Integer>>> id_sizes = new HashMap<>();
	public static  int current = -1;
	
	// Number of features
	public static int ft_number = 12;
	// (Distribution) Number of possible terms by feature
	public static double m_sort_size = 4;
	public static double sd_sort_size = 1;
	// (Distribution) Number of generalizations in an intenstional definition
	public static double m_id_size = 2;
	public static double sd_id_size = 1;
	// (Distribution) Number of attributes  in one generalization
	public static double m_gen_size = 2;
	public static double sd_gen_size = 1;
	// Random generator
	public static Random r;
	
	public static void initialize(int solution_number, boolean same_dataset, int min, int max, int pace, int nb) {
		// Create a new ontology
		Ontology base_ontology ;
		Ontology o;
		FTKBase dm;
		FTKBase case_base;
		if(!LPkg.initialized()) {
			// Initialize
			o = new Ontology();
			dm = new FTKBase();
			try {
				base_ontology = new BaseOntology();
				// Set up
				o.uses(base_ontology);
				dm.create_boolean_objects(o);
			} catch (Exception e) {
				System.out.println("Problem generating the new ontology");
			}
		}
		else {
			o = LPkg.ontology();
			dm = LPkg.dm();
		}
		// Define case base
		case_base = new FTKBase();
		case_base.uses(dm);
		LPkg.initialize(null, o, dm, null, null, new HashSet<FeatureTerm>());
		// If the same dataset must be use, generate a big one and fragment it
		if(same_dataset) {
			TrainingSetProperties model = FTGen.generate(max, solution_number, o, dm, case_base);
			List<FeatureTerm> generated_cases =  new ArrayList<>();
			LinkedList<FeatureTerm> sorted_cases = new LinkedList<>();
			Map<FeatureTerm,LinkedList<FeatureTerm>> general_dataset = new HashMap<>();
			for(FeatureTerm ft : model.differentSolutions())
				general_dataset.put(ft, new LinkedList<FeatureTerm>());
			for(FeatureTerm ft : model.cases)
				general_dataset.get(ft.readPath(model.solution_path)).add(ft);
			// Create an alternate list of examples
			Iterator<FeatureTerm> iterator = new Loop<FeatureTerm>(general_dataset.keySet()).iterator();
			boolean stop = false;
			while(!stop) {
				LinkedList<FeatureTerm> current = general_dataset.get(iterator.next());
				if(!current.isEmpty())
					sorted_cases.add(current.remove());
				stop = true;
				for(LinkedList<FeatureTerm> list : general_dataset.values())
					if(!list.isEmpty())
						stop = false;
			}
			// For every lenght, get that amount of examples from the general dataset
			for(int i=min; i<max; i+=pace) {
				int nb_add = (i==min)? i : pace;
				System.out.println("number of examples to have in the dataset : "+nb_add);
				for (int j = 0; j < nb_add; j++) {
					generated_cases.add(sorted_cases.remove());
				}
				// Add "nb" new training
				for(int j=0; j<nb; j++) {
					TrainingSetProperties out = new TrainingSetProperties();
					out.description_path = model.description_path;
					out.solution_path = model.solution_path;
					out.cases = new ArrayList<>(generated_cases);
					saved.add(out);
					current = saved.size() -1;
					id_sizes.put(out, id_sizes.get(model));
					ft_shares.put(out, ft_shares.get(model));
				}
			}
			// Remove model
			saved.remove(model);
			id_sizes.remove(model);
			ft_shares.remove(model);
			current = saved.size() -1;
		}
		else {
			// Eeach time, create a new dataset
			for(int i=min; i<max; i+=pace)
				FTGen.add(i, solution_number, o, dm, case_base, nb);
		}
	}
	
	public static void initialize(int example_number, int solution_number, Ontology o, FTKBase dm, FTKBase cases, int nb) {
		FTGen.saved = new ArrayList<TrainingSetProperties>();
		FTGen.ft_shares = new HashMap<>();
		FTGen.current = -1;
		System.out.println(" CURRENT NUMBER OF AVAILABLE DATA-SETS: "+saved.size());
		for(int i=0; i<nb; i++)
			FTGen.generate(example_number, solution_number, o, dm, cases);
		System.out.println(" NEW NUMBER OF AVAILABLE DATA-SETS: "+saved.size());
	}
	
	public static void add(int example_number, int solution_number, Ontology o, FTKBase dm, FTKBase cases, int nb) {
		System.out.println(" CURRENT NUMBER OF AVAILABLE DATA-SETS: "+saved.size());
		for(int i=0; i<nb; i++)
			FTGen.generate(example_number, solution_number, o, dm, cases);
		System.out.println(" NEW NUMBER OF AVAILABLE DATA-SETS: "+saved.size());
	}
	
	public static TrainingSetProperties getNext() {
		current ++;
		if(current >= saved.size())
			current = 0;
		return saved.get(current);
	}
	
	public static Integer getSharedFtFromCreation(TrainingSetProperties data_set) {
		return ft_shares.get(data_set);
	}
	
	public static String getDimensionFromCreation(TrainingSetProperties data_set) {
		return id_sizes.get(data_set).toString();
	}
	
	public static TrainingSetProperties generate(int example_number, int solution_number, Ontology o, FTKBase dm, FTKBase cases) {
		TrainingSetProperties out = null;
		try {
			out = generateInternal(example_number, solution_number, o, dm, cases);
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		return out;
	}

	private static TrainingSetProperties generateInternal(int example_number, int solution_number, Ontology o, FTKBase dm, FTKBase cases) throws FeatureTermException {
		r = new Random();
		TrainingSetProperties out = new TrainingSetProperties();
		// Generate Ontology structure
		List<Symbol> attributes = new ArrayList<>();
		List<TermFeatureTerm> solutions = new ArrayList<>();
		Map<Symbol,List<TermFeatureTerm>> values = new HashMap<>();
		Map<TermFeatureTerm,List<Map<Symbol, TermFeatureTerm>>> rules = new HashMap<>();
		// Create the sorts
		Sort any = o.getSort("any");
		any.setDataType(3);
		Sort ontology = new Sort(new Symbol("test-ontology"), any, o);
		Sort cas = new Sort(new Symbol("case"), ontology,o);
		Sort object = new Sort(new Symbol("object"), ontology,o);
		Sort label = new Sort(new Symbol("label"), ontology, o);
		Sort feature = new Sort(new Symbol("feature"),ontology,o);
		// Create the common symbols
		Symbol description = new Symbol("description");
		Symbol solution = new Symbol("solution");
		// Create the different attributes
		for(int i=0; i<ft_number; i++) {
			Symbol symbol = new Symbol("attribute_"+(i+1)); 
			attributes.add(symbol);
			values.put(symbol, new ArrayList<>());
		}
		// Create the different values
		for(Symbol att : attributes) {
			// Select a random number of values, from a normal distribution
			long nb_values = Math.round(r.nextGaussian()*sd_sort_size+m_sort_size);
			for(int i=0; i<nb_values; i++) 
				values.get(att).add(new TermFeatureTerm(new Symbol("value_"+att.toString()+"_"+i), feature));
		}
		// Create the different solutions
		for(int i=0; i<solution_number; i++) {
			// Create new solution
			TermFeatureTerm sol = new TermFeatureTerm("solution_"+(i+1), label);
			solutions.add(sol);
			// Initialize new intensional definition
			List<Map<Symbol,TermFeatureTerm>> id = new ArrayList<>();
			// Create a new intensional definition until it is acceptable
			boolean works = false;
			while(!works) {
				// Restart the intensional definition from scratch
				id = new ArrayList<>();
				// Decide of a size
				long id_size = Math.round(r.nextGaussian() * sd_id_size + m_id_size);
				id_size = Math.max(1, id_size);
				// Create the different generalizations
				for (int j = 0; j < id_size; j++) {
					long gen_size = Math.round(r.nextGaussian() * sd_gen_size + m_gen_size);
					gen_size = Math.max(1, gen_size);
					Map<Symbol, TermFeatureTerm> gen = new HashMap<>();
					List<Symbol> available = new ArrayList<>(attributes);
					for (int k = 0; k < gen_size; k++) {
						// Get random attribute from the availables
						Symbol sy = available.remove(r.nextInt(available.size()));
						gen.put(sy, values.get(sy).get(r.nextInt(values.get(sy).size())));
					}
					id.add(gen);
				}
				// Test the intensional definition
				if(rules.isEmpty())
					works = true;
				for(List<Map<Symbol, TermFeatureTerm>> idef : rules.values()) {
					boolean all_same_generalizations = true;
					for(Map<Symbol, TermFeatureTerm> gen1 : id) {
						for(Map<Symbol, TermFeatureTerm> gen2 : idef) {
							boolean all_same_features = true;
							for(Symbol ft1 : gen1.keySet()) {
								boolean has_equivalent = false;
								for(Symbol ft2 : gen2.keySet()) {
									if(ft1.equals(ft2) && gen1.get(ft1).equivalents(gen2.get(ft2)))
										has_equivalent = true;
								}
								if(!has_equivalent) {
									all_same_features = false;
									break;
								}
							}
							if(!all_same_features) {
								all_same_generalizations = false;
								break;
							}
						}
					}
					if(!all_same_generalizations) {
						works = true;
						break;
					}
				}
			}
			rules.put(sol, id);
		}
		// Check how many generalizations are sharing  a feature value while belonging to different solutions
		int ft_shared_count = 0;
		List<List<Map<Symbol, TermFeatureTerm>>> i_defs = new ArrayList<>(rules.values());
		for(int i=0; i<i_defs.size(); i++) {
			List<Map<Symbol, TermFeatureTerm>> id1 = i_defs.get(i);
			for(int j=i+1; j<i_defs.size(); j++) {
				List<Map<Symbol, TermFeatureTerm>> id2 = i_defs.get(j);
				for(Map<Symbol, TermFeatureTerm> gen1 : id1) 
					for(Map<Symbol, TermFeatureTerm> gen2 : id2) 
						for(Symbol ft1 : gen1.keySet()) 
							for(Symbol ft2 : gen2.keySet()) 
								if(ft1.equals(ft2) && gen1.get(ft1).equivalents(gen2.get(ft2)))
									ft_shared_count ++;
			}
		}
		// Add features to sorts
		cas.addFeature(description, object, null, false);
		cas.addFeature(solution, label, null, false);
		for(Symbol attribute : attributes)
			object.addFeature(attribute, feature, null, false);
		// Add to ontology and dm
		o.addSort(ontology);
		o.addSort(cas);
		o.addSort(object);
		o.addSort(label);
		o.addSort(feature);
		for(TermFeatureTerm ft : solutions)
			dm.addFT(ft);
		for(List<TermFeatureTerm> l : values.values())
			for(TermFeatureTerm ft : l)
				dm.addFT(ft);
		// Create the different examples
		out.cases = new ArrayList<>();
		for(int i=0; i<example_number; i++) {
			TermFeatureTerm ft_case = new TermFeatureTerm("case-"+i, cas);
			TermFeatureTerm ft_description = new TermFeatureTerm("object-"+i, object);
			TermFeatureTerm ft_solution = solutions.get(i%solutions.size());
			// Chose generalization
			Map<Symbol, TermFeatureTerm> ft_attributes = rules.get(ft_solution).get(r.nextInt(rules.get(ft_solution).size()));
			for(Symbol sy : attributes) {
				if(ft_attributes.keySet().contains(sy))
					ft_description.addFeatureValue(sy, ft_attributes.get(sy));
				else
					ft_description.addFeatureValue(sy, values.get(sy).get(r.nextInt(values.get(sy).size())));
			}
			ft_case.addFeatureValue(description, ft_description);
			ft_case.addFeatureValue(solution, ft_solution);
			out.cases.add(ft_case);
			cases.addFT(ft_case);
		}
		out.description_path = new Path();
		out.solution_path = new Path();
		out.description_path.features.add(description);
		out.solution_path.features.add(solution);
		for(TermFeatureTerm s : solutions) {
			System.out.println("> solution:"+s.toStringNOOS(dm));
			System.out.println("> Idef:");
			for(Map<Symbol, TermFeatureTerm>  idef : rules.get(s)) {
				System.out.println("  > Gen:");
				for(Symbol sy : idef.keySet()) {
					System.out.println("      "+sy+" = "+idef.get(sy).toStringNOOS(dm));
				}
			}
			System.out.println("");
		}
		// Save sizes of rules
		List<List<Integer>> rules_size = new ArrayList<>();
		for(FeatureTerm sol : rules.keySet()) {
			List<Integer> gens_size = new ArrayList<>();
			for(Map<Symbol, TermFeatureTerm> gen : rules.get(sol)) {
				gens_size.add(gen.keySet().size());
			}
			rules_size.add(gens_size);
		}
		// Save dataset
		saved.add(out);
		current = saved.size() -1;
		id_sizes.put(out, rules_size);
		ft_shares.put(out, ft_shared_count);
		return out;
	}
	
}
