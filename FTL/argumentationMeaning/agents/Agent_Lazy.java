package agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import argumentation.Argumentation;
import argumentation.BoundaryFix;
import containers.ContrastSet;
import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.base.core.FeatureTerm;
import enumerators.Hierarchy;
import enumerators.Performative;
import enumerators.Relation;
import enumerators.State;
import evaluation.ExpFileManager;
import identifiers.ArgID;
import identifiers.ConID;
import identifiers.GenID;
import interfaces.Agent;
import interfaces.Container;
import interfaces.Message;
import interfaces.Node;
import interfaces.SemioticElement;
import messages.Assert;
import messages.Baptise;
import messages.CheckSelf;
import messages.CheckSize;
import messages.Discuss;
import messages.Evaluation;
import messages.Intransitive;
import messages.Name;
import messages.Ready;
import messages.Remove;
import messages.Replace;
import messages.Seize;
import messages.SendExamples;
import messages.Vote;
import null_objects.NullConID;
import null_objects.NullConcept;
import null_objects.NullContainer;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;
import tools.Mailbox;
import tools.RTriplet;
import tools.Token;
import tools.ToolSet;
import tools.Triplet;

/***
 *  The {@link Agent_Lazy} is the third version of the agent in the new implementation of the protocol.
 *  It works on all the cases, including when one agent cannot see its concept or the other agen'ts concept, unlike {@link Agent_Simple} and {@link Agent_Perfect}.
 * 	The {@link Relation} between concepts, instead of beeing explicitly named, is represented by a {@link Triplet} of ones and zeros which refers to the venn diagram representation of the {@link Relation}.
 *  The aim of {@link Agent_Lazy} is to argument over and resolve all kinds of disagreement arrising on one level of meaning.
 *  When we refer to "disagreement on one level of meaning", we mean on an inconsistant distribution of term symbols for one of the agents' features.
 *  The rest of the agents' features have to be consistent (i.e they should name the same things the same way).
 *  A consequence of this is the necessity to have a shared ontology on anything different than one set of symbols that will be discussed.
 * 
 * @author kemoadrian
 *
 */

public class Agent_Lazy implements Agent{
	
	// Communication
	public String nick;
	public Mailbox mail;
	public Boolean ready;
	public Example example;
	public State current_state;
	public Triplet<ConID, ConID, Relation> disagreement;
	
	// Collections of Triplets
	// Agreements
	public LinkedList<Triplet<ConID, ConID, Relation>> SelfDisagreements;
	public LinkedList<Triplet<ConID, ConID, Relation>> SemanticDisagreements;
	public LinkedList<Triplet<ConID, ConID, Relation>> UntransDisagreements;
	public LinkedList<Triplet<ConID, ConID, Relation>> LexicalDisagreements;
	// Relations and Hierarchies
	public List<Triplet<ConID, ConID, RTriplet>> mRTriplets;
	public List<Triplet<ConID, ConID, RTriplet>> oRTriplets;
	public List<Triplet<ConID, ConID, RTriplet>> ovRTriplets;
	public List<Triplet<ConID, ConID, Hierarchy>> Hierarchies;
	// Argumentations
	public Argumentation argumentation;
	public BoundaryFix boundaryFix;
	public boolean attempt_reshape;
	// Containers
	public ContrastSet Ki;
	public ContrastSet Kc;
	public ContrastSet Kf;
	public ContrastSet Hc;
	public ContrastSet Hf;
	// Concepts
	public List<ConID> m_checked;
	public List<ConID> o_checked;
	public List<Concept> m_transfered;
	public List<Concept> o_transfered;
	public List<Concept> m_new_concepts;
	public List<Concept> o_new_concepts;
	
	// AMAIL stuff
	public Set<Example> positiveExamples;
	public Set<Example> negativeExamples;
	
	// Exchanged elements
	public Map<GenID, Generalization> g_exchanged;
	public Set<Example> e_exchanged;
	
	// See Parent class Javadoc
	public String nick() {
		return this.nick;
	}

	// See Parent class Javadoc
	public void sendMessages(List<? extends Message> mail) {
		Token.sendMessages(mail);
	}

	// See Parent class Javadoc
	public void getMessages(List<? extends Message> mail) {
		this.mail.setMail(mail);
	}
	
	// See Parent class Javadoc
	public boolean initialize(List<FeatureTerm> data_set) {
		// Initializing instance variables
		this.Ki = learn(data_set);
		this.Kc = new ContrastSet(new HashSet<>(), new HashSet<>());
		this.Kf = new ContrastSet(new HashSet<>(), new HashSet<>());
		this.Hc = new ContrastSet(new HashSet<>(), new HashSet<>());
		this.Hf = new ContrastSet(new HashSet<>(), new HashSet<>());
		this.Kc.context.addAll(Ki.context);
		this.Kf.context.addAll(Ki.context);
		this.Hc.context.addAll(Ki.context);
		this.Hf.context.addAll(Ki.context);
		this.mRTriplets = new ArrayList<Triplet<ConID, ConID, RTriplet>>();
		this.oRTriplets = new ArrayList<Triplet<ConID, ConID, RTriplet>>();
		this.ovRTriplets = new ArrayList<Triplet<ConID, ConID, RTriplet>>();
		this.Hierarchies = new ArrayList<Triplet<ConID, ConID, Hierarchy>>();
		this.SelfDisagreements = new LinkedList<Triplet<ConID, ConID, Relation>>();
		this.SemanticDisagreements = new LinkedList<Triplet<ConID, ConID, Relation>>();
		this.UntransDisagreements = new LinkedList<Triplet<ConID, ConID, Relation>>();
		this.LexicalDisagreements = new LinkedList<Triplet<ConID, ConID, Relation>>();
		this.m_checked = new ArrayList<>();
		this.o_checked = new ArrayList<>();
		this.m_transfered = new ArrayList<>();
		this.o_transfered = new ArrayList<>();
		this.m_new_concepts = new ArrayList<>();
		this.o_new_concepts = new ArrayList<>();
		this.positiveExamples = new HashSet<>();
		this.negativeExamples = new HashSet<>();
		this.e_exchanged = new HashSet<>();
		this.g_exchanged = new HashMap<>();
		this.mail = new Mailbox();
		// Set the initial phase
		ready = false;
		current_state = State.Initial;
		disagreement = null;
		example = null;
		// Initialize the final contrast set
		for(Concept c : Ki.getAllConcepts()) {
			this.Kf.addConcept(c.clone());
		}
		return (Kc != null);
	}
	
	// See Parent class Javadoc
	public List<Message> getMail(){
		return mail.getMail();
	}
	
	// Name an example
	public List<String> name(Example e, Container K){
		List<String> output = new ArrayList<>();
		for(Concept c : K.getAllConcepts()){
			if(c.covers(e))
				output.add(c.sign());
		}
		return output;
	}
	
