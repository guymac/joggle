package com.guymcarthur.applets.joggle;

import java.util.Random;

/**
 * Keeps track of the board.
 */
public class JoggleBoard
{
    public static final String[] letters = new String[26];
    protected int rows, cols;
    protected boolean playable;
    private JoggleDie[][] dice;
    private Random random;
    private boolean debug = System.getProperty("DEBUG") != null;

    static
    {
        for (char c = 'a'; c <= 'z'; c++)
            letters[c - 'a'] = (c == 'q') ? "qu" : "" + c;
    }

    public JoggleBoard(JoggleDie[][] dice)
    {
        this.dice = dice;
        this.rows = dice.length;
        this.cols = dice[0].length;
        random = new Random();

        shuffle();
    }

    /**
     * Roll each die and swap each randomly, preserving each die instance.
     */
    public void shuffle()
    {
        try
        {
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++)
                {
                    int a = (int) (random.nextDouble() * rows);
                    int b = (int) (random.nextDouble() * cols);
                    JoggleDie die = (JoggleDie) dice[i][j].clone();
                    dice[i][j] = dice[a][b];
                    dice[a][b] = die;
                    dice[i][j].roll();
                }
        } catch (CloneNotSupportedException ex)
        {
            if (debug)
                System.out.println(ex.getMessage());
        }
        if (debug)
            System.out.println(toString());
    }

    public void setPlayable(boolean playable)
    {
        this.playable = playable;
    }

    public boolean isPlayable()
    {
        return playable;
    }

    public boolean recurse(boolean[][] state, boolean found, String word, int pos, int row, int col)
    {
        if (debug)
            System.out.print("" + pos + ". (" + row + "," + col + ")");
        if (!found)
            found = (pos == word.length());
        if (found)
        {
            if (debug)
                System.out.println("\nFound " + word + "!");
            return found;
        }
        if (state[row][col])
        {
            if (debug)
                System.out.println(" has already been selected!");
            return false;
        }
        String value = getValue(row, col);
        String look;
        if (word.charAt(pos) == 'q')
        {
            look = word.substring(pos, 2 + pos);
            ++pos;
        } else
            look = word.substring(pos, 1 + pos);
        if (debug)
            System.out.println(" = " + value + " ?= " + look + ".");
        if (!(value.equals(look)))
            return false;
        state[row][col] = true;
        // corner tiles
        if (row == 0 && col == 0)
        {
            if (debug)
                System.out.println("Upper left.");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col + 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col + 1);
        } else if (row == 0 && col == cols - 1)
        {
            if (debug)
                System.out.println("Bottom left.");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col);
        } else if (row == rows - 1 && col == 0)
        {
            if (debug)
                System.out.println("Upper right.");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col + 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col + 1);
        } else if (row == rows - 1 && col == cols - 1)
        {
            if (debug)
                System.out.println("Bottom right");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col);
        }
        // side tiles
        else if (row == 0)
        {
            if (debug)
                System.out.println("Left column");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col + 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col + 1);
        } else if (row == rows - 1)
        {
            if (debug)
                System.out.println("Right column");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col + 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col + 1);
        } else if (col == 0)
        {
            if (debug)
                System.out.println("Top row");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col + 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col + 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col + 1);
        } else if (col == cols - 1)
        {
            if (debug)
                System.out.println("Bottom row");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col - 1);
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col - 1);
        }
        // center tiles
        else
        {
            if (debug)
                System.out.println("Center");
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col - 1); // 0
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col - 1); // 1
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col - 1); // 2
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col); // 3
            if (!found)
                found = recurse(state, found, word, 1 + pos, row + 1, col + 1); // 4
            if (!found)
                found = recurse(state, found, word, 1 + pos, row, col + 1); // 5
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col + 1); // 6
            if (!found)
                found = recurse(state, found, word, 1 + pos, row - 1, col); // 7
        }
        state[row][col] = false;
        return found;
    }

    public boolean has(final String word)
    {
        if (debug)
            System.out.println("Checking board for " + word);

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (recurse(new boolean[rows][cols], false, word, 0, i, j))
                    return true;
        return false;
    }

    public String toString()
    {
        StringBuffer strb = new StringBuffer();

        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
            {
                strb.append("(" + i + "," + j + ") = ");
                strb.append(getValue(i, j));
                strb.append(" :: ");
                strb.append(dice[i][j].toString());
                strb.append("\n");
            }
        return strb.toString();
    }

    public String getValue(int x, int y)
    {
        return (x < rows && y < cols) ? dice[x][y].getValue() : null;
    }
}
