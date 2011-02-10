package org.fnppl.opensdx.dbaccess;

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
