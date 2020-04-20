package app;
import entities.*;
import enums.*;

import javax.jms.*;
import javax.persistence.*;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.math.BigDecimal;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.Serializable;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import org.json.simple.parser.ParseException;
import javax.validation.ConstraintViolationException;


public class RegCentar
{
    ////// connections //////
    private JMSContext  context;
    private JMSProducer producer;
    private JMSConsumer consumer;
    
    ////// constants //////
    private static final String REGCENTAR_ID = "17081";
    private static final String DEFAULT_ID   = REGCENTAR_ID + "0000000";
    private static final String TIMESLOT     = "2020-02-10T09:30:00";
    
    
    
    ////// constructor //////
    public RegCentar()
    {
        ////// connections //////
        context  = Main.connFact.createContext();
        producer = context.createProducer();
        consumer = context.createConsumer(Main.topic, MsgProperty.PRIMALAC + "='" + Akter.REGCENTAR + "'");
    }
    
    
    
    ////// communication with operater //////
    public void receiveMessage()
    {
        respondToMessage(consumer.receive());
    }
    
    private synchronized boolean respondToMessage(Message message)
    {
        try
        {
            ObjectMessage msg = (ObjectMessage) message;
            Akcija akcija     = Akcija.getItem(msg.getStringProperty(MsgProperty.AKCIJA + ""));

            switch( akcija )
            {
                case PREDAJ_ZAHTEV:
                {
                    DocumentRequest req = (DocumentRequest) msg.getObject();
                    req.setId(getNextAvailRequestId());
                    req.setStatus(Status.KREIRAN + "");
                    
                    if( !persistRequest(req)           ) return false;
                    if( !isTimeslotAvailable(TIMESLOT) ) return false;
                    if( !submitRequest(req)            ) return false;
                    
                    ObjectMessage response = createMessageForOperater(Akcija.NOVI_ZAHTEV, req);
                    return sendMessage(response);
                }
                case OSVEZI_ZAHTEV:
                {
                    String reqid  = msg.getStringProperty(MsgProperty.REQUEST_ID + "");
                    Status status = getRequestStatus(reqid);
                    if( status == null ) return false;
                    
                    DocumentRequest req = getRequestById(reqid);
                    req.setStatus(status.toString());
                    if( !mergeRequest(req) ) return false;
                    
                    ObjectMessage response = createMessageForOperater(Akcija.OSVEZI_ZAHTEV, req.getId(), req.getStatus());
                    return sendMessage(response);
                }
                case PREUZMI_ZAHTEV:
                {
                    String reqid  = msg.getStringProperty(MsgProperty.REQUEST_ID + "");
                    Status status = getRequestStatus(reqid);
                    if( status != Status.PROIZVEDEN ) return false;
                    
                    DocumentRequest req = getRequestById(reqid);
                    req.setStatus(Status.URUCEN + "");
                    if( !mergeRequest(req) ) return false;
                    
                    ObjectMessage response = createMessageForOperater(Akcija.OSVEZI_ZAHTEV, req.getId(), req.getStatus());
                    return sendMessage(response);
                }
                default:
                {
                    return false;
                }
            }
        }
        catch( JMSException | ClassCastException | IllegalArgumentException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private ObjectMessage createMessageForOperater(Akcija akcija, Object ... args)
    {
        if( akcija == null ) return null;
        
        try
        {
            ObjectMessage response = context.createObjectMessage();
            
            response.setStringProperty(MsgProperty.POSILJALAC     + "", Akter.REGCENTAR + "");
            response.setStringProperty(MsgProperty.PRIMALAC       + "", Akter.OPERATER  + "");
            response.setStringProperty(MsgProperty.AKCIJA         + "", akcija          + "");
            
            switch( akcija )
            {
                case NOVI_ZAHTEV:
                    response.setObject((Serializable) args[0]);
                    break;
                case OSVEZI_ZAHTEV:
                    response.setStringProperty(MsgProperty.REQUEST_ID     + "", args[0]         + "");
                    response.setStringProperty(MsgProperty.REQUEST_STATUS + "", args[1]         + "");
                    break;
            }
            
            return response;
        }
        catch( JMSException | NullPointerException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private boolean sendMessage(ObjectMessage msg)
    {
        if( msg == null ) return false;
        producer.send(Main.topic, msg);
        return true;
    }
    
    
    
    ////// communication with termin centar and perso centar //////
    private static boolean isTimeslotAvailable(String datetime)
    {
        if( datetime == null ) return false;
        HttpURLConnection conn = null;
        
        try
        {
            conn = createConnectionTo("http://collabnet.netset.rs:8081/is/terminCentar/checkTimeslotAvailability?regionalniCentarId=" + REGCENTAR_ID + "&termin=" + datetime, "GET");
            if( conn == null ) return false;
            conn.connect();
            
            JSONObject response = getJSONResponse(conn);
            if( response == null ) return false;
            
            Boolean dostupnost = (Boolean) response.get("dostupnost");
            return dostupnost;
        }
        catch( IOException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        finally
        {
            if( conn != null )
                conn.disconnect();
        }
    }
    
    private static boolean submitRequest(DocumentRequest req)
    {
        if( req == null ) return false;
        HttpURLConnection conn = null;
        
        try
        {
            conn = createConnectionTo("http://collabnet.netset.rs:8081/is/persoCentar/submit", "POST");
            if( conn == null ) return false;
            conn.connect();
            
            return ( sendStringMessage(conn, req.toJSONString()) == HttpURLConnection.HTTP_OK );
        }
        catch( IOException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        finally
        {
            if( conn != null )
                conn.disconnect();
        }
    }
    
    private static Status getRequestStatus(String reqid)
    {
        if( reqid == null ) return null;
        HttpURLConnection conn = null;
        
        try
        {
            conn = createConnectionTo("http://collabnet.netset.rs:8081/is/persoCentar/" + reqid, "GET");
            if( conn == null ) return null;
            conn.connect();
            
            JSONObject response = getJSONResponse(conn);
            if( response == null ) return null;
            
            String status = (String) response.get("status");
            return ( status != null ) ? Status.getItem(status) : null;
        }
        catch( IOException | IllegalArgumentException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        finally
        {
            if( conn != null )
                conn.disconnect();
        }
    }
    
    
    
    ////// http connectivity //////
    private static HttpURLConnection createConnectionTo(String dest, String requestMethod)
    {
        if( dest == null ) return null;
        
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(dest).openConnection();

            conn.setDoInput (true);
            conn.setDoOutput(true);

            conn.setConnectTimeout(1000);
            conn.setReadTimeout   (1000);

            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("Content-Type", "application/json");
            
            return conn;
        }
        catch( IOException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private static int sendStringMessage(HttpURLConnection conn, String msg)
    {
        if( conn == null ) return HttpURLConnection.HTTP_UNAVAILABLE;
        if( msg  == null ) return HttpURLConnection.HTTP_OK;
        
        try( OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()) )
        {
            wr.write(msg);
            wr.flush();
            wr.close();
            return conn.getResponseCode();
        }
        catch( IOException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return HttpURLConnection.HTTP_UNAVAILABLE;
        }
    }
    
    private static JSONObject getJSONResponse(HttpURLConnection conn)
    {
        if( conn == null ) return null;
        
        try
        {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new InputStreamReader(conn.getInputStream()));
            return json;
        }
        catch( IOException | ParseException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    
    
    ////// request helper methods //////
    private static String getMaxRequestId()
    {
        String maxId = Main.em.createNamedQuery("DocumentRequest.getMaxId", String.class).getSingleResult();
        return ( maxId != null ) ? maxId : DEFAULT_ID;
    }
    private static String getNextAvailRequestId()
    {
        BigDecimal nextId = new BigDecimal( getMaxRequestId() ).add(BigDecimal.ONE);
        return nextId.toString();
    }
    
    private static DocumentRequest getRequestById(String id)
    {
        return Main.em.createNamedQuery("DocumentRequest.findById", DocumentRequest.class)
                      .setParameter("id", id)
                      .getSingleResult();
    }

    private static DocumentRequest getRequestByJmbg(String jmbg)
    {
        return Main.em.createNamedQuery("DocumentRequest.findByJmbg", DocumentRequest.class)
                      .setParameter("jmbg", jmbg)
                      .getSingleResult();
    }
    
    private static boolean persistRequest(DocumentRequest req)
    {
        if( req == null ) return false;
        
        try
        {
            Main.em.getTransaction().begin();
            Main.em.persist(req);
            Main.em.getTransaction().commit();
            return true;
        }
        catch( EntityExistsException | ConstraintViolationException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        finally
        {
            if( Main.em.getTransaction().isActive() )
                Main.em.getTransaction().rollback();   // radi se rollback samo u bazi, ne u aplikaciji !!!
        }
    }
    
    private static boolean mergeRequest(DocumentRequest req)
    {
        if( req == null ) return false;
        
        try
        {
            Main.em.getTransaction().begin();
            Main.em.merge(req);
            Main.em.getTransaction().commit();
            return true;
        }
        catch( EntityNotFoundException | ConstraintViolationException ex )
        {
            Logger.getLogger(RegCentar.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        finally
        {
            if( Main.em.getTransaction().isActive() )
                Main.em.getTransaction().rollback();   // radi se rollback samo u bazi, ne u aplikaciji !!!
        }
    }
    
    
    
    ////// testing //////
    private static void test01()
    {
        DocumentRequest req = new DocumentRequest();
        req.setId("170819999995");
        req.setStatus("kreiran");
        
        req.setJmbg("1111111111111");
        req.setIme("John");
        req.setPrezime("Doe");

        req.setImeMajke("JaneSr");
        req.setPrezimeMajke("Doe");
        req.setImeOca("JohnSr");
        req.setPrezimeOca("Doe");

        req.setPol("muski");
        req.setDatumRodjenja(new java.util.Date());
        req.setNacionalnost("srbin");
        req.setBracnoStanje("neozenjen/a");
        req.setProfesija("programer");

        req.setOpstinaPrebivalista("Beograd");
        req.setUlicaPrebivalista("Bana Ivanisa");
        req.setBrojPrebivalista("13a");
        
        System.out.println(RegCentar.isTimeslotAvailable("2020-02-10T09:30:00") ? "true" : "false");
        System.out.println(RegCentar.submitRequest(req) ? "true" : "false");
        System.out.println(RegCentar.getRequestStatus("170819999999") + "");
    }
    

    
}
