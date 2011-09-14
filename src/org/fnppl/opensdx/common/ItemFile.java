package org.fnppl.opensdx.common;

import java.io.File;

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.XMLElementable;
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

//import org.fnppl.opensdx.common.*;

public class ItemFile extends BusinessObject {

	public static String KEY_NAME = "file";

	private FileLocation location;								//MUST
	private BusinessStringItem filetype;						//COULD
	private BusinessStringItem samplerate;						//COULD
	private BusinessStringItem samplesize;						//COULD
	private BusinessStringItem bitrate;							//COULD
	private BusinessStringItem bitratetype;						//COULD
	private BusinessStringItem codec;							//COULD
	private BusinessStringItem codecsettings;					//COULD	
	private BusinessStringItem type;							//COULD
	private BusinessIntegerItem prelistening_offset;			//COULD
	private BusinessIntegerItem prelistening_length;			//COULD
	private BusinessLongItem bytes;								//COULD - better Long than Integer as for big filesizes
	private Checksums checksums;								//COULD
	private BusinessStringItem channels;						//COULD
	private BusinessCollection<BusinessIntegerItem> dimension; 	//COULD
	private BusinessBooleanItem no_file_given;			 		//COULD
	
	public static ItemFile make(File f) {
		ItemFile file = make();
		file.setFile(f);
		return file;
	}

	public static ItemFile make() {
		ItemFile file = new ItemFile();
		file.type = null;
		file.filetype = null;
		file.samplerate = null;
		file.samplesize = null;
		file.bitrate = null;
		file.bitratetype = null;
		file.codec = null;
		file.codecsettings = null;
		file.channels = null;
		file.bytes = null;
		file.location = FileLocation.make();
		file.checksums = Checksums.make();
		file.dimension = null;
		file.prelistening_offset = null;
		file.prelistening_offset = null;
		file.no_file_given = null;
		return file;
	}
	
