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

import containers.ContrastSet;
import csic.iiia.ftl.base.core.*;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.learning.core.Hypothesis;
import csic.iiia.ftl.learning.core.Rule;
import csic.iiia.ftl.learning.core.RuleHypothesis;
import semiotic_elements.Concept;
import semiotic_elements.Example;
import semiotic_elements.Generalization;
import semiotic_elements.Sign;
import tools.LPkg;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ABUI.
 * 
 * @author santi
 */
public class ABUI extends ArgumentationBasedLearning {

	/** The DEBUG. */
	public static int DEBUG = 0;

	/** The ABU i_call_count. */
	public static int ABUI_call_count = 0;

	/** The ABU i_ version. */
	public static int ABUI_VERSION = 1;

	/**
	 * Generate best counter argument abui.
	 * 
	 * @param a
	 *            the a
	 * @param examples
	 *            the examples
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the argument
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	public static Argument generateBestCounterArgumentABUI(Argument a, Collection<FeatureTerm> examples, Collection<Argument> acceptedArguments,
			ArgumentAcceptability aa, Path dp, Path sp, FTKBase dm, Ontology o) throws FeatureTermException, Exception {
		List<Argument> args = new LinkedList<Argument>();
		List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(examples, sp);
		ABUI abui = new ABUI();

		for (FeatureTerm solution : allSolutions) {

			if (!solution.equivalents(a.m_rule.solution)) {
				List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
				List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

				for (FeatureTerm e : examples) {
					if (e.readPath(sp).equivalents(solution)) {
						positive.add(e.readPath(dp));
					} else {
						negative.add(e.readPath(dp));
					}
				}

				Argument beta = null;
				switch (ABUI_VERSION) {
				case 1:
					beta = abui.ABUI1(positive, negative, solution, acceptedArguments, a.m_rule.pattern, aa, o, dm);
					break;
				case 2:
					beta = abui.ABUI2(positive, negative, positive, solution, acceptedArguments, a.m_rule.pattern, aa, o, dm);
					break;
				}
				if (beta != null) {
					args.add(beta);
				}
			}
		}

		{
			Argument best_beta = null;
			float best_conf = 0.0f;

			for (Argument beta : args) {
				float conf = beta.m_rule.reliability;
				if (best_beta == null || conf > best_conf) {
					best_beta = beta;
					best_conf = conf;
				}
			}

			return best_beta;
		}

	}
	
	public static Set<Generalization> learnConcept(Collection<FeatureTerm> training_set, Set<Argument> accepted_arguments, FeatureTerm label){
		ABUI learner = new ABUI();
		Set<Generalization> out = new HashSet<>();
		int pos = 0;
		for (FeatureTerm f : training_set)
			if (f.readPath(LPkg.solution_path()).equivalents(label))
				pos++;
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(training_set, LPkg.solution_path(), LPkg.description_path(), LPkg.ABUI_THRESHOLD);
		RuleHypothesis h = new RuleHypothesis();
		try {
			h = learner.learnConceptABUI(training_set, label, accepted_arguments, aa, LPkg.description_path(), LPkg.solution_path(), LPkg.ontology(), LPkg.dm());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for(Rule r : h.getRules()) {
			if(r.solution.equals(label))
				out.add(new Generalization(r.pattern));
		}
		System.out.println(out.size()+" gen for "+pos);
		return out;
	}
	
	public static Argument counterArgue(Argument a, Set<Argument> accepted_arguments, Collection<FeatureTerm> training_set) {
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(training_set, LPkg.solution_path(), LPkg.description_path(), LPkg.ABUI_THRESHOLD);
		try {
			Argument c_a = ABUI.generateBestCounterArgumentABUI(a, training_set, accepted_arguments, aa, LPkg.description_path(), LPkg.solution_path(), LPkg.dm(), LPkg.ontology());
			return c_a;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	/**
	 * Learn concept abui.
	 * 
	 * @param examples
	 *            the examples
	 * @param solution
	 *            the solution
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the rule hypothesis
	 * @throws Exception
	 *             the exception
	 */
	public RuleHypothesis learnConceptABUI(Collection<FeatureTerm> examples, FeatureTerm solution, Collection<Argument> acceptedArguments,
			ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		List<Argument> args = new LinkedList<Argument>();
		FeatureTerm any = o.getSort("any").createFeatureTerm();

		if (DEBUG >= 1) {
			System.out.println("learnConceptABUI: starting...");
		}

		List<FeatureTerm> allPositive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negative = new LinkedList<FeatureTerm>();
		for (FeatureTerm e : examples) {
			if (e.readPath(sp).equivalents(solution)) {
				positive.add(e.readPath(dp));
				allPositive.add(e.readPath(dp));
			} else {
				negative.add(e.readPath(dp));
			}
		}

		if (DEBUG >= 1) {
			System.out.println("learnConceptABUI: starting with class " + solution.toStringNOOS(dm) + " with " + positive.size() + "/" + negative.size()
					+ " examples.");
		}
		while (positive.size() > 0) {
			Argument a = null;
			switch (ABUI_VERSION) {
			case 1:
				a = ABUI1(positive, negative, solution, acceptedArguments, any, aa, o, dm);
				break;
			case 2:
				a = ABUI2(positive, negative, allPositive, solution, acceptedArguments, any, aa, o, dm);
				break;
			}

			if (a != null) {
				args.add(a);

				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				for (FeatureTerm d : positive) {
					if (a.m_rule.pattern.subsumes(d)) {
						toDelete.add(d);
					}
				}
				positive.removeAll(toDelete);
				if (DEBUG >= 1) {
					System.out.println("learnConceptABUI: argument covers " + toDelete.size() + " examples: " + a.m_rule.reliability);
					System.out.println(a.toStringNOOS(dm));
				}
			} else {
				break;
			}
		}

		if (DEBUG >= 1) {
			System.out.println("learnConceptABUI: done, returning " + args.size() + " arguments.");
		}

		RuleHypothesis h = new RuleHypothesis();
		for (Argument a : args) {
			h.addRule(a.m_rule);
		}

		return h;
	}

