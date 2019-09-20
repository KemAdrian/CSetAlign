/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Copyright (c) 2013, Santiago Ontañón All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * the IIIA-CSIC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
  
 package csic.iiia.ftl.argumentation.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Path;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.Pair;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;

/*
 * @author santi
 */
/**
 * The Class AMAIL.
 */
public class AMAIL {

	/** The last_counterexamples_sent. */
	public int last_counterexamples_sent = 0;

	/** The last_uncoveredexamples_sent. */
	public int last_uncoveredexamples_sent = 0;

	/** The last_skepticalexamples_sent. */
	public int last_skepticalexamples_sent = 0;

	/** The last_rules_sent. */
	public int last_rules_sent = 0;

	/** The VISUALIZ e_ evaluatio n_ afte r_ revision. */
	public boolean VISUALIZE_EVALUATION_AFTER_REVISION = false;

	/** The a_l. */
	public List<ArgumentationAgent> a_l = new LinkedList<ArgumentationAgent>();

	/** The agent name table. */
	HashMap<String, ArgumentationAgent> agentNameTable = new HashMap<String, ArgumentationAgent>();

	/** The token. */
	ArgumentationAgent token = null;

	/** The state. */
	public ArgumentationState state = null;

	/** The sp. */
	Path dp = null, sp = null;

	/** The o. */
	Ontology o = null;

	/** The dm. */
	FTKBase dm = null;

	/** The solution. */
	FeatureTerm solution = null;

	/** The round. */
	int round = 0;

	/** The another round. */
	int anotherRound = 2;

	/**
	 * Instantiates a new aMAIL.
	 * 
	 * @param l_h
	 *            the l_h
	 * @param a_solution
	 *            the a_solution
	 * @param l_examples
	 *            the l_examples
	 * @param l_aa
	 *            the l_aa
	 * @param l_l
	 *            the l_l
	 * @param credulous
	 *            the credulous
	 * @param a_dp
	 *            the a_dp
	 * @param a_sp
	 *            the a_sp
	 * @param a_o
	 *            the a_o
	 * @param a_dm
	 *            the a_dm
	 */
	public AMAIL(List<RuleHypothesis> l_h, FeatureTerm a_solution, List<List<FeatureTerm>> l_examples, List<ArgumentAcceptability> l_aa,
			List<ArgumentationBasedLearning> l_l, boolean credulous, Path a_dp, Path a_sp, Ontology a_o, FTKBase a_dm) {

		for (int i = 0; i < l_h.size(); i++) {
			ArgumentationAgent a = new ArgumentationAgent("Agent " + (i + 1), l_examples.get(i), l_aa.get(i), new RuleHypothesis(l_h.get(i)), l_l.get(i),
					credulous);
			a_l.add(a);
			agentNameTable.put(a.m_name, a);
		}
		token = a_l.get(0);

		// Initial state of argumentation:
		state = new ArgumentationState();
		for (ArgumentationAgent a : a_l) {
			for (Rule r : a.m_hypothesis.getRules()) {
				state.addNewRoot(a.m_name, new Argument(r, a.m_name));
			}
		}

		dp = a_dp;
		sp = a_sp;
		o = a_o;
		dm = a_dm;
		solution = a_solution;
	}

	/**
	 * Instantiates a new aMAIL.
	 * This constructor is only useful so that it can be swapped with the AMAIL2 one:
	 * AMAIL2 assumed "credulous" agents, and so does this constructor:
	 * 
	 * @param h1
	 *            the h1
	 * @param h2
	 *            the h2
	 * @param a_solution
	 *            the a_solution
	 * @param examples1
	 *            the examples1
	 * @param examples2
	 *            the examples2
	 * @param aa1
	 *            the aa1
	 * @param aa2
	 *            the aa2
	 * @param l1
	 *            the l1
	 * @param l2
	 *            the l2
	 * @param a_dp
	 *            the a_dp
	 * @param a_sp
	 *            the a_sp
	 * @param a_o
	 *            the a_o
	 * @param a_dm
	 *            the a_dm
	 */
	public AMAIL(RuleHypothesis h1, RuleHypothesis h2, FeatureTerm a_solution, Collection<FeatureTerm> examples1, Collection<FeatureTerm> examples2,
			ArgumentAcceptability aa1, ArgumentAcceptability aa2, ABUI l1, ABUI l2, Path a_dp, Path a_sp, Ontology a_o, FTKBase a_dm) {
		a_l.add(new ArgumentationAgent("Agent 1", examples1, aa1, new RuleHypothesis(h1), l1));
		a_l.add(new ArgumentationAgent("Agent 2", examples2, aa2, new RuleHypothesis(h2), l2));
		agentNameTable.put(a_l.get(0).m_name, a_l.get(0));
		agentNameTable.put(a_l.get(1).m_name, a_l.get(1));
		token = a_l.get(0);

		// Initial state of argumentation:
		state = new ArgumentationState();
		for (ArgumentationAgent a : a_l) {
			for (Rule r : a.m_hypothesis.getRules()) {
				state.addNewRoot(a.m_name, new Argument(r, a.m_name));
			}
		}

		dp = a_dp;
		sp = a_sp;
		o = a_o;
		dm = a_dm;
		solution = a_solution;
	}

