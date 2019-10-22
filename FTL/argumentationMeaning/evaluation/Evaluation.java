package evaluation;

import csic.iiia.ftl.argumentation.core.AMAIL;
import csic.iiia.ftl.argumentation.core.ArgumentAcceptability;
import csic.iiia.ftl.argumentation.core.ArgumentationBasedLearning;
import csic.iiia.ftl.argumentation.core.LaplaceArgumentAcceptability;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Symbol;
import csic.iiia.ftl.base.core.TermFeatureTerm;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import interfaces.Agent;
import interfaces.Container;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import tools.LPkg;
import tools.ToolSet;

import java.util.*;

public class Evaluation {
	
	public static void disagreementCount(Container Q1, Container Q2, Set<Example> context, boolean initial, Agent a) {
		System.out.println("   > Looking for disagreements...");
		int count = 0;
		int self_disagreements = 0;
		int overlap_disagreements = 0;
		int hypohyper_disagreements = 0;
		int synonymy_disagreements = 0;
		int homonymy_disagreements = 0;
		int blind_disagreements = 0;
		int untrans_disagreements = 0;
		// Look for self disagreements:
		List<Concept> conceptsQ1 = new ArrayList<>(Q1.getAllConcepts());
		List<Concept> conceptsQ2 = new ArrayList<>(Q2.getAllConcepts());
		// Test Q1 self disagreements
		for(int i=0; i<conceptsQ1.size(); i++) {
			for(int j=i+1; j<conceptsQ1.size(); j++) {
				Concept c1 = conceptsQ1.get(i);
				Concept c2 = conceptsQ1.get(j);
				if(ToolSet.intersection(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context)).size() >= ToolSet.THRESHOLD) {
					self_disagreements ++;
					count ++;
					System.out.println("     > Sef-disagreement found between "+c1+" and "+c2);
				}
			}
		}
		// Test Q2 self disagreements
		for(int i=0; i<conceptsQ2.size(); i++) {
			for(int j=i+1; j<conceptsQ2.size(); j++) {
				Concept c1 = conceptsQ2.get(i);
				Concept c2 = conceptsQ2.get(j);
				if(ToolSet.intersection(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context)).size() >= ToolSet.THRESHOLD) {
					self_disagreements ++;
					count ++;
					System.out.println("     > Sef-disagreement found between "+c1+" and "+c2);
				}
			}
		}
		// Test overlap disagreements
		for(Concept c1 : conceptsQ1) {
			for(Concept c2 : conceptsQ2) {
				if(ToolSet.overlap(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context))) {
					overlap_disagreements ++;
					count ++;
					System.out.println("     > Overlap found between "+c1+" and "+c2);
				}
			}
		}
		// Test hypo/hypernymy disagreements
		for(Concept c1 : conceptsQ1) {
			for(Concept c2 : conceptsQ2) {
				if(!ToolSet.equivalent(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context))) {
					if(ToolSet.included(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context))) {
						hypohyper_disagreements ++;
						count ++;
						System.out.println("     > Hyperonymy found between "+c1+" and "+c2);
					}
					else if(ToolSet.included(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context))) {
						hypohyper_disagreements ++;
						count ++;
						System.out.println("     >Hyponymy found between "+c1+" and "+c2);
					}
				}
			}
		}
		// Test synonymy disagreements
		for(Concept c1 : conceptsQ1) {
			for(Concept c2 : conceptsQ2) {
				if(ToolSet.equivalent(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context)) && !c1.sign().equals(c2.sign())) {
					synonymy_disagreements ++;
					count ++;
					System.out.println("     > Synonymy found between "+c1+" and "+c2);
				}
			}
		}
		// Test homonymy disagreements
		for(Concept c1 : conceptsQ1) {
			for(Concept c2 : conceptsQ2) {
				if(ToolSet.disjoint(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context)) && c1.sign().equals(c2.sign())) {
					homonymy_disagreements ++;
					count ++;
					System.out.println("     > Homonymy found between "+c1+" and "+c2);
				}
			}
		}
		// Test blind disagreements
		for(Concept c1 : conceptsQ1) {
			for(Concept c2 : conceptsQ2) {
				if(ToolSet.intersection(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context)).size() < ToolSet.THRESHOLD
						&& (ToolSet.substract(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context)).size() < ToolSet.THRESHOLD
								|| ToolSet.substract(ToolSet.adjunctSet(c2.intensional_definition, context), ToolSet.adjunctSet(c1.intensional_definition, context)).size() < ToolSet.THRESHOLD)
						) {
					blind_disagreements ++;
					count ++;
					System.out.println("     > Undiscernable disagreement found between "+c1+" and "+c2);
				}
			}
		}
		// Test untranslability disagreements
		for(Concept c1 : conceptsQ1) {
			boolean has_equivalent = false;
			for(Concept c2 : conceptsQ2) {
				if(ToolSet.equivalent(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context))) {
					has_equivalent = true;
					break;
				}
			}
			if(!has_equivalent) {
				untrans_disagreements ++;
				count ++;
				System.out.println("     > "+c1+" is untranslatable");
			}
		}
		for(Concept c1 : conceptsQ2) {
			boolean has_equivalent = false;
			for(Concept c2 : conceptsQ1) {
				if(ToolSet.equivalent(ToolSet.adjunctSet(c1.intensional_definition, context), ToolSet.adjunctSet(c2.intensional_definition, context))) {
					has_equivalent = true;
					break;
				}
			}
			if(!has_equivalent) {
				untrans_disagreements ++;
				count ++;
				System.out.println("     > "+c1+" is untranslatable");
			}
		}
		String agent_suffix = (a != null)? "_"+a.nick() : "";
		String time_prefix = (initial)? "i_" : "f_";
		ExpFileManager.addBlock(time_prefix+"i_total_"+agent_suffix,count);
		ExpFileManager.addBlock(time_prefix+"i_self_"+agent_suffix,self_disagreements);
		ExpFileManager.addBlock(time_prefix+"i_overlap_"+agent_suffix,overlap_disagreements);
		ExpFileManager.addBlock(time_prefix+"i_hyponym_"+agent_suffix,hypohyper_disagreements);
		ExpFileManager.addBlock(time_prefix+"i_synonym_"+agent_suffix,synonymy_disagreements);
		ExpFileManager.addBlock(time_prefix+"i_homonym_"+agent_suffix,homonymy_disagreements);
		ExpFileManager.addBlock(time_prefix+"i_blind_"+agent_suffix,blind_disagreements);
		ExpFileManager.addBlock(time_prefix+"i_untranslatable_"+agent_suffix,untrans_disagreements);
	}

	public static double coverage(Agent a, Container Q, Set<Example> overall_context) {
		Set<Example> covered = new HashSet<>();
		for(Concept c : Q.getAllConcepts())
			covered.addAll(a.adjunctSet(c.intensional_definition(), overall_context));
		covered = new HashSet<>(ToolSet.cleanDuplicates(covered));
		return ((double) covered.size()) / overall_context.size();
	}

	public static double shared_coverage(Agent a, Container Q, Container V, Set<Example> overall_context){
		Set<Example> coveredQ = new HashSet<>();
		Set<Example> coveredV = new HashSet<>();
		Set<Example> covered;
		for(Concept c : Q.getAllConcepts())
			coveredQ.addAll(a.adjunctSet(c.intensional_definition(), overall_context));
		for(Concept c : V.getAllConcepts())
			coveredV.addAll(a.adjunctSet(c.intensional_definition(), overall_context));
		covered = new HashSet<>(ToolSet.cleanDuplicates(ToolSet.intersection(coveredQ, coveredV)));
		return ((double) covered.size()) / overall_context.size();
	}
	
	public static double synchronicAgreementK(Agent adam, Agent boby, Set<Example> overall_context) {
		double out = 0.;
		// Synchronic agreement
		for(Example e : overall_context) {
			List<String> adam_answer = adam.name(e,adam.K());
			List<String> boby_answer = boby.name(e,boby.K());
			if(adam_answer.size() == 1 && boby_answer.size() == 1 && adam_answer.get(0).equals(boby_answer.get(0)))
				out ++;
		}
		return out/overall_context.size();
	}
	
	public static double synchronicAgreementKi(Agent adam, Agent boby, Set<Example> overall_context) {
		double out = 0.;
		// Synchronic agreement
		for(Example e : overall_context) {
			List<String> adam_answer = adam.name(e,adam.Ki());
			List<String> boby_answer = boby.name(e,boby.Ki());
			if(adam_answer.size() == 1 && boby_answer.size() == 1 && adam_answer.get(0).equals(boby_answer.get(0)))
				out ++;
		}
		return out/overall_context.size();
	}
	
	public static double localSynchronicAgreementK(Agent adam, Agent boby) {
		double out = 0.;
		// Synchronic agreement
		for(Example e : adam.K().context) {
			List<String> adam_answer = adam.name(e,adam.K());
			List<String> boby_answer = boby.name(e,boby.K());
			if(adam_answer.size() == 1 && boby_answer.size() == 1 && adam_answer.get(0).equals(boby_answer.get(0)))
				out ++;
		}
		return out/adam.K().context.size();
	}
	
	public static double localSynchronicAgreementKi(Agent adam, Agent boby) {
		double out = 0.;
		// Synchronic agreement
		for(Example e : adam.Ki().context) {
			List<String> adam_answer = adam.name(e,adam.Ki());
			List<String> boby_answer = boby.name(e,boby.Ki());
			if(adam_answer.size() == 1 && boby_answer.size() == 1 && adam_answer.get(0).equals(boby_answer.get(0)))
				out ++;
		}
		return out/ adam.Ki().context.size();
	}
	
	public static double diachronicAgreement(Agent agent, Set<Example> context) {
		double num = 0;
		double denom = 0;
		// Diachronic agreement
		List<Example> list = new ArrayList<>(context);
		for(int i=0; i<list.size(); i++) {
			for(int j=i+1; j<list.size(); j++) {
				// Examples
				Example e1 = list.get(i);
				Example e2 = list.get(j);
				// Names
				List<String> answer_K1 = agent.name(e1,agent.K());
				List<String> answer_K2 = agent.name(e2,agent.K());
				List<String> answer_K1i = agent.name(e1,agent.Ki());
				List<String> answer_K2i = agent.name(e2,agent.Ki());
				// Test
				if(answer_K1i.size() == 1 && answer_K2i.size() == 1 && !answer_K1i.equals(answer_K2i)) {
					denom ++;
					if(answer_K1.size() == 1 && answer_K2.size() == 1 && answer_K1.equals(answer_K2))
						num ++;
				}
			}
		}
		return 1 - (num/denom);
	}
	
	public static void amailEvaluation(Agent a1, Agent a2, Set<Example> test, boolean initial) {
		
		try {
			
		// Variables
		List<RuleHypothesis> l_h = new ArrayList<>();
		Map<String, FeatureTerm> solutions = new HashMap<>();
		List<List<FeatureTerm>> l_examples = new ArrayList<>();
		List<ArgumentAcceptability> l_aa = new ArrayList<>();
		List<ArgumentationBasedLearning> l_l = new ArrayList<>();
		FeatureTerm neutral = new TermFeatureTerm(new Symbol("the_solution"), LPkg.solution_sort());
		LPkg.dm().addFT(neutral);
		
		// Clean the left path inconsistencies
		a1.Ki().leftPathConsistent();
		a2.Ki().leftPathConsistent();
		
		// Create a set of all the solutions
		for(Concept c : a1.Ki().getAllConcepts())
				solutions.put(c.sign(), new TermFeatureTerm(new Symbol(c.sign()), LPkg.solution_sort()));
		// Create a set of all the solutions
		for(Concept c : a2.Ki().getAllConcepts())
				solutions.put(c.sign(), new TermFeatureTerm(new Symbol(c.sign()), LPkg.solution_sort()));
		
		// Create the examples
		List<FeatureTerm> ex1 = new ArrayList<>();
		List<FeatureTerm> ex2 = new ArrayList<>();
		for(Concept c : a1.Ki().getAllConcepts()) {
			for(Example e : c.extensional_definition())
				ex1.add(LPkg.createFeature(e.featureterm, solutions.get(c.sign())));
		}
		for(Concept c : a2.Ki().getAllConcepts()) {
			for(Example e : c.extensional_definition())
				ex2.add(LPkg.createFeature(e.featureterm, solutions.get(c.sign())));
		}
		l_examples.add(ex1);
		l_examples.add(ex2);
		
		// Put the hypotheses
		RuleHypothesis h1 = new RuleHypothesis();
		RuleHypothesis h2 = new RuleHypothesis();
		for(Concept c : a1.Ki().getAllConcepts()) {
			for(Generalization g : c.intensional_definition())
				h1.addRule(new Rule(g.generalization() , solutions.get(c.sign())));
		}
		for(Concept c : a2.Ki().getAllConcepts()) {
			for(Generalization g : c.intensional_definition())
				h2.addRule(new Rule(g.generalization() , solutions.get(c.sign())));
		}
		l_h.add(h1);
		l_h.add(h2);
		
		// Create the argument acceptabilities
		ArgumentAcceptability aa1 = new LaplaceArgumentAcceptability(ex1, LPkg.solution_path(), LPkg.description_path(), (float) 0.75);
		ArgumentAcceptability aa2 = new LaplaceArgumentAcceptability(ex2, LPkg.solution_path(), LPkg.description_path(), (float) 0.75);
		l_aa.add(aa1);
		l_aa.add(aa2);
		
		// Create new argumentation base learning
		ArgumentationBasedLearning abl1 = new ArgumentationBasedLearning();
		ArgumentationBasedLearning abl2 = new ArgumentationBasedLearning();
		l_l.add(abl1);
		l_l.add(abl2);
		
		// Create new AMAIL
		AMAIL amail = new AMAIL(l_h, new LinkedList<>(LPkg.different_solutions()).get(0), l_examples, l_aa, l_l, false, LPkg.description_path(), LPkg.solution_path(), LPkg.ontology(), LPkg.dm());
		
		while(amail.moreRoundsP()){
			amail.round(true);
		}
		
		// Test performances
		double out = 0;
		for(Example e : test) {
			// Get answers
			List<FeatureTerm> adam_answer = amail.result().get(0).generatePrediction(e.featureterm, LPkg.dm(), false).solutions;
			List<FeatureTerm> boby_answer = amail.result().get(1).generatePrediction(e.featureterm, LPkg.dm(), false).solutions;
			
			if(adam_answer.size() == 1 && boby_answer.size() == 1 && adam_answer.get(0).equivalents(boby_answer.get(0)))
				out ++;
		}
		System.out.println( out / test.size());
		String time_prefix = (initial)? "i_" : "f_";
		ExpFileManager.addBlock(time_prefix+"amail_sync_ag", out / test.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
