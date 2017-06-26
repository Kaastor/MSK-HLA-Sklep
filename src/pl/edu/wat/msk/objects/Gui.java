package pl.edu.wat.msk.objects;


import hla.rti1516e.ObjectInstanceHandle;

public class Gui {

    private int czasObslugi;
    private int liczbaNaplywajacychKlientow;
    private int okresCzasuNaplywu;
    private int liczbaOkienek;

    private int czasUtworzenia;

    private ObjectInstanceHandle GuiHandle;

    public Gui(int czasObslugi, int liczbaNaplywajacychKlientow, int okresCzasuNaplywu, int liczbaOkienek) {
        this.czasObslugi = czasObslugi;
        this.liczbaNaplywajacychKlientow = liczbaNaplywajacychKlientow;
        this.okresCzasuNaplywu = okresCzasuNaplywu;
        this.liczbaOkienek = liczbaOkienek;
        this.czasUtworzenia = 0;
        this.GuiHandle = null;
    }

    public int getCzasUtworzenia() {
        return czasUtworzenia;
    }

    public Gui(ObjectInstanceHandle GuiHandle)
    {
        this.GuiHandle = GuiHandle;
    }

    public int getCzasObslugi() {
        return czasObslugi;
    }

    public void setCzasObslugi(int czasObslugi) {
        this.czasObslugi = czasObslugi;
    }

    public int getLiczbaNaplywajacychKlientow() {
        return liczbaNaplywajacychKlientow;
    }

    public void setLiczbaNaplywajacychKlientow(int liczbaNaplywajacychKlientow) {
        this.liczbaNaplywajacychKlientow = liczbaNaplywajacychKlientow;
    }

    public int getOkresCzasuNaplywu() {
        return okresCzasuNaplywu;
    }

    public void setOkresCzasuNaplywu(int okresCzasuNaplywu) {
        this.okresCzasuNaplywu = okresCzasuNaplywu;
    }

    public int getLiczbaOkienek() {
        return liczbaOkienek;
    }

    public void setLiczbaOkienek(int liczbaOkienek) {
        this.liczbaOkienek = liczbaOkienek;
    }

    public ObjectInstanceHandle getGuiHandle() {
        return GuiHandle;
    }

    public void setGuiHandle(ObjectInstanceHandle guiHandle) {
        GuiHandle = guiHandle;
    }

    @Override
    public String toString() {
        return "Gui{" +
                "czasObslugi=" + czasObslugi +
                ", liczbaNaplywajacychKlientow=" + liczbaNaplywajacychKlientow +
                ", okresCzasuNaplywu=" + okresCzasuNaplywu +
                ", liczbaOkienek=" + liczbaOkienek +
                '}';
    }
}
