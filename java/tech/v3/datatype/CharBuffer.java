package tech.v3.datatype;


public final class CharBuffer
{
  public final boolean trimLeading;
  public final boolean trimTrailing;
  public final boolean nilEmpty;
  char[] buffer;
  int len;

  public CharBuffer(boolean _trimLeading, boolean _trimTrailing, boolean _nilEmpty) {
    trimLeading = _trimLeading;
    trimTrailing = _trimTrailing;
    nilEmpty = _nilEmpty;
    buffer = new char[32];
    len = 0;
  }
  public CharBuffer() {
    this(false, false, false);
  }
  public final char[] ensureCapacity(int newlen) {
    if (newlen >= buffer.length) {
      char[] newbuffer = new char[newlen * 2];
      System.arraycopy(buffer, 0, newbuffer, 0, len);
      buffer = newbuffer;
    }
    return buffer;
  }
  public final void append(char val) {
    char[] buf = buffer;
    //common case first
    if (len < buf.length) {
      buf[len] = val;
      ++len;
      return;
    }
    buf = ensureCapacity(len+1);
    buf[len] = val;
    ++len;
  }
  public final void append(char[] data, int startoff, int endoff) {
    if(startoff < endoff) {
      int buflen = len;
      final int nchars = endoff - startoff;
      final int newlen = buflen + nchars;
      final char[] buf = ensureCapacity(newlen);

      if (nchars < 5) {
	for(; startoff < endoff; ++startoff, ++buflen)
	  buf[buflen] = data[startoff];
      } else {
	System.arraycopy(data, startoff, buf, buflen, nchars);
      }
      len = newlen;
    }
  }
  public final void clear() { len = 0; }
  public char[] buffer() { return buffer; }
  public final int length() { return len; }
  public final int capacity() { return buffer.length; }
  public final String toString() {
    int strlen = len;
    int startoff = 0;
    if(trimLeading && strlen != 0) {
      for (; startoff < len && Character.isWhitespace(buffer[startoff]); ++startoff);
      strlen = strlen - startoff;
    }
    if(trimTrailing && strlen != 0) {
      int idx = len - 1;
      for (; idx >= startoff && Character.isWhitespace(buffer[idx]); --idx);
      strlen = idx + 1 - startoff;
    }
    if(strlen == 0) {
      if(nilEmpty) {
	return null;
      }
      return "";
    } else {
      return new String(buffer, startoff, strlen);
    }
  }
}
