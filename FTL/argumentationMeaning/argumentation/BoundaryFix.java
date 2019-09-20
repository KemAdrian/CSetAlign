package argumentation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import csic.iiia.ftl.learning.lazymethods.similarity.AUDistance;
import csic.iiia.ftl.learning.lazymethods.similarity.Distance;
import enumerators.FixPhase;
import enumerators.Relation;
import enumerators.State;
import identifiers.ConID;
import interfaces.Agent;
import interfaces.Message;
import messages.Baptise;
import messages.Seize;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.LPkg;
import tools.ToolSet;
import tools.Triplet;

public class BoundaryFix {
	
	// Agent
	public Agent agent;
	
	// Communication
	public Integer nb_loop;
	public Boolean exempted;
	public Boolean id_seized;
	public FixPhase current_phase;
	public Triplet<ConID, ConID, Relation> selfD;

	// Containers
	public Argumentation argumentation;
	
	// AMAIL stuff
	public Distance distance;
	public Set<Example> positiveExamples;
	public Set<Example> negativeExamples;
	
	// Outputs
	public ConID first_id;
	public ConID second_id;
	public Set<Generalization> first_ID;
	public Set<Generalization> second_ID;
	
	public BoundaryFix(Agent a) {
		// Agent
		this.agent = a;
		// Communication
		this.nb_loop = 0;
		this.id_seized = false;
		this.current_phase = FixPhase.ID;
		this.selfD = a.disagreement();
		// Containers
		this.argumentation = new Argumentation(this.agent);
		// AMAIL Stuff
		this.distance = new AUDistance();
		this.positiveExamples = new HashSet<>();
		this.negativeExamples = new HashSet<>();
	}
	
	// Find the new intensional definition (main function)
	public boolean solve() {
		switch (current_phase) {
		case ID :
			this.current_phase = createID();
		case Ext:
			this.current_phase = createExtensionalDefinition();
			break;
		case Loop:
			this.current_phase = haveArgumentation();
			break;
		case Stop:
			this.current_phase = FixPhase.Stop;
		default:
			break;
		}
		return solved();
	}
	
	public FixPhase createID() {
		// Messages to send
		List<Message> toSend = new ArrayList<>();
		if(!id_seized) {
			first_id =  new ConID();
			second_id = new ConID();
			toSend.add(new Baptise(State.FixBoundariesState, first_id, 0));
			toSend.add(new Baptise(State.FixBoundariesState, second_id, 1));
			toSend.add(new Seize(State.FixBoundariesState));
		}
		// Send messages
		agent.sendMessages(toSend);
		return FixPhase.Ext;
	}
	
	// Create the extensional definitions (sets of positive and negative examples)
	public FixPhase createExtensionalDefinition() {
		// Exempt argumentation
		argumentation.exempted = exempted;
		// Get the two concepts involved
		Concept c1 = (selfD.getLeft().id < selfD.getMiddle().id)? agent.getConcept(selfD.getLeft()) : agent.getConcept(selfD.getMiddle());
		Concept c2 = (selfD.getLeft().id > selfD.getMiddle().id)? agent.getConcept(selfD.getLeft()) : agent.getConcept(selfD.getMiddle());
		System.out.println("     > The concepts involved are "+c1+" first, and "+c2+" second.");
		Set<Example> adj_1 = agent.adjunctSet(c1.intensional_definition, agent.K().context);
		Set<Example> adj_2 = agent.adjunctSet(c2.intensional_definition, agent.K().context);
		// Create set of positive examples (without controversial examples)
		if(nb_loop == 0) {
			System.out.println("    > creating the new extensional definition of "+c1.sign()+" by selecting its proper examples");
			positiveExamples.addAll(ToolSet.substract(adj_1, adj_2));
			// Add controversial examples
			System.out.println("    > redistributing the "+ToolSet.intersection(adj_1, adj_2).size()+" examples that belong to both concepts");
			for(Example e : ToolSet.intersection(adj_1, adj_2)) {
				if(closestConcept(c1, c2, e) == c1) {
					System.out.println("      > One example added");
					positiveExamples.add(e);
				}
			}
		}
		else if(nb_loop == 1) {
			System.out.println("    > creating the new extensional definition of "+c2.sign()+" by selecting its proper examples");
			positiveExamples.addAll(ToolSet.substract(adj_2, adj_1));
			// Add controversial examples
			System.out.println("    > redistributing the "+ToolSet.intersection(adj_1, adj_2).size()+" examples that belong to both concepts");
			for(Example e : ToolSet.intersection(adj_1, adj_2)) {
				if(closestConcept(c1, c2, e) == c2) {
					System.out.println("      > One example added");
					positiveExamples.add(e);
				}
			}
		}
		else {
			System.out.println("      > Problem: Wrong phase in input (should be initial or second)");
		}
		// Create set of negative examples
		negativeExamples.addAll(ToolSet.substract(agent.K().context, positiveExamples));
		System.out.println("     > Learning intensional definition on "+positiveExamples.size()+" positive examples and "+negativeExamples.size()+" negative examples");
		// Seting up the min and max number of examples (we assume a max_fp << size of initial concepts)
		argumentation.MAX_FP =
				argumentation.MMAX_FN = 
						argumentation.OMAX_FN = (int) Math.floor(ToolSet.THRESHOLD / 4);
		// Set up examples
		argumentation.setUpExamples(positiveExamples, negativeExamples);
		return FixPhase.Loop;
	}
	
	// Create the intensional definitions for the concept that was involved in a self disagreement
	public FixPhase haveArgumentation() {
		// Have the first turn of argumentation
		argumentation.solve();
		// Exit condition
		if(argumentation.solved) {
			if(nb_loop == 0) {
				// Save IDef
				this.first_ID = argumentation.getIdef();
				// Containers
				this.argumentation = new Argumentation(this.agent);
				// AMAIL Stuff
				this.positiveExamples = new HashSet<>();
				this.negativeExamples = new HashSet<>();
				nb_loop ++;
				return FixPhase.Ext;
			}
			this.second_ID = argumentation.getIdef();
			return FixPhase.Stop;
		}
		return FixPhase.Loop;
	}
	
	// Get if solved
	public boolean solved() {
		return this.current_phase == FixPhase.Stop;
	}
	
	// Gives the closest belief to an example
	public Concept closestConcept(Concept c1, Concept c2, Example e) {
		double d1 = e_i_distance(c1.intensional_definition, e);
		double d2 = e_i_distance(c2.intensional_definition, e);
		// Choose smallest distance
		Concept out = (d1 <= d2)? c1 : c2;
		return out;
	}
	
	// Gives the distance between a set of generalizations and an example
	public double e_i_distance(Set<Generalization> idef, Example e) {
		double num = 0;
		double denom = 0;
		for(Generalization g : idef) {
			try {
				num += distance.distance(g.generalization, e.featureterm, LPkg.ontology(), LPkg.dm());
				denom ++;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return num/denom;
	}

}