	/**
	 * Cover examples abui.
	 * 
	 * @param examples
	 *            the examples
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param aa
	 *            the aa
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
	public List<Argument> coverExamplesABUI(List<FeatureTerm> examples, Collection<Argument> acceptedArguments, ArgumentAcceptability aa, Path dp, Path sp,
			Ontology o, FTKBase dm) throws Exception {
		List<Argument> args = new LinkedList<Argument>();
		FeatureTerm any = o.getSort("any").createFeatureTerm();

		if (DEBUG >= 1) {
			System.out.println("coverExamplesABUI: starting...");
		}

		List<FeatureTerm> allSolutions = Hypothesis.differentSolutions(examples, sp);

		for (FeatureTerm solution : allSolutions) {
			List<FeatureTerm> allPositive = new LinkedList<FeatureTerm>();
			List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
			List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

			for (FeatureTerm e : examples) {
				if (e.readPath(sp).equivalents(solution)) {
					positive.add(e.readPath(dp));
					allPositive.add(e.readPath(dp));
				} else {
					negative.add(e.readPath(dp));
				}
			}

			if (DEBUG >= 1) {
				System.out.println("coverExamplesABUI: starting with class " + solution.toStringNOOS(dm) + " with " + positive.size() + "/" + negative.size()
						+ " examples.");
			}

			while (positive.size() > 0) {
				Argument a = null;
				switch (ABUI_VERSION) {
				case 1:
					a = ABUI1(positive, negative, solution, acceptedArguments, any, aa, o, dm);
					break;
				case 2:
					a = ABUI2(positive, negative, allPositive, solution, acceptedArguments, any, aa, o, dm);
					break;
				}

				if (a != null) {
					args.add(a);

					List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
					for (FeatureTerm d : positive) {
						if (a.m_rule.pattern.subsumes(d)) {
							toDelete.add(d);
						}
					}
					positive.removeAll(toDelete);
					if (DEBUG >= 1) {
						System.out.println("coverExamplesABUI: argument covers " + toDelete.size() + " examples: " + a.m_rule.reliability);
					}
				} else {
					break;
				}
			}

			if (DEBUG >= 1) {
				System.out.println("coverExamplesABUI: done with class " + solution.toStringNOOS(dm) + " leaving  " + positive.size() + " uncovered.");
			}
		}

		if (DEBUG >= 1) {
			System.out.println("coverExamplesABUI: done, returning " + args.size() + " arguments.");
		}

		return args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.argumentation.core.ArgumentationBasedLearning#coverUncoveredExamples(java.util.List,
	 * csic.iiia.ftl.base.core.FeatureTerm, csic.iiia.ftl.learning.core.RuleHypothesis, java.util.List,
	 * csic.iiia.ftl.argumentation.core.ArgumentAcceptability, csic.iiia.ftl.base.core.Path,
	 * csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Ontology, csic.iiia.ftl.base.core.FTKBase)
	 */
	public RuleHypothesis coverUncoveredExamples(List<FeatureTerm> examples, FeatureTerm solution, RuleHypothesis h, List<Argument> acceptedArguments,
			ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		List<Argument> args = new LinkedList<Argument>();
		FeatureTerm any = o.getSort("any").createFeatureTerm();

		if (DEBUG >= 1) {
			System.out.println("coverUncoveredExamplesABUI: starting...");
		}

		List<FeatureTerm> allPositive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> positive = new LinkedList<FeatureTerm>();
		List<FeatureTerm> negative = new LinkedList<FeatureTerm>();

		for (FeatureTerm e : examples) {
			if (e.readPath(sp).equivalents(solution)) {
				boolean covered = false;
				FeatureTerm d = e.readPath(dp);
				FeatureTerm s = e.readPath(sp);
				if (h != null) {
					for (Rule r : h.getRules()) {
						if (r.pattern.subsumes(d) && r.solution.equivalents(s)) {
							covered = true;
							break;
						}
					}
				}

				if (!covered) {
					positive.add(e.readPath(dp));
				}
				allPositive.add(e.readPath(dp));
			} else {
				negative.add(e.readPath(dp));
			}
		}

		if (DEBUG >= 0) {
			System.out.println("coverUncoveredExamplesABUI: starting with class " + solution.toStringNOOS(dm) + " with " + positive.size() + "/"
					+ negative.size() + " examples and " + acceptedArguments.size() + " accepted arguments.");
			System.out.println("coverUncoveredExamplesABUI: aa has " + aa.m_examples.size() + " examples.");
		}

		while (positive.size() > 0) {
			Argument a = null;
			switch (ABUI_VERSION) {
			case 1:
				a = ABUI1(positive, negative, solution, acceptedArguments, any, aa, o, dm);
				break;
			case 2:
				a = ABUI2(positive, negative, allPositive, solution, acceptedArguments, any, aa, o, dm);
				break;
			}

			if (a != null) {
				args.add(a);

				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				for (FeatureTerm d : positive) {
					if (a.m_rule.pattern.subsumes(d)) {
						toDelete.add(d);
					}
				}
				positive.removeAll(toDelete);
				if (DEBUG >= 1) {
					System.out.println("coverUncoveredExamplesABUI: argument covers " + toDelete.size() + " examples: " + a.m_rule.reliability);
				}
			} else {
				break;
			}
		}

		if (DEBUG >= 0) {
			System.out.println("coverUncoveredExamplesABUI: done with class " + solution.toStringNOOS(dm) + " leaving  " + positive.size() + " uncovered.");
		}

		if (DEBUG >= 1) {
			System.out.println("coverUncoveredExamplesABUI: done, returning " + args.size() + " arguments.");
		}

		for (Argument a : args) {
			h.addRule(a.m_rule);
		}

		return h;
	}

