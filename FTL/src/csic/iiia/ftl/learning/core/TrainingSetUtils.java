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
  
 package csic.iiia.ftl.learning.core;

import csic.iiia.ftl.base.core.*;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.Pair;
import csic.iiia.ftl.base.utils.Sampler;
import evaluation.ExpFileManager;
import tools.LPkg;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

// TODO: Auto-generated Javadoc
/**
 * The Class TrainingSetUtils.
 * 
 * @author santi
 */
public class TrainingSetUtils {

	/** The DEBUG. */
	public static int DEBUG = 0;
	public static int LIMIT = 0;
	public static int NB_EX = 0;
	public static int NB_DOMAIN = 0;
	public static int REDUNDANCY = 0;
	
	public static List<List<FeatureTerm>> createRandomTrainingSet(Collection<FeatureTerm> examples, Collection<FeatureTerm> different_solutions, Path s_p, int m_size_min){
		// Create a map for classification of examples according to their solutions
		Map<FeatureTerm, List<FeatureTerm>> classified_training_set = new HashMap<>();
		// Create the keys for the classified map
		for (FeatureTerm solution : different_solutions) {
			classified_training_set.put(solution, new ArrayList<>());
		}
		// Classify the training set
		for (FeatureTerm example : examples) {
				classified_training_set.get(example.readPath(s_p)).add(example);
		}
		System.out.println("     > The overall set of examples is the following:");
		for (FeatureTerm solution : classified_training_set.keySet()) {
			System.out.println("       > The concept " + solution.toStringNOOS() + " has "
					+ classified_training_set.get(solution).size() + " examples");
		}
		// Get the list of entries
		List<Entry<FeatureTerm, List<FeatureTerm>>> list = new ArrayList<>();
		for (Entry<FeatureTerm, List<FeatureTerm>> entry : classified_training_set.entrySet()) {
			if (entry.getValue().size() >= m_size_min * 2)
				list.add(entry);
		}
		// Create the order of priority for disagreements creation
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < 4; i++)
			order.add(i);
		Collections.shuffle(order);
		// Get in memory the max number of concepts
		int maxd = list.size();
		// Get in memory the number of used concepts
		int used = 0;
		// Prepare the results
		int[] disagreements = { 0, 0, 0, 0 };
		// For each type
		for (int i = 0; i < 4; i++) {
			// Get the number of concepts that can still be used
			int disp = maxd - used;
			// Get max of this concept type that can be done
			int maxc = 0;
			switch (order.get(i)) {
				case 0:
					maxc = disp / 3;
					break;
				case 1:
				case 3:
					maxc = disp / 2;
					break;
				case 2:
					maxc = disp;
					break;
				default:
					break;
			}
			// Set the number of this type of disagreements
			int nb = new Random().nextInt(maxc + 1);
			disagreements[order.get(i)] = nb;
			// Update the used
			switch (order.get(i)) {
			case 0:
				used += nb * 3;
				break;
			case 1:
				used += nb * 2;
				break;
			case 2:
				used += nb;
				break;
			case 3:
				used += nb * 2;
				break;
			default:
				break;
			}			
		}
		return createTrainingSet(examples, different_solutions, s_p, m_size_min, disagreements[0], disagreements[1], disagreements[2], disagreements[3]);
	}
	
	public static List<List<FeatureTerm>> createTrainingSet(Collection<FeatureTerm> examples, Collection<FeatureTerm> different_solutions, Path s_p, int m_size_min, int overlapp, int hyper, int syno, int homo){
		System.out.println("   > Creating two new learning sets from our agents...");
		// Create the ouptut
		List<List<FeatureTerm>> output = new ArrayList<>();
		// Create a map for classification of examples according to their solutions
		Map<FeatureTerm,List<FeatureTerm>> classified_training_set = new HashMap<>();
		// Create training set for each agent
		for(int i=0; i<2; i++) {
			output.add(new ArrayList<>());
		}
		// Create the keys for the classified map
		for(FeatureTerm solution : different_solutions) {
			classified_training_set.put(solution, new ArrayList<>());
		}
		// Classify the training set
		for(FeatureTerm example : examples) {
				classified_training_set.get(example.readPath(s_p)).add(example);
		}
		System.out.println("     > The overall set of examples is the following:");
		for(FeatureTerm solution : classified_training_set.keySet()) {
			System.out.println("       > The concept "+solution.toStringNOOS()+" has "+classified_training_set.get(solution).size()+" examples");
		}
		// Resample the data set if the context size has been forced
		if(TrainingSetUtils.NB_EX == 0)
			TrainingSetUtils.NB_EX = examples.size();
		else {
			Map<FeatureTerm,List<FeatureTerm>> resampled = new HashMap<>();
			for(FeatureTerm solution : different_solutions) {
				resampled.put(solution, new ArrayList<>());
			}
			boolean all_empty = false;
			int count = 0;
			while(!all_empty && count < TrainingSetUtils.NB_EX) {
				all_empty = true;
				for(FeatureTerm solution : different_solutions) {
					if(!classified_training_set.get(solution).isEmpty()) {
						resampled.get(solution).add(classified_training_set.get(solution).remove(0));
						all_empty = false;
						count ++;
					}
				}
			}
			System.out.println("     > We resampled the context");
			classified_training_set = resampled;
		}
		ExpFileManager.addBlock("examples",TrainingSetUtils.NB_EX);
		// Get the list of entries
		List<Entry<FeatureTerm,List<FeatureTerm>>> list = new ArrayList<>();
		System.out.println(classified_training_set.entrySet());
		for(Entry<FeatureTerm,List<FeatureTerm>> entry : classified_training_set.entrySet()) {
			System.out.println(entry.getValue().size());
			System.out.println(m_size_min);
			if(entry.getValue().size() >= m_size_min * 2)
				list.add(entry);
		}
		// Shuffle the two lists
		Collections.shuffle(list);
		System.out.println("     > We have "+list.size()+" suitable concepts for the semantic errors");
		ExpFileManager.addBlock("i_concepts",list.size());
		// Get desired number of issues
		int o_counter = overlapp;
		int Hh_counter = hyper;
		int s_counter = syno;
		int h_counter = homo;
		// Create overlapp
		while(list.size() > 2 && o_counter > 0) {
			// Get the concepts
			List<Entry<FeatureTerm,List<FeatureTerm>>> entries = list.subList(0, 3);
			List<FeatureTerm> a1_labels = new ArrayList<>();
			List<FeatureTerm> a2_labels = new ArrayList<>();
			a1_labels.add(entries.get(0).getKey());
			a1_labels.add(entries.get(1).getKey());
			a1_labels.add(entries.get(1).getKey());
			a2_labels.add(entries.get(1).getKey());
			a2_labels.add(entries.get(1).getKey());
			a2_labels.add(entries.get(2).getKey());
			Pair<List<FeatureTerm>,List<FeatureTerm>> result = deal_concepts(entries,a1_labels,a2_labels);
			output.get(0).addAll(result.getM_a());
			output.get(1).addAll(result.getM_b());
			list.removeAll(entries);
			o_counter --;
		}
		// Create hypo/hypernymy
		while(list.size() > 1 && Hh_counter > 0) {
			// Get the concepts
			List<Entry<FeatureTerm,List<FeatureTerm>>> entries = list.subList(0, 2);
			List<FeatureTerm> a1_labels = new ArrayList<>();
			List<FeatureTerm> a2_labels = new ArrayList<>();
			a1_labels.add(entries.get(0).getKey());
			a1_labels.add(entries.get(1).getKey());
			a2_labels.add(entries.get(0).getKey());
			a2_labels.add(entries.get(0).getKey());
			Pair<List<FeatureTerm>,List<FeatureTerm>> result = deal_concepts(entries,a1_labels,a2_labels);
			output.get(0).addAll(result.getM_a());
			output.get(1).addAll(result.getM_b());
			list.removeAll(entries);
			Hh_counter --;
		}
		// Create synonymy
		while(list.size() > 0 && s_counter > 0) {
			// Get the concepts
			List<Entry<FeatureTerm,List<FeatureTerm>>> entries = list.subList(0, 1);
			List<FeatureTerm> a1_labels = new ArrayList<>();
			List<FeatureTerm> a2_labels = new ArrayList<>();
			try {
				a1_labels.add(new SymbolFeatureTerm(new Symbol("synonym."+s_counter+"a"), LPkg.ontology()));
				a2_labels.add(new SymbolFeatureTerm(new Symbol("synonym."+s_counter+"b"), LPkg.ontology()));
				different_solutions.addAll(a1_labels);
				different_solutions.addAll(a2_labels);
			} catch (FeatureTermException e) {
				e.printStackTrace();
			}
			Pair<List<FeatureTerm>,List<FeatureTerm>> result = deal_concepts(entries,a1_labels,a2_labels);
			output.get(0).addAll(result.getM_a());
			output.get(1).addAll(result.getM_b());
			list.removeAll(entries);
			s_counter --;
		}
		// Create homonymy
		while(list.size() > 1 && h_counter > 0) {
			// Get the concepts
			List<Entry<FeatureTerm,List<FeatureTerm>>> entries = list.subList(0, 2);
			List<FeatureTerm> a1_labels = new ArrayList<>();
			List<FeatureTerm> a2_labels = new ArrayList<>();
			try {
				FeatureTerm ns = new SymbolFeatureTerm(new Symbol("homonym."+h_counter), LPkg.ontology());
				different_solutions.add(ns);
				a1_labels.add(entries.get(0).getKey());
				a1_labels.add(ns);
				a2_labels.add(ns);
				a2_labels.add(entries.get(1).getKey());
			} catch (FeatureTermException e) {
				e.printStackTrace();
			}
			Pair<List<FeatureTerm>,List<FeatureTerm>> result = deal_concepts(entries,a1_labels,a2_labels);
			output.get(0).addAll(result.getM_a());
			output.get(1).addAll(result.getM_b());
			list.removeAll(entries);
			h_counter --;
		}
		// Distribute the remaining examples
		while (list.size() > 0 && LIMIT == 0) {
			// Get the concepts
			List<Entry<FeatureTerm, List<FeatureTerm>>> entries = list.subList(0, 1);
			List<FeatureTerm> a1_labels = new ArrayList<>();
			List<FeatureTerm> a2_labels = new ArrayList<>();
			a1_labels.add(entries.get(0).getKey());
			a2_labels.add(entries.get(0).getKey());
			Pair<List<FeatureTerm>, List<FeatureTerm>> result = deal_concepts(entries, a1_labels, a2_labels);
			output.get(0).addAll(result.getM_a());
			output.get(1).addAll(result.getM_b());
			list.removeAll(entries);
		}
		System.out.println("   > Displaying the output structure:");
		for(List<FeatureTerm> l : output) {
			System.out.println("     > For the list #"+output.indexOf(l)+" in output:");
			for(FeatureTerm solution : different_solutions) {
				int count = 0;
				for(FeatureTerm f : l) {
						if(f.readPath(s_p).equals(solution))
							count ++;
				}
				System.out.println("       > For the solution "+solution.toStringNOOS()+" we have "+count+" examples");
			}
		}
		LPkg.set_different_solutions(different_solutions);
		ExpFileManager.addBlock("concerned",output.get(0).size() + output.get(1).size());
		ExpFileManager.addBlock("nb_overlap",overlapp - o_counter);
		ExpFileManager.addBlock("nb_hyponym",(hyper - Hh_counter) + 2 * (overlapp - o_counter));
		ExpFileManager.addBlock("nb_synonym",(syno - s_counter) + 2 * (homo - h_counter));
		ExpFileManager.addBlock("nb_homonym",homo - h_counter);
		// Reinitialize the number of examples
		TrainingSetUtils.NB_EX = 0;
		return output;
	}
	
	public static Pair<List<FeatureTerm>,List<FeatureTerm>> deal_concepts(List<Entry<FeatureTerm,List<FeatureTerm>>> entries, List<FeatureTerm> a1_labels, List<FeatureTerm> a2_labels){
		if(entries.size() != a1_labels.size() || entries.size() != a2_labels.size())
			System.out.println("       > Problem : invalid size for the inputs of dealing concept function");
		// Compute the percentage of redundancy
		int redundancy = TrainingSetUtils.REDUNDANCY;
		if(redundancy > 100) {
			System.out.println("       > Redundancy higher than one hundred, put at one hundred");
			redundancy = 100;
		}
		List<FeatureTerm> a1_out = new ArrayList<>();
		List<FeatureTerm> a2_out = new ArrayList<>();
		// Split concerned examples
		Map<FeatureTerm, List<FeatureTerm>> a1_examples = new HashMap<>();
		Map<FeatureTerm, List<FeatureTerm>> a2_examples = new HashMap<>();
		for(Entry<FeatureTerm, List<FeatureTerm>> l : entries) {
			// Number of redundant and proper examples
			int r = ((l.getValue().size()/2) * redundancy) / 100;
			int p = (l.getValue().size()/2) - r;
			List<FeatureTerm> toDeal = l.getValue();
			Collections.shuffle(toDeal);
			List<FeatureTerm> both_list = new ArrayList<>(toDeal.subList(0, r));
			toDeal.removeAll(both_list);
			List<FeatureTerm> a1_list = new ArrayList<>(toDeal.subList(0, p));
			toDeal.removeAll(a1_list);
			List<FeatureTerm> a2_list = new ArrayList<>(toDeal.subList(0, p));
			a1_list.addAll(both_list);
			a2_list.addAll(both_list);
			a1_examples.put(l.getKey(), a1_list);
			a2_examples.put(l.getKey(), a2_list);
		}
		for(int i=0;i<entries.size();i++) {
			// Deal examples
			List<FeatureTerm> a1_deal = a1_examples.get(entries.get(i).getKey());
			for(FeatureTerm ft : a1_deal) {
				FeatureTerm nft = null;
				try {
					nft = ft.clone(LPkg.dm(), LPkg.ontology());
					nft.substitute(entries.get(i).getKey(), a1_labels.get(i));
				} catch (FeatureTermException e) {
					System.out.println("problem");
					e.printStackTrace();
				}
				a1_out.add(nft);
			}
			List<FeatureTerm> a2_deal = a2_examples.get(entries.get(i).getKey());
			for(FeatureTerm ft : a2_deal) {
				FeatureTerm nft = null;
				try {
					nft = ft.clone(LPkg.dm(), LPkg.ontology());
					nft.substitute(entries.get(i).getKey(), a2_labels.get(i));
				} catch (FeatureTermException e) {
					System.out.println("problem");
					e.printStackTrace();
				}
				a2_out.add(nft);
			}
		}
		return new Pair<List<FeatureTerm>, List<FeatureTerm>>(a1_out, a2_out);
	}
	
	public static List<List<FeatureTerm>> splitTrainingSet(Collection<FeatureTerm> examples, Collection<FeatureTerm> different_solutions, Path s_p, int nb_agent){
		// Create the ouptut
		List<List<FeatureTerm>> output = new ArrayList<>();
		// Create a map for classification of examples according to their solutions
		Map<FeatureTerm,List<FeatureTerm>> classified_training_set = new HashMap<>();
		// Create training set for each agent
		for(int i=0; i<nb_agent; i++) {
			output.add(new ArrayList<>());
		}
		// Create the keys for the classified map
		for(FeatureTerm solution : different_solutions) {
			classified_training_set.put(solution, new ArrayList<>());
		}
		// Classify the training set
		for(FeatureTerm example : examples) {
			classified_training_set.get(example.readPath(s_p)).add(example);
		}
		// Distribute the examples
		for(List<FeatureTerm> classified_examples : classified_training_set.values()) {
			List<FeatureTerm> examples_copy = new ArrayList<>(classified_examples);
			// Shuffle list
			Collections.shuffle(examples_copy);
			int count = 0;
			while(!examples_copy.isEmpty()) {
				output.get(count % nb_agent).add(examples_copy.remove(0));
				count ++;
			}
		}
		return output;
	}
	
	/**
	 * Split training set.
	 * 
	 * @param examples
	 *            the examples
	 * @param n
	 *            the n
	 * @param dp
	 *            the dp
	 * @param sp
	 *            the sp
	 * @param dm
	 *            the dm
	 * @param bias
	 *            the bias
	 * @param redundancy
	 *            the redundancy
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws Exception
	 *             the exception
	 */
	public static List<List<FeatureTerm>> splitTrainingSet(Collection<FeatureTerm> examples, int n, Path dp, Path sp, FTKBase dm, double bias, double redundancy)
			throws FeatureTermException, Exception {
		double matrix[] = null;
		double cbias = 0;
		List<FeatureTerm> differentSolutions = Hypothesis.differentSolutions(examples, sp);
		int ns = differentSolutions.size();
		Random r = new Random();

		// Generate the bias matrix:
		// generate an initial matrix as close as possible to the desired bias:
		for (int i = 0; i < 100; i++) {
			double m[] = new double[n * ns];
			double mbias = 0;
			for (int j = 0; j < n * ns; j++)
				m[j] = r.nextFloat();
			for (int j = 0; j < n; j++) {
				double t = 0;
				for (int k = 0; k < ns; k++)
					t += m[k * n + j];
				for (int k = 0; k < ns; k++)
					m[k * n + j] /= t;
			}
			for (int j = 0; j < n; j++) {
				double t = 0;
				for (int k = 0; k < ns; k++)
					t = (m[k * n + j] - (1.0 / ns)) * (m[k * n + j] - (1.0 / ns));
				mbias += Math.sqrt(t);
			}
			mbias /= n;

			if (matrix == null || Math.abs(bias - mbias) < Math.abs(bias - cbias)) {
				matrix = m;
				cbias = mbias;
			}
		}

		if (DEBUG >= 1)
			System.out.println("Desired bias: " + bias);
		if (DEBUG >= 1)
			System.out.println("Initial bias: " + cbias);

		// Adjust matrix to get closer to desired bias:
		for (int i = 0; i < 1000; i++) {
			boolean stop = true;
			double modifiers[] = { 0.5, 0.75, 0.8, 0.9, 1.1, 1.25, 1.5, 2.0 };

			for (double modifier : modifiers) {
				double m[] = new double[n * ns];
				for (int j = 0; j < n * ns; j++)
					m[j] = matrix[j];
				double mbias = 0;
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < ns; k++) {
						m[k * n + j] = ((m[k * n + j] - (1.0 / n)) * modifier) + (1.0 / n);
						if (m[k * n + j] < 0)
							m[k * n + j] = 0;
						if (m[k * n + j] > 1)
							m[k * n + j] = 1;
					}
					double t = 0;
					for (int k = 0; k < ns; k++)
						t += m[k * n + j];
					if (t > 0)
						for (int k = 0; k < ns; k++)
							m[k * n + j] /= t;
				}
				for (int j = 0; j < n; j++) {
					double t = 0;
					for (int k = 0; k < ns; k++)
						t = (m[k * n + j] - (1.0 / ns)) * (m[k * n + j] - (1.0 / ns));
					mbias += Math.sqrt(t);
				}
				mbias /= n;
				if (Math.abs(bias - mbias) < Math.abs(bias - cbias)) {
					// System.out.println(modifier + " -> " + mbias);
					for (int j = 0; j < n * ns; j++)
						matrix[j] = m[j];
					cbias = mbias;
					stop = false;
					/*
					 * for(FeatureTerm s:differentSolutions) { int j = differentSolutions.indexOf(s); List<Double> d =
					 * new LinkedList<Double>(); for(int k = 0;k<n;k++) d.add(matrix[k*ns+j]);
					 * System.out.println("D for " + s.toStringNOOS(dm) + " -> " + d); }
					 */
				}
			}
			if (stop)
				break;
		}

		if (DEBUG >= 1)
			System.out.println("Adjusted bias: " + cbias);

		// Compute how many cases to distribtue according to redundancy:
		int ncases = (int) ((redundancy * (examples.size()) * (n - 1)) + examples.size());
		if (DEBUG >= 1)
			System.out.println("Redundancy " + redundancy + " -> " + ncases);

		// Sample:
		List<List<FeatureTerm>> training_sets = new LinkedList<List<FeatureTerm>>();
		List<Hypothesis> hypotheses = new LinkedList<Hypothesis>();
		List<FeatureTerm> casesToDistribute = new LinkedList<FeatureTerm>();
		HashMap<FeatureTerm, List<Double>> distributions = new HashMap<FeatureTerm, List<Double>>();

		for (FeatureTerm s : differentSolutions) {
			int i = differentSolutions.indexOf(s);
			List<Double> d = new LinkedList<Double>();
			for (int k = 0; k < n; k++)
				d.add(matrix[k * ns + i]);
			distributions.put(s, d);
			if (DEBUG >= 1)
				System.out.println("Distribution for " + s.toStringNOOS(dm) + " -> " + d);
		}

		for (int i = 0; i < n; i++)
			training_sets.add(new LinkedList<FeatureTerm>());
		for (int i = 0; i < ncases; i++) {
			if (casesToDistribute.isEmpty())
				casesToDistribute.addAll(examples);

			FeatureTerm e = casesToDistribute.get(r.nextInt(casesToDistribute.size()));
			FeatureTerm s = e.readPath(sp);

			boolean found = false;
			// First try to assign it to an agent according to the bias:
			for (int j = 0; j < 10; j++) {
				int a = Sampler.weighted(distributions.get(s));
				if (!training_sets.get(a).contains(e)) {
					training_sets.get(a).add(e);
					found = true;
					break;
				}
			}
			// If not possible give it to a random agent which does not have the case:
			if (!found) {
				while (true) {
					int a = Sampler.random(distributions.get(s));
					if (training_sets.get(a).contains(e)) {
						training_sets.get(a).add(e);
						break;
					}
				}
			}
			casesToDistribute.remove(e);
		}
		return training_sets;
	}
	
	public static List<List<FeatureTerm>> splitTrainingSet_argumentation(Collection<FeatureTerm> examples, List<FeatureTerm> different_solutions, Path dp, Path sp, FTKBase dm, int nb_1, int nb_2) throws FeatureTermException{
		
		if(examples.isEmpty()){
			System.out.println("! Empty set ");
			return null;
		}
		
		ArrayList<List<FeatureTerm>> output = new ArrayList<List<FeatureTerm>>();
		
		List<Integer> merged_ext_1 = new ArrayList<Integer>();
		List<Integer> merged_ext_2 = new ArrayList<Integer>();
		List<Integer> to_increment = new ArrayList<Integer>();
		
		Random r = new Random();
		FeatureTerm s1 = null;
		FeatureTerm s2 = null;
		
		if(nb_1 > different_solutions.size() || nb_2 > different_solutions.size()){
			System.out.println("! not enough categories regarding the merges required ");
			return null;
		}
		
		for(int i = 0; i < 3; i++){
			output.add(new ArrayList<FeatureTerm>());
		}
		
		for(int i = 0; i < nb_1; i++){
			Integer n = r.nextInt(different_solutions.size());
			while(merged_ext_1.contains(n)){
				n = r.nextInt(different_solutions.size());
			}
			merged_ext_1.add(n);
		}
		
		for(int i = 0; i < nb_2; i++){
			Integer n = r.nextInt(different_solutions.size());
			while(merged_ext_1.contains(n) || merged_ext_2.contains(n)){
				n = r.nextInt(different_solutions.size());
			}
			merged_ext_2.add(n);
		}
		
		
		for(int i = 0; i < different_solutions.size(); i++){
			to_increment.add(0);
		}
		
		s1 = different_solutions.get(merged_ext_1.get(0));
		s2 = different_solutions.get(merged_ext_2.get(0));
		
		System.out.println("First dataset :");
		for(Integer i : merged_ext_1){
			System.out.println(different_solutions.get(i).toStringNOOS(dm));
		}
		
		System.out.println("generalized in "+ s1.toStringNOOS(dm));
		
		System.out.println("Second dataset :");
		for(Integer i : merged_ext_2){
			System.out.println(different_solutions.get(i).toStringNOOS(dm));
		}
		
		System.out.println("generalized in "+ s2.toStringNOOS(dm));
		
		// For all FeatureTerm
		for(FeatureTerm f : examples){
			
			// Check the solution & where the FeatureTerm should be added
			FeatureTerm current_solution = f.readPath(sp);
			int s_number = different_solutions.indexOf(current_solution);
			int c_to_add = to_increment.get(s_number) % 3;
			
			// If the solution should be added in 1
			if(c_to_add == 0){
				
				// If the solution is a part of merged 1 & If the solution is different than the first solution of merged 1, change the solution
				if(merged_ext_1.contains(s_number) && !current_solution.equals(s1)){
					f.substitute(current_solution, s1);
				}
			
				// put it in 1
				output.get(0).add(f);
		
				// add 1 to the 1st space of to_increment
				to_increment.set(s_number, c_to_add + 1);
			}
			
			// If the solution should be added in 2
			if(c_to_add == 1){
				
				// If the solution is a part of merged 2 && If the solution is different than the first solution of merged 1, change the solution
				if(merged_ext_2.contains(s_number) && !current_solution.equals(s2)){
					f.substitute(current_solution, s2);
				}
		
				// put it in 2
				output.get(1).add(f);
				
				// add 1 to the 2nd space of to_increment
				to_increment.set(s_number, c_to_add + 1);
			}
			
			// Else
			if(c_to_add == 2){
				
				// Put it in 3
				output.get(2).add(f);
				
				// add 1 to the 3rd space of to_increment
				to_increment.set(s_number, c_to_add + 1);
			}
		}
		
		for(List<FeatureTerm> l : output){
			//System.out.println(l.size());
		}
		
		return output;
	}

	/** The Constant ARTIFICIAL_DATASET. */
	public static final int ARTIFICIAL_DATASET = 0;

	/** The Constant ZOOLOGY_DATASET. */
	public static final int ZOOLOGY_DATASET = 1;

	/** The Constant SOYBEAN_DATASET. */
	public static final int SOYBEAN_DATASET = 2;

	/** The Constant DEMOSPONGIAE_503_DATASET. */
	public static final int DEMOSPONGIAE_503_DATASET = 3;

	/** The Constant DEMOSPONGIAE_280_DATASET. */
	public static final int DEMOSPONGIAE_280_DATASET = 4;

	/** The Constant DEMOSPONGIAE_120_DATASET. */
	public static final int DEMOSPONGIAE_120_DATASET = 5;

	/** The Constant TRAINS_DATASET. */
	public static final int TRAINS_DATASET = 6;

	/** The Constant TRAINS_82_DATASET. */
	public static final int TRAINS_82_DATASET = 61;

	/** The Constant TRAINS_900_DATASET. */
	public static final int TRAINS_900_DATASET = 62;

	/** The Constant TRAINS_100_DATASET. */
	public static final int TRAINS_100_DATASET = 63;

	/** The Constant TRAINS_1000_DATASET. */
	public static final int TRAINS_1000_DATASET = 64;

	/** The Constant TRAINS_10000_DATASET. */
	public static final int TRAINS_10000_DATASET = 65;

	/** The Constant TRAINS_100000_DATASET. */
	public static final int TRAINS_100000_DATASET = 66;

	/** The Constant UNCLE_DATASET. */
	public static final int UNCLE_DATASET = 7;

	/** The Constant UNCLE_DATASET_SETS. */
	public static final int UNCLE_DATASET_SETS = 8;

	/** The Constant UNCLE_DATASET_BOTH. */
	public static final int UNCLE_DATASET_BOTH = 9;

	/** The Constant CARS_DATASET. */
	public static final int CARS_DATASET = 10;

	/** The Constant TOXICOLOGY_DATASET_MRATS. */
	public static final int TOXICOLOGY_DATASET_MRATS = 11;

	/** The Constant TOXICOLOGY_DATASET_FRATS. */
	public static final int TOXICOLOGY_DATASET_FRATS = 12;

	/** The Constant TOXICOLOGY_DATASET_MMICE. */
	public static final int TOXICOLOGY_DATASET_MMICE = 13;

	/** The Constant TOXICOLOGY_DATASET_FMICE. */
	public static final int TOXICOLOGY_DATASET_FMICE = 14;

	/** The Constant TOXICOLOGY_OLD_DATASET_MRATS. */
	public static final int TOXICOLOGY_OLD_DATASET_MRATS = 15;

	/** The Constant TOXICOLOGY_OLD_DATASET_FRATS. */
	public static final int TOXICOLOGY_OLD_DATASET_FRATS = 16;

	/** The Constant TOXICOLOGY_OLD_DATASET_MMICE. */
	public static final int TOXICOLOGY_OLD_DATASET_MMICE = 17;

	/** The Constant TOXICOLOGY_OLD_DATASET_FMICE. */
	public static final int TOXICOLOGY_OLD_DATASET_FMICE = 18;

	/** The Constant KR_VS_KP_DATASET. */
	public static final int KR_VS_KP_DATASET = 19;

	/** The Constant FINANCIAL. */
	public static final int FINANCIAL = 20;

	/** The Constant FINANCIAL_NO_TRANSACTIONS. */
	public static final int FINANCIAL_NO_TRANSACTIONS = 21;

	/** The Constant MUTAGENESIS. */
	public static final int MUTAGENESIS = 22;

	/** The Constant MUTAGENESIS_EASY. */
	public static final int MUTAGENESIS_EASY = 23;

	/** The Constant MUTAGENESIS_DISCRETIZED. */
	public static final int MUTAGENESIS_DISCRETIZED = 24;

	/** The Constant MUTAGENESIS_EASY_DISCRETIZED. */
	public static final int MUTAGENESIS_EASY_DISCRETIZED = 25;

	/** The Constant MUTAGENESIS_NOL_DISCRETIZED. */
	public static final int MUTAGENESIS_NOL_DISCRETIZED = 26;

	/** The Constant MUTAGENESIS_EASY_NOL_DISCRETIZED. */
	public static final int MUTAGENESIS_EASY_NOL_DISCRETIZED = 27;

	/** The Constant RIU_STORIES. */
	public static final int RIU_STORIES = 28;
	
	/** The Constant SEAT for agent 1. */
	public static final int SEAT_1 = 29;
	
	/** The Constant SEAT for agent 2. */
	public static final int SEAT_2 = 30;
	
	/** The Constant SEAT for agent 3. */
	public static final int SEAT_TEST = 31;
	
	/** The Constant SEAT for agent 3. */
	public static final int SEAT_ALL = 34;
	
	/** The Constant SEAT for 900 examples */
	public static final int SEAT_900 = 36;
	
	/** The Constant ZOOLOGY_DATASET_LB */
	public static final int ZOOLOGY_DATASET_LB = 33;
	
	public static final int MY_DATASET = 35;

	/**
	 * Load training set.
	 * 
	 * @param DATASET
	 *            the dATASET
	 * @param o
	 *            the o
	 * @param dm
	 *            the dm
	 * @param case_base
	 *            the case_base
	 * @return the training set properties
	 * @throws FeatureTermException
	 *             the feature term exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static TrainingSetProperties loadTrainingSet(int DATASET, Ontology o, FTKBase dm, FTKBase case_base) throws FeatureTermException, IOException {
		TrainingSetProperties ts = new TrainingSetProperties();
		ts.description_path = new Path();
		ts.solution_path = new Path();
		ts.cases = new LinkedList<FeatureTerm>();

		ts.description_path.features.add(new Symbol("description"));
		ts.solution_path.features.add(new Symbol("solution"));

		switch (DATASET) {
		case ARTIFICIAL_DATASET:
			dm.importNOOS("FTL/Resources/DATA/artificial-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/artificial-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/artificial-512.noos", o);

			ts.name = "artificial";
			ts.problem_sort = o.getSort("artificial-data-problem");
			break;
		case ZOOLOGY_DATASET:
			dm.importNOOS("FTL/Resources/DATA/zoology-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/zoology-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/zoology-cases-102_OLD.noos", o);

			ts.name = "zoology";
			ts.problem_sort = o.getSort("zoo-problem");
			break;
			
		case SOYBEAN_DATASET:
			dm.importNOOS("FTL/Resources/DATA/soybean-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/soybean-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/soybean-cases-307.noos", o);

			ts.name = "soybean";
			ts.problem_sort = o.getSort("soybean-problem");
			break;
		case DEMOSPONGIAE_503_DATASET:
			dm.importNOOS("FTL/Resources/DATA/sponge-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/sponge-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/sponge-cases-503.noos", o);

			ts.solution_path.features.add(new Symbol("order"));

			ts.name = "demospongiae";
			ts.problem_sort = o.getSort("sponge-problem");
			break;
		case DEMOSPONGIAE_280_DATASET:
			dm.importNOOS("FTL/Resources/DATA/sponge-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/sponge-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/sponge-cases-280.noos", o);

			ts.solution_path.features.add(new Symbol("order"));

			ts.name = "demospongiae";
			ts.problem_sort = o.getSort("sponge-problem");
			break;
		case DEMOSPONGIAE_120_DATASET:
			dm.importNOOS("FTL/Resources/DATA/sponge-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/sponge-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/sponge-cases-120.noos", o);

			ts.solution_path.features.add(new Symbol("order"));

			ts.name = "demospongiae";
			ts.problem_sort = o.getSort("sponge-problem");
			break;
		case TRAINS_DATASET:
			dm.importNOOS("FTL/Resources/DATA/trains-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/trains-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/trains-cases-10.noos", o);

			ts.name = "trains";
			ts.problem_sort = o.getSort("trains-problem");
			break;
		case TRAINS_82_DATASET:
			dm.importNOOS("FTL/Resources/DATA/trains-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/trains-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/trains-cases-82.noos", o);

			ts.name = "trains";
			ts.problem_sort = o.getSort("trains-problem");
			break;
		case TRAINS_900_DATASET:
			dm.importNOOS("FTL/Resources/DATA/trains-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/trains-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/trains-cases-900.noos", o);

			ts.name = "trains";
			ts.problem_sort = o.getSort("trains-problem");
			break;
		case TRAINS_100_DATASET:
			dm.importNOOS("FTL/Resources/DATA/trains-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/trains-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/trains-cases-100.noos", o);

			ts.name = "trains";
			ts.problem_sort = o.getSort("trains-problem");
			break;
		case TRAINS_1000_DATASET:
			dm.importNOOS("FTL/Resources/DATA/trains-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/trains-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/trains-cases-1000.noos", o);

			ts.name = "trains";
			ts.problem_sort = o.getSort("trains-problem");
			break;
		case TRAINS_10000_DATASET:
			dm.importNOOS("FTL/Resources/DATA/trains-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/trains-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/trains-cases-10000.noos", o);

			ts.name = "trains";
			ts.problem_sort = o.getSort("trains-problem");
			break;
		case TRAINS_100000_DATASET:
			dm.importNOOS("FTL/Resources/DATA/trains-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/trains-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/trains-cases-100000.noos", o);

			ts.name = "trains";
			ts.problem_sort = o.getSort("trains-problem");
			break;
		case UNCLE_DATASET:
			dm.importNOOS("FTL/Resources/DATA/family-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/family-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/family-cases-12.noos", o);

			ts.name = "uncle";
			ts.problem_sort = o.getSort("uncle-problem");
			break;
		case UNCLE_DATASET_SETS:
			dm.importNOOS("FTL/Resources/DATA/family-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/family-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/family-cases-12-sets.noos", o);

			ts.name = "uncle";
			ts.problem_sort = o.getSort("uncle-problem");
			break;
		case UNCLE_DATASET_BOTH:
			dm.importNOOS("FTL/Resources/DATA/family-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/family-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/family-cases-12.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/family-cases-12-sets.noos", o);

			ts.name = "uncle";
			ts.problem_sort = o.getSort("uncle-problem");
			break;
		case CARS_DATASET:
			dm.importNOOS("FTL/Resources/DATA/car-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/car-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/car-1728.noos", o);

			ts.name = "cars";
			ts.problem_sort = o.getSort("car-problem");
			break;

		case TOXICOLOGY_DATASET_MRATS:
		case TOXICOLOGY_DATASET_FRATS:
		case TOXICOLOGY_DATASET_MMICE:
		case TOXICOLOGY_DATASET_FMICE:
			dm.importNOOS("FTL/Resources/DATA/toxic-eva-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/toxic-eva-dm.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/toxic-eva-filtered-cases-276.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/toxic-eva-cases-371.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/toxic-eva-fixed-cases-371.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/toxic-santi-cases-353.noos", o);

			switch (DATASET) {
			case TOXICOLOGY_DATASET_MRATS:
				ts.solution_path.features.add(new Symbol("m-rats"));
				break;
			case TOXICOLOGY_DATASET_FRATS:
				ts.solution_path.features.add(new Symbol("f-rats"));
				break;
			case TOXICOLOGY_DATASET_MMICE:
				ts.solution_path.features.add(new Symbol("m-mice"));
				break;
			case TOXICOLOGY_DATASET_FMICE:
				ts.solution_path.features.add(new Symbol("f-mice"));
				break;
			}

			ts.name = "toxicology";
			ts.problem_sort = o.getSort("toxic-problem");

			{
				List<FeatureTerm> cs = new LinkedList<FeatureTerm>();
				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				cs.addAll(case_base.searchFT(ts.problem_sort));

				for (FeatureTerm c : cs) {
					FeatureTerm s = c.readPath(ts.solution_path);
					String ss = s.toStringNOOS(dm);
					if (!ss.equals("positive") && !ss.equals("negative")) {
						// remove example, inqdequate!
						case_base.deleteFT(c);
					}
				}

			}
			break;

		case TOXICOLOGY_OLD_DATASET_MRATS:
		case TOXICOLOGY_OLD_DATASET_FRATS:
		case TOXICOLOGY_OLD_DATASET_MMICE:
		case TOXICOLOGY_OLD_DATASET_FMICE:
			dm.importNOOS("FTL/Resources/DATA/toxic-eva-old-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/toxic-eva-old-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/toxic-eva-old-cases.noos", o);

			switch (DATASET) {
			case TOXICOLOGY_OLD_DATASET_MRATS:
				ts.solution_path.features.add(new Symbol("m-rats"));
				break;
			case TOXICOLOGY_OLD_DATASET_FRATS:
				ts.solution_path.features.add(new Symbol("f-rats"));
				break;
			case TOXICOLOGY_OLD_DATASET_MMICE:
				ts.solution_path.features.add(new Symbol("m-mice"));
				break;
			case TOXICOLOGY_OLD_DATASET_FMICE:
				ts.solution_path.features.add(new Symbol("f-mice"));
				break;
			}

			ts.name = "toxicology-old";
			ts.problem_sort = o.getSort("toxic-problem");

			{
				List<FeatureTerm> cs = new LinkedList<FeatureTerm>();
				List<FeatureTerm> toDelete = new LinkedList<FeatureTerm>();
				cs.addAll(case_base.searchFT(ts.problem_sort));

				for (FeatureTerm c : cs) {
					FeatureTerm s = c.readPath(ts.solution_path);
					if (s != null) {
						String ss = s.toStringNOOS(dm);
						if (!ss.equals("positive") && !ss.equals("negative")) {
							// remove example, inqdequate!
							case_base.deleteFT(c);
						}
					}
				}

			}
			break;

		case KR_VS_KP_DATASET:
			dm.importNOOS("FTL/Resources/DATA/kr-vs-kp-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/kr-vs-kp-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/kr-vs-kp-3196.noos", o);

			ts.name = "kr-vs-kp";
			ts.problem_sort = o.getSort("kr-vs-kp-problem");
			break;
		case FINANCIAL_NO_TRANSACTIONS:
			dm.importNOOS("FTL/Resources/DATA/financial-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/financial-dm.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/financial-cases-682-no-transactions.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/financial-cases-10-no-transactions.noos", o);

			ts.name = "financial-no-t";
			ts.problem_sort = o.getSort("loan-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("loan"));
			ts.solution_path.features.add(new Symbol("status"));
			break;
		case FINANCIAL:
			dm.importNOOS("FTL/Resources/DATA/financial-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/financial-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/financial-cases-10.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/financial-cases-682.noos", o);

			ts.name = "financial-no-t";
			ts.problem_sort = o.getSort("loan-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("loan"));
			ts.solution_path.features.add(new Symbol("status"));
			break;
		case MUTAGENESIS:
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-dm.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/mutagenesis-b4-230-cases.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/mutagenesis-b4-25-cases.noos", o);

			ts.name = "mutagenesis-b4";
			ts.problem_sort = o.getSort("mutagenesis-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("problem"));
			ts.solution_path.features.add(new Symbol("solution"));
			break;
		case MUTAGENESIS_EASY:
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/mutagenesis-b4-188-cases.noos", o);

			ts.name = "mutagenesis-b4";
			ts.problem_sort = o.getSort("mutagenesis-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("problem"));
			ts.solution_path.features.add(new Symbol("solution"));
			break;
		case MUTAGENESIS_DISCRETIZED:
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-dm.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/mutagenesis-b4-230-cases.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/mutagenesis-b4-noH-230-cases.noos", o);
			// case_base.ImportNOOS("FTL/Resources/DATA/mutagenesis-b4-noH-25-cases.noos", o);

			ts.name = "mutagenesis-b4-discretized";
			ts.problem_sort = o.getSort("mutagenesis-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("problem"));
			ts.solution_path.features.add(new Symbol("solution"));

			// discretize:
			{
				Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
				Path fp = new Path();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("lumo"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

				fp.features.clear();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("logp"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
			}

			break;
		case MUTAGENESIS_EASY_DISCRETIZED:
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/mutagenesis-b4-188-cases.noos", o);

			ts.name = "mutagenesis-b4-discretized";
			ts.problem_sort = o.getSort("mutagenesis-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("problem"));
			ts.solution_path.features.add(new Symbol("solution"));

			// discretize:
			{
				Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
				Path fp = new Path();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("lumo"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

				fp.features.clear();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("logp"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
			}

			break;
		case MUTAGENESIS_EASY_NOL_DISCRETIZED:
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/mutagenesis-b4-noH-noL-188-cases.noos", o);

			ts.name = "mutagenesis-b4-nol-discretized";
			ts.problem_sort = o.getSort("mutagenesis-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("problem"));
			ts.solution_path.features.add(new Symbol("solution"));

			// discretize:
			{
				Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
				Path fp = new Path();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("lumo"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

				fp.features.clear();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("logp"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
			}
			break;
		case MUTAGENESIS_NOL_DISCRETIZED:
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/mutagenesis-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/mutagenesis-b4-noH-noL-230-cases.noos", o);

			ts.name = "mutagenesis-b4-nol-discretized";
			ts.problem_sort = o.getSort("mutagenesis-problem");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("problem"));
			ts.solution_path.features.add(new Symbol("solution"));

			// discretize:
			{
				Set<FeatureTerm> cases = case_base.searchFT(ts.problem_sort);
				Path fp = new Path();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("lumo"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);

				fp.features.clear();
				fp.features.add(new Symbol("problem"));
				fp.features.add(new Symbol("logp"));
				TrainingSetUtils.discretizeFeature(cases, fp, ts.solution_path, 2);
			}
			break;
		case RIU_STORIES:
			dm.importNOOS("FTL/Resources/DATA/story-ontology.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/story-cases-2.noos", o);

			ts.name = "riu-stories";
			ts.problem_sort = o.getSort("scene");

			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			break;
		case SEAT_1:
			dm.importNOOS("FTL/Resources/seat-ontology.noos", o);
			dm.importNOOS("FTL/Resources/seat-dm.noos",o);
			case_base.importNOOS("FTL/Resources/seat-cases-learn-1.noos", o);
			
			ts.name = "seat";
			ts.problem_sort = o.getSort("seat-case");
			
			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("description"));
			ts.solution_path.features.add(new Symbol("label"));
			break;
		case SEAT_2:
			dm.importNOOS("FTL/Resources/seat-ontology.noos", o);
			dm.importNOOS("FTL/Resources/seat-dm.noos",o);
			case_base.importNOOS("FTL/Resources/seat-cases-learn-2.noos", o);
			
			ts.name = "seat";
			ts.problem_sort = o.getSort("seat-case");
			
			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("description"));
			ts.solution_path.features.add(new Symbol("label"));
			break;			
		case SEAT_TEST:
			dm.importNOOS("FTL/Resources/seat-ontology.noos", o);
			dm.importNOOS("FTL/Resources/seat-dm.noos",o);
			case_base.importNOOS("FTL/Resources/seat-cases-test.noos", o);
			
			ts.name = "seat";
			ts.problem_sort = o.getSort("seat-case");
			
			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("description"));
			ts.solution_path.features.add(new Symbol("label"));
			break;
			
		case SEAT_ALL:
			dm.importNOOS("FTL/Resources/seat-ontology.noos", o);
			dm.importNOOS("FTL/Resources/seat-dm.noos",o);
			case_base.importNOOS("FTL/Resources/seat-cases-all.noos", o);
			
			ts.name = "seat";
			ts.problem_sort = o.getSort("seat-case");
			
			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("description"));
			ts.solution_path.features.add(new Symbol("label"));
			break;
			
		case SEAT_900:
			dm.importNOOS("FTL/Resources/seat-ontology.noos", o);
			dm.importNOOS("FTL/Resources/seat-dm.noos",o);
			case_base.importNOOS("FTL/Resources/seat-cases-900.noos", o);
			
			ts.name = "seat";
			ts.problem_sort = o.getSort("seat-case");
			
			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("description"));
			ts.solution_path.features.add(new Symbol("label"));
			break;
			
		case MY_DATASET:
			dm.importNOOS("FTL/Resources/DATA/mydataset-ontology.noos", o);
			dm.importNOOS("FTL/Resources/DATA/mydataset-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/mydatasetXcases.noos", o);

			ts.name = "myDataset";
			ts.problem_sort = o.getSort("dataset-problem");
			
			ts.description_path.features.clear();
			ts.solution_path.features.clear();
			ts.description_path.features.add(new Symbol("myDescription"));
			ts.solution_path.features.add(new Symbol("mySolution"));
			break;
			
		case ZOOLOGY_DATASET_LB:
			dm.importNOOS("FTL/Resources/DATA/zoology-ontology2.noos", o);
			dm.importNOOS("FTL/Resources/DATA/zoology-dm.noos", o);
			case_base.importNOOS("FTL/Resources/DATA/zoology-cases-101.noos", o);

			ts.name = "zoology";
			ts.problem_sort = o.getSort("zoo-problem");
			break;
			
		default:
			return null;
		}

		ts.cases.addAll(case_base.searchFT(ts.problem_sort));
		return ts;
	}

	/**
	 * Discretize feature.
	 * 
	 * @param cases
	 *            the cases
	 * @param featurePath
	 *            the feature path
	 * @param solutionPath
	 *            the solution path
	 * @param ncuts
	 *            the ncuts
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static void discretizeFeature(Collection<FeatureTerm> cases, Path featurePath, Path solutionPath, int ncuts) throws FeatureTermException {
		List<Float> cuts = findDiscretizationIntervals(cases, featurePath, solutionPath, ncuts);

		// change the values by the discretized ones:
		for (FeatureTerm c : cases) {
			FeatureTerm v = c.readPath(featurePath);
			float fv = 0;
			boolean integer = true;
			if (v != null) {
				if (v instanceof IntegerFeatureTerm) {
					fv = ((IntegerFeatureTerm) v).getValue().floatValue();
				} else {
					fv = ((FloatFeatureTerm) v).getValue();
					integer = false;
				}

				int newV = 0;
				for (Float cut : cuts) {
					if (cut < fv)
						newV++;
					else
						break;
				}
				if (integer) {
					((IntegerFeatureTerm) v).setValue(newV);
				} else {
					((FloatFeatureTerm) v).setValue((float) newV);
				}
			} else {
				System.out.println(c.getName() + " has no value in " + featurePath);
			}
		}
	}

	// this method will split the feature range in 2^uts intervals, and return the cut points:
	/**
	 * Find discretization intervals.
	 * 
	 * @param cases
	 *            the cases
	 * @param featurePath
	 *            the feature path
	 * @param solutionPath
	 *            the solution path
	 * @param cuts
	 *            the cuts
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static List<Float> findDiscretizationIntervals(Collection<FeatureTerm> cases, Path featurePath, Path solutionPath, int cuts)
			throws FeatureTermException {
		List<Pair<Float, Integer>> values = new LinkedList<Pair<Float, Integer>>();
		Vector<FeatureTerm> solutions = new Vector<FeatureTerm>();

		for (FeatureTerm c : cases) {
			FeatureTerm s = c.readPath(solutionPath);
			if (!solutions.contains(s))
				solutions.add(s);
		}

		// get all the values:
		for (FeatureTerm c : cases) {
			FeatureTerm v = c.readPath(featurePath);
			FeatureTerm s = c.readPath(solutionPath);
			Float fv = null;

			if (v != null) {
				if (v instanceof IntegerFeatureTerm) {
					fv = (((IntegerFeatureTerm) v).getValue()).floatValue();
				} else if (v instanceof FloatFeatureTerm) {
					fv = ((FloatFeatureTerm) v).getValue();
				} else {
					throw new FeatureTermException("The feature has a non numeric value!");
				}
				values.add(new Pair<Float, Integer>(fv, solutions.indexOf(s)));
			}
		}

		// sort them:
		{
			boolean change = false;
			int len = values.size();
			do {
				change = false;
				for (int i = 0; i < len - 1; i++) {
					if (values.get(i).mA > values.get(i + 1).mA) {
						Pair<Float, Integer> tmp = values.get(i);
						values.set(i, values.get(i + 1));
						values.set(i + 1, tmp);
						change = true;
					}
				}
			} while (change);
		}

		// for(Pair<Float,Integer> v:values) {
		// System.out.println(v.m_b + " - " + v.m_a);
		// }

		return discretizeFeatureInternal(values, solutions.size(), cuts);
	}

	/**
	 * Discretize feature internal.
	 * 
	 * @param values
	 *            the values
	 * @param nSolutions
	 *            the n solutions
	 * @param cuts
	 *            the cuts
	 * @return the list
	 */
	static List<Float> discretizeFeatureInternal(List<Pair<Float, Integer>> values, int nSolutions, int cuts) {
		if (cuts == 0) {
			return new LinkedList<Float>();
		} else {
			boolean first = true;
			float bestCut = 0, bestE = 0;

			// System.out.println("discretizing " + values.size() + " values");

			int gDistribution[] = new int[nSolutions];
			float gE = 0;
			for (int i = 0; i < values.size() - 1; i++) {
				float cut = (values.get(i).mA + values.get(i + 1).mA) / 2;

				int d1[] = new int[nSolutions];
				int d2[] = new int[nSolutions];
				int n1 = 0;
				int n2 = 0;

				for (int j = 0; j < values.size(); j++) {
					Pair<Float, Integer> v = values.get(j);
					if (first)
						gDistribution[v.mB]++;
					if (v.mA < cut) {
						d1[v.mB]++;
						n1++;
					} else {
						d2[v.mB]++;
						n2++;
					}
				}

				// compute entropy:
				if (first)
					gE = entropy(gDistribution);
				float e1 = entropy(d1);
				float e2 = entropy(d2);
				float e = (e1 * n1 + e2 * n2) / (float) (n1 + n2);

				if (first || e < bestE) {
					first = false;
					bestE = e;
					bestCut = cut;

					// System.out.println("next best: " + cut + " (" + e + ")");
				}
			}

			if (bestE < gE) {
				List<Float> l = new LinkedList<Float>();

				List<Pair<Float, Integer>> vl1 = new LinkedList<Pair<Float, Integer>>();
				List<Pair<Float, Integer>> vl2 = new LinkedList<Pair<Float, Integer>>();

				for (Pair<Float, Integer> v : values) {
					if (v.mA < bestCut) {
						vl1.add(v);
					} else {
						vl2.add(v);
					}
				}

				l.addAll(discretizeFeatureInternal(vl1, nSolutions, cuts - 1));
				l.add(new Float(bestCut));
				l.addAll(discretizeFeatureInternal(vl2, nSolutions, cuts - 1));
				return l;
			} else {
				return new LinkedList<Float>();
			}
		}
	}

	/**
	 * Entropy.
	 * 
	 * @param hist
	 *            the hist
	 * @return the float
	 */
	static float entropy(int hist[]) {
		int n = hist.length;
		int t = 0;

		for (int i = 0; i < n; i++)
			t += hist[i];

		float h = 0;

		// System.out.print("[ " + hist[0] + "," + hist[1] + "] -> ");

		for (int i = 0; i < n; i++) {
			if (hist[i] != 0) {
				float f = (float) hist[i] / (float) (t);
				h -= Math.log(f) * f;
			}
		}
		// System.out.println("" + h);
		return h;
	}

}
