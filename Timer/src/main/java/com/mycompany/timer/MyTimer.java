package com.mycompany.timer;
import enums.*;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.jms.*;

import java.util.logging.Level;
import java.util.logging.Logger;


@Stateless
public class MyTimer {
    
    @Resource(lookup = "MyConnFactory")
    public ConnectionFactory connFact;
        
    @Resource(lookup = "MyTopic")
    public Topic topic;
    
    private JMSContext  context;
    private JMSProducer producer;
    
    private static final String EMPTY_ID = "-";
    
    
    
    @PostConstruct
    public void init()
    {
        String errmsg = "";
        if( connFact == null ) errmsg += "Ne postoji connection factory\n";
        if( topic    == null ) errmsg += "Ne postoji topic\n";
        if( errmsg.length() > 0 )
            throw new NullPointerException(errmsg);
        
        context  = connFact.createContext();
        producer = context.createProducer();
    }
    
    @Schedule(second = "*/2", minute = "*",  hour = "*", persistent = false)
    public void tick()
    {
        try
        {
            ObjectMessage msg = context.createObjectMessage();
            msg.setStringProperty(MsgProperty.POSILJALAC + "", Akter.OPERATER       + "");
            msg.setStringProperty(MsgProperty.PRIMALAC   + "", Akter.OPERATER       + "");
            msg.setStringProperty(MsgProperty.AKCIJA     + "", Akcija.OSVEZI_ZAHTEV + "");
            msg.setStringProperty(MsgProperty.REQUEST_ID + "", EMPTY_ID                 );

            producer.send(topic, msg);
        }
        catch( JMSException ex )
        {
            Logger.getLogger(MyTimer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