	/**
	 * ABUI.
	 * 
	 * This is the ABUI induction algorithm as explained in our "Concept Convergence" paper submitted to AAMAS 2010: -
	 * returns an argument which is more specific than "g", and which is not attacked by any argument in
	 * "acceptedArguments"
	 * 
	 * NOTE: this algorithm uses the definition of "attack" present in the paper, not the one in "Common"
	 * 
	 * @param positiveDescriptions
	 *            the positive descriptions
	 * @param negativeDescriptions
	 *            the negative descriptions
	 * @param solution
	 *            the solution
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param g
	 *            the g
	 * @param aa
	 *            the aa
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the argument
	 * @throws Exception
	 *             the exception
	 */
	public Argument ABUI1(List<FeatureTerm> positiveDescriptions, List<FeatureTerm> negativeDescriptions, FeatureTerm solution,
			Collection<Argument> acceptedArguments, FeatureTerm g, ArgumentAcceptability aa, Ontology o, FTKBase dm) throws Exception {

		List<Argument> H = new LinkedList<Argument>(); // This list will contain the candidate arguments

		ABUI_call_count++;

		for (FeatureTerm e : positiveDescriptions) {
			if (DEBUG >= 2) {
				System.out.println("New seed...");
			}
			if (g.subsumes(e)) {
				FeatureTerm c = e.clone(dm, o);
				if (c.getName() != null) {
					c.setName(null);
				}
				while (c != null) {

					Argument a = new Argument(new Rule(c, solution, 0.0f, 0));
					if (DEBUG >= 2) {
						System.out.println(c.toStringNOOS(dm));
					}
					if (DEBUG >= 2) {
						System.out.println("Generalization confidence: " + aa.degree(a));
					}
					if (aa.accepted(a)) {
						a.m_rule.reliability = aa.degree(a);
						H.add(a);
					}

					List<FeatureTerm> G = FTRefinement.getGeneralizations(c, dm, o);
					List<FeatureTerm> G2 = new LinkedList<FeatureTerm>();

					if (DEBUG >= 2) {
						System.out.println(G.size() + " generalizations.");
					}

					for (FeatureTerm h : G) {
						if (g.subsumes(h) && !h.subsumes(g)) {
							boolean attacked = false;
							for (Argument a2 : acceptedArguments) {
								if (!a2.m_rule.solution.equivalents(solution) && h.subsumes(a2.m_rule.pattern)) {
									attacked = true;
									break;
								}
							}
							if (!attacked) {
								G2.add(h);
							}
						}
					}

					if (DEBUG >= 2) {
						System.out.println(G2.size() + " filtered generalizations.");
					}

					c = null;
					{
						FeatureTerm best_c = null;
						float best_conf = 0.0f;
						float max_c = 0.0f;
						float min_c = 1.0f;

						for (FeatureTerm c2 : G2) {
							Argument a2 = new Argument(new Rule(c2, solution, 0.0f, 0));
							float conf = aa.degree(a2);
							if (best_c == null || conf > best_conf) {
								best_c = c2;
								best_conf = conf;
							}

							if (conf > max_c)
								max_c = conf;
							if (conf < min_c)
								min_c = conf;
						}

						c = best_c;
						if (DEBUG >= 2) {
							System.out.println("Generalization confidence: " + best_conf + " the range was [" + min_c + "-" + max_c + "] (with " + G2.size()
									+ ")");
						}
					}
				}
			}
		}

		{
			Argument best_a = null;
			float best_conf = 0.0f;

			for (Argument a : H) {
				float conf = a.m_rule.reliability;
				if (best_a == null || conf > best_conf) {
					best_a = a;
					best_conf = conf;
				}
			}

			return best_a;
		}
	}

