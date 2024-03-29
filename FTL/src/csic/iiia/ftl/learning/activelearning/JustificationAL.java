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
  
 package csic.iiia.ftl.learning.activelearning;

import java.util.LinkedList;
import java.util.List;

import java.util.HashSet;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Path;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.Pair;
import csic.iiia.ftl.learning.core.InformationMeasurement;
import csic.iiia.ftl.learning.core.Prediction;

// TODO: Auto-generated Javadoc
/**
 * The Class JustificationAL.
 */
public class JustificationAL extends QueryByCommittee {

	/** The Constant CONFIDENCE_AVERAGE. */
	public final static int CONFIDENCE_AVERAGE = 0; // Averages confidences for each cluster

	/** The Constant CONFIDENCE_SUM. */
	public final static int CONFIDENCE_SUM = 1; // Adds confidences for each cluster

	/** The Constant CONFIDENCE_BAYES. */
	public final static int CONFIDENCE_BAYES = 2; // A better estimation to turn confidences into probabilities

	/** The Constant DISAGREEMENT_FIRST_SECOND. */
	public final static int DISAGREEMENT_FIRST_SECOND = 0;

	/** The Constant DISAGREEMENT_ENTROPY. */
	public final static int DISAGREEMENT_ENTROPY = 1;

	/** The m_confidence method. */
	int m_confidenceMethod = 0;

	/** The m_disageement method. */
	int m_disageementMethod = 0;

