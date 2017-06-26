package pl.edu.wat.msk.Statystyka;

import hla.rti.jlc.EncodingHelpers;
import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;
import pl.edu.wat.msk.Sklep.SklepFederate;

/**
 * Created by Pawel on 2017-06-26.
 */
public class StatystykaFederateAmbassador  extends NullFederateAmbassador {

    private StatystykaFederate statystykaFederate;

    // these variables are accessible in the package
    protected double federateTime        = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    public StatystykaFederateAmbassador( StatystykaFederate statystykaFederate )
    {
        this.statystykaFederate = statystykaFederate;
    }

    private void log( String message )
    {
        System.out.println( "StatytykaFederateAmbassador: " + message );
    }

    private short decodeValue(byte[] bytes )
    {
        HLAinteger16BE value = statystykaFederate.encoderFactory.createHLAinteger16BE();
        try
        {
            value.decode( bytes );
            return value.getValue();
        }
        catch( DecoderException de )
        {
            de.printStackTrace();
            return 0;
        }
    }

    @Override
    public void synchronizationPointRegistrationFailed( String label, SynchronizationPointFailureReason reason )
    {
        log( "Failed to register sync point: " + label + ", reason="+reason );
    }

    @Override
    public void synchronizationPointRegistrationSucceeded( String label )
    {
        log( "Successfully registered sync point: " + label );
    }

    @Override
    public void announceSynchronizationPoint( String label, byte[] tag )
    {
        log( "Synchronization point announced: " + label );
        if( label.equals(statystykaFederate.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized( String label, FederateHandleSet failed )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(statystykaFederate.READY_TO_RUN) )
            this.isReadyToRun = true;
    }

    @Override
    public void timeRegulationEnabled( LogicalTime time )
    {
        this.federateTime = ((HLAfloat64Time)time).getValue();
        this.isRegulating = true;
    }

    @Override
    public void timeConstrainedEnabled( LogicalTime time )
    {
        this.federateTime = ((HLAfloat64Time)time).getValue();
        this.isConstrained = true;
    }

    @Override
    public void timeAdvanceGrant( LogicalTime time )
    {
        this.federateTime = ((HLAfloat64Time)time).getValue();
        this.isAdvancing = false;
    }

    @Override
    public void discoverObjectInstance( ObjectInstanceHandle theObject,
                                        ObjectClassHandle theObjectClass,
                                        String objectName ) throws FederateInternalError
    {
        log( "Discoverd Object: handle=" + theObject + ", classHandle=" + theObjectClass + ", name=" + objectName );
        //co jak nowy obj
    }

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrder,
                                        TransportationTypeHandle transport,
                                        SupplementalReflectInfo reflectInfo )
            throws FederateInternalError
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        reflectAttributeValues( theObject,
                theAttributes,
                tag,
                sentOrder,
                transport,
                null,
                sentOrder,
                reflectInfo );
    }

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        LogicalTime time,
                                        OrderType receivedOrdering,
                                        SupplementalReflectInfo reflectInfo )
            throws FederateInternalError
    {
        //na nikogo nie subuje
    }

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass,
                                    ParameterHandleValueMap theParameters,
                                    byte[] tag,
                                    OrderType sentOrdering,
                                    TransportationTypeHandle theTransport,
                                    SupplementalReceiveInfo receiveInfo )
            throws FederateInternalError
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        this.receiveInteraction( interactionClass,
                theParameters,
                tag,
                sentOrdering,
                theTransport,
                null,
                sentOrdering,
                receiveInfo );
    }

    @Override
    public void receiveInteraction( InteractionClassHandle interactionClass,
                                    ParameterHandleValueMap theParameters,
                                    byte[] tag,
                                    OrderType sentOrdering,
                                    TransportationTypeHandle theTransport,
                                    LogicalTime time,
                                    OrderType receivedOrdering,
                                    SupplementalReceiveInfo receiveInfo )
            throws FederateInternalError {
        //odebranie interakcji od gui o info
        StringBuilder builder = new StringBuilder("Interaction Received:");

        builder.append(" handle=" + interactionClass);


        if( interactionClass.equals(statystykaFederate.koniecSymulacjiHandle) )
        {
            System.out.println("Po co mi koniec symulacji? XD");
        }
        if ( interactionClass.equals(statystykaFederate.wyslijWynikiHandle))
        {
                int liczbaKlientow = EncodingHelpers.decodeInt(theParameters.get(statystykaFederate.liczbaKlientowHandle));
                int liczbaObsluzonych = EncodingHelpers.decodeInt(theParameters.get(statystykaFederate.liczbaObsluzonychHandle));

                statystykaFederate.stats.setStats(liczbaKlientow, liczbaObsluzonych);

            System.out.println("Klienci: " + liczbaKlientow);
            System.out.println("Obsluzeni: " + liczbaObsluzonych);
        }
//        if( interactionClass.equals(federate.daneStartowe) )
//        {
//            builder.append( " (daneStartowe)" );
//            this.federate.rti_otworz_kase(((HLAfloat64Time)time).getValue());
//        }
//        else
//         if( interactionClass.equals(sklepFederate.generujKlienta) )
//                {
//                    builder.append( " (generujKlienta)" );
//                    this.sklepFederate.rti_zamknij_kase();
//                }
//        else if( interactionClass.equals(federate.koniecSymulacjiHandle) )
//        {
//            builder.append( " (koniecSymulacjiHandle)" );
//            this.federate.endSim();
//        }

        builder.append(", tag=" + new String(tag) + ", time=" + ((HLAfloat64Time) time).getValue());

        log(builder.toString());
    }}
