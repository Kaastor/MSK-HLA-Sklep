package pl.edu.wat.msk.Sklep;


import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import pl.edu.wat.msk.objects.Gui;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class SklepFederate {

    public static final int ITERATIONS = 1000000;
    public static final String READY_TO_RUN = "ReadyToRun";
    public static int timer;

    private int nastepnaGeneracja = 0;
    private int okresGeneracji = 1;
    private int generowanaLiczbaKlientow = 1;

    private RTIambassador rtiamb;
    private SklepFederateAmbassador fedamb;
    private HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    protected InteractionClassHandle koniecSymulacjiHandle;
    protected InteractionClassHandle generujKlientaHandle;

    protected ObjectClassHandle GuiHandle;
    protected AttributeHandle liczbaNaplywajacychKlientowAttHandle;
    protected AttributeHandle okresCzasuNaplywuHandle;

    public int simTime;

    private Gui gui;

    private void log(String message) {
        System.out.println(fedamb.federateTime + " SklepFederate: " + message);
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
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
        fedamb = new SklepFederateAmbassador(this);
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

        rtiamb.joinFederationExecution(federateName, "SklepFederate", "federation");
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

        //musi wiedziec o istnieniu obiektu/interakcji

        for (timer = 0; timer < ITERATIONS; timer++) {
                simTime = timer;
                generujKlientow(timer);
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
        koniecSymulacjiHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.koniecSymulacji");
        rtiamb.subscribeInteractionClass(koniecSymulacjiHandle);

        generujKlientaHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.generujKlienta");
        rtiamb.publishInteractionClass(generujKlientaHandle);

        this.GuiHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Gui");
        this.liczbaNaplywajacychKlientowAttHandle = rtiamb.getAttributeHandle(this.GuiHandle, "liczbaNaplywajacychKlientow");
        this.okresCzasuNaplywuHandle = rtiamb.getAttributeHandle(this.GuiHandle, "okresCzasuNaplywu");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(this.liczbaNaplywajacychKlientowAttHandle);
        attributes.add(this.okresCzasuNaplywuHandle);
        rtiamb.subscribeObjectClassAttributes(GuiHandle, attributes);

    }

    private void sendInteraction(String type) throws RTIexception {
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        if (type.equals("generujKlienta")) {
            ParameterHandleValueMap parameters1 = rtiamb.getParameterHandleValueMapFactory().create(0);
            rtiamb.sendInteraction(generujKlientaHandle, parameters1, generateTag(), time);
        }
        if (type.equals("koniecSymulacji")) {
            ParameterHandleValueMap parameters4 = rtiamb.getParameterHandleValueMapFactory().create(0);
            rtiamb.sendInteraction(koniecSymulacjiHandle, parameters4, generateTag(), time);
        }
    }

    private void advanceTime(double timeStep) throws RTIexception {
        fedamb.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timeStep);
        rtiamb.timeAdvanceRequest(time);
        while (fedamb.isAdvancing) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
        log("Time Advanced to " + fedamb.federateTime);
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

    public void rtiNoweGui(ObjectInstanceHandle guiHandle) throws Exception
    {
        gui= new Gui(guiHandle);
    }

    public void rtiUpdateGui(ObjectInstanceHandle klient, int liczbaNaplywajacychKlientow, int okresCzasuNaplywu) {
        gui.setLiczbaNaplywajacychKlientow(liczbaNaplywajacychKlientow);
        gui.setOkresCzasuNaplywu(okresCzasuNaplywu);
        okresGeneracji = okresCzasuNaplywu;
        generowanaLiczbaKlientow = liczbaNaplywajacychKlientow;
        nastepnaGeneracja += okresGeneracji;
        log("Update: liczbaNaplywajacychKlientow" + liczbaNaplywajacychKlientow + ",  okresCzasuNaplywu: " + okresCzasuNaplywu);
    }

    public void generujKlientow(int czasSymulacji) throws RTIexception {
        if (nastepnaGeneracja == czasSymulacji) {
            for (int i = 0; i < generowanaLiczbaKlientow; i++) {
                sendInteraction("generujKlienta");
            }
            nastepnaGeneracja += okresGeneracji;
        }
    }

    public static void main(String[] args) {
        try {
            new SklepFederate().runFederate("Sklep");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
