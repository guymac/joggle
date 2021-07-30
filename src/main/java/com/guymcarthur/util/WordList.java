package com.guymcarthur.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("serial")
public class WordList extends Vector<String>
{
    private boolean debug=System.getProperty("DEBUG") != null;

    /**
     * @param in Alphabetically ordered word list
     */
    public WordList(BufferedReader in) throws IOException
    {
        super(10000, 1000);
        String line;
        
        while ((line = in.readLine()) != null)
            addElement(line.trim().toLowerCase());
    }

    /**
     * @param word Fragment to lookup
     * @return 1 if word is in list, -1 if word(s) in list start with word, 0 if no
     *         words start with word
     */
    public int has(final String word)
    {
        boolean foundStart = false;
        int lo = 0, hi = size(), mid = hi / 2;
        while (hi > lo + 1)
        {
            mid = lo + (hi - lo) / 2;
            if (debug)
                System.out.println("" + lo + " < " + mid + " < " + hi);
            String compare = (String) elementAt(mid);
            int s = word.compareTo(compare);
            if (debug)
                System.out.println("" + s + " = Comparing " + word + " to " + compare);
            if (s == 0)
                return 1;
            if (s > 0) // word follows compare
                lo = mid;
            else // word precedes compare
                hi = mid;
            if (!foundStart)
                foundStart = compare.startsWith(word);
        }
        return (foundStart) ? -1 : 0;
    }
}
