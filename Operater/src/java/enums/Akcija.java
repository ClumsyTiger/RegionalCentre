package enums;

public enum Akcija
{
    PREDAJ_ZAHTEV ("Predaj zahtev" ),
    OSVEZI_ZAHTEV ("Osvezi zahtev" ),
    PREUZMI_ZAHTEV("Preuzmi zahtev"),
    NOVI_ZAHTEV   ("Novi zahtev"   );

    private String akcija;

    Akcija(String akcija) { this.akcija = akcija; }
    public String toString() { return akcija; }
    
    public static Akcija getItem(String str)
    {
        if( str.equals(PREDAJ_ZAHTEV  + "") ) return PREDAJ_ZAHTEV;
        if( str.equals(OSVEZI_ZAHTEV  + "") ) return OSVEZI_ZAHTEV;
        if( str.equals(PREUZMI_ZAHTEV + "") ) return PREUZMI_ZAHTEV;
        if( str.equals(NOVI_ZAHTEV    + "") ) return NOVI_ZAHTEV;
        throw new IllegalArgumentException("Nevalidna akcija");
    }
    public static int getIndex(String str) { return getItem(str).ordinal(); }
};
