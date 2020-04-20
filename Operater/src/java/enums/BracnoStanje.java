package enums;

public enum BracnoStanje
{
    NEOZENJEN_A  ("neozenjen/a"   ),
    OZENJEN_UDATA("ozenjen/udata" ),
    RAZVEDEN_A   ("razveden/a"    ),
    UDOVAC_ICA   ("udovac/udovica");

    private String stanje;

    BracnoStanje(String stanje) { this.stanje = stanje; }
    public String toString() { return stanje; }
    
    public static BracnoStanje getItem(String str)
    {
        if( str.equals(NEOZENJEN_A   + "") ) return NEOZENJEN_A;
        if( str.equals(OZENJEN_UDATA + "") ) return OZENJEN_UDATA;
        if( str.equals(RAZVEDEN_A    + "") ) return RAZVEDEN_A;
        if( str.equals(UDOVAC_ICA    + "") ) return UDOVAC_ICA;
        throw new IllegalArgumentException("Nevalidno bracno stanje");
    }
    public static int getIndex(String str) { return getItem(str).ordinal(); }
};

