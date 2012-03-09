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

package org.fnppl.opensdx.keyserver_web;


import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.RuntimeServices;


public class VeloLog implements LogChute {
	public static VeloLog instance = null;
	
	public static VeloLog getInstance() {
		if(instance == null) {
			instance = new VeloLog();		
		}
		
		return instance;
	}
	
  public VeloLog() {
//    try {
//      /*
//       *  register this class as a logger with the Velocity singleton
//       *  (NOTE: this would not work for the non-singleton method.)
//       */
//      Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this );
//      Velocity.init();
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
  }

  /**
   *  This init() will be invoked once by the LogManager
   *  to give you the current RuntimeServices instance
   */
  public void init(RuntimeServices rsvc) {
    // do nothing
  }

  /**
   *  This is the method that you implement for Velocity to
   *  call with log messages.
   */
  public void log(int level, String message) {
    /*  do something useful */
	  if(isLevelEnabled(level)) {
		  System.out.println(level+"::"+message);
	  }
  }

  /**
   *  This is the method that you implement for Velocity to
   *  call with log messages.
   */
  public void log(int level, String message, Throwable t)
  {
    /*  do something useful */
	  if(isLevelEnabled(level)) {
		  System.out.println(level+"::"+message);
		  t.printStackTrace();
	  }
  }

  /**
   *  This is the method that you implement for Velocity to
   *  check whether a specified log level is enabled.
   */
  public boolean isLevelEnabled(int level)
  {
    /*  do something useful */
    //return someBooleanValue;
	  if(level == -1 ) {
		  return true;
	  }
	  return false;
	  
  }
}
