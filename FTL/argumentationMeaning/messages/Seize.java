package messages;

import enumerators.Performative;
import enumerators.State;
import tools.Mailbox;

public class Seize extends AbstractMessage {
	
	private Boolean element;

	public Seize(State state) {
		// Initialize message
		this.state = state;
		this.type = Performative.Seize;
		this.setElement(true);
	}
	
	public Seize(State state, Boolean switch_pos) {
		// Initialize message
		this.state = state;
		this.type = Performative.Seize;
		this.setElement(switch_pos);
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
