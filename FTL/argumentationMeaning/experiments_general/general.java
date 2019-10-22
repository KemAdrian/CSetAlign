package experiments_general;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import evaluation.ExpFileManager;
import identifiers.IDCounter;
import run_general_scripts.run_general;
import tools.ToolSet;

public class general {
		// IVs of the experiments�
	private static int N = 1;
	private static int DOMAIN  = TrainingSetUtils.ZOOLOGY_DATASET_LB;
	private static String STRATEGY = "EXT";
	private static String DOMAIN_NAME = getDomainName(DOMAIN);
	
	public static void main(String[] args) throws Exception {
		
		// Initiate the file manager variables
		ExpFileManager.addBlock("nb_exp",N);
		ExpFileManager.addBlock("nb_domain",general.DOMAIN);
		ExpFileManager.addBlock("name_domain",general.DOMAIN_NAME);
		ExpFileManager.addBlock("strategy", general.STRATEGY);
		ToolSet.THRESHOLD = 6;

		//LPkg.ABUI_THRESHOLD = (float) 0.95;
		// Set domain
		TrainingSetUtils.NB_DOMAIN = DOMAIN;

		// Run N experiments
		for(int i=0; i<N; i++) {
			// Header
			System.out.println(" _ _ _ _ _ _ _ _ _ _ _ EXP "+(i+1)+" _ _ _ _ _ _ _ _ _ _ _ ");
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
				if(System.currentTimeMillis() - t1 > 100000000) {
					System.out.println("    > INTERRUPTING <");
					t.interrupt();
					break;
				}
			}
			IDCounter.reset();
			ExpFileManager.nextLine();
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
