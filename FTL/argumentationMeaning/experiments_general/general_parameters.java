package experiments_general;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import evaluation.ExpFileManager;
import identifiers.IDCounter;
import run_general_scripts.run_parameters;
import tools.LPkg;
import tools.ToolSet;

public class general_parameters {
	// IVs of the experiments
	public static int N = 10;
	public static int DOMAIN  = TrainingSetUtils.ZOOLOGY_DATASET_LB;
	public static int THRESHOLD = 5;
	public static int REDUNDANCY = 0;
	public static String STRATEGY = "EXT";
	public static String DOMAIN_NAME = getDomainName(DOMAIN);



	public static void main(String[] args) throws Exception {


		// Initiate the file manager variables
		ExpFileManager.addBlock("nb_exp",N);
		ExpFileManager.addBlock("nb_domain",general_parameters.DOMAIN);
		ExpFileManager.addBlock("name_domain",general_parameters.DOMAIN_NAME);
		ExpFileManager.addBlock("strategy", general_parameters.STRATEGY);

		// Set domain
		TrainingSetUtils.NB_DOMAIN = DOMAIN;
		
		//ABUI.DEBUG = 1;
		for (int r = 0; r < 101; r += 50) {
			for (int aa = 0; aa < 100; aa += 5) {
				// Run N experiments
				for (int i = 0; i < N; i++) {
					REDUNDANCY = r;
					// Initiate the file manager variables
					ToolSet.THRESHOLD = THRESHOLD;
					ExpFileManager.addBlock("threshold",THRESHOLD);
					ExpFileManager.addBlock("redundancy",r);
					ExpFileManager.addBlock("abui_threshold",((float) aa) / 100);
					LPkg.ABUI_THRESHOLD = ((float) aa) / 100;
					TrainingSetUtils.REDUNDANCY = r;
					// Header
					System.out.println(" _ _ _ _ _ _ _ _ _ _ _ EXP " + (i + 1) + " _ _ _ _ _ _ _ _ _ _ _ ");
					// Display parameters info
					System.out.println("redundancy = " + r);
					System.out.println("arg accept = " + ((float) aa) / 100);
					// Create the experiment's thread
					Thread t = new Thread(new run_parameters());
					// Get initial time
					long t1 = System.currentTimeMillis();
					// Start experiemnt
					t.start();
					while (t.getState() != Thread.State.TERMINATED) {
						// do nothing
						Thread.sleep(1000);
						// Check that it didn't get stuck in a loop
						if (System.currentTimeMillis() - t1 > 60000) {
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
		}
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
