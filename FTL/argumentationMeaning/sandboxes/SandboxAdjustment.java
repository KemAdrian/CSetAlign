package sandboxes;

import agents.Agent_General;
import csic.iiia.ftl.base.core.*;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import tools.LPkg;
import tools.ToolSet;

import java.util.HashSet;
import java.util.List;

public class SandboxAdjustment {
	
	
	public static void main(String[] args) {
		
		try {

			// Opening of the Cases Set
			int TEST = TrainingSetUtils.ZOOLOGY_DATASET;
			
			ToolSet.THRESHOLD = 11;
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
			
			List<List<FeatureTerm>> training_sets = TrainingSetUtils.createTrainingSet(training_set.cases, training_set.differentSolutions(), training_set.solution_path, ToolSet.THRESHOLD, 1, 0, 0, 0);

			Agent_General adam = new Agent_General();
			Agent_General boby = new Agent_General();
			
			adam.nick = "adam";
			boby.nick = "boby";
			
			System.out.println(training_sets.get(0).size());
			System.out.println(training_sets.get(1).size());
	
			adam.initialize(training_sets.get(0));
			boby.initialize(training_sets.get(1));
			
			for(Concept c : adam.Ki().getAllConcepts()) {
				System.out.println(adam.Ki().context);
				System.out.println(c.extensional_definition);
				System.out.println(adam.Ki().getAllConcepts().size());
				System.out.println(adam.Ki().context.size());
				System.out.println(c.extensional_definition.size());
				System.out.println(ToolSet.substract(adam.Ki().context, c.extensional_definition).size());
				for(Example e : c.extensional_definition) {
					System.out.println(e);
					System.out.println(ToolSet.contains(adam.Ki().context, e));
					break;
				}
				break;
			}
			
			
		
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	

}
