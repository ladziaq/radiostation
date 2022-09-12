
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
import android.widget.Toast;

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
        cotEvent.setType("a-f-G-U-C-I");
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

        double latitude = mapView.getSelfMarker().getPoint().getLatitude();
        double longitude = mapView.getSelfMarker().getPoint().getLongitude();

        String longAndLat;

        longAndLat = Double.toString(latitude).substring(0,6) + ' ' + Double.toString(longitude).substring(0,7);
        textView.setText(longAndLat);


        /**************************** PRZYCISKI *****************************/


        markButton.setOnClickListener(view -> {
            double lat = Double.parseDouble(latitudeEditText.getText().toString());
            double lon = Double.parseDouble(longitudeEditText.getText().toString());
            if((lat > 90 || lat < -90) || (lon > 180 || lon < -180)){
                Toast toast = Toast.makeText(context, "INPUT LATIDUE BETWEEN -90 TO 90 AND LONGITUDE BETWEEN -180 AND 180", Toast.LENGTH_SHORT);
                toast.show();
            }else {
                CotEvent cotEvent = createPoint();
                cotEvent.setUID("default");

                CotMapComponent.getInternalDispatcher().dispatch(cotEvent);
            }
        });

        newMarkButton.setOnClickListener(view -> {
            double lat = Double.parseDouble(latitudeEditText.getText().toString());
            double lon = Double.parseDouble(longitudeEditText.getText().toString());
            if((lat > 90 || lat < -90) || (lon > 180 || lon < -180)){
                Toast toast = Toast.makeText(context, "INPUT LATIDUE BETWEEN -90 TO 90 AND LONGITUDE BETWEEN -180 AND 180", Toast.LENGTH_LONG);
                toast.show();
            }else {
                CotEvent cotEvent = createPoint();
                markerCounter++;
                uid = "UID " + markerCounter;
                cotEvent.setUID(uid);
                CotMapComponent.getInternalDispatcher().dispatch(cotEvent);
            }

        });
        String text = "192.168.102.1";
        radioLocationButton.setOnClickListener(v -> {
            try {
                new LongRunningTask().execute(text);
            }catch (Exception e){
                e.printStackTrace();
            }
        });


        try {
            new LongRunningTask().execute(text);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static class LongRunningTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... string) {
            Address address = GenericAddress.parse("udp" + ":" + string[0]
                    + "/" + "161");

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(address);
            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(1000);
            target.setRetries(3);

            Snmp snmp = null;
            String textOfResponse = "0";


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
                    textOfResponse = "0";
                } else {

                    for (int i = 0; i < response.size(); i++) {
                        VariableBinding vb = response.get(i);
                        textOfResponse = vb.getVariable().toString();
                        System.out.println(vb.getOid() + " = " + vb.getVariable());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        @SuppressLint("SetTextI18n")
        protected void onPostExecute(String info) {
            if(info.equals("0")){
                radioLattextView.setText("wait");
                radioLontextView.setText("wait");
            }else {
                String lat = info.substring(16, 18) + "." + info.substring(18, 20) + info.charAt(26);
                String lon = info.substring(28, 31) + "." +info.substring(31,33)+ info.charAt(39);
                radioLattextView.setText(lat);
                radioLontextView.setText(lon);

                lat = lat.substring(0,4);
                lon = lon.substring(0,5);
                CotPoint cotPoint = new CotPoint(Double.parseDouble(lat),Double.parseDouble(lon),0.0,2.0,2.0);
                CoordinatedTime time = new CoordinatedTime();
                CotEvent cotEvent = new CotEvent();
                cotEvent.setTime(time);
                cotEvent.setStart(time);
                cotEvent.setHow("h-e");
                cotEvent.setType("a-f-G-U-C-I");
                cotEvent.setStale(time.addMinutes(10));
                cotEvent.setPoint(cotPoint);
                cotEvent.setUID("radiostation");
                CotMapComponent.getInternalDispatcher().dispatch(cotEvent);
                Log.d("myTag", info);
            }
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
