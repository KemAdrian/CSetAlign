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
  
 package csic.iiia.ftl.base.utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Pair.
 * 
 * @param <T1>
 *            the generic type
 * @param <T2>
 *            the generic type
 */
public class Pair<T1, T2> {

	/** The m_a. */
	public T1 mA;

	/** The m_b. */
	public T2 mB;

	/**
	 * Gets the m_a.
	 * 
	 * @return the m_a
	 */
	public T1 getM_a() {
		return mA;
	}

	/**
	 * Sets the m_a.
	 * 
	 * @param m_a
	 *            the m_a to set
	 */
	public void setM_a(T1 m_a) {
		this.mA = m_a;
	}

	/**
	 * Gets the m_b.
	 * 
	 * @return the m_b
	 */
	public T2 getM_b() {
		return mB;
	}

	/**
	 * Sets the m_b.
	 * 
	 * @param m_b
	 *            the m_b to set
	 */
	public void setM_b(T2 m_b) {
		this.mB = m_b;
	}

	/**
	 * Instantiates a new pair.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 */
	public Pair(T1 a, T2 b) {
		mA = a;
		mB = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "<" + mA + "," + mB + ">";
	}
}
