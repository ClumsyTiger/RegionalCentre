package app;
import gblayout.*;
import entities.*;
import enums.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.jms.*;

import java.util.LinkedList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.Serializable;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.ParseException;


public class RequestForm extends JFrame {
    ////// id-status section //////
    private JComboBox  idSL;
    private JLabel     statusLBL;
    ////// basic personal info //////
    private JTextField jmbgTF;
    private JTextField imeTF;
    private JTextField prezimeTF;
    ////// parent info //////
    private JTextField imeMajkeTF;
    private JTextField prezimeMajkeTF;
    private JTextField imeOcaTF;
    private JTextField prezimeOcaTF;
    ////// more personal info //////
    private JComboBox  polSL;
    private JTextField datumRodjenjaTF;
    private JTextField nacionalnostTF;
    private JTextField profesijaTF;
    private JComboBox  bracnoStanjeSL;
    ////// place of residence //////
    private JTextField opstinaTF;
    private JTextField ulicaTF;
    private JTextField brojTF;
    ////// button section //////
    private JButton    button;
    ////// layout //////
    static final int BORDER = 12;   // window border in pixels
    static final int GAP    = 7;    // gap between components in pixels
    
    ////// connections //////
    private JMSContext  context;
    private JMSProducer producer;
    private JMSConsumer consumer;
    
    ////// requests //////
    private LinkedList<DocumentRequest> reqs;
    
    ////// constants //////
    private static final int S_DISP_LEN       = 15;
    private static final int M_DISP_LEN       = 30;
    private static final int L_DISP_LEN       = 50;
    private static final String S_DISP_STR    = "                                  ";
    
    private static final int ID_LEN           = 12;
    private static final int JMBG_LEN         = 13;
    private static final int IME_LEN          = 30;
    private static final int PREZIME_LEN      = 50;
    
    private static final int DATUM_LEN        = 10;
    private static final String DATUM_FORMAT  = "yyyy-MM-dd";
    private static final int NACIONALNOST_LEN = 30;
    private static final int PROFESIJA_LEN    = 30;
    
    private static final int OPSTINA_LEN      = 50;
    private static final int ULICA_LEN        = 50;
    private static final int BROJ_LEN         = 10;
    
    
    
    ////// constructor //////
    public RequestForm()
    {
        ////// components and layout //////
        initComponents();
        
        ////// connections //////
        context  = Main.connFact.createContext();
        producer = context.createProducer();
        consumer = context.createConsumer(Main.topic, MsgProperty.PRIMALAC + "='" + Akter.OPERATER + "'");
        
        consumer.setMessageListener(e -> respondToMessage(e));
        
        ////// requests //////
        reqs = new LinkedList<>();
    }
    
    
    
