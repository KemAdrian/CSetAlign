package tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enumerators.State;
import interfaces.Agent;
import interfaces.Message;
import messages.Present;
import semiotic_elements.Example;

public class Token {
	
	private static Agent defender;
	private static Agent attacker;
	private static Map<Agent,Map<State,List<Message>>> messages;
	
	public static void initialize(Agent a, Agent d){
		Token.attacker = a;
		Token.defender = d;
		Token.messages = new HashMap<>();
		messages.put(a, new HashMap<>());
		messages.put(d, new HashMap<>());
		for(State state : State.values()) {
			messages.get(a).put(state, new ArrayList<>());
			messages.get(d).put(state, new ArrayList<>());
		}
	}
	
	public static void switchRoles(){
		Agent temp = Token.attacker;
		Token.attacker = Token.defender;
		Token.defender = temp;
		State current = defender.state();
		defender.getMessages(messages.get(defender).get(current));
		messages.get(defender).put(current, new ArrayList<>());
	}
	
	public static Agent defender(){
		return Token.defender;
	}
	
	public static Agent attacker(){
		return Token.attacker;
	}
	
	public static void sendMessages(List<? extends Message> messages) {
		for(Message m : messages) {
			Token.messages.get(attacker).get(m.readState()).add(m);
		}
	}
	
	public static void sendExample(Example e) {
		Token.messages.get(attacker).get(State.WaitExample).add(new Present(State.WaitExample,e));
		Token.messages.get(defender).get(State.WaitExample).add(new Present(State.WaitExample,e));
	}

}
