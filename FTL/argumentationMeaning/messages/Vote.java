package messages;

import enumerators.Performative;
import enumerators.State;
import tools.Mailbox;
import tools.Triplet;

public class Vote extends AbstractMessage {
	
	private Triplet<String, String, Double> element;

	public Vote(State state, Triplet<String, String, Double> t) {
		// Initialize message
		this.state = state;
		this.type = Performative.Elect;
		this.setElement(t.clone());
	}

	public Triplet<String, String, Double> getElement() {
		return element;
	}

	public void setElement(Triplet<String, String, Double> element) {
		this.element = element.clone();
	}
	
	public void readMessage(Mailbox m) {
		m.vote = this.getElement();
	}

}
