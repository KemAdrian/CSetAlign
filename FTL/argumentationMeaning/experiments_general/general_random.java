package experiments_general;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import evaluation.ExpFileManager;
import run_general_scripts.run_random;
import tools.ToolSet;

public class general_random {
	// IVs of the experiments²
	private static int N = 200;
	private static int DOMAIN  = TrainingSetUtils.SOYBEAN_DATASET;
	private static int THRESHOLD = 5;
	private static String STRATEGY = "EXT";
	private static String DOMAIN_NAME = getDomainName(DOMAIN);
	
	public static void main(String[] args) throws Exception {
		
		// Initiate the file manager variables
		ExpFileManager.RECORD = 1;
		ExpFileManager.n = general_random.N;
		ExpFileManager.nb_domain = general_random.DOMAIN;
		ExpFileManager.domain = general_random.DOMAIN_NAME;
		ExpFileManager.strategy = general_random.STRATEGY;
		ToolSet.THRESHOLD = THRESHOLD;

		// Create file for experiments
		ExpFileManager.createDraft();
		// Run N experiments
		for(int i=0; i<N; i++) {
			// Header
			System.out.println(" _ _ _ _ _ _ _ _ _ _ _ EXP "+(i+1)+" _ _ _ _ _ _ _ _ _ _ _ ");
			// Create the experiment's thread
			Thread t = new Thread(new run_random());
			// Get initial time
			long t1 = System.currentTimeMillis();
			// Start experiemnt
			t.start();
			while(t.getState() != Thread.State.TERMINATED) {
				// do nothing
				Thread.sleep(1000);
				// Check that it didn't get stuck in a loop
				if(System.currentTimeMillis() - t1 > 100000000) {
					System.out.println("    > INTERRUPTING <");
					t.interrupt();
					break;
				}
			}
			ExpFileManager.writeDraft();
			if(ExpFileManager.success < 0)
				break;
			ExpFileManager.reset();
 		}
		ExpFileManager.saveDraft();
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
