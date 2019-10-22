package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import interfaces.Agent;
import semiotic_elements.Generalization;
import tools.Counter;
import tools.Mailbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Assert extends AbstractMessage{
	
	private List<Generalization> element;
	
	public Assert(State state, ConID id) {
		this.state = state;
		this.type = Performative.Assert;
		this.id = id;
	}

	public Assert(Agent a, State state, ConID id, String sign, Collection<Generalization> I) {
		this.state = state;
		// Count elements
		Counter.getGeneralizationCounter(a).increment(I.size());
		// Initialize message
		this.type = Performative.Assert;
		this.id = id;
		this.sign = sign.toString();
		this.setElement(new ArrayList<>(I));
	}

	public List<Generalization> getElement() {
		return element;
	}

	public void setElement(List<Generalization> element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.con_id = this.getId();
		m.sign = this.getSign();
		m.generalization_list = this.getElement();
	}
	

}
