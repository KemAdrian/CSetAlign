package sandboxes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.argumentation.core.AMAIL;
import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.argumentation.core.ArgumentAcceptability;
import csic.iiia.ftl.argumentation.core.ArgumentationBasedLearning;
import csic.iiia.ftl.argumentation.core.LaplaceArgumentAcceptability;
import csic.iiia.ftl.base.core.BaseOntology;
import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Sort;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import tools.LPkg;

public class SandboxAMAIL {
	
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
		;

		TrainingSetProperties training_set = TrainingSetUtils.loadTrainingSet(TEST, o, dm, case_base);
		List<List<FeatureTerm>> training_tests = new ArrayList<List<FeatureTerm>>();

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
		
		
		ABUI learner = new ABUI();
		ABUI.ABUI_VERSION = 2;
		
		System.out.println(o.getSorts());
		System.out.println("after there");
		for(FeatureTerm ft : dm.getAllTerms()){
			try {
				TermFeatureTerm fts = (TermFeatureTerm) ft;
				System.out.println(fts.getName());
				System.out.println(fts.getSort());
				//System.out.println(fts.getValue());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		System.out.println("before there");
		
		for(Sort s : o.getSorts()){
			System.out.println("");
			System.out.println(s);
			System.out.println(s.getSuper());
			System.out.println(s.getSubSorts());
			System.out.println(s.getFeatures());
			for(Symbol sy : s.getFeatures()){
				System.out.println("  "+sy);
				System.out.println("   "+s.featureSort(sy));
			}
		}
		
		System.out.println(training_set.cases.get(0).toStringNOOS(dm));
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);

		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);

		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path,
				new HashSet<FeatureTerm>(training_set.differentSolutions()));
		
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
				new ArrayList<Argument>(), aa, training_set.description_path, training_set.solution_path, o, dm);
		RuleHypothesis h2 = learner.learnConceptABUI(training_tests.get(2), training_set.differentSolutions(),
				new ArrayList<Argument>(), aa, training_set.description_path, training_set.solution_path, o, dm);
		
		for(Rule r : h1.getRules()){
			if(r.solution.equals(training_set.differentSolutions().get(0)))
				r.solution = training_set.differentSolutions().get(1);
		}
		
		//System.out.println(LearningPackage.solution_sort());
		
		// Delet a Rule in RuleHypotheses
		System.out.println(h1.toString(dm));
		System.out.println(h2.toString(dm));
		
		// Hypothesis l_h
		List<RuleHypothesis> l_h = new ArrayList<>();
		l_h.add(h1);
		l_h.add(h2);
		
		List<List<FeatureTerm>> l_f = new ArrayList<>();
		l_f.add(training_tests.get(0));
		l_f.add(training_tests.get(2));
		
		AMAIL amail = new AMAIL(l_h, training_set.differentSolutions().get(1), l_f, l_a, l_abl, false, training_set.description_path, training_set.solution_path, o, dm);
		
		while(amail.moreRoundsP()){
			amail.round(true);
		}
		
		// Test performances
		double out = 0;
		for(FeatureTerm e : training_set.cases) {
			// Get answers
			List<FeatureTerm> adam_answer = amail.result().get(0).generatePrediction(e.readPath(training_set.description_path), dm, false).solutions;
			List<FeatureTerm> boby_answer = amail.result().get(1).generatePrediction(e.readPath(training_set.description_path), dm, false).solutions;
			
			if(adam_answer.size() == 1 && boby_answer.size() == 1 && adam_answer.get(0).equivalents(boby_answer.get(0)))
				out ++;
			System.out.println(adam_answer.get(0).toStringNOOS(dm));
			System.out.println(boby_answer);
		}
		System.out.println( out / training_set.cases.size());
		
		
		
	}

}
