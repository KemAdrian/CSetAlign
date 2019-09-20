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
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import csic.iiia.ftl.learning.core.TrainingSetProperties;
import csic.iiia.ftl.learning.core.TrainingSetUtils;
import tools.LPkg;

public class SandboxAMAIL2 {
	
	public static void main(String[] args) throws Exception {

		// Opening of the Cases Set
		int TEST = TrainingSetUtils.DEMOSPONGIAE_280_DATASET;

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
		
		TermFeatureTerm g = (TermFeatureTerm) training_set.cases.get(0).clone(o);
		LPkg.initialize(g, o, dm, training_set.description_path, training_set.solution_path, new HashSet<FeatureTerm>(training_set.differentSolutions()));
		
		List<List<FeatureTerm>> trainings = TrainingSetUtils.createTrainingSet(training_set.cases, training_set.differentSolutions(), training_set.solution_path, 0, 0, 0, 0, 0);
		// Mix cases
		
		
		// To use when we have the seat dataset in input
		ABUI learner = new ABUI();
		ABUI.ABUI_VERSION = 2;
		
		/*System.out.println(o.getSorts());
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
		
		System.out.println(training_set.cases.get(0).toStringNOOS(dm));*/

		g.setName(null);
		g.defineFeatureValue(training_set.description_path, null);
		g.defineFeatureValue(training_set.solution_path, null);
		
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(trainings.get(0), training_set.solution_path, training_set.description_path, (float)0.75);
		ArgumentAcceptability aa2 = new LaplaceArgumentAcceptability(trainings.get(1), training_set.solution_path, training_set.description_path, (float)0.75);
		
		ArgumentationBasedLearning abl1 = new ArgumentationBasedLearning();
		ArgumentationBasedLearning abl2 = new ArgumentationBasedLearning();
		
		List<ArgumentationBasedLearning> l_abl = new ArrayList<>();
		l_abl.add(abl1);
		l_abl.add(abl2);
		
		List<ArgumentAcceptability> l_a = new ArrayList<>();
		l_a.add(aa);
		l_a.add(aa2);
		
		// Hypothesis
		RuleHypothesis h1 = learner.learnConceptABUI(trainings.get(0), training_set.differentSolutions(),
				new ArrayList<Argument>(), aa, training_set.description_path, training_set.solution_path, o, dm);
		RuleHypothesis h2 = learner.learnConceptABUI(trainings.get(1), training_set.differentSolutions(),
				new ArrayList<Argument>(), aa, training_set.description_path, training_set.solution_path, o, dm);
		
		//RuleHypothesis h11 = new RuleHypothesis(h1);
		//RuleHypothesis h12 = new RuleHypothesis(h2);
		
		//System.out.println(LearningPackage.solution_sort());
		
		// Delete a Rule in RuleHypotheses
		System.out.println(h1.toString(dm));
		System.out.println(h2.toString(dm));
		
		// Hypothesis l_h
		List<RuleHypothesis> l_h = new ArrayList<>();
		l_h.add(h1);
		l_h.add(h2);
		
		List<List<FeatureTerm>> l_f = new ArrayList<>();
		l_f.add(trainings.get(0));
		l_f.add(trainings.get(1));
		
		double[] num_1 = {0., 0., 0., 0.};
		double[] num_2 = {0., 0., 0., 0.};
		double denum = 0.;
		// Test performances by solution
		for(FeatureTerm solution : training_set.differentSolutions()) {
			AMAIL amail = new AMAIL(l_h, solution, l_f, l_a, l_abl, false, training_set.description_path, training_set.solution_path, o, dm);
			while(amail.moreRoundsP()){
				amail.round(true);
			}
			double[] score_1 = {0., 0., 0., 0.};
			double[] score_2 = {0., 0., 0., 0.};
			double[] instances = {0., 0.};
			double[] assignments_1 = {0., 0.};
			double[] assignments_2 = {0., 0.};
			for(FeatureTerm example : training_set.cases) {
				// Check answer from the agents
				List<FeatureTerm> answer_1 = amail.result().get(0).generatePrediction(example.readPath(training_set.description_path), dm, false).solutions;
				List<FeatureTerm> answer_2 = amail.result().get(1).generatePrediction(example.readPath(training_set.description_path), dm, false).solutions;
				// Check the real answer and update score
				boolean instance = example.readPath(training_set.solution_path).equivalents(solution);
				Boolean assignment_1 = (answer_1.size() == 1)? answer_1.get(0).equals(solution) : null;
				Boolean assignment_2 = (answer_2.size() == 1)? answer_2.get(0).equals(solution) : null;
				// Update instances
				if(instance)
					instances[0] = instances[0] + 1;
				else
					instances[1] = instances[1] + 1;
				// Update assignments
				if(assignment_1)
					assignments_1[0] = assignments_1[0] + 1;
				else
					assignments_1[1] = assignments_1[1] + 1;
				if(assignment_2)
					assignments_2[0] = assignments_2[0] + 1;
				else
					assignments_2[1] = assignments_2[1] + 1;
				// Update the scores
				if(instance) {
					if(assignment_1 == null)
						score_1[3] = score_1[3] + 1;
					else if(assignment_1 == false)
						score_1[3] = score_1[3] + 1;
					else if(assignment_1 == true)
						score_1[0] = score_1[0] + 1;
					else
						System.out.println("   > BUG !!!!!!!!!!!!!!!!!!!!!!!");
					if(assignment_2 == null)
						score_2[3] = score_2[3] + 1;
					else if(assignment_2 == false)
						score_2[3] = score_2[3] + 1;
					else if(assignment_2 == true)
						score_2[0] = score_2[0] + 1;
					else
						System.out.println("   > BUG !!!!!!!!!!!!!!!!!!!!!!!");
				}
				if(!instance) {
					if(assignment_1 == null)
						score_1[2] = score_1[2] + 1;
					else if(assignment_1 == false)
						score_1[1] = score_1[1] + 1;
					else if(assignment_1 == true)
						score_1[2] = score_1[2] + 1;
					else
						System.out.println("   > BUG !!!!!!!!!!!!!!!!!!!!!!!");
					if(assignment_2 == null)
						score_2[2] = score_2[2] + 1;
					else if(assignment_2 == false)
						score_2[1] = score_2[1] + 1;
					else if(assignment_2 == true)
						score_2[2] = score_2[2] + 1;
					else
						System.out.println("   > BUG !!!!!!!!!!!!!!!!!!!!!!!");
				}
			}
			// Update the 
			System.out.println("   > Solution : "+solution.toStringNOOS(dm));
			System.out.println("      > score 1 : "+score_1[0]+" "+score_1[1]+" "+score_1[2]+" "+score_1[3]);
			System.out.println("      > P/R 1: "+score_1[0] / (score_1[0] + score_1[2])+" / "+score_1[0]/(score_1[0] + score_1[3]));
			System.out.println("      > score 2 : "+score_2[0]+" "+score_2[1]+" "+score_2[2]+" "+score_2[3]);
			System.out.println("      > P/R 2: "+score_2[0] / (score_2[0] + score_2[2])+" / "+score_2[0]/(score_2[0] + score_2[3]));
			for(int i=0; i<4; i++) {
				num_1[i] = num_1[i] + (score_1[i] / training_set.cases.size());
				num_2[i] = num_2[i] + (score_2[i] / training_set.cases.size());
			}
			denum ++;
		System.out.println("   > Rules exchanged = "+amail.last_rules_sent);
		System.out.println("   > Examples exchanged = "+(amail.last_counterexamples_sent+amail.last_uncoveredexamples_sent+amail.last_skepticalexamples_sent));
		}
		System.out.println("   > Average : ");
		System.out.println("      > score 1 : "+num_1[0] / denum+" "+num_1[1] / denum+" "+num_1[2] / denum+" "+num_1[3] / denum);
		System.out.println("      > score 2 : "+num_2[0] / denum+" "+num_2[1] / denum+" "+num_2[2] / denum+" "+num_2[3] / denum);
		System.out.println("      > P/R 1: "+num_1[0] / (num_1[0] + num_1[2])+" / "+num_1[0]/(num_1[0] + num_1[3]));
		System.out.println("      > P/R 2: "+num_2[0] / (num_2[0] + num_2[2])+" / "+num_2[0]/(num_2[0] + num_2[3]));
		
		/*double num = 0;
		double denom = 0;
		// Diachronic agreement
		List<FeatureTerm> list = new ArrayList<FeatureTerm>(training_set.cases);
		for(int i=0; i<list.size(); i++) {
			for(int j=i+1; j<list.size(); j++) {
				// Examples
				FeatureTerm e1 = list.get(i);
				FeatureTerm e2 = list.get(j);
				// Names
				List<FeatureTerm> answer_K1 = amail.result().get(1).generatePrediction(e1.readPath(training_set.description_path), dm, false).solutions;
				List<FeatureTerm> answer_K2 = amail.result().get(1).generatePrediction(e2.readPath(training_set.description_path), dm, false).solutions;
				List<FeatureTerm> answer_K1i = h11.generatePrediction(e1.readPath(training_set.description_path), dm, false).solutions;
				List<FeatureTerm> answer_K2i = h11.generatePrediction(e2.readPath(training_set.description_path), dm, false).solutions;
				// Test
				if(answer_K1i.size() == 1 && answer_K2i.size() == 1 && !answer_K1i.equals(answer_K2i)) {
					denom ++;
					if(answer_K1.size() == 1 && answer_K2.size() == 1 && answer_K1.equals(answer_K2))
						num ++;
				}
			}
		}
		System.out.println(1 - (num/denom));*/
		
		
		//System.out.println( out / training_set.cases.size());
		
		//System.out.println(amail.result().get(0).toString(dm));
		//System.out.println(amail.result().get(1).toString(dm));
		
	}

}
