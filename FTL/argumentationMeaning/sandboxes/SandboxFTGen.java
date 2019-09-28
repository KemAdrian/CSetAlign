package sandboxes;

import csic.iiia.ftl.learning.core.TrainingSetProperties;
import tools.FTGen;

public class SandboxFTGen {
	
	public static void main(String[] arg) {
		
		FTGen.initialize(3, false, true, 100, 1000, 100, 3);

		for(TrainingSetProperties ts : FTGen.saved){
			System.out.println(ts.cases.size());
		}


		// Get data set
		TrainingSetProperties dataset = FTGen.getNext();

		// Regroup rules
		/*Map<FeatureTerm, List<Rule>> idefs = new HashMap<>();
		Map<FeatureTerm, List<FeatureTerm>> edefs = new HashMap<>();
		for(FeatureTerm s : dataset.differentSolutions()) {
			idefs.put(s, new ArrayList<>());
			edefs.put(s, new ArrayList<>());
		}
		for(Rule r : FTGen.saved_rules.get(dataset))
			idefs.get(r.solution).add(r);
		for(FeatureTerm ft : dataset.cases)
			edefs.get(ft.readPath(LPkg.solution_path())).add(ft);

		// Check if the idefs are subsuming all the examples from the edefs
		System.out.println("Own Examples");
		for(FeatureTerm s : dataset.differentSolutions()){
			int total = 0;
			int classified = 0;
			for(FeatureTerm ft : edefs.get(s)) {
				for (Rule r : idefs.get(s)) {
					if (r.pattern.subsumes(ft.readPath(LPkg.description_path()))) {
						classified++;
						break;
					}
				}
				total++;
			}
			System.out.println("solution = "+s.toStringNOOS(LPkg.dm()));
			System.out.println("successes = "+classified+" / "+total);
		}

		// Check if the idefs are subsuming any examples that are not from the edefs
		System.out.println("Others Examples");
		for(FeatureTerm s : dataset.differentSolutions()){
			int total = 0;
			int classified = 0;
			List<FeatureTerm> other_exs = new ArrayList<>();
			for(FeatureTerm s2 : dataset.differentSolutions()){
				if(!s2.equivalents(s)){
					other_exs.addAll(edefs.get(s2));
				}
			}
			for(FeatureTerm ft : other_exs) {
				for (Rule r : idefs.get(s)) {
					if (r.pattern.subsumes(ft.readPath(LPkg.description_path()))) {
						classified++;
						break;
					}
				}
				total++;
			}
			System.out.println("solution = "+s.toStringNOOS(LPkg.dm()));
			System.out.println("successes = "+classified+" / "+total);
		}*/
		
	}

}