	/**
	 * ABUI 2.
	 * 
	 * This is the ABUI induction algorithm as explained in our "Concept Convergence" paper submitted to AAMAS 2010: -
	 * returns an argument which is more specific than "g", and which is not attacked by any argument in
	 * "acceptedArguments"
	 * 
	 * NOTE: this algorithm uses the definition of "attack" present in the paper, not the one in "Common"
	 * 
	 * @param uncoveredPositiveDescriptions
	 *            the uncovered positive descriptions
	 * @param negativeDescriptions
	 *            the negative descriptions
	 * @param positiveDescriptions
	 *            the positive descriptions
	 * @param solution
	 *            the solution
	 * @param acceptedArguments
	 *            the accepted arguments
	 * @param g
	 *            the g
	 * @param aa
	 *            the aa
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @return the argument
	 * @throws Exception
	 *             the exception
	 */
	public Argument ABUI2(List<FeatureTerm> uncoveredPositiveDescriptions, List<FeatureTerm> negativeDescriptions, List<FeatureTerm> positiveDescriptions,
			FeatureTerm solution, Collection<Argument> acceptedArguments, FeatureTerm g, ArgumentAcceptability aa, Ontology o, FTKBase dm) throws Exception {

		List<Argument> H = new LinkedList<Argument>(); // This list will contain the candidate arguments
		if (acceptedArguments == null)
			acceptedArguments = new LinkedList<Argument>();

		ABUI_call_count++;

		if (DEBUG >= 1) {
			System.out.println("ABUI2: UPD: " + uncoveredPositiveDescriptions.size() + " P/N:" + positiveDescriptions.size() + "/"
					+ negativeDescriptions.size());
		}

		for (FeatureTerm e : uncoveredPositiveDescriptions) {
			List<FeatureTerm> notYetCoveredPositiveDescriptions = new LinkedList<FeatureTerm>();
			notYetCoveredPositiveDescriptions.addAll(positiveDescriptions);

			if (DEBUG >= 2) {
				System.out.println("ABUI2: New seed...");
			}
			if (g.subsumes(e)) {
				FeatureTerm c = e.clone(dm, o);
				if (c.getName() != null) {
					c.setName(null);
				}
				while (c != null) {
					// remove all the already covered:
					{
						List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
						for (FeatureTerm d : notYetCoveredPositiveDescriptions)
							if (c.subsumes(d)) {
								toDelete.add(d);
							}
						notYetCoveredPositiveDescriptions.removeAll(toDelete);
					}

					Argument a = new Argument(new Rule(c, solution, 0.0f, 0));
					if (DEBUG >= 2) {
						System.out.println(c.toStringNOOS(dm));
					}
					if (DEBUG >= 2) {
						System.out.println("ABUI2: Generalization confidence: " + aa.degree(a));
					}
					if (aa.accepted(a)) {
						a.m_rule.reliability = aa.degree(a);
						H.add(a);
					}

					List<FeatureTerm> G = new LinkedList<FeatureTerm>();

					for (FeatureTerm d : notYetCoveredPositiveDescriptions) {
						FeatureTerm au = FTAntiunification.simpleAntiunification(c, d, o, dm);
						if (au != null)
							G.add(au);
					}
					List<FeatureTerm> G2 = new LinkedList<FeatureTerm>();

					if (DEBUG >= 2) {
						System.out.println("ABUI2: " + G.size() + " generalizations.");
					}

					for (FeatureTerm h : G) {
						if (g.subsumes(h) && !h.subsumes(g)) {
							boolean attacked = false;
							for (Argument a2 : acceptedArguments) {
								if (!a2.m_rule.solution.equivalents(solution) && h.subsumes(a2.m_rule.pattern)) {
									attacked = true;
									break;
								}
							}
							if (!attacked) {
								G2.add(h);
							}
						}
					}

					if (DEBUG >= 2) {
						System.out.println("ABUI2: " + G2.size() + " filtered generalizations.");
					}

					c = null;
					{
						FeatureTerm best_c = null;
						float best_conf = 0.0f;
						float max_c = 0.0f;
						float min_c = 1.0f;

						for (FeatureTerm c2 : G2) {
							Argument a2 = new Argument(new Rule(c2, solution, 0.0f, 0));
							float conf = aa.degree(a2);
							if (best_c == null || conf > best_conf) {
								best_c = c2;
								best_conf = conf;
							}

							if (conf > max_c)
								max_c = conf;
							if (conf < min_c)
								min_c = conf;
						}

						c = best_c;
						if (DEBUG >= 2) {
							System.out.println("ABUI2: Generalization confidence: " + best_conf + " the range was [" + min_c + "-" + max_c + "] (with "
									+ G2.size() + ")");
						}
					}
				}
			}
		}

		{
			Argument best_a = null;
			float best_conf = 0.0f;

			for (Argument a : H) {
				float conf = a.m_rule.reliability;
				if (best_a == null || conf > best_conf) {
					best_a = a;
					best_conf = conf;
				}
			}

			return best_a;
		}
	}
	
