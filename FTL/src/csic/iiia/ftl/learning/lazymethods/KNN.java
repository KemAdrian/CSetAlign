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
  
 package csic.iiia.ftl.learning.lazymethods;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import csic.iiia.ftl.base.core.FTKBase;
import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.Ontology;
import csic.iiia.ftl.base.core.Path;
import csic.iiia.ftl.base.utils.Pair;
import csic.iiia.ftl.learning.core.Prediction;
import csic.iiia.ftl.learning.lazymethods.similarity.Distance;

// TODO: Auto-generated Javadoc
/**
 * The Class KNN.
 */
public class KNN {

	/**
	 * The Class SDComparator.
	 */
	static class SDComparator implements Comparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object arg0, Object arg1) {
			double o0 = ((Pair<Pair<FeatureTerm, FeatureTerm>, Double>) arg0).mB;
			double o1 = ((Pair<Pair<FeatureTerm, FeatureTerm>, Double>) arg1).mB;

			if (o0 > o1)
				return 1;
			else if (o0 < o1)
				return -1;
			else
				return 0;
		}

	}

	/**
	 * Predict.
	 * 
	 * @param problem
	 *            the problem
	 * @param cases
	 *            the cases
	 * @param description_path
	 *            the description_path
	 * @param solution_path
	 *            the solution_path
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param K
	 *            the k
	 * @param d
	 *            the d
	 * @return the prediction
	 * @throws Exception
	 *             the exception
	 */
	public static Prediction predict(FeatureTerm problem, List<FeatureTerm> cases, Path description_path, Path solution_path, Ontology o, FTKBase dm, int K,
			Distance d) throws Exception {
		List<Pair<Pair<FeatureTerm, FeatureTerm>, Double>> solutions_distances = new LinkedList<Pair<Pair<FeatureTerm, FeatureTerm>, Double>>();

		for (FeatureTerm c : cases) {
			FeatureTerm description = c.readPath(description_path);
			FeatureTerm solution = c.readPath(solution_path);
			double distance = d.distance(problem, description, o, dm);

			solutions_distances.add(new Pair<Pair<FeatureTerm, FeatureTerm>, Double>(new Pair<FeatureTerm, FeatureTerm>(c, solution), distance));
		}

		Collections.sort(solutions_distances, new KNN.SDComparator());

		Prediction p = new Prediction();
		p.problem = problem;

		for (int i = 0; i < K; i++) {
			Pair<Pair<FeatureTerm, FeatureTerm>, Double> solution_distance = solutions_distances.remove(0);

			System.out.println(solution_distance.mA.mA.getName() + " - " + solution_distance.mA.mB.toStringNOOS(dm) + " -> " + solution_distance.mB);

			if (!p.solutions.contains(solution_distance.mA.mB)) {
				p.solutions.add(solution_distance.mA.mB);
				p.justifications.put(solution_distance.mA.mB, null);
				p.support.put(solution_distance.mA.mB, 0);
			}

			p.support.put(solution_distance.mA.mB, 1 + p.support.get(solution_distance.mA.mB));
		}

		return p;
	}

	/*
	 * This method returns different predictions for different values of K at the same time
	 */
	/**
	 * Multiple predictions.
	 * 
	 * @param problem
	 *            the problem
	 * @param cases
	 *            the cases
	 * @param description_path
	 *            the description_path
	 * @param solution_path
	 *            the solution_path
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param Kl
	 *            the kl
	 * @param d
	 *            the d
	 * @return the list
	 * @throws Exception
	 *             the exception
	 */
	public static List<Prediction> multiplePredictions(FeatureTerm problem, List<FeatureTerm> cases, Path description_path, Path solution_path, Ontology o,
			FTKBase dm, List<Integer> Kl, Distance d) throws Exception {
		List<Pair<Pair<FeatureTerm, FeatureTerm>, Double>> solutions_distances = new LinkedList<Pair<Pair<FeatureTerm, FeatureTerm>, Double>>();

		for (FeatureTerm c : cases) {
			FeatureTerm description = c.readPath(description_path);
			FeatureTerm solution = c.readPath(solution_path);
			double distance = d.distance(problem, description, o, dm);

			solutions_distances.add(new Pair<Pair<FeatureTerm, FeatureTerm>, Double>(new Pair<FeatureTerm, FeatureTerm>(c, solution), distance));
		}

		Collections.sort(solutions_distances, new KNN.SDComparator());
		List<Prediction> pl = new LinkedList<Prediction>();

		for (Integer K : Kl) {
			Prediction p = new Prediction();
			p.problem = problem;
			System.out.println(" K = " + K + " ------------------------- ");
			for (int i = 0; i < K; i++) {
				Pair<Pair<FeatureTerm, FeatureTerm>, Double> solution_distance = solutions_distances.get(i);

				System.out.println(solution_distance.mA.mA.getName() + " - " + solution_distance.mA.mB.toStringNOOS(dm) + " -> " + solution_distance.mB);

				if (!p.solutions.contains(solution_distance.mA.mB)) {
					p.solutions.add(solution_distance.mA.mB);
					p.justifications.put(solution_distance.mA.mB, null);
					p.support.put(solution_distance.mA.mB, 0);
				}

				p.support.put(solution_distance.mA.mB, 1 + p.support.get(solution_distance.mA.mB));
			}
			pl.add(p);
		}

		return pl;
	}

}
