package pl.edu.wat.msk.objects;


import hla.rti1516e.ObjectInstanceHandle;

public class Klient {

    private int id;
    private int priorytet;
    private int obslugiwany;
    private int obsluzony;
    private int idKolejki;
    private int czasUtworzenia;

    public ObjectInstanceHandle KlientHandle;

    //kontruktor dla klienta
    public Klient(int czasUtworzenia)
    {
        this.id = 0;
        this.czasUtworzenia = czasUtworzenia;
        this.priorytet = 0;
        this.obslugiwany = 0;
        this.obsluzony = 0;
        this.idKolejki = 0;
    }

    //konstruktor dla odbiorcy(subskrybującego)
    public Klient(ObjectInstanceHandle KlientHandle)
    {
        this.KlientHandle = KlientHandle;
        this.idKolejki = 0;
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

    @Override
    public String toString() {
        return "Klient{" +
                "id=" + id +
                '}';
    }
}
