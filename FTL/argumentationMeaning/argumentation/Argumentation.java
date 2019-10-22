package argumentation;

import arguments.ArgTree;
import arguments.Belief;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import enumerators.ArgPhase;
import enumerators.Relation;
import identifiers.ConID;
import interfaces.Agent;
import interfaces.Message;
import interfaces.Node;
import messages.AcceptAttack;
import messages.AcceptBelief;
import messages.SendAttack;
import messages.SendBelief;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.LPkg;
import tools.ToolSet;

import java.util.*;

public class Argumentation {
	
	// Temp
	public int MAX_FP = 0;
	public int MMAX_FN = 0;
	public int OMAX_FN = 0;
	
	// Debug
	public static int DEBUG = 0;
	// Mailbox
	private List<Message> toSend;
	// Phase
	private ArgPhase current_phase;
	// Agent and ABUI
	public Agent agent;
	// Solved
	boolean solved;
	// At a position to resolve
	public boolean exempted;
	
	// Agents' nicks
	private String m_nick;
	// Agents' new signs
	public String con_newSign;
	public String hypo_newSign;
	// Agents' new ids
	public ConID m_con_id;
	public ConID o_con_id;
	public ConID hypo_id;
	// Special tokens for the argumentation
	private static FeatureTerm solutionToken;
	private static FeatureTerm notSolutionToken;
	public ArgTree my_argumentation_tree;
	public ArgTree other_argumentation_tree;
	private Set<Argument> accepted_arguments;
	// Set of examples
	private Set<Example> positive_examples;
	//private Set<Example> negative_examples;
	public Map<FeatureTerm,Set<Example>> context;
	
