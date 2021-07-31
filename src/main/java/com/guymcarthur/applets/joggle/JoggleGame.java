package com.guymcarthur.applets.joggle;

/** 
 * Represents a players state.
 * Implements rules of the game.
 */
import java.text.ChoiceFormat;
import java.util.*;
import com.guymcarthur.util.CachingWordList;

public class JoggleGame extends Observable {
  protected JoggleBoard board;
  protected int score, total=0;
  protected int wordcount=0;
  protected Hashtable <String, Integer> added;
  protected CachingWordList list;
  protected static final ChoiceFormat fmt=new ChoiceFormat(" 1# point.|1< points.");  

  public class GameMessage {
    public int pts=0;
    String msg, word=null;

    public GameMessage(String msg) {
      this.msg=msg;
    }

    public GameMessage(String msg, String word, int pts) {
      this.msg=msg;
      this.word=word;
      this.pts=pts;
    }
  }

  public JoggleGame(final JoggleBoard board, final CachingWordList list) {
    this.board=board;
    this.list=list;
  }

  /**
   * Start a new game.
   */
  public void init() {
    score=0;
    added=new Hashtable <> ();
  }

  public synchronized void total() {
    total+=score;
    wordcount+=added.size();
    setChanged();
    notifyObservers(new GameMessage("Matching words eliminated."));
  }

  public int score(String word) {
    return score(word, true);
  }

  public int score(String word, boolean check) {
    setChanged(); // somethings gonna happen, I can feel it!

    if(word.length()<3) {
      notifyObservers(new GameMessage("Word must be at least three letters long.", word, 0));
      return 0;
    }
    if(check) {
      if(added.containsKey(word)) {
	notifyObservers(new GameMessage("\""+word+"\" already used.", word, 0));      
	return 0;
      }
      if(!board.has(word)) {
	notifyObservers(new GameMessage("\""+word+"\" not found on the board.", word, 0));
	return 0;
      }
      if(list.has(word)!=1) {
	notifyObservers(new GameMessage("\""+word+"\" not found in dictionary.", word, 0));
	return 0;
      }
    }

    int pts=getPoints(word);
    //total += pts;
    synchronized(this) { score += pts; }
    added.put(word, Integer.valueOf(pts));
    notifyObservers(new GameMessage("\""+word+"\" scored "+pts+" "+fmt.format(pts), word, pts));
    
    return pts;
  }

  public static int getPoints(final String word) {
    if(word.length()<3) return 0;

    switch(word.length()) {
    case(3):
      return 1;
    case(4):
      return 1;
    case(5):
      return 2;
    case(6):
      return 3;
    case(7):
      return 5;
    default: // 8 or more
      return 11;
    }
  }
}