	public RuleHypothesis learnConceptABUI(Collection<FeatureTerm> examples, Collection<FeatureTerm> solution, Collection<Argument> acceptedArguments,
			ArgumentAcceptability aa, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		
		RuleHypothesis output = new RuleHypothesis();
		ABUI learner = new ABUI();
		for(FeatureTerm s : solution){
			for(Rule r : learner.learnConceptABUI(examples, s, acceptedArguments, aa, dp, sp, o, dm).getRules()){
				output.addRule(r);
			}
		}
		return output;
	}
	
	public RuleHypothesis initialTraining(Collection<FeatureTerm> examples, Collection<FeatureTerm> solution, Path dp, Path sp, Ontology o, FTKBase dm) throws Exception {
		RuleHypothesis output = new RuleHypothesis();
		ABUI learner = new ABUI();
		List<Argument> arguments = new ArrayList<Argument>();
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(examples, sp, dp, LPkg.ABUI_THRESHOLD);
		//ArgumentAcceptability aa = new AccuracyArgumentAcceptability(examples, sp, dp, ExpFileManager.abui_threshold);
		for(FeatureTerm s : solution){
			for(Rule r : learner.learnConceptABUI(examples, s, arguments, aa, dp, sp, o, dm).getRules()){
				output.addRule(r);
			}
		}
		return output;
	}
	
