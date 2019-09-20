package tools;

public class Triplet<L,M,R> {

	private L left;
	private M middle;
	private R right;
	
	public Triplet(L e1, M e2, R e3){
		this.left = e1;
		this.middle = e2;
		this.right = e3;
	}
	
	public L getLeft(){
		return this.left;
	}
	
	public M getMiddle(){
		return this.middle;
	}
	
	public R getRight(){
		return this.right;
	}
	
	public void setLeft(L left){
		this.left = left;
	}
	
	public void setMiddle(M middle){
		this.middle = middle;
	}
	
	public void setRight(R right){
		this.right = right;
	}
	
	public int hashCode(){
		return this.left.hashCode() ^ this.middle.hashCode() ^ this.right.hashCode();
	}
	
	public String toString(){
		return "("+left.toString()+" ; "+middle.toString()+" ; "+right.toString()+")";
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o == this) return true;
	    if (o == null) return false;
	    if (getClass() != o.getClass()) return false;
	    return equals((Triplet<L,M,R>) o);
	}
	
	public boolean equals(Triplet<L,M,R> t) {
		return (left.equals(t.left) && middle.equals(t.middle) && right.equals(t.right));
	}
	
	public Triplet<L,M,R> clone() {
		return new Triplet<L, M, R>(this.left, this.middle, this.right);
	}
}
