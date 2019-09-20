package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import tools.Mailbox;
import tools.Triplet;

public class Relation extends AbstractMessage{
	
	private Triplet<ConID, ConID, enumerators.Relation> element;

	public Relation(State state, Triplet<ConID, ConID,  enumerators.Relation> t) {
		// Initialize message
		this.state = state;
		this.type = Performative.Relation;
		this.setElement(t.clone());
	}

	public Triplet<ConID, ConID,  enumerators.Relation> getElement() {
		return element;
	}

	public void setElement(Triplet<ConID, ConID,  enumerators.Relation> element) {
		this.element = element.clone();
	}
	
	public void readMessage(Mailbox m) {
		m.relation = this.getElement();
	}

}
