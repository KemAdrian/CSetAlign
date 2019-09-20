package interfaces;

import enumerators.Performative;
import enumerators.State;
import tools.Mailbox;

public interface Message {
	
	public Performative readPerformative();
	public State readState();
	public void readMessage(Mailbox m);

}
