/**  
*   Parse  -- a string parsing utilities. These commonly would be put on a 
*   socket or any data input serially. These parsers are lightweight.
*
*   Standard usage of the parser, is after instantiating it, to pass your data
*   into one of the three Parse methods. They return false until a valid string
*   is matched.
*  
*   Three non-abstract classes <code>ParseStartEnd</code>, 
*   <code>ParseStartLength</code>, and <code>ParseEndOnly</code>. These are
*   parsers which depend on a start and end character, just and end character,
*   and a start character and a length (no end character). In each parser, a 
*   constructor and method <code>Parse(char c)</code> is required to implement.
*   
*   The client methods, are expected to usually not call <code>Parse(char c)</code>
*   but the one taking a <code>String</code> or <code>StringBuffer</code> instead.
*   Use <code>StringBuffer</code> where possible, as it is likely to be more 
*   efficient.
*
*   @version 1.0
*   @author Mark Olson 
*/  

package rds.parse ;


public class ParseStartLength extends ParserBase implements Parser  {
	/** constant for startChar */
	private final char startChar ;
	/** constant for our string length */
	private final int Length ;
	public ParseStartLength(char start,int stringLength) {
		super() ;
		startChar = start ;
		Length = stringLength ;
	}
	/** getStartChar()  retrieve start Char */
	public char getStartChar() { return startChar ; } 
	/** getLength() retrieve length */
	public int getLength() { return Length ; } 

	/** our parser. Start, count characters, until end is reached. Don't 
	* restart until new start character in */
	public boolean parse(byte c) {
		if (ignore[c]) return false ;
		if (c == startChar && !getStarted())	{
		    startString() ;
		    isDone = false ;
		}
		if (getStarted()) {
		    append(c) ;
		    if (buildBuff.limit() == Length ) {
			saveString() ;
				return true ;
			}
		}
		return false ;
	}
	public String toString() { 
	    return "ParseStartLength: start: " + 
		startChar + " Length: " + Length ; 
	}

};
