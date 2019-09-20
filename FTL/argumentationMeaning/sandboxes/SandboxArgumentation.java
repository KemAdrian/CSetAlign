package sandboxes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import agents.Agent_General;
import argumentation.Argumentation;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import enumerators.State;
import semiotic_elements.Example;
import tools.LPkg;
import tools.Token;
import tools.ToolSet;

public class SandboxArgumentation {
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.SEAT_ALL;
		
		ToolSet.THRESHOLD = 10;

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
		
		List<List<FeatureTerm>> training_tests = new ArrayList<>();
		
		// To use when we have the seat dataset in input
		for (int i = 0; i < 3; i++) {
			ArrayList<FeatureTerm> hey = new ArrayList<FeatureTerm>();
			training_tests.add(hey);
		}

		for (FeatureTerm e : training_set.cases) {
			if (Integer.parseInt(e.getName().toString().replace("e", "")) < 50) {
				training_tests.get(0).add(e);
			} else if (Integer.parseInt(e.getName().toString().replace("e", "")) < 100) {
				training_tests.get(1).add(e);
			} else {
				training_tests.get(2).add(e);
			}
		}

		//List<FeatureTerm> training_set_a1 = training_tests.get(0);
		//List<FeatureTerm> training_set_a2 = training_tests.get(1);
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		
		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
	
		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));

		Agent_General adam = new Agent_General();
		Agent_General boby = new Agent_General();
		
		adam.nick = "adam";
		boby.nick = "boby";

		adam.initialize(training_tests.get(2));
		boby.initialize(training_tests.get(2));
		Token.initialize(boby, adam);
		
		// Create two sets of positive and negative examples that are not compatible for the new concept
		Set<Example> adam_pos = new HashSet<>();
		Set<Example> adam_neg = new HashSet<>();
		Set<Example> boby_pos = new HashSet<>();
		Set<Example> boby_neg = new HashSet<>();
		
		System.out.println(boby.K.getAllConcepts());
		
		adam_pos.addAll(adam.K.getConcept("label:armchair").extensional_definition());
		adam_pos.addAll(adam.K.getConcept("label:chair").extensional_definition());
		adam_neg.addAll(adam.K.getConcept("label:stool").extensional_definition());
		
		adam.argumentation = new Argumentation(adam);
		adam.argumentation.setUpExamples(adam_pos, adam_neg);
		
		boby_neg.addAll(boby.K.getConcept("label:armchair").extensional_definition());
		boby_pos.addAll(boby.K.getConcept("label:chair").extensional_definition());
		boby_pos.addAll(boby.K.getConcept("label:stool").extensional_definition());
		
		boby.argumentation = new Argumentation(boby);
		boby.argumentation.setUpExamples(boby_pos, boby_neg);
		
		// Put Adam and Boby at the beginning of the argumentation on a new concept
		adam.current_state = State.BuildIDState;
		boby.current_state = State.BuildIDState;
		
		System.out.println("Oracle : starts discussion,  agent " + Token.defender().toString() + " in defense and agent "
					+ Token.attacker().toString() + " in attack ("+((Agent_General) Token.attacker()).current_state+")");
		while (true) {
			Token.defender().turn();
			if (adam.current_state == State.Stop && boby.current_state == State.Stop)
				break;
			System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
					+ Token.defender().toString() + " in attack ("+((Agent_General) Token.attacker()).current_state+")");
			Token.switchRoles();
			
			Set<Example> positive_examples = new HashSet<>(adam.K.getConcept("label:chair").extensional_definition());
			Set<Example> negative_examples = new HashSet<>(adam.K.getConcept("label:stool").extensional_definition());
			adam.argumentation.setUpExamples(positive_examples, negative_examples);
			positive_examples = new HashSet<>(boby.K.getConcept("label:chair").extensional_definition());
			negative_examples = new HashSet<>(boby.K.getConcept("label:armchair").extensional_definition());
			boby.argumentation.setUpExamples(positive_examples, negative_examples);
		}
		
	}


}