	public ItemFile setFile(File f) {
		if (filetype == null || filetype.getString().equals("")) {
			if (f.getName().contains(".")) {
				filetype = new BusinessStringItem("filetype", f.getName().substring(f.getName().lastIndexOf('.')+1).toLowerCase());	
			}
		}
		location = null;
		checksums = null;
		bytes = null;
		if (f.exists() && !f.isDirectory()) {
			location = FileLocation.make(f.getAbsolutePath());
			int b = (int)f.length();
			bytes = new BusinessLongItem("bytes", b);
			try {
				byte[][] sums = SecurityHelper.getMD5SHA1(f);
				checksums = Checksums.make(sums[0],sums[1],null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public static ItemFile fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		ItemFile file = new ItemFile();
		file.initFromBusinessObject(bo);
		
		file.type = BusinessStringItem.fromBusinessObject(bo, "type");
		file.prelistening_length = BusinessIntegerItem.fromBusinessObject(bo, "prelistening_length");
		file.prelistening_offset = BusinessIntegerItem.fromBusinessObject(bo, "prelistening_offset");		
		file.filetype = BusinessStringItem.fromBusinessObject(bo, "filetype");
		file.samplerate = BusinessStringItem.fromBusinessObject(bo, "samplerate");
		file.samplesize = BusinessStringItem.fromBusinessObject(bo, "samplesize");
		file.bitrate = BusinessStringItem.fromBusinessObject(bo, "bitrate");
		file.bitratetype = BusinessStringItem.fromBusinessObject(bo, "bitratetype");
		file.codec = BusinessStringItem.fromBusinessObject(bo, "codec");
		file.codecsettings = BusinessStringItem.fromBusinessObject(bo, "codecsettings");
		file.channels = BusinessStringItem.fromBusinessObject(bo, "channels");
		file.bytes = BusinessLongItem.fromBusinessObject(bo, "bytes");
		
		file.checksums = Checksums.fromBusinessObject(bo);
		file.location = FileLocation.fromBusinessObject(bo);
		BusinessObject dim = file.handleBusinessObject("dimension");
		if (dim==null) {
			file.dimension = null;
		} else {
			try {
				file.dimension = new BusinessCollection<BusinessIntegerItem>() {
					public String getKeyname() {
						return "dimension";
					}
				};
				BusinessStringItem w = dim.handleBusinessStringItem("width");
				BusinessStringItem h = dim.handleBusinessStringItem("height");
				if (w!=null) file.dimension.add(new BusinessIntegerItem("width", Integer.parseInt(w.getString())));
				if (h!=null) file.dimension.add(new BusinessIntegerItem("height", Integer.parseInt(h.getString())));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return file;
	}
	
	public ItemFile setLocation(FileLocation loc) {
		location = loc;
		return this;
	}
	
	public ItemFile dimension(int width, int height) {
		dimension = new BusinessCollection<BusinessIntegerItem>() {
			public String getKeyname() {
				return "dimension";
			}
		};
		dimension.add(new BusinessIntegerItem("width", width));
		dimension.add(new BusinessIntegerItem("height", height));
		return this;
	}
	
	public ItemFile remove_dimension() {
		dimension = null;
		return this;
	}
		
	public Integer getDimensionWidth() {
		if (dimension==null) return null;
		for (int i=0;i<dimension.size();i++) {
			if (dimension.get(i).getKeyname().equals("width")) {
				return new Integer(dimension.get(i).getIntValue());
			}
		}
		return null;
	}
	
	public Integer getDimensionHeight() {
		if (dimension==null) return null;
		for (int i=0;i<dimension.size();i++) {
			if (dimension.get(i).getKeyname().equals("height")) {
				return new Integer(dimension.get(i).getIntValue());
			}
		}
		return null;
	}
	
	public ItemFile md5(byte[] bytes) {
		if (checksums==null) {
			checksums = Checksums.make();
		}
		checksums.md5(bytes);
		return this;
	}

	public ItemFile sha1(byte[] bytes) {
		if (checksums==null) {
			checksums = Checksums.make();
		}
		checksums.sha1(bytes);
		return this;
	}
	
	public ItemFile sha256(byte[] bytes) {
		if (checksums==null) {
			checksums = Checksums.make();
		}
		checksums.sha256(bytes);
		return this;
	}

	public ItemFile type(String type) {
		if (type == null) {
			this.type = null;
		} else {
			this.type = new BusinessStringItem("type", type);
		}
		return this;
	}

	public ItemFile filetype(String filetype) {
		if (filetype==null) {
			this.filetype = null;
		} else {
			this.filetype = new BusinessStringItem("filetype", filetype);
		}
		return this;
	}

	public ItemFile samplerate(String samplerate) {
		if (samplerate==null) {
			this.samplerate = null;
		} else {
			this.samplerate = new BusinessStringItem("samplerate", samplerate);
		}
		return this;
	}
	
	public ItemFile samplesize(String samplesize) {
		if (samplesize==null) {
			this.samplesize = null;
		} else {
			this.samplesize = new BusinessStringItem("samplesize", samplesize);
		}
		return this;
	}
	
	public ItemFile bitrate(String bitrate) {
		if (bitrate==null) {
			this.bitrate = null;
		} else {
			this.bitrate = new BusinessStringItem("bitrate", bitrate);
		}
		return this;
	}
	
	public ItemFile bitratetype(String bitratetype) {
		if (bitratetype==null) {
			this.bitratetype = null;
		} else {
			this.bitratetype = new BusinessStringItem("bitratetype", bitratetype);
		}
		return this;
	}
	
	public ItemFile codec(String codec) {
		if (codec==null) {
			this.codec = null;
		} else {
			this.codec = new BusinessStringItem("codec", codec);
		}
		return this;
	}
	
	public ItemFile codecsettings(String codecsettings) {
		if (codecsettings==null) {
			this.codecsettings = null;
		} else {
			this.codecsettings = new BusinessStringItem("codecsettings", codecsettings);
		}
		return this;
	}	
	
	public ItemFile channels(String channels) {
		if (channels == null) {
			this.channels = null;
		} else {
			this.channels = new BusinessStringItem("channels", channels);
		}
		return this;
	}
	
	public ItemFile prelistening_length(int prelistening_length) {
		this.prelistening_length = new BusinessIntegerItem("prelistening_length", prelistening_length);
		return this;
	}
	
	public ItemFile prelistening_offset(int prelistening_offset) {
		this.prelistening_offset = new BusinessIntegerItem("prelistening_offset", prelistening_offset);
		return this;
	}

	public ItemFile bytes(long length) {
		this.bytes = new BusinessLongItem("bytes", length);
		return this;
	}

	public void setPrelistening_offset(int prelistening_offset) {
		this.prelistening_offset = new BusinessIntegerItem("prelistening_offset", prelistening_offset);
	}

	public void setPrelistening_length(int prelistening_length) {
		this.prelistening_length = new BusinessIntegerItem("prelistening_length", prelistening_length);
	}

	public String getType() {
		if (type==null) return null;
		return type.getString();
	}

	public String getFiletype() {
		if (filetype==null) return null;
		return filetype.getString();
	}
	
	public String getSamplerate() {
		if (samplerate==null) return null;
		return samplerate.getString();
	}
	
	public String getSamplesize() {
		if (samplesize==null) return null;
		return samplesize.getString();
	}
	
	public String getBitrate() {
		if (bitrate==null) return null;
		return bitrate.getString();
	}
	
	public String getBitratetype() {
		if (bitratetype==null) return null;
		return bitratetype.getString();
	}
	
	public String getCodec() {
		if (codec==null) return null;
		return codec.getString();
	}
	
	public String getCodecsettings() {
		if (codecsettings==null) return null;
		return codecsettings.getString();
	}

	public String getChannels() {
		if (channels==null) return null;
		return channels.getString();
	}
	
	public String getLocationPath() {
		if (location==null) return null;
		return location.getPath();
	}
	
	public int getBytes() {
		if (bytes==null) return -1;
		return bytes.getLongValue();
	}
	
	public int getPrelistening_length() {
		if (prelistening_length==null) return -1;
		return prelistening_length.getIntValue();		
	}
	
	public int getPrelistening_offset() {
		if (prelistening_offset==null) return -1;
		return prelistening_offset.getIntValue();		
	}	

	public String getKeyname() {
		return KEY_NAME;
	}
	
	public ItemFile checksums(Checksums checksums) {
		this.checksums = checksums;
		return this;
	}
	
	public Checksums getChecksums() {
		return checksums;
	}
	
	public ItemFile no_file_given(boolean no_file_given) {
		this.no_file_given  = new BusinessBooleanItem("no_file_given", no_file_given);
		return this;
	}
	
	public boolean getNo_file_given() {
		if (no_file_given==null) return false;
		return no_file_given.getBoolean();
	}
	
}


