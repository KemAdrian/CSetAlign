package tools;

public class MutableInt {

	private int i;
	
	public MutableInt() {
		this.i = 0;
	}
	
	public void increment(int i) {
		this.i += i;
	}
	
	public int get() {
		return i;
	}
	
	public String toString() {
		return String.valueOf(i);
	}
}
