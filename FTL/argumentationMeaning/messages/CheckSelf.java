package messages;

import enumerators.Performative;
import enumerators.State;
import tools.Mailbox;

public class CheckSelf extends AbstractMessage {
	
	private Boolean element;

	public CheckSelf(State state) {
		// Initialize message
		this.state = state;
		this.type = Performative.CheckSelf;
		this.setElement(true);
	}

	public Boolean getElement() {
		return element;
	}

	public void setElement(Boolean element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.bool_switch = this.getElement();
	}
	
}