	/**
	 * Instantiates a new justification al.
	 * 
	 * @param classifiers
	 *            the classifiers
	 * @param confidenceMethod
	 *            the confidence method
	 * @param disageementMethod
	 *            the disageement method
	 */
	public JustificationAL(int classifiers, int confidenceMethod, int disageementMethod) {
		super(classifiers);
		m_confidenceMethod = confidenceMethod;
		m_disageementMethod = disageementMethod;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.activelearning.QueryByCommittee#toString()
	 */
	public String toString() {
		String conf[] = { "average", "sum", "Bayes" };
		String disa[] = { "FS", "entropy" };
		return "JustificationAL(" + m_nClassifiers + "-" + conf[m_confidenceMethod] + "-" + disa[m_disageementMethod] + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see csic.iiia.ftl.learning.activelearning.QueryByCommittee#disagreement(java.util.List, java.util.List,
	 * java.util.List, csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.Path, csic.iiia.ftl.base.core.FTKBase)
	 */
	public double disagreement(List<Prediction> predictions, List<List<FeatureTerm>> trainingSets, List<FeatureTerm> differentSolutions, Path dp, Path sp,
			FTKBase dm) throws FeatureTermException {
		List<Pair<FeatureTerm, Double>> solutions = new LinkedList<Pair<FeatureTerm, Double>>();
		List<Pair<FeatureTerm, Pair<Double, Integer>>> solutionClusters = new LinkedList<Pair<FeatureTerm, Pair<Double, Integer>>>();

		// Compute solution confidence:
		for (Prediction p : predictions) {
			FeatureTerm solution = p.getSolution();

			solutions
					.add(new Pair<FeatureTerm, Double>(solution, justificationConfidenceQuick(p.justifications.get(solution), solution, trainingSets, dp, sp)));
		}

		for (FeatureTerm s : differentSolutions) {
			Pair<FeatureTerm, Pair<Double, Integer>> cluster = new Pair<FeatureTerm, Pair<Double, Integer>>(s, new Pair<Double, Integer>(0.0, 0));
			solutionClusters.add(cluster);
		}

		switch (m_confidenceMethod) {
		case CONFIDENCE_AVERAGE:
			for (Pair<FeatureTerm, Double> solution : solutions) {
				Pair<FeatureTerm, Pair<Double, Integer>> cluster = null;
				for (Pair<FeatureTerm, Pair<Double, Integer>> tmp : solutionClusters) {
					if (tmp.mA.equals(solution.mA)) {
						cluster = tmp;
						break;
					}
				}
				if (cluster == null) {
					cluster = new Pair<FeatureTerm, Pair<Double, Integer>>(solution.mA, new Pair<Double, Integer>(0.0, 0));
					solutionClusters.add(cluster);
				}
				cluster.mB.mA += solution.mB;
				cluster.mB.mB++;
			}

			// average:
			for (Pair<FeatureTerm, Pair<Double, Integer>> tmp : solutionClusters) {
				tmp.mB.mA /= tmp.mB.mB;
			}
			break;
		case CONFIDENCE_SUM:
			for (Pair<FeatureTerm, Double> solution : solutions) {
				Pair<FeatureTerm, Pair<Double, Integer>> cluster = null;
				for (Pair<FeatureTerm, Pair<Double, Integer>> tmp : solutionClusters) {
					if (tmp.mA.equals(solution.mA)) {
						cluster = tmp;
						break;
					}
				}
				if (cluster == null) {
					cluster = new Pair<FeatureTerm, Pair<Double, Integer>>(solution.mA, new Pair<Double, Integer>(0.0, 0));
					solutionClusters.add(cluster);
				}
				cluster.mB.mA += solution.mB;
				cluster.mB.mB++;
			}
			break;
		case CONFIDENCE_BAYES:
			for (Pair<FeatureTerm, Double> solution : solutions) {
				for (Pair<FeatureTerm, Pair<Double, Integer>> cluster : solutionClusters) {
					if (cluster.mA.equals(solution.mA)) {
						cluster.mB.mA += solution.mB;
					} else {
						cluster.mB.mA += (1 - solution.mB) / (differentSolutions.size() - 1);
					}
				}
			}

			for (Pair<FeatureTerm, Pair<Double, Integer>> cluster : solutionClusters) {
				cluster.mB.mA /= solutions.size();
			}
			break;
		}

		// Sort them:
		{
			int l = solutionClusters.size();
			boolean change = true;
			while (change) {
				change = false;
				for (int i = 0; i < l - 1; i++) {
					Pair<FeatureTerm, Pair<Double, Integer>> u1 = solutionClusters.get(i);
					Pair<FeatureTerm, Pair<Double, Integer>> u2 = solutionClusters.get(i + 1);

					if (u1.mB.mA < u2.mB.mA) {
						FeatureTerm tmp1 = u1.mA;
						Pair<Double, Integer> tmp2 = u1.mB;
						u1.mA = u2.mA;
						u1.mB = u2.mB;
						u2.mA = tmp1;
						u2.mB = tmp2;
						change = true;
					}
				}
			}
		}

		switch (m_disageementMethod) {
		case DISAGREEMENT_FIRST_SECOND: {
			double c1 = 0;
			double c2 = 0;
			double max = 1.0;

			if (m_confidenceMethod == CONFIDENCE_SUM)
				max = trainingSets.size();

			if (solutionClusters.size() >= 1)
				c1 = solutionClusters.get(0).mB.mA;
			if (solutionClusters.size() >= 2)
				c2 = solutionClusters.get(1).mB.mA;

			// for(Pair<FeatureTerm,Pair<Double,Integer>> c:solutionClusters) {
			// System.out.print("<" + c.m_a.toStringNOOS(dm) + ", " + c.m_b.m_a + "> ");
			// }
			// System.out.println(" -->> " + c1 + " - " + c2 + " -> " + (1.0-(c1-c2)));

			return max - (c1 - c2);
		}

		case DISAGREEMENT_ENTROPY: {
			double[] p = new double[differentSolutions.size()];
			int i = 0;
			for (Pair<FeatureTerm, Pair<Double, Integer>> cluster : solutionClusters) {
				p[i++] = cluster.mB.mA;
			}
			double h = InformationMeasurement.entropyD(i, p);

			// System.out.print("Entropy: [");
			// for(double tmp:p) System.out.print(tmp + " ");
			// System.out.println("] -> " + h);
			return h;
		}
		}

		return 0;
	}

	/**
	 * Justification confidence quick.
	 * 
	 * @param pattern
	 *            the pattern
	 * @param solution
	 *            the solution
	 * @param trainingSets
	 *            the training sets
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @return the double
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	private double justificationConfidenceQuick(FeatureTerm pattern, FeatureTerm solution, List<List<FeatureTerm>> trainingSets, Path dp, Path sp)
			throws FeatureTermException {
		HashSet<FeatureTerm> all = new HashSet<FeatureTerm>();
		HashSet<FeatureTerm> subsumed = new HashSet<FeatureTerm>();
		double aye = 0.0;
		double nay = 0.0;

		if (pattern == null)
			return 0.5;

		for (List<FeatureTerm> ts : trainingSets)
			all.addAll(ts);

		for (FeatureTerm c : all) {
			FeatureTerm d = c.readPath(dp);
			if (pattern.subsumes(d))
				subsumed.add(c);
		}

		for (List<FeatureTerm> ts : trainingSets) {

			for (FeatureTerm c : ts) {
				if (subsumed.contains(c)) {
					FeatureTerm s = c.readPath(sp);
					if (s.equals(solution))
						aye++;
					else
						nay++;
				}
			}
		}

		return (1.0 + aye) / (2.0 + aye + nay);
	}
}
