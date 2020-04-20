package entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.json.simple.JSONObject;

@Entity
@Table(name = "zahtev")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DocumentRequest.getMaxId", query = "SELECT MAX(d.id) FROM DocumentRequest d"),
    @NamedQuery(name = "DocumentRequest.findAll", query = "SELECT d FROM DocumentRequest d"),
    @NamedQuery(name = "DocumentRequest.findById", query = "SELECT d FROM DocumentRequest d WHERE d.id = :id"),
    @NamedQuery(name = "DocumentRequest.findByJmbg", query = "SELECT d FROM DocumentRequest d WHERE d.jmbg = :jmbg"),
    @NamedQuery(name = "DocumentRequest.findByIme", query = "SELECT d FROM DocumentRequest d WHERE d.ime = :ime"),
    @NamedQuery(name = "DocumentRequest.findByPrezime", query = "SELECT d FROM DocumentRequest d WHERE d.prezime = :prezime"),
    @NamedQuery(name = "DocumentRequest.findByImeMajke", query = "SELECT d FROM DocumentRequest d WHERE d.imeMajke = :imeMajke"),
    @NamedQuery(name = "DocumentRequest.findByImeOca", query = "SELECT d FROM DocumentRequest d WHERE d.imeOca = :imeOca"),
    @NamedQuery(name = "DocumentRequest.findByPrezimeMajke", query = "SELECT d FROM DocumentRequest d WHERE d.prezimeMajke = :prezimeMajke"),
    @NamedQuery(name = "DocumentRequest.findByPrezimeOca", query = "SELECT d FROM DocumentRequest d WHERE d.prezimeOca = :prezimeOca"),
    @NamedQuery(name = "DocumentRequest.findByPol", query = "SELECT d FROM DocumentRequest d WHERE d.pol = :pol"),
    @NamedQuery(name = "DocumentRequest.findByDatumRodjenja", query = "SELECT d FROM DocumentRequest d WHERE d.datumRodjenja = :datumRodjenja"),
    @NamedQuery(name = "DocumentRequest.findByNacionalnost", query = "SELECT d FROM DocumentRequest d WHERE d.nacionalnost = :nacionalnost"),
    @NamedQuery(name = "DocumentRequest.findByProfesija", query = "SELECT d FROM DocumentRequest d WHERE d.profesija = :profesija"),
    @NamedQuery(name = "DocumentRequest.findByBracnoStanje", query = "SELECT d FROM DocumentRequest d WHERE d.bracnoStanje = :bracnoStanje"),
    @NamedQuery(name = "DocumentRequest.findByOpstinaPrebivalista", query = "SELECT d FROM DocumentRequest d WHERE d.opstinaPrebivalista = :opstinaPrebivalista"),
    @NamedQuery(name = "DocumentRequest.findByUlicaPrebivalista", query = "SELECT d FROM DocumentRequest d WHERE d.ulicaPrebivalista = :ulicaPrebivalista"),
    @NamedQuery(name = "DocumentRequest.findByBrojPrebivalista", query = "SELECT d FROM DocumentRequest d WHERE d.brojPrebivalista = :brojPrebivalista"),
    @NamedQuery(name = "DocumentRequest.findByStatus", query = "SELECT d FROM DocumentRequest d WHERE d.status = :status")})
