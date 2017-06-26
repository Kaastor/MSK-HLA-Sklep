package pl.edu.wat.msk.Gui;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;
import pl.edu.wat.msk.GUI;
import pl.edu.wat.msk.Sklep.SklepFederate;
import pl.edu.wat.msk.Sklep.SklepFederateAmbassador;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Pawel on 2017-06-26.
 */
public class GuiFederate {
    public static final int ITERATIONS = 1000000;
    public static final String READY_TO_RUN = "ReadyToRun";
    public static int timer;

    private RTIambassador rtiamb;
    private GuiFederateAmbassador fedamb;
    private HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    protected InteractionClassHandle koniecSymulacjiHandle;

    protected InteractionClassHandle daneSymulacjiHandle;
    protected ParameterHandle czasObslugiHandle;
    protected ParameterHandle liczbaNaplywajacychKlientowHandle;
    protected ParameterHandle okresCzasuNaplywuHandle;
    protected ParameterHandle liczbaOkienekHandle;



    private void log( String message )
    {
        System.out.println( "GuiFederate: " + message );
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
        log( "Creating RTIambassador..." );
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
        log( "Connecting..." );
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
        log( "Creating RTIambassador..." );
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
        log( "Connecting..." );
        fedamb = new GuiFederateAmbassador( this );
        rtiamb.connect( fedamb, CallbackModel.HLA_EVOKED );
        log( "Creating Federation..." );
        try
        {
            URL[] modules = new URL[]{
                    (new File("fom.xml")).toURI().toURL()
            };

            rtiamb.createFederationExecution( "federation", modules );
            log( "Created Federation" );
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

        this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();

        rtiamb.registerFederationSynchronizationPoint( READY_TO_RUN, null );
        while( fedamb.isAnnounced == false )
        {
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }
        waitForUser();
        rtiamb.synchronizationPointAchieved( READY_TO_RUN );
        log( "Achieved sync point: " + READY_TO_RUN + ", waiting for federation..." );
        while( fedamb.isReadyToRun == false )
        {
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }
        enableTimePolicy();
        log( "Time Policy Enabled" );
        //////////////////////////////////////////////////////////////////////////////

        publishAndSubscribe();
        log( "Published and Subscribed" );

        GUI.run(this);
    }

    public void resign() throws Exception{
        rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
        log( "Resigned from Federation" );

        try
        {
            rtiamb.destroyFederationExecution( "MSKfed" );
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
        this.koniecSymulacjiHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.koniecSymulacji" );
        rtiamb.publishInteractionClass(koniecSymulacjiHandle);

        this.daneSymulacjiHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.daneSymulacji");
        rtiamb.publishInteractionClass(daneSymulacjiHandle);

        czasObslugiHandle = rtiamb.getParameterHandle(this.daneSymulacjiHandle, "czasObslugi");
        liczbaNaplywajacychKlientowHandle = rtiamb.getParameterHandle(this.daneSymulacjiHandle, "liczbaNaplywajacychKlientow");
        okresCzasuNaplywuHandle = rtiamb.getParameterHandle(this.daneSymulacjiHandle, "okresCzasuNaplywu");
        liczbaOkienekHandle = rtiamb.getParameterHandle(this.daneSymulacjiHandle, "liczbaOkienek");


    }

    public void sendInteraction(String type) throws RTIexception
    {
        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );

        if(type.equals("koniecSymulacji"))
        {
            log("Wysylam koniecSymulacji");
            ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
            rtiamb.sendInteraction( koniecSymulacjiHandle, parameters, generateTag(), time );
        }
    }

    public void sendStats (float czas, int naplywajacy, float okres, int okienka) throws RTIexception{
        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );

            ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(4);
            HLAfloat64BE czasObslugi = encoderFactory.createHLAfloat64BE( czas );
            HLAinteger32BE liczbaNaplywajacych = encoderFactory.createHLAinteger32BE( naplywajacy);
            HLAfloat64BE okresCzasuNaplywu = encoderFactory.createHLAfloat64BE( okres);
            HLAinteger32BE liczbaOkienek = encoderFactory.createHLAinteger32BE( okienka);

        parameters.put(czasObslugiHandle, czasObslugi.toByteArray());
        parameters.put(liczbaNaplywajacychKlientowHandle, liczbaNaplywajacych.toByteArray());
        parameters.put(okresCzasuNaplywuHandle, okresCzasuNaplywu.toByteArray());
        parameters.put(liczbaOkienekHandle, liczbaOkienek.toByteArray());

        log("sending configuration");

        rtiamb.sendInteraction(daneSymulacjiHandle, parameters,generateTag(),time);
    }

    public void advanceTime( double timeStep ) throws RTIexception
    {
        fedamb.isAdvancing = true;
        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime + timeStep );
        rtiamb.timeAdvanceRequest( time );
        while( fedamb.isAdvancing )
        {
            rtiamb.evokeMultipleCallbacks( 0.1, 0.2 );
        }
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
