package org.fnppl.opensdx.common;

import java.util.Vector;

import org.fnppl.opensdx.security.SecurityHelper;
import org.fnppl.opensdx.xml.ChildElementIterator;
import org.fnppl.opensdx.xml.XMLElementable;

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
 * @author Bertram Boedeker <bboedeker@gmx.de>
 * 
 */

public class LicenseBasis extends BusinessObject {

	public static String KEY_NAME = "license_basis";
	
	private Territorial territorial;  							 //MUST
	private BusinessCollection<BusinessDatetimeItem> timeframe;	 //MUST
	private BusinessObject pricing;								 //SHOULD
	private BusinessBooleanItem streaming_allowed;				 //COULD
	private BusinessCollection<BusinessStringItem> channels;	 //COULD
	private BusinessStringItem asOnBundle;
	
	private LicenseBasis() {
		
	}
	
	public static LicenseBasis make(Territorial territorial, long from, long to) {
		LicenseBasis b = new LicenseBasis();
		b.territorial = territorial;
		b.timeframe = new BusinessCollection<BusinessDatetimeItem>() {
			public String getKeyname() {
				return "timeframe";
			}
		};
		b.timeframe.add(new BusinessDatetimeItem("from", from));
		b.timeframe.add(new BusinessDatetimeItem("to", to));
		b.pricing = null;
		b.streaming_allowed = null;
		b.channels = null;
		b.asOnBundle = null;
		return b;
	}
	
	public static LicenseBasis make() {
		LicenseBasis b = new LicenseBasis();
		b.territorial = null;
		b.timeframe = null;
		b.pricing = null;
		b.streaming_allowed = null;
		b.channels = null;
		b.asOnBundle = null;
		return b;
	}	
	
	public static LicenseBasis makeAsOnBundle() {
		LicenseBasis b = new LicenseBasis();
		b.territorial = null;
		b.timeframe = null;
		b.pricing = null;
		b.streaming_allowed = null;
		b.channels = null;
		b.asOnBundle = new BusinessStringItem("as_on_bundle", "");
		return b;
	}
	
	public static LicenseBasis fromBusinessObject(BusinessObject bo) {
		if (bo==null) return null;
		if (!bo.getKeyname().equals(KEY_NAME)) {
			bo = bo.handleBusinessObject(KEY_NAME);
		}
		if (bo==null) return null;
		final LicenseBasis b = new LicenseBasis();
		b.initFromBusinessObject(bo);
		
		b.territorial = Territorial.fromBusinessObject(bo);
		b.timeframe = null;
		
		BusinessObject boTimeFrame = b.handleBusinessObject("timeframe");
		if (boTimeFrame!=null) {
			b.timeframe = new BusinessCollection<BusinessDatetimeItem>() {
				public String getKeyname() {
					return "timeframe";
				}
			};
			BusinessDatetimeItem from = BusinessDatetimeItem.fromBusinessObject(boTimeFrame, "from");
			if (from!=null) {
				b.timeframe.add(from);
			} else {
				b.timeframe.add(new BusinessDatetimeItem("from", -1L));
			}
			BusinessDatetimeItem to = BusinessDatetimeItem.fromBusinessObject(boTimeFrame, "to");
			if (to!=null) {
				b.timeframe.add(to);
			} else {
				b.timeframe.add(new BusinessDatetimeItem("to", -1L));
			}
		}
		
		b.pricing = bo.handleBusinessObject("pricing");
		b.streaming_allowed = BusinessBooleanItem.fromBusinessObject(b, "streaming_allowed");
		
		b.channels = null;
		XMLElementable xChannels = bo.getOtherObject("channels");
		if (xChannels!=null && xChannels instanceof BusinessObject) {
			b.channels = new BusinessCollection<BusinessStringItem>() {
				public String getKeyname() {
					return "channels";
				}
			};
			new ChildElementIterator(b, "channels","channel") {
				public void processBusinessStringItem(BusinessStringItem item) {
					b.channels.add(item);
				}
			};
		}
		b.asOnBundle = BusinessStringItem.fromBusinessObject(b, "as_on_bundle");
		return b;
	}
	
	
	public LicenseBasis timeframe_from_datetime(long timeframe_from_datetime) {
		if (timeframe==null) {
			timeframe = new BusinessCollection<BusinessDatetimeItem>() {
				public String getKeyname() {
					return "timeframe";
				}
			};
		}
		timeframe.set(new BusinessDatetimeItem("from", timeframe_from_datetime));
		return this;
	}
	
	public LicenseBasis timeframe_to_datetime(long timeframe_to_datetime) {
		if (timeframe==null) {
			timeframe = new BusinessCollection<BusinessDatetimeItem>() {
				public String getKeyname() {
					return "timeframe";
				}
			};
		}
		timeframe.set(new BusinessDatetimeItem("to", timeframe_to_datetime));
		return this;
	}
	
