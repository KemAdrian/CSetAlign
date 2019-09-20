package messages;

import java.util.List;

import enumerators.Performative;
import enumerators.State;
import tools.Mailbox;

public class Name extends AbstractMessage{

	private List<String> element;

	public Name(State state, List<String> names) {
		// Initialize message
		this.state = state;
		this.type = Performative.Name;
		this.setElement(names);
	}

	public List<String> getElement() {
		return element;
	}

	public void setElement(List<String> names) {
		this.element = names;
	}
	
	public void readMessage(Mailbox m) {
		m.sign_list = this.getElement();
	}

}
