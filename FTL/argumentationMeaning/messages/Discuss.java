package messages;

import enumerators.Performative;
import enumerators.Relation;
import enumerators.State;
import identifiers.ConID;
import tools.Mailbox;
import tools.Triplet;

public class Discuss extends AbstractMessage {
	
	private Triplet<ConID, ConID, Relation> element;

	public Discuss(State state, Triplet<ConID, ConID, Relation> disagreement) {
		// Initialize message
		this.state = state;
		this.type = Performative.Debate;
		this.setElement(disagreement.clone());
	}

	public Triplet<ConID, ConID, Relation> getElement() {
		return element;
	}

	public void setElement(Triplet<ConID, ConID, Relation> element) {
		this.element = element.clone();
	}
	
	public void readMessage(Mailbox m) {
		m.relation = this.getElement();
	}

}
