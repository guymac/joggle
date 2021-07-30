package com.guymcarthur.applets.joggle;

import java.util.*;
import com.guymcarthur.Debuggable;
import com.guymcarthur.util.CachingWordList;

/**
 * Keeps track of player state. Intended to be used through action or mouse event handlers.
 */
public class JogglePlayer extends JoggleGame implements Debuggable {
  protected boolean[][] state;
  protected StringBuffer word;
  protected Vector selected;
  private boolean debug=false;

  public JogglePlayer(JoggleBoard board, CachingWordList list) {
    super(board, list);
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug=debug;
  }

  /**
   * Start a new game.
   */
  public void init() {
    super.init();
    reset();
  }

 /** 
   * Start a new word.
   */
  public void reset() {
    state=new boolean[board.rows][board.cols];
    word=new StringBuffer();
    selected=new Vector();
  }

  /** 
   * Supports the mouse-click interface.
   */
  public boolean select(int row, int col) {
    if(state[row][col]) {
      if(debug) System.out.println(word.toString()+" has been terminated.");
      /* tile already selected,
	 so this is a word-terminating action
	 
	 if the word is long enough and it's in the list
	 add the point value to the total and score
      */
      if(word.length()>2) {
	if(debug) System.out.println("Scoring word.");
	score(word.toString());
      }
      if(debug) System.out.println("Resetting.");
      reset();
    } else {
      /* tile has not been selected
	 if it's selectable, select it
	 else they're starting over so reset
      */

      if(selected.size()>0) {
	if(debug) System.out.println("Continuing word "+word.toString());
	// (n+1)th selection
	int[] last=(int[])selected.elementAt(selected.size()-1);
	// what's the difference?
	// is it a neighbor? if so, selectable
	int dx2=(last[0]-row)*(last[0]-row);
	int dy2=(last[1]-col)*(last[1]-col);
	if(debug) System.out.println("dx = "+dx2+", dy = "+dy2+".");
	// reset if not a selectable
	if((dx2+dy2)>2) {
	        if(debug) System.out.println("Starting a new word.");
		reset();
	}
      }
      state[row][col]=true;
      selected.addElement(new int[] { row, col});
      word.append(board.getValue(row, col));
      if(debug) System.out.println("Word is now "+word.toString());
    }

    return state[row][col];
  }


}
