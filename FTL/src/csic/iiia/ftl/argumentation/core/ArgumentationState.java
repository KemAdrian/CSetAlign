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

import java.util.LinkedList;
import java.util.List;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.Pair;
import csic.iiia.ftl.learning.core.Rule;

// TODO: Auto-generated Javadoc
/**
 * The Class ArgumentationState.
 * 
 * @author santi
 */
public class ArgumentationState {

	/** The m_base arguments. */
	List<Pair<String, ArgumentationTree>> m_baseArguments = new LinkedList<Pair<String, ArgumentationTree>>();

	/** The m_retracted arguments. */
	List<Pair<String, ArgumentationTree>> m_retractedArguments = new LinkedList<Pair<String, ArgumentationTree>>();

	/**
	 * Instantiates a new argumentation state.
	 */
	public ArgumentationState() {

	}

	/**
	 * Instantiates a new argumentation state.
	 * 
	 * @param as
	 *            the as
	 */
	public ArgumentationState(ArgumentationState as) {
		for (Pair<String, ArgumentationTree> p : as.m_baseArguments) {
			m_baseArguments.add(new Pair<String, ArgumentationTree>(p.mA, p.mB.clone()));
		}
		for (Pair<String, ArgumentationTree> p : as.m_retractedArguments) {
			m_retractedArguments.add(new Pair<String, ArgumentationTree>(p.mA, p.mB.clone()));
		}
	}

	/**
	 * Adds the new root.
	 * 
	 * @param agent
	 *            the agent
	 * @param root
	 *            the root
	 */
	public void addNewRoot(String agent, Argument root) {
		m_baseArguments.add(new Pair<String, ArgumentationTree>(agent, new ArgumentationTree(root)));
	}

