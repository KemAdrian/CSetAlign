package interfaces;

import java.util.Map;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.base.core.FeatureTerm;
import identifiers.ArgID;
import identifiers.GenID;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.Test;

public interface Node {
	
	// Get the id of this node
	public ArgID getid();
	// Get the id of the node that this nod is attacking
	public ArgID attacks();
	// Get the agent that created this node
	public String getAgent();
	// Get the label of the elements that the node is supposed to cover
	public FeatureTerm getLabel();
	// Transform node into semiotic elements
	public Set<Example> toExamples();
	public Set<Argument> toArguments();
	public Set<Generalization> toGeneralizations();
	// Obtaining the four predictive values
	public Set<Example> covered_ex(Map<FeatureTerm, Set<Example>> context);
	public Set<Example> uncovered_ex(Map<FeatureTerm, Set<Example>> context);
	public Set<Example> positive_ex(Map<FeatureTerm, Set<Example>> context);
	public Set<Example> negative_ex(Map<FeatureTerm, Set<Example>> context);
	// Generate an array of attacks against this node
	public Node replace(Set<Argument> arguments, Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn, Agent agent);
	public Set<Node> attack(Map<FeatureTerm, Set<Example>> context, Set<Argument> accepted_arguments, Set<Node> past_attacks, int maxfp, int maxfn, Agent agent);
	// Testing acceptability
	public Test classification(Map<FeatureTerm, Set<Example>> context);
	public Test classification(Generalization g, Map<FeatureTerm, Set<Example>> context);
	public Test classification(Set<Generalization> I, Map<FeatureTerm, Set<Example>> context);
	public boolean acceptable(Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn);
	// Fill or empty the node
	public boolean filled();
	public boolean fill(Map<GenID,Generalization> exchanged);
	public void empty(Map<GenID,Generalization> exchanged);
	public Node clone();

}