	/**
	 * Gets the token.
	 * 
	 * @return the token
	 */
	public String getToken() {
		return token.m_name;
	}

	/**
	 * Gets the next agent.
	 * 
	 * @return the next agent
	 */
	public String getNextAgent() {
		int pos = a_l.indexOf(token);
		pos++;
		if (pos >= a_l.size())
			pos = 0;
		return a_l.get(pos).m_name;
	}

	/**
	 * Gets the previous agent.
	 * 
	 * @return the previous agent
	 */
	public String getPreviousAgent() {
		int pos = a_l.indexOf(token);
		pos--;
		if (pos < 0)
			pos = a_l.size() - 1;
		return a_l.get(pos).m_name;
	}

	/**
	 * More rounds p.
	 * 
	 * @return true, if successful
	 */
	public boolean moreRoundsP() {
		return anotherRound > 0;
	}

	/**
	 * Round.
	 * 
	 * @param singleMessage
	 *            the single message
	 * @throws Exception
	 *             the exception
	 */
	public void round(boolean singleMessage) throws Exception {
		boolean anyAttack = false;
		anotherRound--;

		System.out.println("");
		System.out.println("AMAIL: agent " + token.m_name + " has the token in round " + round);

		for (ArgumentationAgent a : a_l) {
			if (a != token) {
				for (Rule r : a.m_hypothesis.getRules()) {
					ArgumentationTree at = state.getTree(r);
					System.out.println("AA of A" + state.getRoot(r).m_ID + " (by A" + a.m_name + "): " + token.m_aa.degree(new Argument(r)) + " (Tree size: "
							+ at.getSize() + ")");
				}
			}
		}

		// Try to defend all of the self arguments which are currently defeated:
		List<ArgumentationTree> toDefend = state.getDefeated(token.m_name);
		System.out.println("AMAIL: agent " + token.m_name + " has to defend " + toDefend.size() + " roots");
		for (ArgumentationTree at : toDefend) {
			// Defend argument:
			// Get all the arguments that have to be attacked:
			List<Argument> challengers = at.getChallengers();
			List<Pair<Argument, Argument>> attacks = new LinkedList<Pair<Argument, Argument>>();
			System.out.println("AMAIL: agent " + token.m_name + " has to attack " + challengers.size() + " arguments to defend its root");
			for (Argument a : challengers) {
				Argument b = findSingleCounterArgument(a, token, state, at, dp, sp, o, dm, a_l);
				if (b == null) {
					at.settle(a, token.m_name);
					attacks.clear();
					break;
				} else {
					attacks.add(new Pair<Argument, Argument>(a, b));
				}
			}

			System.out.println("AMAIL: agent " + token.m_name + " can send " + attacks.size() + " attacks for this root!");

			// Send the attacks!
			for (Pair<Argument, Argument> attack : attacks) {
				at.addAttack(attack.mA, attack.mB);
				if (attack.mB.m_type == Argument.ARGUMENT_EXAMPLE) {
					ArgumentationAgent other = agentNameTable.get(attack.mA.m_agent);
					if (other != null) {
						if (token.sendExample(other, attack.mB.m_example, state))
							last_counterexamples_sent++;
					} else {
						System.err.println("Couldn't find the agent corresponding to attack.m_a.m_agent!!");
					}
				} else {
					last_rules_sent++;
				}
				anyAttack = true;
				if (singleMessage)
					break;
			}
			if (singleMessage && anyAttack)
				break;
		}

		// Settle all the arguments of the other agent which are acceptable:
		List<Pair<Argument, ArgumentationTree>> acceptable = state.getAcceptable(token.m_name, token.m_aa, a_l);
		for (Pair<Argument, ArgumentationTree> a : acceptable) {
			a.mB.settle(a.mA, token.m_name);
		}

		// Find unacceptable arguments "I", and attack one:
		if (!singleMessage || !anyAttack) {
			List<Pair<Argument, ArgumentationTree>> unacceptable = state.getUnacceptable(token.m_name, token.m_aa, a_l);
			System.out.println("AMAIL: agent " + token.m_name + " finds " + unacceptable.size() + " arguments of the other agent unacceptable");
			boolean anyExampleReceived = false;
			for (Pair<Argument, ArgumentationTree> a : unacceptable) {
				// Attack argument:
				Argument b = findSingleCounterArgument(a.mA, token, state, a.mB, dp, sp, o, dm, a_l);
				if (b == null) {
					if (token.m_credulous) {
						// Credulous agents believe in arguments they cannot attack:
						a.mB.settle(a.mA, token.m_name);
						System.out.println("AMAIL: credulous agent " + token.m_name + " settling for an opponent root.");
					} else {
						// Skeptical agents ask for evidence for arguments they cannot attack:
						ArgumentationAgent other = agentNameTable.get(a.mA.m_agent);
						// If some example has alredy been sent, maybe the argument is already acceptable, so, we have
						// to check again:
						if (!anyExampleReceived || !token.m_aa.accepted(a.mA)) {
							if (other != null) {
								List<FeatureTerm> examples = generateEndorsingExamples(a.mA, other.m_examples, other.m_alreadySentExamples.get(token.m_name),
										dp, sp, dm, o);
								if (examples.isEmpty()) {
									List<FeatureTerm> tmp = generateEndorsingExamples(a.mA, other.m_examples, null, dp, sp, dm, o);
									System.err.println("AMAIL: skeptical agent " + token.m_name + " asking opponent " + a.mA.m_agent
											+ " for positive examples of a root, but couldn't find any!!!");
									System.err.println("AMAIL: " + tmp.size() + " examples available, but all were already sent.");
									for (FeatureTerm tmp_e : tmp) {
										System.err.println("AMAIL: " + tmp_e.getName() + "(" + token.m_examples.contains(tmp_e) + ","
												+ token.m_aa.m_examples.contains(tmp_e) + ")");
									}
									System.err.println("AMAIL: argument profile for " + a.mA);
									System.err.println("AMAIL: argument profile for " + token.m_name + ": " + token.coveredExamples(a.mA, dp, sp) + " (AA: "
											+ token.m_aa.degree(a.mA) + " -> " + token.m_aa.accepted(a.mA) + ")");
									System.err.println("AMAIL: argument profile for " + other.m_name + ": " + other.coveredExamples(a.mA, dp, sp) + " (AA: "
											+ other.m_aa.degree(a.mA) + " -> " + other.m_aa.accepted(a.mA) + ")");
									System.exit(1);
								} else {
									if (other.sendExample(token, examples.get(0), state))
										last_skepticalexamples_sent++;
									System.out.println("AMAIL: skeptical agent " + token.m_name + " asking opponent " + a.mA.m_agent
											+ " for positive examples of a root.");
									anyExampleReceived = true;
									;
								}
							}
						}
					}
				} else {
					if (b.m_type == Argument.ARGUMENT_EXAMPLE)
						System.out.println("AMAIL: agent " + token.m_name + " sending an counterexample attack to " + a.mA.m_agent + "!");
					else
						System.out.println("AMAIL: agent " + token.m_name + " sending a rule attack to " + a.mA.m_agent + "!");
					a.mB.addAttack(a.mA, b);
					if (b.m_type == Argument.ARGUMENT_EXAMPLE) {
						ArgumentationAgent other = agentNameTable.get(a.mA.m_agent);
						if (other != null) {
							if (token.sendExample(other, b.m_example, state))
								last_counterexamples_sent++;
						} else {
							System.err.println("Couldn't find the agent corresponding to a.m_a.m_agent!!");
						}
					} else {
						last_rules_sent++;
					}
					anyAttack = true;
					break;
				}
			}
		}

		if (!anyAttack) {
			// Check for uncovered:
			for (FeatureTerm e : token.m_examples) {
				if (e.readPath(sp).equivalents(solution)) {
					for (ArgumentationAgent other : a_l) {
						if (other != token && !other.m_examples.contains(e) && other.m_hypothesis.coveredByAnyRule(e.readPath(dp)) == null) {
							System.out.println("AMAIL: Agent " + token.m_name + " sending uncovered example " + e.getName().get() + " to " + other.m_name);

							if (token.sendExample(other, e, state))
								last_uncoveredexamples_sent++;
							anyAttack = true;
							break;
						}
					}
					if (anyAttack)
						break;
				}
			}
		}

		// Belief Revision:
		for (ArgumentationAgent a : a_l) {
			if (a == token)
				a.beliefRevision(state, solution, dp, sp, o, dm, true, a_l);
			else
				a.beliefRevision(state, solution, dp, sp, o, dm, false, a_l);
		}

		if (anyAttack) {
			anotherRound = a_l.size();
		}

		// pass the token:
		int pos = a_l.indexOf(token);
		pos++;
		if (pos >= a_l.size())
			pos = 0;
		token = a_l.get(pos);
		round++;
		System.out.println("ok 2");
	}

