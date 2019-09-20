package scripts_lazy_local_disagreements;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import agents.Agent_Lazy;
import containers.ContrastSet;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import enumerators.State;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Sign;
import tools.ToolSet;
import tools.LPkg;
import tools.Token;

public class Lazy_Local_Disagreement_Case_4_2 {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.ZOOLOGY_DATASET_LB;
		
		ToolSet.THRESHOLD = 1;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		
		System.out.println(" DATA SET SIZE : "+training_set.cases.size());
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		
		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
	
		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));

		List<FeatureTerm> training_set_a1 = training_set.cases;
		List<FeatureTerm> training_set_a2 = training_set.cases;
		
		Agent_Lazy adam = new Agent_Lazy();
		Agent_Lazy boby = new Agent_Lazy();
		
		adam.nick = "adam";
		boby.nick = "boby";

		adam.initialize(training_set_a1);
		boby.initialize(training_set_a2);
		
		// Get the concepts from mammal, fish and bird
		Concept adamMammal = adam.Ki.getConcept("solution:mammal");
		Concept adamFish = adam.Ki.getConcept("solution:fish");
		Concept adamBird = adam.Ki.getConcept("solution:bird");
		
		Concept bobyMammal = boby.Ki.getConcept("solution:mammal");
		Concept bobyFish = boby.Ki.getConcept("solution:fish");
		Concept bobyBird = boby.Ki.getConcept("solution:bird");
		
		// Create super concepts
		Concept adamChimera = new Concept(new Sign("solution:fish"), new HashSet<>(), new HashSet<>());
		Concept bobyChimera = new Concept(new Sign("solution:mammal"), new HashSet<>(), new HashSet<>());
		Concept bobySphynx = new Concept(new Sign("solution:bird"), new HashSet<>(), new HashSet<>());
		
		// Add intensional definitions
		adamChimera.intensional_definition.addAll(adamMammal.intensional_definition());
		adamChimera.intensional_definition.addAll(adamFish.intensional_definition());
		//adamChimera.addGeneralizations(adamBird.intensional_definition());

		bobyChimera.intensional_definition.addAll(bobyMammal.intensional_definition());
		//bobyChimera.intensional_definition.addAll(bobyFish.intensional_definition());
		//bobyChimera.intensional_definition.addAll(bobyBird.intensional_definition());

		//bobySphynx.intensional_definition.addAll(bobyMammal.intensional_definition());
		bobySphynx.intensional_definition.addAll(bobyFish.intensional_definition());
		bobySphynx.intensional_definition.addAll(bobyBird.intensional_definition());
		
		// Add extensional definitions
		adamChimera.extensional_definition.addAll(adamMammal.extensional_definition());
		adamChimera.extensional_definition.addAll(adamFish.extensional_definition());
		//adamChimera.addExamples(adamBird.extensional_definition());

		bobyChimera.extensional_definition.addAll(bobyMammal.extensional_definition());
		//bobyChimera.addExamples(bobyFish.extensional_definition());
		//bobyChimera.addExamples(bobyBird.extensional_definition());

		//bobySphynx.addExamples(bobyMammal.extensional_definition());
		bobySphynx.extensional_definition.addAll(bobyFish.extensional_definition());
		bobySphynx.extensional_definition.addAll(bobyBird.extensional_definition());
		
		// Delet mammal, fish and bird from Ki
		adam.Ki.removeConcept(adamMammal);
		adam.Ki.removeConcept(adamFish);
		adam.Ki.removeConcept(adamBird);
		
		boby.Ki.removeConcept(bobyMammal);
		boby.Ki.removeConcept(bobyFish);
		boby.Ki.removeConcept(bobyBird);
		
		// Add new concepts
		adam.Ki.addConcept(adamChimera);
		boby.Ki.addConcept(bobyChimera);
		boby.Ki.addConcept(bobySphynx);
		
		// Make new contexts
		HashSet<Example> adamContext = new HashSet<Example>();
		HashSet<Example> bobyContext = new HashSet<Example>();
		
		for(Concept c : adam.Ki.getAllConcepts())
			adamContext.addAll(c.extensional_definition());
		for(Concept c : boby.Ki.getAllConcepts())
			bobyContext.addAll(c.extensional_definition());
		
		adam.Ki.context = new HashSet<>(adamContext);
		adam.Kc.context = new HashSet<>(adamContext);
		adam.Kf.context = new HashSet<>(adamContext);
		adam.Hc.context = new HashSet<>(adamContext);
		adam.Hf.context = new HashSet<>(adamContext);
		boby.Ki.context = new HashSet<>(bobyContext);
		boby.Kc.context = new HashSet<>(bobyContext);
		boby.Kf.context = new HashSet<>(bobyContext);
		boby.Hc.context = new HashSet<>(bobyContext);
		boby.Hf.context = new HashSet<>(bobyContext);
		
		// Replace K
		adam.Kf = new ContrastSet(new HashSet<>(), new HashSet<>(adamContext));
		boby.Kf = new ContrastSet(new HashSet<>(), new HashSet<>(bobyContext));
		
		for(Concept k : adam.Ki.getAllConcepts()){
			adam.Kf.addConcept(k.clone());
		}
		
		for(Concept k : boby.Ki.getAllConcepts()){
			boby.Kf.addConcept(k.clone());
		}
		
		// Initialize the scores
		Double score_adam = 0.;
		Double score_boby = 0.;
		Integer total = 0;
		
		// Final Test, taking the original classification and looking if the agents are classifying the same
		for(FeatureTerm f : training_set.cases){
			
			// Create the example to evaluate and the solution to look up to
			String s = f.readPath(LPkg.solution_path()).toStringNOOS(LPkg.dm());
			Example e = new Example(f.readPath(LPkg.description_path()));
			
			if(!s.equals("bird")){
				if (adam.name(e,adam.Kf).size() == 1) {
					if (adam.name(e,adam.Kf).get(0).equals("solution:" + s))
						score_adam++;
				}

				if (boby.name(e,adam.Kf).size() == 1) {
					if (boby.name(e,adam.Kf).get(0).equals("solution:" + s))
						score_boby++;
				}

				total++;
			}
			
		}
		
		System.out.println("\n INITIAL SCORES:");
		System.out.println("Adam = "+(score_adam/total)*100+"%");
		System.out.println("Boby = "+(score_boby/total)*100+"%");
		
		Token.initialize(adam, boby);
		
		LinkedList<Example> overallContext = new LinkedList<Example>();
		overallContext.addAll(adam.Ki.context);
		overallContext.addAll(boby.Ki.context);
		
		System.out.println("Oracle : starts discussion,  agent " + Token.defender().toString() + " in defense and agent "
					+ Token.attacker().toString() + " in attack ("+Token.attacker().state()+")");
		while (true) {
			Token.defender().turn();
			if (adam.current_state == State.WaitExample && boby.current_state == State.WaitExample) {
				if(overallContext.isEmpty())
					break;
				Token.sendExample(overallContext.removeFirst());
			}
			System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
					+ Token.defender().toString() + " in attack ("+Token.attacker().state()+")");
			Token.switchRoles();
		}
		
		System.out.println("\n");
		System.out.println("adam final contrast set: "+adam.Kf.getAllConcepts());
		System.out.println("boby final contrast set: "+boby.Kf.getAllConcepts());
		
		// Re-initialize the scores
		score_adam = 0.;
		score_boby = 0.;
		total = 0;
		
		// Final Test, taking the original classification and looking if the agents are classifying the same
		for(FeatureTerm f : training_set.cases){
			
			// Create the example to evaluate and the solution to look up to
			String s = f.readPath(LPkg.solution_path()).toStringNOOS(LPkg.dm());
			Example e = new Example(f.readPath(LPkg.description_path()));
			
			if(!s.equals("bird")){
				if (adam.name(e,adam.Kf).size() == 1) {
					if (adam.name(e,adam.Kf).get(0).equals("solution:" + s))
						score_adam++;
				}

				if (boby.name(e,adam.Kf).size() == 1) {
					if (boby.name(e,adam.Kf).get(0).equals("solution:" + s))
						score_boby++;
				}

				total++;
			}
		}
		
		System.out.println("\n FINAL SCORES:");
		System.out.println("Adam = "+(score_adam/total)*100+"%");
		System.out.println("Boby = "+(score_boby/total)*100+"%");
	}

}
