package interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import argumentation.Argumentation;
import containers.ContrastSet;
import csic.iiia.ftl.base.core.FeatureTerm;
import enumerators.Hierarchy;
import enumerators.Relation;
import enumerators.State;
import identifiers.ConID;
import identifiers.GenID;
import messages.AbstractMessage;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.Triplet;

/**
 * An {@link Agent} is an entity that can represent knowledge with {@link SemioticElement}s, exchange {@link AbstractMessage}s and have an {@link Argumentation}.
 * 
 * @author kemoadrian
 *
 */
public interface Agent {
	
	// Name (for Debug purpose)
	public String nick();
	
	// Containers
	public ContrastSet Ki();
	public ContrastSet K();
	public ContrastSet H();
	
	// Current step of the argumentation
	public State state();
	
	// Messages

	/**
	 * Add a list of messages to add the mailbox of the {@link Agent}.
	 * 
	 * @param mail A {@link List} of {@link AbstractMessage}
	 */
	public void getMessages(List<? extends Message> mail);

	/**
	 * Add a list of messages to the mailbox of the other {@link Agent}.
	 * 
	 * @param mail A {@link List} of {@link AbstractMessage}
	 */
	public void sendMessages(List<? extends Message> mail);

	/**
	 * Get the messages in the mailbox of an agent
	 * @return a {@link List} of {@link AbstractMessage} that is the current mail of the agent
	 */
	public List<? extends Message> getMail();
	
	// Argumentation
	
	/**
	 * Current disagreement that the {@link Agent} is investigating.
	 * @return a {@link Triplet} figuring the current disagreement that this {@link Agent} is investigating. These three elements are respectively the two {@link ConID} of the {@link Concept}s involved in the disagreement, and the {@link Relation} between these two concepts.
	 */
	public Triplet<ConID,ConID,Relation> disagreement();
	
	/**
	 * {@link Example}s previously exchanged by the {@link Agent}s.
	 * @return a {@link Set} of {@link Example}s
	 */
	public Set<Example> e_exchanged();
	
	/**
	 * {@link Generalization}s previously exchanged by the {@link Agent}s.
	 * @return a {@link Set} of {@link Generalization}s
	 */
	public Map<GenID,Generalization> g_exchanged();
	
	/**
	 * Add {@link Example}s to the set of {@link Example}s marked as exchanged between the {@link Agent}s.
	 * @param S a {@link Set} of {@link Example}s
	 */
	public void addEExchanged(Collection<Example> S);
	
	/**
	 * Add {@link Generalization}s to the set of {@link Generalization}s marked as exchanged between the {@link Agent}s.
	 * @param S a {@link Set} of {@link Generalization}s
	 */
	public void addGExchanged(Collection<Generalization> S);
	
 	
	// Evaluation functions
	/**
	 * Compute the r-triplet of two {@link SemioticElement}s
	 * @param s1
	 * @param s2
	 * @return
	 */
	public int[] evaluation(SemioticElement s1, SemioticElement s2);
	public int[] evaluation(Set<SemioticElement> S, SemioticElement s);
	public int[] evaluation(SemioticElement s, Set<SemioticElement> S);
	public int[] evaluation(Set<SemioticElement> S1, Set<SemioticElement> S2);
	
	public String display(int[] evaluation);

	// Agreement functions
	public List<String> name(Example e, Container K);
	public Concept getConcept(ConID id);
	public Relation agree(int[] evaluation);

	/**
	 * Agreement and semiotic functions (gives the relation between two concepts.
	 * with a modality of {@link Relation})
	 * 
	 * @param se1 a {@link SemioticElement}
	 * @param se2 a {@link SemioticElement}
	 * @return the {@link Relation} modality of the agent over these semiotic
	 *         elements.
	 */
	public Relation agree(SemioticElement se1, SemioticElement se2);

	/**
	 * @param se1  a {@link SemioticElement}
	 * @param set2 a {@link Set} of {@link SemioticElement}
	 * @return the {@link Relation} modality of the agent over these semiotic
	 *         elements.
	 */
	public Relation agree(SemioticElement se1, Set<SemioticElement> set2);

	/**
	 * @param set1 a {@link Set} of {@link SemioticElement}
	 * @param se2  a {@link SemioticElement}
	 * @return the {@link Relation} modality of the agent over the semiotic element
	 *         and the set of semiotic elements.
	 */
	public Relation agree(Set<SemioticElement> set1, SemioticElement se2);

	/**
	 * @param set1 a {@link Set} of {@link SemioticElement}
	 * @param set2 a {@link Set} of {@link SemioticElement}
	 * @return the {@link Relation} modality of the agent over the semiotic element
	 *         and the set of semiotic elements.
	 */
	public Relation agree(Set<SemioticElement> set1, Set<SemioticElement> set2);
	
	// Adjunct sets
	public Set<Example> adjunctSet(Generalization g, Set<Example> context);
	public Set<Example> adjunctSet(Set<Generalization> I, Set<Example> context);
	
	// Hierarchies
	public Hierarchy hierarchyKind(int[] evaluation);

	// Machine Learning tools

	/**
	 * Execute the turn of the {@link Agent}
	 * 
	 * @return the {@link State} of the {@link Agent} for the next turn
	 */
	public State turn();

	/**
	 * Initialize the {@link Agent}.
	 * 
	 * @param data_set the {@link List} of {@link FeatureTerm} used for the initial
	 *                 training.
	 * @return <tt>true</tt> if the {@link ContrastSet} has been correctly
	 *         initialized.
	 */
	public boolean initialize(List<FeatureTerm> data_set);

	/**
	 * Use a data set to create the initial {@link ContrastSet} of the
	 * {@link Agent}.
	 * 
	 * @param data_set the {@link List} of {@link FeatureTerm} used for the
	 *                 learning.
	 * @return a new {@link ContrastSet} that partitions that data set.
	 */
	public ContrastSet learn(List<FeatureTerm> data_set);

}
