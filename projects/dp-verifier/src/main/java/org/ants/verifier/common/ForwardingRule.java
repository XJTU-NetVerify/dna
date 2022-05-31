/*
 * Atomic Predicates Verifier
 * 
 * Copyright (c) 2013 UNIVERSITY OF TEXAS AUSTIN. All rights reserved. Developed
 * by: HONGKUN YANG and SIMON S. LAM http://www.cs.utexas.edu/users/lam/NRL/
 * Copyright (c) 2022 ANTS Lab, Xi'an Jiaotong University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimers.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimers in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the UNIVERSITY OF TEXAS AUSTIN nor the names of the
 * developers may be used to endorse or promote products derived from this
 * Software without specific prior written permission.
 * 
 * 4. Any report or paper describing results derived from using any part of this
 * Software must cite the following publication of the developers: Hongkun Yang
 * and Simon S. Lam, Real-time Verification of Network Properties using Atomic
 * Predicates, IEEE/ACM Transactions on Networking, April 2016, Volume 24, No.
 * 2, pages 887-900 (first published March 2015, Digital Object Identifier:
 * 10.1109/TNET.2015.2398197).
 * 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH
 * THE SOFTWARE.
 */

package org.ants.verifier.common;

public class ForwardingRule implements Comparable<ForwardingRule>{
	long destip;
	int prefixlen;
	String outinterface;
	int priority;
	int bddrep;
	boolean visible;
	
	public ForwardingRule(long destip, int prefixlen, String outinterface)
	{
		this.destip = destip;
		this.prefixlen = prefixlen;
		this.outinterface = outinterface;
		this.priority = prefixlen;
		this.visible = true;
	}
	public ForwardingRule(long destip, int prefixlen, String outinterface, int priority)
	{
		this.destip = destip;
		this.prefixlen = prefixlen;
		this.outinterface = outinterface;
		this.priority = priority;
		this.visible = true;
	}
	
	public int compareTo(ForwardingRule another_rule) {
        return this.prefixlen - another_rule.prefixlen;
    }
	
	public void setVisible()
	{
		visible = true;
	}
	
	public void setInvisible()
	{
		visible = false;
	}
	
	public boolean isvisible()
	{
		return visible;
	}
	
	public long getdestip()
	{
		return destip;
	}
	
	public int getprefixlen()
	{
		return prefixlen;
	}
	
	public String getiname()
	{
		return outinterface;
	}
	public int getPriority()
	{
		return priority;
	}
	
	public void setBDDRep(int bddentry)
	{
		bddrep = bddentry;
	}
	
	public int getBDDRep()
	{
		return bddrep;
	}

	public String toString()
	{
		return destip + " " + prefixlen + " " + outinterface;
	}
}