	// See Parent class Javadoc
	public ContrastSet learn(List<FeatureTerm> data_set) {
		ABUI.ABUI_VERSION = 2;
		ABUI learner = new ABUI();
		try {
			return learner.makeContrastSet(data_set);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// See implemented Agent interface Javadoc
	public State turn(){
		switch (current_state) {
		case BuildConState:
			this.current_state = buildConcept();
			break;
		case BuildEDState:
			this.current_state = buildExtensionalDefinition();
			break;
		case BuildIDState:
			this.current_state = buildIntensionalDefiniton();
			break;
		case BuildIdsState:
			this.current_state = buildIds();
			break;
		case BuildSignState:
			this.current_state = buildSign();
			break;
		case CheckEDState:
			this.current_state = checkExtension();
			break;
		case CheckIdsState:
			this.current_state = checkId();
			break;
		case CheckOtherInternalEqualitiesState:
			this.current_state = checkOtherInternalEqualites();
			break;
		case CheckSelfInternalEqualitiesState:
			this.current_state = checkSelfInternalEqualities();
			break;
		case ChooseDisagreementState:
			this.current_state = chooseDisagreement();
			break;
		case ExchangeRelatedState:
			this.current_state = exchangeRelatedConcepts();
			break;
		case EvaluateReadinessState:
			this.current_state = evaluateReadiness();
			break;
		case DeleteBlindState:
			this.current_state = deleteBlind();
			break;
		case DeleteConState:
			this.current_state = deleteUnachieved();
			break;
		case DraftEvaluationState:
			this.current_state = draftEvaluation();
			break;
		case ElectSignsState:
			this.current_state = electSign();
			break;
		case EvaluateSignsState:
			this.current_state = evaluateSigns();
			break;
		case FixBoundariesState:
			this.current_state = fixBoundaries();
			break;
		case Initial:
			this.current_state = State.WaitExample;
			break;
		case MakeEvaluationState:
			this.current_state = makeOverallEvaluation();
			break;
		case NameExample:
			this.current_state = name();
			break;
		case SendEvaluationState:
			this.current_state = sendnewEvaluation();
			break;
		case SendExternalEqualitiesState:
			this.current_state = sendExternalEqualities();
			break;
		case SendSelfInternalEqualitiesState:
			this.current_state = sendSelfInternalEqualities();
			break;
		case TransferConceptState:
			this.current_state = transferConcepts();
			break;
		case UpdateContrastSetState:
			this.current_state = updateContrastSet();
			break;
		case UpdateDisagreementsState:
			this.current_state = updateDisagreements();
			break;
		case UpdateEvaluationState:
			this.current_state = sendUpdatedEvaluation();
			break;
		case UpdateHypothesisState:
			this.current_state = updateHypothesis();
			break;
		case UpdateSignsState:
			this.current_state = updateSign();
			break;
		case UpgradeKnowledgeState:
			this.current_state = upgradeKnowledge();
			break;
		case ValidOtherInternalEqualitiesState:
			this.current_state = validOtherInternalEqualities();
			break;
		case VoteForSignsState:
			this.current_state = voteForSign();
			break;
		case WaitExample:
			this.current_state = waitForInput();
			break;
		default:
			this.current_state = waitForInput();
			break;
		
		}
		return State.WaitExample;
	}
	
	// Wait for an input
	public State waitForInput() {
		if(!mail.getMessages(Performative.Present).isEmpty()) {
			System.out.println("   > Received an example!");
			for(Message m : mail.getMessages(Performative.Present)) {
				mail.readMessage(m);
				example = mail.example;
			}
			mail.clearPerformative(Performative.Present);
			return State.NameExample;
		}
		System.out.println("   > Hasn't received any example, waiting...");
		return State.WaitExample;
	}
	
	// Name a received example
	public State name() {
		List<Message> toSend = new ArrayList<>();
		List<String> names = name(example, Kf);
		System.out.println("   > The example has been named with: "+names);
		// Ask for a self evaluation
		toSend.add(new CheckSelf(State.SendEvaluationState));
		toSend.add(new Name(State.EvaluateSignsState, names));
		sendMessages(toSend);
 		return State.EvaluateSignsState;
	}
	
	// Evaluate the signs used to name the received example
	public State evaluateSigns() {
		System.out.println("   > Evaluating the signs...");
		List<Message> toSend = new ArrayList<>();
		// Re-evaluate our signs
		List<String> m_signs =  name(example, Kf);
		List<String> o_signs = new ArrayList<>();
		// Get the other's signs
		for(Message m : mail.getMessages(Performative.Name)) {
			mail.readMessage(m);
			o_signs.addAll(mail.sign_list);
		}
		mail.clearPerformative(Performative.Name);
		System.out.println("     > We named the example       : "+m_signs);
		System.out.println("     > The other named the example: "+o_signs);
		// Check if there is one sign and if its the same as ours
		if(m_signs.size() == 1 && o_signs.size() == 1 && (m_signs.get(0).equals(o_signs.get(0)))) {
			System.out.println("       > The signs are the same, no problem here!");
			example = null;
			return State.WaitExample;
		}
		// Check if all the concepts have already been checked
		boolean m_all_checked = true;
		boolean o_all_checked = true;
		for(String s : m_signs) {
			if(!m_checked.contains(Kf.getConcept(s).id)) {
				m_all_checked = false;
				break;
			}	
		}
		for(String s : o_signs) {
			if(!o_checked.contains(Hf.getConcept(s).id)) {
				o_all_checked = false;
				break;
			}	
		}
		// If all the concepts have been checked, do nothing and wait for a new example
		if(m_all_checked && o_all_checked) {
			System.out.println("       > The signs are different, but their concepts are all checked, do nothing!");
			example = null;
			return State.WaitExample;
		}
		// If all my concepts have been checked, do nothing but start an argumentation
		else if(m_all_checked && !o_all_checked) {
			System.out.println("       > The signs are different, but my concepts are all checked, wait to receive other agent's concepts...");
			return State.ExchangeRelatedState;
		}
		// Otherwise, send the intensional definitions of our signs, and mark them to add to our current contrast set
		else {
			System.out.println("       > The signs are different, and my concepts haven't been sent, statring an argumentation...");
			for(String s : m_signs) {
				// Get the concept
				Concept c = Kf.getConcept(s);
				// Send the assert
				if(m_checked.contains(c.id))
					toSend.add(new Assert(State.ExchangeRelatedState, c.id));
				else
					toSend.add(new Assert(this, State.ExchangeRelatedState, c.id, c.sign(), c.intensional_definition));
				// Add to the concepts to add
				m_new_concepts.add(c);
				System.out.println("         > Asserting the concept "+c+" to the other agent...");
			}
		}
		// Send messages
		sendMessages(toSend);
		return State.ExchangeRelatedState;
	}
	
	// Pick a disagreement to argue about
	public State chooseDisagreement(){
		//Getting the received examples
		for (Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			Collection<Example> examples = mail.example_list;
			System.out.println("   > Adding " + examples.size() + " examples to contrast set ");
			Kc.addExamples(new HashSet<Example>(examples));
			Kf.addExamples(new HashSet<Example>(examples));
			System.out.println("   > Adding " + examples.size() + " examples to hypothesis");
			Hc.addExamples(new HashSet<Example>(examples));
			Hf.addExamples(new HashSet<Example>(examples));
			e_exchanged.addAll((new HashSet<>(examples)));
		}
		this.cleanMailbox(Performative.SendExamples);
		// Display contrast set + hypothesis
		System.out.println("Contrast-set : "+Kc.getAllConcepts());
		System.out.println("Hypothesis   : "+Hc.getAllConcepts());
		System.out.println("Hierarchies  : "+Hierarchies);
		// If the self and semantic disagreements are empty, time to look for untranslatable disagreements
		if(SelfDisagreements.isEmpty() && SemanticDisagreements.isEmpty())
			UntransDisagreements = new LinkedList<>(this.findUntranslatables());
		// Display disagreements
		System.out.println("   > Self disagreements : "+SelfDisagreements);
		System.out.println("   > Semantinc disagreements : "+SemanticDisagreements);
		System.out.println("   > Untranslatability disagreements : "+UntransDisagreements);
		System.out.println("   > Lexical disagreements : "+LexicalDisagreements);
		// Finding the disagreement to agree on
		Triplet<ConID, ConID, Relation> d = null;
		for (Message m : mail.getMessages(Performative.Debate)) {
			mail.readMessage(m);
			d = mail.relation;
		}
		// Clean Mailbox
		this.cleanMailbox(Performative.Debate);
		// If an argumentation has been proposed, load the corresponding disagreement
		if(d != null){
			System.out.println("   > Message received for a new discussion on "+getConcept(d.getLeft())+" vs "+getConcept(d.getMiddle())+" that have a relation of "+d.getRight());
			disagreement = getDisagreement(d.getMiddle(), d.getLeft(), d.getRight());
		}
		// Otherwise, look at what to argue about
		else if(!getAllDisagreements().isEmpty()) {
			// Prepare to send disagreement to address
			LinkedList<Message> toSend = new LinkedList<>();
			// Check which kind of disagreement to address
			if(!SelfDisagreements.isEmpty())
				disagreement = SelfDisagreements.getFirst();
			else if(!SemanticDisagreements.isEmpty())
				disagreement = SemanticDisagreements.getFirst();
			else if(!UntransDisagreements.isEmpty())
				disagreement = UntransDisagreements.getFirst();
			else if(!LexicalDisagreements.isEmpty())
				disagreement = LexicalDisagreements.getFirst();
			// Send disagreement
			toSend.add(new Discuss(State.ChooseDisagreementState, disagreement));
			sendMessages(toSend);
			System.out.println("   > Message sent for a new discussion on "+disagreement.getLeft()+" vs "+disagreement.getMiddle());
		}
		// If no disagreement remain to discuss, start to discuss the signs of the concepts
		else {
			return State.VoteForSignsState;
		}
		// Move to the new state according to the disagreement that has been chosen
		System.out.println("   > New Disagreement to discuss: "+disagreement.getLeft()+" vs "+disagreement.getMiddle()+": "+disagreement.getRight());
		// If the disagreement is an internal disagreement (self disagreement, go to fix boundaries)
		if(isSelfDisagreement(disagreement))
			return State.FixBoundariesState;
		// If the disagreement is on the partition, go to build the extensional definition of a new context
		else if(disagreement.getRight() == Relation.Inclusion || disagreement.getRight() == Relation.Overlap || disagreement.getRight() == Relation.Untrans)
			return State.BuildEDState;
		// If the disagreement is on blind concepts, go to delete the smallest concept
		else if(disagreement.getRight() == Relation.Blind)
			return State.DeleteBlindState;
		// If the disagreement is on the sign, go to change the signs of the adequate concepts
		else if(disagreement.getRight() == Relation.Equivalence || disagreement.getRight() == Relation.Disjunction)
			return State.UpdateSignsState;
		return State.WaitExample;
	}
	
	public State deleteBlind() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Check which concept has the more examples
		Triplet<ConID, ConID, RTriplet> evaluation = getOverallRTriplet(disagreement.getLeft(), disagreement.getMiddle());
		Concept remove = null;
		System.out.println("   > Removing a concept causing blindness...");
		// Check if has been asked to remove a concept already
		for(Message m : mail.getMessages(Performative.Remove)) {
			mail.readMessage(m);
			remove = getConcept(mail.con_id);
			System.out.println("     > Asked to delete the concept "+remove);
		}
		cleanMailbox(Performative.Remove);
		// If not, select a concept to remove (the one with the smallest number of proper examples)
		if(remove == null) {
			if(evaluation.getRight().getTriplet()[0] < evaluation.getRight().getTriplet()[2]) {
				remove = getConcept(evaluation.getLeft());
			}
			else {
				remove = getConcept(evaluation.getMiddle());
			}
			toSend.add(new Remove(State.DeleteBlindState, remove.id));
			System.out.println("     > Asking to remove the concept "+remove);
		}
		// Remove the concept to remove
		System.out.println("       > Deleting concept");
		removeConcept(remove);
		// Reset disagreement
		disagreement = null;
		// Send the messages
		System.out.println(toSend);
		sendMessages(toSend);
		return State.ChooseDisagreementState;
	}
	
	public State fixBoundaries() {
		// Prepare the list of concepts to add
		m_new_concepts = new ArrayList<>();
		o_new_concepts = new ArrayList<>();
		// If the boundary fix object is not initialized, do it
		if(boundaryFix == null)
			boundaryFix = new BoundaryFix(this);
		// Check if this agent should be in charge of the new concept's creation
		boundaryFix.exempted = !isMyBusiness();
		// Check if the id creation has been seized
		for(Message m : mail.getMessages(Performative.Seize)) {
			mail.readMessage(m);
			boundaryFix.id_seized = mail.bool_switch;
		}
		cleanMailbox(Performative.Seize);
		// Update the new ids
		for(Message m : mail.getMessages(Performative.Baptise)) {
			mail.readMessage(m);
			if(mail.integer == 0)
				boundaryFix.first_id = mail.con_id;
			if(mail.integer == 1)
				boundaryFix.second_id = mail.con_id;
		}
		cleanMailbox(Performative.Baptise);
		// Read mail
		System.out.println("   > Checking nodes to delete (accepted attacks)...");
		for (Message m : mail.getMessages(Performative.AcceptAttack)) {
			mail.readMessage(m);
			ArgID to_delete_id = mail.arg_id;
			if (boundaryFix.argumentation.my_argumentation_tree.contains(to_delete_id)) {
				Node to_delete_node = boundaryFix.argumentation.my_argumentation_tree.getAttackNode(to_delete_id);
				Node to_delete_attacked_node = boundaryFix.argumentation.my_argumentation_tree
						.getNode(to_delete_node.attacks());
				boundaryFix.argumentation.my_argumentation_tree.deleteNode(to_delete_id);
				boundaryFix.argumentation.my_argumentation_tree.deleteNode(to_delete_attacked_node.getid());
				System.out.println("     > Deleting the node " + to_delete_id + " and the node that it attacked: "
						+ to_delete_attacked_node.getid());
			} else if (boundaryFix.argumentation.other_argumentation_tree.contains(to_delete_id)) {
				Node to_delete_node = boundaryFix.argumentation.other_argumentation_tree.getAttackNode(to_delete_id);
				Node to_delete_attacked_node = boundaryFix.argumentation.other_argumentation_tree
						.getNode(to_delete_node.attacks());
				boundaryFix.argumentation.other_argumentation_tree.deleteNode(to_delete_id);
				boundaryFix.argumentation.other_argumentation_tree.deleteNode(to_delete_attacked_node.getid());
				System.out.println("     > Deleting the node " + to_delete_id + " and the node that it attacked: "
						+ to_delete_attacked_node.getid());
			} else {
				System.out.println("     > Problem: did not find the attacked node");
			}
		}
		cleanMailbox(Performative.AcceptAttack);
		System.out.println("   > Checking attacks to add...");
		for (Message m : mail.getMessages(Performative.Attack)) {
			mail.readMessage(m);
			Node to_add = mail.node;
			boolean added_to_mine = boundaryFix.argumentation.my_argumentation_tree.addNode(to_add);
			boolean added_to_other = boundaryFix.argumentation.other_argumentation_tree.addNode(to_add);
			if (added_to_mine)
				System.out.println("     > Adding " + to_add.getid() + " to own tree");
			if (added_to_other)
				System.out.println("     > Adding " + to_add.getid() + " to other's tree");
			// If there are examples to add, add them
			if (!to_add.toExamples().isEmpty()) {
				Set<Example> updated_context = boundaryFix.argumentation.context.get(to_add.getLabel());
				updated_context.addAll(to_add.toExamples());
				updated_context = new HashSet<>(ToolSet.cleanDuplicates(updated_context));
				boundaryFix.argumentation.context.put(to_add.getLabel(), updated_context);
				Kc.addExamples(to_add.toExamples());
				Kf.addExamples(to_add.toExamples());
				Hc.addExamples(to_add.toExamples());
				Hf.addExamples(to_add.toExamples());
				addEExchanged(to_add.toExamples());
			}
		}
		cleanMailbox(Performative.Attack);
		System.out.println("   > Checking believes that have been accepted...");
		for (Message m : mail.getMessages(Performative.AcceptBelief)) {
			mail.readMessage(m);
			ArgID to_accept = mail.arg_id;
			if (boundaryFix.argumentation.my_argumentation_tree.root.getid().equals(to_accept)) {
				System.out.println("     > Our belief has been accepted!");
				boundaryFix.argumentation.my_argumentation_tree.agreed_upon = true;
			} else
				System.out.println("     > Problem: unknown belief has been agreed upon");
		}
		cleanMailbox(Performative.AcceptBelief);
		System.out.println("   > Checking believes to add...");
		for (Message m : mail.getMessages(Performative.Belief)) {
			mail.readMessage(m);
			Node new_belief = mail.belief;
			System.out.println("     > Adding " + new_belief.getid() + " as the new belief of the other agent");
			boundaryFix.argumentation.other_argumentation_tree.addRoot(new_belief);
		}
		cleanMailbox(Performative.Belief);
		// Try to fix the first border
		if(boundaryFix.solve()) {
			// Create new concept
			Concept Ci = (disagreement.getLeft().id < disagreement.getMiddle().id)? getConcept(disagreement.getLeft()) : getConcept(disagreement.getMiddle());
			Concept Cj = (disagreement.getLeft().id > disagreement.getMiddle().id)? getConcept(disagreement.getLeft()) : getConcept(disagreement.getMiddle());
			if(isSelfSelfDisagreement(disagreement)) {
				if(!boundaryFix.first_ID.isEmpty()) {
					m_new_concepts.add(new Concept(boundaryFix.first_id, Ci.sign, boundaryFix.first_ID, adjunctSet(boundaryFix.first_ID, Kc.context)));
				}
				if(!boundaryFix.second_ID.isEmpty()) {
					m_new_concepts.add(new Concept(boundaryFix.second_id, Cj.sign, boundaryFix.second_ID, adjunctSet(boundaryFix.second_ID, Kc.context)));
				}
			}
			else if(isOtherSelfDisagreement(disagreement)) {
				if(!boundaryFix.first_ID.isEmpty()) {
					o_new_concepts.add(new Concept(boundaryFix.first_id, Ci.sign, boundaryFix.first_ID, adjunctSet(boundaryFix.first_ID, Kc.context)));
				}
				if(!boundaryFix.second_ID.isEmpty()) {
					o_new_concepts.add(new Concept(boundaryFix.second_id, Cj.sign, boundaryFix.second_ID, adjunctSet(boundaryFix.second_ID, Kc.context)));
				}
			}
			removeConcept(Ci);
			removeConcept(Cj);
			// Reinitialize the boundary fix object
			boundaryFix = null;
			// Change Phase
			return State.ExchangeRelatedState;
		}
		return State.FixBoundariesState;
	}
	
	public State buildExtensionalDefinition(){
		// Create a list of messages to send
		List<Message> toSend = new ArrayList<>();
		// Get the two involved concepts
		System.out.println("   > Creating the sets of positive and negative examples");
		Concept c1 = getConcept(disagreement.getLeft());
		Concept c2 = getConcept(disagreement.getMiddle());
		// If we are in an untranslatable disagreement, one of the two concepts will be null
		if(disagreement.getRight() == Relation.Untrans) {
			positiveExamples = new HashSet<Example>(ToolSet.union(c1.extensional_definition, c2.extensional_definition));
			negativeExamples = new HashSet<Example>(ToolSet.substract(Kc.context, positiveExamples));
		}
		// In the case of an inclusion, the new concept will be the part of the hypernym that is not the hyponym
		if(disagreement.getRight() == Relation.Inclusion){
			Concept hypernym = getHypernym(c1.id, c2.id);
			Concept hyponym = getHyponym(c1.id, c2.id);
			System.out.println(hypernym);
			System.out.println(hyponym);
			positiveExamples = new HashSet<Example>(ToolSet.substract(hypernym.extensional_definition, hyponym.extensional_definition));
			negativeExamples = new HashSet<Example>(ToolSet.substract(Kc.context, positiveExamples));
		}
		// In the case of an overlap, the new concept will be the part that is covered by both concepts
		if(disagreement.getRight() == Relation.Overlap){
			positiveExamples = new HashSet<Example>(ToolSet.intersection(c1.extensional_definition, c2.extensional_definition));
			negativeExamples = new HashSet<Example>(ToolSet.substract(Kc.context, positiveExamples));
		}
		// In the case of
		System.out.println("   > number of positive examples: "+positiveExamples.size());
		System.out.println("   > number of negative examples: "+negativeExamples.size());
		// Give to the other agent the size of our extensional definition to see if at least one is over the threshold
		toSend.add(new CheckSize(State.CheckEDState, positiveExamples.size()));
		sendMessages(toSend);
		// Move to the next phase
		return State.CheckEDState;
	}
	
	public State checkExtension() {
		// Create a list of messages to send
		List<Message> toSend = new ArrayList<>();
		// Get the two extensional definitions sizes
		System.out.println("   > Checking the size of the predicted extensional definitions");
		Integer m_size = positiveExamples.size();
		Integer o_size = -1;
		for(Message m : mail.getMessages(Performative.ExtSize)) {
			mail.readMessage(m);
			o_size = mail.integer;
		}
		cleanMailbox(Performative.ExtSize);
		// Check if we received the ext size of the other agent
		if(o_size < 0)
			System.out.println("    > Problem: we didn't receive the ext size of the other agent");
		// Initialize the argumentation if it has not been done yet
		argumentation = new Argumentation(this);
		argumentation.setUpExamples(positiveExamples, negativeExamples);
		// Check if the decision of who creates the new concept has been seized
		Boolean seized = false;
		System.out.println("   > Check if the argumentation has been seized...");
		for(Message m : mail.getMessages(Performative.Seize)) {
			mail.readMessage(m);
			seized = true;
			argumentation.exempted = mail.bool_switch;
			System.out.println("     > the argumentation has been seized: this agent creates the new concept is "+!argumentation.exempted);
		}
		cleanMailbox(Performative.Seize);
		// Dispense of creating the new concept the agent that does not have to
		if(!seized) {
			argumentation.exempted = !(isMyBusiness() && positiveExamples.size() >= ToolSet.THRESHOLD);
			System.out.println("     > seizing the argumentation: this agent creates the new concept is "+!argumentation.exempted);
			toSend.add(new Seize(State.CheckEDState, !argumentation.exempted));
		}
		// If not, green light to argue but first, set max FP size and max FN size
		System.out.println("   > Setting max false positives and negatives...");
		argumentation.MAX_FP = (disagreement.getRight() == Relation.Untrans)? 0 : (int) Math.floor((ToolSet.THRESHOLD - 1) / 4);
		argumentation.MMAX_FN = (disagreement.getRight() == Relation.Untrans)? 0 : Math.min(m_size - ToolSet.THRESHOLD, argumentation.MAX_FP);
		argumentation.OMAX_FN = (disagreement.getRight() == Relation.Untrans)? 0 : Math.min(o_size - ToolSet.THRESHOLD, argumentation.MAX_FP);
		System.out.println("     > Max false positives : "+argumentation.MAX_FP);
		System.out.println("     > Max false negatives for my Idef : "+argumentation.MMAX_FN);
		System.out.println("     > Max false negatives for other's Idef : "+argumentation.OMAX_FN);
		// Send messages
		sendMessages(toSend);
		return State.BuildIDState;
	}
	
	public State buildIntensionalDefiniton(){
		// Read mail
		System.out.println("   > Checking nodes to delete...");
		for(Message m : mail.getMessages(Performative.AcceptAttack)) {
			mail.readMessage(m);
			ArgID to_delete_id = mail.arg_id;
			if (argumentation.my_argumentation_tree.contains(to_delete_id)) {
				Node to_delete_node = argumentation.my_argumentation_tree.getAttackNode(to_delete_id);
				Node to_delete_attacked_node = argumentation.my_argumentation_tree.getNode(to_delete_node.attacks());
				argumentation.my_argumentation_tree.deleteNode(to_delete_id);
				argumentation.my_argumentation_tree.deleteNode(to_delete_attacked_node.getid());
				System.out.println("     > Deleting the node " + to_delete_id + " and the node that it attacked: "
						+ to_delete_attacked_node.getid());
			} else if (argumentation.other_argumentation_tree.contains(to_delete_id)) {
				Node to_delete_node = argumentation.other_argumentation_tree.getAttackNode(to_delete_id);
				Node to_delete_attacked_node = argumentation.other_argumentation_tree.getNode(to_delete_node.attacks());
				argumentation.other_argumentation_tree.deleteNode(to_delete_id);
				argumentation.other_argumentation_tree.deleteNode(to_delete_attacked_node.getid());
				System.out.println("     > Deleting the node " + to_delete_id + " and the node that it attacked: "
						+ to_delete_attacked_node.getid());
			} else {
				System.out.println("     > Problem: did not find the attacked node");
			}
		}
		cleanMailbox(Performative.AcceptAttack);
		System.out.println("   > Checking attacks to add...");
		for(Message m : mail.getMessages(Performative.Attack)) {
			mail.readMessage(m);
			Node to_add = mail.node;
			boolean added_to_mine = argumentation.my_argumentation_tree.addNode(to_add);
			boolean added_to_other = argumentation.other_argumentation_tree.addNode(to_add);
			if (added_to_mine)
				System.out.println("     > Adding " + to_add.getid() + " to own tree");
			if (added_to_other)
				System.out.println("     > Adding " + to_add.getid() + " to other's tree");
			// If there are examples to add, add them
			if (!to_add.toExamples().isEmpty()) {
				System.out.println("     > Adding examples...");
				Set<Example> updated_context = argumentation.context.get(to_add.getLabel());
				updated_context.addAll(to_add.toExamples());
				updated_context = new HashSet<>(ToolSet.cleanDuplicates(updated_context));
				argumentation.context.put(to_add.getLabel(), updated_context);
				Kc.addExamples(to_add.toExamples());
				Kf.addExamples(to_add.toExamples());
				Hc.addExamples(to_add.toExamples());
				Hf.addExamples(to_add.toExamples());
				addEExchanged(to_add.toExamples());
			}
		}
		cleanMailbox(Performative.Attack);
		System.out.println("   > Checking believes that have been accepted...");
		for(Message m : mail.getMessages(Performative.AcceptBelief)) {
			mail.readMessage(m);
			ArgID to_accept = mail.arg_id;
			if (argumentation.my_argumentation_tree.root.getid().equals(to_accept)) {
				System.out.println("     > Our belief has been accepted!");
				argumentation.my_argumentation_tree.agreed_upon = true;
			} else
				System.out.println("     > Problem: unknown belief has been agreed upon");
		}
		cleanMailbox(Performative.AcceptBelief);
		System.out.println("   > Checking believes to add...");
		for(Message m : mail.getMessages(Performative.Belief)) {
			mail.readMessage(m);
			Node new_belief = mail.belief;
			System.out.println("     > Adding " + new_belief.getid() + " as the new belief of the other agent");
			argumentation.other_argumentation_tree.addRoot(new_belief);
		}
		cleanMailbox(Performative.Belief);
		System.out.println("my tree:");
		System.out.println(argumentation.my_argumentation_tree.toString());
		System.out.println("the other's tree:");
		System.out.println(argumentation.other_argumentation_tree.toString());
		if(!argumentation.solve()) {
			System.out.println("my tree:");
			System.out.println(argumentation.my_argumentation_tree.toString());
			System.out.println("the other's tree:");
			System.out.println(argumentation.other_argumentation_tree.toString());
			return State.BuildIDState;
		}
		return State.BuildSignState;
	}
	
	public State buildSign() {
		// Eventual list of signs to send
		List<Message> toSend = new LinkedList<>();
		// Check if the other created the signs:
		boolean seized = false;
		System.out.println("   > Checking if the sign creation has been seized...");
		for (Message m : mail.getMessages(Performative.Seize)) {
			mail.readMessage(m);
			seized = mail.bool_switch;
			System.out.println("     > The sign creation has been seized!");
		}
		cleanMailbox(Performative.Seize);
		if (seized) {
			System.out.println("   > Receiving the new signs...");
			for (Message m : mail.getMessages(Performative.Baptise)) {
				mail.readMessage(m);
				String nature = (mail.integer == 0)? "concept" : "co-hyponym";
				argumentation.setSigns(mail.sign, mail.integer);
				System.out.println("     > The sign of the new "+nature+" will be "+mail.sign);
			}
		} else {
			System.out.println("   > Creating the new signs...");
			String new_sign = "custom:" + Sign.getNewSymbol();
			toSend.add(new Baptise(State.BuildSignState, new_sign, 0));
			argumentation.con_newSign = new_sign;
			System.out.println("     > The sign of the new concept will be "+new_sign);
			if(disagreement.getRight() == Relation.Inclusion) {
				String new_sign_2 = "custom:" + Sign.getNewSymbol();
				toSend.add(new Baptise(State.BuildSignState, new_sign_2, 1));
				argumentation.hypo_newSign = new_sign_2;
				System.out.println("     > The sign of the new co-hyponym will be "+new_sign_2);
			}
			toSend.add(new Seize(State.BuildSignState));
		}
		cleanMailbox(Performative.Baptise);
		sendMessages(toSend);
		return State.BuildIdsState;
	}
	
	public State buildIds() {
		// Eventual list of signs to send
		List<Message> toSend = new LinkedList<>();
		// Check if the other created the signs:
		boolean seized = false;
		System.out.println("   > Checking if the id creation has been seized...");
		for (Message m : mail.getMessages(Performative.Seize)) {
			mail.readMessage(m);
			seized = mail.bool_switch;
			System.out.println("     > The id of the co_hyponym creation has been seized!");
		}
		cleanMailbox(Performative.Seize);
		System.out.println("   > Creating the new ids...");
		ConID new_id = new ConID();
		toSend.add(new Baptise(State.CheckIdsState, new_id, 0));
		argumentation.m_con_id = new_id;
		System.out.println("     > The id of my new concept will be "+new_id);
		if(disagreement.getRight() == Relation.Inclusion && !seized) {
			ConID new_id2 = new ConID();
			toSend.add(new Baptise(State.CheckIdsState, new_id2, 1));
			argumentation.hypo_id = new_id2;
			System.out.println("     > The id of the new co-hyponym will be "+new_id2);
			toSend.add(new Seize(State.BuildIdsState));
		}
		sendMessages(toSend);
		return State.CheckIdsState;
	}
	
	public State checkId() {
		System.out.println("   > Checking if we received new ids...");
		for (Message m : mail.getMessages(Performative.Baptise)) {
			mail.readMessage(m);
			String nature = (mail.integer == 0)? "concept" : "co-hyponym";
			argumentation.setIDs(mail.con_id, mail.integer);
			System.out.println("     > The id of the new "+nature+" will be "+mail.con_id);
		}
		cleanMailbox(Performative.Baptise);
		if (argumentation.getIdef().isEmpty())
			return State.DeleteConState;
		return State.BuildConState;
	}
	
	public State buildConcept(){
		// Reset list of new concepts
		m_new_concepts = new ArrayList<>();
		o_new_concepts = new ArrayList<>();
		// Get the semiotic elements of the new concept
		Sign sign = new Sign(argumentation.con_newSign.toString());
		ConID m_id = argumentation.m_con_id;
		ConID o_id = argumentation.o_con_id; 
		Set<Generalization> m_idef = argumentation.getIdef();
		Set<Generalization> o_idef = argumentation.getIdef();
		Set<Example> m_edef = adjunctSet(m_idef,Kc.getContext());
		Set<Example> o_edef = adjunctSet(o_idef,Kc.getContext());
		// Create new concepts
		Concept m_new_concept = new Concept(m_id, sign, m_idef, m_edef);
		Concept o_new_concept = new Concept(o_id, sign, o_idef, o_edef);
		System.out.println("   > Creating the new concept "+m_new_concept+" with "+m_idef.size()+" generalizations and "+m_edef.size()+" examples");
		System.out.println("   > Creating the new concept "+o_new_concept+" with "+o_idef.size()+" generalizations and "+o_edef.size()+" examples");
		// If the disagreement was an inclusion, create a new concept for the co-hyponym
		if(disagreement.getRight() == Relation.Inclusion) {
			System.out.println("   > The disagreement is a hypo/hypernymy, creating co-hyponym...");
			// Get the co-hyponym
			Concept hypernym = getHypernym(disagreement.getLeft(), disagreement.getMiddle());
			Concept hyponym = getHyponym(disagreement.getLeft(), disagreement.getMiddle());
			System.out.println("     > The hyponym was "+hyponym);
			// Create the concept
			Concept h_new_concept = new Concept(argumentation.hypo_id, new Sign(argumentation.hypo_newSign), new HashSet<>(hyponym.intensional_definition), new HashSet<>(hyponym.extensional_definition));
			System.out.println("     > Creating the new co-hyponym "+h_new_concept);
			// Add it to the same container as the hypernym
			if(getContainer(hyponym.id) == Kc) {
				o_new_concepts.add(o_new_concept);
				o_new_concepts.add(h_new_concept); 
			}
			if(getContainer(hyponym.id) == Hc) {
				m_new_concepts.add(m_new_concept);
				m_new_concepts.add(h_new_concept);
			}
			// Recall that the co-hyponym sign's should be changed
			//replaceConcept(hyponym, argumentation.hypo_newSign);
			// Remove hypernym
			removeConcept(hypernym);
		}
		// Add the concepts
		else if (disagreement.getRight() == Relation.Overlap){
			m_new_concepts.add(m_new_concept);
			o_new_concepts.add(o_new_concept);
		}
		else if (disagreement.getRight() == Relation.Untrans) {
			Concept Ci = getConcept(disagreement.getLeft());
			Concept Cj = getConcept(disagreement.getMiddle());
			if(!Ci.isNull()) {
				if(getContainer(Ci.id) == Kc)
					o_new_concepts.add(o_new_concept);
				if(getContainer(Ci.id) == Hc)
					m_new_concepts.add(m_new_concept);
			}
			if(!Cj.isNull()) {
				if(getContainer(Cj.id) == Kc)
					o_new_concepts.add(o_new_concept);
				if(getContainer(Cj.id) == Hc)
					m_new_concepts.add(m_new_concept);
			}
		}
		return State.ExchangeRelatedState;
	}
	
	public State deleteUnachieved() {
		// Reset list of new concepts
		m_new_concepts = new ArrayList<>();
		o_new_concepts = new ArrayList<>();
		System.out.println("   > Argumentation failed, checking the disagreements to delete...");
		// Look for the concepts to delete
		if(disagreement.getRight() == Relation.Overlap) {
			removeConcept(getConcept(disagreement.getLeft()));
			removeConcept(getConcept(disagreement.getMiddle()));
			System.out.println("     > Disagreement was an overlap, deleting the two involved concepts");
		}
		if(disagreement.getRight() == Relation.Inclusion) {
			Concept hyponym = getHyponym(disagreement.getLeft(), disagreement.getMiddle());
			Concept hypernym = getHypernym(disagreement.getLeft(), disagreement.getMiddle());
			Concept h_new_concept = new Concept(argumentation.hypo_id, new Sign(argumentation.hypo_newSign), new HashSet<>(hyponym.intensional_definition), new HashSet<>(hyponym.extensional_definition));
			System.out.println("       > Creating the new concept "+h_new_concept);
			// Add it to the same container as the hypernym
			if (getContainer(hyponym.id) == Kc) {
				o_new_concepts.add(h_new_concept);
				System.out.println("       > the hyponym was ours, add the hyponym to the other contrast set");
			}
			if (getContainer(hyponym.id) == Hc) {
				m_new_concepts.add(h_new_concept);
				System.out.println("       > the hyponym was the other's, add the hyponym to our contrast set");
			}
			// Remove Hypernym
			removeConcept(hypernym);
			System.out.println("     > Disagreement was an inclusion, deleting hypernym and adding hyponym");
		}
		if(disagreement.getRight() == Relation.Untrans) {
			removeConcept(getConcept(disagreement.getLeft()));
			removeConcept(getConcept(disagreement.getMiddle()));
			System.out.println("     > Disagreement was untranslability, deleting the untranslatable concept");
		}
		return State.ExchangeRelatedState;
	}
	
	// Add the asserts to the hypothesis, then ask for the assert of concepts that might be in disagreement
	public State exchangeRelatedConcepts() {
		System.out.println("   > Creating the argumentation contrast set...");
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Concepts to add
		List<Concept> received = new ArrayList<>();
		// Put received concepts in the list to add
		for (Message m : mail.getMessages(Performative.Assert)) {
			mail.readMessage(m);
			ConID id = mail.con_id;
			Concept c = new NullConcept();
			if(o_checked.contains(id)) {
				c = Hf.getConcept(id);
			}
			else {
				Sign sign = new Sign(mail.sign);
				Set<Generalization> ID = new HashSet<>(mail.generalization_list);
				Set<Example> ED = adjunctSet(ID, Kc.context);
				c = new Concept(id, sign, ID, ED);
			}
			received.add(c);
		}
		mail.clearPerformative(Performative.Assert);
		// Add the received concepts
		o_new_concepts.addAll(received);
		// Check for new concepts to add to the argumentation
		List<Concept> to_add = new ArrayList<>();
		System.out.println("     > The other agent involved the following concepts in the argumentation: "+received);
		if(!received.isEmpty()) {
			System.out.println("     > Checking for concepts to involve in the argumentation...");
			for (Concept c1 : received) {
				for (Concept c2 : Kf.getAllConcepts()) {
					if (agree(c1, c2) != Relation.Disjunction && !(m_new_concepts.contains(c2) || to_add.contains(c2))) {
						System.out.println("       > The concepts "+c1+" and "+c2+" are not disjoint, we should involve "+c2+" in the argumentation!");
						// Add to concepts to add
						to_add.add(c2);
					}
					if(agree(c1, c2) == Relation.Disjunction && c1.sign().equals(c2.sign()) && !(m_new_concepts.contains(c2) || to_add.contains(c2))) {
						System.out.println("       > The concepts "+c1+" and "+c2+" are disjoint and share the same sign, we should involve "+c2+" in the argumentation!");
						// Add to concepts to add
						to_add.add(c2);
					}
				}
			}
			System.out.println("   > For now, our concepts involved in the argumentation are        : "+m_new_concepts);
			System.out.println("   > For now, the other's concepts involved in the argumentation are: "+o_new_concepts);
		}
		// Reset the readiness lock
		ready = false;
		// If no concepts to add,
		if(!to_add.isEmpty()) {
			m_new_concepts.addAll(to_add);
			for(Concept c : to_add) {
				// Send it to the other agent
				if(m_checked.contains(c.id))
					toSend.add(new Assert( State.ExchangeRelatedState, c.id));
				else
					toSend.add(new Assert(this, State.ExchangeRelatedState, c.id, c.sign(), c.intensional_definition));
			}
		}
		else {
			System.out.println("     > No more concepts to invlove...");
			// Tell other agent
			ready = true;
			toSend.add(new Ready());
		}
		// Send messages
		sendMessages(toSend);
		return State.EvaluateReadinessState;
	}
	
	// Check if both agents are ready to continue the argumentation
	public State evaluateReadiness() {
		System.out.println("   > Check who is ready to continue the argumentation...");
		boolean m_ready = ready;
		boolean o_ready = false;
		// If both agents are ready, continue the argumentation
		for(Message m : mail.getMessages(Performative.ArgumentationReady)) {
			mail.readMessage(m);
			o_ready = mail.bool_switch;
		}
		mail.clearPerformative(Performative.ArgumentationReady);
		if(o_ready && m_ready) {
			System.out.println("     > The agents are ready, continue the argumentation!");
			return State.TransferConceptState;
		}
		System.out.println("     > One agent is not ready, wait for more concepts to add...");
		return State.ExchangeRelatedState;
	}
	
	public State transferConcepts() {
		// Put the transfered concepts in the set of transfered concepts
		for(Concept c : m_new_concepts)
			if(Kf.contains(c))
				m_transfered.add(c);
		for(Concept c : o_new_concepts)
			if(Hf.contains(c))
				o_transfered.add(c);
		// Remove the concepts that we will argue on from the final contrast set
		for(Concept c : m_new_concepts)
			if(!Kf.removeConcept(c).isNull())
				System.out.println("     > The concept "+c+" was found in the final contrast set and therefore has been removed...");
		// Remove the concepts that we will argue on from the final hypothesis
		for(Concept c : o_new_concepts)
			if(!Hf.removeConcept(c).isNull())
				System.out.println("     > The concept "+c+" was found in the final hypothesis and therefore has been removed...");
		return State.SendEvaluationState;
	}
	
	public State sendnewEvaluation() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Check if the self evaluations should be made
		Boolean selfcheck = false;
		// Read the messages
		for(Message m : mail.getMessages(Performative.CheckSelf)) {
			mail.readMessage(m);
			selfcheck = mail.bool_switch;
		}
		cleanMailbox(Performative.CheckSelf);
		// Make new evaluations (my new concepts vs other's concepts)
		System.out.println("   > Checking our new concepts against other agent's concepts...");
		for(Concept Ci : m_new_concepts) {
			for(Concept Cj : Hc.getAllConcepts()) {
				RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
				toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, rt)));
				System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
			}
		}
		// Make new evaluations (other's new concepts vs my concepts)
		System.out.println("   > Checking our concepts against other agent's new concepts...");
		for(Concept Ci : Kc.getAllConcepts()) {
			for(Concept Cj : o_new_concepts) {
				RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
				toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, rt)));
				System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
			}
		}
		// Make new evaluations (my new concepts vs other's new concepts)
		System.out.println("   > Checking our new concepts against other agent's new concepts...");
		for(Concept Ci : m_new_concepts) {
			for(Concept Cj : o_new_concepts) {
				RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
				toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, rt)));
				System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
			}
		}
		// Check if we should check self relations
		if(selfcheck) {
			// Make new evaluations (my new concepts vs themselves)
			System.out.println("   > Checking our new concepts against themselves..");
			for(int i=0; i<m_new_concepts.size(); i++) {
				for(int j=0; j<i+1; j++) {
					Concept Ci = m_new_concepts.get(i);
					Concept Cj = m_new_concepts.get(j);
					RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
					toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, rt)));
					System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
				}
			}
			// Make new evaluations (other's new concepts vs themselves)
			System.out.println("   > Checking other agent's new concepts against themselves..");
			for(int i=0; i<o_new_concepts.size(); i++) {
				for(int j=0; j<i+1; j++) {
					Concept Ci = o_new_concepts.get(i);
					Concept Cj = o_new_concepts.get(j);
					RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
					toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, rt)));
					System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
				}
			}
		}
		// Add concepts
		for(Concept c : m_new_concepts) {
			Kc.addConcept(c);
			System.out.println("   > Adding concept "+c+" to contrast set...");
		}
		for(Concept c : o_new_concepts) {
			Hc.addConcept(c);
			System.out.println("   > Adding concept "+c+" to hypothesis...");
		}
		// Send new evaluations
		sendMessages(toSend);
		return State.DraftEvaluationState;
	}
	
	// First tentative to build the overall r-triplet
	public State draftEvaluation() {
		// List of messages to send
		List<Message> toSend = new ArrayList<>();
		// Has the evaluation been seized?
		boolean seized = false;
		System.out.println("   > Checking if the evaluation has been seized...");
		for(Message m : mail.getMessages(Performative.Seize)) {
			mail.readMessage(m);
			seized = mail.bool_switch;
			if(seized)
				System.out.println("     > Other agent in charge of the evaluation!");
		}
		cleanMailbox(Performative.Seize);
		// For each draft of the overall r-triplet, 
		for(Message m : mail.getMessages(Performative.Evaluation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, RTriplet> T = mail.rtriplet;
			// Get the concepts
			Concept Ci = getConcept(T.getLeft());
			Concept Cj = getConcept(T.getMiddle());
			System.out.println("   > Checking the evaluation between "+Ci+" and "+Cj+"...");
			// Get the RTriplets
			RTriplet m_rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
			System.out.println("     > Our evaluation    : "+m_rt);
			RTriplet o_rt = T.getRight();
			System.out.println("     > Other's evaluation: "+o_rt);
			RTriplet overall_rt = m_rt.combine(o_rt);
			System.out.println("     > Overall evaluation: "+overall_rt);
			// get the triplets
			int[] m_evaluation = m_rt.getTriplet();
			int[] o_evaluation = o_rt.getTriplet(Ci.id, Cj.id);
			int[] overall_evaluation = overall_rt.getTriplet();
			// For each integer of the r-triplet,
			for(int i=0; i<3; i++) {
				// Check if examples need to be exchanged, if this agent is the one that has less examples or should seize the evaluation
				if((overall_evaluation[i] < 0) && (m_evaluation[i] < o_evaluation[i] || (m_evaluation[i] == o_evaluation[i] && !seized))){
					// If yes, send them
					Collection<Example> candidates = requestExamples(Ci.id, Cj.id, i);
					Collection<Example> selected = ToolSet.substract(candidates, e_exchanged);
					e_exchanged.addAll(selected);
					toSend.add(new SendExamples(this, State.UpdateEvaluationState, selected));
					// Check if the agents have the same number of examples and hasn't been seized
					if(m_evaluation[i] == o_evaluation[i] && !seized) {
						System.out.println("       > We seize the evaluation");
						toSend.add(new Seize(State.DraftEvaluationState));
					}
				}
			}
			toSend.add(new Evaluation(State.UpdateEvaluationState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, overall_rt)));
		}
		cleanMailbox(Performative.Evaluation);
		sendMessages(toSend);
		return State.UpdateEvaluationState;
	}
	
	public State sendUpdatedEvaluation() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Add examples
		for(Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			Kc.addExamples(mail.example_list);
			Kf.addExamples(mail.example_list);
			Hc.addExamples(mail.example_list);
			Hf.addExamples(mail.example_list);
			e_exchanged.addAll(mail.example_list);
		}
		cleanMailbox(Performative.SendExamples);
		// Send the updated local evaluations
		for(Message m : mail.getMessages(Performative.Evaluation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, RTriplet> T = mail.rtriplet;
			// Get the concepts
			Concept Ci = getConcept(T.getLeft());
			Concept Cj = getConcept(T.getMiddle());
			System.out.println("   > Checking the evaluation between "+Ci+" and "+Cj+"...");
			RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
			toSend.add(new Evaluation(State.MakeEvaluationState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, rt)));
			System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
		}
		cleanMailbox(Performative.Evaluation);
		// Send new evaluations
		sendMessages(toSend);
		return State.MakeEvaluationState;
	}
	
	public State makeOverallEvaluation() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Read new evaluation
		for(Message m : mail.getMessages(Performative.Evaluation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, RTriplet> T = mail.rtriplet;
			Concept Ci = getConcept(T.getLeft());
			Concept Cj = getConcept(T.getMiddle());
			System.out.println("   > Checking the evaluation between "+Ci+" and "+Cj+"...");
			// Get the RTriplets
			RTriplet m_rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
			System.out.println("     > Our evaluation    : "+m_rt);
			RTriplet o_rt = T.getRight();
			System.out.println("     > Other's evaluation: "+o_rt);
			RTriplet overall_rt = m_rt.rectify(o_rt);
			System.out.println("     > Overall evaluation: "+overall_rt);
			// Add the RTriplet
			mRTriplets.add(new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, m_rt));
			oRTriplets.add(new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, o_rt));
			ovRTriplets.add(new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, overall_rt));
			// If it's an inclusion, add the hierarchy
			if(agree(overall_rt.getTriplet()) == Relation.Inclusion) {
				Triplet<ConID, ConID, Hierarchy> new_hierarchy = new Triplet<ConID, ConID, Hierarchy>(Ci.id, Cj.id, hierarchyKind(overall_rt.getTriplet()));
				Hierarchies.add(new_hierarchy);
			}
			// Send the overall relation to the other agent
			toSend.add(new messages.Relation(State.UpdateDisagreementsState, new Triplet<ConID, ConID, Relation>(Ci.id, Cj.id, agree(overall_rt.getTriplet()))));
		}
		sendMessages(toSend);
		cleanMailbox(Performative.Evaluation);
		return State.UpdateDisagreementsState;
	}
	
	public State updateDisagreements() {
		for(Message m : mail.getMessages(Performative.Relation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, Relation> relation = mail.relation;
			System.out.println("   > Checkign the relation between "+relation.getLeft()+" and "+relation.getMiddle()+"...");
			if(isDisagreement(relation))
				addDisagreement(relation);
		}
		cleanMailbox(Performative.Relation);
		// Delete the disagreement
		removeDisagreement(disagreement);
		this.disagreement = null;
		return State.SendSelfInternalEqualitiesState;
	}
	
	// Removing self equalities
	public State sendSelfInternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Check which of our concepts are equivalents
		System.out.println("   > Checking which of our concepts are equivalents...");
		for(Concept Ci : Kc.getAllConcepts()) {
			for(Concept Cj : m_new_concepts) {
				if(!Ci.equals(Cj) && (agree(Ci, Cj) == Relation.Equivalence || agree(Ci,Cj) == Relation.Blind)) {
					RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
					toSend.add(new Evaluation(State.CheckOtherInternalEqualitiesState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, rt)));
					System.out.println("     > Found the concepts "+Ci+" and "+Cj+"!");
				}
			}
		}
		sendMessages(toSend);
		return State.CheckOtherInternalEqualitiesState;
	}
	
	public State checkOtherInternalEqualites() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Read new evaluations
		System.out.println("   > Check which of the other agent's concepts the other agent sees as equivalents or blinds...");
		for(Message m : mail.getMessages(Performative.Evaluation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, RTriplet> T = mail.rtriplet;
			// Get the concepts
			Concept Ci = getConcept(T.getLeft());
			Concept Cj = getConcept(T.getMiddle());
			System.out.println("     > Checking the evaluation between "+Ci+" and "+Cj+"...");
			RTriplet m_rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
			// Get the RTriplets
			System.out.println("       > Our evaluation    : "+m_rt);
			RTriplet o_rt = T.getRight();
			System.out.println("       > Other's evaluation: "+o_rt);
			RTriplet overall_rt = m_rt.combine(o_rt);
			System.out.println("       > Overall evaluation: "+overall_rt);
			// Get the triplets
			int[] m_evaluation = m_rt.getTriplet();
			int[] o_evaluation = o_rt.getTriplet(Ci.id, Cj.id);
			int[] overall_evaluation = overall_rt.getTriplet();
			// For each integer of the r-triplet
			for(int i=0; i<3; i++) {
				// Check if examples need to be exchanged, if this agent is the one that has less examples or should seize the evaluation
				if((overall_evaluation[i] < 0) && (m_evaluation[i] < o_evaluation[i] || (m_evaluation[i] == o_evaluation[i]))){
					// If yes, send them
					Collection<Example> candidates = requestExamples(Ci.id, Cj.id, i);
					Collection<Example> selected = ToolSet.substract(candidates, e_exchanged);
					e_exchanged.addAll(selected);
					toSend.add(new SendExamples(this, State.CheckSelfInternalEqualitiesState, ToolSet.substract(candidates, e_exchanged)));
				}
			}
			// Notify the other agent
			toSend.add(new Evaluation(State.CheckSelfInternalEqualitiesState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, overall_rt)));
		}
		cleanMailbox(Performative.Evaluation);
		sendMessages(toSend);
		return State.CheckSelfInternalEqualitiesState;
	}
	
	public State checkSelfInternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Add examples
		for(Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			Kc.addExamples(mail.example_list);
			Kf.addExamples(mail.example_list);
			Hc.addExamples(mail.example_list);
			Hf.addExamples(mail.example_list);
			e_exchanged.addAll(mail.example_list);
		}
		cleanMailbox(Performative.SendExamples);
		// Read new evaluations
		System.out.println("   > Check which of our concepts are now seen as equivalents or blinds...");
		for(Message m : mail.getMessages(Performative.Evaluation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, RTriplet> T = mail.rtriplet;
			// Get the concepts
			Concept Ci = getConcept(T.getLeft());
			Concept Cj = getConcept(T.getMiddle());
			System.out.println("     > Checking the evaluation between "+Ci+" and "+Cj+"...");
			// Get the RTriplets
			RTriplet m_rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
			System.out.println("       > Our evaluation    : "+m_rt);
			RTriplet o_rt = T.getRight();
			System.out.println("       > Other's evaluation: "+o_rt);
			RTriplet overall_rt = m_rt.combine(o_rt);
			System.out.println("       > Overall evaluation: "+overall_rt);
			// Get the triplets
			int[] m_evaluation = m_rt.getTriplet();
			int[] o_evaluation = o_rt.getTriplet(Ci.id, Cj.id);
			int[] overall_evaluation = overall_rt.getTriplet();
			// For each integer of the r-triplet,
			for(int i=0; i<3; i++) {
				// Check if examples need to be exchanged, if this agent is the one that has less examples or should seize the evaluation
				if((overall_evaluation[i] < 0) && (m_evaluation[i] < o_evaluation[i])){
					// If yes, send them
					Collection<Example> candidates = requestExamples(Ci.id, Cj.id, i);
					Collection<Example> selected = ToolSet.substract(candidates, e_exchanged);
					e_exchanged.addAll(selected);
					toSend.add(new SendExamples(this, State.ValidOtherInternalEqualitiesState, ToolSet.substract(candidates, e_exchanged)));
				}
			}
			// Notify the other agent
			toSend.add(new Evaluation(State.ValidOtherInternalEqualitiesState, new Triplet<ConID, ConID, RTriplet>(Ci.id, Cj.id, overall_rt)));
		}
		cleanMailbox(Performative.Evaluation);
		sendMessages(toSend);
		return State.ValidOtherInternalEqualitiesState;
	}
	
	public State validOtherInternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Add examples
		for (Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			Kc.addExamples(mail.example_list);
			Kf.addExamples(mail.example_list);
			Hc.addExamples(mail.example_list);
			Hf.addExamples(mail.example_list);
			e_exchanged.addAll(mail.example_list);
		}
		cleanMailbox(Performative.SendExamples);
		// Read new evaluations
		System.out.println("   > Check which of the other agent's concepts are now seen as equivalents or blinds...");
		for (Message m : mail.getMessages(Performative.Evaluation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, RTriplet> T = mail.rtriplet;
			// Get the concepts
			Concept Ci = getConcept(T.getLeft());
			Concept Cj = getConcept(T.getMiddle());
			System.out.println("     > Checking the evaluation between "+Ci+" and "+Cj+"...");
			// Get the RTriplets
			RTriplet m_rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
			System.out.println("       > Our evaluation    : " + m_rt);
			RTriplet o_rt = T.getRight();
			System.out.println("       > Other's evaluation: " + o_rt);
			RTriplet overall_rt = m_rt.rectify(o_rt);
			System.out.println("       > Overall evaluation: " + o_rt);
			if(agree(overall_rt.getTriplet()) == Relation.Equivalence || agree(overall_rt.getTriplet()) == Relation.Blind) {
				// Find the latest and remove the other one
				Concept to_remove = (Ci.id.id < Cj.id.id)? Ci : Cj;
				toSend.add(new Remove(State.UpdateContrastSetState, to_remove.id));
				System.out.println("       > Agreeing with the other agent that the two concepts are equivalent or blind");
			}
		}
		cleanMailbox(Performative.Evaluation);
		sendMessages(toSend);
		return State.SendExternalEqualitiesState;
	}
	
	
	// Removing other multiple equalites
	public State sendExternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Check, for each of our concept, if two of the other's concepts are equivalent with it
		Map<ConID,List<ConID>> equivalences = new HashMap<>();
		for(Concept Ci : Kc.getAllConcepts()) {
			List<ConID> equivalents = new ArrayList<>();
			for(Concept Cj : Hc.getAllConcepts()) {
				RTriplet rt = getOverallRTriplet(Ci.id, Cj.id).getRight();
				if(agree(rt.getTriplet()) == Relation.Equivalence) {
					equivalents.add(Cj.id);
				}
			}
			equivalences.put(Ci.id, equivalents);
		}
		// Send the equivalences to the other agents
		System.out.println("   > Checking if some of the other's concepts are violating the transitivity of our equivalence...");
		for(Entry<ConID,List<ConID>> entry : equivalences.entrySet()) {
			if(entry.getValue().size() > 1) {
				System.out.println("     > The concepts "+entry.getValue()+" are two many to be equivalents, informing other agent...");
				// If yes, notify other agent
				toSend.add(new Intransitive(State.UpdateContrastSetState, entry.getKey(), entry.getValue()));
			}
		}
		sendMessages(toSend);
		return State.UpdateContrastSetState;
	}
	
	public State updateContrastSet(){
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Keep track of concepts that have been removed
		List<Concept> to_remove = new ArrayList<>();
		System.out.println("   > Removing concepts from our contrast set...");
		// Remove the concets for which there is a green light for removal
		for(Message m : mail.getMessages(Performative.Remove)) {
			mail.readMessage(m);
			Concept c = getConcept(mail.con_id);
			if(!c.isNull() && !to_remove.contains(c)) {
				System.out.println("     > We remove the concept "+c+" that has an equivalent");
				toSend.add(new Remove(State.UpdateHypothesisState, c.id));
				to_remove.add(c);
			}
		}
		cleanMailbox(Performative.Remove);
		// Remove the concepts that violate the transitivity of our equivalence
		for(Message m : mail.getMessages(Performative.Intransitive)) {
			mail.readMessage(m);
			// Get the list of the concepts that should be equivalent
			ArrayList<Concept> equivalents = new ArrayList<>();
			for(ConID id : mail.id_list)
				equivalents.add(getConcept(id));
			// Remove the concepts that have allready been deleted or marked to delete
			equivalents.removeAll(to_remove);
			// Check if we still should remove concepts
			if(equivalents.size() > 1) {
				System.out.println("     > The following concepts are seen as the equivalent of a same concept by the other agent: "+equivalents);
				// Find the hypernymest concept (don't check, that word does not exist)
				Concept global_hypernym = null;
				for(int i=0; i<equivalents.size(); i++) {
					for(int j=i+1; j<equivalents.size(); j++) {
						// Get the concepts
						Concept C1 = equivalents.get(i);
						Concept C2 = equivalents.get(j);
						int[] evaluation = evaluation(C1, C2);
						// Check that they are indeed hypo/hypernymes
						if(agree(evaluation) != Relation.Inclusion)
							System.out.println("       > Problem: the concepts should be in a relation of inclusion but instead we have : "+display(evaluation));
						// Get the hypernym
						Concept local_hypernym = (hierarchyKind(evaluation) == Hierarchy.Hyperonymy)? C1 : C2;
						if(global_hypernym == null)
							global_hypernym = local_hypernym;
						else if(hierarchyKind(evaluation(local_hypernym,global_hypernym)) == Hierarchy.Hyperonymy)
							global_hypernym = local_hypernym;
					}
				}
				System.out.println("       > We found the globla hypernym "+global_hypernym+", it is the only one we should keep");
				// Once we found the global hypernym, we delete all the other concepts from our contrast set
				for(Concept c : equivalents) {
					if(!c.equals(global_hypernym)) {
						System.out.println("     > We remove the concept "+c+" that was violating the transitivity of our equivalence");
						toSend.add(new Remove(State.UpdateHypothesisState, c.id));
						to_remove.add(c);
					}
				}
			}
			else
				System.out.println("       > All the involved concepts will be removed, no issue");
		}
		// Effectively removing concepts
		for(Concept c : to_remove)
			removeConcept(c);
		cleanMailbox(Performative.Intransitive);
		sendMessages(toSend);
		return State.UpdateHypothesisState;
	}
	
	public State updateHypothesis() {
		// Remove the concets that have been deleted from the other agent's contrast set
		System.out.println("   > Removing contrast sets from our hypothesis...");
		for (Message m : mail.getMessages(Performative.Remove)) {
			mail.readMessage(m);
			Concept c = getConcept(mail.con_id);
			System.out.println("     > We remove the concept "+c+" that has been removed from the other agent's contrast set");
			removeConcept(c);
		}
		cleanMailbox(Performative.Remove);
		return State.ChooseDisagreementState;
	}
	
	public State updateSign(){
		// List of eventual new signs to send
		List<Message> toSend = new ArrayList<>();
		// New signs for the new concepts
		String new_sign = null;
		String new_sign_2 = null;
		// Read the messages and get the new signs
		for(Message m : mail.getMessages(Performative.Baptise)){
			mail.readMessage(m);
			if (mail.integer == 0)
				new_sign = mail.sign;
			if (mail.integer == 1)
				new_sign_2 = mail.sign;
		}
		cleanMailbox(Performative.Baptise);
		// If the disagreement is a synonymy, replace the two concepts by a synonym
		if(disagreement.getRight() == Relation.Equivalence) {
			// Check if we received a sign. If not, send a new sign
			if(new_sign == null) {
				// Create new sign
				new_sign = "custom:"+Sign.getNewSymbol();
				// Send it to the other agent
				toSend.add(new Baptise(State.UpdateSignsState, new_sign, 0));
				sendMessages(toSend);
			}
			// Get the concept from contrast set
			Concept m_old_concept = getConcept(disagreement.getLeft());
			// Get the concept from hypothesis
			Concept o_old_concept = getConcept(disagreement.getMiddle());
			// Replace concept in contrast set
			System.out.println("    > Replacing the sign of our concept "+m_old_concept.sign()+" by a new sign "+new_sign);
			replaceConcept(m_old_concept, new_sign);
			// Replace concept in hypothesis
			System.out.println("    > Replacing the sign of the other's concept "+o_old_concept.sign()+" by a new sign "+new_sign);
			replaceConcept(o_old_concept, new_sign);
		}
		// If the disagreement is a homonymy, replace each concept with new concepts that have different signs
		if(disagreement.getRight() == Relation.Disjunction) {
			// Check if we received a sign. If not, send a new sign
			if (new_sign == null && new_sign_2 == null) {
				// Create new sign
				new_sign = "custom:" + Sign.getNewSymbol();
				new_sign_2 = "custom:" + Sign.getNewSymbol();
				// Send it to the other agent
				toSend.add(new Baptise(State.UpdateSignsState, new_sign, 1));
				toSend.add(new Baptise(State.UpdateSignsState, new_sign_2, 0));
				sendMessages(toSend);
			}
			// Get the concept from contrast set
			Concept m_old_concept = getConcept(disagreement.getLeft());
			// Get the concept from hypothesis
			Concept o_old_concept = getConcept(disagreement.getMiddle());
			// Replace concept in contrast set
			System.out.println("    > Replacing the sign of our concept "+m_old_concept.sign()+" by a new sign "+new_sign);
			replaceConcept(m_old_concept, new_sign);
			// Replace concept in hypothesis
			System.out.println("    > Replacing the sign of the other's concept "+o_old_concept.sign()+" by a new sign "+new_sign_2);
			replaceConcept(o_old_concept, new_sign_2);
		}
		// Delete the disagreement
		removeDisagreement(disagreement);
		this.disagreement = null;
		// Delet the relations that are not disagreements
		this.cleanDisagreements();
		// Move on
		return State.ChooseDisagreementState;
	}
	
	public State voteForSign(){
		List<Message> toSend = new LinkedList<>();
		// Check whether or not the other agent will count the votes
		boolean seized = false;
		for (Message m : mail.getMessages(Performative.Seize)) {
			mail.readMessage(m);
			seized = mail.bool_switch;
		}
		cleanMailbox(Performative.Seize);
		if(!seized) {
			toSend.add(new Seize(State.VoteForSignsState));
			sendMessages(toSend);
			return State.ElectSignsState;
		}
		for(Concept Ci : m_transfered){
			for(Concept Cj : Kc.getAllConcepts()){
				Triplet<String, String, Double> t = new Triplet<String, String, Double>(Cj.sign(), Ci.sign(), Double.valueOf(ToolSet.intersection(Ci.extensional_definition, Cj.extensional_definition).size()) / Ci.extensional_definition.size());
				toSend.add(new Vote(State.ElectSignsState, t));
				System.out.println("   > The agent sends "+t.getRight()+" votes to name the concept "+t.getLeft()+" with the sign "+t.getMiddle());
			}
		}
		sendMessages(toSend);
		return State.ElectSignsState;
	}
	
	public State electSign(){
		// Check whether or not the other agent counted the votes
		boolean seized = false;
		for(Message m : mail.getMessages(Performative.Seize)) {
			mail.readMessage(m);
			seized = mail.bool_switch;
		}
		cleanMailbox(Performative.Seize);
		// If the other agent counter the votes, update the signs according to what it sent
		if(seized) {
			for (Message m : mail.getMessages(Performative.Replace)) {
				mail.readMessage(m);
				ConID id = mail.con_id;
				String sign = mail.sign;
				System.out.println("   > Replacing the sign of concept "+id+" by "+sign+"...");
				Kc.getConcept(id).sign = new Sign(sign);
				Hc.getConcept(id).sign = new Sign(sign);
			}
			cleanMailbox(Performative.Replace);
			System.out.println("   > The new contrast set is: "+Kc.getAllConcepts());
			System.out.println("   > The new hypothesis is  : "+Hc.getAllConcepts());
			// Argumentation terminated normally, it counts as a success
			ExpFileManager.success = 1;
			return State.UpgradeKnowledgeState;
		}
		// Seize the naming process
		List<Message> toSend = new ArrayList<>();
		toSend.add(new Seize(State.ElectSignsState));
		// Compute the vote
		Map<String,Map<String,Double>> vote = new HashMap<>();
		// Initialize the map
		for(Concept c : Kc.getAllConcepts()){
			vote.put(c.sign(), new HashMap<>());
		}
		// Get the votes from the other
		for(Message m : mail.getMessages(Performative.Elect)){
			mail.readMessage(m);
			Triplet<String, String, Double> t = mail.vote;
			Map<String, Double> v = vote.get(t.getLeft());
			if (v.get(t.getMiddle()) == null)
				v.put(t.getMiddle(), 0.);
			v.put(t.getMiddle(), v.get(t.getMiddle()) + t.getRight());
		}
		// Put own votes
		for(Concept Ci : m_transfered){
			for(Concept Cj : Kc.getAllConcepts()){
				Map<String,Double> v = vote.get(Cj.sign());
				if(v.get(Ci.sign()) == null)
					v.put(Ci.sign(), 0.);
				v.put(Ci.sign(), v.get(Ci.sign()) + Double.valueOf(ToolSet.intersection(Ci.extensional_definition, Cj.extensional_definition).size()) / Ci.extensional_definition.size());
			}
		}
		// For each past concept, replace a new concept's name
		Set<String> settledCases = new HashSet<>();
		for(String s : vote.keySet()){
			System.out.println("   > Voting for concept "+s);
			String winner = null;
			Double bestScore = 0.;
			for(Entry<String, Double> v : vote.get(s).entrySet()){
				System.out.println("      > The sign "+v.getKey()+" received "+v.getValue()+" votes");
				if(!settledCases.contains(v.getKey()) && v.getValue() > bestScore){
					System.out.println("      > This sign hasn't been used yet and has the best score so far");
					winner = v.getKey();
					bestScore = v.getValue();
				}
			}
			if(winner != null){
				System.out.println("   > There is a winner! "+s+" is now named "+winner);
				settledCases.add(winner);
				toSend.add(new Replace(State.ElectSignsState, Kc.getConcept(s).id, winner));
				toSend.add(new Replace(State.ElectSignsState, Hc.getConcept(s).id, winner));
				Kc.getConcept(s).sign = new Sign(winner);
				Hc.getConcept(s).sign = new Sign(winner);
			}
		}
		cleanMailbox(Performative.Elect);
		System.out.println("   > The new contrast set is: "+Kc.getAllConcepts());
		System.out.println("   > The new hypothesis is  : "+Hc.getAllConcepts());
		// Send messages
		sendMessages(toSend);
		// Argumentation terminated normally, it counts as a success
		ExpFileManager.success = 1;
		return State.UpgradeKnowledgeState;
	}
	
	// Save newly achieved contrast set and update hypothesis
	public State upgradeKnowledge() {
		// Remove the transfered concepts from the checked concepts
		System.out.println("   > Removing own concepts that have been transfered from the checked concepts...");
		for (Concept c : m_transfered) {
			System.out.println("     > "+c);
			m_checked.remove(c.id);
		}
		System.out.println("   > Removing other's concepts that have been transfered from the checked concepts...");
		for (Concept c : o_transfered) {
			System.out.println("     > "+c);
			o_checked.remove(c.id);
		}
		// Put the current concepts in the final contrast set
		System.out.println("   > Putting own concepts from the current contrast sets to checked concepts...");
		for (Concept c : Kc.getAllConcepts()) {
			System.out.println("     > "+c);
			Kf.addConcept(c);
			m_checked.add(c.id);
		}
		System.out.println("   > Putting other's concepts from the current contrast sets to checked concepts...");
		for (Concept c : Hc.getAllConcepts()) {
			System.out.println("     > "+c);
			Hf.addConcept(c);
			o_checked.add(c.id);
		}
		// Clean concepts to add
		System.out.println("   > Own checked concepts  : "+m_checked);
		System.out.println("   > Other checked concepts: "+o_checked);
		m_transfered = new ArrayList<>();
		o_transfered = new ArrayList<>();
		m_new_concepts = new ArrayList<>();
		o_new_concepts = new ArrayList<>();
		// Clean Kc and Hc
		Kc = new ContrastSet(new HashSet<>(), new HashSet<>(Kf.getContext()));
		Hc = new ContrastSet(new HashSet<>(), new HashSet<>(Kf.getContext()));
		// Remove the example tested
		example = null;
		// Go to vote for the signs of Kf
		return State.WaitExample;
	}
	
	public ContrastSet Ki() {
		return this.Ki;
	}
	
	public ContrastSet K() {
		return this.Kf;
	}
	
	public ContrastSet H() {
		return this.Hf;
	}
	
	public State state() {
		return current_state;
	}
	
	public Triplet<ConID,ConID,Relation> disagreement() {
		return this.disagreement;
	}
	
	public Set<Example> e_exchanged(){
		return this.e_exchanged;
	}
	
	public Map<GenID, Generalization> g_exchanged(){
		return this.g_exchanged;
	}
	
	public void addEExchanged(Collection<Example> E) {
		this.e_exchanged.addAll(E);
	}
	
	public void addGExchanged(Collection<Generalization> I) {
		for(Generalization g : I) {
			this.g_exchanged.put(g.id, g);
		}
	}
	
	/**
	 * Print an evaluation in the terminal.
	 * @param ev, the evaluation to display in the terminal.
	 */
	public void print(int[] ev){
		if(ev == null)
			System.out.println("null");
		else
			System.out.println(ev[0]+", "+ev[1]+", "+ev[2]);
	}
	
	/**
	 * Return an evaluation as a {@link String}.
	 * @param ev the evaluation
	 * @return the {@link String} representing the evaluation
	 */
	public String display(int[] ev) {
		return ev[0]+", "+ev[1]+", "+ev[2];
	}
	
	// Getter for collections
	public Triplet<ConID, ConID, Relation> getRelation(ConID id1, ConID id2){
		Triplet<ConID, ConID, RTriplet> t = getOverallRTriplet(id1, id2);
		return new Triplet<ConID, ConID, Relation>(id1, id2, agree(t.getRight().getTriplet()));
	}
	
	public Triplet<ConID, ConID,RTriplet> getOwnRTriplet(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, RTriplet> t : mRTriplets)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}
	
	public Triplet<ConID, ConID,RTriplet> getOtherRTriplet(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, RTriplet> t : oRTriplets)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}
	
	public Triplet<ConID, ConID,RTriplet> getOverallRTriplet(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, RTriplet> t : ovRTriplets)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}
	
	public Triplet<ConID, ConID, Hierarchy> getHierarchies(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, Hierarchy> t : Hierarchies)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}
	
	public Triplet<ConID, ConID, Relation> getDisagreement(ConID id1, ConID id2, Relation d){
		for(Triplet<ConID, ConID, Relation> t : getAllDisagreements()) {
			if (t.getRight() == d)
				if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
					return t;
		}
		System.out.println("        > couldn't find it in "+getAllDisagreements());
		return null;
	}
	
