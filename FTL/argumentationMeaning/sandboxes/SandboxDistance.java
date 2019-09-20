package sandboxes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import agents.Agent_General;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import csic.iiia.ftl.learning.lazymethods.similarity.AUDistance;
import csic.iiia.ftl.learning.lazymethods.similarity.Distance;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.LPkg;

public class SandboxDistance {
	
	public static void main(String[] args) throws Exception {
		
		int TEST = TrainingSetUtils.DEMOSPONGIAE_120_DATASET;
		Distance distance = new AUDistance();

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		
		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
	
		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));
		
		
		HashMap<FeatureTerm, Set<FeatureTerm>> map = new HashMap<>();
		
		for(FeatureTerm sol : training_set.differentSolutions()) {
			map.put(sol, new HashSet<>());
		}
		
		for(FeatureTerm c : training_set.cases) {
			map.get(c.readPath(training_set.solution_path)).add(c);
		}
		
		for(Entry<FeatureTerm, Set<FeatureTerm>> e : map.entrySet()) {
			System.out.println(e.getKey().toStringNOOS()+" "+e.getValue().size());
		}
		
		System.out.println("\n");
		
		// Create an agent
		Agent_General agent = new Agent_General();
		agent.initialize(training_set.cases);
		
		// Measure learning distances
		ArrayList<Concept> concepts = new ArrayList<Concept>(agent.K.getAllConcepts());
		for (int i = 0; i < concepts.size(); i++) {
		    for (int j = i + 1; j < concepts.size(); j++) {
		    	// For each pair of concept, measure the difference between the generalizations
		    	double num = 0.;
				double denom = 0.;
				for(Generalization g1 : concepts.get(i).intensional_definition()) {
					for(Generalization g2 : concepts.get(j).intensional_definition()) {
						double d = distance.distance(g1.generalization, g2.generalization, o, dm);
						num += d;
						denom += 1;
					}
				}
				System.out.println("Extra-class distance between "+concepts.get(i).sign()+" and "+concepts.get(j).sign()+" = "+num/denom);
		    }
		}
		
		System.out.println("\n -- Examples -- \n");
		
		// Measure intra-class average distance
		for(Entry<FeatureTerm, Set<FeatureTerm>> e : map.entrySet()) {
			ArrayList<FeatureTerm> list = new ArrayList<FeatureTerm>(e.getValue());
			double num = 0.;
			double denom = 0.;
			// Get all pairs of concepts
			for (int i = 0; i < list.size(); i++) {
			    for (int j = i + 1; j < list.size(); j++) {
			    	// get the two examples
			    	FeatureTerm f1 = list.get(i).readPath(training_set.description_path);
			    	FeatureTerm f2 = list.get(j).readPath(training_set.description_path);
			    	double d = distance.distance(f1, f2, o, dm);
			    	num += d;
			    	denom += 1;
			    }
			}
			System.out.println("Intra-class distance of "+e.getKey().toStringNOOS(dm)+" = "+num/denom);
		}
		
		System.out.println("\n");
		
		// Measure extra-class average distance
		ArrayList<Entry<FeatureTerm, Set<FeatureTerm>>> list = new ArrayList<Entry<FeatureTerm, Set<FeatureTerm>>>(map.entrySet());
		for (int i = 0; i < list.size(); i++) {
		    for (int j = i + 1; j < list.size(); j++) {
				if(!list.get(i).getKey().equals(list.get(j).getKey())) {
					double num = 0.;
					double denom = 0.;
					// Get each pair of examples
					for(FeatureTerm ff1 : list.get(i).getValue()) {
						for(FeatureTerm ff2 : list.get(j).getValue()) {
					    	FeatureTerm f1 = ff1.readPath(training_set.description_path);
					    	FeatureTerm f2 = ff2.readPath(training_set.description_path);
					    	double d = distance.distance(f1, f2, o, dm);
					    	num += d;
					    	denom += 1;
						}
					}
					System.out.println("Extra-class distance between "+list.get(i).getKey().toStringNOOS(dm)+" and "+list.get(j).getKey().toStringNOOS(dm)+" = "+num/denom);
				}
			}
		}
		
		
		System.out.println("\n -- Idef vs. Examples -- \n");
		for(Concept c1 : agent.K.getAllConcepts()) {
			for(Concept c2 : agent.K.getAllConcepts()) {
				// Test generalizations
				System.out.println("   > Testing idef of concept "+c1.sign()+" vs edef of concepts "+c2.sign());
				System.out.println(c1.intensional_definition.size());
				for(Generalization gn : c1.intensional_definition()) {
					double num = 0.;
					double denom = 0.;
					FeatureTerm f1 = gn.generalization;
					for(Example e : c2.extensional_definition()) {
				    	FeatureTerm f2 = e.featureterm;
				    	double d = distance.distance(f1, f2, o, dm);
				    	num += d;
				    	denom += 1;
					}
					System.out.println("      > generalization : "+num/denom);
				}
			}
		}
		
	}

}
