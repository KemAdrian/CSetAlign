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
  
 package csic.iiia.ftl.base.core.subsumption;

import csic.iiia.ftl.base.core.FeatureTerm;
import csic.iiia.ftl.base.core.SetFeatureTerm;
import csic.iiia.ftl.base.utils.FeatureTermException;
import csic.iiia.ftl.base.utils.SubsumptionTimeOutException;

/**
 * The Class MetaSubsumption.
 * 
 * @author santi
 */
public class MetaSubsumption {

	/**
	 * Subsumes. It gives half a second to regular subsumption, and otherwise, switch to CSP subsumption
	 * 
	 * @param f1
	 *            the f1
	 * @param f2
	 *            the f2
	 * @return true, if successful
	 * @throws FeatureTermException
	 *             the feature term exception
	 */
	public static boolean subsumes(FeatureTerm f1, FeatureTerm f2) throws FeatureTermException {
		try {
			if (f1 instanceof SetFeatureTerm || f2 instanceof SetFeatureTerm) {
				return FTSubsumption.subsumes(f1, f2);
			} else {
				return FTSubsumption.subsumes(f1, f2, 200);
			}
		} catch (SubsumptionTimeOutException e) {
			return CSPSubsumption.subsumes(f1, f2);
		}
	}

}