	/**
	 * Gets the examples.
	 * 
	 * @return the examples
	 */
	public List<FeatureTerm> getExamples() {
		List<FeatureTerm> examples = new LinkedList<FeatureTerm>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			examples.addAll(aNode.mB.getExamples());
		}
		return examples;
	}

	/**
	 * Gets the examples sent to agent.
	 * 
	 * @param name
	 *            the name
	 * @return the examples sent to agent
	 */
	public List<FeatureTerm> getExamplesSentToAgent(String name) {
		List<FeatureTerm> examples = new LinkedList<FeatureTerm>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			examples.addAll(aNode.mB.getExamplesSentToAgent(name));
		}
		return examples;
	}

	/**
	 * Gets the roots.
	 * 
	 * @param agent
	 *            the agent
	 * @return the roots
	 */
	public List<Argument> getRoots(String agent) {
		List<Argument> roots = new LinkedList<Argument>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mA.equals(agent)) {
				roots.add(aNode.mB.getRoot());
			}
		}
		return roots;
	}

	/**
	 * Gets the trees.
	 * 
	 * @return the trees
	 */
	public List<ArgumentationTree> getTrees() {
		List<ArgumentationTree> trees = new LinkedList<ArgumentationTree>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			trees.add(aNode.mB);
		}
		return trees;
	}

	/**
	 * Gets the retracted trees.
	 * 
	 * @return the retracted trees
	 */
	public List<ArgumentationTree> getRetractedTrees() {
		List<ArgumentationTree> trees = new LinkedList<ArgumentationTree>();
		for (Pair<String, ArgumentationTree> aNode : m_retractedArguments) {
			trees.add(aNode.mB);
		}
		return trees;
	}

	/**
	 * Gets the all trees.
	 * 
	 * @return the all trees
	 */
	public List<ArgumentationTree> getAllTrees() {
		List<ArgumentationTree> trees = new LinkedList<ArgumentationTree>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			trees.add(aNode.mB);
		}
		for (Pair<String, ArgumentationTree> aNode : m_retractedArguments) {
			trees.add(aNode.mB);
		}
		return trees;
	}

	/**
	 * Gets the trees.
	 * 
	 * @param agent
	 *            the agent
	 * @return the trees
	 */
	public List<ArgumentationTree> getTrees(String agent) {
		List<ArgumentationTree> trees = new LinkedList<ArgumentationTree>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mA.equals(agent)) {
				trees.add(aNode.mB);
			}
		}
		return trees;
	}

	/**
	 * Retract root.
	 * 
	 * @param a
	 *            the a
	 */
	public void retractRoot(Argument a) {
		List<Pair<String, ArgumentationTree>> toDelete = new LinkedList<Pair<String, ArgumentationTree>>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mB.getRoot() == a)
				toDelete.add(aNode);
		}
		m_baseArguments.removeAll(toDelete);
		m_retractedArguments.addAll(toDelete);
	}

	/**
	 * Gets the unacceptable.
	 * 
	 * @param name
	 *            the name
	 * @param aa
	 *            the aa
	 * @param agents
	 *            the agents
	 * @return the unacceptable
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public List<Pair<Argument, ArgumentationTree>> getUnacceptable(String name, ArgumentAcceptability aa, List<ArgumentationAgent> agents)
			throws FeatureTermException {
		List<Pair<Argument, ArgumentationTree>> args = new LinkedList<Pair<Argument, ArgumentationTree>>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (!aNode.mA.equals(name) && !aNode.mB.defeatedP()) {
				List<Argument> defenders = aNode.mB.getDefenders(agents);
				// System.out.println(defenders.size() + " defenders for A"+aNode.m_b.getRoot().m_ID+" generated by " +
				// aNode.m_a);
				if (defenders.size() == 0) {
					System.err.println("getUnacceptable: defenders set is empty, ut it can't be!");
					System.err.println(aNode.mB);
				}
				for (Argument a : defenders) {
					if (!aa.accepted(a))
						args.add(new Pair<Argument, ArgumentationTree>(a, aNode.mB));
				}
			}
		}
		return args;
	}

	/**
	 * Gets the acceptable.
	 * 
	 * @param name
	 *            the name
	 * @param aa
	 *            the aa
	 * @param agents
	 *            the agents
	 * @return the acceptable
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public List<Pair<Argument, ArgumentationTree>> getAcceptable(String name, ArgumentAcceptability aa, List<ArgumentationAgent> agents)
			throws FeatureTermException {
		List<Pair<Argument, ArgumentationTree>> args = new LinkedList<Pair<Argument, ArgumentationTree>>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (!aNode.mA.equals(name)) {
				List<Argument> defenders = aNode.mB.getDefenders(agents);
				for (Argument a : defenders) {
					if (aa.accepted(a))
						args.add(new Pair<Argument, ArgumentationTree>(a, aNode.mB));
				}
			}
		}
		return args;
	}

	/**
	 * Gets the settled.
	 * 
	 * @param agents
	 *            the agents
	 * @return the settled
	 */
	public List<Argument> getSettled(List<ArgumentationAgent> agents) {
		List<Argument> args = new LinkedList<Argument>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			for (Argument a : aNode.mB.getRuleArguments()) {
				if (aNode.mB.settledP(a, agents) && !aNode.mB.defeatedP(a)) {
					args.add(a);
				}
			}
		}
		for (Pair<String, ArgumentationTree> aNode : m_retractedArguments) {
			for (Argument a : aNode.mB.getRuleArguments()) {
				if (aNode.mB.settledP(a, agents) && !aNode.mB.defeatedP(a)) {
					args.add(a);
				}
			}
		}
		return args;
	}

	/**
	 * Gets the undefeated arguments.
	 * 
	 * @return the undefeated arguments
	 */
	public List<Argument> getUndefeatedArguments() {
		List<Argument> args = new LinkedList<Argument>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			for (Argument a : aNode.mB.getArguments()) {
				if (a != null) {
					if (a.m_type == Argument.ARGUMENT_RULE && !aNode.mB.defeatedP(a)) {
						args.add(a);
					}
				} else {
					System.err.println("null argument in the argument list of a tree!!!");
				}
			}
		}
		for (Pair<String, ArgumentationTree> aNode : m_retractedArguments) {
			for (Argument a : aNode.mB.getArguments()) {
				if (a != null) {
					if (a.m_type == Argument.ARGUMENT_RULE && !aNode.mB.defeatedP(a)) {
						args.add(a);
					}
				} else {
					System.err.println("null argument in the argument list of a tree!!!");
				}
			}
		}
		return args;
	}

	/**
	 * Gets the defeated.
	 * 
	 * @param name
	 *            the name
	 * @return the defeated
	 */
	public List<ArgumentationTree> getDefeated(String name) {
		List<ArgumentationTree> args = new LinkedList<ArgumentationTree>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mA.equals(name) && aNode.mB.defeatedP()) {
				args.add(aNode.mB);
			}
		}
		return args;
	}

	/**
	 * Gets the defeated unsettled.
	 * 
	 * @param name
	 *            the name
	 * @param agents
	 *            the agents
	 * @return the defeated unsettled
	 */
	public List<ArgumentationTree> getDefeatedUnsettled(String name, List<ArgumentationAgent> agents) {
		List<ArgumentationTree> args = new LinkedList<ArgumentationTree>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mA.equals(name) && aNode.mB.defeatedP() && !aNode.mB.settledP(aNode.mB.getRoot(), agents)) {
				args.add(aNode.mB);
			}
		}
		return args;
	}

	// This method is called when the agent "name" increases it's example base:
	/**
	 * Un settle.
	 * 
	 * @param name
	 *            the name
	 */
	public void unSettle(String name) {
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mA.equals(name))
				aNode.mB.unSettle();
		}
	}

	// Gets the root argument corresponding to the rule 'r', if it exists:
	/**
	 * Gets the root.
	 * 
	 * @param r
	 *            the r
	 * @return the root
	 */
	public Argument getRoot(Rule r) {
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mB.getRoot().m_rule == r)
				return aNode.mB.getRoot();
		}
		return null;
	}

	// Gets the tree with root argument corresponding to the rule 'r', if it exists:
	/**
	 * Gets the tree.
	 * 
	 * @param r
	 *            the r
	 * @return the tree
	 */
	public ArgumentationTree getTree(Rule r) {
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mB.getRoot().m_rule == r)
				return aNode.mB;
		}
		return null;
	}

	/**
	 * Retract unacceptable.
	 * 
	 * @param name
	 *            the name
	 * @param aa
	 *            the aa
	 * @return the int
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public int retractUnacceptable(String name, ArgumentAcceptability aa) throws FeatureTermException {
		List<Pair<String, ArgumentationTree>> toRetract = new LinkedList<Pair<String, ArgumentationTree>>();
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			if (aNode.mB.retractUnacceptable(name, aa)) {
				// root retracted:
				toRetract.add(aNode);
			}
		}
		for (Pair<String, ArgumentationTree> aNode : toRetract) {
			m_baseArguments.remove(aNode);
			m_retractedArguments.add(aNode);
		}
		return toRetract.size();
	}

	/**
	 * To string.
	 * 
	 * @param agents
	 *            the agents
	 * @return the string
	 */
	public String toString(List<ArgumentationAgent> agents) {
		String tmp = "Maintained:\n";
		for (Pair<String, ArgumentationTree> aNode : m_baseArguments) {
			tmp += aNode.mB.toString(agents);
		}
		tmp += "Retracted:\n";
		for (Pair<String, ArgumentationTree> aNode : m_retractedArguments) {
			tmp += aNode.mB.toString(agents);
		}
		return tmp;
	}

}
