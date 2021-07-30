package com.guymcarthur.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class CachingWordList extends WordList {
  private Hashtable cache;
  
    public CachingWordList(BufferedReader in) throws IOException {
      super(in);
      cache=new Hashtable(500);
    }
  /**
   * @param word Fragment to lookup
   * @return 1 if word is in list, -1 if word(s) in list start with word,
   * 0 if no words start with word
   */
  public int has(String word) {  
    if(cache.containsKey(word)) return ((Integer)cache.get(word)).intValue();
    Integer value=new Integer(super.has(word));
    cache.put(word, value);
    return value.intValue();
  }
}
