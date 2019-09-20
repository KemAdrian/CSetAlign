package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import tools.Mailbox;

public class Replace extends AbstractMessage {

	public Replace(State state, ConID id, String sign) {
		// Initialize message
		this.state = state;
		this.type = Performative.Replace;
		this.setId(id);
		this.setSign(sign);
	}
	
	public void readMessage(Mailbox m) {
		m.con_id = this.getId();
		m.sign = this.getSign();
	}

}
