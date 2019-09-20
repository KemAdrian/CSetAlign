package messages;

import enumerators.Performative;
import enumerators.State;
import identifiers.ConID;
import interfaces.Message;

public abstract class AbstractMessage implements Message{
	
	protected ConID id;
	protected String sign;
	protected Performative type;
	protected State state;
	
	public ConID getId() {
		return id;
	}
	
	public void setId(ConID id) {
		this.id = id;
	}
	
	public String getSign() {
		return sign;
	}
	
	public void setSign(String sign) {
		this.sign = sign;
	}
	
	public State readState() {
		return this.state;
	}
	
	public Performative readPerformative(){
		return this.type;
	}
	
}