public Set<Triplet<ConID, ConID, RTriplet>> getAllRTriplets(){
		Set<Triplet<ConID, ConID, RTriplet>> output = new HashSet<>();
		output.addAll(mRTriplets);
		output.addAll(oRTriplets);
		output.addAll(ovRTriplets);
		return output;
	}
	
	public Set<Triplet<ConID, ConID, Relation>> getDisagreements(){
		Set<Triplet<ConID, ConID, Relation>> output = new HashSet<>();
		output.addAll(SelfDisagreements);
		output.addAll(SemanticDisagreements);
		output.addAll(LexicalDisagreements);
		output.addAll(UntransDisagreements);
		return output;
	}
	
	public Concept getHyponym(ConID id1, ConID id2) {
		Triplet<ConID, ConID, Hierarchy> h = getHierarchies(id1, id2);
		if(h.getRight() == Hierarchy.Hyponymy)
			return getConcept(h.getLeft());
		else if(h.getRight() == Hierarchy.Hyperonymy)
			return getConcept(h.getMiddle());
		else
			return null;
	}
	
	public Concept getHypernym(ConID id1, ConID id2) {
		Triplet<ConID, ConID, Hierarchy> h = getHierarchies(id1, id2);
		if(h == null)
			System.out.println(Hierarchies);
		if(h.getRight() == Hierarchy.Hyperonymy)
			return getConcept(h.getLeft());
		else if(h.getRight() == Hierarchy.Hyponymy)
			return getConcept(h.getMiddle());
		else
			return null;
	}
	
	public Concept getConcept(ConID id) {
		for(Concept c : getAllConcepts())
			if(c.id.equals(id))
				return c;
		return new NullConcept();
	}
	
	public Set<Concept> getAllConcepts(){
		Set<Concept> output = new HashSet<>();
		output.addAll(Kc.getAllConcepts());
		output.addAll(Hc.getAllConcepts());
		return output;
	}
	
	public Container getContainer(ConID id) {
		if(!Kc.getConcept(id).isNull())
			return Kc;
		if(!Hc.getConcept(id).isNull())
			return Hc;
		return new NullContainer();
	}
	
	public void removeConcept(Concept c) {
		Container Q = getContainer(c.id);
		removeConcept(c,Q);
	}
	
	// Removing and replacing concepts
	public boolean removeConcept(Concept c, Container cn) {
		if(c.isNull())
			return false;
		// List of relations, hierarchies and disagreements to delet
		List<Triplet<ConID, ConID, RTriplet>> relationsToDelet = new ArrayList<>();
		List<Triplet<ConID, ConID, Hierarchy>> hierarchiesToDelet = new ArrayList<>();
		List<Triplet<ConID, ConID, Relation>> disagreementsToDelet = new ArrayList<>();
		
		// Mark to emove associated relations
		for (Triplet<ConID, ConID, RTriplet> t : getAllRTriplets())
			if (t.getLeft().equals(c.id) || t.getMiddle().equals(c.id))
				relationsToDelet.add(t);
		// Mark to remove associated hierarchies
		for (Triplet<ConID, ConID, Hierarchy> t : Hierarchies)
			if (t.getLeft().equals(c.id) || t.getMiddle().equals(c.id))
				hierarchiesToDelet.add(t);
		// Mark to remove associated disagreements
		for (Triplet<ConID, ConID, Relation> t : getAllDisagreements())
			if (t.getLeft().equals(c.id) || t.getMiddle().equals(c.id))
				disagreementsToDelet.add(t);

		// Remove the triplets
		mRTriplets.removeAll(relationsToDelet);
		oRTriplets.removeAll(relationsToDelet);
		ovRTriplets.removeAll(relationsToDelet);
		Hierarchies.removeAll(hierarchiesToDelet);
		for(Triplet<ConID, ConID, Relation> disagreement : disagreementsToDelet)
			removeDisagreement(disagreement);
		// Remove the concept
		cn.removeConcept(c);
		return true;
	}
	
	public void replaceConcept(Concept replaced, String replacing) {
		Concept to_change_sign = replaced;
		to_change_sign.sign = new Sign(replacing);
	}
	
	// Evaluation functions
	public int[] evaluation(SemioticElement se1, SemioticElement se2){
		int A = 0;
		int B = 0;
		int AB = 0;
		A = Math.min(ToolSet.substract(se1.getExtension(Kc), se2.getExtension(Kc)).size(),ToolSet.THRESHOLD);
		B = Math.min(ToolSet.substract(se2.getExtension(Kc), se1.getExtension(Kc)).size(),ToolSet.THRESHOLD);
		AB = Math.min(ToolSet.intersection(se1.getExtension(Kc), se2.getExtension(Kc)).size(),ToolSet.THRESHOLD);
		int[] out = {A, AB, B};
		return out;
	}
	
	
	public int[] evaluation(Set<SemioticElement> set1, Set<SemioticElement> set2) {
		HashSet<Example> tt1 = new HashSet<>();
		HashSet<Example> tt2 = new HashSet<>();
		int A = 0;
		int B = 0;
		int AB = 0;
		for(SemioticElement se1 : set1)
			tt1.addAll(se1.getExtension(Kc));
		for(SemioticElement se2 : set2)
			tt2.addAll(se2.getExtension(Kc));
		A = Math.min(ToolSet.substract(tt1, tt2).size(),ToolSet.THRESHOLD);
		B = Math.min(ToolSet.substract(tt2, tt1).size(),ToolSet.THRESHOLD);
		AB = Math.min(ToolSet.intersection(tt1, tt2).size(),ToolSet.THRESHOLD);
		int[] out = {A, AB, B};
		return out;
	}

	public  int[] evaluation(SemioticElement se1, Set<SemioticElement> set2) {
		Set<SemioticElement> set1 = new HashSet<>(se1.getExtension(Kc));
		return evaluation(set1, set2);
	}

	public  int[] evaluation(Set<SemioticElement> set1, SemioticElement se2) {
		return evaluation(se2, set1);
	}	

	// Different agreement fonctions (see paper for more information)
	
	public Relation agree(int[] ev){
		if(ev[0] < 0 || ev[1] < 0 || ev[2] <0 )
			return null;
		if((ev[0] < ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD)|| (ev[0] >= ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD) || (ev[0] < ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD))
			return Relation.Blind;
		if(ev[0] < ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD)
			return Relation.Equivalence;
		if(ev[0] >= ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD)
			return Relation.Disjunction;
		if((ev[0] < ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD) || (ev[0] >= ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2]< ToolSet.THRESHOLD))
			return Relation.Inclusion;
		if(ev[0] >= ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD)
			return Relation.Overlap;
		return null;
	}
	
	public Relation agree(SemioticElement se1, SemioticElement se2){
		int[] ev = evaluation(se1, se2);
		if(ev[0] < 0 || ev[1] < 0 || ev[2] <0 )
			return null;
		if((ev[0] < ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD)|| (ev[0] >= ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD) || (ev[0] < ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD))
			return Relation.Blind;
		if(ev[0] < ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD)
			return Relation.Equivalence;
		if(ev[0] >= ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD)
			return Relation.Disjunction;
		if((ev[0] < ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD) || (ev[0] >= ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2]< ToolSet.THRESHOLD))
			return Relation.Inclusion;
		if(ev[0] >= ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD)
			return Relation.Overlap;
		return null;
	}
	
	public Relation agree(Set<SemioticElement> set1, Set<SemioticElement> set2) {
		int[] ev = evaluation(set1, set2);
		if(ev[0] < 0 || ev[1] < 0 || ev[2] <0 )
			return null;
		if((ev[0] < ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD)|| (ev[0] >= ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD) || (ev[0] < ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD))
			return Relation.Blind;
		if(ev[0] < ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD)
			return Relation.Equivalence;
		if(ev[0] >= ToolSet.THRESHOLD && ev[1] < ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD)
			return Relation.Disjunction;
		if((ev[0] < ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD) || (ev[0] >= ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2]< ToolSet.THRESHOLD))
			return Relation.Inclusion;
		if(ev[0] >= ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD)
			return Relation.Overlap;
		return null;
	}

	public Relation agree(SemioticElement se1, Set<SemioticElement> set2) {
		Set<SemioticElement> set1 = new HashSet<>(se1.getExtension(Kc));
		return agree(set1, set2);
	}

	public Relation agree(Set<SemioticElement> set1, SemioticElement se2) {
		return agree(se2, set1);
	}
	
	public Set<Example> adjunctSet(Generalization g, Set<Example> context) {
		return ToolSet.adjunctSet(g,context);
	}
	
	public Set<Example> adjunctSet(Set<Generalization> I, Set<Example> context){
		return ToolSet.adjunctSet(I,context);
	}

	public boolean isDisagreement(Triplet<ConID, ConID, Relation> t){
		if(t.getRight() == Relation.Overlap || t.getRight() == Relation.Inclusion || t.getRight() == Relation.Blind)
			return true;
		else if(t.getRight().equals(Relation.Equivalence) && !getConcept(t.getLeft()).sign().equals(getConcept(t.getMiddle()).sign()))
			return true;
		else if(t.getRight().equals(Relation.Disjunction) && getConcept(t.getLeft()).sign().equals(getConcept(t.getMiddle()).sign()))
			return true;
		else
			return false;
	}
	
	public boolean isSelfDisagreement(Triplet<ConID, ConID, Relation> t) {
		return getContainer(t.getLeft()) == getContainer(t.getMiddle());
	}
	
	public boolean isSelfSelfDisagreement(Triplet<ConID, ConID, Relation> t) {
		if(!isDisagreement(t))
			return false;
		return getContainer(t.getLeft()) == Kc && getContainer(t.getMiddle()) == Kc;
	}
	
	public boolean isOtherSelfDisagreement(Triplet<ConID, ConID, Relation> t) {
		if(!isDisagreement(t))
			return false;
		return getContainer(t.getLeft()) == Hc && getContainer(t.getMiddle()) == Hc;
	}
	
	public boolean isMyBusiness() {
		if(isSelfSelfDisagreement(disagreement)) {
			System.out.println("       > The disagreement is a self disagreement of mine, it is my business");
			return true;
		}
		if(isOtherSelfDisagreement(disagreement)) {
			System.out.println("       > The disagreement is a self disagreement of the other, it is not my business");
			return false;
		}
		ConID Ci = getConcept(disagreement.getLeft()).id;
		ConID Cj = getConcept(disagreement.getMiddle()).id;
		if(disagreement.getRight() == Relation.Untrans) {
			ConID nonNull = (!Ci.isNull())? Ci : Cj;
			return(getContainer(nonNull) == Kc);
		}
		RTriplet m_rt = getOwnRTriplet(Ci, Cj).getRight();
		RTriplet o_rt = getOtherRTriplet(Ci, Cj).getRight();
		boolean only_to_have_examples = (m_rt.getTriplet(Ci, Cj)[1] == ToolSet.THRESHOLD && o_rt.getTriplet(Ci, Cj)[1] < ToolSet.THRESHOLD);
		boolean have_the_examples = m_rt.getTriplet(Ci, Cj)[1] == ToolSet.THRESHOLD;
		boolean have_right_container = true;
		if(disagreement.getRight() == Relation.Inclusion) {
			boolean m_covers_side = (m_rt.getTriplet(Ci, Cj)[0] == ToolSet.THRESHOLD || m_rt.getTriplet(Ci, Cj)[2] == ToolSet.THRESHOLD);
			boolean o_covers_side = (o_rt.getTriplet(Ci, Cj)[0] == ToolSet.THRESHOLD || o_rt.getTriplet(Ci, Cj)[2] == ToolSet.THRESHOLD);
			only_to_have_examples = m_covers_side && !o_covers_side;
			have_the_examples = m_covers_side;
			ConID hypernym = getHypernym(Ci, Cj).id;
			if(getContainer(hypernym) == Hc)
				have_right_container = false;
		}
		if(only_to_have_examples) {
			System.out.println("       > I am the only one to have enough examples, it is my business");
			return true;
		}
		else {
			System.out.println("       > I have enough example     : "+have_the_examples);
			System.out.println("       > I have the right container: "+have_right_container);
			System.out.println("       > It is my business         : "+(have_the_examples && have_right_container));
			return have_the_examples && have_right_container;
		}
	}
	
	
	public void cleanDisagreements() {
		this.cleanDisagreement(SelfDisagreements);
		this.cleanDisagreement(SemanticDisagreements);
		this.cleanDisagreement(UntransDisagreements);
		this.cleanDisagreement(LexicalDisagreements);
	}
	
	public void cleanDisagreement(List<Triplet<ConID, ConID, Relation>> Disagreements){
		List<Triplet<ConID, ConID, Relation>> toDelet = new ArrayList<>();
		for(Triplet<ConID, ConID, Relation> t : Disagreements){
			if(!isDisagreement(t))
				toDelet.add(t);
		}
		Disagreements.removeAll(toDelet);
	}
	
	public void cleanMailbox(Performative performative){
		mail.clearPerformative(performative);
	}
	
	public void cleanMailbox(List<Performative> performatives){
		for(Performative p : performatives) {
			mail.clearPerformative(p);
		}
	}
	
	private List<Triplet<ConID,ConID,Relation>> getAllDisagreements(){
		List<Triplet<ConID,ConID,Relation>> out = new LinkedList<>();
		out.addAll(SelfDisagreements);
		out.addAll(SemanticDisagreements);
		out.addAll(UntransDisagreements);
		out.addAll(LexicalDisagreements);
		return out;
	}
	
	private boolean addDisagreement(Triplet<ConID, ConID, Relation> disagreement) {
		System.out.println("     > adding a new disagreement: "+disagreement);
		if(isSelfDisagreement(disagreement))
			SelfDisagreements.add(disagreement);
		else {
			switch (disagreement.getRight()) {
			case Equivalence:
			case Disjunction:
				LexicalDisagreements.add(disagreement);
				break;
			case Inclusion:
				SemanticDisagreements.addLast(disagreement);
				break;
			case Blind:
			case Overlap:
				SemanticDisagreements.addFirst(disagreement);
				break;
			case Untrans:
				UntransDisagreements.add(disagreement);
				break;
			default:
				System.out.println("     > Problem: we found an unknown case of disagreement between the concepts "+ disagreement.getLeft() + " and " + disagreement.getMiddle());
				break;
			}
		}
		return true;
	}
	
	private boolean removeDisagreement(Triplet<ConID, ConID, Relation> disagreement) {
		if(disagreement == null) {
			System.out.println("     > no disgreement to remove.");
			return false;
		}
		System.out.println("     > removing the disagreement: "+disagreement);
		if(isSelfDisagreement(disagreement))
			SelfDisagreements.remove(disagreement);
		else {
			for (Triplet<ConID, ConID, Relation> t : getAllDisagreements()) {
				switch (t.getRight()) {
				case Equivalence:
				case Disjunction:
					LexicalDisagreements.remove(disagreement);
					break;
				case Inclusion:
				case Overlap:
				case Blind:
					SemanticDisagreements.remove(disagreement);
					break;
				case Untrans:
					UntransDisagreements.remove(disagreement);
					break;
				}
			}
		}
		return true;
	}
	
	// Get the list of untranslatable concepts
	public List<Triplet<ConID,ConID,Relation>> findUntranslatables(){
		// Prepare output
		List<Triplet<ConID,ConID,Relation>> out = new ArrayList<>();
		// For each concept of contrast set, see if we have a relation of equivalence
		for(Concept C1 : Kc.getAllConcepts()) {
			boolean hasEquivalent = false;
			for(Concept C2 : Hc.getAllConcepts()) {
				if(getRelation(C1.id, C2.id).getRight() == Relation.Equivalence) {
					hasEquivalent = true;
					break;
				}
			}
			if(!hasEquivalent)
				out.add(new Triplet<ConID, ConID, Relation>(C1.id, new NullConID(), Relation.Untrans));
		}
		// For each concept of contrast set, see if we have a relation of equivalence
		for (Concept C1 : Hc.getAllConcepts()) {
			boolean hasEquivalent = false;
			for (Concept C2 : Kc.getAllConcepts()) {
				if (getRelation(C1.id, C2.id).getRight() == Relation.Equivalence) {
					hasEquivalent = true;
					break;
				}
			}
			if (!hasEquivalent)
				out.add(new Triplet<ConID, ConID, Relation>(new NullConID(), C1.id, Relation.Untrans));
		}
		return out;
	}
	
	public Hierarchy hierarchyKind(int[] ev){
		if(ev[0] >= ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] < ToolSet.THRESHOLD)
			return Hierarchy.Hyperonymy;
		if(ev[0] < ToolSet.THRESHOLD && ev[1] >= ToolSet.THRESHOLD && ev[2] >= ToolSet.THRESHOLD)
			return Hierarchy.Hyponymy;
		return Hierarchy.Blind;
	}
	
	public Collection<Example> requestExamples(ConID id1, ConID id2, int request){
		// Get intensional definitions
		System.out.println("            > Request for examples involving "+id1+" and "+id2);
		Set<Generalization> idef1 = getConcept(id1).intensional_definition;
		Set<Generalization> idef2 = getConcept(id2).intensional_definition;
		return requestExamples(idef1, idef2, request);
	}
	
	public Collection<Example> requestExamples(Set<Generalization> m_i_def, Set<Generalization> o_i_def, int request){
		Set<Example> S1 = adjunctSet(m_i_def, Kc.context);
		Set<Example> S2 = adjunctSet(o_i_def, Kc.context);
		Collection<Example> candidates = new HashSet<>();
		Collection<Example> out = new HashSet<Example>();
		switch (request) {
		// Example coverd by our concept and not covered by the other's concept
		case 0:
			candidates = ToolSet.substract(S1, S2);
			if(candidates.size() <= ToolSet.THRESHOLD)
				System.out.println("               > Not enough examples in the concept");
			out = new HashSet<>(ToolSet.optiRandomSubset(this,candidates, ToolSet.THRESHOLD));
			System.out.println("               > Sending "+out.size()+" examples that belong to our definition but not to the other's definiton");
			break;
		// Example coverd by our concept and covered by the other's concept
		case 1:	
			candidates = ToolSet.intersection(S1, S2);
			if(candidates.size() <= ToolSet.THRESHOLD)
				System.out.println("               > Not enough examples in the concept");
			out = new HashSet<>(ToolSet.optiRandomSubset(this,candidates, ToolSet.THRESHOLD));
			System.out.println("               > Sending "+out.size()+" examples that belong to our definition and to the other's defintion");
			break;
		// Example not coverd by our concept and covered by the other's concept
		case 2:
			candidates = ToolSet.substract(S2, S1);
			if(candidates.size() <= ToolSet.THRESHOLD)
				System.out.println("               > Not enough examples in the concept");
			out = new HashSet<>(ToolSet.optiRandomSubset(this,candidates, ToolSet.THRESHOLD));
			System.out.println("               > Sending "+out.size()+" examples that don't belong to our definitoin but belong to the other's definition");
			break;
		// If the integer is not associated to a request, inform
		default:
			System.out.println("   > Problem, integer out of range");
		}
		return out;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return nick;
	}

}
