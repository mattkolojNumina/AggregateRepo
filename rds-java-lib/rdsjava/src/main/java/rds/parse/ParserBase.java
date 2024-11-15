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
*   @see ParseStartEnd
*   @see ParseStartLength
*   @see ParseEndOnly
*
*   @version 1.0
*   @author Mark Olson 
*/  


package rds.parse ;


import java.nio.* ;
import java.nio.charset.* ;
/** 
*  This is the abstract base class for the parsers. Individual parsers 
*  are contracted to supply the method <code>Parse(char c)</code>. The 
*  parsers which take <code>String</code> or <code>StringBuffer</code>
*  repeatedly call the supplied character parse method.
*/

public abstract class ParserBase implements Parser {
    private String lastResult ; /** where last result is cached */
    protected boolean isStarted ; /** flag to tell if we are processing */
    protected boolean isDone ; /** is a valid string complete? */

   /**
    * Parses a single character.  When implementing a concrete parser,
    * only the single-character parser needs to be supplied.  This method
    * should return {@code true} when a string is completed, {@code false}
    * otherwise.  The {@code isStarted} and {@code isDone} flags are set
    * during processing.
    * 
    * @param   c  the next character to be input. 
    * 
    * @see ParserBase#append
    * @see ParserBase#startString
    * @see ParserBase#saveString
    * @see ParseStartEnd#Parse
    */
    public abstract boolean parse(byte c) ;

    protected ByteBuffer buildBuff ;
    private CharBuffer convertBuff ;
    private static Charset charSet  = Charset.forName("US-ASCII") ;
    private static CharsetDecoder decoder = charSet.newDecoder() ;
    protected boolean[] ignore = new boolean[256] ;
    /** 
     * setIngore allows us to skip characters if required.
     * @param b --- the bytes to add to our skip list.
     * 
     */
    public void setIgnore(byte b) {
        ignore[(int) b] = true ;
    }
    /**
     *  @param input  a string buffer input. The single character parse is called on
     *  each character of the string. The result returns true, then the 
     *  <code>StringBuffer</code> is truncated to the next characters to be read.
     * 
     *  returns <code>true</code> when a successful message is parsed.
     */
    public boolean parse(ByteBuffer input) {
    int b ;
	input.flip() ;
	for (int i=0,len = input.remaining(); i < len  ; i++)  {
		b = (int)input.get() ;
		//System.out.println("byte " + b);
		if (b<0) continue ;
		if (ignore[b]) continue ;
	    if (parse( (byte)b )) {
		input.compact() ;
		return true ;
	    }
	}
	input.clear() ;
	return false ;
    }
    /** constructor no parameters */
    ParserBase() {
	lastResult = "" ;
	isStarted = false ;
	isDone = false ;
	buildBuff = ByteBuffer.allocate(1024) ;
	convertBuff = CharBuffer.allocate(1024) ;
	for (int i =0 ; i < 256 ; i++ ) ignore[i] = false ;
    } ;

    /** retrieve the last string result */
    public String getResult() { return lastResult ; } 
    public CharBuffer getCharBufferResult() { 
	CharBuffer result = convertBuff.asReadOnlyBuffer() ;
	return result ;
    }
    /** are we processing a string now? */
    public boolean getStarted() { return isStarted ; }
    /** has a string completed */
    public boolean getDone() { return isDone ; } 
    /** force a reset to start processing next */
    public void reset() { isDone = false ; }
    /**
     *  <code> startString()</code> and <code>saveString()</code> are called by
     *  client classes to assist in handling the strings.
     */
    /** startString(), called by parsers when starting on a string */
    protected void startString() {
	isStarted = true ;
	isDone = false ;
	// System.out.println("start") ;
        buildBuff.clear() ;
    }
    /** called by parsers when complete, results are saved. */
    protected void saveString() {
	//	System.out.println("done") ;
	buildBuff.flip() ;

	convertBuff.clear() ;
	decoder.decode(buildBuff,convertBuff,true) ;
	convertBuff.flip() ;
	lastResult = convertBuff.toString() ;
	isDone = true ;
	isStarted = false ;
    }
    /** called by concrete parser implementations to append a character to the
	string being built */
    protected void append(byte c) { 
	buildBuff.put(c) ; 
	//	System.out.println("append " + (char)c) ;
    } 
} ;
