package pl.edu.wat.msk.Okienko;


import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import pl.edu.wat.msk.objects.Klient;
import pl.edu.wat.msk.objects.Okienko;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class OkienkoFederate {

    public static int liczbaOkienek =2;
    public static int czasObslugi = 20;
    public static int zakonczenieObslugiCzas = 0;

    public static final int ITERATIONS = 1000000;
    public static final String READY_TO_RUN = "ReadyToRun";
    public static int timer;
    public static int maxOkienkoId = 0;

    private int simTime;
    private RTIambassador rtiamb;
    private OkienkoFederateAmbassador fedamb;
    private HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    protected InteractionClassHandle koniecSymulacjiHandle;
    protected InteractionClassHandle klientObsluzonyHandle;
    protected ParameterHandle idObsluzonegoKlientaHandle;

    //udostepniane z fom
    protected ObjectClassHandle KlientHandle;
    protected AttributeHandle idHandle;
    protected AttributeHandle priorytetHandle;
    protected AttributeHandle obslugiwanyHandle;
    protected AttributeHandle obsluzonyHandle;
    protected AttributeHandle idKolejkiHandle;
    protected AttributeHandle wKolejceHandle;

    LinkedList<Okienko> listaOkienek = new LinkedList<>();
    LinkedList<Klient> listaKlientow = new LinkedList<>();

    private void log(String message) {
        System.out.println(simTime + " :OkienkoFederate: " + message);
    }

    private void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createFederation(String federateName) throws Exception {
        log("Creating RTIambassador...");
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
        log("Connecting...");
        fedamb = new OkienkoFederateAmbassador(this);
        rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);
        log("Creating Federation...");
        try {
            URL[] modules = new URL[]{
                    (new File("fom.xml")).toURI().toURL()
            };

            rtiamb.createFederationExecution("federation", modules);
            log("Created Federation ");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception loading one of the FOM modules from disk: " + urle.getMessage());
            urle.printStackTrace();
            return;
        }

        rtiamb.joinFederationExecution(federateName, "OkienkoFederate", "federation");
        log("Joined Federation as " + federateName);

    }

    public void runFederate(String federateName) throws Exception {
        createFederation(federateName);

        this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();
        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);
        while (fedamb.isAnnounced == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
        waitForUser();
        rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (fedamb.isReadyToRun == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
        enableTimePolicy();
        log("Time Policy Enabled");
        publishAndSubscribe();
        log("Published and Subscribed");

        for (int i = 0  ; i < liczbaOkienek ; i++)
            rtiNoweOkienko();

        for (timer = 0; timer < ITERATIONS; timer++) {
            simTime = timer;
            obslugaKlientow();
            advanceTime(1.0);
        }
        resign();
    }

    private void resign() throws Exception {
        rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
        log("Resigned from Federation");

        try {
            rtiamb.destroyFederationExecution("federation");
            log("Destroyed Federation");
        } catch (FederationExecutionDoesNotExist dne) {
            log("No need to destroy federation, it doesn't exist");
        } catch (FederatesCurrentlyJoined fcj) {
            log("Didn't destroy federation, federates still joined");
        }
    }

    private void publishAndSubscribe() throws RTIexception {
        this.KlientHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Klient");
        this.idHandle = rtiamb.getAttributeHandle(this.KlientHandle, "id");
        this.priorytetHandle = rtiamb.getAttributeHandle(this.KlientHandle, "priorytet");
        this.obslugiwanyHandle = rtiamb.getAttributeHandle(this.KlientHandle, "obslugiwany");
        this.obsluzonyHandle = rtiamb.getAttributeHandle(this.KlientHandle, "obsluzony");
        this.idKolejkiHandle = rtiamb.getAttributeHandle(this.KlientHandle, "idKolejki");
        this.wKolejceHandle = rtiamb.getAttributeHandle(this.KlientHandle, "wKolejce");
        AttributeHandleSet attributes2 = rtiamb.getAttributeHandleSetFactory().create();
        attributes2.add(this.idHandle);
        attributes2.add(this.priorytetHandle);
        attributes2.add(this.obslugiwanyHandle);
        attributes2.add(this.obsluzonyHandle);
        attributes2.add(this.idKolejkiHandle);
        attributes2.add(this.wKolejceHandle);
        rtiamb.subscribeObjectClassAttributes(KlientHandle, attributes2);

        koniecSymulacjiHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.koniecSymulacji");
        rtiamb.subscribeInteractionClass(koniecSymulacjiHandle);

        klientObsluzonyHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.klientObsluzony");
        idObsluzonegoKlientaHandle = rtiamb.getParameterHandle(this.klientObsluzonyHandle, "idObsluzonegoKlienta");
        rtiamb.publishInteractionClass(klientObsluzonyHandle);
    }

    private void advanceTime(double timeStep) throws RTIexception {
//        log("Advancing...");
        fedamb.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep);
        rtiamb.timeAdvanceRequest(time);
        while (fedamb.isAdvancing) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
//        log("Time Advanced to " + fedamb.federateTime);
    }


    private void enableTimePolicy() throws Exception {
        HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);
        this.rtiamb.enableTimeRegulation(lookahead);
        while (fedamb.isRegulating == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
        this.rtiamb.enableTimeConstrained();
        while (fedamb.isConstrained == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private byte[] generateTag() {
        return ("(timestamp) " + System.currentTimeMillis()).getBytes();
    }

    public void endSim() {
        timer = ITERATIONS;
        listaKlientow.clear();
        listaOkienek.clear();
    }

    private void sendInteraction(String type, int klientId) throws RTIexception {
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        if (type.equals("klientObsluzony")) {
            ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);

            HLAinteger32BE[] idKlienta = new HLAinteger32BE[1];
            idKlienta[0] = encoderFactory.createHLAinteger32BE(klientId);

            HLAfixedArray<HLAinteger32BE> idObsluzonegoKlienta = encoderFactory.createHLAfixedArray( idKlienta );
            parameters.put(idObsluzonegoKlientaHandle, idObsluzonegoKlienta.toByteArray());

            rtiamb.sendInteraction(klientObsluzonyHandle, parameters, generateTag(), time);
        }
    }

    public void obslugaKlientow() throws Exception {
        log(listaOkienek.toString());
        log(listaKlientow.toString());
        for(Klient klient : listaKlientow){
            if(klient.getwKolejce() == 0){
                dodajDoKolejki(klient);
            }
        }

        for(Okienko okienko : listaOkienek){
            if(okienko.getWolne() == 1){//pobierz kogos do obslugi jak jest
                okienko.setObslugiwany( okienko.getKolejkaUprzywilejowana().poll() );
                if(okienko.getObslugiwany() != null){
                    log("KlientId " + okienko.getObslugiwany().getId() + " (uprzywilejowany) został pobrany do obslugi przez " + okienko.getId());
                    okienko.getObslugiwany().setObslugiwany(1);//do etstow
                    okienko.setWolne(0);
                    zakonczenieObslugiCzas = simTime + czasObslugi; //ustaw kiedy koniec obslugi
                }
                else{
                    okienko.setObslugiwany( okienko.getKolejkaZwykla().poll() );
                    if(okienko.getObslugiwany() != null){
                        log("KlientId " + okienko.getObslugiwany().getId() + " (zwykly) został pobrany do obslugi przez " + okienko.getId());
                        okienko.getObslugiwany().setObslugiwany(1);
                        okienko.setWolne(0);
                        zakonczenieObslugiCzas = simTime + czasObslugi; //ustaw kiedy koniec obslugi
                    }
                }
            }
            else{
                if(okienko.getObslugiwany() != null) { //ktos jest w ogole obslugiwany -> ustawione zakonczenieObslugiCzas
                    if (zakonczenieObslugiCzas == simTime) {
                        sendInteraction("klientObsluzony", okienko.getObslugiwany().getId());
                        log("KlientId " + okienko.getObslugiwany().getId() + " został obsluzony");
                        listaKlientow.remove(okienko.getObslugiwany());
                        okienko.setObslugiwany(null);
                        okienko.setWolne(1);
                    }
                }
            }
        }
    }

    public void rtiNowyKlient(ObjectInstanceHandle klientHandle) throws Exception
    {
        Klient klient= new Klient(klientHandle);
        listaKlientow.add(klient);
    }

    private void dodajDoKolejki(Klient klient) throws Exception{ //stad sie nie da, trzeba z glownej petli, zrobic jak marcin
        for(Okienko okienko : listaOkienek){
            if(okienko.getId() == klient.getIdKolejki()) {
                if(klient.getPriorytet() == 1) {
                    okienko.getKolejkaUprzywilejowana().add(klient);
                    klient.setwKolejce(1);
                    log("Klient " + klient.getId() + " dodany do uprzywilejowanej o Id: "+ okienko.getId());
                }
                else {
                    okienko.getKolejkaZwykla().add(klient);
                    klient.setwKolejce(1);
                    log("Klient " + klient.getId() + " dodany do zwyklej o Id: " + okienko.getId());
                }
            }
        }
    }

    public void rtiNoweOkienko() throws Exception {
        Okienko okienko = new Okienko(simTime + 1);
        okienko.setId(maxOkienkoId + 1);
        maxOkienkoId = maxOkienkoId + 1;
        listaOkienek.add(okienko);
        log("OkienkoId: " + okienko.getId() + " nowe okienko wygenerowane.");
    }

    public void rtiUpdateKlient(ObjectInstanceHandle klient, int id, int idKolejka, int obsluzony, int obslugiwany, int priorytet,  int wKolejce) {
        //update klient jesli taki istnieje w kolejce
        int index = -1;
        for (int i = 0; i < listaKlientow.size(); i++)    //znajdz klienta ktory byl updatowany
        {
            if (listaKlientow.get(i).getKlientHandle().equals(klient))
                index = i;
        }
        if (index != -1) //przydziel mu wartosci
        {
            Klient updateklient = listaKlientow.get(index);
            if (updateklient.getId() == 0 || updateklient.getId() == id) {
                updateklient.setId(id);
                updateklient.setIdKolejki(idKolejka);
                updateklient.setObslugiwany(obslugiwany);
                updateklient.setObsluzony(obsluzony);
                updateklient.setPriorytet(priorytet);
                updateklient.setwKolejce(wKolejce);
            }
        }
    }


    public static void main(String[] args) {
        try {
            new OkienkoFederate().runFederate("Okienko");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
