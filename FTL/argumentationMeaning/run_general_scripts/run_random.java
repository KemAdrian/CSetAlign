package run_general_scripts;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import agents.Agent_General;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import enumerators.State;
import evaluation.Evaluation;
import evaluation.ExamplePrint;
import evaluation.ExpFileManager;
import semiotic_elements.Example;
import tools.LPkg;
import tools.MutableInt;
import tools.Token;
import tools.ToolSet;

public class run_random implements Runnable{

	public void run() {
		
		try {

			// Opening of the Cases Set
			int TEST = ExpFileManager.nb_domain;
			//Argumentation_General.DEBUG = 1;
			//ABUI.DEBUG = 1;
	
			ABUI.ABUI_VERSION = 2;
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
			List<List<FeatureTerm>> training_sets = TrainingSetUtils.createRandomTrainingSet(training_set.cases, training_set.differentSolutions(), training_set.solution_path, ToolSet.THRESHOLD);
			Agent_General adam = new Agent_General();
			Agent_General boby = new Agent_General();
			
			// Typology of errors
			ExpFileManager.g_count.put(adam, new MutableInt());
			ExpFileManager.g_count.put(boby, new MutableInt());
			ExpFileManager.e_count.put(adam, new MutableInt());
			ExpFileManager.e_count.put(boby, new MutableInt());
			
			adam.nick = "adam";
			boby.nick = "boby";
	
			adam.initialize(training_sets.get(0));
			boby.initialize(training_sets.get(1));
			
			// Number of expected concepts
			System.out.println(adam.K.context.size());
			System.out.println(boby.K.context.size());
			// Make overall context
			Set<Example> overall_context = new HashSet<>(ToolSet.cleanDuplicates(ToolSet.union(adam.K.context, boby.K.context)));
			// Create a print for concept count
			ExamplePrint exP = new ExamplePrint(adam.Ki.getAllConcepts(), boby.Ki.getAllConcepts());
			// Output expected concepts (bruteforce 1)
			for (Entry<Integer, MutableInt> e : exP.makeCount(overall_context)) {
				System.out.println("expecting concept between " + exP.getConcepts(e.getKey()) + " (" + e.getValue() + ")");
				if (e.getValue().get() >= ToolSet.THRESHOLD) {
					ExpFileManager.expected_concepts++;
				}
			}
			System.out.println("expected : " + ExpFileManager.expected_concepts);
			
			// Number and types of disagreements
			Evaluation.disagreementCount(adam.Ki, boby.Ki, overall_context, true, null);
			Evaluation.disagreementCount(adam.Ki, boby.Ki, adam.Ki.context, true, adam);
			Evaluation.disagreementCount(adam.Ki, boby.Ki, boby.Ki.context, true, boby);
			// Synchronic agreement
			//Evaluation.i_amailEvaluation(adam, boby, overall_context);
			ExpFileManager.initial_sync_agreement = Evaluation.synchronicAgreementKi(adam, boby, overall_context);
			ExpFileManager.local_initial_sync_agreement.put(adam, Evaluation.localSynchronicAgreementKi(adam, boby));
			ExpFileManager.local_initial_sync_agreement.put(boby, Evaluation.localSynchronicAgreementKi(boby, adam));
			// Diachronic agreement
			ExpFileManager.initial_diac_agreement.put(adam, Evaluation.diachronicAgreement(adam, adam.Ki().context));
			ExpFileManager.initial_diac_agreement.put(boby, Evaluation.diachronicAgreement(boby, boby.Ki().context));
			ExpFileManager.final_diac_agreement.put(adam, 0.);
			ExpFileManager.final_diac_agreement.put(boby, 0.);
			// Get the coverage
			ExpFileManager.i_cover.put(adam, Evaluation.coverage(adam, adam.Ki, overall_context));
			ExpFileManager.i_cover.put(boby, Evaluation.coverage(boby, boby.Ki, overall_context));
			
			Token.initialize(adam, boby);
			System.out.println("Oracle : starts discussion,  agent " + Token.defender().toString() + " in defense and agent "
						+ Token.attacker().toString() + " in attack ("+((Agent_General) Token.attacker()).current_state+")");
			while (true) {
				if (adam.current_state == State.Stop && boby.current_state == State.Stop)
					break;
				System.out.println(adam.e_exchanged.size());
				System.out.println(boby.e_exchanged.size());
				Token.defender().turn();
				System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
						+ Token.defender().toString() + " in attack ("+((Agent_General) Token.attacker()).current_state+")");
				System.out.println("  > shared examples: "+ToolSet.cleanDuplicates(ToolSet.intersection(adam.K.context, boby.K.context)).size());
				Token.switchRoles();
			}
			
			// Create a print for concept count
			ExamplePrint exP2 = new ExamplePrint(adam.K.getAllConcepts(), boby.K.getAllConcepts());
			int final_expected_concepts = 0;
			// Output expected concepts (bruteforce 1)
			for (Entry<Integer, MutableInt> e : exP2.makeCount(overall_context)) {
				System.out.println("expecting concept between " + exP2.getConcepts(e.getKey()) + " (" + e.getValue() + ")");
				if (e.getValue().get() >= ToolSet.THRESHOLD) {
					final_expected_concepts ++;
				}
			}
			System.out.println("expected : " + final_expected_concepts);
			
			// Number and types of disagreements
			Evaluation.disagreementCount(adam.K, boby.K, overall_context, false, null);
			Evaluation.disagreementCount(adam.K, boby.K, adam.K.context, false, adam);
			Evaluation.disagreementCount(adam.K, boby.K, boby.K.context, false, boby);

			// Exchange ration
			ExpFileManager.exchange_ratio = ((double) ToolSet.cleanDuplicates(ToolSet.intersection(adam.K.context, boby.K.context)).size()) / overall_context.size();
			// Synchronic agreement
			ExpFileManager.final_sync_agreement = Evaluation.synchronicAgreementK(adam, boby, overall_context);
			ExpFileManager.local_final_sync_agreement.put(adam, Evaluation.localSynchronicAgreementK(adam, boby));
			ExpFileManager.local_final_sync_agreement.put(boby, Evaluation.localSynchronicAgreementK(boby, adam));
			// Diachronic agreement
			ExpFileManager.final_diac_agreement.put(adam, Evaluation.diachronicAgreement(adam, adam.Ki().context));
			ExpFileManager.final_diac_agreement.put(boby, Evaluation.diachronicAgreement(boby, boby.Ki().context));
			// Get the coverage
			ExpFileManager.f_cover.put(adam, Evaluation.coverage(adam, adam.K, overall_context));
			ExpFileManager.f_cover.put(boby, Evaluation.coverage(boby, boby.K, overall_context));
			// Final threshold
			ExpFileManager.threshold = ToolSet.THRESHOLD;
			// Number of observed concepts
			ExpFileManager.observed_concepts = Math.max(adam.K.getAllConcepts().size(), boby.K.getAllConcepts().size());
			
			// Make an Amail evaluation
			//Evaluation.f_amailEvaluation(adam, boby, overall_context);
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
