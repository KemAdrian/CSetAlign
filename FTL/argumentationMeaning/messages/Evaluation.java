package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import tools.Mailbox;
import tools.RTriplet;
import tools.Triplet;

public class Evaluation extends AbstractMessage{
	
	private Triplet<ConID, ConID, RTriplet> element;

	public Evaluation(State state, Triplet<ConID, ConID, RTriplet> t) {
		// Initialize message
		this.state = state;
		this.type = Performative.Evaluation;
		this.setElement(t.clone());
	}

	public Triplet<ConID, ConID, RTriplet> getElement() {
		return element;
	}

	public void setElement(Triplet<ConID, ConID, RTriplet> element) {
		this.element = element.clone();
	}
	
	public void readMessage(Mailbox m) {
		m.rtriplet = this.getElement();
	}

}