public class DocumentRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public  static final String EMPTY_ID     = "-";
    private static final String DATUM_FORMAT = "yyyy-MM-dd";

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 12, max = 12)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 13)
    @Column(name = "JMBG")
    private String jmbg;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "ime")
    private String ime;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "prezime")
    private String prezime;
    @Basic(optional = false)
    @NotNull()
    @Size(min = 1, max = 30)
    @Column(name = "imeMajke")
    private String imeMajke;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "imeOca")
    private String imeOca;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "prezimeMajke")
    private String prezimeMajke;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "prezimeOca")
    private String prezimeOca;
    @Basic(optional = false)
    @NotNull()
    @Size(min = 1, max = 20)
    @Column(name = "pol")
    private String pol;
    @Basic(optional = false)
    @NotNull()
    @Column(name = "datumRodjenja")
    @Temporal(TemporalType.DATE)
    private Date datumRodjenja;
    @Basic(optional = false)
    @NotNull()
    @Size(min = 1, max = 30)
    @Column(name = "nacionalnost")
    private String nacionalnost;
    @Basic(optional = false)
    @NotNull()
    @Size(min = 1, max = 30)
    @Column(name = "profesija")
    private String profesija;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "bracnoStanje")
    private String bracnoStanje;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "opstinaPrebivalista")
    private String opstinaPrebivalista;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "ulicaPrebivalista")
    private String ulicaPrebivalista;
    @Basic(optional = false)
    @NotNull()
    @Size(min = 1, max = 10)
    @Column(name = "brojPrebivalista")
    private String brojPrebivalista;
    @Basic(optional = false)
    @NotNull()
    @Size(min = 1, max = 20)
    @Column(name = "status")
    private String status;

    public DocumentRequest() {
    }

    public DocumentRequest(String id) {
        this.id = id;
    }

    public DocumentRequest(String id, String jmbg, String ime, String prezime, String pol, Date datumRodjenja, String nacionalnost, String opstinaPrebivalista, String ulicaPrebivalista, String brojPrebivalista, String status) {
        this.id = id;
        this.jmbg = jmbg;
        this.ime = ime;
        this.prezime = prezime;
        this.pol = pol;
        this.datumRodjenja = datumRodjenja;
        this.nacionalnost = nacionalnost;
        this.opstinaPrebivalista = opstinaPrebivalista;
        this.ulicaPrebivalista = ulicaPrebivalista;
        this.brojPrebivalista = brojPrebivalista;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJmbg() {
        return jmbg;
    }

    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getImeMajke() {
        return imeMajke;
    }

    public void setImeMajke(String imeMajke) {
        this.imeMajke = imeMajke;
    }

    public String getImeOca() {
        return imeOca;
    }

    public void setImeOca(String imeOca) {
        this.imeOca = imeOca;
    }

    public String getPrezimeMajke() {
        return prezimeMajke;
    }

    public void setPrezimeMajke(String prezimeMajke) {
        this.prezimeMajke = prezimeMajke;
    }

    public String getPrezimeOca() {
        return prezimeOca;
    }

    public void setPrezimeOca(String prezimeOca) {
        this.prezimeOca = prezimeOca;
    }

    public String getPol() {
        return pol;
    }

    public void setPol(String pol) {
        this.pol = pol;
    }

    public Date getDatumRodjenja() {
        return datumRodjenja;
    }

    public void setDatumRodjenja(Date datumRodjenja) {
        this.datumRodjenja = datumRodjenja;
    }

    public String getNacionalnost() {
        return nacionalnost;
    }

    public void setNacionalnost(String nacionalnost) {
        this.nacionalnost = nacionalnost;
    }

    public String getProfesija() {
        return profesija;
    }

    public void setProfesija(String profesija) {
        this.profesija = profesija;
    }

    public String getBracnoStanje() {
        return bracnoStanje;
    }

    public void setBracnoStanje(String bracnoStanje) {
        this.bracnoStanje = bracnoStanje;
    }

    public String getOpstinaPrebivalista() {
        return opstinaPrebivalista;
    }

    public void setOpstinaPrebivalista(String opstinaPrebivalista) {
        this.opstinaPrebivalista = opstinaPrebivalista;
    }

    public String getUlicaPrebivalista() {
        return ulicaPrebivalista;
    }

    public void setUlicaPrebivalista(String ulicaPrebivalista) {
        this.ulicaPrebivalista = ulicaPrebivalista;
    }

    public String getBrojPrebivalista() {
        return brojPrebivalista;
    }

    public void setBrojPrebivalista(String brojPrebivalista) {
        this.brojPrebivalista = brojPrebivalista;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DocumentRequest)) {
            return false;
        }
        DocumentRequest other = (DocumentRequest) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "entities.DocumentRequest[ id=" + id + " ]";
    }
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat(DATUM_FORMAT);
        
        json.put("id",                  id                 );
        json.put("JMBG",                jmbg               );
        json.put("ime",                 ime                );
        json.put("prezime",             prezime            );
        json.put("imeMajke",            imeMajke           );
        json.put("imeOca",              imeOca             );
        json.put("prezimeMajke",        prezimeMajke       );
        json.put("prezimeOca",          prezimeOca         );
        json.put("pol",                 pol                );
        json.put("datumRodjenja",       sdf.format(datumRodjenja));
        json.put("nacionalnost",        nacionalnost       );
        json.put("profesija",           profesija          );
        json.put("bracnoStanje",        bracnoStanje       );
        json.put("opstinaPrebivalista", opstinaPrebivalista);
        json.put("ulicaPrebivalista",   ulicaPrebivalista  );
        json.put("brojPrebivalista",    brojPrebivalista   );
        json.put("status",              status             );
        
        return json;
    }
    
    public String toJSONString() {
        return toJSON().toString();
    }

}
