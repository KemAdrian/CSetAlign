package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ArgID;
import tools.Mailbox;

public class AcceptAttack extends AbstractMessage{

	private ArgID element;

	public AcceptAttack(State state, ArgID attack_id) {
		// Initialize message
		this.state = state;
		this.type = Performative.AcceptAttack;
		this.setElement(attack_id);
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
