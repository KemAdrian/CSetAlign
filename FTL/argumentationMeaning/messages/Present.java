package messages;

import enumerators.Performative;
import enumerators.State;
import semiotic_elements.Example;
import tools.Mailbox;

public class Present extends AbstractMessage{

	private Example element;

	public Present(State state, Example e) {
		// Initialize message
		this.state = state;
		this.type = Performative.Present;
		this.setElement(e);
	}

	public Example getElement() {
		return element;
	}

	public void setElement(Example element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.example = this.getElement();
	}

}
