package enums;

public enum Status
{
    NOVI        ("novi"            ),
    NEISPRAVAN  ("neispravan"      ),
    KREIRAN     ("kreiran"         ),
    U_PRODUKCIJI("uProdukciji"     ),
    PROIZVEDEN  ("proizveden"      ),
    URUCEN      ("urucen"          );

    private String status;

    Status(String status) { this.status = status; }
    public String toString() { return status; }
    
    public static Status getItem(String str)
    {
        if( str.equals(NOVI         + "") ) return NOVI;
        if( str.equals(NEISPRAVAN   + "") ) return NEISPRAVAN;
        if( str.equals(KREIRAN      + "") ) return KREIRAN;
        if( str.equals(U_PRODUKCIJI + "") ) return U_PRODUKCIJI;
        if( str.equals(PROIZVEDEN   + "") ) return PROIZVEDEN;
        if( str.equals(URUCEN       + "") ) return URUCEN;
        throw new IllegalArgumentException("Nevalidan status");
    }
    public static int getIndex(String str) { return getItem(str).ordinal(); }
};
