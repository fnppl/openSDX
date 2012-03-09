package org.fnppl.opensdx.common;
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




/**
 * 
 * @author Michael Reincke <mreincke@finetunes.net>
 * 
 */
public class InfoUtube extends BusinessObject {

	public static String KEY_NAME = "utube";

	private BusinessStringItem url;								//COULD
	private BusinessStringItem channel;							//COULD

	public static InfoUtube make() {
		InfoUtube utube = new InfoUtube();
		utube.url = null;
		utube.channel = null;
		return utube;
	}
	
	public static InfoUtube fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final InfoUtube utube = new InfoUtube();
		utube.initFromBusinessObject(bo);

		utube.url = BusinessStringItem.fromBusinessObject(bo, "url");
		utube.channel = BusinessStringItem.fromBusinessObject(bo, "channel");
		
		return utube;
	}

	public InfoUtube url(String url) {
		this.url = new BusinessStringItem("url", url);
		return this;
	}

	public InfoUtube channel(String channel) {
		this.channel = new BusinessStringItem("channel", channel);
		return this;
	}

	public String getUrl() {
		if (url==null) return null;
		return url.getString();
	}

	public String getChannel() {
		if (channel==null) return null;
		return channel.getString();
	}
	public String getKeyname() {
		return KEY_NAME;
	}
}
