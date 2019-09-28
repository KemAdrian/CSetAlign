package experiments_general;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import evaluation.ExpFileManager;
import identifiers.Counter;
import run_general_scripts.run_general;
import tools.FTGen;

public class general_scalable {
		// IVs of the experiments
	private static int N = 20;
	private static int DOMAIN  = TrainingSetUtils.DEMOSPONGIAE_280_DATASET;
	private static String STRATEGY = "EXT";
	private static String DOMAIN_NAME = getDomainName(DOMAIN);
	
	public static void main(String[] args) throws Exception {
				
		// Initiate the file manager variables
		ExpFileManager.RECORD = 1;
		ExpFileManager.SAVE_FTGEN = 1;
		ExpFileManager.n = general_scalable.N;
		ExpFileManager.nb_domain = general_scalable.DOMAIN;
		ExpFileManager.domain = general_scalable.DOMAIN_NAME;
		ExpFileManager.strategy = general_scalable.STRATEGY;
		
		// Bracket for context size
		int min = 100;
		int max = 1000;
		int pace = 100;

		// Create contexts
		FTGen.initialize(3, false, true, min, max, pace, N);
		System.out.println(FTGen.saved.size());
		// Create file for experiments
		ExpFileManager.createDraft();
		// Run N experiments
		for(int j=min; j<max; j+=pace) {
			for(int i=0; i<N; i++) {
				// Header
				System.out.println(" _ _ _ _ _ _ _ _ _ _ _ EXP "+(i+1)+" _ _ _ _ _ _ _ _ _ _ _ ");
				// Force the context's size
				ExpFileManager.nb_examples = j;
				// Create the experiment's thread
				Thread t = new Thread(new run_general());
				// Get initial time
				long t1 = System.currentTimeMillis();
				// Start experiment
				t.start();
				while(t.getState() != Thread.State.TERMINATED) {
					// do nothing
					Thread.sleep(1000);
					// Check that it didn't get stuck in a loop
					if((System.currentTimeMillis() - t1) > 100000000) {
						System.out.println("    > INTERRUPTING <");
						t.interrupt();
						break;
					}
				}
				ExpFileManager.writeDraft();
				if(ExpFileManager.success < 0)
					break;
				Counter.reset();
				ExpFileManager.reset();
			}
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
