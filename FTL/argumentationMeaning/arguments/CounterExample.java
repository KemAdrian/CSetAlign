package arguments;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.base.core.FeatureTerm;
import identifiers.ArgID;
import identifiers.GenID;
import interfaces.Agent;
import interfaces.Node;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.ToolSet;

public class CounterExample extends TreeNode {
	
	public ArgID attack;
	public Set<Example> examples;
	
	public ArgID getid() {
		return id;
	}
	
	public String getAgent() {
		return agent_nick;
	}
	
	public FeatureTerm getLabel() {
		return overall_set_label;
	}
	
	public ArgID attacks() {
		return attack;
	}
	
	public boolean filled() {
		return true;
	}
	
	public static class Builder {
		// Own variables
		public Set<Example> examples;
		
		// Inherited variables
		public ArgID id;
		public ArgID attack;
		public String agent_nick;
		public FeatureTerm overall_set_label;
		
		// Builders
		public Builder() {
			this.id = new ArgID();
			this.agent_nick = "Unknown";
			this.examples = new HashSet<>();
		}
		
		public Builder as(ArgID id) {
			this.id = id;
			return this;
		}
		
		public Builder from(String nick) {
			this.agent_nick = nick;
			return this;
		}
		
		public Builder against(ArgID attack) {
			this.attack = attack;
			return this;
		}
		
		public Builder labelled(FeatureTerm label) {
			this.overall_set_label = label;
			return this;
		}
		
		public Builder withExamples(Collection<Example> examples) {
			this.examples.addAll(examples);
			return this;
		}
		
		public CounterExample build() {
			CounterExample c_ex = new CounterExample();
			// Fill standard variables
			c_ex.id = this.id;
			c_ex.attack = this.attack;
			c_ex.agent_nick = this.agent_nick;
			c_ex.overall_set_label = this.overall_set_label;
			c_ex.examples = this.examples;
			return c_ex;
		}
		
	}
	
	private CounterExample() {
		// Use builder instead
		assert true;
	}
	
	public boolean fill(Map<GenID,Generalization> exchanged) {
		System.out.println("       > Cannot fill counter example...");
		// Do nothing
		return true;
	}
	
	public void empty(Map<GenID,Generalization> exchanged) {
		System.out.println("       > Cannot empty counter example...");
		// Do nothing
		assert true;
	}
	
	public Set<Example> covered_ex(Map<FeatureTerm, Set<Example>> context){
		return examples;
	}
	
	public Set<Example> uncovered_ex(Map<FeatureTerm, Set<Example>> context){
		Set<Example> output = new HashSet<>();
		for(Set<Example> set : context.values()) {
			output.addAll(set);
		}
		return new HashSet<Example>(ToolSet.substract(output, examples));
	}
	
	public Node replace(Set<Argument> arguments, Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn, Agent agent) {
		System.out.println("     > Problem: you are trying to replace a counter example. Do not do that. That's not how things work.");
		return this;
	}
	
	public boolean acceptable(Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn) {
		return classification(context).fp <= maxfp;
	}
	
	public Set<Node> attack(Map<FeatureTerm, Set<Example>> context, Set<Argument> accepted_arguments, Set<Node> past_attacks, int maxfp, int maxfn, Agent agent){
		System.out.println("     > Problem: this is a counter example and should not be attacked");
		return new HashSet<Node>();
	}
	
	public Set<Generalization> toGeneralizations() {
		return new HashSet<Generalization>();
	}
	
	public Set<Argument> toArguments() {
		return new HashSet<Argument>();
	}
	
	public Set<Example> toExamples(){
		return new HashSet<>(examples);
	}
	
	public CounterExample clone() {
		return new CounterExample.Builder()
				.as(this.id)
				.against(this.attack)
				.from(this.agent_nick)
				.labelled(this.overall_set_label)
				.withExamples(new HashSet<>(examples))
				.build();
	}

}
