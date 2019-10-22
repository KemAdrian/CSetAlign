package identifiers;

public class ExID extends IDCounter {
	
	public Integer id;
	
	public ExID() {
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
	    return equals((ExID) o);
	}
	
	public boolean equals(ExID id) {
		if (id == this) return true;
	    if (id == null) return false;
		return this.id.equals(id.id);
	}

}
