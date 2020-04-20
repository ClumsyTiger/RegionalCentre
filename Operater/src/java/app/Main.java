package app;
import javax.annotation.*;
import javax.swing.*;
import javax.jms.*;


public class Main {
    
    @Resource(lookup = "MyConnFactory")
    static ConnectionFactory connFact;
    
    @Resource(lookup = "MyTopic")
    static Topic topic;
    
    
    
    public static void main(String[] args)
    {
        ////// check if injection is successful //////
        String errmsg = "";
        if( connFact == null ) errmsg += "Ne postoji connection factory\n";
        if( topic    == null ) errmsg += "Ne postoji topic\n";
        if( errmsg.length() > 0 )
            throw new NullPointerException(errmsg);
        
        ////// setting application look and feel //////
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch( ClassNotFoundException
             | IllegalAccessException
             | InstantiationException
             | UnsupportedLookAndFeelException ex )
        {}
        
        ////// create request form //////
        RequestForm form = new RequestForm();
        SwingUtilities.invokeLater( () -> form.setVisible(true) );
    }
}
