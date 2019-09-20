package messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import enumerators.Performative;
import enumerators.State;
import evaluation.ExpFileManager;
import interfaces.Agent;
import semiotic_elements.Example;
import tools.Mailbox;

public class SendExamples extends AbstractMessage{

	private List<Example> element;

	public SendExamples(Agent a, State state, Collection<Example> se) {
		// Count elements
		if(ExpFileManager.RECORD == 1)
			ExpFileManager.e_count.get(a).increment(se.size());
		// Initialize message
		this.state = state;
		this.type = Performative.SendExamples;
		this.setElement(new ArrayList<Example>(se));
	}

	public List<Example> getElement() {
		return element;
	}

	public void setElement(List<Example> element) {
		this.element = element;
	}
	
	public void readMessage(Mailbox m) {
		m.example_list = this.getElement();
	}

}
