
package com.atakmap.android.plugintemplate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.plugintemplate.plugin.R;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;

import com.atakmap.android.plugintemplate.plugin.SnmpMenager;
import com.atakmap.coremap.cot.event.CotAttribute;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.cot.event.CotPoint;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.time.CoordinatedTime;

import org.snmp4j.mp.SnmpConstants;

import java.sql.Time;

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
    private final TextView radioLattextView;
    private final TextView radioLontextView;
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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        longAndLat = latitude.toString() + ' ' + longitude.toString();
        textView.setText(longAndLat);

        /**************************** SNMP GET *****************************/


        String data = SnmpMenager.snmpGet("192.168.103.1","public","1.3.6.1.4.1.4045.61005681.20.1.0");




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

           radioLontextView.setText(data);
           radioLattextView.setText(data);
        });





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
