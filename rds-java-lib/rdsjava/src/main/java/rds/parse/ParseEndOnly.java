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

public class ParseEndOnly extends ParserBase implements Parser {
	private final byte endByte ; /** constant to hold endChar */

	/** constructor sets constants */
	public ParseEndOnly(byte end) {
		super() ;
		endByte = end ;
	}
	/** retrieve the endChar */
	public byte getEndByte() { return endByte ; }

	/** concrete parser. This one is "always started" */ 
	public boolean parse(byte b) {
	  if (ignore[b]) return false ;
	  if (b == endByte) {
	      saveString() ;
	      return true ;
	  }
	  if (!getStarted()) {  startString() ; }
	  append(b) ;
	  return false ;
	}
	public String toString() { 
	    return "ParseEndOnly: endChar " + endByte ;
	}

};
