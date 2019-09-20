package identifiers;

public class GenID extends Counter {
	
	public Integer id;
	
	public GenID() {
		id = count;
		if(!this.isNull())
			count += 1;
	}
	
	public String toString() {
		return "Generalization("+id+")";
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
	    if (o == null) return false;
	    if (getClass() != o.getClass()) return false;
	    return equals((GenID) o);
	}
	
	public boolean equals(GenID id) {
		if (id == this) return true;
	    if (id == null) return false;
		return this.id.equals(id.id);
	}
	
	public boolean isNull() {
		return false;
	}

}
