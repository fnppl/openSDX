package org.fnppl.opensdx.dmi;

/*
 * Copyright (C) 2010-2011 
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



import java.util.Vector;
import org.fnppl.opensdx.common.BaseObjectWithConstraints;

public class MediaFile extends BaseObjectWithConstraints {

	public MediaFile() {
		names.add("type"); values.add(null); constraints.add("MUST");
		names.add("filetype"); values.add(null); constraints.add("MUST");
		names.add("channels"); values.add(null); constraints.add("MUST");
		names.add("bytes"); values.add(null); constraints.add("MUST");
		names.add("location"); values.add(null); constraints.add("MUST");
		names.add("sha1"); values.add(null); constraints.add("MUST");
		names.add("md5"); values.add(null); constraints.add("MUST");
	}

// methods
	public void setType(String type) {
		set("type", type);
	}

	public String getType() {
		return get("type");
	}

	public void setFiletype(String filetype) {
		set("filetype", filetype);
	}

	public String getFiletype() {
		return get("filetype");
	}

	public void setChannels(String channels) {
		set("channels", channels);
	}

	public String getChannels() {
		return get("channels");
	}

	public void setBytes(long lengthInbytes) {
		set("bytes", lengthInbytes);
	}

	public long getBytes() {
		return getLong("bytes");
	}
	
//	<http>
//	<url></url><!-- MUST ; should be tls -->
//	<user></user><!-- COULD ; http-basic-header-auth -->
//	<pass></pass><!-- COULD ; http-basic-header-auth -->
//	<expiresdatetime>2011-06-10 20:24:00 GMT+00:00</expiresdatetime><!-- MUST ; until when this file is definitely available to be called -->
//</http><!-- http means that, this file is not delivered, but it can be pulled by the receiving parties ; integrity is assured by checksums -->
	public void setLocationHTTP(String url, String user, String pass, long expiresdatetime) {
		set("location", new String[] {"http",url,user,pass,FeedCreator.datemeGMT.format(expiresdatetime)});	
	}

//	<ftp>
//		<server></server>
//		<port></port>
//		<path></path>
//		<user></user><!-- COULD ; -->
//		<pass></pass><!-- COULD ; -->
//		<expiresdatetime>2011-06-10 20:24:00 GMT+00:00</expiresdatetime><!-- MUST ; until when this file is definitely available to be called -->
// </ftp>
	public void setLocationFTP(String server, int port, String user, String pass, long expiresdatetime) {
		set("location", new String[] {"ftp",server,""+port, user,pass,FeedCreator.datemeGMT.format(expiresdatetime)});	
	}

	public String[] getLocation() {
		return (String[])getObject("location");
	}

	public void setSHA1(String sha1) {
		set("sha1", sha1);
	}

	public String getSHA1() {
		return get("sha1");
	}
	
	public void setMD5(String md5) {
		set("md5", md5);
	}

	public String getMD5() {
		return get("md5");
	}

}
