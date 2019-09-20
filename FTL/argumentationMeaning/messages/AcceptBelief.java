package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ArgID;
import tools.Mailbox;

public class AcceptBelief extends AbstractMessage{

	private ArgID element;

	public AcceptBelief(State state, ArgID belief_id) {
		// Initialize message
		this.state = state;
		this.type = Performative.AcceptBelief;
		this.setElement(belief_id);
	}

	public ArgID getElement() {
		return element;
	}

	public void setElement(ArgID element) {
		this.element = element;
	}

	public void readMessage(Mailbox m) {
		m.arg_id = this.getElement();
	}
	

}