	public ContrastSet makeContrastSet(Collection<FeatureTerm> examples) throws Exception {
		RuleHypothesis seed = initialTraining(examples, LPkg.different_solutions(), LPkg.description_path(), LPkg.solution_path(), LPkg.ontology(), LPkg.dm());
		HashSet<Example> context = new HashSet<Example>();
		for(FeatureTerm f : examples){
			for(Symbol sb : LPkg.description_path().features){
				context.add(new Example(f.featureValue(sb)));
			}
		}
		HashSet<Concept> concepts = new HashSet<Concept>();
		for(FeatureTerm sf : LPkg.different_solutions()){
			Sign s = new Sign(LPkg.solution_path().toString()+":"+sf.toStringNOOS(LPkg.dm()));
			HashSet<Generalization> I = new HashSet<Generalization>();
			HashSet<Example> E = new HashSet<Example>();
			for(Rule r : seed.getRules()){
				if(sf.equivalents(r.solution)){
					I.add(new Generalization(r.pattern));
					for(Example e : context){
						if(r.pattern.subsumes(e.representation()))
							E.add(e);
					}
				}
			}
			if(!I.isEmpty() && !E.isEmpty())
				concepts.add(new Concept(s, I, E));
		}
		ContrastSet cset = new ContrastSet(concepts,context);
		return cset;
	}

	public static Set<Generalization> mergedDefinition(Set<Example> context, Concept c1, Concept c2) {
		
		ABUI learner = new ABUI();
		
		HashSet<Generalization> intensionalDefinition = new HashSet<Generalization>();
		Set<FeatureTerm> extensionalDefinition = new HashSet<FeatureTerm>();
		Set<FeatureTerm> solutions = new HashSet<FeatureTerm>();
		
		FeatureTerm notSolutionToken = null;
		FeatureTerm solutionToken = null;
		
		try {
			notSolutionToken = new TermFeatureTerm(new Symbol("not_the_solution"), LPkg.solution_sort());
			LPkg.dm().addFT(notSolutionToken);
			solutions.add(notSolutionToken);
			solutionToken = new TermFeatureTerm(new Symbol("the_solution"), LPkg.solution_sort());
			LPkg.dm().addFT(solutionToken);
			solutions.add(solutionToken);
		} catch (FeatureTermException e) {
			e.printStackTrace();
		}
		
		for(Example e : context){
			if(c1.covers(e) || c2.covers(e)){
				FeatureTerm f = LPkg.createFeature(e.featureterm, solutionToken);
				extensionalDefinition.add(f);
			}
			else{
				FeatureTerm f = LPkg.createFeature(e.featureterm, notSolutionToken);
				extensionalDefinition.add(f);
			}
		}
		
		List<Argument> arguments = new ArrayList<Argument>();
		ArgumentAcceptability aa = new LaplaceArgumentAcceptability(extensionalDefinition, LPkg.solution_path(), LPkg.description_path(), LPkg.ABUI_THRESHOLD);
		
		try {
			for(Rule r : learner.learnConceptABUI(extensionalDefinition, solutions, arguments, aa, LPkg.description_path(), LPkg.solution_path(), LPkg.ontology(), LPkg.dm()).getRules()){
				intensionalDefinition.add(new Generalization(r.pattern));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return intensionalDefinition;
	}

}
