package sandboxes;

import csic.iiia.ftl.argumentation.core.*;
import csic.iiia.ftl.base.core.*;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import tools.LPkg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SandboxABUI {
	
	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.SEAT_TEST;

		Ontology base_ontology;
		Ontology o = new Ontology();

		base_ontology = new BaseOntology();

		o.uses(base_ontology);

		FTKBase dm = new FTKBase();
		FTKBase case_base = new FTKBase();

		case_base.uses(dm);
		dm.create_boolean_objects(o);

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		List<List<FeatureTerm>> training_tests = new ArrayList<>();

		// To use when we have the seat data set in input
		for (int i = 0; i < 3; i++) {
			ArrayList<FeatureTerm> hey = new ArrayList<>();
			training_tests.add(hey);
		}

		assert training_set != null;
		for (FeatureTerm e : training_set.cases) {
			if (Integer.parseInt(e.getName().toString().replace("e", "")) < 50) {
				training_tests.get(0).add(e);
			} else if (Integer.parseInt(e.getName().toString().replace("e", "")) < 100) {
				training_tests.get(1).add(e);
			} else {
				training_tests.get(2).add(e);
			}
		}
		
		
		ABUI learner = new ABUI();
		ABUI.ABUI_VERSION = 2;

		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);

		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);

		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<>(training_set.differentSolutions()));

		System.out.println(training_set.description_path);

		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(training_tests.get(0), training_set.solution_path, training_set.description_path, (float)0.75);
		ArgumentAcceptability aa2 = new LaplaceArgumentAcceptability(training_tests.get(0), training_set.solution_path, training_set.description_path, (float)0.75);
		
		ArgumentationBasedLearning abl1 = new ArgumentationBasedLearning();
		ArgumentationBasedLearning abl2 = new ArgumentationBasedLearning();
		
		List<ArgumentationBasedLearning> l_abl = new ArrayList<>();
		l_abl.add(abl1);
		l_abl.add(abl2);
		
		List<ArgumentAcceptability> l_a = new ArrayList<>();
		l_a.add(aa);
		l_a.add(aa2);
		
		// Hypothesis
		RuleHypothesis h1 = learner.learnConceptABUI(training_tests.get(0), training_set.differentSolutions(),
				new ArrayList<>(), aa, training_set.description_path, training_set.solution_path, o, dm);
		RuleHypothesis h2 = learner.learnConceptABUI(training_tests.get(2), training_set.differentSolutions(),
				new ArrayList<>(), aa, training_set.description_path, training_set.solution_path, o, dm);
		
		for(Rule r : h1.getRules()) {
			System.out.println(r.pattern.toStringNOOS(dm));
		}
		
	}

}
