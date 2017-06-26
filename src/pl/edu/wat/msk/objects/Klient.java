package pl.edu.wat.msk.objects;


import dissimlab.random.SimGenerator;
import hla.rti1516e.ObjectInstanceHandle;
import pl.edu.wat.msk.Okienko.OkienkoFederate;

public class Klient {

    private int id;
    private int priorytet;
    private int obslugiwany;
    private int obsluzony;
    private int wKolejce;
    private int idKolejki;
    private int czasUtworzenia;

    private ObjectInstanceHandle KlientHandle;

    //kontruktor dla klienta
    public Klient(int czasUtworzenia) {
        SimGenerator simGenerator = new SimGenerator();
        this.id = 0;
        this.KlientHandle = null;
        this.czasUtworzenia = czasUtworzenia;
        this.wKolejce = 0;
        this.obslugiwany = 0;
        this.obsluzony = 0;
        this.idKolejki = simGenerator.uniformInt(1, OkienkoFederate.liczbaOkienek+1);
        this.priorytet = simGenerator.uniformInt(0, 2);
    }
    //konstruktor dla odbiorcy(subskrybującego)
    public Klient(ObjectInstanceHandle KlientHandle)
    {
        this.KlientHandle = KlientHandle;
        //tutaj wybor wartosci + update
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPriorytet() {
        return priorytet;
    }

    public void setPriorytet(int priorytet) {
        this.priorytet = priorytet;
    }

    public int getObslugiwany() {
        return obslugiwany;
    }

    public void setObslugiwany(int obslugiwany) {
        this.obslugiwany = obslugiwany;
    }

    public int getObsluzony() {
        return obsluzony;
    }

    public void setObsluzony(int obsluzony) {
        this.obsluzony = obsluzony;
    }

    public int getIdKolejki() {
        return idKolejki;
    }

    public void setIdKolejki(int idKolejki) {
        this.idKolejki = idKolejki;
    }

    public ObjectInstanceHandle getKlientHandle() {
        return KlientHandle;
    }

    public void setKlientHandle(ObjectInstanceHandle klientHandle) {
        KlientHandle = klientHandle;
    }

    public int getCzasUtworzenia() {
        return czasUtworzenia;
    }

    public void setCzasUtworzenia(int czasUtworzenia) {
        this.czasUtworzenia = czasUtworzenia;
    }

    public int getwKolejce() {
        return wKolejce;
    }

    public void setwKolejce(int wKolejce) {
        this.wKolejce = wKolejce;
    }

    @Override
    public String toString() {
        return "Klient{" +
                "id=" + id +
                ", priorytet =" + priorytet +
                ", idKolejki=" + idKolejki +
                ", wKolejce=" + wKolejce +
                '}';
    }
}
