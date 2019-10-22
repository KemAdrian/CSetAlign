package run_lazy_scripts;

import agents.Agent_Lazy;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.*;
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class run_test_lazy implements Runnable{

	public void run() {
		
		try {

			// Opening of the Cases Set
			int TEST = TrainingSetUtils.NB_DOMAIN;
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

			assert training_set != null;
			TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
			
			g.setName(null);
			g.defineFeatureValue(training_set.description_path, null);
			g.defineFeatureValue(training_set.solution_path, null);
		
			LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<>(training_set.differentSolutions()));
			List<List<FeatureTerm>> training_sets = TrainingSetUtils.createTrainingSet(training_set.cases, training_set.differentSolutions(), training_set.solution_path, ToolSet.THRESHOLD,0,0,0,0);
			Agent_Lazy adam = new Agent_Lazy();
			Agent_Lazy boby = new Agent_Lazy();
			
			// Typology of errors
			MutableInt g_count_adam = new MutableInt(), g_count_boby = new MutableInt(), e_count_adam = new MutableInt(), e_count_boby = new MutableInt();
			ExpFileManager.addBlock("g_count_"+adam.nick(), g_count_adam);
			ExpFileManager.addBlock("g_count_"+boby.nick(), g_count_boby);
			ExpFileManager.addBlock("e_count_"+adam.nick(), e_count_adam);
			ExpFileManager.addBlock("e_count_"+boby.nick(), e_count_boby);
			
			adam.nick = "adam";
			boby.nick = "boby";
	
			adam.initialize(training_sets.get(0));
			boby.initialize(training_sets.get(1));
			
			// Number of expected concepts
			System.out.println(adam.Ki.context.size());
			System.out.println(boby.Ki.context.size());
			// Make overall context
			Set<Example> overall_context = new HashSet<>(ToolSet.cleanDuplicates(ToolSet.union(adam.Ki.context, boby.Ki.context)));
			// Create a print for concept count
			ExamplePrint exP = new ExamplePrint(adam.Ki.getAllConcepts(), boby.Ki.getAllConcepts());
			// Output expected concepts (bruteforce 1)
			int count = 0;
			for (Entry<Integer, MutableInt> e : exP.makeCount(overall_context)) {
				System.out.println("expecting concept between " + exP.getConcepts(e.getKey()) + " (" + e.getValue() + ")");
				if (e.getValue().get() >= ToolSet.THRESHOLD) {
					count++;
				}
			}
			ExpFileManager.addBlock("expected",count);

			// Number and types of disagreements
			Evaluation.disagreementCount(adam.Ki, boby.Ki, overall_context, true, null);
			Evaluation.disagreementCount(adam.Ki, boby.Ki, adam.Ki.context, true, adam);
			Evaluation.disagreementCount(adam.Ki, boby.Ki, boby.Ki.context, true, boby);
			// Synchronic agreement
			ExpFileManager.addBlock("i_sync_oag",Evaluation.synchronicAgreementKi(adam, boby, overall_context));
			ExpFileManager.addBlock("i_sync_lag_"+adam.nick,Evaluation.localSynchronicAgreementKi(adam, boby));
			ExpFileManager.addBlock("i_sync_lag_"+boby.nick,Evaluation.localSynchronicAgreementKi(boby, adam));
			// Diachronic agreement
			ExpFileManager.addBlock("i_diac_lag_"+adam.nick, Evaluation.diachronicAgreement(adam, adam.Ki().context));
			ExpFileManager.addBlock("i_diac_lag_"+boby.nick, Evaluation.diachronicAgreement(boby, boby.Ki().context));
			// Get the coverage
			ExpFileManager.addBlock("i_cover_"+adam.nick,Evaluation.coverage(adam, adam.Ki, overall_context));
			ExpFileManager.addBlock("i_cover_"+boby.nick,Evaluation.coverage(boby, boby.Ki, overall_context));
			ExpFileManager.addBlock("i_cover_ov", Evaluation.shared_coverage(adam, adam.Ki, boby.Ki, overall_context));
			
			// List of examples to test
			LinkedList<Example> toTest = new LinkedList<>(overall_context);
			
			Token.initialize(adam, boby);
			System.out.println("Oracle : starts discussion,  agent " + Token.defender().toString() + " in defense and agent "
						+ Token.attacker().toString() + " in attack ("+Token.attacker().state()+")");
			while (true) {
				if (adam.current_state == State.WaitExample && boby.current_state == State.WaitExample) {
					if(toTest.isEmpty())
						break;
					Token.sendExample(toTest.removeFirst());
				}
				Token.defender().turn();
				System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
						+ Token.defender().toString() + " in attack ("+ Token.attacker().state()+")");
				System.out.println("  > shared examples: "+ToolSet.cleanDuplicates(ToolSet.intersection(adam.Kf.context, boby.Kf.context)).size());
				Token.switchRoles();
			}
			
			// Create a print for concept count
			ExamplePrint exP2 = new ExamplePrint(adam.Kf.getAllConcepts(), boby.Kf.getAllConcepts());
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
			Evaluation.disagreementCount(adam.K(), boby.K(), overall_context, false, null);
			Evaluation.disagreementCount(adam.K(), boby.K(), adam.K().context, false, adam);
			Evaluation.disagreementCount(adam.K(), boby.K(), boby.K().context, false, boby);

			// Exchange ration
			ExpFileManager.addBlock("exchange", ((double) ToolSet.cleanDuplicates(ToolSet.intersection(adam.K().context, boby.K().context)).size()) / overall_context.size());
			// Synchronic agreement
			ExpFileManager.addBlock("f_sync_oag",Evaluation.synchronicAgreementK(adam, boby, overall_context));
			ExpFileManager.addBlock("f_sync_lag_"+adam.nick,Evaluation.localSynchronicAgreementK(adam, boby));
			ExpFileManager.addBlock("f_sync_lag_"+boby.nick,Evaluation.localSynchronicAgreementK(boby, adam));
			// Diachronic agreement
			ExpFileManager.addBlock("f_diac_lag_"+adam.nick, Evaluation.diachronicAgreement(adam, adam.Ki().context));
			ExpFileManager.addBlock("f_diac_lag_"+boby.nick, Evaluation.diachronicAgreement(boby, boby.Ki().context));
			// Get the coverage
			ExpFileManager.addBlock("f_cover_"+adam.nick,Evaluation.coverage(adam, adam.K(), overall_context));
			ExpFileManager.addBlock("f_cover_"+boby.nick,Evaluation.coverage(boby, boby.K(), overall_context));
			ExpFileManager.addBlock("f_cover_ov", Evaluation.shared_coverage(adam, adam.K(), boby.K(), overall_context));
			// Final threshold
			ExpFileManager.addBlock("threshold", ToolSet.THRESHOLD);
			// Number of observed concepts
			ExpFileManager.addBlock("observed",Math.max(adam.K().getAllConcepts().size(), boby.K().getAllConcepts().size()));;
			// Make an Amail evaluation
			//Evaluation.f_amailEvaluation(adam, boby, overall_context);
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
