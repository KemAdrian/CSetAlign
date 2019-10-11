package tools;

import csic.iiia.ftl.base.core.*;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.TrainingSetProperties;

import java.util.*;

public class FTGen {
	
	// Saved training sets
	public static List<TrainingSetProperties> saved = new ArrayList<>();
	private static Map<TrainingSetProperties, Integer> ft_shares = new HashMap<>();
	private static Map<TrainingSetProperties, List<List<Integer>>> id_sizes = new HashMap<>();
	private static Map<TrainingSetProperties, List<Rule>> saved_rules = new HashMap<>();
	public static  int current = -1;
	
	// Number of features
	public static int ft_number = 12;
	// (Distribution) Number of possible terms by feature
	public static double m_sort_size = 10;
	public static double sd_sort_size = 0.;
	// (Distribution) Number of generalizations in an intentional definition
	public static double m_id_size = 1;
	public static double sd_id_size = 0.;
	// (Distribution) Number of attributes  in one generalization
	public static double m_gen_size = 4 ;
	public static double sd_gen_size = 3.;
	// Random generator
	public static Random r;

	public static void parametric_initialize(int solution_number, int min, int max, int pace, int nb, Loop<Pair<Double, Double>> sort_size_list, Loop<Pair<Double, Double >> id_size_list, Loop<Pair<Double, Double>> gen_size_list ){
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
        LPkg.initialize(null, o, dm, null, null, new HashSet<>());
        // Save the old parameters
        Pair<Double, Double> old_sort_sizes = new Pair<>(m_sort_size, sd_sort_size);
        Pair<Double, Double> old_id_sizes = new Pair<>(m_id_size, sd_id_size);
        Pair<Double, Double> old_gen_sizes = new Pair<>(m_gen_size, sd_gen_size);
        // Create loop iterators
        Iterator<Pair<Double, Double>> sort_size_iterator = sort_size_list.listIterator();
        Iterator<Pair<Double, Double>> id_size_iterator = id_size_list.listIterator();
        Iterator<Pair<Double, Double>> gen_size_iterator = gen_size_list.listIterator();
        // For each n, create a different set of different length data sets
        List<List<TrainingSetProperties>> training_sets = new ArrayList<>();
        for(int i=0; i<nb; i++){
            Pair<Double, Double> current_sort_sizes = sort_size_iterator.next();
            Pair<Double, Double> current_id_sizes = id_size_iterator.next();
            Pair<Double, Double> current_gen_sizes = gen_size_iterator.next();
            m_sort_size = current_sort_sizes.getLeft();
            sd_sort_size = current_sort_sizes.getRight();
            m_id_size = current_id_sizes.getLeft();
            sd_id_size = current_id_sizes.getRight();
            m_gen_size = current_gen_sizes.getLeft();
            sd_gen_size = current_gen_sizes.getRight();
            training_sets.add(differentLengthDataSet(solution_number, min, max, pace, o, dm, case_base));
        }
        // Restore parameters values
        m_sort_size = old_sort_sizes.getLeft();
        sd_sort_size = old_sort_sizes.getRight();
        m_id_size = old_id_sizes.getLeft();
        sd_id_size = old_id_sizes.getRight();
        m_gen_size = old_gen_sizes.getLeft();
        sd_gen_size = old_gen_sizes.getRight();
        // Add each of them sequentially
        for(int i=min; i<max; i+=pace){
            for(List<TrainingSetProperties> t_test : training_sets){
                TrainingSetProperties to_add = new TrainingSetProperties();
                TrainingSetProperties model = t_test.get((i-min)/pace);
                to_add.cases = new ArrayList<>(model.cases);
                to_add.description_path = model.description_path;
                to_add.solution_path = model.solution_path;
                saved.add(to_add);
                current = saved.size() -1;
                id_sizes.put(to_add, id_sizes.get(model));
                ft_shares.put(to_add, ft_shares.get(model));
                saved_rules.put(to_add, saved_rules.get(model));
            }
        }
        // Remove the models
        for(List<TrainingSetProperties> t_set : training_sets){
            for(TrainingSetProperties model : t_set){
                saved.remove(model);
                id_sizes.remove(model);
                ft_shares.remove(model);
                saved_rules.remove(model);
                current = saved.size() -1;
            }
        }
	}
	
