/*
 * Created on Mar 29, 2005 by Mark Olson
 *
 */
package rds.parse;

import java.nio.ByteBuffer;

/**
 * @author Mark Olson --- Numina Systems Corporation
 *
 */
public interface Parser {
    public void setIgnore(byte b)  ;
    public boolean parse(ByteBuffer input) ;
    public boolean parse(byte c) ;
    public String getResult() ; 

}
