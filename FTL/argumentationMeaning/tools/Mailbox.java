package tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import enumerators.Performative;
import enumerators.Relation;
import identifiers.ArgID;
import identifiers.ConID;
import interfaces.Message;
import interfaces.Node;
import semiotic_elements.Example;
import semiotic_elements.Generalization;

public class Mailbox {
	
	// List of messages
	Map<Performative,List<Message>> messages;
	
	// Booleans
	public Boolean bool_switch;
	// Objects (String)
	public String sign;
	// Objects (Nodes)
	public Node node;
	public Node belief;
	// Objects (IDs)
	public ArgID arg_id;
	public ConID con_id;
	public ConID con_id1, con_id2;
	// Objects (Other)
	public Integer integer;
	public Pair<ConID, ConID> pair;
	public Triplet<ConID, ConID, RTriplet> rtriplet;
	public Triplet<String, String, Double> vote;
	public Triplet<ConID, ConID, Relation> relation;
	// Semiotic Elements
	public Example example;
	// Collections of Semiotic Elements
	public List<ConID> id_list;
	public Collection<String> sign_list;
	public Collection<Example> example_list;
	public Collection<Generalization> generalization_list;
	
	public Mailbox() {
		this.messages = new HashMap<>();
		for(Performative perf : Performative.values())
			messages.put(perf, new ArrayList<>());
	}
	
	public void nullifyVariables() {
		this.bool_switch = false;
		this.sign = null;
		this.node = null;
		this.belief = null;
		this.con_id = null;
		this.con_id1 = null;
		this.con_id2 = null;
		this.arg_id = null;
		this.integer = null;
		this.pair = null;
		this.rtriplet = null;
		this.vote = null;
		this.relation = null;
		this.example = null;
		this.id_list = null;
		this.sign_list = null;
		this.example_list = null;
		this.generalization_list = null;
	}
	
	public void clearPerformative(Performative perf) {
		messages.put(perf, new ArrayList<>());
		nullifyVariables();
	}
	
	public List<Message> getMessages(Performative performative){
		return messages.get(performative);
	}
	
	public void readMessage(Message m) {
		nullifyVariables();
		m.readMessage(this);
	}
	
	public void setMail(List<? extends Message> mail) {
		for(Message m : mail) {
			messages.get(m.readPerformative()).add(m);
		}
	}
	
	public List<Message> getMail(){
		List<Message> output = new ArrayList<>();
		for(Entry<Performative,List<Message>> m : messages.entrySet()) {
			output.addAll(m.getValue());
		}
		return output;
	}

}