    ////// communication with regional centre and timer //////
    private synchronized boolean respondToMessage(Message message)
    {
        try
        {
            ObjectMessage msg = (ObjectMessage) message;
            Akcija akcija     = Akcija.getItem(msg.getStringProperty(MsgProperty.AKCIJA + ""));

            switch( akcija )
            {
                case NOVI_ZAHTEV:
                {
                    DocumentRequest req = (DocumentRequest) msg.getObject();
                    addRequestToList(req);
                    loadForm(req);
                    return true;
                }
                case OSVEZI_ZAHTEV:
                case PREUZMI_ZAHTEV:
                {
                    Akter posiljalac = Akter.getItem(msg.getStringProperty(MsgProperty.POSILJALAC + ""));
                    
                    switch( posiljalac )
                    {
                        case OPERATER:
                        {
                            DocumentRequest req = getSelectedRequest();
                            if( req == null ) return true;
                            
                            Status status = Status.getItem(req.getStatus());
                            if( status != Status.KREIRAN && status != Status.U_PRODUKCIJI )
                                return false;
                            
                            ObjectMessage response = createMessageForRegCentar(Akcija.OSVEZI_ZAHTEV, req.getId());
                            return sendMessage(response);
                        }
                        case REGCENTAR:
                        {
                            String reqid  = msg.getStringProperty(MsgProperty.REQUEST_ID + "");
                            Status status = Status.getItem(msg.getStringProperty(MsgProperty.REQUEST_STATUS + ""));
                            
                            setRequestStatus(reqid, status);
                            if( reqid.equals(idSL.getSelectedItem()) )
                                setFormStatus(status);
                            
                            return true;
                        }
                        default:
                        {
                            return false;
                        }
                    }
                    
                    
                }
                default:
                {
                    return false;
                }
            }
        }
        catch( JMSException | ClassCastException | IllegalArgumentException ex )
        {
            Logger.getLogger(RequestForm.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private ObjectMessage createMessageForRegCentar(Akcija akcija, Object ... args)
    {
        if( akcija == null ) return null;
        
        try
        {
            ObjectMessage response = context.createObjectMessage();
            
            response.setStringProperty(MsgProperty.POSILJALAC + "", Akter.OPERATER  + "");
            response.setStringProperty(MsgProperty.PRIMALAC   + "", Akter.REGCENTAR + "");
            response.setStringProperty(MsgProperty.AKCIJA     + "", akcija          + "");
            
            switch( akcija )
            {
                case PREDAJ_ZAHTEV:
                    response.setObject((Serializable) args[0]);
                    break;
                case OSVEZI_ZAHTEV:
                    response.setStringProperty(MsgProperty.REQUEST_ID + "", args[0] + "");
                    break;
                case PREUZMI_ZAHTEV:
                    response.setStringProperty(MsgProperty.REQUEST_ID + "", args[0] + "");
                    break;
            }
            
            return response;
        }
        catch( JMSException | ClassCastException | NullPointerException ex )
        {
            Logger.getLogger(RequestForm.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private boolean sendMessage(ObjectMessage msg)
    {
        if( msg == null ) return false;
        producer.send(Main.topic, msg);
        return true;
    }
    
    
    
    ////// action listeners //////
    private synchronized boolean doButtonActionListener(Akcija akcija)
    {
        if( akcija == null ) return false;
        
        switch( akcija )
        {
            case PREDAJ_ZAHTEV:
            {
                if( isFormValid() )
                {
                    DocumentRequest req = createRequestFromForm();
                    ObjectMessage msg = createMessageForRegCentar(Akcija.PREDAJ_ZAHTEV, req);
                    return sendMessage(msg);
                }
                else
                {
                    setFormStatus(Status.NEISPRAVAN);
                    return false;
                }
            }
            case OSVEZI_ZAHTEV:
            {
                ObjectMessage msg = createMessageForRegCentar(Akcija.OSVEZI_ZAHTEV, idSL.getSelectedItem());
                return sendMessage(msg);
            }
            case PREUZMI_ZAHTEV:
            {
                ObjectMessage msg = createMessageForRegCentar(Akcija.PREUZMI_ZAHTEV, idSL.getSelectedItem());
                return sendMessage(msg);
            }
            case NOVI_ZAHTEV:
            {
                clearForm();
                return true;
            }
            default:
            {
                return false;
            }
        }
    }
    
    private synchronized void updateFormSelectionListener()
    {
        String reqid = (String) idSL.getSelectedItem();
        loadForm(getRequestById(reqid));
    }
    
    
    
    ////// request list methods //////
    private boolean addRequestToList(DocumentRequest req)
    {
        if( req == null ) return false;
        reqs.add(req);
        idSL.addItem(req.getId());
        idSL.setSelectedItem(req);
        return true;
    }
    
    private DocumentRequest getRequestById(String id)
    {
        if( id == null || id.equals(DocumentRequest.EMPTY_ID) )
            return null;
        
        for( DocumentRequest req : reqs )
            if( req.getId().equals(id) )
                return req;
        
        return null;
    }
    
    private DocumentRequest getSelectedRequest()
    {
        String reqid = (String) idSL.getSelectedItem();
        return getRequestById(reqid);
    }
    
    private boolean setRequestStatus(String id, Status status)
    {
        if( id == null || status == null ) return false;
        DocumentRequest req = getRequestById(id);

        if( req == null ) return false;
        req.setStatus(status + "");
        
        return true;
    }
    
    
    
    ////// layout methods //////
    private void initComponents()
    {
        ////// id-status section //////
        idSL      = new JComboBox();                // CHAR(12)
        idSL.addItem(DocumentRequest.EMPTY_ID);
        idSL.addItemListener(e -> updateFormSelectionListener());
        idSL.setPrototypeDisplayValue(S_DISP_STR);
        statusLBL = new JLabel(Status.NOVI + "");   // enum Status

        ////// basic personal info //////
        jmbgTF    = new JTextField(S_DISP_LEN);   // CHAR(13)
        imeTF     = new JTextField(M_DISP_LEN);   // NVARCHAR(30)
        prezimeTF = new JTextField(L_DISP_LEN);   // NVARCHAR(50)

        ////// parent info //////
        imeMajkeTF     = new JTextField(M_DISP_LEN);   // NVARCHAR(30)
        prezimeMajkeTF = new JTextField(L_DISP_LEN);   // NVARCHAR(50)
        imeOcaTF       = new JTextField(M_DISP_LEN);   // NVARCHAR(30)
        prezimeOcaTF   = new JTextField(L_DISP_LEN);   // NVARCHAR(50)

        ////// more personal info //////
        polSL           = new JComboBox();              // enum Pol
        polSL.addItem(Pol.MUSKI  + "");
        polSL.addItem(Pol.ZENSKI + "");
        polSL.setPrototypeDisplayValue(S_DISP_STR);
        datumRodjenjaTF = new JTextField(S_DISP_LEN);   // DATE
        nacionalnostTF  = new JTextField(M_DISP_LEN);   // NVARCHAR(30)
        profesijaTF     = new JTextField(M_DISP_LEN);   // NVARCHAR(30)
        bracnoStanjeSL  = new JComboBox();              // enum BracnoStanje
        bracnoStanjeSL.addItem(BracnoStanje.NEOZENJEN_A   + "");
        bracnoStanjeSL.addItem(BracnoStanje.OZENJEN_UDATA + "");
        bracnoStanjeSL.addItem(BracnoStanje.RAZVEDEN_A    + "");
        bracnoStanjeSL.addItem(BracnoStanje.UDOVAC_ICA    + "");
        bracnoStanjeSL.setPrototypeDisplayValue(S_DISP_STR);

        ////// place of residence //////
        opstinaTF = new JTextField(L_DISP_LEN);   // NVARCHAR(50)
        ulicaTF   = new JTextField(L_DISP_LEN);   // NVARCHAR(50)
        brojTF    = new JTextField(S_DISP_LEN);   // NVARCHAR(10)

        ////// button section //////
        button    = new JButton();
        setButtonStatus(Status.NOVI);
        button.addActionListener(e -> doButtonActionListener( Akcija.getItem(e.getActionCommand()) ) );
        
        ////// layout //////
        GBHelper pos = new GBHelper();   // gridbag helper object
        
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));

        // row 1-2
        addLabelAndComponentRow(content, pos, "ID zahteva",     idSL     );
        addLabelAndComponentRow(content, pos, "Status zahteva", statusLBL);
        addRowGap(content, pos, GAP);
        // row 3-5
        addLabelAndComponentRow(content, pos, "JMBG",    jmbgTF   );
        addLabelAndComponentRow(content, pos, "Ime",     imeTF    );
        addLabelAndComponentRow(content, pos, "Prezime", prezimeTF);
        addRowGap(content, pos, GAP);
        // row 6-9
        addLabelAndComponentRow(content, pos, "Ime majke",     imeMajkeTF    );
        addLabelAndComponentRow(content, pos, "Prezime majke", prezimeMajkeTF);
        addLabelAndComponentRow(content, pos, "Ime oca",       imeOcaTF      );
        addLabelAndComponentRow(content, pos, "Prezime oca",   prezimeOcaTF  );
        addRowGap(content, pos, GAP);
        // row 10-14
        addLabelAndComponentRow(content, pos, "Pol",             polSL          );
        addLabelAndComponentRow(content, pos, "Datum rodjenja",  datumRodjenjaTF);
        addLabelAndComponentRow(content, pos, "Nacionalnost",    nacionalnostTF );
        addLabelAndComponentRow(content, pos, "Profesija",       profesijaTF    );
        addLabelAndComponentRow(content, pos, "Bracno stanje",   bracnoStanjeSL );
        addRowGap(content, pos, GAP);
        // row 15-17
        addLabelAndComponentRow(content, pos, "Opstina prebivalista", opstinaTF);
        addLabelAndComponentRow(content, pos, "Ulica prebivalista",   ulicaTF  );
        addLabelAndComponentRow(content, pos, "Broj prebivalista",    brojTF   );
        addRowGap(content, pos, GAP);
        // row 18
        addColGap(content, pos);
        addColGap(content, pos);
        content.add(button,  pos.align(GridBagConstraints.EAST));   pos.nextRow();
        addRowGap(content, pos);

        ////// app //////
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setTitle("Operater");
        super.setContentPane(content);
        super.pack();
        super.setResizable(false);
        super.setLocationRelativeTo(null);
    }
    
    private static void addLabelAndComponentRow(JPanel panel, GBHelper pos, String str, JComponent comp)
    {
        panel.add(new JLabel(str), pos        ); pos.nextCol();
        panel.add(new Gap(GAP),    pos        ); pos.nextCol();
        panel.add(comp,            pos.width().align(GridBagConstraints.WEST)); pos.nextRow();
    }
    
    private static void addRowGap(JPanel panel, GBHelper pos, int GAP) { panel.add(new Gap(GAP), pos); pos.nextRow(); }
    private static void addRowGap(JPanel panel, GBHelper pos         ) { panel.add(new Gap(),    pos); pos.nextRow(); }
    
    private static void addColGap(JPanel panel, GBHelper pos, int GAP) { panel.add(new Gap(GAP), pos); pos.nextCol(); }
    private static void addColGap(JPanel panel, GBHelper pos         ) { panel.add(new Gap(),    pos); pos.nextCol(); }
    
    
    
    ////// request form methods //////
    private void loadForm(DocumentRequest req)
    {
        ItemListener[] lis = idSL.getItemListeners();
        for( ItemListener el : lis )
            idSL.removeItemListener(el);
        
        SimpleDateFormat sdf = new SimpleDateFormat(DATUM_FORMAT);
        Status status = (req != null) ? Status.getItem(req.getStatus()) : Status.NOVI;
        
        idSL     .setSelectedItem((req != null) ? req.getId() : DocumentRequest.EMPTY_ID );
        statusLBL.setText        (status + ""                                            );

        jmbgTF   .setText((req != null) ? req.getJmbg()    : "");
        imeTF    .setText((req != null) ? req.getIme()     : "");
        prezimeTF.setText((req != null) ? req.getPrezime() : "");

        imeMajkeTF    .setText((req != null) ? req.getImeMajke()     : "");
        prezimeMajkeTF.setText((req != null) ? req.getPrezimeMajke() : "");
        imeOcaTF      .setText((req != null) ? req.getImeOca()       : "");
        prezimeOcaTF  .setText((req != null) ? req.getPrezimeOca()   : "");

        polSL          .setSelectedIndex((req != null) ? Pol.getIndex(req.getPol())                   : 0 );
        datumRodjenjaTF.setText         ((req != null) ? sdf.format(req.getDatumRodjenja())           : "");
        nacionalnostTF .setText         ((req != null) ? req.getNacionalnost()                        : "");
        profesijaTF    .setText         ((req != null) ? req.getProfesija()                           : "");
        bracnoStanjeSL .setSelectedIndex((req != null) ? BracnoStanje.getIndex(req.getBracnoStanje()) : 0 );

        opstinaTF.setText((req != null) ? req.getOpstinaPrebivalista() : "");
        ulicaTF  .setText((req != null) ? req.getUlicaPrebivalista()   : "");
        brojTF   .setText((req != null) ? req.getBrojPrebivalista()    : "");

        setFormStatus(status);
        
        for( ItemListener el : lis )
            idSL.addItemListener(el);
    }
    
    private void clearForm() { loadForm(null); }
    
    private void setFormStatus(Status status)
    {
        if( status == null ) return;
        boolean editable = (status == Status.NOVI || status == Status.NEISPRAVAN);

        statusLBL.setText(status + "");
        
        jmbgTF   .setEditable(editable);
        imeTF    .setEditable(editable);
        prezimeTF.setEditable(editable);

        imeMajkeTF    .setEditable(editable);
        prezimeMajkeTF.setEditable(editable);
        imeOcaTF      .setEditable(editable);
        prezimeOcaTF  .setEditable(editable);

        polSL          .setEnabled (editable);
        datumRodjenjaTF.setEditable(editable);
        nacionalnostTF .setEditable(editable);
        profesijaTF    .setEditable(editable);
        bracnoStanjeSL .setEnabled (editable);

        opstinaTF.setEditable(editable);
        ulicaTF  .setEditable(editable);
        brojTF   .setEditable(editable);

        setButtonStatus(status);
    }
    
    private void setButtonStatus(Status status)
    {
        if( status == null ) return;
        
        Akcija akcija;
        switch( status )
        {
            case NOVI:    case NEISPRAVAN:     akcija = Akcija.PREDAJ_ZAHTEV;  break;
            case KREIRAN: case U_PRODUKCIJI:   akcija = Akcija.OSVEZI_ZAHTEV;  break;
            case PROIZVEDEN:                   akcija = Akcija.PREUZMI_ZAHTEV; break;
            case URUCEN:                       akcija = Akcija.NOVI_ZAHTEV;    break;
            default:                           akcija = null;                  break;
        }
        
        if( akcija != null )
        {
            button.setText         (akcija + "");
            button.setActionCommand(akcija + "");
        }
    }
    
    private boolean isFormValid()
    {
        boolean valid = true;
        
        if( !isTextOK(jmbgTF, JMBG_LEN, JMBG_LEN) || !isTextNumeric(jmbgTF) ) { insertNOK(jmbgTF);    valid = false; }
        if( !isTextOK(imeTF,     1, IME_LEN    ) )                            { insertNOK(imeTF);     valid = false; }
        if( !isTextOK(prezimeTF, 1, PREZIME_LEN) )                            { insertNOK(prezimeTF); valid = false; }
        
        if( !isTextOK(imeMajkeTF,     1, IME_LEN    ) )   { insertNOK(imeMajkeTF);     valid = false; }
        if( !isTextOK(prezimeMajkeTF, 1, PREZIME_LEN) )   { insertNOK(prezimeMajkeTF); valid = false; }
        if( !isTextOK(imeOcaTF,       1, IME_LEN    ) )   { insertNOK(imeOcaTF);       valid = false; }
        if( !isTextOK(prezimeOcaTF,   1, PREZIME_LEN) )   { insertNOK(prezimeOcaTF);   valid = false; }

        if( parseDate(datumRodjenjaTF.getText(), DATUM_FORMAT) == null )  { insertNOK(datumRodjenjaTF);  valid = false; }
        if( !isTextOK(nacionalnostTF, 1, NACIONALNOST_LEN) )              { insertNOK(nacionalnostTF);   valid = false; }
        if( !isTextOK(profesijaTF,    1, PROFESIJA_LEN   ) )              { insertNOK(profesijaTF);      valid = false; }
        
        if( !isTextOK(opstinaTF, 1, OPSTINA_LEN) )   { insertNOK(opstinaTF);   valid = false; }
        if( !isTextOK(ulicaTF,   1, ULICA_LEN  ) )   { insertNOK(ulicaTF);     valid = false; }
        if( !isTextOK(brojTF,    1, BROJ_LEN   ) )   { insertNOK(brojTF);      valid = false; }

        return valid;
    }
    
    private DocumentRequest createRequestFromForm()
    {
        DocumentRequest req = new DocumentRequest();
        
        req.setId(DocumentRequest.EMPTY_ID);
        req.setStatus(Status.NOVI + "");
        
        req.setJmbg   (   jmbgTF.getText());
        req.setIme    (    imeTF.getText());
        req.setPrezime(prezimeTF.getText());
        
        req.setImeMajke    (    imeMajkeTF.getText());
        req.setPrezimeMajke(prezimeMajkeTF.getText());
        req.setImeOca      (      imeOcaTF.getText());
        req.setPrezimeOca  (  prezimeOcaTF.getText());

        req.setPol          ( (String)polSL.getSelectedItem() );
        req.setDatumRodjenja( parseDate(datumRodjenjaTF.getText(), DATUM_FORMAT) );
        req.setNacionalnost ( nacionalnostTF.getText() );
        req.setProfesija    (    profesijaTF.getText() );
        req.setBracnoStanje ( (String)bracnoStanjeSL.getSelectedItem() );
        
        req.setOpstinaPrebivalista(opstinaTF.getText());
        req.setUlicaPrebivalista  (  ulicaTF.getText());
        req.setBrojPrebivalista   (   brojTF.getText());
        
        return req;
    }
    
    
    
    ////// utility methods //////
    private static boolean isTextOK(JTextComponent comp, int minlen, int maxlen)
    {
        String str = comp.getText();
        if( str == null )                                    return false;
        if( str.length() < minlen || str.length() > maxlen ) return false;
        if( str.length() > 0      && str.charAt(0) == '#'  ) return false;
        return true;
    }
    
    private static boolean isTextNumeric(JTextComponent comp)
    {
        String str = comp.getText();
        if( str == null || str.length() == 0 ) return false;
        return str.chars().allMatch(Character::isDigit);
    }
    
    private static Date parseDate(String datestr, String format)
    {
        if( datestr == null )
            return null;

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        
        try {
            return sdf.parse(datestr);
        }
        catch( ParseException ex ) {
            return null;
        }
    }
    
    private static void insertNOK(JTextComponent comp)
    {
        String str = comp.getText();

        if     ( str == null || str.length() == 0 ) comp.setText("#");
        else if( str.charAt(0) != '#'  )            comp.setText('#' + str);
    }
    
    
    
}