package tools;

public class Pair<L,R> {

	private final L left;
	private final R right;
	
	public Pair(L e1, R e2){
		this.left = e1;
		this.right = e2;
	}
	
	public L getLeft(){
		return this.left;
	}
	
	public R getRight(){
		return this.right;
	}
	
	public int hashCode(){
		return this.left.hashCode() ^ this.right.hashCode();
	}
	
	/*public boolean equals(Object p){
		if (!(p instanceof Pair))
			return false;
		@SuppressWarnings("rawtypes")
		Pair pairo = (Pair) p;
		return (this.left.toString().equals(pairo.getLeft().toString()) && this.right.toString().equals(pairo.getRight().toString())) ||  (this.left.toString().equals(pairo.getRight().toString()) && this.right.toString().equals(pairo.getLeft().toString()));
	}*/
	
	public String toString(){
		return "("+left.toString()+" ; "+right.toString()+")";
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o == this) return true;
	    if (o == null) return false;
	    if (getClass() != o.getClass()) return false;
	    return equals((Pair<L,R>) o);
	}
	
	public boolean equals(Pair<L,R> pair) {
		return (left.equals(pair.left) && right.equals(pair.right));
	}
	
}
