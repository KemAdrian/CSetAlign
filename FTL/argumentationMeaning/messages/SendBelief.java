package messages;

import enumerators.Performative;
import enumerators.State;
import interfaces.Agent;
import interfaces.Node;
import tools.Counter;
import tools.Mailbox;

public class SendBelief extends AbstractMessage {
	
	
	private Node element;

	public SendBelief(Agent a, State state, String nick, Node belief) {
		// Count elements
		Counter.getGeneralizationCounter(a).increment(belief.toGeneralizations().size());
		// Initialize message
		this.state = state;
		this.type = Performative.Belief;
		this.sign = nick;
		this.setElement(belief.clone());
	}

	public Node getElement() {
		return element;
	}

	public void setElement(Node element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.sign = this.getSign();
		m.belief = this.getElement();
	}

}
