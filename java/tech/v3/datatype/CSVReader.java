package tech.v3.datatype;


import java.io.EOFException;
import java.util.ArrayList;


public final class CSVReader {
  final CharReader reader;
  final char quot;
  final char sep;
  public static final int EOF=-1;
  public static final int EOL=-2;
  public static final int SEP=1;
  public static final int QUOT=2;

  public CSVReader(CharReader rdr, char _quot, char _sep) {
    reader = rdr;
    quot = _quot;
    sep = _sep;
  }

  final void csvReadQuote(CharBuffer sb) throws EOFException {
    char[] buffer = reader.buffer();
    while(buffer != null) {
      int startpos = reader.position();
      int len = buffer.length;
      for(int pos = startpos; pos < len; ++pos) {
	final char curChar = buffer[pos];
	if (curChar == quot) {
	  sb.append(buffer,startpos,pos);
	  if (reader.readFrom(pos+1) == quot) {
	    sb.append(quot);
	    buffer = reader.buffer();
	    len = buffer.length;
	    startpos = reader.position();
	    //account for loop increment
	    pos = startpos - 1;
	  } else {
	    reader.unread();
	    return;
	  }
	}
      }
      sb.append(buffer,startpos,len);
      buffer = reader.nextBuffer();
    }
    throw new EOFException("EOF encountered within quote");
  }
  //Read a row from a CSV file.
  final int csvRead(CharBuffer sb) throws EOFException {
    char[] buffer = reader.buffer();
    final char localSep = sep;
    final char localQuot = quot;
    while(buffer != null) {
      final int startpos = reader.position();
      final int len = buffer.length;
      for(int pos = startpos; pos < len; ++pos) {
	final char curChar = buffer[pos];
	if (curChar == localQuot) {
	  sb.append(buffer, startpos, pos);
	  reader.position(pos + 1);
	  return QUOT;
	} else if (curChar == localSep) {
	  sb.append(buffer, startpos, pos);
	  reader.position(pos + 1);
	  return SEP;
	} else if (curChar == '\n') {
	  sb.append(buffer, startpos, pos);
	  reader.position(pos + 1);
	  return EOL;
	} else if (curChar == '\r') {
	  sb.append(buffer, startpos, pos);
	  if (reader.readFrom(pos+1) != '\n') {
	    reader.unread();
	  }
	  return EOL;
	}
      }
      sb.append(buffer, startpos, len);
      buffer = reader.nextBuffer();
    }
    return EOF;
  }

  public static final class RowReader
  {
    final CSVReader rdr;
    final CharBuffer sb;
    ArrayList<String> row;
    UnaryPredicate pred;

    public RowReader(CharReader _r, CharBuffer cb, UnaryPredicate _pred, char quot, char sep) {
      rdr = new CSVReader(_r, quot, sep);
      sb = cb;
      pred = _pred;
    }
    public void setPredicate(UnaryPredicate p) { pred = p; }
    public static final boolean emptyStr(String s) {
      return s == null || s.length() == 0;
    }
    public static final boolean emptyRow(ArrayList<String> row) {
      int sz = row.size();
      return sz == 0 || (sz == 1 && emptyStr(row.get(0)));
    }
    public final ArrayList currentRow() { return row; }
    public final ArrayList nextRow() throws EOFException {
      //It turns out it is fast to just create a new row object
      //rather than clone an existing one.
      final ArrayList<String> curRow = new ArrayList<String>(8);
      sb.clear();
      int tag;
      int colidx = 0;
      final UnaryPredicate p = pred;
      do {
	tag = rdr.csvRead(sb);
	if(tag != QUOT) {
	  if (p.unaryLong(colidx))
	    curRow.add(sb.toString());
	  ++colidx;
	  sb.clear();
	} else {
	  rdr.csvReadQuote(sb);
	}
      } while(tag > 0);
      if (!(tag == EOF && emptyRow(curRow))) {
	row = curRow;
	return curRow;
      }
      else
	return null;
    }
  }
}
