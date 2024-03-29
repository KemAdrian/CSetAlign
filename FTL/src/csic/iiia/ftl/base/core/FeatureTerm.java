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
  
 package csic.iiia.ftl.base.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import csic.iiia.ftl.base.core.subsumption.CSPSubsumption;
import csic.iiia.ftl.base.core.subsumption.FTSubsumption;
import csic.iiia.ftl.base.core.subsumption.MetaSubsumption;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.Pair;

/**
 * The Class FeatureTerm.
 */
public abstract class FeatureTerm implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -62226518223622800L;

	/** The Constant SUBSUMPTION_NORMAL. */
	public static final int SUBSUMPTION_NORMAL = 0;

	/** The Constant SUBSUMPTION_CSP. */
	public static final int SUBSUMPTION_CSP = 1;

	/** The Constant SUBSUMPTION_META. */
	public static final int SUBSUMPTION_META = 2;

	/** The SUBSUMPTO n_ method. */
	public static int SUBSUMPTON_METHOD = SUBSUMPTION_NORMAL;

	/** The m name. */
	Symbol mName = null;

	/** The m sort. */
	Sort mSort;

	/**
	 * Gets the sort.
	 * 
	 * @return the sort
	 */
	public Sort getSort() {
		return mSort;
	}

	/**
	 * Sets the sort.
	 * 
	 * @param s
	 *            the new sort
	 */
	public void setSort(Sort s) {
		mSort = s;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public Symbol getName() {
		return mName;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(Symbol name) {
		mName = name;
	}

	/**
	 * Gets the data type.
	 * 
	 * @return the data type
	 */
	public int getDataType() {
		if (mSort == null) {
			return Sort.DATATYPE_SET;
		}

		return mSort.mDataType;
	}

	/**
	 * Equivalents.
	 * 
	 * @param f
	 *            the f
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public boolean equivalents(FeatureTerm f) {
		try {
			return subsumes(f) && f.subsumes(this);
		} catch (FeatureTermException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Checks for value.
	 * 
	 * @return true, if successful
	 */
	public abstract boolean hasValue();

	/**
	 * Checks if is leaf.
	 * 
	 * @return true, if is leaf
	 */
	public abstract boolean isLeaf();

	/**
	 * Checks if is constant.
	 * 
	 * @return true, if is constant
	 */
	public boolean isConstant() {
		return false;
	}

	/**
	 * Feature values.
	 * 
	 * @param feature
	 *            the feature
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public List<FeatureTerm> featureValues(String feature) throws FeatureTermException {
		FeatureTerm v = featureValue(feature);
		if (v == null)
			return new LinkedList<FeatureTerm>();
		if (v instanceof SetFeatureTerm) {
			List<FeatureTerm> l = new LinkedList<FeatureTerm>();
			l.addAll(((SetFeatureTerm) v).getSetValues());
			return l;
		} else {
			List<FeatureTerm> l = new LinkedList<FeatureTerm>();
			l.add(v);
			return l;
		}
	}

	/**
	 * Feature values.
	 * 
	 * @param feature
	 *            the feature
	 * @return the list
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public List<FeatureTerm> featureValues(Symbol feature) throws FeatureTermException {
		FeatureTerm v = featureValue(feature);
		if (v == null)
			return new LinkedList<FeatureTerm>();
		if (v instanceof SetFeatureTerm) {
			List<FeatureTerm> l = new LinkedList<FeatureTerm>();
			l.addAll(((SetFeatureTerm) v).getSetValues());
			return l;
		} else {
			List<FeatureTerm> l = new LinkedList<FeatureTerm>();
			l.add(v);
			return l;
		}
	}

	/**
	 * Feature value.
	 * 
	 * @param feature
	 *            the feature
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm featureValue(String feature) throws FeatureTermException {
		throw new FeatureTermException("FeatureValue of non-term: " + this.getClass().getName() + " -> " + feature);
	}

	/**
	 * Feature value.
	 * 
	 * @param feature
	 *            the feature
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm featureValue(Symbol feature) throws FeatureTermException {
		throw new FeatureTermException("FeatureValue of non-term: " + this.getClass().getName() + " -> " + feature.get());
	}

	/**
	 * Read path.
	 * 
	 * @param path
	 *            the path
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm readPath(Path path){
		FeatureTerm t = this;
		for (Symbol s : path.features) {
			try {
				if (t != null) {
					t = t.featureValue(s);
				}	
			} catch (Exception e) {
				// TODO: handle exception
			}
		} // for

		return t;
	} // FeatureTerm::readPath

	/**
	 * To string noos.
	 * 
	 * @return the string
	 */
	public String toStringNOOS() {
		return toStringNOOS(null);
	} // FeatureTerm::toStringNOOS

	/**
	 * To string noos.
	 * 
	 * @param tabs
	 *            the tabs
	 * @return the string
	 */
	public String toStringNOOS(int tabs) {
		return toStringNOOS(null, tabs);
	} // FeatureTerm::toStringNOOS

	/**
	 * To string noos.
	 * 
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	public String toStringNOOS(FTKBase dm) {
		String tmp;
		List<FeatureTerm> bindings = new LinkedList<FeatureTerm>();

		tmp = toStringNOOSInternal(bindings, 0, dm);

		{
			int i;
			String tmp2, tmp3;

			// Search for nonused variables, and remove them:
			for (i = 0; i < bindings.size(); i++) {
				tmp2 = "!X" + (i + 1) + ")";
				tmp3 = "!X" + (i + 1) + "\n";
				if (!tmp.contains(tmp2) && !tmp.contains(tmp3)) {
					// delete variable:
					tmp2 = "?X" + (i + 1) + " ";
					tmp = tmp.replace(tmp2, "");
				} // if
			} // for
		}

		return tmp;
	} // FeatureTerm::toStringNOOS

	/**
	 * To string noos.
	 * 
	 * @param dm
	 *            the dm
	 * @param tabs
	 *            the tabs
	 * @return the string
	 */
	public String toStringNOOS(FTKBase dm, int tabs) {
		String tmp;
		List<FeatureTerm> bindings = new LinkedList<FeatureTerm>();

		tmp = toStringNOOSInternal(bindings, tabs, dm);

		{
			int i;
			String tmp2, tmp3;

			// Search for nonused variables, and remove them:
			for (i = 0; i < bindings.size(); i++) {
				tmp2 = "!X" + (i + 1) + ")";
				tmp3 = "!X" + (i + 1) + "\n";
				if (!tmp.contains(tmp2) && !tmp.contains(tmp3)) {
					// delete variable:
					tmp2 = "?X" + (i + 1) + " ";
					tmp = tmp.replace(tmp2, "");
				} // if
			} // for
		}

		return tmp;
	} // FeatureTerm::toStringNOOS

	/**
	 * To string noos internal.
	 * 
	 * @param bindings
	 *            the bindings
	 * @param tabs
	 *            the tabs
	 * @param dm
	 *            the dm
	 * @return the string
	 */
	String toStringNOOSInternal(List<FeatureTerm> bindings, int tabs, FTKBase dm) {
		String tmp = "";
		int ID = -1;

		if (mName != null && dm != null && dm.contains(this)) {
			return tmp + mName.get();
		}

		ID = bindings.indexOf(this);
		if (ID == -1) {
			bindings.add(this);
			ID = bindings.indexOf(this);

			tmp += "(define ?X" + (ID + 1) + " (" + mSort.get();

			if (mName != null) {
				tmp += " :id " + mName.get();
			} // if

			return tmp + "))";
		} else {
			return "!X" + (ID + 1);
		} // if
	} // FeatureTerm::toStringNOOSInternal

	// Makes an independent copy of the Feature Term
	/**
	 * Clone.
	 * 
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm clone(Ontology o) throws FeatureTermException {
		return clone((FTKBase) null, o);
	} // FeatureTerm::clone

	/**
	 * Clone.
	 * 
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm clone(FTKBase dm, Ontology o) throws FeatureTermException {
		HashMap<FeatureTerm, FeatureTerm> correspondences = new HashMap<FeatureTerm, FeatureTerm>();

		return cloneInternal(correspondences, dm, o);
	} // FeatureTerm::clone

	/**
	 * Clone.
	 * 
	 * @param correspondences
	 *            the correspondences
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm clone(HashMap<FeatureTerm, FeatureTerm> correspondences) throws FeatureTermException {
		return clone(null, correspondences);
	} // FeatureTerm::clone

	/**
	 * Clone.
	 * 
	 * @param dm
	 *            the dm
	 * @param correspondences
	 *            the correspondences
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm clone(FTKBase dm, HashMap<FeatureTerm, FeatureTerm> correspondences) throws FeatureTermException {
		return cloneInternal(correspondences, dm, mSort.getOntology());
	} // FeatureTerm::clone

	/**
	 * Clone.
	 * 
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @param correspondences
	 *            the correspondences
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public FeatureTerm clone(FTKBase dm, Ontology o, HashMap<FeatureTerm, FeatureTerm> correspondences) throws FeatureTermException {
		return cloneInternal(correspondences, dm, o);
	} // FeatureTerm::clone

	/**
	 * Clone internal.
	 * 
	 * @param correspondences
	 *            the correspondences
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	FeatureTerm cloneInternal(HashMap<FeatureTerm, FeatureTerm> correspondences, FTKBase dm, Ontology o) throws FeatureTermException {
		FeatureTerm correspondence = correspondences.get(this);

		if (correspondence == null) {
			if (dm == null || !dm.contains(this)) {
				return cloneInternal2(correspondences, dm, o);
			} else {
				return this;
			} // if
		} else {
			return correspondence;
		} // if
	} // FeatureTerm::cloneInternal

	/**
	 * Substitute.
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 */
	public void substitute(FeatureTerm f1, FeatureTerm f2) {
		HashSet<FeatureTerm> visited = new HashSet<FeatureTerm>();
		List<FeatureTerm> open_nodes = new LinkedList<FeatureTerm>();
		FeatureTerm node;
		boolean beautifySets = false;

		visited.add(this);
		open_nodes.add(this);
		open_nodes.add(f2);

		while (!open_nodes.isEmpty()) {
			node = open_nodes.remove(0);

			if (node instanceof TermFeatureTerm) {
				for (Entry<Symbol, FeatureTerm> feature2 : ((TermFeatureTerm) node).getFeatures()) {
					FeatureTerm ft2 = feature2.getValue();
					if (ft2 != null) {
						if (ft2.equals(f1)) {
							((TermFeatureTerm) node).defineFeatureValue(feature2.getKey(), f2);
						} else {
							if (!visited.contains(ft2)) {
								visited.add(ft2);
								open_nodes.add(ft2);
							}
						}
					} else {
						System.err.println("Warning: feature has null value!");
					}
				}
			} // if

			if (node instanceof SetFeatureTerm) {
				List<FeatureTerm> setValues = ((SetFeatureTerm) node).getSetValues();
				if (setValues.contains(f2)) {
					setValues.remove(f1);
					if (setValues.size() == 1)
						beautifySets = true;
					for (FeatureTerm ft2 : setValues) {
						if (!visited.contains(ft2)) {
							visited.add(ft2);
							open_nodes.add(ft2);
						}
					} // for
				} else {
					for (FeatureTerm ft2 : setValues) {
						if (ft2.equals(f1)) {
							((SetFeatureTerm) node).getSetValues().set(((SetFeatureTerm) node).getSetValues().indexOf(ft2), f2);
						} else {
							if (!visited.contains(ft2)) {
								visited.add(ft2);
								open_nodes.add(ft2);
							}
						}
					} // for
				}
			} // if/
		} // while

		if (beautifySets) {
			HashMap<SetFeatureTerm, Set<Pair<TermFeatureTerm, Symbol>>> m = FTRefinement.setsWithAllParents(this);
			for (SetFeatureTerm set : m.keySet()) {
				if (set.getSetValues().size() == 1) {
					Set<Pair<TermFeatureTerm, Symbol>> parents = m.get(set);
					for (Pair<TermFeatureTerm, Symbol> parent : parents) {
						parent.mA.defineFeatureValue(parent.mB, set.getSetValues().get(0));
					}
				}
			}
		}

	} // substitute

	/**
	 * Clone internal2.
	 * 
	 * @param correspondences
	 *            the correspondences
	 * @param dm
	 *            the dm
	 * @param o
	 *            the o
	 * @return the feature term
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	abstract FeatureTerm cloneInternal2(HashMap<FeatureTerm, FeatureTerm> correspondences, FTKBase dm, Ontology o) throws FeatureTermException;

	/**
	 * Subsumes.
	 * 
	 * @param f
	 *            the f
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public boolean subsumes(FeatureTerm f) throws FeatureTermException {
		switch (SUBSUMPTON_METHOD) {
		case SUBSUMPTION_CSP:
			return CSPSubsumption.subsumes(this, f);
		case SUBSUMPTION_META:
			return MetaSubsumption.subsumes(this, f);
		default:
			return FTSubsumption.subsumes(this, f);
		}
	}

	/**
	 * Check data type.
	 * 
	 * @return true, if successful
	 */
	public boolean checkDataType() {

		if (mSort == null) {
			if (!(this instanceof SetFeatureTerm)) {
				System.err.println("Feature term has data type " + mSort.mDataType + " and has class " + this.getClass().getSimpleName());
				return false;
			}
			return true;
		}
		if (mSort.mDataType == Sort.DATATYPE_INTEGER && !(this instanceof IntegerFeatureTerm) || mSort.mDataType == Sort.DATATYPE_FLOAT
				&& !(this instanceof FloatFeatureTerm) || mSort.mDataType == Sort.DATATYPE_SYMBOL && !(this instanceof SymbolFeatureTerm)
				|| mSort.mDataType == Sort.DATATYPE_FEATURETERM && !(this instanceof TermFeatureTerm) || mSort.mDataType == Sort.DATATYPE_SET
				&& !(this instanceof SetFeatureTerm)) {
			System.err.println("Feature term has data type " + mSort.mDataType + " for sort " + mSort.get() + " and has class "
					+ this.getClass().getSimpleName());
			return false;
		}

		return true;
	}

	/**
	 * Consistency check.
	 * 
	 * @param dm
	 *            the dm
	 * @return true, if successful
	 */
	public boolean consistencyCheck(FTKBase dm) {
		List<Pair<FeatureTerm, Path>> nodes = FTRefinement.variablesWithPaths(this, dm);

		for (Pair<FeatureTerm, Path> node : nodes) {
			if (!node.mA.checkDataType()) {
				System.err.println("Feature Term not properly formed! error in path: " + node.mB);
				return false;
			}
		}

		return true;
	}
}
