package pl.edu.wat.msk.objects;


import hla.rti1516e.ObjectInstanceHandle;

import java.util.LinkedList;

public class Okienko {

    private int id;
    private LinkedList<Klient> kolejkaZwykla;
    private LinkedList<Klient> kolejkaUprzywilejowana;
    private int wolne;
    private ObjectInstanceHandle OkienkoHandle;
    private int czasUtworzenia;

    //kontruktor dla klienta
    public Okienko(int czasUtworzenia)
    {
        this.id = 0;
        this.OkienkoHandle = null;
        this.czasUtworzenia = czasUtworzenia;
        this.kolejkaZwykla = new LinkedList<>();
        this.kolejkaUprzywilejowana = new LinkedList<>();
        this.wolne = 0;
    }

    //konstruktor dla odbiorcy(subskrybującego)
    public Okienko(ObjectInstanceHandle KlientHandle)
    {
        this.OkienkoHandle = KlientHandle;
        this.id = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWolne() {
        return wolne;
    }

    public void setWolne(int wolne) {
        this.wolne = wolne;
    }

    public ObjectInstanceHandle getOkienkoHandle() {
        return OkienkoHandle;
    }

    public void setOkienkoHandle(ObjectInstanceHandle okienkoHandle) {
        OkienkoHandle = okienkoHandle;
    }

    public int getCzasUtworzenia() {
        return czasUtworzenia;
    }

    public void setCzasUtworzenia(int czasUtworzenia) {
        this.czasUtworzenia = czasUtworzenia;
    }

    public LinkedList<Klient> getKolejkaZwykla() {
        return kolejkaZwykla;
    }

    public LinkedList<Klient> getKolejkaUprzywilejowana() {
        return kolejkaUprzywilejowana;
    }

    @Override
    public String toString() {
        return "Okienko{" +
                "id=" + id +
                ", kolejkaZwykla.size=" + kolejkaZwykla.size() +
                '}';
    }
}
