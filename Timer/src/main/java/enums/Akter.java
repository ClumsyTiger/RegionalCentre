package enums;

public enum Akter
{
    OPERATER ("operater" ),
    REGCENTAR("regcentar");

    private String akter;

    Akter(String akter) { this.akter = akter; }
    public String toString() { return akter; }
    
    public static Akter getItem(String str)
    {
        if( str.equals(OPERATER  + "") ) return OPERATER;
        if( str.equals(REGCENTAR + "") ) return REGCENTAR;
        throw new IllegalArgumentException("Nevalidan akter");
    }
    public static int getIndex(String str) { return getItem(str).ordinal(); }
};
