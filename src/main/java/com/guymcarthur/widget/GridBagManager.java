package com.guymcarthur.widget;

import java.awt.*;

public class GridBagManager {
  public static void add(Container container, Component component, int row, int column, int rowspan, int colspan, int fill, int anchor, double weight_x, double weight_y, int top, int left, int bottom, int right)
  {
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = row; c.gridx = column;
    c.gridwidth = colspan; c.gridheight = rowspan;
    c.fill = fill; c.anchor = anchor;
    c.weightx = weight_x; c.weighty = weight_y;
    if (top+bottom+left+right > 0)
      c.insets = new Insets(top, left, bottom, right);
        
    ((GridBagLayout)container.getLayout()).setConstraints(component, c);
    container.add(component);
  }
    
  public static void add(Container container, Component component, int row, int column) {
    add(container, component, row, column, 
              1, 1, GridBagConstraints.NONE, 
              GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 0, 0);
  }
  public static void add(Container container, Component component, int row, int column, int rowspan, int colspan) {
    add(container, component, row, column, 
              rowspan, colspan, GridBagConstraints.NONE, 
              GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 0, 0, 0);
  }
    
  public static void add(Container container, Component component, int row, int column, int rowspan, int colspan, int top, int left, int bottom, int right) {
    add(container, component, row, column, 
              rowspan, colspan, GridBagConstraints.NONE, 
              GridBagConstraints.NORTHWEST, 
              0.0, 0.0, top, left, bottom, right);
  }
    
  public static void add(Container container, Component component, int row, int column, int rowspan, int colspan, int fill, int anchor) {
    add(container, component, row, column, 
              rowspan, colspan, fill, 
              anchor, 0.0, 0.0, 0, 0, 0, 0);
  }
    
}
