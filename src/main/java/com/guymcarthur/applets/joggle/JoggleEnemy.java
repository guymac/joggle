package com.guymcarthur.applets.joggle;

import com.guymcarthur.Debuggable;
import com.guymcarthur.util.CachingWordList;

/**
 * Implements a computer player.
 * Intended to be run in a background thread.
 */
public class JoggleEnemy extends JoggleGame implements Runnable, Debuggable {
  protected boolean debug, active; //stop is deprecated
  protected int skill=100;
  
  public static final int SKILL_NOVICE = 0;
  public static final int SKILL_INTERMEDIATE = 9;
  public static final int SKILL_ADVANCED = 19;
  public static final int SKILL_MASTER = 99;
    
  public JoggleEnemy(JoggleBoard board, CachingWordList list) {
    super(board, list);
  }

  public void setSkill(int skill) {
    this.skill=skill;
  }
  
  public String getSkill() {
  	switch(skill) {
		case(SKILL_NOVICE):
			return "Novice";
		case(SKILL_INTERMEDIATE):
			return "Intermediate";
		case(SKILL_ADVANCED):
			return "Advanced";
		case(SKILL_MASTER):
			return "Master";
		default:
			return null;
	}
  }

  public void setActive(boolean active) {
    this.active=active;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug=debug;
  }

  public void run() {
    if(debug) System.out.println("Computer has begun play.");

    for(int i=0;i<board.rows;i++)
      for(int j=0;j<board.cols;j++) 
        if(active) recurse(new boolean[board.rows][board.cols], true, "", i, j);
    active=false;

    if(debug) System.out.println("Computer has finished play.");
  }

  // this is where the action is!
  public boolean recurse(boolean[][] state, boolean more, String strb, int row, int col) {
    if(!active) return false;
    try {
      Thread.currentThread().sleep(10);
    } catch(InterruptedException ex) { 
      if(debug) System.out.println("Computer was interrupted.");
    }

    if(debug) System.out.print("("+row+","+col+") ");
    if(state[row][col]) {
      if(debug) System.out.println("Cell has already been selected!");
      return false;
    }
    
    String letter=board.getValue(row,col);
    String word=strb+letter;
    
    if(debug) System.out.println("Word is now "+word+".");

    if(skill<SKILL_INTERMEDIATE && word.length()>4) return false;
    else if(skill<SKILL_ADVANCED && word.length()>5) return false;
    
    if(word.length()>1) switch(list.has(word)) {
    case(0):
      if(debug) System.out.println("Nothing starts with "+word+".");
      return false;
    case(1):
      if(word.length()<3) break;
      if(debug) System.out.println("Scoring "+word+".");
      if(!added.containsKey(word)) score(word, false); // don't check if it's on the board or in hash
      if(skill<SKILL_MASTER) {
        strb="";
        return false;
        }
    } 

    state[row][col]=true;

    // corner tiles
    if(row==0 && col==0) {
      recurse(state, more, word, row+1, col);
      recurse(state, more, word, row, col+1);
      recurse(state, more, word, row+1, col+1);
    } else if(row==0 && col==board.cols-1) {
      recurse(state, more, word, row, col-1);
      recurse(state, more, word, row+1, col-1);
      recurse(state, more, word, row+1, col);
    } else if(row==board.rows-1 && col==0) {
      recurse(state, more, word, row-1, col);
      recurse(state, more, word, row-1, col+1);
      recurse(state, more, word, row, col+1);
    }  else if(row==board.rows-1 && col==board.cols-1) {
      recurse(state, more, word, row, col-1);
      recurse(state, more, word, row-1, col-1);
      recurse(state, more, word, row-1, col);
    } // side tiles
    else if(row==0) { 
      recurse(state, more, word, row, col-1);
      recurse(state, more, word, row, col+1);
      recurse(state, more, word, row+1, col-1);
      recurse(state, more, word, row+1, col);
      recurse(state, more, word, row+1, col+1);
    } else if(row==board.rows-1) {
      recurse(state, more, word, row, col-1);
      recurse(state, more, word, row, col+1);
      recurse(state, more, word, row-1, col-1);
      recurse(state, more, word, row-1, col);
      recurse(state, more, word, row-1, col+1);
    } else if(col==0) {
      recurse(state, more, word, row-1, col);
      recurse(state, more, word, row+1, col);
      recurse(state, more, word, row-1, col+1);
      recurse(state, more, word, row, col+1);
      recurse(state, more, word, row+1, col+1);
    } else if(col==board.cols-1) {
      recurse(state, more, word, row-1, col);
      recurse(state, more, word, row+1, col);
      recurse(state, more, word, row-1, col-1);
      recurse(state, more, word, row, col-1);
      recurse(state, more, word, row+1, col-1);
    } // center tiles
    else {
      recurse(state, more, word, row-1, col-1); // 0
      recurse(state, more, word, row, col-1); // 1
      recurse(state, more, word, row+1, col-1); // 2
      recurse(state, more, word, row+1, col); // 3
      recurse(state, more, word, row+1, col+1); // 4
      recurse(state, more, word, row, col+1); // 5
      recurse(state, more, word, row-1, col+1); // 6
      recurse(state, more, word, row-1, col); // 7
    }
    
    state[row][col]=false;
    return false;

  }
}
