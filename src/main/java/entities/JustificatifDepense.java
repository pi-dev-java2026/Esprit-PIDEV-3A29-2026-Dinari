package entities;
import java.util.Date;
import java.util.Objects;
public class JustificatifDepense {


    private int idJustificatif;
    private String filepath;
    private Date dateajout;
    private String typefichier;
    private int idDepense  ;

    public JustificatifDepense() {
    }

    public JustificatifDepense(int idJustificatif, String filepath, Date dateajout, String typefichier, int idDepense) {
        this.idJustificatif = idJustificatif;
        this.filepath = filepath;
        this.dateajout = dateajout;
        this.typefichier = typefichier;
        this.idDepense = idDepense;
    }

    public JustificatifDepense(String filepath, Date dateajout, String typefichier, int idDepense) {
        this.filepath = filepath;
        this.dateajout = dateajout;
        this.typefichier = typefichier;
        this.idDepense = idDepense;
    }

    public int getIdJustificatif() {
        return idJustificatif;
    }

    public void setIdJustificatif(int idJustificatif) {
        this.idJustificatif = idJustificatif;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Date getDateajout() {
        return dateajout;
    }

    public void setDateajout(Date dateajout) {
        this.dateajout = dateajout;
    }

    public String getTypefichier() {
        return typefichier;
    }

    public void setTypefichier(String typefichier) {
        this.typefichier = typefichier;
    }


    public int getIdDepense() {
        return idDepense;
    }

    public void setIdDepense(int idDepense) {
        this.idDepense = idDepense;
    }


    @Override
    public String toString() {
        return "JustificatifDepense{" +
                "idJustificatif=" + idJustificatif +
                ", filepath='" + filepath + '\'' +
                ", dateajout=" + dateajout +
                ", typefichier='" + typefichier + '\'' +
                ", idDepense=" + idDepense +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JustificatifDepense that = (JustificatifDepense) o;
        return idJustificatif == that.idJustificatif && idDepense == that.idDepense && Objects.equals(filepath, that.filepath) && Objects.equals(dateajout, that.dateajout) && Objects.equals(typefichier, that.typefichier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idJustificatif, filepath, dateajout, typefichier, idDepense);
    }
}