	public LicenseBasis as_on_bundle(boolean value) {
		if (value) {
			asOnBundle = new BusinessStringItem("as_on_bundle", "");
			territorial = null;
			timeframe = null;
			pricing = null;
			streaming_allowed = null;
			channels = null;
		} else {
			asOnBundle = null;
			if (timeframe==null) {
				timeframe = new BusinessCollection<BusinessDatetimeItem>() {
					public String getKeyname() {
						return "timeframe";
					}
				};
				timeframe.add(new BusinessDatetimeItem("from", -1L));
				timeframe.add(new BusinessDatetimeItem("to", -1L));
			}
		}
		return this;
	}
	
	public boolean isAsOnBundle() {
		if (asOnBundle==null) return false;
		else return true;
	}
	
	public Long getTimeframeFrom() {
		if (timeframe==null) return null;
		for (int i=0;i<timeframe.size();i++) {
			if (timeframe.get(i).getKeyname().equals("from")) return new Long(timeframe.get(i).getDatetime());  
		}
		return null;
	}
	
	public String getTimeframeFromText() {
		Long l = getTimeframeFrom();
		if (l==null) return null;
		return SecurityHelper.getFormattedDate(l.longValue());
	}
	
	public Long getTimeframeTo() {
		if (timeframe==null) return null;
		for (int i=0;i<timeframe.size();i++) {
			if (timeframe.get(i).getKeyname().equals("to")) return new Long(timeframe.get(i).getDatetime());  
		}
		return null;
	}
	
	public String getTimeframeToText() {
		Long l = getTimeframeTo();
		if (l==null) return null;
		return SecurityHelper.getFormattedDate(l.longValue());
	}
	
	
	public LicenseBasis streaming_allowed(boolean streaming_allowed) {
		this.streaming_allowed = new BusinessBooleanItem("streaming_allowed", streaming_allowed);
		if (!streaming_allowed) {
			channels = null;
		}
		return this;
	}
	
	public LicenseBasis addChannel(String name, boolean allow) {
		if (channels == null) {
			channels = new BusinessCollection<BusinessStringItem>() {
				public String getKeyname() {
					return "channels";
				}
			}; 
		}
		BusinessStringItem item = new BusinessStringItem("channels", name);
		item.setAttribute("type", (allow?"allow":"disallow"));
		channels.add(item);
		return this;
	}

	public boolean isStreaming_allowed() {
		if (streaming_allowed==null) return false;
		return streaming_allowed.getBoolean();
	}

	
	public LicenseBasis pricing_pricecode(String pricecode) {
		if (pricing==null) {
			pricing = new BusinessObject() {
				public String getKeyname() {
					return "pricing";
				}
			};
		}
		if (pricecode==null) {
			pricing.removeObject("pricecode");
		} else {
			pricing.setObject(new BusinessStringItem("pricecode", pricecode));
		}
		return this;
	}

	public LicenseBasis pricing_wholesale(String wholesale) {
		if (pricing==null) {
			pricing = new BusinessObject() {
				public String getKeyname() {
					return "pricing";
				}
			};
		}
		if (wholesale==null) {
			pricing.removeObject("wholesale");
		} else {
			pricing.setObject(new BusinessStringItem("wholesale", wholesale));
		}
		return this;
	}
	
	public String getKeyname() {
		return KEY_NAME;
	}
	
	public String getPricingWholesale() {
		if (pricing == null || pricing.getBusinessStringItem("wholesale")==null) return null;
		return pricing.getBusinessStringItem("wholesale").getString();
	}
	
	public String getPricingPricecode() {
		if (pricing == null || pricing.getBusinessStringItem("pricecode")==null) return null;
		return pricing.getBusinessStringItem("pricecode").getString();
	}
	
	public Territorial getTerritorial() {
		return territorial;
	}
	
	public void setTerritorial(Territorial t) {
		territorial = t;
	}
	
	public int getChannelsCount() {
		if (channels==null) return 0;
		return channels.size();
	}
	
	public String getChannelName(int index) {
		if (channels==null) return null;
		return channels.get(index).getString();
	}
	
	public boolean getChannelAllowed(int index) {
		if (channels==null) return false;
		String type = channels.get(index).getAttribute("type");
		if (type.equalsIgnoreCase("allow")) return true;
		return false;
	}
	
	public void removeChannel(int index) {
		if (channels==null) return;
		channels.remove(index);
	}
	public void removeAllChannels() {
		if (channels==null) return;
		channels.removeAll();
	}
	public void removeChannels() {
		channels = null;
	}
	
}
