package identifiers;

public class ArgID extends IDCounter {
	
	public Integer id;
	
	public ArgID() {
		id = count;
		count += 1;
	}
	
	public String toString() {
		return "Arg("+id+")";
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
	    if (o == null) return false;
	    if (getClass() != o.getClass()) return false;
	    return equals((ArgID) o);
	}
	
	public boolean equals(ArgID id) {
		if (id == this) return true;
	    if (id == null) return false;
		return this.id.equals(id.id);
	}

}
