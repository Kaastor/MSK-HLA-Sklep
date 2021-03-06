package pl.edu.wat.msk.Klient;


import hla.rti1516e.*;
import hla.rti1516e.encoding.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

public class KlientFederateAmbassador extends NullFederateAmbassador
{

    private KlientFederate klientFederate;

    // these variables are accessible in the package
    protected double federateTime        = 0.0;
    protected double federateLookahead   = 1.0;

    protected boolean isRegulating       = false;
    protected boolean isConstrained      = false;
    protected boolean isAdvancing        = false;

    protected boolean isAnnounced        = false;
    protected boolean isReadyToRun       = false;

    public KlientFederateAmbassador(KlientFederate klientFederate)
    {
        this.klientFederate = klientFederate;
    }

    private void log( String message )
    {
        System.out.println( "KlientFederateAmbassador: " + message );
    }

    private short decodeValue(byte[] bytes )
    {
        HLAinteger16BE value = klientFederate.encoderFactory.createHLAinteger16BE();
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

    protected int decodeValueInt(byte[] bytes )
    {
        HLAinteger32BE value = klientFederate.encoderFactory.createHLAinteger32BE();
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
        if( label.equals(klientFederate.READY_TO_RUN) )
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized( String label, FederateHandleSet failed )
    {
        log( "Federation Synchronized: " + label );
        if( label.equals(klientFederate.READY_TO_RUN) )
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
            this.klientFederate.rtiNoweGui(theObject);
        }
        catch (Exception e) {}
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
        int liczbaOkienek=0;

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

            if( attributeHandle.equals(klientFederate.liczbaOkienekHandle) )
            {
                builder.append( attributeHandle );
                builder.append( " id:" );
                builder.append( decodeValue(theAttributes.get(attributeHandle)) );

                liczbaOkienek = decodeValue(theAttributes.get(attributeHandle));
            }
            else
            {
                builder.append( attributeHandle );
                builder.append( " (Unknown)   " );
            }

            builder.append( "\n" );
        }

        log( builder.toString() );
        this.klientFederate.rtiUpdateGui(theObject, liczbaOkienek);
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
        int klientId = -1;
        builder.append( " handle=" + interactionClass );
        if( interactionClass.equals(klientFederate.generujKlientaHandle) )
        {
            builder.append( " (generujKlienta)" );
            try {
                this.klientFederate.rtiNowyKlient();
            }
            catch (Exception e){}
            builder.append( ", tag=" + new String(tag) + ", time=" + ((HLAfloat64Time)time).getValue() );
        }
        else if( interactionClass.equals(klientFederate.koniecSymulacjiHandle) )
        {
            builder.append( " (koniecSymulacji)" );
            this.klientFederate.endSim();
            builder.append( ", tag=" + new String(tag) + ", time=" + ((HLAfloat64Time)time).getValue() );
            log("Koniec Symulacji -" + builder.toString() );
        }
        else if( interactionClass.equals(klientFederate.klientObsluzonyHandle) )
        {
            builder.append( " (klientObsluzony)" );
            byte[] bajty = theParameters.get(klientFederate.idObsluzonegoKlientaHandle);
            int liczbaBajtow = bajty.length/4 -1;
            HLAinteger32BE[] idObsluzonegoKlienta = new HLAinteger32BE[liczbaBajtow];

            for(int i = 0;i < liczbaBajtow ; i++)
                idObsluzonegoKlienta[i] = klientFederate.encoderFactory.createHLAinteger32BE();
            HLAfixedArray<HLAinteger32BE> values = klientFederate.encoderFactory.createHLAfixedArray(idObsluzonegoKlienta);
            try {
                values.decode( bajty );
                for( int i = 0 ; i < liczbaBajtow ; ++i )
                {
                    builder.append(values.get( i ).getValue()+", ");
                    klientId = values.get( i ).getValue();
                }
            } catch (DecoderException e) { e.printStackTrace();  }
            this.klientFederate.utylizacjaKlienta(klientId);
            builder.append( ", tag=" + new String(tag) + ", time=" + ((HLAfloat64Time)time).getValue() );
        }
    }
}
