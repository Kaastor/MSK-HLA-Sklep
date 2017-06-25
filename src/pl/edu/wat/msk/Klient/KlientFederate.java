package pl.edu.wat.msk.Klient;


import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import pl.edu.wat.msk.objects.Klient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class KlientFederate {

    public static final int ITERATIONS = 1000000;
    public static final String READY_TO_RUN = "ReadyToRun";
    public static int timer;
    public static int maxKlientId = 0;

    private RTIambassador rtiamb;
    private KlientFederateAmbassador fedamb;
    private HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    protected InteractionClassHandle koniecSymulacjiHandle;
    protected InteractionClassHandle generujKlientaHandle;

    //udostepniane z fom
    protected ObjectClassHandle KlientHandle;
    protected AttributeHandle idHandle;
    protected AttributeHandle priorytetHandle;
    protected AttributeHandle obslugiwanyHandle;
    protected AttributeHandle obsluzonyHandle;
    protected AttributeHandle idKolejkiHandle;

    LinkedList<Klient> listaKlientow = new LinkedList<>();

    private void log(String message) {
        System.out.println("KlientFederate: " + message);
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
        fedamb = new KlientFederateAmbassador(this);
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

        rtiamb.joinFederationExecution(federateName, "KlientFederate", "federation");
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

        for (timer = 0; timer < ITERATIONS; timer++) {
            klientUtylizacja(timer);
            log("Advancing...");
            advanceTime(1.0);
            log("Time Advanced to " + fedamb.federateTime);
        }
        resign();
    }

    private void resign() throws Exception {
        rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
        log("Resigned from Federation");

        try {
            rtiamb.destroyFederationExecution("MSKfed");
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
        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(this.idHandle);
        attributes.add(this.priorytetHandle);
        attributes.add(this.obslugiwanyHandle);
        attributes.add(this.obsluzonyHandle);
        attributes.add(this.idKolejkiHandle);
        rtiamb.publishObjectClassAttributes(KlientHandle, attributes);

        this.KlientHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Klient");
        this.idHandle = rtiamb.getAttributeHandle(this.KlientHandle, "id");
        this.priorytetHandle = rtiamb.getAttributeHandle(this.KlientHandle, "priorytet");
        this.obslugiwanyHandle = rtiamb.getAttributeHandle(this.KlientHandle, "obslugiwany");
        this.obsluzonyHandle = rtiamb.getAttributeHandle(this.KlientHandle, "obsluzony");
        this.idKolejkiHandle = rtiamb.getAttributeHandle(this.KlientHandle, "idKolejki");
        AttributeHandleSet attributes2 = rtiamb.getAttributeHandleSetFactory().create();
        attributes2.add(this.idHandle);
        attributes2.add(this.priorytetHandle);
        attributes2.add(this.obslugiwanyHandle);
        attributes2.add(this.obsluzonyHandle);
        attributes2.add(this.idKolejkiHandle);
        rtiamb.subscribeObjectClassAttributes(KlientHandle, attributes2);


        generujKlientaHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.generujKlienta");
        rtiamb.subscribeInteractionClass(generujKlientaHandle);
        koniecSymulacjiHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.koniecSymulacji");
        rtiamb.publishInteractionClass(koniecSymulacjiHandle);
    }


    private void advanceTime(double timeStep) throws RTIexception {
        fedamb.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep);
        rtiamb.timeAdvanceRequest(time);
        while (fedamb.isAdvancing) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private void updateAttributeValues(Klient klient) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
        HLAinteger16BE idValue = encoderFactory.createHLAinteger16BE((short) (klient.getId()));
        attributes.put(idHandle, idValue.toByteArray());
        HLAinteger16BE idKolejkiValue = encoderFactory.createHLAinteger16BE((short) (klient.getIdKolejki()));
        attributes.put(idKolejkiHandle, idKolejkiValue.toByteArray());
        HLAinteger16BE priorytetValue = encoderFactory.createHLAinteger16BE((short) (klient.getPriorytet()));
        attributes.put(priorytetHandle, priorytetValue.toByteArray());
        HLAinteger16BE obsluzonyValue = encoderFactory.createHLAinteger16BE((short) (klient.getObsluzony()));
        attributes.put(obsluzonyHandle, obsluzonyValue.toByteArray());
        HLAinteger16BE obslugiwanyValue = encoderFactory.createHLAinteger16BE((short) (klient.getObslugiwany()));
        attributes.put(obslugiwanyHandle, obslugiwanyValue.toByteArray());
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        System.out.println(klient.getKlientHandle() + " " + attributes + " " + generateTag() + " " + time);
        rtiamb.updateAttributeValues(klient.getKlientHandle(), attributes, generateTag(), time);
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
    }


    public void klientUtylizacja(int czasSymulacji) throws RTIexception {
//            for(int i = 0 ; i < listaKlientow.size() ; i++) {
////                if (listaKlientow.get(i).isObsluzony()) {
////                    listaKlientow.remove(i);
////                    log(listaKlientow.get(i).getId() + " usuniety.");
////                }
////
////                if(czasSymulacji == 50){
////                    listaKlientow.get(i).setObsluzony(true);
////                    updateAttributeValues(listaKlientow.get(i));
////                }
//            }
        log(listaKlientow.toString());
    }

    public void rtiNowyKlient() throws Exception {
        log("GenerujeKlienta");
        Klient klient = new Klient();
        klient.setId(maxKlientId + 1);
        maxKlientId = maxKlientId + 1;
        listaKlientow.add(klient);
    }

    public void rtiUpdateKlient(ObjectInstanceHandle klient, int id, int idKolejka, int obsluzony, int obslugiwany, int priorytet) {
        //update klient jesli taki istnieje w kolejce
        int index = -1;
        for (int i = 0; i < listaKlientow.size(); i++)    //znajdz klienta ktory byl updatowany
        {
            if (listaKlientow.get(i).KlientHandle.equals(klient))
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
            }
        }
    }

    private ObjectInstanceHandle registerObject() throws RTIexception {
        return rtiamb.registerObjectInstance(KlientHandle);
    }

    public static void main(String[] args) {
        try {
            new KlientFederate().runFederate("Klient");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
