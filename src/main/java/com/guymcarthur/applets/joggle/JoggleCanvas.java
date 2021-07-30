package com.guymcarthur.applets.joggle;

import java.awt.*;
import java.util.Hashtable;

/**
 * Extension of Canvas backed by a JoggleBoard data model
 */
@SuppressWarnings("serial")
public class JoggleCanvas extends Canvas
{
    private JoggleBoard board;
    private Hashtable <String, Image> images;
    private int cellWidth, cellHeight;
    private Rectangle[][] rects;
    private int width, height;
    private boolean debug = System.getProperty("DEBUG") != null;

    /**
     * @param board
     * @param images letter =&gt; Image of letter
     */
    public JoggleCanvas(JoggleBoard board, Hashtable <String, Image> images)
    {
        this.board = board;
        this.images = images;

        Image image = (Image) (images.elements().nextElement());
        cellHeight = image.getHeight(this);
        cellWidth = image.getWidth(this);

        rects = new Rectangle[board.rows][board.cols];
        for (int i = 0; i < board.rows; i++)
            for (int j = 0; j < board.cols; j++)
                rects[i][j] = new Rectangle(i * cellWidth, j * cellHeight, cellWidth, cellHeight);

        if (debug)
            System.out.println("Preferred size = " + getPreferredSize());
    }

    public void paint(Graphics g)
    {
        width = getSize().width;
        height = getSize().height;
        for (int i = 0; i < board.rows; i++)
            for (int j = 0; j < board.cols; j++)
            {
                int off_x = i * width / board.rows;
                int off_y = j * height / board.cols;
                g.drawImage((Image) images.get(board.getValue(i, j)), off_x, off_y, this);
            }
    }

    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }

    public Dimension getMaximumSize()
    {
        return getMinimumSize();
    }

    public Dimension getMinimumSize()
    {
        Image image = (Image) (images.elements().nextElement());
        return new Dimension(board.rows * image.getWidth(this), board.cols * image.getHeight(this));
    }

    public int[] getCell(int x, int y)
    {
        for (int i = 0; i < board.rows; i++)
            for (int j = 0; j < board.cols; j++)
            {
                Rectangle r = rects[i][j];
                if (r.contains(x, y))
                {
                    return new int[] { i, j };
                }
            }
        return new int[] { -1, -1 };
    }

    public void selectCell(int row, int col, Color color)
    {
        Rectangle r = rects[row][col];
        Graphics g = getGraphics();
        g.setColor(color);
        g.drawRect(r.x + 4, r.y + 3, r.width - 7, r.height - 7);

    }

}
