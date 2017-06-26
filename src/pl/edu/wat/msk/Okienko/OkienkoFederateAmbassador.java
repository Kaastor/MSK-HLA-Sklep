package pl.edu.wat.msk.Okienko;

import hla.rti1516e.*;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAinteger16BE;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class OkienkoFederateAmbassador extends NullFederateAmbassador
{

    private OkienkoFederate okienkoFederate;

    // these variables are accessible in the package
    protected double federateTime        = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    public OkienkoFederateAmbassador(OkienkoFederate okienkoFederate)
    {
        this.okienkoFederate = okienkoFederate;
    }

    private void log( String message )
    {
        System.out.println( "OkienkoFederateAmbassador: " + message );
    }

    private short decodeValue(byte[] bytes )
    {
        HLAinteger16BE value = okienkoFederate.encoderFactory.createHLAinteger16BE();
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
        if( label.equals(okienkoFederate.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized( String label, FederateHandleSet failed )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(okienkoFederate.READY_TO_RUN) )
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
        try {
            this.okienkoFederate.rtiNowyKlient(theObject);
        }
        catch (Exception e)
        {}
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
        int id=0,  idKolejka =0, obsluzony=0, obslugiwany=0, priorytet=0, wKolejce = 0;

        StringBuilder builder = new StringBuilder( "Reflection for object:" );
        builder.append( " handle=" + theObject );
        builder.append( ", tag=" + new String(tag) + ", time=" + ((HLAfloat64Time)time).getValue() );

        // print the attribute information
        builder.append( ", attributeCount=" + theAttributes.size() );
        builder.append( "\n" );
        for( AttributeHandle attributeHandle : theAttributes.keySet() )
        {
            // print the attibute handle
            builder.append( "\tattributeHandle=" );

            if( attributeHandle.equals(okienkoFederate.idHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " id:" );
                builder.append( decodeValue(theAttributes.get(attributeHandle)) );

                id = decodeValue(theAttributes.get(attributeHandle));
            }
            else if( attributeHandle.equals(okienkoFederate.idKolejkiHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " idKolejkiHandle:" );
                builder.append( decodeValue(theAttributes.get(attributeHandle)) );

                idKolejka = decodeValue(theAttributes.get(attributeHandle));
            }
            else if( attributeHandle.equals(okienkoFederate.obslugiwanyHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " obslugiwanyHandle:" );
                builder.append( decodeValue(theAttributes.get(attributeHandle)) );

                obslugiwany = decodeValue(theAttributes.get(attributeHandle));
            }
            else if( attributeHandle.equals(okienkoFederate.obsluzonyHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " obsluzonyHandle:" );
                builder.append( decodeValue(theAttributes.get(attributeHandle)) );

                obsluzony = decodeValue(theAttributes.get(attributeHandle));
            }
            else if( attributeHandle.equals(okienkoFederate.priorytetHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " priorytetHandle:" );
                builder.append( decodeValue(theAttributes.get(attributeHandle)) );

                priorytet = decodeValue(theAttributes.get(attributeHandle));
            }
            else if( attributeHandle.equals(okienkoFederate.wKolejceHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " wKolejceHandle:" );
                builder.append( decodeValue(theAttributes.get(attributeHandle)) );

                priorytet = decodeValue(theAttributes.get(attributeHandle));
            }
            else
            {
                builder.append( attributeHandle );
                builder.append( " (Unknown)   " );
            }

            builder.append( "\n" );
        }

        log( builder.toString() );

        this.okienkoFederate.rtiUpdateKlient(theObject, id, idKolejka, obsluzony, obslugiwany, priorytet, wKolejce);
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
            throws FederateInternalError
    {
        //odebranie interakcji od gui o info
        StringBuilder builder = new StringBuilder( "Interaction Received:" );

        builder.append( " handle=" + interactionClass );
       if( interactionClass.equals(okienkoFederate.koniecSymulacjiHandle) )
        {
            builder.append( " (koniecSymulacji)" );
            this.okienkoFederate.endSim();
        }

        builder.append( ", tag=" + new String(tag) + ", time=" + ((HLAfloat64Time)time).getValue() );

        log( builder.toString() );
    }
}