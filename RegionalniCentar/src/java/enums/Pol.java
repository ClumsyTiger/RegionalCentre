package enums;

public enum Pol
{
    MUSKI ("muski" ),
    ZENSKI("zenski");

    private String pol;

    Pol(String pol) { this.pol = pol; }
    public String toString() { return pol; }
    
    public static Pol getItem(String str)
    {
        if( str.equals(MUSKI  + "") ) return MUSKI;
        if( str.equals(ZENSKI + "") ) return ZENSKI;
        throw new IllegalArgumentException("Nevalidan pol");
    }
    public static int getIndex(String str) { return getItem(str).ordinal(); }
};
