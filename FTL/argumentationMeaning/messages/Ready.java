package messages;

import enumerators.Performative;
import enumerators.State;
import tools.Mailbox;

public class Ready extends AbstractMessage {
	
	private Boolean element;

	public Ready() {
		// Initialize message
		this.state = State.EvaluateReadinessState;
		this.type = Performative.ArgumentationReady;
		this.setElement(true);
	}
	
	public Ready(State state, Boolean switch_pos) {
		// Initialize message
		this.state = State.EvaluateReadinessState;
		this.type = Performative.ArgumentationReady;
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
