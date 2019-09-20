package run_general_scripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import evaluation.Evaluation;
import evaluation.ExamplePrint;
import evaluation.ExpFileManager;
import experiments_general.general_parameters;
import semiotic_elements.Example;
import tools.LPkg;
import tools.MutableInt;
import tools.ToolSet;

public class run_parameters implements Runnable {
	
	public void run() {
		
		try {
			System.out.println(ToolSet.THRESHOLD);
			// Opening of the Cases Set
			int TEST = general_parameters.DOMAIN;
			//Argumentation_General.DEBUG = 1;
			//ABUI.DEBUG = 1;
	
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
			
			List<List<FeatureTerm>> training_sets = TrainingSetUtils.createTrainingSet(training_set.cases, training_set.differentSolutions(), training_set.solution_path, general_parameters.THRESHOLD, 1, 0, 0, 0);

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
			Set<Example> overall_context = new HashSet<>(ToolSet.cleanDuplicates(ToolSet.union(adam.K.context, boby.K.context)));
			// List of all concepts
			ExamplePrint exP = new ExamplePrint(adam.K.getAllConcepts(), boby.K.getAllConcepts());
			Map<Integer,MutableInt> count = new HashMap<>();
			for(Example e : overall_context) {
				Integer print = exP.getPrint(e);
				if(count.get(print) == null)
					count.put(print, new MutableInt());
				count.get(print).increment(1);
			}
			String resume = "";
			for(Entry<Integer, MutableInt> e : count.entrySet()) {
				System.out.println("key : "+exP.getConcepts(e.getKey()));
				System.out.println("result : "+e.getValue());
				resume += (" ; "+e.getValue());
				if(e.getValue().get() >= ToolSet.THRESHOLD) {
					ExpFileManager.expected_concepts ++;
				}
			}
			System.out.println("expected : "+ExpFileManager.expected_concepts);
			
			// Synchronic agreement
			ExpFileManager.initial_sync_agreement = Evaluation.synchronicAgreementKi(adam, boby, overall_context);
			// Diachronic agreement
			ExpFileManager.initial_diac_agreement.put(adam, Evaluation.diachronicAgreement(adam, adam.Ki().context));
			ExpFileManager.initial_diac_agreement.put(boby, Evaluation.diachronicAgreement(boby, boby.Ki().context));
			ExpFileManager.final_diac_agreement.put(adam, 0.);
			ExpFileManager.final_diac_agreement.put(boby, 0.);
			
			// Synchronic agreement
			ExpFileManager.final_sync_agreement = Evaluation.synchronicAgreementK(adam, boby, overall_context);
			// Diachronic agreement
			ExpFileManager.final_diac_agreement.put(adam, Evaluation.diachronicAgreement(adam, adam.Ki().context));
			ExpFileManager.final_diac_agreement.put(boby, Evaluation.diachronicAgreement(boby, boby.Ki().context));
			// Final threshold
			ExpFileManager.threshold = ToolSet.THRESHOLD;
			// Number of observed concepts
			ExpFileManager.observed_concepts = Math.max(adam.K.getAllConcepts().size(), boby.K.getAllConcepts().size());
			// Stock information on the other file
			ExpFileManager.writeDraft_p(ExpFileManager.redundancy+" ; "+ExpFileManager.abui_threshold+resume);
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
