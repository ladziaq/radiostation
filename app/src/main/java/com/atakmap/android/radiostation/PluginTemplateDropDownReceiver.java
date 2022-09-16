
package com.atakmap.android.radiostation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.radiostation.plugin.R;
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
    private final Button radioLocationButton;
    @SuppressLint("StaticFieldLeak")
    private static TextView radioLattextView;
    @SuppressLint("StaticFieldLeak")
    private static TextView radioLontextView;
    private final EditText latitudeEditText;
    private final EditText longitudeEditText;
    private final Button markButton;
    private final Button newMarkButton;
    private final Button addRadiostationButton;
    private final Button modifyAddrButton;
    private final RadioButton radio_custom_val;
    private final RadioGroup radioGroup;
    private final RadioButton radio0,radio1;
    private final EditText ipaddr1;
    private final EditText ipaddr2;
    private final EditText ipaddr3;
    private final EditText ipaddr4;
    private static int markerCounter = 0;
    private  String uid;
    String oktet1;
    String oktet2;
    String oktet3;
    String oktet4;
    private static boolean  isChecked = false;
    private static boolean  isCheckedRadio0 = false;
    private static boolean  isCheckedRadio1 = false;
    private static boolean  isCheckedRadio2 = false;
    private static boolean  isCheckedRadio3 = false;

    String[] adreses = {"198.168.102.1" ,"127.0.0.1"};




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
        cotEvent.setType("a-f-G-U-C-I"); ///////
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
        radioLocationButton = myFragmentView.findViewById(R.id.radioStationButton);

        latitudeEditText = myFragmentView.findViewById(R.id.latitude_editText);
        longitudeEditText = myFragmentView.findViewById(R.id.longitude_editText);
        markButton = myFragmentView.findViewById(R.id.markButton);
        newMarkButton = myFragmentView.findViewById(R.id.newMarkButton);
        addRadiostationButton = myFragmentView.findViewById(R.id.addRadiostationButton);
        modifyAddrButton = myFragmentView.findViewById(R.id.modifyAddrButton);
        radioGroup = myFragmentView.findViewById(R.id.radio_group);

        radio0 = myFragmentView.findViewById(R.id.radio_0);
        radio1 = myFragmentView.findViewById(R.id.radio_1);
        radio_custom_val = myFragmentView.findViewById(R.id.radio_custom_val);


        radioLattextView = myFragmentView.findViewById(R.id.lat_textView);
        radioLontextView = myFragmentView.findViewById(R.id.lon_textView);
        ipaddr1 = myFragmentView.findViewById(R.id.ipAddr1);
        ipaddr2 = myFragmentView.findViewById(R.id.ipAddr2);
        ipaddr3 = myFragmentView.findViewById(R.id.ipAddr3);
        ipaddr4 = myFragmentView.findViewById(R.id.ipAddr4);


        ipaddr1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(ipaddr1.getText().toString().length()==3)
                {
                    ipaddr2.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ipaddr2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(ipaddr2.getText().toString().length()==3)
                {
                    ipaddr3.requestFocus();
                }
                if(ipaddr2.getText().toString().length()==0)
                {
                    ipaddr1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ipaddr3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(ipaddr3.getText().toString().length()==3)
                {
                    ipaddr4.requestFocus();
                }
                if(ipaddr3.getText().toString().length()==0)
                {
                    ipaddr2.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ipaddr4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(ipaddr4.getText().toString().length()==0)
                {
                    ipaddr3.requestFocus();
                }

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });


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

        radio0.setOnClickListener(v -> {
            radio_custom_val.setChecked(false);
            isChecked = false;
        });
        radio1.setOnClickListener(v -> {
            radio_custom_val.setChecked(false);
            isChecked = false;
        });


        radio_custom_val.setOnClickListener(v -> {
           if(isChecked) {
               radio_custom_val.setChecked(false);
               radioGroup.clearCheck();
               isChecked = false;
           }else {
               radioGroup.clearCheck();
               isChecked = true;
           }
        });

        addRadiostationButton.setOnClickListener(view -> {
            oktet1 =  ipaddr1.getText().toString();
            oktet2 =  ipaddr2.getText().toString();
            oktet3 =  ipaddr3.getText().toString();
            oktet4 =  ipaddr4.getText().toString();

            if(oktet1.equals("")||oktet2.equals("")||oktet3.equals("")||oktet4.equals("")) {
                Toast toast = Toast.makeText(context, "INSERT IP ADDRESS", Toast.LENGTH_SHORT);
                toast.show();
            }else{
                if(!radio_custom_val.isChecked()){
                    Toast toast1 = Toast.makeText(context, "CLICK INPUT CHECKBOX", Toast.LENGTH_SHORT);
                    toast1.show();
                }else {

                    String ipAddress = oktet1 + "." + oktet2 + "." + oktet3 + "." + oktet4;

                    if (radio0.getText().toString().equals("EMPTY IP ADDRESS")) {
                        radio0.setText(ipAddress);
                    } else {
                        if (radio1.getText().toString().equals("EMPTY IP ADDRESS")) {
                            radio1.setText(ipAddress);
                        } else {
                            Toast toast = Toast.makeText(context, "YOU CAN'T ADD MORE RADIOSTATIONS", Toast.LENGTH_SHORT);
                            toast.show();
                            }
                        }
                    }
                }

        });

        modifyAddrButton.setOnClickListener(view -> {
            oktet1 =  ipaddr1.getText().toString();
            oktet2 =  ipaddr2.getText().toString();
            oktet3 =  ipaddr3.getText().toString();
            oktet4 =  ipaddr4.getText().toString();


            if(oktet1.equals("")||oktet2.equals("")||oktet3.equals("")||oktet4.equals("")) {
                Toast toast = Toast.makeText(context, "INSERT IP ADDRESS", Toast.LENGTH_SHORT);
                toast.show();
            }else {

                    if (radio0.isChecked() || radio1.isChecked()) {
                        int selectedId = radioGroup.getCheckedRadioButtonId();

                        if (selectedId == radio0.getId()) {
                            radio0.setText(oktet1 + "." + oktet2 + "." + oktet3 + "." + oktet4);
                        }
                        if (selectedId == radio1.getId()) {
                            radio1.setText(oktet1 + "." + oktet2 + "." + oktet3 + "." + oktet4);
                        }


                    } else {
                        Toast toast = Toast.makeText(context, "CHOOSE ADDRESS TO MODIFY", Toast.LENGTH_SHORT);
                        toast.show();

                    }

            }
        });



        radioLocationButton.setOnClickListener(v -> {
            oktet1 =  ipaddr1.getText().toString();
            oktet2 =  ipaddr2.getText().toString();
            oktet3 =  ipaddr3.getText().toString();
            oktet4 =  ipaddr4.getText().toString();
            String ipAddress = "";

            if(radio_custom_val.isChecked() || radio0.isChecked() || radio1.isChecked() ){
                if (!radio_custom_val.isChecked()) {

                    int selectedId = radioGroup.getCheckedRadioButtonId();

                    if (selectedId == radio0.getId()) {
                        ipAddress = radio0.getText().toString();
                    }
                    if (selectedId == radio1.getId()) {
                        ipAddress = radio1.getText().toString();
                    }

                    if(ipAddress.equals("EMPTY IP ADDRESS")){
                        Toast toast = Toast.makeText(context, "EMPTY ADDRESS", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else {
                    if(oktet1.equals("")||oktet2.equals("")||oktet3.equals("")||oktet4.equals("")) {
                        Toast toast = Toast.makeText(context, "EMPTY ADDRESS", Toast.LENGTH_SHORT);
                        toast.show();
                    }else {
                        ipAddress = oktet1 + "." + oktet2 + "." + oktet3 + "." + oktet4;
                    }
                }
                if(ipAddress.equals("EMPTY IP ADDRESS") || ipAddress.equals("")){
                    Toast toast = Toast.makeText(context, "EMPTY ADDRESS", Toast.LENGTH_SHORT);
                    toast.show();
                }else {
                    Toast toast = Toast.makeText(context, "CONNECTING WITH: " + ipAddress, Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        new LongRunningTask().execute(ipAddress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }else {

                Toast toast = Toast.makeText(context, "CHOOSE OR INSERT IP ADDRESS", Toast.LENGTH_SHORT);
                toast.show();
            }
        });


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
                radioLattextView.setText("no connection");
                radioLontextView.setText("no connection");
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
