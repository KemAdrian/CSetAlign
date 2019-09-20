package messages;

import java.util.ArrayList;
import java.util.List;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import tools.Mailbox;

public class Intransitive extends AbstractMessage{

	private List<ConID> element;

	public Intransitive(State state, ConID id, List<ConID> intransitives) {
		// Initialize message
		this.state = state;
		this.type = Performative.Intransitive;
		this.setId(id);
		this.setElement(new ArrayList<>(intransitives));
	}

	public List<ConID> getElement() {
		return element;
	}

	public void setElement(List<ConID> element) {
		this.element = new ArrayList<>(element);
	}
	
	public void readMessage(Mailbox m) {
		m.con_id = this.getId();
		m.id_list = this.getElement();
	}

}
