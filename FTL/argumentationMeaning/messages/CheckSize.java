package messages;

import enumerators.Performative;
import enumerators.State;
import tools.Mailbox;

public class CheckSize extends AbstractMessage{

	private Integer element;

	public CheckSize(State state, Integer size) {
		// Initialize message
		this.state = state;
		this.type = Performative.ExtSize;
		this.setElement(size);
	}

	public Integer getElement() {
		return element;
	}

	public void setElement(Integer element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.integer = this.getElement();
	}
	
}
