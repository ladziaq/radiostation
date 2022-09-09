
package com.atakmap.android.plugintemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.plugintemplate.plugin.R;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;

import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.time.CoordinatedTime;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class PluginTemplateDropDownReceiver extends DropDownReceiver implements
        OnStateListener {

    public static final String TAG = PluginTemplateDropDownReceiver.class
            .getSimpleName();

    public static final String SHOW_PLUGIN = "com.atakmap.android.plugintemplate.SHOW_PLUGIN";
    private final Context pluginContext;
    private final View myFragmentView;
    private final Button markButton;
    private final Button newMarkButton;
    private final Button radioLocationButton;
    private final TextView textView;
    @SuppressLint("StaticFieldLeak")
    private static TextView radioLattextView;
    @SuppressLint("StaticFieldLeak")
    private static TextView radioLontextView;
    private final EditText latitudeEditText;
    private final EditText longitudeEditText;
    private static int markerCounter = 0;
    private  String uid;

    public CotEvent createPoint(){

        String lat = latitudeEditText.getText().toString();
        String lon = longitudeEditText.getText().toString();

        if(lat.equals("")  || lon.equals("")){
            lat = "0";
            lon = "0";
        }

        CotPoint cotPoint = new CotPoint(Double.parseDouble(lat),Double.parseDouble(lon),0.0,2.0,2.0);
        CotEvent cotEvent = new CotEvent();
        CoordinatedTime time = new CoordinatedTime();

        cotEvent.setTime(time);
        cotEvent.setStart(time);
        cotEvent.setHow("h-e");
        cotEvent.setType("a-n-G-U-C-I"); //a-n-G-U-C-I
        cotEvent.setStale(time.addMinutes(10));
        cotEvent.setPoint(cotPoint);

        return cotEvent;
    }
    @SuppressLint("SetTextI18n")
    public PluginTemplateDropDownReceiver(final MapView mapView,
                                          final Context context) {
        super(mapView);
        this.pluginContext = context;

        // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
        // In this case, using it is not necessary - but I am putting it here to remind
        // developers to look at this Inflator

        myFragmentView = PluginLayoutInflater.inflate(pluginContext, R.layout.radiostation_layout, null);
        textView = myFragmentView.findViewById(R.id.longLatTextView);
        markButton = myFragmentView.findViewById(R.id.markButton);
        newMarkButton = myFragmentView.findViewById(R.id.newMarkButton);
        radioLocationButton = myFragmentView.findViewById(R.id.radioStationButton);

        latitudeEditText = myFragmentView.findViewById(R.id.latitude_editText);
        longitudeEditText = myFragmentView.findViewById(R.id.longitude_editText);
        radioLattextView = myFragmentView.findViewById(R.id.lat_textView);
        radioLontextView = myFragmentView.findViewById(R.id.lon_textView);


        /**************************** USTALANIE POŁOŻENIA *****************************/

        Double latitude = mapView.getSelfMarker().getPoint().getLatitude();
        Double longitude = mapView.getSelfMarker().getPoint().getLongitude();

        String longAndLat;

        longAndLat = latitude.toString().substring(0,6) + ' ' + longitude.toString().substring(0,7);
        textView.setText(longAndLat);


        /**************************** PRZYCISKI *****************************/
        markButton.setOnClickListener(view -> {
            CotEvent cotEvent = createPoint();
            cotEvent.setUID("default");
            CotMapComponent.getInternalDispatcher().dispatch(cotEvent);
        });

        newMarkButton.setOnClickListener(view -> {
            CotEvent cotEvent = createPoint();
            markerCounter++;
            uid = "UID " + markerCounter;
            cotEvent.setUID(uid);
            CotMapComponent.getInternalDispatcher().dispatch(cotEvent);
        });

        radioLocationButton.setOnClickListener(view -> {

            try {
                new LongRunningTask().execute();
            }catch (Exception e){
                e.printStackTrace();
            }

        });


    }

    private static class LongRunningTask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... string) {
            Address address = GenericAddress.parse("udp" + ":" + "192.168.102.1"
                    + "/" + "161");

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(address);
            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(1000);
            target.setRetries(3);

            Snmp snmp = null;
            String textOfResponse = "";


            try {
                PDU pdu = new PDU();

                pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.4045.61005681.20.2.1.0")));

                DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();

                snmp = new Snmp(transport);

                snmp.listen();

                pdu.setType(PDU.GET);
                ResponseEvent respEvent = snmp.send(pdu, target);

                PDU response = respEvent.getResponse();


                if (response == null) {
                    textOfResponse = "empty response";
                } else {

                    for (int i = 0; i < response.size(); i++) {
                        VariableBinding vb = response.get(i);
                        textOfResponse = vb.getVariable().toString();
                        System.out.println(vb.getOid() + " = " + vb.getVariable());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                textOfResponse = "no connection";
            } finally {
                if (snmp != null) {
                    try {
                        snmp.close();
                    } catch (IOException ex1) {
                        snmp = null;
                    }
                }

            }
            return textOfResponse;
        }
        protected void onPostExecute(String info) {
            String lat = info.substring(16,27);
            String lon = info.substring(28,40);
            radioLattextView.setText(lat);
            radioLontextView.setText(lon);
            Log.d("myTag", info);
        }

    }

    /**************************** PUBLIC METHODS *****************************/

    public void disposeImpl() {
    }

    /**************************** INHERITED METHODS *****************************/

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if (action == null)
            return;

        if (action.equals(SHOW_PLUGIN)) {

            Log.d(TAG, "showing plugin drop down");
            showDropDown(myFragmentView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                    HALF_HEIGHT, false, this);
        }
    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
    }

    @Override
    public void onDropDownClose() {
    }

}
