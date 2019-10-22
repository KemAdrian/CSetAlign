package experiments_general;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import evaluation.ExpFileManager;
import identifiers.IDCounter;
import run_general_scripts.run_general;
import tools.ToolSet;

import java.util.List;

public class general_cover {
		// IVs of the experiments�
	private static int N = 100;
	private static String STRATEGY = "EXT";
	
	public static void main(String[] args) throws Exception {

		// Set domain
		List<Integer> domains = List.of(TrainingSetUtils.ZOOLOGY_DATASET_LB, TrainingSetUtils.DEMOSPONGIAE_120_DATASET, TrainingSetUtils.SOYBEAN_DATASET);
		List<Integer> thresholds = List.of(6,10,11);

		// Run N experiments
		for(int j=0; j<domains.size(); j++){
			for (int i = 0; i < N; i++) {
				// Initiate the file manager variables
				ToolSet.THRESHOLD = thresholds.get(j);
				TrainingSetUtils.NB_DOMAIN = domains.get(j);
				ExpFileManager.addBlock("nb_exp",N);
				ExpFileManager.addBlock("nb_domain", domains.get(j));
				ExpFileManager.addBlock("name_domain", getDomainName(domains.get(j)));
				ExpFileManager.addBlock("strategy", general_cover.STRATEGY);
				// Header
				System.out.println(" _ _ _ _ _ _ _ _ _ _ _ EXP " + (i + 1) + " _ _ _ _ _ _ _ _ _ _ _ ");
				// Create the experiment's thread
				Thread t = new Thread(new run_general());
				// Get initial time
				long t1 = System.currentTimeMillis();
				// Start experiment
				t.start();
				while (t.getState() != Thread.State.TERMINATED) {
					// do nothing
					Thread.sleep(1000);
					// Check that it didn't get stuck in a loop
					if (System.currentTimeMillis() - t1 > 100000000) {
						System.out.println("    > INTERRUPTING <");
						t.interrupt();
						break;
					}
				}
				IDCounter.reset();
				ExpFileManager.nextLine();
			}
 		}
		ExpFileManager.createDraft();
	}
	
	private static String getDomainName(int i) {
		switch (i) {
		case TrainingSetUtils.SEAT_ALL:
			return "SEAT";
		case TrainingSetUtils.ZOOLOGY_DATASET_LB:
			return "ZOOS";
		case TrainingSetUtils.DEMOSPONGIAE_120_DATASET:
			return "SPON";
		case TrainingSetUtils.SOYBEAN_DATASET:
			return "SOYB";
		default:
			return "NC";
		}
	}

}
