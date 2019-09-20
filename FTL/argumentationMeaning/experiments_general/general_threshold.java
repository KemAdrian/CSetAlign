package experiments_general;

import java.util.Random;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import evaluation.ExpFileManager;
import run_general_scripts.run_general;
import tools.ToolSet;

public class general_threshold {
		// IVs of the experiments�
	public static int N = 150;
	public static int DOMAIN  = TrainingSetUtils.SOYBEAN_DATASET;
	public static int THRESHOLD = 10;
	public static String STRATEGY = "EXT";
	public static String DOMAIN_NAME = getDomainName(DOMAIN);
	
	public static void main(String[] args) throws Exception {
		
		// Create a Random object
		Random rand = new Random(System.currentTimeMillis());
		// Initiate the file manager variables
		ExpFileManager.RECORD = 1;
		ExpFileManager.n = general_threshold.N;
		ExpFileManager.nb_domain =general_threshold.DOMAIN;
		ExpFileManager.domain = general_threshold.DOMAIN_NAME;
		ExpFileManager.strategy = general_threshold.STRATEGY;
		// Create file for experiments
		ExpFileManager.createDraft();
		// Run N experiments
		for(int i=0; i<N; i++) {
			// Put a random threshold
			THRESHOLD = rand.nextInt(15)+1;
			ToolSet.THRESHOLD = THRESHOLD;
			// Header
			System.out.println(" _ _ _ _ _ _ _ _ _ _ _ EXP "+(i+1)+" _ _ _ _ _ _ _ _ _ _ _ ");
			// Create the experiment's thread
			Thread t = new Thread(new run_general());
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
