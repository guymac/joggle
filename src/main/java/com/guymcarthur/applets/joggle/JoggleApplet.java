/*************************************************************************
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ***************************************************************************/
package com.guymcarthur.applets.joggle;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import com.guymcarthur.Debuggable;
import com.guymcarthur.widget.GridBagManager;
import com.guymcarthur.util.CachingWordList;

/**
 * $Id$
 * 
 * TO DO: <ul>
 * <li>loading in thread
 * <li>paint while loading
 * <li>parameter for game time
 * </ul>
 */
public class JoggleApplet extends Applet implements Debuggable {
    /* Joggle classes */
    JoggleBoard board;
    JoggleCanvas canvas;
    JogglePlayer player;
    JoggleEnemy computer;

    /* AWT components */
    Button StartButton, PauseButton;
    Choice SkillChoice;
    Label PlayerScore, ComputerScore, PlayerTotal, ComputerTotal;
    TextField PlayerEntry;
    TextArea PlayerMessages, PlayerWords, ComputerWords;
    Scrollbar Timer;
    Label EntryLabel, ScrollLabel, Round;

    /* instance variables */
    Hashtable tiles=new Hashtable(26);
    private boolean running, debug;
    private int rounds=0;
    private static final DecimalFormat MBytes=new DecimalFormat("#,###.00 MBytes");

    public String getAppletInfo() {
        return "This is Joggle version 0.99 beta by Guy McArthur (guym@guymcarthur.com).";
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug=debug;
    }

    /**
     * ScoreKeeper 1
     */
    public class PlayerObserver implements Observer {
        /**
         * Catch the game message from the player
         */
        public void update(Observable o, Object arg) {
            JoggleGame.GameMessage gm=(JoggleGame.GameMessage)arg;
            PlayerScore.setText(""+player.score);
            PlayerTotal.setText(""+player.total);
            PlayerMessages.append("\n"+gm.msg);

            // there's some silly bug and it wasn't "tailing" this textarea like
            // the other textareas, even though they're created, initialized and
            // appended to in exactly the same manner
            try {
                PlayerMessages.setCaretPosition(PlayerMessages.getText().length());
            } catch(Throwable t) { 
                // maybe IllegalArgumentException but really IllegalComponentStateException
            }
            if(gm.pts>0) PlayerWords.append(gm.word+" (+"+gm.pts+")\n");
        }
    }

    /**
     * ScoreKeeper 2
     */
    public class ComputerObserver implements Observer {
        /**
         * Catch the game message from the computer
         */
        public void update(Observable o, Object arg) {
            JoggleGame.GameMessage gm=(JoggleGame.GameMessage)arg;
            ComputerScore.setText(""+computer.score);
            ComputerTotal.setText(""+computer.total);
            if(gm.pts>0) ComputerWords.append(gm.word+" (+"+gm.pts+")\n");
        }
    }


    /**
     * "HourGlass"
     */
    public class TimerThread extends Thread {  
        private final ChoiceFormat fmt=new ChoiceFormat(" 1# second remaining|1< seconds remaining.");  
        boolean active; //stop is deprecated

        public void setActive(boolean active) {
            synchronized(this) { this.active=active; }
        }

        public boolean isActive() {
            return active;
        }

        public void run() {
            while(active) try {
                for(int time=Timer.getMinimum();time<Timer.getMaximum();time++) {
                    if(!active) break;
                    Timer.setValue(time);
                    int rem=Timer.getMaximum()-time;
                    ScrollLabel.setText(""+rem+fmt.format(rem));
                    sleep(1000);
                }
                setRunning(false);
            } catch(InterruptedException ex) {
                message("Timer was interrupted.");
            }
            active=false;
        }
    }


    TimerThread timer;
    Thread thread;

