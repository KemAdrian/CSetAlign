package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import tools.Mailbox;

public class Remove extends AbstractMessage{

	public Remove(State state, ConID id) {
		this.state = state;
		this.type = Performative.Remove;
		this.id = id;
	}
	
	public void readMessage(Mailbox m) {
		m.con_id = this.getId();
	}

}