	public Argumentation(Agent agent) {
		// Set ABUI version
		ABUI.ABUI_VERSION = 2;
		
		// Reset the variables
		this.toSend = new ArrayList<>();
		this.current_phase = ArgPhase.CreateIDefPhase;
		this.agent = agent;
		this.solved = false;
		this.exempted = false;
		// Nicks
		this.m_nick = agent.nick();
		// Agents' new signs
		this.con_newSign = null;
		this.hypo_newSign = null;
		// Agents' new ids
		this.m_con_id = null;
		this.o_con_id = null;
		this.hypo_id = null;
		// Reset arguments and generalizations
		this.accepted_arguments = new HashSet<>();
		this.my_argumentation_tree = new ArgTree();
		this.other_argumentation_tree = new ArgTree();
		// Reset set of examples
		this.positive_examples = null;
		//this.negative_examples = null;
		// If the tokens are not created yet, create them
		try {
			if(solutionToken == null){
				solutionToken = new TermFeatureTerm(new Symbol("the_solution"), LPkg.solution_sort());
				LPkg.dm().addFT(solutionToken);
			}
			if(notSolutionToken == null){
				notSolutionToken = new TermFeatureTerm(new Symbol("not_the_solution"), LPkg.solution_sort());
				LPkg.dm().addFT(notSolutionToken);
			}
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
	}
	
	public void setUpExamples(Set<Example> positiveExamples, Set<Example> negativeExamples){
		// Set up the examples
		this.context = new HashMap<>();
		this.positive_examples = positiveExamples;
		//this.negative_examples = negativeExamples;
		context.put(solutionToken, positiveExamples);
		context.put(notSolutionToken, negativeExamples);
	}
	
	// General function that execute the selected phase
	public boolean solve() {
		System.out.println("max fp = "+MAX_FP);
		System.out.println("mmax fn = "+MMAX_FN);
		System.out.println("omax fn = "+OMAX_FN);
		/*System.out.println("positive examples           = "+positive_examples);
		System.out.println("negative examples           = "+negative_examples);
		System.out.println("positive examples (context) = "+context.get(solutionToken));
		System.out.println("negative examples (context) = "+context.get(notSolutionToken));
		System.out.println("exchanged examples          = "+agent.e_exchanged());*/
		switch(current_phase) {
		case CreateIDefPhase:
			current_phase = createIDef();
			break;
		case FirstExPhase:
			current_phase = firstIDefExamination();
			break;
		case ExIDefPhase:
			current_phase = standardIDefExamination();
			break;
		case Stop:
			current_phase = ArgPhase.Stop;
			break;
		}
		agent.sendMessages(toSend);
		cleanMailbox();
		return solved;
	}
	
	// Find intensional definition for positive examples
	private ArgPhase createIDef() {
		Belief myBelief;
		// Check if there are enough examples to create a belief
		if(exempted) {
			System.out.println("     > This agent has been exempted of concept creation");
			myBelief = new Belief.Builder().from(m_nick).labelled(solutionToken).build();
		}
		else if(positive_examples.size() < ToolSet.THRESHOLD) {
			System.out.println("     > Not enough examples to learn a belief");
			myBelief = new Belief.Builder().from(m_nick).labelled(solutionToken).build();
		}
		else {
			Set<Generalization> generalizations = new HashSet<>();
			System.out.println("     > Looking for a concept with an intensional definition that would make a satisfying belief");
			for(Concept c : agent.K().getAllConcepts())
				if(agent.agree(c, new HashSet<>(positive_examples)) == Relation.Equivalence) {
					Belief test = new Belief.Builder().from(m_nick).labelled(solutionToken).withGeneralizations(c.intensional_definition).build();
					System.out.println(test.classification(context));
					System.out.println(test.acceptable(context, MAX_FP, MMAX_FN));
					if(test.acceptable(context, MAX_FP, MMAX_FN)) {
						generalizations.addAll(c.intensional_definition);
						break;
					}
				}
			if(generalizations.isEmpty())
				for(Concept c : agent.H().getAllConcepts())
					if(agent.agree(c, new HashSet<>(positive_examples)) == Relation.Equivalence) {
						Belief test = new Belief.Builder().from(m_nick).labelled(solutionToken).withGeneralizations(c.intensional_definition).build();
						System.out.println(test.classification(context));
						System.out.println(test.acceptable(context, MAX_FP, MMAX_FN));
						if(test.acceptable(context, MAX_FP, MMAX_FN)) {
							generalizations.addAll(c.intensional_definition);
							break;
						}
					}
			if(!generalizations.isEmpty()) {
				System.out.println("     > Found an intensional definition that would make a good belief in one container");
				myBelief = new Belief.Builder().from(m_nick).labelled(solutionToken).withGeneralizations(generalizations).build();
			}
			else {
				System.out.println("     > Could not find satisfying intensional definition, learning new belief");
				myBelief = new Belief.Builder().from(m_nick).labelled(solutionToken).learn(context, accepted_arguments);
				if(!myBelief.acceptable(context,MAX_FP,MMAX_FN)) {
					System.out.println("       > Problem: our belief is unacceptable in our context!");
					int counter = 0;
					Float save = LPkg.ABUI_THRESHOLD;
					while(!myBelief.acceptable(context, MAX_FP, MMAX_FN)) {
						// Put an exit door
						if(LPkg.ABUI_THRESHOLD >= 0.9499) {
							System.out.println("       > Unable to create an acceptable intensional definition, sending an empty one...");
							myBelief = new Belief.Builder().from(m_nick).labelled(solutionToken).build();
							break;
						}
						else {
							LPkg.ABUI_THRESHOLD += (0.95 - LPkg.ABUI_THRESHOLD) / 2;
							System.out.println("       > Trying to create an acceptable belief: "+counter+" (aa= "+LPkg.ABUI_THRESHOLD+")");
							myBelief = myBelief.replace(accepted_arguments, context, MAX_FP, MMAX_FN, agent);
							counter ++;
						}
					}
					LPkg.ABUI_THRESHOLD = save;
				}
			}
		}
		my_argumentation_tree.addRoot(myBelief);
		System.out.println("evaluation: "+myBelief.classification(context));
		//Create a copy of belief
		Belief belief = myBelief.clone();
		// Empty belief before sending
		belief.empty(agent.g_exchanged());
		// Add generalizations to list of exchanges
		agent.addGExchanged(belief.generalizations());
		// Send belief to the other agent
		toSend.add(new SendBelief(agent, agent.state(), m_nick, belief));
		return ArgPhase.FirstExPhase;
	}
	
	// First examination of other's intensional definitionF
	private ArgPhase  firstIDefExamination() {
		// Check if we can accept other's belief
		if(other_argumentation_tree.root.acceptable(context,MAX_FP,OMAX_FN)) {
			System.out.println("      > Other's belief is accepted");
			other_argumentation_tree.agreed_upon = true;
			toSend.add(new AcceptBelief(agent.state(), other_argumentation_tree.root.getid()));
			return ArgPhase.ExIDefPhase;
		}
		// Otherwise, attack
		System.out.println("      > Other's belief rejected, attacking...");
		Set<Node> attacks = other_argumentation_tree.root.attack(context, accepted_arguments, new HashSet<>(), MAX_FP, OMAX_FN, agent);
		for(Node attack : attacks) {
			other_argumentation_tree.addNode(attack);
			// Create a copy of the attack before sending
			Node atk = attack.clone();
			// Empty attack before sending
			atk.empty(agent.g_exchanged());
			// Add generalizations to list of exchanges
			agent.addGExchanged(atk.toGeneralizations());
			// Send attack to the other agent
			toSend.add(new SendAttack(agent, agent.state(), m_nick, atk));
		}
		return ArgPhase.ExIDefPhase;
	}
	
	// Examine intensional definition tree of other agent
	private ArgPhase standardIDefExamination() {
		// Testing my tree
		if(!my_argumentation_tree.agreed_upon) {
			System.out.println("     > Checking our argumentation tree:");
			Set<Node> accepted_nodes = new HashSet<>();
			Set<Node> refused_nodes = new HashSet<>();
			Set<Node> past_nodes = new HashSet<>();
			Set<Node> destroy_buffer;
			// Test all the leaves until all defeated nodes are found
			do {
				destroy_buffer  = new HashSet<>();
				for(Node atk : my_argumentation_tree.getLeaves()) {
					if(!atk.getAgent().equals(m_nick)) {
						System.out.println("TESTING "+ atk.getid()+" = "+atk.acceptable(context, MAX_FP, MMAX_FN));
						if (atk.acceptable(context, MAX_FP, MMAX_FN)) {
							accepted_nodes.add(atk);
							if(atk.attacks() != null)
								destroy_buffer.add(my_argumentation_tree.getNode(atk.attacks()));
						} else
							refused_nodes.add(atk);
					}
				}
				// Delete the nodes that have been defeated
				for(Node destroyed : destroy_buffer) {
					System.out.println("       > Destroying the defeted node "+destroyed.getid());
					my_argumentation_tree.deleteNode(destroyed.getid());
				}
				// Keep deleted nodes in memory
				past_nodes.addAll(destroy_buffer);
			} while(!destroy_buffer.isEmpty());
			// Add accepted nodes to accepted arguments and notify other agent
			for(Node accepted : accepted_nodes) {
				System.out.println("       > Accepting "+accepted.getid());
				Set<Argument> arguments = accepted.toArguments();
				if(!arguments.isEmpty())
					accepted_arguments.addAll(arguments);
				toSend.add(new AcceptAttack(agent.state(), accepted.getid()));
			}
			// Generate attacks for the leaves that have been refused
			for(Node refused : refused_nodes) {
				System.out.println("       > Refusing "+refused.getid()+" and creating counter attacks");
				for(Node attack : refused.attack(context, accepted_arguments, past_nodes, MAX_FP, MMAX_FN, agent)) {
					// Add attack to argumentation tree
					my_argumentation_tree.addNode(attack);
					// Create a copy of the attack before sending
					Node atk = attack.clone();
					// Empty attack before sending
					atk.empty(agent.g_exchanged());
					// Add generalizations to list of exchanges
					agent.addGExchanged(atk.toGeneralizations());
					// Send attack to the other agent
					toSend.add(new SendAttack(agent, agent.state(), m_nick, atk));
				}
			}
			// If belief has been defeated, create new belief
			if(my_argumentation_tree.root == null) {
				// Look for the old belief
				Node myBelief = null;
				for(Node n : past_nodes)
					if(n.attacks() == null)
						myBelief = n;
				// Replace the old belief
				assert myBelief != null;
				myBelief = myBelief.replace(accepted_arguments, context, MAX_FP, MMAX_FN, agent);
				// Check if the new belief is acceptable
				if(!myBelief.acceptable(context,MAX_FP,MMAX_FN)) {
					System.out.println("       > Problem: our belief is unacceptable in our context! Sending empty belief instead");
					int counter = 0;
					Float save = LPkg.ABUI_THRESHOLD;
					while(!myBelief.acceptable(context, MAX_FP, MMAX_FN)) {
						// Put an exit door
						if(LPkg.ABUI_THRESHOLD >= 0.9499) {
							System.out.println("       > Unable to create an acceptable intensional definition, sending an empty one...");
							myBelief = new Belief.Builder().from(m_nick).labelled(solutionToken).build();
							break;
						}
						else {
							LPkg.ABUI_THRESHOLD += (0.95 - LPkg.ABUI_THRESHOLD) / 2;
							myBelief = myBelief.replace(accepted_arguments, context, MAX_FP, MMAX_FN, agent);
							System.out.println("       > Trying to create an acceptable belief: "+counter+" (aa= "+LPkg.ABUI_THRESHOLD+")");
							counter ++;
						}
					}
					LPkg.ABUI_THRESHOLD = save;
				}
				// TEMP
				System.out.println(myBelief.classification(context));
				my_argumentation_tree.addRoot(myBelief);
				//Create a copy of belief
				Node belief = myBelief.clone();
				// Empty belief before sending
				belief.empty(agent.g_exchanged());
				// Add generalizations to list of exchanges
				agent.addGExchanged(belief.toGeneralizations());
				// Send belief to the other agent
				toSend.add(new SendBelief(agent, agent.state(), m_nick, belief));
			}
		}
		else
			System.out.println("     > Our argumentation tree has been agreed upon");
		// Testing the other's tree
		if(!other_argumentation_tree.agreed_upon) {
			System.out.println("     > Checking the other's argumentation tree");
			Set<Node> accepted_nodes = new HashSet<>();
			Set<Node> refused_nodes = new HashSet<>();
			Set<Node> destroy_buffer;
			do {
				destroy_buffer  = new HashSet<>();
				for(Node atk : other_argumentation_tree.getLeaves()) {
					if(!atk.getAgent().equals(m_nick)) {
						if (atk.acceptable(context, MAX_FP, OMAX_FN)) {
							accepted_nodes.add(atk);
							if(atk.attacks() != null)
								destroy_buffer.add(other_argumentation_tree.getNode(atk.attacks()));
						} else
							refused_nodes.add(atk);
					}
				}
				// Delete the nodes that have been defeated
				for(Node destroyed : destroy_buffer) {
					System.out.println("       > Destroying the defeted node "+destroyed.getid());
					other_argumentation_tree.deleteNode(destroyed.getid());
				}
			} while(!destroy_buffer.isEmpty());
			// Add accepted nodes to accepted arguments and notify other agent
			for (Node accepted : accepted_nodes) {
				System.out.println("       > Accepting "+accepted.getid());
				if(accepted.getid().equals(other_argumentation_tree.rootID())) {
					System.out.println("         > "+accepted.getid()+" is the belief of the other agent");
					other_argumentation_tree.agreed_upon = true;
					toSend.add(new AcceptBelief(agent.state(), accepted.getid()));
				}
				else {
					Set<Argument> arguments = accepted.toArguments();
					if (!arguments.isEmpty())
						accepted_arguments.addAll(arguments);
					toSend.add(new AcceptAttack(agent.state(), accepted.getid()));
				}
			}
			// Generate attacks for the leaves that have been refused
			for (Node refused : refused_nodes) {
				System.out.println("       > Refusing "+refused.getid()+" and creating counter attacks");
				for (Node attack : refused.attack(context, accepted_arguments, destroy_buffer, MAX_FP, OMAX_FN, agent)) {
					// TEMP
					System.out.println(attack.classification(context));
					other_argumentation_tree.addNode(attack);
					// Create a copy of the attack before sending
					Node atk = attack.clone();
					// Empty attack before sending
					atk.empty(agent.g_exchanged());
					// Add generalizations to list of exchanges
					agent.addGExchanged(atk.toGeneralizations());
					// Send attack to the other agent
					toSend.add(new SendAttack(agent, agent.state(), m_nick, atk));
				}
			}
		}
		else
			System.out.println("     > Other's argumentation tree has been agreed upon");
		if(my_argumentation_tree.agreed_upon && other_argumentation_tree.agreed_upon) {
			System.out.println("       > Finishing argumentation, displaying final scores...");
			System.out.println("         > Our final score    : "+my_argumentation_tree.root.classification(context));
			System.out.println("         > Other's final score: "+other_argumentation_tree.root.classification(context));
			solved = true;
			return ArgPhase.Stop;
		}
		return ArgPhase.ExIDefPhase;
	}
	
	// Get the intensional definition currently designed for our agent
	public Set<Generalization> getIdef(){
		if(exempted) {
			System.out.println("     > After being exempted of creating the new concept, returning the other's intensional definition...");
			return other_argumentation_tree.root.toGeneralizations();
		}
		System.out.println("     > Returning the intensional definition that we built...");
		return my_argumentation_tree.root.toGeneralizations();
	}
	
	// Set new signs and ids
	public void setSigns(String sign, Integer i) {
		if(i == 0) {
			con_newSign = sign;
		}
		if(i == 1) {
			hypo_newSign = sign;
		}
	}
	
	// Set new signs and ids
	public void setIDs(ConID id, Integer i) {
		if (i == 0) {
			o_con_id = id;
		}
		if (i == 1) {
			hypo_id = id;
		}
	}
	
	// Empty the argumentation mailbox
	private void cleanMailbox() {
		toSend = new ArrayList<>(); 
	}
	
}