	public static void initialize(int solution_number, boolean same_data_set, boolean sequential, int min, int max, int pace, int nb) {
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
		LPkg.initialize(null, o, dm, null, null, new HashSet<>());
		// If the same data set must be use, generate a big one and fragment it
		if(same_data_set){
			// Add new data sets
			for(TrainingSetProperties model : differentLengthDataSet(solution_number, min, max, pace, o, dm, case_base)){
				for(int i=0; i<nb; i++){
					TrainingSetProperties to_add = new TrainingSetProperties();
					to_add.cases = new ArrayList<>(model.cases);
					to_add.description_path = model.description_path;
					to_add.solution_path = model.solution_path;
					saved.add(to_add);
					current = saved.size() -1;
					id_sizes.put(to_add, id_sizes.get(model));
					ft_shares.put(to_add, ft_shares.get(model));
					saved_rules.put(to_add, saved_rules.get(model));
					// Remove old data sets
					saved.remove(model);
					id_sizes.remove(model);
					ft_shares.remove(model);
					saved_rules.remove(model);
					current = saved.size() -1;
				}
			}
		}
		else if(sequential) {
			// For each n, create a different set of different length data sets
			List<List<TrainingSetProperties>> training_sets = new ArrayList<>();
			for(int i=0; i<nb; i++){
				training_sets.add(differentLengthDataSet(solution_number, min, max, pace, o, dm, case_base));
			}
			// Add each of them sequentially
			for(int i=min; i<max; i+=pace){
				for(List<TrainingSetProperties> t_test : training_sets){
					TrainingSetProperties to_add = new TrainingSetProperties();
					TrainingSetProperties model = t_test.get((i-min)/pace);
					to_add.cases = new ArrayList<>(model.cases);
					to_add.description_path = model.description_path;
					to_add.solution_path = model.solution_path;
					saved.add(to_add);
					current = saved.size() -1;
					id_sizes.put(to_add, id_sizes.get(model));
					ft_shares.put(to_add, ft_shares.get(model));
					saved_rules.put(to_add, saved_rules.get(model));
				}
			}
			// Remove the models
			for(List<TrainingSetProperties> t_set : training_sets){
				for(TrainingSetProperties model : t_set){
					saved.remove(model);
					id_sizes.remove(model);
					ft_shares.remove(model);
					saved_rules.remove(model);
					current = saved.size() -1;
				}
			}
		}
		else {
			// Each time, create a new data set
			for(int i=min; i<max; i+=pace)
				FTGen.add(i, solution_number, o, dm, case_base, nb);
		}
	}

	private static List<TrainingSetProperties> differentLengthDataSet(int solution_number, int min, int max, int pace, Ontology o, FTKBase dm, FTKBase case_base){
		// Create a model for the new data set that will be declined in different sizes
		List<TrainingSetProperties> out = new ArrayList<>();
		TrainingSetProperties model = FTGen.generate(max, solution_number, o, dm, case_base);
		List<FeatureTerm> generated_cases =  new ArrayList<>();
		LinkedList<FeatureTerm> sorted_cases = new LinkedList<>();
		Map<FeatureTerm,LinkedList<FeatureTerm>> general_data_set = new HashMap<>();
		for(FeatureTerm ft : model.differentSolutions())
			general_data_set.put(ft, new LinkedList<>());
		for(FeatureTerm ft : model.cases)
			general_data_set.get(ft.readPath(model.solution_path)).add(ft);
		// Create an alternate list of examples
		Iterator<FeatureTerm> iterator = new Loop<>(general_data_set.keySet()).iterator();
		boolean stop = false;
		while(!stop) {
			LinkedList<FeatureTerm> current = general_data_set.get(iterator.next());
			if(!current.isEmpty())
				sorted_cases.add(current.remove());
			stop = true;
			for(LinkedList<FeatureTerm> list : general_data_set.values())
				if (!list.isEmpty()) {
					stop = false;
					break;
				}
		}
		for(int i=min; i<max; i+=pace) {
			int nb_add = (i==min)? i : pace;
			System.out.println("number of examples to have in the data set : "+nb_add);
			for (int j = 0; j < nb_add; j++) {
				generated_cases.add(sorted_cases.remove());
			}
			// Add "nb" new training
			TrainingSetProperties to_add = new TrainingSetProperties();
			to_add.description_path = model.description_path;
			to_add.solution_path = model.solution_path;
			to_add.cases = new ArrayList<>(generated_cases);
			saved.add(to_add);
			current = saved.size() -1;
			id_sizes.put(to_add, id_sizes.get(model));
			ft_shares.put(to_add, ft_shares.get(model));
			saved_rules.put(to_add, saved_rules.get(model));
			out.add(to_add);
		}
		// Remove Model
		saved.remove(model);
		id_sizes.remove(model);
		ft_shares.remove(model);
		saved_rules.remove(model);
		current = saved.size() -1;
		return out;
	}
	
