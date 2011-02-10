package org.fnppl.dbaccess;

/*
 * Copyright (C) 2010-2011 
 * 							fine people e.V. <opensdx@fnppl.org> 
 * 							Henning Thieß <ht@fnppl.org>
 * 
 * 							http://fnppl.org
 * 
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
 * HT 20110210
 * 
 * 1. layer on top of the *real* database - may it be nosql / sql / object / whatever (of course, i got for postgres)
 * 2. this layer then can serve not only database-lines, but the *real* objects instead
 * 3. this layer is connected to other instances to allow caching (like distributed memcache)
 * 4. connection goes by tcp4 and is defined on init-phase...
 * 
 */

public class ConnectedObjectPersistence {

}
