package sandboxes;
import java.util.HashSet;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import evaluation.ExpFileManager;
import semiotic_elements.Generalization;
import tools.FTGen;
import tools.LPkg;
import tools.ToolSet;

public class Sandbox {
	
	public static void main(String[] args) throws Exception {

		//int TEST = TrainingSetUtils.SEAT_TEST;

		// Standard initialization
		ToolSet.THRESHOLD = 1;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = FTGen.generate(100, 2, o, dm, case_base);
		//TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		
		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
	
		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));
		
		FeatureTerm t = training_set.cases.get(0);
		t.getSort().test(dm);
		
		System.out.println(o.getDescription());
		
		//List<List<FeatureTerm>> training_sets = TrainingSetUtils.createTrainingSet(training_set.cases, training_set.differentSolutions(), training_set.solution_path, general_parameters.THRESHOLD, 1, 0, 0, 0);
		
		/*ABUI.ABUI_VERSION = 2;
		ABUI learner = new ABUI();
		ContrastSet cs = learner.makeContrastSet(training_set.cases);
		for(Concept c : cs.getAllConcepts()) {
			System.out.println(c.intensional_definition.size());
			for(Generalization gen : c.intensional_definition) {
				System.out.println(gen.generalization.toStringNOOS(dm));
			}
		}*/
		ABUI.ABUI_VERSION = 2;
		//ABUI.DEBUG = 2;
		ExpFileManager.abui_threshold = (float) 0.75;
		Set<Generalization> c = ABUI.learnConcept(training_set.cases, new HashSet<>(), training_set.differentSolutions().get(0));
		for(Generalization gen : c) {
			System.out.println(gen.generalization.toStringNOOS(dm));
		}
		// Test accuracy
		int tp = 0;
		int tn = 0;
		int fp = 0;
		int fn = 0;
		for(FeatureTerm f : training_set.cases) {
			boolean covers = false;
			boolean should_cover = false;
			if(f.readPath(training_set.solution_path).equivalents(training_set.differentSolutions().get(0))) {
				should_cover = true;
			}
			for(Generalization gen : c) {
				if(gen.generalizes(f.readPath(training_set.description_path))) {
					covers = true;
					break;
				}
			}
			if(covers && should_cover)
				tp ++;
			if(!covers && !should_cover)
				tn ++;
			if(covers && !should_cover)
				fp ++;
			if(!covers && should_cover)
				fn ++;
		}
		System.out.println("tp= "+tp+" fp= "+fp+" tn= "+tn+" fn= "+fn);
		
	}

}
