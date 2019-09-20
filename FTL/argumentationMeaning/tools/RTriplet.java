package tools;

import identifiers.ConID;

public class RTriplet {
	
	public ConID Ci,Cj;
	public Integer a,b,c;
	
	public RTriplet(ConID Ci, ConID Cj, int[] triplet) {
		this.Ci = Ci;
		this.Cj = Cj;
		this.a = triplet[0];
		this.b = triplet[1];
		this.c = triplet[2];
	}
	
	public int[] getTriplet() {
		return new int[] {a,b,c};
	}
	
	public int[] getTriplet(ConID Ci, ConID Cj) {
		if(!(this.Ci.equals(Ci) && this.Cj.equals(Cj)) && !(this.Ci.equals(Cj) && this.Cj.equals(Ci)))
			return null;
		if(this.Ci.equals(Ci) && this.Cj.equals(Cj))
			return getTriplet();
		if(this.Ci.equals(Cj) && this.Cj.equals(Ci))
			return getReverseTriplet();
		return null;
	}
	
	public int[] getReverseTriplet() {
		return new int[]{c,b,a};
	}
	
	public Integer getRElement(ConID Ci, ConID Cj) {
		if((Ci.equals(this.Ci) && Cj.equals(this.Cj)) || (Ci.equals(this.Cj) && Cj.equals(this.Ci)))
			return b;
		if((Ci.equals(this.Ci) && Cj.isNull()) || (Cj.equals(this.Ci) && Ci.isNull()))
			return a;
		if((Ci.equals(this.Cj) && Cj.isNull()) || (Cj.equals(this.Cj) && Ci.isNull()))
			return c;
		return null;
	}
	
	public void setRElement(ConID Ci, ConID Cj, int el) {
		if((Ci.equals(this.Ci) && Cj.equals(this.Cj)) || (Ci.equals(this.Cj) && Cj.equals(this.Ci)))
			b = el;
		if((Ci.equals(this.Ci) && Cj.isNull()) || (Cj.equals(this.Ci) && Ci.isNull()))
			a = el;
		if((Ci.equals(this.Cj) && Cj.isNull()) || (Cj.equals(this.Cj) && Ci.isNull()))
			c = el;
	}
	
	public RTriplet combine(RTriplet rt) {
		if(!(rt.Ci.equals(Ci) && rt.Cj.equals(Cj)) && !(rt.Ci.equals(Cj) && rt.Cj.equals(Ci)))
			return null;
		int[] m_rt = this.getTriplet();
		int[] o_rt = rt.getTriplet(Ci,Cj);
		int[] out = new int[] {0,0,0};
		for(int i=0; i<3; i++) {
			if(m_rt[i] >= ToolSet.THRESHOLD || o_rt[i] >= ToolSet.THRESHOLD)
				out[i] = ToolSet.THRESHOLD;
			/*else if(m_rt[i] + o_rt[i] >= 2 * ToolSet.THRESHOLD)
				out[i] = ToolSet.THRESHOLD;*/
			else if(m_rt[i] + o_rt[i] < ToolSet.THRESHOLD)
				out[i] = 0;
			else
				out[i] = -1;
		}
		return new RTriplet(Ci, Cj, out);
	}
	
	public RTriplet substract(RTriplet rt) {
		if(!(rt.Ci.equals(Ci) && rt.Cj.equals(Cj)) && !(rt.Ci.equals(Cj) && rt.Cj.equals(Ci)))
			return null;
		int[] m_rt = this.getTriplet();
		int[] o_rt = rt.getTriplet(Ci,Cj);
		int[] out = new int[] {m_rt[0] - o_rt[0], m_rt[1] - o_rt[1], m_rt[2] - o_rt[2]};
		return new RTriplet(Ci, Cj, out);
	}
	
	public RTriplet rectify(RTriplet rt) {
		if(!(rt.Ci.equals(Ci) && rt.Cj.equals(Cj)) && !(rt.Ci.equals(Cj) && rt.Cj.equals(Ci)))
			return null;
		int[] m_rt = this.getTriplet();
		int[] o_rt = rt.getTriplet(Ci,Cj);
		int[] out = new int[] {0,0,0};
		for(int i=0; i<3; i++)
			if(m_rt[i] >= ToolSet.THRESHOLD || o_rt[i] >= ToolSet.THRESHOLD)
				out[i] = ToolSet.THRESHOLD;
		return new RTriplet(Ci, Cj, out);
	}
	
	public String toString() {
		return "("+a+" "+b+" "+c+")";
	}
	

}
