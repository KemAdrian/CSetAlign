package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import tools.Mailbox;

public class Baptise extends AbstractMessage{

	private Integer element;
	
	public Baptise(State state, ConID id, Integer i) {
		// Initialize message
		this.state = state;
		this.type = Performative.Baptise;
		this.setId(id);
		this.setElement(i);
	}

	public Baptise(State state, String sign, Integer i) {
		// Initialize message
		this.state = state;
		this.type = Performative.Baptise;
		this.setSign(sign);
		this.setElement(i);
	}
	
	public Baptise(State state, String sign, ConID id, Integer i) {
		// Initialize message
		this.state = state;
		this.type = Performative.Baptise;
		this.setId(id);
		this.setSign(sign);
		this.setElement(i);
	}

	public Integer getElement() {
		return element;
	}

	public void setElement(Integer element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.con_id = this.getId();
		m.sign = this.getSign();
		m.integer = this.getElement();
	}
	
}
