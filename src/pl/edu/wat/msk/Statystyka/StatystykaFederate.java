package pl.edu.wat.msk.Statystyka;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
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
import pl.edu.wat.msk.Gui.GuiFederate;
import pl.edu.wat.msk.Gui.GuiFederateAmbassador;
import pl.edu.wat.msk.Stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Pawel on 2017-06-26.
 */
public class StatystykaFederate {

        public static final int ITERATIONS = 1000000;
        public static final String READY_TO_RUN = "ReadyToRun";
        public static int timer;

        private RTIambassador rtiamb;
        private StatystykaFederateAmbassador fedamb;
        private HLAfloat64TimeFactory timeFactory;
        protected EncoderFactory encoderFactory;

        protected InteractionClassHandle koniecSymulacjiHandle;

        protected InteractionClassHandle daneSymulacjiHandle;
        protected ParameterHandle czasObslugiHandle;
        protected ParameterHandle liczbaNaplywajacychKlientowHandle;
        protected ParameterHandle okresCzasuNaplywuHandle;
        protected ParameterHandle liczbaOkienekHandle;

        protected InteractionClassHandle wyslijWynikiHandle;
        protected ParameterHandle liczbaKlientowHandle;
        protected ParameterHandle liczbaObsluzonychHandle;

        public Stats stats;



    private void log( String message )
    {
        System.out.println( "StatystykaFederate: " + message );
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
        fedamb = new StatystykaFederateAmbassador( this );
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

        rtiamb.joinFederationExecution( federateName, "StatystykaFederate",	"federation" );
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

        //stats.run(this);
        this.stats = new Stats();

        for (timer = 0; timer < ITERATIONS; timer++) {

            advanceTime(1.0);
        }



        for (timer = 0; timer < ITERATIONS; timer++) {

            advanceTime(1.0);
        }
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
        koniecSymulacjiHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.koniecSymulacji" );
        rtiamb.subscribeInteractionClass(koniecSymulacjiHandle);

        wyslijWynikiHandle = rtiamb.getInteractionClassHandle("HLAinteractionRoot.wyslijWyniki" );
        liczbaKlientowHandle = rtiamb.getParameterHandle(wyslijWynikiHandle, "liczbaKlientow");
        liczbaObsluzonychHandle = rtiamb.getParameterHandle(wyslijWynikiHandle, "liczbaObsluzonych");
        rtiamb.subscribeInteractionClass(wyslijWynikiHandle);

    }


    public void sendInteraction(String type) throws RTIexception
    {
        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );
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
            new StatystykaFederate().runFederate( "Statystyka" );
        }
        catch( Exception rtie )
        {
            rtie.printStackTrace();
        }
    }
}
