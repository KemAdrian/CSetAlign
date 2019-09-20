package arguments;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.learning.core.Rule;
import identifiers.ArgID;
import interfaces.Node;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.Test;
import tools.ToolSet;

public abstract class TreeNode implements Node{
	
	public static int COUNTER_ARG = 1;
	public ArgID id;
	public String agent_nick;
	public FeatureTerm overall_set_label;
	
	public Set<Example> positive_ex(Map<FeatureTerm, Set<Example>> context){
		return context.get(overall_set_label);
	}
	
	public Set<Example> negative_ex(Map<FeatureTerm, Set<Example>> context){
		Set<Example> output = new HashSet<>();
		for(FeatureTerm f : context.keySet()) {
			if(!f.equals(overall_set_label))
				output.addAll(context.get(f));
		}
		return output;
	}
	
	public Set<Example> all_ex(Map<FeatureTerm, Set<Example>> context){
		Set<Example> output = new HashSet<>();
		for(Set<Example> value : context.values())
			output.addAll(value);
		return output;
	}

	// Get adjunct set of a generalization
	public Set<Example> adjunctSet(Generalization g, Set<Example> context) {
		Set<Example> output = new HashSet<>();
		for (Example e : context)
			if (g.generalizes(e))
				output.add(e);
		return output;
	}
	
	// Get adjunct set of a set of generalization
	public Set<Example> adjunctSet(Set<Generalization> I, Set<Example> context) {
		Set<Example> output = new HashSet<>();
		for(Generalization g : I)
			output.addAll(adjunctSet(g, context));
		return new HashSet<Example>(ToolSet.cleanDuplicates(output));
	}
	
	public Test classification(Map<FeatureTerm, Set<Example>> context) {
		// Set of examples
		 Set<Example> covered = covered_ex(context),
				 uncovered = uncovered_ex(context),
				 positives = positive_ex(context),
				 negatives = negative_ex(context);
		// Scores
		int t_p = ToolSet.intersection(positives, covered).size(),
				t_n = ToolSet.intersection(negatives, uncovered).size(),
				f_p = ToolSet.intersection(negatives, covered).size(),
				f_n = ToolSet.intersection(positives, uncovered).size();
		return new Test(t_p,f_p,t_n,f_n);
	}
	
	public Test classification(Generalization g, Map<FeatureTerm, Set<Example>> context) {
		Set<Example> covered = new HashSet<>(),
				uncovered = new HashSet<>(),
				positives = positive_ex(context),
				negatives = negative_ex(context);
		for(Set<Example> set : context.values()) {
			covered.addAll(adjunctSet(g, set));
			uncovered.addAll(ToolSet.substract(set, covered));
		}
		// Scores
		int t_p = ToolSet.intersection(positives, covered).size(),
				t_n = ToolSet.intersection(negatives, uncovered).size(),
				f_p = ToolSet.intersection(negatives, covered).size(),
				f_n = ToolSet.intersection(positives, uncovered).size();
		return new Test(t_p,f_p,t_n,f_n);
	}
	
	public Test classification(Set<Generalization> I, Map<FeatureTerm, Set<Example>> context) {
		Set<Example> covered = new HashSet<>(),
				uncovered = new HashSet<>(),
				positives = positive_ex(context),
				negatives = negative_ex(context);
		for(Set<Example> set : context.values()) {
			covered.addAll(adjunctSet(I, set));
			uncovered.addAll(ToolSet.substract(set, covered));
		}
		// Scores
		int t_p = ToolSet.intersection(positives, covered).size(),
				t_n = ToolSet.intersection(negatives, uncovered).size(),
				f_p = ToolSet.intersection(negatives, covered).size(),
				f_n = ToolSet.intersection(positives, uncovered).size();
		return new Test(t_p,f_p,t_n,f_n);
	}
	
	public Argument generalizationToArgument(Generalization g, FeatureTerm token) {
		// Get the rule from the generalization
		Rule arg_rule = new Rule(g.generalization, token);
		// Put the rule in a new argument
		Argument argument = new Argument(arg_rule);
		// Save the id of the generalization
		argument.id = g.id;
		// Add the argument to the output set
		return argument;
	}
	
	public Generalization argumentToGeneralization(Argument a){
		// Get the rule from the argument
		Rule arg_rule = a.m_rule;
		// Put the feature term in a new generalization
		Generalization g = new Generalization(arg_rule.pattern);
		// Put the id of the argument if an id has been saved
		if(a.id != null)
			g.id = a.id;
		return g;
	}
	
	public FeatureTerm oppositeLabel(Set<FeatureTerm> labels) {
		FeatureTerm output = null;
		if(labels.size() != 2 || !labels.contains(overall_set_label)) {
			System.out.println("       > The set of labels is invalid");
			return output;
		}
		for(FeatureTerm f : labels) {
			if(!f.equals(overall_set_label)) {
				return f;
			}
		}
		return output;
	}
	
	public TreeNode clone() {
		return null;
	}

}
