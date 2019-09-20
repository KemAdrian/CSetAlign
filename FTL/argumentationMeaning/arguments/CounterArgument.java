package arguments;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import csic.iiia.ftl.argumentation.core.ABUI;
import csic.iiia.ftl.argumentation.core.Argument;
import csic.iiia.ftl.base.core.FeatureTerm;
import identifiers.ArgID;
import identifiers.GenID;
import interfaces.Agent;
import interfaces.ArgNode;
import interfaces.Node;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.FTConv;
import tools.ToolSet;

public class CounterArgument extends TreeNode implements ArgNode {
	
	public ArgID attack;
	public Map<GenID,Generalization> generalizations;
	public boolean filled = true;
	
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
		return this.filled;
	}
	
	
	public Set<Generalization> generalizations(){
		return new HashSet<>(getFullGeneralizations());
	}
	
	public static class Builder {
		// Own variables
		public boolean filled = true;
		public Map<GenID, Generalization> generalizations;
		
		// Inherited variables
		public ArgID id;
		public ArgID attack;
		public String agent_nick;
		public FeatureTerm overall_set_label;
		
		// Builders
		public Builder() {
			this.id = new ArgID();
			this.agent_nick = "Unknown";
			this.filled = true;
			this.generalizations = new HashMap<GenID, Generalization>();
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
		
		public Builder withGenIDs(Collection<GenID> genIds) {
			for(GenID id : genIds)
				this.generalizations.put(id, null);
			return this;
		}
		
		public Builder withGeneralizations(Collection<Generalization> generalizations) {
			for(Generalization g : generalizations)
				this.generalizations.put(g.id, g);
			return this;
		}
		
		public CounterArgument build() {
			CounterArgument c_arg = new CounterArgument();
			// Fill standard variables
			c_arg.id = this.id;
			c_arg.attack = this.attack;
			c_arg.agent_nick = this.agent_nick;
			c_arg.filled = this.filled;
			c_arg.overall_set_label = this.overall_set_label;
			c_arg.generalizations = this.generalizations;
			// Check if filled
			c_arg.filled = c_arg.getEmptyGeneralizations().isEmpty();
			return c_arg;
		}
		
	}
	
	private CounterArgument() {
		// Use builder instead
		assert true;
	}
	
	private Collection<GenID> getEmptyGeneralizations() {
		Set<GenID> out = new HashSet<>();
		for(Entry<GenID,Generalization> entry : generalizations.entrySet())
			if(entry.getValue() == null)
				out.add(entry.getKey());
		return out;
	}
	
	private Collection<Generalization> getFullGeneralizations() {
		Set<Generalization> out = new HashSet<>();
		for(Entry<GenID,Generalization> entry : generalizations.entrySet())
			if(entry.getValue() != null)
				out.add(entry.getValue());
		return out;
	}
	
	public Collection<Generalization> getValidPart(Collection<Argument> arguments, Map<FeatureTerm, Set<Example>> context){
		Set<Generalization> out = new HashSet<>(generalizations());
		// Test for argument coverage
		for(Generalization g : new LinkedList<>(out)) {
			for(Argument arg : arguments) {
				if(g.generalizes(argumentToGeneralization(arg)) && !arg.m_rule.solution.equivalents(overall_set_label)) {
					out.remove(g);
					break;
				}
			}
		}
		// Test for false positive coverage
		Set<Example> f_positives = new HashSet<>(ToolSet.intersection(negative_ex(context), covered_ex(context)));
		for(Generalization g : new LinkedList<>(out)) {
			if(!adjunctSet(g, f_positives).isEmpty())
				out.remove(g);
		}
		return out;
	}
	
	public Collection<Generalization> getInvalidPart(Collection<Argument> arguments, Map<FeatureTerm, Set<Example>> context){
		Set<Generalization> out = new HashSet<>(generalizations());
		out.removeAll(getValidPart(arguments, context));
		return out;
	}
	
	public Node replace(Set<Argument> arguments, Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn, Agent agent) {
		System.out.println("     > Replacing the attack...");
		// Reuse valid part
		Set<Generalization> reused = new HashSet<>(getValidPart(arguments, context));
		System.out.println("       > Reusing "+reused.size()+"/"+generalizations().size()+" generalizations...");
		// Subsume the examples that are positives but not subsumed by the reused generalizations
		Map<FeatureTerm, Set<Example>> local_context = new HashMap<>();
		local_context.put(overall_set_label, new HashSet<>(ToolSet.substract(positive_ex(context), adjunctSet(reused, positive_ex(context)))));
		local_context.put(oppositeLabel(context.keySet()), new HashSet<>(ToolSet.substract(all_ex(context), local_context.get(overall_set_label))));
		Set<Generalization> news = ABUI.learnConcept(FTConv.contextToLearningSet(local_context), arguments, overall_set_label);
		System.out.println("       > Adding "+news.size()+" newly learned generalizations...");
		// Prepare outputs
		Set<Generalization> argument = new HashSet<>();
		Set<Example> example = new HashSet<>();
		argument.addAll(reused);
		argument.addAll(news);
		// If counter argument is not enough, replace by counter example instead
		Set<Example> remaining = new HashSet<>(ToolSet.substract(ToolSet.intersection(covered_ex(context), negative_ex(context)), adjunctSet(argument, all_ex(context))));
		if(remaining.size() > maxfp) {
			System.out.println("         > Generalizations missed "+remaining.size()+" examples, sending counter example instead...");
			argument.clear();
			example.addAll(ToolSet.optiRandomSubset(agent, ToolSet.intersection(covered_ex(context), negative_ex(context)), maxfp + 1));
			example = new HashSet<>(ToolSet.substract(example, agent.e_exchanged()));
		}
		if(!argument.isEmpty())
			return new CounterArgument.Builder().from(agent_nick).against(attack).labelled(overall_set_label).withGeneralizations(argument).build();
		return new CounterExample.Builder().from(agent_nick).against(attack).labelled(overall_set_label).withExamples(example).build();
	}
	
	public boolean acceptable(Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn) {
		return classification(context).fp <= maxfp;
	}
	
	public Set<Node> attack(Map<FeatureTerm, Set<Example>> context, Set<Argument> accepted_arguments, Set<Node> past_attacks, int maxfp, int maxfn, Agent agent){
		Set<Node> out = new HashSet<>();
		// Check false positives
		if(classification(context).fp > maxfp) {
			System.out.println("       > High number of false positives, generating attack...");
			// Look for positive attacks
			Set<Generalization> argument = new HashSet<>();
			Set<Example> example  =new HashSet<>();
			// Check for past attack
			Node past_attack = null;
			for(Node n : past_attacks)
				if(id.equals(n.attacks()) && n.getLabel().equivalents(oppositeLabel(context.keySet())))
					past_attack = n;
			// If found, update it
			if(past_attack != null) {
				System.out.println("         > Found past attack, modifying it...");
				argument.addAll(past_attack.replace(accepted_arguments, context, maxfp, maxfn, agent).toGeneralizations());
			}
			// Else, create new attack
			else {
				System.out.println("         > Didn't find past attack to modify, creating a new one...");
				// Define the attack context
				Map<FeatureTerm, Set<Example>> local_context = new HashMap<>();
				local_context.put(oppositeLabel(context.keySet()), new HashSet<>(ToolSet.intersection(covered_ex(context), negative_ex(context))));
				local_context.put(overall_set_label, new HashSet<>(ToolSet.substract(all_ex(context), local_context.get(oppositeLabel(context.keySet())))));
				// Try to create a counter-argument
				System.out.println("           > "+getInvalidPart(accepted_arguments, context).size()+"/"+generalizations().size()+" invalid generalizations to defeat...");
				for(Generalization g : getInvalidPart(accepted_arguments, context)) {
					Argument arg = ABUI.counterArgue(generalizationToArgument(g, overall_set_label), accepted_arguments, FTConv.contextToLearningSet(local_context));
					if(arg != null)
						argument.add(argumentToGeneralization(arg));
				}
			}
			// Send counter examples if counter arguments are insufficient
			Set<Example> remaining = new HashSet<>(ToolSet.substract(ToolSet.intersection(covered_ex(context), negative_ex(context)), adjunctSet(argument, all_ex(context))));
			if(remaining.size() > maxfp) {
				System.out.println("         > Generalizations missed "+remaining.size()+" examples, sending counter example instead...");
				argument.clear();
				example.addAll(ToolSet.optiRandomSubset(agent, ToolSet.intersection(covered_ex(context), negative_ex(context)), maxfp + 1));
				example = new HashSet<>(ToolSet.substract(example, agent.e_exchanged()));
			}
			// Add attacks to output
			if(!argument.isEmpty())
				out.add(new CounterArgument.Builder().from(agent.nick()).against(id).labelled(oppositeLabel(context.keySet())).withGeneralizations(argument).build());
			if(!example.isEmpty())
				out.add(new CounterExample.Builder().from(agent.nick()).against(id).labelled(oppositeLabel(context.keySet())).withExamples(example).build());
		}
		System.out.println("     > Created "+out.size()+" counter arguments");
		return out;
	}
	
	
	public boolean fill(Map<GenID,Generalization> exchanged) {
		Set<GenID> ids = generalizations.keySet();
		System.out.println("       > Trying to fill "+ids.size()+" generalizations...");
		for(GenID id : ids) {
			Generalization g = exchanged.get(id);
			if(g == null)
				return false;
			generalizations.put(id, g);
		}
		this.getEmptyGeneralizations().isEmpty();
		System.out.println("         > Filled "+getFullGeneralizations().size()+"/"+generalizations.size()+" generalizations");
		return filled;
	}
	
	public void empty(Map<GenID,Generalization> exchanged) {
		System.out.println("       > Trying to empty "+generalizations.size()+" generalizations...");
		int count = 0;
		for(GenID id_ex : exchanged.keySet()) {
			if(generalizations.containsKey(id_ex)) {
				generalizations.put(id_ex, null);
				count ++;
				if(filled)
					filled = false;
			}
		}
		System.out.println("         > Emptied "+count+"/"+generalizations.size()+" generalizations");
	}
	
	public Set<Example> covered_ex(Map<FeatureTerm, Set<Example>> context){
		Set<Example> output = new HashSet<>();
		for(Set<Example> set : context.values())
			output.addAll(adjunctSet(generalizations(), set));
		return output;
	}
	
	public Set<Example> uncovered_ex(Map<FeatureTerm, Set<Example>> context){
		Set<Example> output = new HashSet<>();
		for(Set<Example> set : context.values()) {
			output.addAll(set);
		}
		return new HashSet<Example>(ToolSet.substract(output, covered_ex(context)));
	}
	
	
	public Set<Generalization> toGeneralizations() {
		return generalizations();
	}
	
	public Set<Argument> toArguments() {
		Set<Argument> output = new HashSet<>();
		for(Generalization g : generalizations())
			output.add(generalizationToArgument(g, overall_set_label));
		return output;
	}
	
	public Set<Example> toExamples(){
		return new HashSet<Example>();
	}
	
	public CounterArgument clone() {
		return new CounterArgument.Builder()
				.as(this.id)
				.against(this.attack)
				.from(this.agent_nick)
				.labelled(this.overall_set_label)
				.withGeneralizations(this.getFullGeneralizations())
				.withGenIDs(this.getEmptyGeneralizations())
				.build();
	}

}
