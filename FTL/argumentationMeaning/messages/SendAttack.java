package messages;

import enumerators.Performative;
import enumerators.State;
import evaluation.ExpFileManager;
import interfaces.Agent;
import interfaces.Node;
import tools.Mailbox;

public class SendAttack extends AbstractMessage {
	
	
	private Node element;

	public SendAttack(Agent a, State state, String nick, Node argument) {
		// Count elements
		if(ExpFileManager.RECORD == 1) {
			ExpFileManager.e_count.get(a).increment(argument.toExamples().size());
			ExpFileManager.g_count.get(a).increment(argument.toGeneralizations().size());
		}
		// Initialize message
		this.state = state;
		this.type = Performative.Attack;
		this.sign = nick;
		this.setElement(argument.clone());
	}

	public Node getElement() {
		return element;
	}

	public void setElement(Node element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.sign = this.sign;
		m.node = this.getElement();
	}

}