	public static void initialize(int example_number, int solution_number, Ontology o, FTKBase dm, FTKBase cases, int nb) {
		FTGen.saved = new ArrayList<>();
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
			// Initialize new intentional definition
			List<Map<Symbol,TermFeatureTerm>> id = new ArrayList<>();
			// Create a new intentional definition until it is acceptable
			boolean works = false;
			System.out.println("	> Creating a new rule...");
			while(!works) {
				// Restart the intentional definition from scratch
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
						// Get random attribute from the available
						Symbol sy = available.remove(r.nextInt(available.size()));
						gen.put(sy, values.get(sy).get(r.nextInt(values.get(sy).size())));
					}
					id.add(gen);
				}
				// Test the intentional definition
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
			System.out.println("	> Rule created!");
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
		// Save sizes of rules
		List<List<Integer>> rules_size = new ArrayList<>();
		for(TermFeatureTerm sol : rules.keySet()) {
			List<Integer> gens_size = new ArrayList<>();
			for(Map<Symbol, TermFeatureTerm> gen : rules.get(sol)) {
				gens_size.add(gen.keySet().size());
			}
			rules_size.add(gens_size);
		}
		// Save rules
		List<Rule> to_add_rules = new ArrayList<>();
		for(TermFeatureTerm sol : rules.keySet()) {
			for(Map<Symbol, TermFeatureTerm> gen : rules.get(sol)) {
				TermFeatureTerm pattern = new TermFeatureTerm(object);
				for(Map.Entry<Symbol, TermFeatureTerm> ent : gen.entrySet())
					pattern.addFeatureValue(ent.getKey(), ent.getValue());
				Rule new_rule = new Rule(pattern, sol );
				to_add_rules.add(new_rule);
			}
		}
		// Create the different examples
		out.cases = new ArrayList<>();
		for(int i=0; i<example_number; i++) {
			boolean valid = false;
			System.out.println("	> Creating a new example...");
			while (!valid) {
				valid = true;
				TermFeatureTerm ft_case = new TermFeatureTerm("case-" + i, cas);
				TermFeatureTerm ft_description = new TermFeatureTerm("object-" + i, object);
				TermFeatureTerm ft_solution = solutions.get(i % solutions.size());
				// Chose generalization
				Map<Symbol, TermFeatureTerm> ft_attributes = rules.get(ft_solution).get(r.nextInt(rules.get(ft_solution).size()));
				for (Symbol sy : attributes) {
					if (ft_attributes.containsKey(sy))
						ft_description.addFeatureValue(sy, ft_attributes.get(sy));
					else
						ft_description.addFeatureValue(sy, values.get(sy).get(r.nextInt(values.get(sy).size())));
				}
				// Check if new example is subsumed by an unacceptable rule
				for(Rule r : to_add_rules) {
					if (!r.solution.equivalents(ft_solution) && r.pattern.subsumes(ft_description)) {
						//System.out.println(r.solution.equivalents(ft_solution));
						//System.out.println(r.pattern.subsumes(ft_description));
						valid = false;
						break;
					}
				}
				if(valid) {
					ft_case.addFeatureValue(description, ft_description);
					ft_case.addFeatureValue(solution, ft_solution);
					out.cases.add(ft_case);
					cases.addFT(ft_case);
				}
			}
			System.out.println("	> Example created!");
		}
		out.description_path = new Path();
		out.solution_path = new Path();
		out.description_path.features.add(description);
		out.solution_path.features.add(solution);
		if(LPkg.description_path() == null && LPkg.solution_path() == null)
			LPkg.set_paths(out.description_path, out.solution_path);
		if(LPkg.generic() == null)
			LPkg.set_generic(new TermFeatureTerm(new Symbol(UUID.randomUUID().toString()), cas));
		for(TermFeatureTerm s : solutions) {
			System.out.println("> solution:" + s.toStringNOOS(dm));
			System.out.println("> Idef:");
			for (Map<Symbol, TermFeatureTerm> idef : rules.get(s)) {
				System.out.println("  > Gen:");
				for (Symbol sy : idef.keySet()) {
					System.out.println("      " + sy + " = " + idef.get(sy).toStringNOOS(dm));
				}
			}
			System.out.println("\n");
		}
		// Save data set
		saved.add(out);
		current = saved.size() -1;
		id_sizes.put(out, rules_size);
		ft_shares.put(out, ft_shared_count);
		saved_rules.put(out, to_add_rules);
		return out;
	}
	
}
