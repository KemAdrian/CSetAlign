package tools;

public class Test {
	
	public int tp;
	public int fp;
	public int tn;
	public int fn;
	
	public Test(int tp, int fp, int tn, int fn) {
		this.tp = tp;
		this.fp = fp;
		this.tn = tn;
		this.fn = fn;
	}
	
	public String toString() {
		return "tp = "+tp+"; fp = "+fp+"; tn = "+tn+"; fn = "+fn;
	}

}