    public void init() {
        /* Load game items from archive */
        message(getAppletInfo());
        // load tile images
        try {
            message("Loading images.");
            MediaTracker mt = new MediaTracker(this);

            for(int i=0;i<board.letters.length;i++) {
                Image img;
                String file="/images/"+board.letters[i]+".jpg";
                if(debug) message("Loading "+file);
                mt.addImage(img=loadImage(file), i);
                tiles.put(board.letters[i], img);
            }
            mt.waitForAll();
            message("Adding Board.");
        } catch(IOException ex) {
            message("IOException loading images.", ex);
        } catch(InterruptedException ex) {
            message("Interrupted loading images.", ex);
        } 

        // load dice
        try {
            message("Loading dice");
            // the weird thing about getResourceAsStream is it can return null behind 
            // certain filtering proxies with certain browsers
            InputStream in = this.getClass().getResourceAsStream("/dice.txt");
            if(in==null) throw(new IOException("Could not find resource dice.txt"));
            BufferedReader reader=new BufferedReader(new InputStreamReader(in));
            JoggleDie[] dice=JoggleDie.loadDice(reader);
            reader.close();
            in.close();

            message("Loaded "+dice.length+" dice.");

            // set up the board
            int rows, cols;
            try {
                rows=Integer.parseInt(getParameter("rows"));
                cols=Integer.parseInt(getParameter("cols"));
            } catch(Throwable t) {
                rows=4;
                cols=4;
            }

            JoggleDie[][] play=new JoggleDie[rows][cols];
            for(int i=0, idx=0, off=0;i<rows;i++) for(int j=0;j<cols;j++) {
                if(idx>=dice.length) ++off; // go back if we need more dice
                play[i][j]=dice[idx-off];
                if(debug) message("Playing with die "+play[i][j].toString());
                ++idx;
            }

            message("Board size is "+play.length+" x "+play[0].length+".");
            board=new JoggleBoard(play);
            if(debug) board.setDebug(true);      
        } catch(IOException ex) {
            message("Could not load dice.", ex);
        }
        // load word list
        try {
            message("Loading word list.");
            InputStream in = this.getClass().getResourceAsStream("/words.txt");
            if(in==null) throw(new IOException("Could not find resource words.txt"));
            BufferedReader reader=new BufferedReader(new InputStreamReader(in));
            CachingWordList list=new CachingWordList(reader);
            reader.close();
            in.close();

            message("Loaded "+list.size()+" words.");

            player=new JogglePlayer(board, list);
            computer=new JoggleEnemy(board, list);

            if(debug) computer.setDebug(true);
            player.addObserver(new PlayerObserver());
            computer.addObserver(new ComputerObserver());
        } catch(IOException ex) {
            message("Could not load words.", ex);
        }

        /* Initialize components */
        Panel Control=new Panel(new FlowLayout(FlowLayout.LEFT));
        Panel Status=new Panel(new FlowLayout(FlowLayout.RIGHT));
        StartButton=new Button("Start");
        PauseButton=new Button("Pause");

        SkillChoice=new Choice();
        SkillChoice.add("Novice");
        SkillChoice.add("Intermediate");
        SkillChoice.add("Advanced");
        SkillChoice.add("Joggleholic");
        SkillChoice.select("Advanced");

        Round=new Label("Round "+rounds);
        Control.setBackground(Color.blue);
        Control.setForeground(Color.white);
        Status.setBackground(Color.blue);
        Status.setForeground(Color.white);
        /*
      although this fixes it on Netscape, it breaks it on older
      Mac JVMs, not sure what best solution is
         */
        StartButton.setBackground(Color.blue);
        PauseButton.setBackground(Color.blue);
        StartButton.setForeground(Color.white);
        PauseButton.setForeground(Color.white);
        SkillChoice.setBackground(Color.blue);
        SkillChoice.setForeground(Color.white);
        //Round.setForeground(Color.white);

        Control.add(StartButton);
        Control.add(PauseButton);
        Control.add(SkillChoice);
        Status.add(Round);

        PlayerScore=new Label("0", Label.RIGHT);
        ComputerScore=new Label("0", Label.RIGHT);
        PlayerTotal=new Label("0", Label.RIGHT);
        ComputerTotal=new Label("0", Label.RIGHT);

        canvas=new JoggleCanvas(board, tiles);
        if(debug) canvas.setDebug(true);

        PlayerEntry=new TextField(36);
        PlayerEntry.setBackground(Color.lightGray);
        EntryLabel=new Label("Input words above or click on tiles.");

        PlayerMessages=new TextArea("", 3, 20, TextArea.SCROLLBARS_VERTICAL_ONLY);
        PlayerWords=new TextArea("", 3, 15, TextArea.SCROLLBARS_VERTICAL_ONLY);
        ComputerWords=new TextArea("", 3, 15, TextArea.SCROLLBARS_VERTICAL_ONLY);
        PlayerMessages.setBackground(Color.lightGray);
        PlayerWords.setBackground(Color.lightGray);
        ComputerWords.setBackground(Color.lightGray);

        Timer=new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 180);
        ScrollLabel=new Label("Press \"Start\" to initiate a game.");

