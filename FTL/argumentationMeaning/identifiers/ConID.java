package identifiers;

public class ConID extends Counter {
	
	public Integer id;
	
	public ConID() {
		id = count;
		if(!this.isNull())
			count += 1;
	}
	
	public String toString() {
		return "Concept("+id+")";
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
	    if (o == null) return false;
	    if (getClass() != o.getClass()) return false;
	    return equals((ConID) o);
	}
	
	public boolean equals(ConID id) {
		if (id == this) return true;
	    if (id == null) return false;
		return this.id.equals(id.id);
	}
	
	public boolean isNull() {
		return false;
	}

}
