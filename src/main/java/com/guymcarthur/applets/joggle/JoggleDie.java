package com.guymcarthur.applets.joggle;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

public class JoggleDie extends Object implements Cloneable {
  private String[] faces;
  private int face=0;

  public JoggleDie(String[] faces) throws Exception {
    if(faces.length<6) throw(new Exception("The die must have at least six faces."));
    this.faces=faces;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public String getValue() {
    return faces[face];
  }

  public String roll() {
    return faces[face=(int)(Math.random()*faces.length)];
  }

  public String toString() {
    StringBuffer strb=new StringBuffer(faces[0]);
    for(int i=1;i<faces.length;i++) strb.append(", ").append(faces[i]);
    return strb.toString();
  }

  public final static JoggleDie[] loadDice(BufferedReader in) throws IOException {
    Vector dice=new Vector();
    // okay, basically I'm writing my own split function
    String line;
    while((line=in.readLine())!=null) {
      if(line.startsWith("#")) continue;
      StringTokenizer st=new StringTokenizer(line.trim(), ",");
      String[] tokens=new String[st.countTokens()];
      for(int i=0;i<tokens.length;i++) { 
	tokens[i]=st.nextToken().trim().toLowerCase();
	if(tokens[i].equals("q")) tokens[i]="qu";
      }
      try {
	dice.addElement(new JoggleDie(tokens));
      } catch(Throwable t) {
	t.printStackTrace(System.err);
      }
    }
    // toArray() and toArray(Object) are 1.2 methods so
    // this is the workaround
    JoggleDie[] jd=new JoggleDie[dice.size()];
    for(int i=0;i<dice.size();i++) jd[i]=(JoggleDie)dice.elementAt(i);
    return jd;
  }
}