        /* Event Handlers */
        StartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setRunning((!running) ? true : false);
            }
        });

        PauseButton.addActionListener(new ActionListener() {
            boolean paused;

            public void actionPerformed(ActionEvent e) {
                if(!running) return;
                if(!paused) {
                    PauseButton.setLabel("Resume");
                    board.setPlayable(false);
                    try {
                        if(timer.isActive()) timer.suspend();
                        if(computer.isActive()) thread.suspend();
                    } catch(SecurityException ex) {
                    }
                    canvas.setVisible(false);
                } else {
                    canvas.setVisible(true);
                    PauseButton.setLabel("Pause");
                    board.setPlayable(true);
                    try { 
                        if(computer.isActive()) thread.resume();
                        if(timer.isActive()) timer.resume();
                    } catch(SecurityException ex) {
                    }
                }
                paused=!paused;
            }
        });

        SkillChoice.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                switch(SkillChoice.getSelectedIndex()) {
                    case(0): // novice
                        computer.setSkill(computer.SKILL_NOVICE);
                    break;
                    case(1): // advanced
                        computer.setSkill(computer.SKILL_INTERMEDIATE);
                    break;
                    case(2):
                        computer.setSkill(computer.SKILL_ADVANCED);
                    break;
                    case(3):
                        computer.setSkill(computer.SKILL_MASTER);
                    break;
                }

            }
        });

        PlayerEntry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(!board.isPlayable()) return;
                scoreWords();
            }
        });

        canvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) { 
                if(!running) {
                    setRunning(true);
                    return;
                }
                int[] cell=canvas.getCell(e.getX(), e.getY());
                if(cell[0]==-1 || cell[1]==-1) return;
                if(player.select(cell[0], cell[1])) canvas.selectCell(cell[0], cell[1], Color.red); 
                else canvas.repaint();
            }
            public void mouseEntered(MouseEvent e) { 
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) { 
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            public void mousePressed(MouseEvent e) { }
            public void mouseReleased(MouseEvent e) { }
        });

        canvas.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) { }

            public void mouseDragged(MouseEvent e) { 
                int[] cell=canvas.getCell(e.getX(), e.getY());
                if(cell[0]==-1 || cell[1]==-1) return;
                if(player.select(cell[0], cell[1])) canvas.selectCell(cell[0], cell[1], Color.red); else canvas.repaint();
            }
        });

        /* Initialize self */
        setBackground(Color.white);

        setLayout(new GridBagLayout());
        // row 0
        int row=0;

        GridBagManager.add(this, Control, row, 0, 1, 7, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTHEAST);
        GridBagManager.add(this, Status, row, 7, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.SOUTHEAST);
        // row 1
        GridBagManager.add(this, new Label("Player:"), ++row, 0, 1, 1);
        GridBagManager.add(this, new Label("Score"), row, 1, 1, 1);
        GridBagManager.add(this, new Label("Total"), row, 4, 1, 1);
        GridBagManager.add(this, PlayerScore, row, 2, 1, 2);
        GridBagManager.add(this, PlayerTotal, row, 5, 1, 2);

        // row 2
        GridBagManager.add(this, new Label("Computer:"), ++row, 0, 1, 1);
        GridBagManager.add(this, new Label("Score"), row, 1, 1, 1);
        GridBagManager.add(this, new Label("Total"), row, 4, 1, 1);
        GridBagManager.add(this, ComputerScore, row, 2, 1, 2);
        GridBagManager.add(this, ComputerTotal, row, 5, 1, 2);

        // row 3
        GridBagManager.add(this, canvas, ++row, 0, 8, 8);

        // row 11
        GridBagManager.add(this, PlayerEntry, row=11, 0, 1, 8, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        // row 12
        GridBagManager.add(this, EntryLabel, ++row, 0, 1, 8, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        // row 13
        GridBagManager.add(this, PlayerMessages, ++row, 0, 2, 8, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        // row 15
        GridBagManager.add(this, PlayerWords, row=15, 0, 2, 4, GridBagConstraints.NONE, GridBagConstraints.WEST);
        GridBagManager.add(this, ComputerWords, row, 4, 2, 4, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
        // row 17
        GridBagManager.add(this, new Label("Player's Words"), row=17, 0, 1, 4, GridBagConstraints.BOTH, GridBagConstraints.WEST);
        GridBagManager.add(this, new Label("Computer's Words"), row, 4, 1, 4, GridBagConstraints.BOTH, GridBagConstraints.WEST);

        // row 18
        GridBagManager.add(this, Timer, ++row, 0, 1, 8, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        // row 19
        GridBagManager.add(this, ScrollLabel, ++row, 0, 1, 8, GridBagConstraints.BOTH, GridBagConstraints.WEST);
    }// end method init


    /** 
     * Start or stop a game.
     * @param start if true, start else stop
     */
    public void setRunning(boolean start) {
        if(start && !running) {
            board.shuffle();
            canvas.repaint();
            player.init();
            computer.init();
            PlayerEntry.requestFocus();
            PlayerMessages.setText("Game is beginning.");
            timer=new TimerThread();
            timer.setActive(true);
            timer.start();
            ComputerWords.setText("");
            PlayerWords.setText("");
            ComputerWords.setVisible(false);
            computer.setActive(true);
            thread=new Thread(computer);
            switch(SkillChoice.getSelectedIndex()) {
                case(0): // novice
                    computer.setSkill(computer.SKILL_NOVICE);
                break;
                case(1): // advanced
                    computer.setSkill(computer.SKILL_INTERMEDIATE);
                break;
                case(2):
                    computer.setSkill(computer.SKILL_ADVANCED);
                break;
                case(3):
                    computer.setSkill(computer.SKILL_MASTER);
                break;
            }
            System.out.println("Skill level is "+computer.getSkill());
            thread.start();
            try { thread.setPriority(Thread.MIN_PRIORITY); } catch(SecurityException e) { 
                message("Could not change thread priority.", e);
            }
            try { timer.setPriority(Thread.MIN_PRIORITY/*timer.getPriority()-1*/); } catch(SecurityException e) { 
                message("Could not change timer priority.", e);
            }
            ++rounds;
            Round.setText("Round "+rounds);
            message("Round "+rounds+" started.");
            board.setPlayable(true);
            StartButton.setLabel("Stop"); 
            PlayerScore.setText("0");
            ComputerScore.setText("0");
        }
        else if(!start && running) { // stop
            timer.setActive(false);
            computer.setActive(false);
            board.setPlayable(false);

            message("Round "+rounds+" finished.");

            scoreWords();
            PlayerEntry.setText("");
            ComputerWords.setVisible(true);

            for(Enumeration e=player.added.keys();e.hasMoreElements();) {
                String key=(String)e.nextElement();
                if(computer.added.containsKey(key)) {
                    int pts=((Integer)player.added.get(key)).intValue();
                    player.score-=pts;
                    computer.score-=pts;
                    player.added.remove(key);
                    computer.added.remove(key);
                }
            }

            PlayerWords.setText("");
            ComputerWords.setText("");

            for(Enumeration e=player.added.keys();e.hasMoreElements();) {
                String key=(String)e.nextElement();
                int pts=((Integer)player.added.get(key)).intValue();
                PlayerWords.append(key+" ("+pts+")\n");
            }

            for(Enumeration e=computer.added.keys();e.hasMoreElements();) {
                String key=(String)e.nextElement();
                int pts=((Integer)computer.added.get(key)).intValue();
                ComputerWords.append(key+" ("+pts+")\n");
            }

            player.total();
            computer.total();

            try {
                PlayerWords.setCaretPosition(0);
                ComputerWords.setCaretPosition(0);
            } catch(Throwable t) { }

            ScrollLabel.setText("Press \"Start\" to initiate a new round.");
            StartButton.setLabel("Start"); 
            StartButton.requestFocus();

            int p=player.added.size();
            int s=computer.added.size();
            System.out.println("Player got "+p+" words. Average words/round ="+player.wordcount/rounds);
            System.out.println("Computer got "+s+" words. Average words/round ="+computer.wordcount/rounds);

            if(debug) System.out.println("Free memory is "+MBytes.format(Runtime.getRuntime().freeMemory()/(1024d*1024d)));
            if(debug) System.out.println("Total memory is "+MBytes.format(Runtime.getRuntime().totalMemory()/(1024d*1024d)));
        }
        running=start;
    }


    /**
     * Handle the words from the textfield player entry
     * so the player can continue play. It may be a better idea TO DO
     * this in a thread.
     */
    public void scoreWords() {
        String buffer=PlayerEntry.getText();
        PlayerEntry.setText("");
        StringTokenizer st=new StringTokenizer(buffer.trim(), " \t,;");
        while(st.hasMoreTokens())
            player.score(st.nextToken());
    }

    /**
     * Loads an image from the archive file (JAR)
     * @param name relative path to file (GIF or JPG, 
                 PNG is supported in JDK 1.3)
     * @return the Image
     */
    public Image loadImage(String name) throws IOException {
        InputStream in = getClass().getResourceAsStream(name);
        byte[] buffer = new byte[in.available()];
        in.read(buffer); 
        in.close();
        return Toolkit.getDefaultToolkit().createImage(buffer);
    }

    public void message(String message, Throwable t) {
        message(message);
        if(debug) t.printStackTrace(System.out);
    }

    public void message(String message) {
        showStatus(message);
        if(debug) System.out.println(message);
    }
}

