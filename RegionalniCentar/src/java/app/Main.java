package app;
import javax.annotation.*;
import javax.jms.*;
import javax.persistence.*;


public class Main {
    
    @Resource(lookup = "MyConnFactory")
    static ConnectionFactory connFact;
    
    @Resource(lookup = "MyTopic")
    static Topic topic;
    
    static EntityManager em;
    static RegCentar regcentar;
    
    
    
    public static void main(String[] args)
    {
        ////// create resources //////
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("RegCentarPU");
        em  = emf.createEntityManager();
        
        ////// check if injection is successful //////
        String errmsg = "";
        if( connFact == null ) errmsg += "Ne postoji connection factory\n";
        if( topic    == null ) errmsg += "Ne postoji topic\n";
        if( errmsg.length() > 0 )
            throw new NullPointerException(errmsg);
        
        ////// create regional centre and respond to incoming requests //////
        regcentar = new RegCentar();
        while( true )
            regcentar.receiveMessage();
    }
    
}
