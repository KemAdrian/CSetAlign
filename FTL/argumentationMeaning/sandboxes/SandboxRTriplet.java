package sandboxes;

import identifiers.ConID;
import tools.RTriplet;
import tools.ToolSet;

public class SandboxRTriplet {
	
	public static void main(String[] arg) throws Exception{
		
		ToolSet.THRESHOLD = 1;
		
		ConID Ci = new ConID();
		ConID Cj = new ConID();
		
		int[] t1 = {1, 0, 1};
		int[] t2 = {0, 1, 0};
		
		RTriplet rt1 = new RTriplet(Ci, Cj, t1);
		RTriplet rt2 = new RTriplet(Cj, Ci, t2);
		System.out.println(rt1);
		System.out.println(rt1.combine(rt2));
		
	
	}
	
	public static String display(int[] t) {
		return  "("+t[0]+" "+t[1]+" "+t[2]+")";
	}

}
