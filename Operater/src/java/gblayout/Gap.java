package gblayout;
import java.awt.*;
import javax.swing.*;


public class Gap extends JComponent {
    
    // Creates filler with minimum size, but expandable infinitely.
    public Gap() {
        Dimension min = new Dimension(0, 0);
        Dimension max = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        setMinimumSize(min);
        setPreferredSize(min);
        setMaximumSize(max);
    }
    
    // Creates rigid filler.
    public Gap(int size) {
        Dimension dim = new Dimension(size, size);
        setMinimumSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);
    }
}