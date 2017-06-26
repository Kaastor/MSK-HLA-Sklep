package pl.edu.wat.msk.Gui;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import pl.edu.wat.msk.GUI;
import pl.edu.wat.msk.objects.Gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class GuiFederate {
    public static final int ITERATIONS = 1000000;
    public static final String READY_TO_RUN = "ReadyToRun";
    public static int timer;
    public static boolean start;
    private RTIambassador rtiamb;
    private GuiFederateAmbassador fedamb;
    private HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    protected InteractionClassHandle koniecSymulacjiHandle;

    protected ObjectClassHandle GuiHandle;
    protected AttributeHandle czasObslugiHandle;
    protected AttributeHandle liczbaNaplywajacychKlientowHandle;
    protected AttributeHandle okresCzasuNaplywuHandle;
    protected AttributeHandle liczbaOkienekHandle;

    public Gui gui;

    public int simTime;
    public static boolean zakonczSymulacje = false;

    private void log( String message )
    {
        System.out.println( fedamb.federateTime + " GuiFederate: " + message );
    }

    private void waitForUser()
    {
        log( " >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" );
        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
        try
        {
            reader.readLine();
        }
        catch( Exception e )
        {
            log( "Error while waiting for user input: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void createFederation(String federateName) throws Exception{
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
        fedamb = new GuiFederateAmbassador( this );
        rtiamb.connect( fedamb, CallbackModel.HLA_EVOKED );
        log( "Creating Federation..." );
        try
        {
            URL[] modules = new URL[]{
                    (new File("fom.xml")).toURI().toURL()
            };

            rtiamb.createFederationExecution( "federation", modules );
            log( "Created Federation " );
        }
        catch( FederationExecutionAlreadyExists exists )
        {
            log( "Didn't create federation, it already existed" );
        }
        catch( MalformedURLException urle )
        {
            log( "Exception loading one of the FOM modules from disk: " + urle.getMessage() );
            urle.printStackTrace();
            return;
        }

        rtiamb.joinFederationExecution( federateName, "GuiFederate",	"federation" );
        log( "Joined Federation as " + federateName );

    }

    public void runFederate( String federateName ) throws Exception
    {
        createFederation(federateName);
        this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();
        rtiamb.registerFederationSynchronizationPoint( READY_TO_RUN, null );
        while( fedamb.isAnnounced == false )
        {
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }

        GUI.run(this);

        waitForUser();

        rtiamb.synchronizationPointAchieved( READY_TO_RUN );
        log( "Achieved sync point: " + READY_TO_RUN + ", waiting for federation..." );

        gui = new Gui(Integer.valueOf(GUI.czasObslugiText.getText()),
                Integer.valueOf(GUI.liczbaKlientowText.getText()),
                Integer.valueOf(GUI.okresCzasuNaplywuText.getText()),
                Integer.valueOf(GUI.liczbaOkienekText.getText()));

        while( fedamb.isReadyToRun == false )
        {
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }

        enableTimePolicy();
        log( "Time Policy Enabled" );
        publishAndSubscribe();
        log( "Published and Subscribed" );

        for (timer = 0; timer < ITERATIONS; timer++) {
                simTime = timer;
                symulacja();
                advanceTime(1.0);
        }
        resign();
    }

    public void resign() throws Exception{
        rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
        log( "Resigned from Federation" );

        try
        {
            rtiamb.destroyFederationExecution( "federation" );
            log( "Destroyed Federation" );
        }
        catch( FederationExecutionDoesNotExist dne )
        {
            log( "No need to destroy federation, it doesn't exist" );
        }
        catch( FederatesCurrentlyJoined fcj )
        {
            log( "Didn't destroy federation, federates still joined" );
        }
    }

    private void publishAndSubscribe() throws RTIexception
    {
        this.GuiHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Gui");
        this.czasObslugiHandle = rtiamb.getAttributeHandle(this.GuiHandle, "czasObslugi");
        this.liczbaNaplywajacychKlientowHandle = rtiamb.getAttributeHandle(this.GuiHandle, "liczbaNaplywajacychKlientow");
        this.okresCzasuNaplywuHandle = rtiamb.getAttributeHandle(this.GuiHandle, "okresCzasuNaplywu");
        this.liczbaOkienekHandle = rtiamb.getAttributeHandle(this.GuiHandle, "liczbaOkienek");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(this.czasObslugiHandle);
        attributes.add(this.liczbaNaplywajacychKlientowHandle);
        attributes.add(this.okresCzasuNaplywuHandle);
        attributes.add(this.liczbaOkienekHandle);
        rtiamb.publishObjectClassAttributes(GuiHandle, attributes);

        this.koniecSymulacjiHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.koniecSymulacji" );
        rtiamb.publishInteractionClass(koniecSymulacjiHandle);

    }

    public void sendInteraction(String type) throws RTIexception
    {
        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );
        if(type.equals("koniecSymulacji"))
        {
            log("Wysylam symulacja");
            ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
            rtiamb.sendInteraction( koniecSymulacjiHandle, parameters, generateTag(), time );
        }
    }

    private void updateAttributeValues(Gui gui) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
        HLAinteger16BE czasObslugiValue = encoderFactory.createHLAinteger16BE((short) (gui.getCzasObslugi()));
        attributes.put(czasObslugiHandle, czasObslugiValue.toByteArray());
        HLAinteger16BE liczbaNaplywajacychKlientowValue = encoderFactory.createHLAinteger16BE((short) (gui.getLiczbaNaplywajacychKlientow()));
        attributes.put(liczbaNaplywajacychKlientowHandle, liczbaNaplywajacychKlientowValue.toByteArray());
        HLAinteger16BE liczbaOkienekValue = encoderFactory.createHLAinteger16BE((short) (gui.getLiczbaOkienek()));
        attributes.put(liczbaOkienekHandle, liczbaOkienekValue.toByteArray());
        HLAinteger16BE okresCzasuNaplywuValue = encoderFactory.createHLAinteger16BE((short) (gui.getOkresCzasuNaplywu()));
        attributes.put(okresCzasuNaplywuHandle, okresCzasuNaplywuValue.toByteArray());

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        log("Nowe wartosci GUI.");
        rtiamb.updateAttributeValues(gui.getGuiHandle(), attributes, generateTag(), time);
    }

    public void advanceTime( double timeStep ) throws RTIexception
    {
        fedamb.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + timeStep );
        rtiamb.timeAdvanceRequest( time );
        while( fedamb.isAdvancing ){
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }
        log("Time Advanced to " + fedamb.federateTime);
    }

    private void enableTimePolicy() throws Exception
    {
        HLAfloat64Interval lookahead = timeFactory.makeInterval( fedamb.federateLookahead );
        this.rtiamb.enableTimeRegulation( lookahead );
        while( fedamb.isRegulating == false )
        {
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }
        this.rtiamb.enableTimeConstrained();
        while( fedamb.isConstrained == false )
        {
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }
    }

    private byte[] generateTag()
    {
        return ("(timestamp) "+System.currentTimeMillis()).getBytes();
    }

    public void endSim()
    {
        timer = ITERATIONS;
    }

    private void symulacja() throws Exception{
        if (gui.getCzasUtworzenia() <= simTime && gui.getGuiHandle() == null) {
            gui.setGuiHandle(registerObject());
            updateAttributeValues(gui);
        }

        if(zakonczSymulacje){
            sendInteraction("koniecSymulacji");
            endSim();
        }
    }

    private ObjectInstanceHandle registerObject() throws RTIexception {
        return rtiamb.registerObjectInstance(GuiHandle);
    }

    public static void main( String[] args )
    {
        try
        {
            new GuiFederate().runFederate( "GUI" );
        }
        catch( Exception rtie )
        {
            rtie.printStackTrace();
        }
    }
}
