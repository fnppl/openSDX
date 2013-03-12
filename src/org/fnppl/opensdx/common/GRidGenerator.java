package org.fnppl.opensdx.common;

import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Vector;


public class GRidGenerator {

//	static int maxIndex;
//	static String anfang = "A1-0389P";
//	static String anfangRoh = "A10389P";
	public static String range = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static Hashtable<Character, Integer> charToInt = new Hashtable<Character, Integer>(); 
	public static Hashtable<Integer, Character> intToChar = new Hashtable<Integer, Character>();
	static {
		for( int c=0; c<range.length(); c++) {
			char l = range.charAt(c);
			charToInt.put(l, c);
			intToChar.put(c, l);
		}		
	}
	
	/**
	 * ermittelt das letzte zeichen bzw. die prüsumme des GRid's
	 * @param grid
	 * @return
	 */
	public static char checkCharacterCalculation(char[] prefix, char[] variable_part_of_grid) throws Exception {
		if(prefix.length != 7) {
			throw new Exception("Grid-prefix != 7");
		}
		if(variable_part_of_grid.length != 10) {
			throw new Exception("Grid-base != 10");
		}
		
		char[] a = new char[18];
		for(int i=0;i<prefix.length;i++) {
			a[17-i] = prefix[i];
		}
		for(int i=0;i<variable_part_of_grid.length;i++) {
			a[17-prefix.length-i] = variable_part_of_grid[i];
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
	 * wandelt ein GRid in einen String inkl. Bindestrichen um
	 * @param grid zu aendernder GRid
	 * @return konvertierten String
	 */
	public static String convertGridToString (char[] grid) {
		String converted = "";
		for ( int i = 0; i < grid.length; i++) {
			if ( i == 2 || i == 7 || i == 17) {
				converted += "-";
			}
			converted += grid[i];
		}
		return converted;
	}
	
	/**
	 * erstellt komplette GRid's
	 * @param prefix
	 * @param variable
	 * @param anzahl
	 * @return
	 * @throws Exception
	 */
	public static Vector<char[]> generateRandomGrids(char[] prefix, char[] variable, int anzahl) throws Exception {
		//OBACHT: variable kann hier länge 0 bis 9 haben => das sind die VON VORNE vorgegebenen werte
		//TODO HT 2013-02-27
		
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
	
	public static char[] albumIDToGRid(char[] prefix, long albumID) throws Exception {
		
		char[] issuerCode = new char[10];
		String neueBasis = Long.toString(albumID, 36).toUpperCase();
		if ( neueBasis.length()<=issuerCode.length ) {
			int unterschied = issuerCode.length-neueBasis.length();
			for (int i=0; i<issuerCode.length; i++) {
				if (i<unterschied) {
					issuerCode[i] = '0';
				} else {
				issuerCode[i] = neueBasis.charAt(i-unterschied);
				}
			}
		}
		char[] grid = new char[prefix.length +issuerCode.length +1];
		char checkCharacter = checkCharacterCalculation(prefix, issuerCode);
		System.arraycopy(prefix, 0, grid, 0, prefix.length);
		System.arraycopy(issuerCode, 0, grid, prefix.length, issuerCode.length);
		grid[grid.length-1] = checkCharacter;
		return grid;
	}
	
	public static long gridToAlbumID (char[] grid) {
//		char[] issuerCode = new char[10];
		String uglyIssuerCode = "";
		for (int i=11; i>=2; i--) {
//			issuerCode[11-i] = grid[grid.length-i];
			uglyIssuerCode +=  grid[grid.length-i]; // nicht schoen, aber funktional
		}
		long albumID = Long.parseLong(uglyIssuerCode, 36);
		return albumID;
	}
	
	public static void main(String[] args) throws Exception {
		
//		/**
//		 * test albumIDToGRid und gridToAlbumId
//		 */
//		char[] prefix = new char[]{
//				'A','1','0','3','8','9','P'
//		};
//		long albumID = 1358781035676L;
//		System.out.println(albumID);
//		System.out.println("===");
//		char[] testChar = albumIDToGRid(prefix, albumID);
//		for (int a=0; a<testChar.length; a++) {
//			System.out.print(testChar[a]);
//		}
//		System.out.println("\n===");
//		long testgridToAlbumID = gridToAlbumID(testChar);
//		System.out.println(testgridToAlbumID);
		
//==============================================================================================================	
		
//		/**
//		 * test generateRandomGrids
//		 */
//		char[] prefix = new char[]{
//				'A','1','0','3','8','9','P'
//		};
//		char[] variable = new char[]{
//				'A','B','C',
//		};
//		int anzahl = 10;
//		Vector<char[]> probe = generateRandomGrids(prefix, variable, anzahl);
//		for ( int a = 0; a < probe.size(); a++) {
//			char[] tmp = probe.get(a);
//			for(int b = 0; b < tmp.length; b++){
//				System.out.print(tmp[b]);
//			}
//			System.out.print("\t");
//			System.out.println(convertGridToString(probe.get(a)));
//		}
	}
}
