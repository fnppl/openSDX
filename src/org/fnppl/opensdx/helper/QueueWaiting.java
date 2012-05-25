package org.fnppl.opensdx.helper;
/*
 * Copyright (C) 2010-2012 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 */

/*
 * Software license
 *
 * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
 *  
 * This file is part of openSDX
 * openSDX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * openSDX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * and GNU General Public License along with openSDX.
 * If not, see <http://www.gnu.org/licenses/>.
 *      
 */

/*
 * Documentation license
 * 
 * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
 * 
 * This file is part of openSDX.
 * Permission is granted to copy, distribute and/or modify this document 
 * under the terms of the GNU Free Documentation License, Version 1.3 
 * or any later version published by the Free Software Foundation; 
 * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
 * A copy of the license is included in the section entitled "GNU 
 * Free Documentation License" resp. in the file called "FDL.txt".
 * 
 */
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class QueueWaiting<E> {
	private Vector<E> queue = null;
	private boolean readyForNext = true;
	private boolean stop = false;
	
	public QueueWaiting() {
		queue = new Vector<E>();
		readyForNext = true;
		stop = false;
	}
	
	public synchronized E get() {
		while(queue.size()==0 || !readyForNext) {
			try {
				//System.out.println("waiting ... "+queue.size()+" -> "+readyForNext);
				wait();
			} catch(InterruptedException e) {
				System.out.println("InterruptedException caught");
			}
		}
		if (stop) return null;
		readyForNext = false;
		return queue.remove(0);
	}

	public synchronized void put(E value) {
		queue.add(value);
		notify();
	}
	
	public synchronized void readyForNext() {
		readyForNext = true;
		notify();
	}
	
	public synchronized List<E> list() {
		return Collections.unmodifiableList(queue);
	}
	
	public synchronized int countWaiting() {
		return queue.size();
	}
	
	public synchronized void stop() {
		stop = true;
		notify();
	}
}
