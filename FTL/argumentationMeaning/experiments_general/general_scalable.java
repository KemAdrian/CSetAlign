package experiments_general;

import csic.iiia.ftl.learning.core.TrainingSetUtils;
import evaluation.ExpFileManager;
import identifiers.IDCounter;
import run_general_scripts.run_scalable;
import tools.FTGen;
import tools.Loop;
import tools.Pair;

public class general_scalable {
		// IVs of the experiments
	private static int N = 120;
	private static int DOMAIN  = TrainingSetUtils.DEMOSPONGIAE_280_DATASET;
	private static String STRATEGY = "EXT";
	private static String DOMAIN_NAME = getDomainName(DOMAIN);
	
	public static void main(String[] args) throws Exception {

		// Initiate the file manager variables
		ExpFileManager.addBlock("nb_exp",N);
		ExpFileManager.addBlock("nb_domain",general_scalable.DOMAIN);
		ExpFileManager.addBlock("name_domain",general_scalable.DOMAIN_NAME);
		ExpFileManager.addBlock("strategy", general_scalable.STRATEGY);
		
		// Bracket for context size
		int min = 100;
		int max = 101;
		int pace = 100;

		// Initialize parameters
        Loop<Pair<Double, Double>> sort_size_list = new Loop<>();
        Loop<Pair<Double, Double >> id_size_list = new Loop<>();
        Loop<Pair<Double, Double>> gen_size_list = new Loop<>();

        double var = 0.;
		for(int i=6; i<11; i++){
		    for(int j=1; j<5; j++) {
                for (int k = 2; k < 7; k++) {
                    sort_size_list.add(new Pair<>((double) i, var));
                    id_size_list.add(new Pair<>((double) j, var));
                    gen_size_list.add(new Pair<>((double) k, var));
                }
            }
        }

		// Create contexts
		FTGen.parametric_initialize(3, min, max, pace, N, sort_size_list, id_size_list, gen_size_list);
		System.out.println(FTGen.saved.size());
		// Run N experiments
		for(int j=min; j<max; j+=pace) {
			for(int i=0; i<N; i++) {
				// Header
				System.out.println(" _ _ _ _ _ _ _ _ _ _ _ EXP "+(i+1)+" _ _ _ _ _ _ _ _ _ _ _ ");
				// Force the context's size
				TrainingSetUtils.NB_EX = j;
				// Create the experiment's thread
				Thread t = new Thread(new run_scalable());
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
				IDCounter.reset();
				ExpFileManager.nextLine();
			}
			ExpFileManager.createDraft();
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
