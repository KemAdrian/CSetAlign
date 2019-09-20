package scripts_seat;

import java.util.HashSet;
import java.util.List;

import agents.Agent_General;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import enumerators.State;
import experiments_general.general_parameters;
import semiotic_elements.Concept;
import tools.LPkg;
import tools.Token;
import tools.ToolSet;

public class run_seats_general2 {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.SEAT_TEST;
		
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
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		
		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
	
		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));
		
		List<List<FeatureTerm>> training_sets = TrainingSetUtils.createTrainingSet(training_set.cases, training_set.differentSolutions(), training_set.solution_path, general_parameters.THRESHOLD, 1, 0, 0, 0);

		Agent_General adam = new Agent_General();
		Agent_General boby = new Agent_General();
		
		adam.nick = "adam";
		boby.nick = "boby";

		adam.initialize(training_sets.get(0));
		boby.initialize(training_sets.get(1));
		
		for(Concept c : adam.Ki.getAllConcepts()) {
			System.out.println(c.intensional_definition.size());
		}
		
		//boby.K.renameConcept("i", 0);
		
		Token.initialize(adam, boby);
		System.out.println("Oracle : starts discussion,  agent " + Token.defender().toString() + " in defense and agent "
					+ Token.attacker().toString() + " in attack ("+((Agent_General) Token.attacker()).current_state+")");
		while (true) {
			Token.defender().turn();
			if (adam.current_state == State.Stop && boby.current_state == State.Stop)
				break;
			System.out.println("\nOracle : switch roles, agent " + Token.attacker().toString() + " in defense and agent "
					+ Token.defender().toString() + " in attack ("+((Agent_General) Token.attacker()).current_state+")");
			Token.switchRoles();
		}
		
		/*for(Concept c : adam.K.getAllConcepts()){
			for(Generalization r : c.intensional_definition){
				System.out.println(r.generalization.toStringNOOS(dm));
			}
			System.out.println("- - - - - - -");
		}*/
	}

}
