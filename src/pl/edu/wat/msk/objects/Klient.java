package pl.edu.wat.msk.objects;


import hla.rti1516e.ObjectInstanceHandle;

public class Klient {

    private int id;
    private int priorytet;
    private boolean obslugiwany;
    private boolean obsluzony;
    private int idKolejki;

    public ObjectInstanceHandle KlientHandle;

    //kontruktor dla klienta
    public Klient()
    {
        this.id = 0;
        this.priorytet = 0;
        this.obslugiwany = false;
        this.obsluzony = false;
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

    public boolean isObslugiwany() {
        return obslugiwany;
    }

    public void setObslugiwany(boolean obslugiwany) {
        this.obslugiwany = obslugiwany;
    }

    public boolean isObsluzony() {
        return obsluzony;
    }

    public void setObsluzony(boolean obsluzony) {
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

    @Override
    public String toString() {
        return "Klient{" +
                "id=" + id +
                '}';
    }
}
