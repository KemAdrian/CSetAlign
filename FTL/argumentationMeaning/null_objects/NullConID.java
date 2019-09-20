package null_objects;

import identifiers.ConID;

public class NullConID extends ConID{
	
	public NullConID() {
		id = -1;
	}
	
	public String toString() {
		return "NullID";
	}
	
	public boolean equals(Object o) {
		return false;
	}
	
	public boolean isNull() {
		return true;
	}

}
