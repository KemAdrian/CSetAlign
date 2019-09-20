package sandboxes;

import csic.iiia.ftl.learning.core.TrainingSetProperties;
import tools.FTGen;

public class SandboxFTGen {
	
	public static void main(String[] arg) throws Exception{
		
		FTGen.initialize(3, true, 100, 1000, 100, 25);
		
		int i = 0;
		for(TrainingSetProperties set : FTGen.saved) {
			System.out.println(i+" : "+set.cases.size());
			i++;
		}
		
	}

}
