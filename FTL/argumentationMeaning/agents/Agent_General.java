package agents;

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
import interfaces.*;
import messages.*;
import null_objects.NullConID;
import null_objects.NullConcept;
import null_objects.NullContainer;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;
import tools.*;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;
import static tools.ToolSet.*;

/***
 *  The {@link Agent_General} is class of {@link Agent} that search systematically for disagreements.
 *  Unlike {@link Agent_Lazy} which is also an agent, {@link Agent_General} does not need to be presented an example to start the argumentation.
 * 
 * @author kemoadrian
 *
 */

public class Agent_General implements Agent{
	
	// Communication
	private Mailbox mail;
	public String nick;
	public State current_state;
	public Triplet<ConID, ConID, Relation> disagreement;
	
	// Collections of Triplets
	// Agreements
	private LinkedList<Triplet<ConID, ConID, Relation>> SelfDisagreements;
	private LinkedList<Triplet<ConID, ConID, Relation>> SemanticDisagreements;
	private LinkedList<Triplet<ConID, ConID, Relation>> UntransDisagreements;
	private LinkedList<Triplet<ConID, ConID, Relation>> LexicalDisagreements;
	// Relations and Hierarchies
	private List<Triplet<ConID, ConID, RTriplet>> mRTriplets;
	private List<Triplet<ConID, ConID, RTriplet>> oRTriplets;
	private List<Triplet<ConID, ConID, RTriplet>> ovRTriplets;
	public List<Triplet<ConID, ConID, Hierarchy>> Hierarchies;
	// Argumentation
	private BoundaryFix boundaryFix;
	public Argumentation argumentation;
	// Containers
	public ContrastSet Ki;
	public ContrastSet K;
	public ContrastSet H;
	// Concepts
	private List<Concept> m_new_concepts;
	private List<Concept> o_new_concepts;
	
