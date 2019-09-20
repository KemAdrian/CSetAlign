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
import tools.Test;
import tools.ToolSet;

public class Belief extends TreeNode implements ArgNode{
	
	public boolean filled;
	public Map<GenID, Generalization> generalizations;
	
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
		return null;
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
		
		public Belief build() {
			Belief belief = new Belief();
			// Fill standard variables
			belief.id = this.id;
			belief.agent_nick = this.agent_nick;
			belief.filled = this.filled;
			belief.overall_set_label = this.overall_set_label;
			belief.generalizations = this.generalizations;
			// Check if filled
			belief.filled = belief.getEmptyGeneralizations().isEmpty();
			return belief;
		}
		
		public Belief learn(Map<FeatureTerm, Set<Example>> context, Set<Argument> arguments) {
			Belief belief = new Belief();
			// Build
			belief.id = this.id;
			belief.agent_nick = this.agent_nick;
			belief.filled = this.filled;
			belief.overall_set_label = this.overall_set_label;
			belief.generalizations = this.generalizations;
			// Learn
			Set<FeatureTerm> learningSet = FTConv.contextToLearningSet(context);
			for(Generalization g : ABUI.learnConcept(learningSet, arguments, overall_set_label))
				belief.generalizations.put(g.id, g);
			// Check if filled
			belief.filled = belief.getEmptyGeneralizations().isEmpty();
			// TEMP
			System.out.println("classification: "+belief.classification(context));
			return belief;
		}
	}
	
	private Belief() {
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
	
	public Belief replace(Set<Argument> arguments, Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn, Agent agent) {
		System.out.println("     > Replacing the belief...");
		// Reuse valid part
		Set<Generalization> reused = new HashSet<>(getValidPart(arguments, context));
		System.out.println("       > Reusing "+reused.size()+"/"+generalizations().size()+" generalizations...");
		// Subsume the examples that are positives but not subsumed by the reused generalizations
		Map<FeatureTerm, Set<Example>> local_context = new HashMap<>();
		local_context.put(overall_set_label, new HashSet<>(ToolSet.substract(positive_ex(context), adjunctSet(reused, positive_ex(context)))));
		local_context.put(oppositeLabel(context.keySet()), new HashSet<>(ToolSet.substract(all_ex(context), local_context.get(overall_set_label))));
		Set<Generalization> news = ABUI.learnConcept(FTConv.contextToLearningSet(local_context), arguments, overall_set_label);
		System.out.println("       > Adding "+news.size()+" newly learned generalizations...");
		Set<Generalization> out = new HashSet<>();
		out.addAll(reused);
		out.addAll(news);
		// Add positive arguments
		for(Argument arg : arguments)
			if(arg.m_rule.solution.equivalents(overall_set_label))
				out.add(argumentToGeneralization(arg));
		System.out.println("       > Adding adequate generalizations from positive attacks for a total of "+out.size()+" generalizations");
		// TEMP
		Belief output = new Belief.Builder().from(agent_nick).labelled(overall_set_label).withGeneralizations(out).build();
		System.out.println("classification: "+output.classification(context));
		return new Belief.Builder().from(agent_nick).labelled(overall_set_label).withGeneralizations(out).build();
	}
	
	public boolean acceptable(Map<FeatureTerm, Set<Example>> context, int maxfp, int maxfn) {
		Test classification = classification(context);
		return generalizations().isEmpty() || (classification.fp <= maxfp && classification.fn <= maxfn);
	}
	
	public Set<Node> attack(Map<FeatureTerm, Set<Example>> context, Set<Argument> accepted_arguments, Set<Node> past_attacks, int maxfp, int maxfn, Agent agent){
		Set<Node> out = new HashSet<>();
		// Check false positives
		if(classification(context).fp > maxfp) {
			System.out.println("       > High number of false positives, generating attack...");
			// Look for positive attacks
			Set<Generalization> p_argument = new HashSet<>();
			Set<Example> p_example = new HashSet<>();
			// Check for a past attack
			Node past_attack = null;
			for(Node n : past_attacks) {
				System.out.println(n.attacks());
				System.out.println(id);
			}
			for(Node n : past_attacks)
				if(id.equals(n.attacks()) && n.getLabel().equivalents(oppositeLabel(context.keySet())))
					past_attack = n;
			// If found, update it
			if(past_attack != null) {
				System.out.println("         > Found past attack, modifying it...");
				p_argument.addAll(past_attack.replace(accepted_arguments, context, maxfp, maxfn, agent).toGeneralizations());
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
						p_argument.add(argumentToGeneralization(arg));
				}
			}
			// Send counter examples if counter arguments are insufficient
			Set<Example> remaining = new HashSet<>(ToolSet.substract(ToolSet.intersection(covered_ex(context), negative_ex(context)), adjunctSet(p_argument, all_ex(context))));
			if(remaining.size() > maxfp){
				System.out.println("         > Generalizations missed "+remaining.size()+" examples, sending counter example instead...");
				p_argument.clear();
				System.out.println(ToolSet.intersection(covered_ex(context), negative_ex(context)));
				p_example.addAll(ToolSet.optiRandomSubset(agent,ToolSet.intersection(covered_ex(context), negative_ex(context)), maxfp + 1));
				System.out.println(agent.e_exchanged().size());
				System.out.println(agent.e_exchanged());
				System.out.println(p_example);
				p_example = new HashSet<>(ToolSet.substract(p_example, agent.e_exchanged()));
				System.out.println(p_example.size());
			}
			// Add attacks to output
			if(!p_argument.isEmpty())
				out.add(new CounterArgument.Builder().from(agent.nick()).against(id).labelled(oppositeLabel(context.keySet())).withGeneralizations(p_argument).build());
			if(!p_example.isEmpty())
				out.add(new CounterExample.Builder().from(agent.nick()).against(id).labelled(oppositeLabel(context.keySet())).withExamples(p_example).build());	
		}
		// Check false negatives
		if(classification(context).fn > maxfn) {
			System.out.println("       > High number of false negatives, generating attack...");
			// Look for positive attacks
			Set<Generalization> n_argument = new HashSet<>();
			Set<Example> n_example = new HashSet<>();
			// Check for past attacks
			Node past_attack = null;
			for(Node n : past_attacks)
				if(id.equals(n.attacks()) && n.getLabel().equivalents(overall_set_label))
					past_attack = n;
			// If found, update it
			if(past_attack != null) {
				System.out.println("         > Found past attack, modifying it...");
				n_argument.addAll(past_attack.replace(accepted_arguments, context, maxfp, maxfn, agent).toGeneralizations());
			}
			// Else, create new attack
			else {
				System.out.println("         > Didn't find past attack to modify, creating a new one...");
				// Define the attack's context
				Map<FeatureTerm, Set<Example>> local_context = new HashMap<>();
				local_context.put(overall_set_label, new HashSet<>(ToolSet.intersection(uncovered_ex(context), positive_ex(context))));
				local_context.put(oppositeLabel(context.keySet()), new HashSet<>(ToolSet.substract(all_ex(context), local_context.get(overall_set_label))));
				// Try to create a new set of generalizations
				n_argument.addAll(ABUI.learnConcept(FTConv.contextToLearningSet(local_context), accepted_arguments, overall_set_label));
			}
			// Send counter example if counter arguments are insufficient
			Set<Example> remaining = new HashSet<>(ToolSet.substract(ToolSet.intersection(positive_ex(context), uncovered_ex(context)), adjunctSet(n_argument, all_ex(context))));
			if(remaining.size() > maxfn) {
				System.out.println("         > Generalizations missed "+remaining.size()+" examples, sending counter example instead...");
				n_argument.clear();
				n_example.addAll(ToolSet.optiRandomSubset(agent, ToolSet.intersection(positive_ex(context), uncovered_ex(context)), maxfn + 1));
				System.out.println(agent.e_exchanged().size());
				System.out.println(agent.e_exchanged());
				System.out.println(n_example);
				n_example = new HashSet<>(ToolSet.substract(n_example, agent.e_exchanged()));
			}
			// Add attacks to output
			if (!n_argument.isEmpty())
				out.add(new CounterArgument.Builder().from(agent.nick()).against(id).labelled(overall_set_label).withGeneralizations(n_argument).build());
			if (!n_example.isEmpty())
				out.add(new CounterExample.Builder().from(agent.nick()).against(id).labelled(overall_set_label).withExamples(n_example).build());
		}
		System.out.println("     > Created "+out.size()+" counter arguments");
		return out;
	}
	
	public boolean fill(Map<GenID,Generalization> exchanged) {
		Collection<GenID> ids = getEmptyGeneralizations();
		System.out.println("       > Trying to fill "+ids.size()+" generalizations...");
		for(GenID id : ids) {
			Generalization g = exchanged.get(id);
			if(g != null)
				generalizations.put(id, g);
		}
		filled = this.getEmptyGeneralizations().isEmpty();
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
				filled = false;
			}
		}
		System.out.println("         > Emptied "+count+"/"+generalizations.size()+" generalizations");
	}
	
	public Set<Example> covered_ex(Map<FeatureTerm, Set<Example>> context){
		Set<Example> out = new HashSet<>();
		for(Set<Example> set : context.values())
			out.addAll(adjunctSet(generalizations(), set));
		return out;
	}
	
	public Set<Example> uncovered_ex(Map<FeatureTerm, Set<Example>> context){
		Set<Example> out = new HashSet<>();
		for(Set<Example> set : context.values()) {
			out.addAll(set);
		}
		return new HashSet<Example>(ToolSet.substract(out, covered_ex(context)));
	}
	
	
	public Set<Generalization> toGeneralizations(){
		return generalizations();
	}
	
	public Set<Argument> toArguments() {
		Set<Argument> output = new HashSet<>();
		for(Generalization g : generalizations())
			generalizationToArgument(g, overall_set_label);
		return output;
	}
	
	public Set<Example> toExamples(){
		return new HashSet<Example>();
	}
	
	public Belief clone() {
		return new Belief.Builder()
				.as(this.id)
				.from(this.agent_nick)
				.labelled(this.overall_set_label)
				.withGeneralizations(this.getFullGeneralizations())
				.withGenIDs(this.getEmptyGeneralizations())
				.build();
	}

}
