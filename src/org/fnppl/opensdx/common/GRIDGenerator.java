package org.fnppl.opensdx.common;

import java.security.SecureRandom;
import java.util.*;

/*
 * Copyright (C) 2010-2015 
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

public class GRIDGenerator {

//	static int maxIndex;
//	static String anfang = "A1-0389P";
//	static String anfangRoh = "A10389P";
	public static String range = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static Hashtable<Character, Integer> charToInt = new Hashtable<Character, Integer>(); 
	public static Hashtable<Integer, Character> intToChar = new Hashtable<Integer, Character>();
	public static Hashtable<Character, Integer> charToIntISO = new Hashtable<Character, Integer>(); 
	public static Hashtable<Integer, Character> intToCharISO = new Hashtable<Integer, Character>();
	static {
		for( int c=0; c<range.length(); c++) {
			char l = range.charAt(c);
			charToInt.put(l, c);
			intToChar.put(c, l);
		}
		for (int d=0; d<range.length(); d++) {
			char m = range.charAt(d);
			if (d>=13) {
				int eins = d+1;
				if (eins>=26) {
					int zwei = d+2;
					charToIntISO.put(m, zwei);
					intToCharISO.put(zwei, m);
//					}
				} else {
					charToIntISO.put(m, eins);
					intToCharISO.put(eins, m);
				}
			} else {
				charToIntISO.put(m, d);
				intToCharISO.put(d, m);
			}
		}
	}

	/**
	 * Ermittelt das letzte zeichen bzw. die prüfsumme des GRIDs.
	 * @param prefix besteht aus "Identifier Scheme element" und "Issuer Code element"
	 * @param variable_part_of_GRID ist das "Release Number element"
	 * @return check character als char
	 * @throws Exception falls die parameter nicht die richtige laenge haben.
	 */
	public static char checkCharacterCalculation(char[] prefix, char[] variable_part_of_GRID) throws Exception {
		if(prefix.length != 7) {
			throw new Exception("GRID-prefix != 7");
		}
		if(variable_part_of_GRID.length != 10) {
			throw new Exception("GRID-base != 10");
		}
		
		char[] a = new char[18];
		for(int i=0;i<prefix.length;i++) {
			a[17-i] = prefix[i];
		}
		for(int i=0;i<variable_part_of_GRID.length;i++) {
			a[17-prefix.length-i] = variable_part_of_GRID[i];
		}
		
		int s = 0;
		int p = 36;
//		System.out.println("go:");
//		for (int nakla =0; nakla<a.length; nakla++) {
//			System.out.println("nr. " +nakla +"\t" +"wert " +a[nakla]);
//		}
//		System.out.println("ende");
		
		for (int j=1; j<=17; j++) {
			s = p%37 + charToInt.get(a[17-j+1]);
//			System.out.println("j: " +j +"\tchar: " +a[j-1] +"\tp = " +p +"\ts = " +s);
			if ( s%36 == 0 ) {
				p = 36 * 2;
			} else {
				p = (s%36) * 2;
			}
		}
//		System.out.println("j: " +18 +"\tchar: " +a[17] +"\tp = " +p +"\ts = ???" );
		
		int werta = 0;
		while( (p%37+werta-1)%36 != 0) {
			werta++;
		}
//		System.out.println("gesuchter Wert: " +werta +"\tentspricht: " +range.charAt(werta));
		char checkDigit = range.charAt(werta);
		
		return checkDigit;
	}
	
	/**
	 * Ermittelt aus albumID und variable (wird vor albumID gesetzt) das check digit nach ISO6346 Norm.
	 * Wobei hier nicht 11 der Teiler ist, sondern 13.
	 * @param albumID
	 * @param variable
	 * @return check digit als char
	 */
	public static char checkAlbumidISO (long albumID, char[] variable) {
		char[] albumChar = String.valueOf(albumID).toCharArray();
		char[] toCheckISO = new char[2+albumChar.length];
		System.arraycopy(variable, 0, toCheckISO, 0, variable.length);
		System.arraycopy(albumChar, 0, toCheckISO, variable.length, albumChar.length);
		int sum = 0;
		
		for (int a=0; a<toCheckISO.length; a++) {
			sum = (int) (sum+(Math.pow(2, a)*charToIntISO.get(toCheckISO[a])));
		}
		int quotient = sum/13;
		int produkt = quotient*13;
		int differenz = sum-produkt;
		char checkISO = intToCharISO.get(differenz);
//		System.out.println("summe: " +sum +", quotient: " +quotient +", produkt: " +produkt +", differenz: " +differenz +", checkISO: " +checkISO);
		return checkISO;
	}
	/**
	 * Wandelt ein GRID in einen String inkl. Bindestrichen um.
	 * @param GRID zu aendernder GRID
	 * @return konvertierter String
	 */
	public static String convertGRIDToString (char[] GRID) {
		String converted = "";
		for ( int i = 0; i < GRID.length; i++) {
			if ( i == 2 || i == 7 || i == 17) {
				converted += "-";
			}
			converted += GRID[i];
		}
		return converted;
	}
	
	/**
	 * Erstellt komplette GRID's.
	 * @param prefix besteht aus "Identifier Scheme element" und "Issuer Code element"
	 * @param variable wird am anfang von "Release Number element" gesetzt
	 * @param anzahl anzahl der zu erstellenden GRIDs
	 * @return einen Vector mit der gewuenschten Anzahl GRIDs
	 * @throws Exception
	 */
	public static Vector<char[]> generateRandomGRIDs(char[] prefix, char[] variable, int anzahl) throws Exception {
		//OBACHT: variable kann hier länge 0 bis 9 haben => das sind die VON VORNE vorgegebenen werte
		
		SecureRandom rand = new SecureRandom();
		Vector<char[]> veci= new Vector<char[]>();
		
		int random;

		for (int z = 0; z < anzahl; z++) {
			char[] zeichenRoh = new char[10];
			for (int x = 0; x<variable.length; x++) {
				zeichenRoh[x] = variable[x];
			}
			
			for (int y = (variable.length); y < zeichenRoh.length; y++) {
				random = rand.nextInt(intToChar.size());				
				zeichenRoh[y] = intToChar.get(random);
			}
			
			char charCheck = checkCharacterCalculation(prefix, zeichenRoh);
			char[] ausgabe = new char[prefix.length +zeichenRoh.length +1]; // die 1 ist für den charCheck
			System.arraycopy(prefix, 0, ausgabe, 0, prefix.length);
			System.arraycopy(zeichenRoh, 0, ausgabe, prefix.length, zeichenRoh.length);
			ausgabe[prefix.length+zeichenRoh.length] = charCheck;
			veci.add(ausgabe);
		}
		return veci;
	}
	
	/**
	 * Erstellt aus einer albumid ein kompletten grid. Ein check character, der albumid nach
	 * DIN 6346 norm prueft und ein pattern, welches anzeigt, dass der grid aus einer albumid
	 * gemacht wurde, sind auch enthalen.
	 * @param prefix besteht aus "Identifier Scheme element" und "Issuer Code element"
	 * @param variable wird am anfang von "Release Number element" gesetzt (als Pattern)
	 * @param albumID Grundlage des neuen GRIDs
	 * @return GRID in form eines char-arrays
	 * @throws Exception, falls checkCharacterCalculation scheitert
	 */
	public static char[] albumIDToGRID(char[] prefix, char[] variable, long albumID) throws Exception {
		char[] releaseNumberElement = new char[10];
		long albumIDKurz = albumID/1000;
		String neueBasis = Long.toString(albumIDKurz, 36).toUpperCase();
		char checkISO = checkAlbumidISO(albumID, variable);
		if (neueBasis.length()<releaseNumberElement.length) {
			int unterschied = releaseNumberElement.length-neueBasis.length()-1;
			for (int i=0; i<(releaseNumberElement.length-1); i++) {
				if (i<variable.length) {
					releaseNumberElement[i] = variable[i];
				}else if (i<unterschied) {
					releaseNumberElement[i] = '0';
				} else {
				releaseNumberElement[i] = neueBasis.charAt(i-unterschied);
				}
			}
		}
		releaseNumberElement[releaseNumberElement.length-1] = checkISO;
		char[] GRID = new char[prefix.length +releaseNumberElement.length +1];
		char checkCharacter = checkCharacterCalculation(prefix, releaseNumberElement);
		System.arraycopy(prefix, 0, GRID, 0, prefix.length);
		System.arraycopy(releaseNumberElement, 0, GRID, prefix.length, releaseNumberElement.length);
		GRID[GRID.length-1] = checkCharacter;
		return GRID;
	}
	
	/**
	 * Erstellt aus einen grid eine Liste von möglichen albumids. 
	 * Vorher wird geprueft, ob der grid aus einer albumid entstanden sein könnte. 
	 * @param GRID Grundlage des Vektors mit den möglichen albumids. 
	 * @param variable pattern, um zu erkennen, dass der grid aus einer albumid entstanden sein koennte 
	 * 			laenge: 2 zeichen
	 * @return vector mit allen möglichen albumids, die zum grid passen
	 */
	public static Vector<Long> GRIDToAlbumID (char[] GRID, char[] variable) {
		if ( variable.length==2 && GRID[7]==variable[0] && GRID[8]==variable[1] ) {
			String uglyReleaseNumberElement = "";
			Vector<Long> albumVector = new Vector<Long>();
			for (int i=9; i>=3; i--) {
				uglyReleaseNumberElement +=  GRID[GRID.length-i]; // nicht schoen, aber funktional
			}
			long albumID = Long.parseLong(uglyReleaseNumberElement, 36);
			
			albumID = albumID*1000;
			long control = albumID;
			for ( int l=0; l<1000; l++){
				char iso = checkAlbumidISO(control, variable);
				if (iso == GRID[GRID.length-2]) {
					albumVector.add(control);
	//				System.out.println(">>>" +control +" at pos " +m);
				}
				control++;
			}
			if ( albumVector.size()>0 ) {
				return albumVector;
			} else {
				throw new Error("no possible GRIDs"); //TODO:fehlernachricht ergänzen
			}
		} else {
			throw new Error("pattern-check failed"); //TODO:fehlernachricht ergänzen
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		/**
		 * test albumIDToGRID und GRIDToAlbumId
		 */
//		char[] prefix = new char[]{
//				'A','1','0','3','8','9','P'
//		};
//		char[] variable = new char[]{
//			'F','T'
//		};
//		long albumID = 1358781035676L;
//		System.out.println(albumID);
//		System.out.println("===");
//		char[] gridTest = albumIDToGRID(prefix, variable, albumID);
//		for (int a=0; a<gridTest.length; a++) {
//			System.out.print(gridTest[a]);
//		}
//		System.out.println("\n" +convertGRIDToString(gridTest));
//		System.out.println("===");
//		Vector<Long> testGRIDToAlbumID = GRIDToAlbumID(gridTest, variable);
//		for (int b=0; b<testGRIDToAlbumID.size(); b++){
//			System.out.println(testGRIDToAlbumID.get(b));
//		}
		
//==============================================================================================================	
		
		/**
		 * test generateRandomGRIDs
		 */
//		char[] prefix = new char[]{
//				'A','1','0','3','8','9','P'
//		};
//		char[] variable = new char[]{
//				'A','B','C',
//		};
//		int anzahl = 10;
//		Vector<char[]> probe = generateRandomGRIDs(prefix, variable, anzahl);
//		for ( int a = 0; a < probe.size(); a++) {
//			char[] tmp = probe.get(a);
//			for(int b = 0; b < tmp.length; b++){
//				System.out.print(tmp[b]);
//			}
//			System.out.print("\t");
//			System.out.println(convertGRIDToString(probe.get(a)));
//		}
	}
}