	/**
	 * Result.
	 * 
	 * @return the list
	 */
	public List<RuleHypothesis> result() {
		List<RuleHypothesis> ret = new LinkedList<RuleHypothesis>();
		for (ArgumentationAgent a : a_l) {
			ret.add(a.m_hypothesis);
		}

		return ret;
	}

	/**
	 * Argue.
	 * 
	 * @param l_h
	 *            the l_h
	 * @param a_solution
	 *            the a_solution
	 * @param l_examples
	 *            the l_examples
	 * @param l_aa
	 *            the l_aa
	 * @param l_l
	 *            the l_l
	 * @param credulous
	 *            the credulous
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<RuleHypothesis> argue(List<RuleHypothesis> l_h, FeatureTerm a_solution, List<List<FeatureTerm>> l_examples,
			List<ArgumentAcceptability> l_aa, List<ArgumentationBasedLearning> l_l, boolean credulous, Path dp, Path sp, Ontology o, FTKBase dm)
			throws Exception {

		AMAIL argumentation = new AMAIL(l_h, a_solution, l_examples, l_aa, l_l, credulous, dp, sp, o, dm);
		while (argumentation.moreRoundsP())
			argumentation.round(false);

		System.out.println("Argumentation state at the end of AMAIL:");
		System.out.println(argumentation.state);

		return argumentation.result();
	}

	/**
	 * Find single counter argument.
	 * 
	 * @param argumentToAttack
	 *            the argument to attack
	 * @param attacker
	 *            the attacker
	 * @param state
	 *            the state
	 * @param context
	 *            the context
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param agents
	 *            the agents
	 * @return the argument
	 * @throws Exception
	 *             the exception
	 */
	public static Argument findSingleCounterArgument(Argument argumentToAttack, ArgumentationAgent attacker, ArgumentationState state,
			ArgumentationTree context, Path dp, Path sp, Ontology o, FTKBase dm, List<ArgumentationAgent> agents) throws Exception {
		List<Argument> settled = state.getSettled(agents);

		// System.out.println("findSingleCounterArgument: attacking argument " + argumentToAttack.toStringNOOS(dm));
		// System.out.println("findSingleCounterArgument: " + settled.size() + " settled arguments.");

		try {
			System.out.println("findSingleCounterArgument: looking for counterarguments...");
			Argument counterArgument = ABUI.generateBestCounterArgumentABUI(argumentToAttack, attacker.m_examples, settled, attacker.m_aa, dp, sp, dm, o);

			if (counterArgument != null && !context.containsEquivalent(counterArgument)) {
				counterArgument.m_agent = attacker.m_name;
				return counterArgument;
			}

			System.out.println("findSingleCounterArgument: no counterargument found, looking for counterexamples...");
			if (argumentToAttack.m_agent == null) {
				System.err.println("Argument agent generator is null!!!");
			}
			List<Argument> counterArguments = generateCounterExamples(argumentToAttack, attacker.m_examples,
					attacker.m_alreadySentExamples.get(argumentToAttack.m_agent), dp, sp, dm, o);
			if (counterArguments != null && counterArguments.size() > 0) {
				counterArguments.get(0).m_agent = attacker.m_name;
				return counterArguments.get(0);
			}

			System.out.println("findSingleCounterArgument: no counterexamples found either");

			return null;
		} catch (FeatureTermException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Evaluate rule.
	 * 
	 * @param r
	 *            the r
	 * @param examples
	 *            the examples
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static void evaluateRule(Rule r, List<FeatureTerm> examples, Path dp, Path sp) throws FeatureTermException {
		int correct = 0;
		int incorrect = 0;
		int total = 0;

		for (FeatureTerm e : examples) {
			FeatureTerm d = e.readPath(dp);

			if (r.pattern.subsumes(d)) {
				FeatureTerm s = e.readPath(sp);
				if (r.solution.equivalents(s)) {
					correct++;
				} else {
					incorrect++;
				}
			}

			total++;
		}

		System.out.println("evaluateRule: +" + correct + " -" + incorrect + " ("
				+ (correct + incorrect > 0 ? ((100.0f * correct) / (correct + incorrect)) + "%" : "-") + ") covering "
				+ ((100.0f * (correct + incorrect)) / total) + "%");
	}

	/**
	 * Evaluate rule set.
	 * 
	 * @param h_raw
	 *            the h_raw
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param examples1
	 *            the examples1
	 * @param examples2
	 *            the examples2
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param dm
	 *            the dm
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	public static void evaluateRuleSet(RuleHypothesis h_raw, List<Argument> acceptedArguments, List<FeatureTerm> examples1, List<FeatureTerm> examples2,
			Path dp, Path sp, FTKBase dm) throws FeatureTermException, Exception {
		List<FeatureTerm> examples = new LinkedList<FeatureTerm>();
		examples.addAll(examples1);
		for (FeatureTerm e : examples2) {
			if (!examples.contains(e)) {
				examples.add(e);
			}
		}

		RuleHypothesis h = new RuleHypothesis(h_raw);
		if (acceptedArguments != null) {
			for (Argument a : acceptedArguments) {
				h.addRule(a.m_rule);
			}
		}

		System.out.println("/* rule set evaluation ***************************************\\");
		for (Rule r : h.getRules()) {
			evaluateRule(r, examples, dp, sp);
		}
		System.out.println("\\*************************************************************/");
	}

	/**
	 * Generate counter examples.
	 * 
	 * @param a
	 *            the a
	 * @param examples1
	 *            the examples1
	 * @param already_sent
	 *            the already_sent
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<Argument> generateCounterExamples(Argument a, Collection<FeatureTerm> examples1, Collection<FeatureTerm> already_sent, Path dp, Path sp,
			FTKBase dm, Ontology o) throws FeatureTermException {
		List<Argument> counterArguments = new LinkedList<Argument>();

		if (a.m_type != Argument.ARGUMENT_RULE) {
			return null;
		}

		for (FeatureTerm e : examples1) {
			if (already_sent == null || !already_sent.contains(e)) {
				FeatureTerm d = e.readPath(dp);
				FeatureTerm s = e.readPath(sp);

				if (a.m_rule.pattern.subsumes(d)) {
					if (!a.m_rule.solution.equivalents(s)) {
						counterArguments.add(new Argument(e));
					}
				}
			}
		}

		return counterArguments;
	}

	/**
	 * Generate endorsing examples.
	 * 
	 * @param a
	 *            the a
	 * @param examples1
	 *            the examples1
	 * @param already_sent
	 *            the already_sent
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<FeatureTerm> generateEndorsingExamples(Argument a, Collection<FeatureTerm> examples1, Collection<FeatureTerm> already_sent, Path dp,
			Path sp, FTKBase dm, Ontology o) throws FeatureTermException {
		List<FeatureTerm> endorsingArguments = new LinkedList<FeatureTerm>();

		if (a.m_type != Argument.ARGUMENT_RULE) {
			return null;
		}

		for (FeatureTerm e : examples1) {
			if (already_sent == null || !already_sent.contains(e)) {
				FeatureTerm d = e.readPath(dp);
				FeatureTerm s = e.readPath(sp);

				if (a.m_rule.pattern.subsumes(d)) {
					if (a.m_rule.solution.equivalents(s)) {
						endorsingArguments.add(e);
					}
				}
			}
		}

		return endorsingArguments;
	}
}
