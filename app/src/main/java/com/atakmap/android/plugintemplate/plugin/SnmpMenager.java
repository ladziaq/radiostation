package com.atakmap.android.plugintemplate.plugin;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpMenager{


    public static CommunityTarget createDefault(String ip, String community) {
        Address address = GenericAddress.parse("udp" + ":" + ip
                + "/" + "161");

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(address);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(1000);
        target.setRetries(3);
        return target;
    }

    public  static  String snmpGet(String ip, String community, String oid) {

        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        String textOfResponse = "";
        try {

            PDU pdu = new PDU();

            pdu.add(new VariableBinding(new OID(oid)));

            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();

            snmp = new Snmp(transport);



            snmp.listen();

            pdu.setType(PDU.GET);
            ResponseEvent respEvent = snmp.send(pdu, target);

            PDU response = respEvent.getResponse();


            if (response == null) {
                textOfResponse = response.getErrorStatusText();
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
}
