package enums;

public enum MsgProperty
{
    AKCIJA        ("akcija"        ),
    POSILJALAC    ("posiljalac"    ),
    PRIMALAC      ("primalac"      ),
    REQUEST_ID    ("request_id"    ),
    REQUEST_STATUS("request_status");

    private String property;

    MsgProperty(String property) { this.property = property; }
    public String toString() { return property; }
    
    public static MsgProperty getItem(String str)
    {
        if( str.equals(AKCIJA         + "") ) return AKCIJA;
        if( str.equals(POSILJALAC     + "") ) return POSILJALAC;
        if( str.equals(PRIMALAC       + "") ) return PRIMALAC;
        if( str.equals(REQUEST_ID     + "") ) return REQUEST_ID;
        if( str.equals(REQUEST_STATUS + "") ) return REQUEST_STATUS;
        throw new IllegalArgumentException("Nevalidna osobina");
    }
    public static int getIndex(String str) { return getItem(str).ordinal(); }
};