	// AMAIL stuff
	private Set<Example> positiveExamples;
	private Set<Example> negativeExamples;
	
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
		this.K = new ContrastSet(new HashSet<>(), new HashSet<>());
		this.H = new ContrastSet(new HashSet<>(), new HashSet<>());
		this.K.context.addAll(Ki.context);
		this.H.context.addAll(Ki.context);
		this.mRTriplets = new ArrayList<>();
		this.oRTriplets = new ArrayList<>();
		this.ovRTriplets = new ArrayList<>();
		this.Hierarchies = new ArrayList<>();
		this.SelfDisagreements = new LinkedList<>();
		this.SemanticDisagreements = new LinkedList<>();
		this.UntransDisagreements = new LinkedList<>();
		this.LexicalDisagreements = new LinkedList<>();
		this.m_new_concepts = new ArrayList<>();
		this.o_new_concepts = new ArrayList<>();
		this.positiveExamples = new HashSet<>();
		this.negativeExamples = new HashSet<>();
		this.e_exchanged = new HashSet<>();
		this.g_exchanged = new HashMap<>();
		this.mail = new Mailbox();
		// Set the initial phase
		current_state = State.Initial;
		disagreement = null;
		return (K != null);
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
		//System.out.println(e_exchanged().size());
		//System.out.println(e_exchanged());
		switch (current_state) {
		case BuildConState:
			this.current_state = buildConcept();
			break;
		case BuildEDState:
			this.current_state = buildExtensionalDefinition();
			break;
		case BuildIDState:
			this.current_state = buildIntensionalDefinition();
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
			this.current_state = checkOtherInternalEqualities();
			break;
		case CheckSelfInternalEqualitiesState:
			this.current_state = checkSelfInternalEqualities();
			break;
		case ChooseDisagreementState:
			this.current_state = chooseDisagreement();
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
		case FixBoundariesState:
			this.current_state = fixBoundaries();
			break;
		case Initial:
			this.current_state = State.SendIDState;
			break;
		case MakeEvaluationState:
			this.current_state = makeOverallEvaluation();
			break;
		case ReceiveIDState:
			this.current_state = receiveIntensionalDefinition();
			break;
		case SendEvaluationState:
			this.current_state = sendNewEvaluation();
			break;
		case SendExternalEqualitiesState:
			this.current_state = sendExternalEqualities();
			break;
		case SendIDState:
			this.current_state = sendIntensionalDefinition();
			break;
		case SendSelfInternalEqualitiesState:
			this.current_state = sendSelfInternalEqualities();
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
		case ValidOtherInternalEqualitiesState:
			this.current_state = validOtherInternalEqualities();
			break;
		case VoteForSignsState:
			this.current_state = voteForSign();
			break;
		default:
			this.current_state = State.Stop;
			break;
		}
		return State.Stop;
	}
	
	
	private State sendIntensionalDefinition(){
		List<Message> to_send = new ArrayList<>();
		// Reset the list of new concepts
		m_new_concepts = new ArrayList<>();
		// Get all our concepts and create a new {@link Assert} to send 
		for(Concept c : Ki.getAllConcepts()){
			m_new_concepts.add(c);
			// Mark the intensional definitions as shared
			addGExchanged(c.intensional_definition);
			// Send to other agent the intensional definition
			to_send.add(new Assert(this, State.ReceiveIDState, c.id, c.sign(), c.intensional_definition));
			System.out.println("   > The concept "+c+" has been sent to the attacker");
		}
		// Reinitialize K
		K = new ContrastSet(new HashSet<>(), K.context);
		// Ask for a self evaluation
		to_send.add(new CheckSelf(State.SendEvaluationState));
		// Send all the messages created and move on
		sendMessages(to_send);
		return State.ReceiveIDState;
	}
	
	private State receiveIntensionalDefinition(){
		// Reset the list of new concepts
		o_new_concepts = new ArrayList<>();
		// Adding concepts of the other
		for(Message m : mail.getMessages(Performative.Assert)){
			mail.readMessage(m);
			// Create new concept
			ConID id = mail.con_id;
			Sign s = new Sign(mail.sign);
			Set<Generalization> intension = new HashSet<>(mail.generalization_list);
			Set<Example> extension = adjunctSet(intension, H.getContext());
			Concept c = new Concept(id, s, intension, extension);
			addGExchanged(intension);
			// Add new concept to hypothesis
			o_new_concepts.add(c);
			System.out.println("   > The concept "+c+" has been received");
		}
		// Clean the mailbox
		cleanMailbox(Performative.Assert);	
		return State.SendEvaluationState;
	}
	
	// Pick a disagreement to argue about
	private State chooseDisagreement(){
		//Getting the received examples
		for (Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			Collection<Example> examples = mail.example_list;
			System.out.println("   > Adding " + examples.size() + " examples to contrast set ");
			K.addExamples(new HashSet<>(examples));
			System.out.println("   > Adding " + examples.size() + " examples to hypothesis");
			H.addExamples(new HashSet<>(examples));
			e_exchanged.addAll((new HashSet<>(examples)));
		}
		this.cleanMailbox(Performative.SendExamples);
		// Display contrast set + hypothesis
		System.out.println("Contrast-set : "+K.getAllConcepts());
		System.out.println("Hypothesis   : "+H.getAllConcepts());
		System.out.println("Hierarchies  : "+Hierarchies);
		// If the self and semantic disagreements are empty, time to look for untranslatable disagreements
		if(SelfDisagreements.isEmpty() && SemanticDisagreements.isEmpty())
			UntransDisagreements = new LinkedList<>(this.findUntranslatable());
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
		assert disagreement != null;
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
		return State.Stop;
	}
	
	private State deleteBlind() {
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
			assert evaluation != null;
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
	
	private State fixBoundaries() {
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
				Node to_delete_attacked_node = boundaryFix.argumentation.my_argumentation_tree.getNode(to_delete_node.attacks());
				boundaryFix.argumentation.my_argumentation_tree.deleteNode(to_delete_id);
				boundaryFix.argumentation.my_argumentation_tree.deleteNode(to_delete_attacked_node.getid());
				System.out.println("     > Deleting the node " + to_delete_id + " and the node that it attacked: "+ to_delete_attacked_node.getid());
			} else if (boundaryFix.argumentation.other_argumentation_tree.contains(to_delete_id)) {
				Node to_delete_node = boundaryFix.argumentation.other_argumentation_tree.getAttackNode(to_delete_id);
				Node to_delete_attacked_node = boundaryFix.argumentation.other_argumentation_tree.getNode(to_delete_node.attacks());
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
			// If there are generalizations, remember them
			if (!to_add.toGeneralizations().isEmpty()) {
				addGExchanged(to_add.toGeneralizations());
			}
			// Fill the node
			if (!to_add.filled()) {
				boolean filling_success = to_add.fill(g_exchanged);
				if (!filling_success)
					System.out.println("     > PROBLEM: Failed to fill the received attack. Missing generalizations....");
			}
			// If there are examples to add, add them
			if (!to_add.toExamples().isEmpty()) {
				Set<Example> updated_context = boundaryFix.argumentation.context.get(to_add.getLabel());
				updated_context.addAll(to_add.toExamples());
				updated_context = new HashSet<>(cleanDuplicates(updated_context));
				boundaryFix.argumentation.context.put(to_add.getLabel(), updated_context);
				K.addExamples(to_add.toExamples());
				H.addExamples(to_add.toExamples());
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
			System.out.println("     > Adding " + new_belief.getid()+ " as the new belief of the other agent");
			// Add received generalizations
			addGExchanged(new_belief.toGeneralizations());
			// Fill belief
			if (!new_belief.filled()) {
				boolean filling_success = new_belief.fill(g_exchanged);
				if (!filling_success)
					System.out.println("     > PROBLEM: Failed to fill the received belief. Missing generalizations....");
			}
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
					m_new_concepts.add(new Concept(boundaryFix.first_id, Ci.sign, boundaryFix.first_ID, adjunctSet(boundaryFix.first_ID, K.context)));
				}
				if(!boundaryFix.second_ID.isEmpty()) {
					m_new_concepts.add(new Concept(boundaryFix.second_id, Cj.sign, boundaryFix.second_ID, adjunctSet(boundaryFix.second_ID, K.context)));
				}
			}
			else if(isOtherSelfDisagreement(disagreement)) {
				if(!boundaryFix.first_ID.isEmpty()) {
					o_new_concepts.add(new Concept(boundaryFix.first_id, Ci.sign, boundaryFix.first_ID, adjunctSet(boundaryFix.first_ID, K.context)));
				}
				if(!boundaryFix.second_ID.isEmpty()) {
					o_new_concepts.add(new Concept(boundaryFix.second_id, Cj.sign, boundaryFix.second_ID, adjunctSet(boundaryFix.second_ID, K.context)));
				}
			}
			removeConcept(Ci);
			removeConcept(Cj);
			// Reinitialize the boundary fix object
			boundaryFix = null;
			// Change Phase
			return State.SendEvaluationState;
		}
		return State.FixBoundariesState;
	}
	
	private State buildExtensionalDefinition(){
		// Create a list of messages to send
		List<Message> toSend = new ArrayList<>();
		// Get the two involved concepts
		System.out.println("   > Creating the sets of positive and negative examples");
		Concept c1 = getConcept(disagreement.getLeft());
		Concept c2 = getConcept(disagreement.getMiddle());
		// If we are in an untranslatable disagreement, one of the two concepts will be null
		if(disagreement.getRight() == Relation.Untrans) {
			positiveExamples = new HashSet<>(union(c1.extensional_definition, c2.extensional_definition));
			negativeExamples = new HashSet<>(substract(K.context, positiveExamples));
		}
		// In the case of an inclusion, the new concept will be the part of the hypernym that is not the hyponym
		if(disagreement.getRight() == Relation.Inclusion){
			Concept hypernym = getHypernym(c1.id, c2.id);
			Concept hyponym = getHyponym(c1.id, c2.id);
			System.out.println(hypernym);
			System.out.println(hyponym);
			assert hypernym != null;
			assert hyponym != null;
			positiveExamples = new HashSet<>(substract(hypernym.extensional_definition, hyponym.extensional_definition));
			negativeExamples = new HashSet<>(substract(K.context, positiveExamples));
		}
		// In the case of an overlap, the new concept will be the part that is covered by both concepts
		if(disagreement.getRight() == Relation.Overlap){
			positiveExamples = new HashSet<>(intersection(c1.extensional_definition, c2.extensional_definition));
			negativeExamples = new HashSet<>(substract(K.context, positiveExamples));
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
	
	private State checkExtension() {
		// Create a list of messages to send
		List<Message> toSend = new ArrayList<>();
		// Get the two extensional definitions sizes
		System.out.println("   > Checking the size of the predicted extensional definitions");
		int m_size = positiveExamples.size();
		int o_size = -1;
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
		boolean seized = false;
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
			argumentation.exempted = !(isMyBusiness() && positiveExamples.size() >= THRESHOLD);
			System.out.println("     > seizing the argumentation: this agent creates the new concept is "+!argumentation.exempted);
			toSend.add(new Seize(State.CheckEDState, !argumentation.exempted));
		}
		// If not, green light to argue but first, set max FP size and max FN size
		System.out.println("   > Setting max false positives and negatives...");
		argumentation.MAX_FP = ((int) Math.floor((THRESHOLD - 1)) / 4);
		argumentation.MMAX_FN = Math.min(m_size - THRESHOLD, argumentation.MAX_FP);
		argumentation.OMAX_FN = Math.min(o_size - THRESHOLD, argumentation.MAX_FP);
		System.out.println("     > Max false positives : "+argumentation.MAX_FP);
		System.out.println("     > Max false negatives for my Idef : "+argumentation.MMAX_FN);
		System.out.println("     > Max false negatives for other's Idef : "+argumentation.OMAX_FN);
		// Send messages
		sendMessages(toSend);
		return State.BuildIDState;
	}
	
	private State buildIntensionalDefinition(){
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
			// If there are generalizations, remember them
			if (!to_add.toGeneralizations().isEmpty()) {
				addGExchanged(to_add.toGeneralizations());
			}
			// Fill the node
			if(!to_add.filled()) {
				boolean filling_success = to_add.fill(g_exchanged);
				if(!filling_success)
					System.out.println("     > PROBLEM: Failed to fill the received attack. Missing generalizations....");
			}
			// If there are examples to add, add them
			if (!to_add.toExamples().isEmpty()) {
				System.out.println("     > Adding examples...");
				Set<Example> updated_context = argumentation.context.get(to_add.getLabel());
				updated_context.addAll(to_add.toExamples());
				updated_context = new HashSet<>(cleanDuplicates(updated_context));
				argumentation.context.put(to_add.getLabel(), updated_context);
				K.addExamples(to_add.toExamples());
				H.addExamples(to_add.toExamples());
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
			// Add received generalizations
			addGExchanged(new_belief.toGeneralizations());
			// Fill belief
			if(!new_belief.filled()) {
				boolean filling_success = new_belief.fill(g_exchanged);
				if(!filling_success)
					System.out.println("     > PROBLEM: Failed to fill the received belief. Missing generalizations....");
			}
			System.out.println("     > Adding " + new_belief.getid()+ " as the new belief of the other agent");
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
	
	private State buildSign() {
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
	
	private State buildIds() {
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
	
	private State checkId() {
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

	private State buildConcept(){
		// Reset list of new concepts
		m_new_concepts = new ArrayList<>();
		o_new_concepts = new ArrayList<>();
		// Get the semiotic elements of the new concept
		Sign sign = new Sign(argumentation.con_newSign);
		ConID m_id = argumentation.m_con_id;
		ConID o_id = argumentation.o_con_id; 
		Set<Generalization> m_idef = argumentation.getIdef();
		Set<Generalization> o_idef = argumentation.getIdef();
		Set<Example> m_edef = adjunctSet(m_idef,K.getContext());
		Set<Example> o_edef = adjunctSet(o_idef,K.getContext());
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
			assert hyponym != null;
			Concept h_new_concept = new Concept(argumentation.hypo_id, new Sign(argumentation.hypo_newSign), new HashSet<>(hyponym.intensional_definition), new HashSet<>(hyponym.extensional_definition));
			System.out.println("     > Creating the new co-hyponym "+h_new_concept);
			// Add it to the same container as the hypernym
			if(getContainer(hyponym.id) == K) {
				o_new_concepts.add(o_new_concept);
				o_new_concepts.add(h_new_concept); 
			}
			if(getContainer(hyponym.id) == H) {
				m_new_concepts.add(m_new_concept);
				m_new_concepts.add(h_new_concept);
			}
			// Recall that the co-hyponym sign's should be changed
			//replaceConcept(hyponym, argumentation.hypo_newSign);
			// Remove hypernym
			assert hypernym != null;
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
				if(getContainer(Ci.id) == K)
					o_new_concepts.add(o_new_concept);
				if(getContainer(Ci.id) == H)
					m_new_concepts.add(m_new_concept);
			}
			if(!Cj.isNull()) {
				if(getContainer(Cj.id) == K)
					o_new_concepts.add(o_new_concept);
				if(getContainer(Cj.id) == H)
					m_new_concepts.add(m_new_concept);
			}
		}
		return State.SendEvaluationState;
	}

	private State deleteUnachieved() {
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
			assert hyponym != null;
			Concept h_new_concept = new Concept(argumentation.hypo_id, new Sign(argumentation.hypo_newSign), new HashSet<>(hyponym.intensional_definition), new HashSet<>(hyponym.extensional_definition));
			System.out.println("       > Creating the new concept "+h_new_concept);
			// Add it to the same container as the hypernym
			if (getContainer(hyponym.id) == K) {
				o_new_concepts.add(h_new_concept);
				System.out.println("       > the hyponym was ours, add the hyponym to the other contrast set");
			}
			if (getContainer(hyponym.id) == H) {
				m_new_concepts.add(h_new_concept);
				System.out.println("       > the hyponym was the other's, add the hyponym to our contrast set");
			}
			// Recall that the co-hyponym sign's should be changed
			//replaceConcept(hyponym, argumentation.hypo_newSign);
			// Remove Hypernym
			assert hypernym != null;
			removeConcept(hypernym);
			System.out.println("     > Disagreement was an inclusion, deleting hypernym and adding hyponym");
		}
		if(disagreement.getRight() == Relation.Untrans) {
			removeConcept(getConcept(disagreement.getLeft()));
			removeConcept(getConcept(disagreement.getMiddle()));
			System.out.println("     > Disagreement was untranslability, deleting the untranslatable concept");
		}
		return State.SendEvaluationState;
	}

	private State sendNewEvaluation() {
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
			for(Concept Cj : H.getAllConcepts()) {
				RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
				toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<>(Ci.id, Cj.id, rt)));
				System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
			}
		}
		// Make new evaluations (other's new concepts vs my concepts)
		System.out.println("   > Checking our concepts against other agent's new concepts...");
		for(Concept Ci : K.getAllConcepts()) {
			for(Concept Cj : o_new_concepts) {
				RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
				toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<>(Ci.id, Cj.id, rt)));
				System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
			}
		}
		// Make new evaluations (my new concepts vs other's new concepts)
		System.out.println("   > Checking our new concepts against other agent's new concepts...");
		for(Concept Ci : m_new_concepts) {
			for(Concept Cj : o_new_concepts) {
				RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
				toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<>(Ci.id, Cj.id, rt)));
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
					toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<>(Ci.id, Cj.id, rt)));
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
					toSend.add(new Evaluation(State.DraftEvaluationState, new Triplet<>(Ci.id, Cj.id, rt)));
					System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
				}
			}
		}
		// Add concepts
		for(Concept c : m_new_concepts) {
			K.addConcept(c);
			System.out.println("   > Adding concept "+c+" to contrast set...");
		}
		for(Concept c : o_new_concepts) {
			H.addConcept(c);
			System.out.println("   > Adding concept "+c+" to hypothesis...");
		}
		// Send new evaluations
		sendMessages(toSend);
		return State.DraftEvaluationState;
	}
	
	// First tentative to build the overall r-triplet
	private State draftEvaluation() {
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
					Collection<Example> selected = substract(candidates, e_exchanged);
					e_exchanged.addAll(selected);
					toSend.add(new SendExamples(this, State.UpdateEvaluationState, selected));
					// Check if the agents have the same number of examples and hasn't been seized
					if(m_evaluation[i] == o_evaluation[i] && !seized) {
						System.out.println("       > We seize the evaluation");
						toSend.add(new Seize(State.DraftEvaluationState));
					}
				}
			}
			toSend.add(new Evaluation(State.UpdateEvaluationState, new Triplet<>(Ci.id, Cj.id, overall_rt)));
		}
		cleanMailbox(Performative.Evaluation);
		sendMessages(toSend);
		return State.UpdateEvaluationState;
	}

	private State sendUpdatedEvaluation() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Add examples
		for(Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			K.addExamples(mail.example_list);
			H.addExamples(mail.example_list);
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
			toSend.add(new Evaluation(State.MakeEvaluationState, new Triplet<>(Ci.id, Cj.id, rt)));
			System.out.println("     > "+Ci+" vs "+Cj+" = "+rt);
		}
		cleanMailbox(Performative.Evaluation);
		// Send new evaluations
		sendMessages(toSend);
		return State.MakeEvaluationState;
	}

	private State makeOverallEvaluation() {
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
			mRTriplets.add(new Triplet<>(Ci.id, Cj.id, m_rt));
			oRTriplets.add(new Triplet<>(Ci.id, Cj.id, o_rt));
			ovRTriplets.add(new Triplet<>(Ci.id, Cj.id, overall_rt));
			// If it's an inclusion, add the hierarchy
			if(agree(overall_rt.getTriplet()) == Relation.Inclusion) {
				Triplet<ConID, ConID, Hierarchy> new_hierarchy = new Triplet<>(Ci.id, Cj.id, hierarchyKind(overall_rt.getTriplet()));
				Hierarchies.add(new_hierarchy);
			}
			// Send the overall relation to the other agent
			toSend.add(new messages.Relation(State.UpdateDisagreementsState, new Triplet<>(Ci.id, Cj.id, agree(overall_rt.getTriplet()))));
		}
		sendMessages(toSend);
		cleanMailbox(Performative.Evaluation);
		return State.UpdateDisagreementsState;
	}

	private State updateDisagreements() {
		for(Message m : mail.getMessages(Performative.Relation)) {
			mail.readMessage(m);
			Triplet<ConID, ConID, Relation> relation = mail.relation;
			System.out.println("   > Checking the relation between "+relation.getLeft()+" and "+relation.getMiddle()+"...");
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
	private State sendSelfInternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Check which of our concepts are equivalents
		System.out.println("   > Checking which of our concepts are equivalents...");
		for(Concept Ci : K.getAllConcepts()) {
			for(Concept Cj : m_new_concepts) {
				if(!Ci.equals(Cj) && (agree(Ci, Cj) == Relation.Equivalence || agree(Ci,Cj) == Relation.Blind)) {
					RTriplet rt = new RTriplet(Ci.id, Cj.id, evaluation(Ci, Cj));
					toSend.add(new Evaluation(State.CheckOtherInternalEqualitiesState, new Triplet<>(Ci.id, Cj.id, rt)));
					System.out.println("     > Found the concepts "+Ci+" and "+Cj+"!");
				}
			}
		}
		sendMessages(toSend);
		return State.CheckOtherInternalEqualitiesState;
	}
	
	private State checkOtherInternalEqualities() {
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
					Collection<Example> selected = substract(candidates, e_exchanged);
					e_exchanged.addAll(selected);
					toSend.add(new SendExamples(this, State.CheckSelfInternalEqualitiesState, substract(candidates, e_exchanged)));
				}
			}
			// Notify the other agent
			toSend.add(new Evaluation(State.CheckSelfInternalEqualitiesState, new Triplet<>(Ci.id, Cj.id, overall_rt)));
		}
		cleanMailbox(Performative.Evaluation);
		sendMessages(toSend);
		return State.CheckSelfInternalEqualitiesState;
	}
	
	private State checkSelfInternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Add examples
		for(Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			K.addExamples(mail.example_list);
			H.addExamples(mail.example_list);
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
					Collection<Example> selected = substract(candidates, e_exchanged);
					e_exchanged.addAll(selected);
					toSend.add(new SendExamples(this, State.ValidOtherInternalEqualitiesState, substract(candidates, e_exchanged)));
				}
			}
			// Notify the other agent
			toSend.add(new Evaluation(State.ValidOtherInternalEqualitiesState, new Triplet<>(Ci.id, Cj.id, overall_rt)));
		}
		cleanMailbox(Performative.Evaluation);
		sendMessages(toSend);
		return State.ValidOtherInternalEqualitiesState;
	}
	
	private State validOtherInternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Add examples
		for (Message m : mail.getMessages(Performative.SendExamples)) {
			mail.readMessage(m);
			K.addExamples(mail.example_list);
			H.addExamples(mail.example_list);
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
	private State sendExternalEqualities() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		// Check, for each of our concept, if two of the other's concepts are equivalent with it
		Map<ConID,List<ConID>> equivalences = new HashMap<>();
		for(Concept Ci : K.getAllConcepts()) {
			List<ConID> equivalents = new ArrayList<>();
			for(Concept Cj : H.getAllConcepts()) {
				RTriplet rt = requireNonNull(getOverallRTriplet(Ci.id, Cj.id)).getRight();
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
	
	private State updateContrastSet(){
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
	
	private State updateHypothesis() {
		// Remove the concepts that have been deleted from the other agent's contrast set
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
	
	private State updateSign(){
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
		// Delete the relations that are not disagreements
		this.cleanDisagreements();
		// Move on
		return State.ChooseDisagreementState;
	}
	
	private State voteForSign(){
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
		for(Concept Ci : Ki.getAllConcepts()){
			for(Concept Cj : K.getAllConcepts()){
				Triplet<String, String, Double> t = new Triplet<>(Cj.sign(), Ci.sign(), (double) intersection(Ci.extensional_definition, Cj.extensional_definition).size() / Ci.extensional_definition.size());
				toSend.add(new Vote(State.ElectSignsState, t));
				System.out.println("   > The agent sends "+t.getRight()+" votes to name the concept "+t.getLeft()+" with the sign "+t.getMiddle());
			}
		}
		sendMessages(toSend);
		return State.ElectSignsState;
	}
	
	private State electSign(){
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
				K.getConcept(id).sign = new Sign(sign);
				H.getConcept(id).sign = new Sign(sign);
			}
			cleanMailbox(Performative.Replace);
			System.out.println("   > The new contrast set is: "+K.getAllConcepts());
			System.out.println("   > The new hypothesis is  : "+H.getAllConcepts());
			// Argumentation terminated normally, it counts as a success
			ExpFileManager.addBlock("success",1);
			return State.Stop;
		}
		// Seize the naming process
		List<Message> toSend = new ArrayList<>();
		toSend.add(new Seize(State.ElectSignsState));
		// Compute the vote
		Map<String,Map<String,Double>> vote = new HashMap<>();
		// Initialize the map
		for(Concept c : K.getAllConcepts()){
			vote.put(c.sign(), new HashMap<>());
		}
		// Get the votes from the other
		for(Message m : mail.getMessages(Performative.Elect)){
			mail.readMessage(m);
			Triplet<String, String, Double> t = mail.vote;
			Map<String, Double> v = vote.get(t.getLeft());
			v.putIfAbsent(t.getMiddle(), 0.);
			v.put(t.getMiddle(), v.get(t.getMiddle()) + t.getRight());
		}
		// Put own votes
		for(Concept Ci : Ki.getAllConcepts()){
			for(Concept Cj : K.getAllConcepts()){
				Map<String,Double> v = vote.get(Cj.sign());
				v.putIfAbsent(Ci.sign(), 0.);
				v.put(Ci.sign(), v.get(Ci.sign()) + (double) intersection(Ci.extensional_definition, Cj.extensional_definition).size() / Ci.extensional_definition.size());
			}
		}
		// For each past concept, replace a new concept's name
		HashSet<String> settledCases = new HashSet<>();
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
				toSend.add(new Replace(State.ElectSignsState, K.getConcept(s).id, winner));
				toSend.add(new Replace(State.ElectSignsState, H.getConcept(s).id, winner));
				K.getConcept(s).sign = new Sign(winner);
				H.getConcept(s).sign = new Sign(winner);
			}
		}
		cleanMailbox(Performative.Elect);
		System.out.println("   > The new contrast set is: "+K.getAllConcepts());
		System.out.println("   > The new hypothesis is  : "+H.getAllConcepts());
		// Send messages
		sendMessages(toSend);
		return State.Stop;
	}
	
	/**
	 * The final {@link State} of the argumentation. The script that instantiates the two {@link Agent}s can now terminate.
	 * @return the {@link State#Stop}
	 */
	public State stop(){
		return State.Stop;
	}
	
	public boolean sent(Set<Generalization> I) {
		for(Generalization g : I)
			if(g_exchanged.get(g.id) == null)
				return false;
		return true;
	}
	
	public ContrastSet Ki() {
		return this.Ki;
	}
	
	public ContrastSet K() {
		return this.K;
	}
	
	public ContrastSet H() {
		return this.H;
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
	 * Display an evaluation in the terminal.
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
	private Triplet<ConID, ConID, Relation> getRelation(ConID id1, ConID id2){
		Triplet<ConID, ConID, RTriplet> t = getOverallRTriplet(id1, id2);
		assert t != null;
		return new Triplet<>(id1, id2, agree(t.getRight().getTriplet()));
	}

	private Triplet<ConID, ConID,RTriplet> getOwnRTriplet(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, RTriplet> t : mRTriplets)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}

	private Triplet<ConID, ConID,RTriplet> getOtherRTriplet(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, RTriplet> t : oRTriplets)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}
	
	private Triplet<ConID, ConID,RTriplet> getOverallRTriplet(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, RTriplet> t : ovRTriplets)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}
	
	private Triplet<ConID, ConID, Hierarchy> getHierarchies(ConID id1, ConID id2){
		for(Triplet<ConID, ConID, Hierarchy> t : Hierarchies)
			if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
				return t;
		return null;
	}
	
	private Triplet<ConID, ConID, Relation> getDisagreement(ConID id1, ConID id2, Relation d){
		for(Triplet<ConID, ConID, Relation> t : getAllDisagreements()) {
			if (t.getRight() == d)
				if(id1.equals(t.getLeft()) && id2.equals(t.getMiddle()) || (id1.equals(t.getMiddle()) && id2.equals(t.getLeft())))
					return t;
		}
		System.out.println("        > couldn't find it in "+getAllDisagreements());
		return null;
	}
	
	private Set<Triplet<ConID, ConID, RTriplet>> getAllRTriplets(){
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

	private Concept getHyponym(ConID id1, ConID id2) {
		Triplet<ConID, ConID, Hierarchy> h = getHierarchies(id1, id2);
		assert h != null;
		if(h.getRight() == Hierarchy.Hyponymy)
			return getConcept(h.getLeft());
		else if(h.getRight() == Hierarchy.Hyperonymy)
			return getConcept(h.getMiddle());
		else
			return null;
	}
	
	private Concept getHypernym(ConID id1, ConID id2) {
		Triplet<ConID, ConID, Hierarchy> h = getHierarchies(id1, id2);
		if(h == null)
			System.out.println(Hierarchies);
		assert h != null;
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
		output.addAll(K.getAllConcepts());
		output.addAll(H.getAllConcepts());
		return output;
	}
	
	private Container getContainer(ConID id) {
		if(!K.getConcept(id).isNull())
			return K;
		if(!H.getConcept(id).isNull())
			return H;
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
	
	private void replaceConcept(Concept replaced, String replacing) {
		replaced.sign = new Sign(replacing);
	}
	
	// Evaluation functions
	public int[] evaluation(SemioticElement se1, SemioticElement se2){
		int A = Math.min(substract(se1.getExtension(K), se2.getExtension(K)).size(), THRESHOLD);
		int B = Math.min(substract(se2.getExtension(K), se1.getExtension(K)).size(), THRESHOLD);
		int AB = Math.min(intersection(se1.getExtension(K), se2.getExtension(K)).size(), THRESHOLD);
		return new int[]{A, AB, B};
	}
	
	public int[] evaluation(Set<SemioticElement> set1, Set<SemioticElement> set2) {
		HashSet<Example> tt1 = new HashSet<>();
		HashSet<Example> tt2 = new HashSet<>();
		for(SemioticElement se1 : set1)
			tt1.addAll(se1.getExtension(K));
		for(SemioticElement se2 : set2)
			tt2.addAll(se2.getExtension(K));
		int A = Math.min(substract(tt1, tt2).size(), THRESHOLD);
		int B = Math.min(substract(tt2, tt1).size(), THRESHOLD);
		int AB = Math.min(intersection(tt1, tt2).size(), THRESHOLD);
		return new int[]{A, AB, B};
	}

	public  int[] evaluation(SemioticElement se1, Set<SemioticElement> set2) {
		Set<SemioticElement> set1 = new HashSet<>(se1.getExtension(K));
		return evaluation(set1, set2);
	}

	public  int[] evaluation(Set<SemioticElement> set1, SemioticElement se2) {
		return evaluation(se2, set1);
	}	

	// Different agreement fonctions (see paper for more information)
	
	public Relation agree(int[] ev){
		if(ev[0] < 0 || ev[1] < 0 || ev[2] <0 )
			return null;
		if((ev[0] < THRESHOLD && ev[1] < THRESHOLD && ev[2] < THRESHOLD)|| (ev[0] >= THRESHOLD && ev[1] < THRESHOLD && ev[2] < THRESHOLD) || (ev[0] < THRESHOLD && ev[1] < THRESHOLD && ev[2] >= THRESHOLD))
			return Relation.Blind;
		if(ev[0] < THRESHOLD && ev[1] >= THRESHOLD && ev[2] < THRESHOLD)
			return Relation.Equivalence;
		if(ev[0] >= THRESHOLD && ev[1] < THRESHOLD && ev[2] >= THRESHOLD)
			return Relation.Disjunction;
		if((ev[0] < THRESHOLD && ev[1] >= THRESHOLD && ev[2] >= THRESHOLD) || (ev[0] >= THRESHOLD && ev[1] >= THRESHOLD && ev[2]< THRESHOLD))
			return Relation.Inclusion;
		if(ev[0] >= THRESHOLD && ev[1] >= THRESHOLD && ev[2] >= THRESHOLD)
			return Relation.Overlap;
		return null;
	}
	
	public Relation agree(SemioticElement se1, SemioticElement se2){
		int[] ev = evaluation(se1, se2);
		if(ev[0] < 0 || ev[1] < 0 || ev[2] <0 )
			return null;
		if((ev[0] < THRESHOLD && ev[1] < THRESHOLD && ev[2] < THRESHOLD)|| (ev[0] >= THRESHOLD && ev[1] < THRESHOLD && ev[2] < THRESHOLD) || (ev[0] < THRESHOLD && ev[1] < THRESHOLD && ev[2] >= THRESHOLD))
			return Relation.Blind;
		if(ev[0] < THRESHOLD && ev[1] >= THRESHOLD && ev[2] < THRESHOLD)
			return Relation.Equivalence;
		if(ev[0] >= THRESHOLD && ev[1] < THRESHOLD && ev[2] >= THRESHOLD)
			return Relation.Disjunction;
		if((ev[0] < THRESHOLD && ev[1] >= THRESHOLD && ev[2] >= THRESHOLD) || (ev[0] >= THRESHOLD && ev[1] >= THRESHOLD && ev[2]< THRESHOLD))
			return Relation.Inclusion;
		if(ev[0] >= THRESHOLD && ev[1] >= THRESHOLD && ev[2] >= THRESHOLD)
			return Relation.Overlap;
		return null;
	}
	
	public Relation agree(Set<SemioticElement> set1, Set<SemioticElement> set2) {
		int[] ev = evaluation(set1, set2);
		if(ev[0] < 0 || ev[1] < 0 || ev[2] <0 )
			return null;
		if((ev[0] < THRESHOLD && ev[1] < THRESHOLD && ev[2] < THRESHOLD)|| (ev[0] >= THRESHOLD && ev[1] < THRESHOLD && ev[2] < THRESHOLD) || (ev[0] < THRESHOLD && ev[1] < THRESHOLD && ev[2] >= THRESHOLD))
			return Relation.Blind;
		if(ev[0] < THRESHOLD && ev[1] >= THRESHOLD && ev[2] < THRESHOLD)
			return Relation.Equivalence;
		if(ev[0] >= THRESHOLD && ev[1] < THRESHOLD && ev[2] >= THRESHOLD)
			return Relation.Disjunction;
		if((ev[0] < THRESHOLD && ev[1] >= THRESHOLD && ev[2] >= THRESHOLD) || (ev[0] >= THRESHOLD && ev[1] >= THRESHOLD && ev[2]< THRESHOLD))
			return Relation.Inclusion;
		if(ev[0] >= THRESHOLD && ev[1] >= THRESHOLD && ev[2] >= THRESHOLD)
			return Relation.Overlap;
		return null;
	}

	public Relation agree(SemioticElement se1, Set<SemioticElement> set2) {
		Set<SemioticElement> set1 = new HashSet<>(se1.getExtension(K));
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

	private boolean isDisagreement(Triplet<ConID, ConID, Relation> t){
		if(t.getRight() == Relation.Overlap || t.getRight() == Relation.Inclusion || t.getRight() == Relation.Blind)
			return true;
		else if(t.getRight().equals(Relation.Equivalence) && !getConcept(t.getLeft()).sign().equals(getConcept(t.getMiddle()).sign()))
			return true;
		else return t.getRight().equals(Relation.Disjunction) && getConcept(t.getLeft()).sign().equals(getConcept(t.getMiddle()).sign());
	}
	
	private boolean isSelfDisagreement(Triplet<ConID, ConID, Relation> t) {
		return getContainer(t.getLeft()) == getContainer(t.getMiddle());
	}
	
	private boolean isSelfSelfDisagreement(Triplet<ConID, ConID, Relation> t) {
		if(!isDisagreement(t))
			return false;
		return getContainer(t.getLeft()) == K && getContainer(t.getMiddle()) == K;
	}
	
	private boolean isOtherSelfDisagreement(Triplet<ConID, ConID, Relation> t) {
		if(!isDisagreement(t))
			return false;
		return getContainer(t.getLeft()) == H && getContainer(t.getMiddle()) == H;
	}
	
	private boolean isMyBusiness() {
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
			return(getContainer(nonNull) == K);
		}
		RTriplet m_rt = requireNonNull(getOwnRTriplet(Ci, Cj)).getRight();
		RTriplet o_rt = requireNonNull(getOtherRTriplet(Ci, Cj)).getRight();
		boolean only_to_have_examples = (m_rt.getTriplet(Ci, Cj)[1] == THRESHOLD && o_rt.getTriplet(Ci, Cj)[1] < THRESHOLD);
		boolean have_the_examples = m_rt.getTriplet(Ci, Cj)[1] == THRESHOLD;
		boolean have_right_container = true;
		if(disagreement.getRight() == Relation.Inclusion) {
			boolean m_covers_side = (m_rt.getTriplet(Ci, Cj)[0] == THRESHOLD || m_rt.getTriplet(Ci, Cj)[2] == THRESHOLD);
			boolean o_covers_side = (o_rt.getTriplet(Ci, Cj)[0] == THRESHOLD || o_rt.getTriplet(Ci, Cj)[2] == THRESHOLD);
			only_to_have_examples = m_covers_side && !o_covers_side;
			have_the_examples = m_covers_side;
			ConID hypernym = requireNonNull(getHypernym(Ci, Cj)).id;
			if(getContainer(hypernym) == H)
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
	
	
	private void cleanDisagreements() {
		this.cleanDisagreement(SelfDisagreements);
		this.cleanDisagreement(SemanticDisagreements);
		this.cleanDisagreement(UntransDisagreements);
		this.cleanDisagreement(LexicalDisagreements);
	}
	
	private void cleanDisagreement(List<Triplet<ConID, ConID, Relation>> Disagreements){
		List<Triplet<ConID, ConID, Relation>> toDelet = new ArrayList<>();
		for(Triplet<ConID, ConID, Relation> t : Disagreements){
			if(!isDisagreement(t))
				toDelet.add(t);
		}
		Disagreements.removeAll(toDelet);
	}
	
	private void cleanMailbox(Performative performative){
		mail.clearPerformative(performative);
	}
	
	private List<Triplet<ConID,ConID,Relation>> getAllDisagreements(){
		List<Triplet<ConID,ConID,Relation>> out = new LinkedList<>();
		out.addAll(SelfDisagreements);
		out.addAll(SemanticDisagreements);
		out.addAll(UntransDisagreements);
		out.addAll(LexicalDisagreements);
		return out;
	}
	
	private void addDisagreement(Triplet<ConID, ConID, Relation> disagreement) {
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
	}
	
	private void removeDisagreement(Triplet<ConID, ConID, Relation> disagreement) {
		if(disagreement == null) {
			System.out.println("     > no disgreement to remove.");
			return;
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
	}
	
	// Get the list of untranslatable concepts
	private List<Triplet<ConID,ConID,Relation>> findUntranslatable(){
		// Prepare output
		List<Triplet<ConID,ConID,Relation>> out = new ArrayList<>();
		// For each concept of contrast set, see if we have a relation of equivalence
		for(Concept C1 : K.getAllConcepts()) {
			boolean hasEquivalent = false;
			for(Concept C2 : H.getAllConcepts()) {
				if(getRelation(C1.id, C2.id).getRight() == Relation.Equivalence) {
					hasEquivalent = true;
					break;
				}
			}
			if(!hasEquivalent)
				out.add(new Triplet<>(C1.id, new NullConID(), Relation.Untrans));
		}
		// For each concept of contrast set, see if we have a relation of equivalence
		for (Concept C1 : H.getAllConcepts()) {
			boolean hasEquivalent = false;
			for (Concept C2 : K.getAllConcepts()) {
				if (getRelation(C1.id, C2.id).getRight() == Relation.Equivalence) {
					hasEquivalent = true;
					break;
				}
			}
			if (!hasEquivalent)
				out.add(new Triplet<>(new NullConID(), C1.id, Relation.Untrans));
		}
		return out;
	}
	
	public Hierarchy hierarchyKind(int[] ev){
		if(ev[0] >= THRESHOLD && ev[1] >= THRESHOLD && ev[2] < THRESHOLD)
			return Hierarchy.Hyperonymy;
		if(ev[0] < THRESHOLD && ev[1] >= THRESHOLD && ev[2] >= THRESHOLD)
			return Hierarchy.Hyponymy;
		return Hierarchy.Blind;
	}
	
	private Collection<Example> requestExamples(ConID id1, ConID id2, int request){
		// Get intensional definitions
		System.out.println("            > Request for examples involving "+id1+" and "+id2);
		Set<Generalization> idef1 = getConcept(id1).intensional_definition;
		Set<Generalization> idef2 = getConcept(id2).intensional_definition;
		return requestExamples(idef1, idef2, request);
	}
	
	private Collection<Example> requestExamples(Set<Generalization> m_i_def, Set<Generalization> o_i_def, int request){
		Set<Example> S1 = adjunctSet(m_i_def, K.context);
		Set<Example> S2 = adjunctSet(o_i_def, K.context);
		Collection<Example> candidates;
		Collection<Example> out = new HashSet<>();
		switch (request) {
		// Example covered by our concept and not covered by the other's concept
		case 0:
			candidates = substract(S1, S2);
			if(candidates.size() <= THRESHOLD)
				System.out.println("               > Not enough examples in the concept");
			out = new HashSet<>(optiRandomSubset(this,candidates, THRESHOLD));
			System.out.println("               > Sending "+out.size()+" examples that belong to our definition but not to the other's definiton");
			break;
		// Example covered by our concept and covered by the other's concept
		case 1:	
			candidates = intersection(S1, S2);
			if(candidates.size() <= THRESHOLD)
				System.out.println("               > Not enough examples in the concept");
			out = new HashSet<>(optiRandomSubset(this,candidates, THRESHOLD));
			System.out.println("               > Sending "+out.size()+" examples that belong to our definition and to the other's defintion");
			break;
		// Example not covered by our concept and covered by the other's concept
		case 2:
			candidates = substract(S2, S1);
			if(candidates.size() <= THRESHOLD)
				System.out.println("               > Not enough examples in the concept");
			out = new HashSet<>(optiRandomSubset(this,candidates, THRESHOLD));
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
