package com.daizhirui.bletemperature;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * GattStandard provides some methods that can parse a UUID and return its pre-defined name, property,
 * permission or type according to the specification provided by the Bluetooth SIG.
 * To visit the original material, click https://www.bluetooth.com/specifications/gatt/ .
 */
class GattStandard {

    private static final String TAG = GattStandard.class.getSimpleName();

    static final UUID GENERIC_ACCESS_SERVICE_UUID = new UUID(0x0000180000001000L, 0x800000805F9B34FBL);
    static final UUID DEVICE_NAME_CHARACTERISTIC_UUID = new UUID(0x00002A0000001000L, 0x800000805F9B34FBL);
    static final UUID APPEARANCE_CHARACTERISTIC_UUID = new UUID(0x00002A0100001000L, 0x800000805F9B34FBL);

    /**
     * Parse the uuid according to the service specification.
     * @param uuid UUID of a service
     * @return  A string representing the service name.
     */
    static String parseServiceName(@NotNull UUID uuid) {
        int assignedNumber = (int) ((uuid.getMostSignificantBits() >> 32) & 0xffff);
        switch (assignedNumber) {
            case 0x1800: return "Generic Access";
            case 0x1811: return "Alert Notification Service";
            case 0x1815: return "Automation IO";
            case 0x180F: return "Battery Service";
            case 0x1810: return "Blood Pressure";
            case 0x181B: return "Body Composition";
            case 0x181E: return "Bond Management Service";
            case 0x181F: return "Continuous Glucose Monitoring";
            case 0x1805: return "Current Time Service";
            case 0x1818: return "Cycling Power";
            case 0x1816: return "Cycling Speed and Cadence";
            case 0x180A: return "Device Information";
            case 0x181A: return "Environmental Sensing";
            case 0x1826: return "Fitness Machine";
            case 0x1801: return "Generic Attribute";
            case 0x1808: return "Glucose";
            case 0x1809: return "Health Thermometer";
            case 0x180D: return "Heart Rate";
            case 0x1823: return "HTTP Proxy";
            case 0x1812: return "Human Interface Device";
            case 0x1802: return "Immediate Alert";
            case 0x1821: return "Indoor Positioning";
            case 0x183A: return "Insulin Delivery";
            case 0x1820: return "Internet Protocol Support Service";
            case 0x1803: return "Link Loss";
            case 0x1819: return "Location and Navigation";
            case 0x1827: return "Mesh Provisioning Service";
            case 0x1828: return "Mesh Proxy Service";
            case 0x1807: return "Next DST Change Service";
            case 0x1825: return "Object Transfer Service";
            case 0x180E: return "Phone Alert Status Service";
            case 0x1822: return "Pulse Oximeter Service";
            case 0x1829: return "Reconnection Configuration";
            case 0x1806: return "Reference Time Update Service";
            case 0x1814: return "Running Speed and Cadence";
            case 0x1813: return "Scan Parameters";
            case 0x1824: return "Transport Discovery";
            case 0x1804: return "Tx Power";
            case 0x181C: return "User Data";
            case 0x181D: return "Weight Scale";
            default: return "Undefined";
        }
    }

    /**
     * Parse the uuid according to the characteristic specification.
     * @param uuid UUID of a characteristic
     * @return  A string representing the characteristic name
     */
    @NotNull
    static String parseCharacteristicName(@NotNull UUID uuid) {
        int assignedNumber = (int) ((uuid.getMostSignificantBits() >> 32) & 0xffff);
        switch (assignedNumber) {
            case 0x2A7E:
                return "Aerobic Heart Rate Lower Limit";
            case 0x2A84:
                return "Aerobic Heart Rate Upper Limit";
            case 0x2A7F:
                return "Aerobic Threshold";
            case 0x2A80:
                return "Age";
            case 0x2A5A:
                return "Aggregate";
            case 0x2A43:
                return "Alert Category ID";
            case 0x2A42:
                return "Alert Category ID Bit Mask";
            case 0x2A06:
                return "Alert Level";
            case 0x2A44:
                return "Alert Notification Control Point";
            case 0x2A3F:
                return "Alert Status";
            case 0x2AB3:
                return "Altitude";
            case 0x2A81:
                return "Anaerobic Heart Rate Lower Limit";
            case 0x2A82:
                return "Anaerobic Heart Rate Upper Limit";
            case 0x2A83:
                return "Anaerobic Threshold";
            case 0x2A58:
                return "Analog";
            case 0x2A59:
                return "Analog Output";
            case 0x2A73:
                return "Apparent Wind Direction";
            case 0x2A72:
                return "Apparent Wind Speed";
            case 0x2A01:
                return "Appearance";
            case 0x2AA3:
                return "Barometric Pressure Trend";
            case 0x2A19:
                return "Battery Level";
            case 0x2A1B:
                return "Battery Level State";
            case 0x2A1A:
                return "Battery Power State";
            case 0x2A49:
                return "Blood Pressure Feature";
            case 0x2A35:
                return "Blood Pressure Measurement";
            case 0x2A9B:
                return "Body Composition Feature";
            case 0x2A9C:
                return "Body Composition Measurement";
            case 0x2A38:
                return "Body Sensor Location";
            case 0x2AA4:
                return "Bond Management Control Point";
            case 0x2AA5:
                return "Bond Management Features";
            case 0x2A22:
                return "Boot Keyboard Input Report";
            case 0x2A32:
                return "Boot Keyboard Output Report";
            case 0x2A33:
                return "Boot Mouse Input Report";
            case 0x2AA8:
                return "CGM Feature";
            case 0x2AA7:
                return "CGM Measurement";
            case 0x2AAB:
                return "CGM Session Run Time";
            case 0x2AAA:
                return "CGM Session Start Time";
            case 0x2AAC:
                return "CGM Specific Ops Control Point";
            case 0x2AA9:
                return "CGM Status";
            case 0x2ACE:
                return "Cross Trainer Data";
            case 0x2A5C:
                return "CSC Feature";
            case 0x2A5B:
                return "CSC Measurement";
            case 0x2A2B:
                return "Current Time";
            case 0x2A66:
                return "Cycling Power Control Point";
            case 0x2A65:
                return "Cycling Power Feature";
            case 0x2A63:
                return "Cycling Power Measurement";
            case 0x2A64:
                return "Cycling Power Vector";
            case 0x2A99:
                return "Database Change Increment";
            case 0x2A85:
                return "Date of Birth";
            case 0x2A86:
                return "Date of Threshold Assessment";
            case 0x2A08:
                return "Date Time";
            case 0x2AED:
                return "Date UTC";
            case 0x2A0A:
                return "Day Date Time";
            case 0x2A09:
                return "Day of Week";
            case 0x2A7D:
                return "Descriptor Value Changed";
            case 0x2A7B:
                return "Dew Point";
            case 0x2A56:
                return "Digital";
            case 0x2A57:
                return "Digital Output";
            case 0x2A0D:
                return "DST Offset";
            case 0x2A6C:
                return "Elevation";
            case 0x2A87:
                return "Email Address";
            case 0x2A0B:
                return "Exact Time 100";
            case 0x2A0C:
                return "Exact Time 256";
            case 0x2A88:
                return "Fat Burn Heart Rate Lower Limit";
            case 0x2A89:
                return "Fat Burn Heart Rate Upper Limit";
            case 0x2A26:
                return "Firmware Revision String";
            case 0x2A8A:
                return "First Name";
            case 0x2AD9:
                return "Fitness Machine Control Point";
            case 0x2ACC:
                return "Fitness Machine Feature";
            case 0x2ADA:
                return "Fitness Machine Status";
            case 0x2A8B:
                return "Five Zone Heart Rate Limits";
            case 0x2AB2:
                return "Floor Number";
            case 0x2AA6:
                return "Central Address Resolution";
            case 0x2A00:
                return "Device Name";
            case 0x2A04:
                return "Peripheral Preferred Connection Parameters";
            case 0x2A02:
                return "Peripheral Privacy Flag";
            case 0x2A03:
                return "Reconnection Address";
            case 0x2A05:
                return "Service Changed";
            case 0x2A8C:
                return "Gender";
            case 0x2A51:
                return "Glucose Feature";
            case 0x2A18:
                return "Glucose Measurement";
            case 0x2A34:
                return "Glucose Measurement Context";
            case 0x2A74:
                return "Gust Factor";
            case 0x2A27:
                return "Hardware Revision String";
            case 0x2A39:
                return "Heart Rate Control Point";
            case 0x2A8D:
                return "Heart Rate Max";
            case 0x2A37:
                return "Heart Rate Measurement";
            case 0x2A7A:
                return "Heat Index";
            case 0x2A8E:
                return "Height";
            case 0x2A4C:
                return "HID Control Point";
            case 0x2A4A:
                return "HID Information";
            case 0x2A8F:
                return "Hip Circumference";
            case 0x2ABA:
                return "HTTP Control Point";
            case 0x2AB9:
                return "HTTP Entity Body";
            case 0x2AB7:
                return "HTTP Headers";
            case 0x2AB8:
                return "HTTP Status Code";
            case 0x2ABB:
                return "HTTPS Security";
            case 0x2A6F:
                return "Humidity";
            case 0x2B22:
                return "IDD Annunciation Status";
            case 0x2B25:
                return "IDD Command Control Point";
            case 0x2B26:
                return "IDD Command Data";
            case 0x2B23:
                return "IDD Features";
            case 0x2B28:
                return "IDD History Data";
            case 0x2B27:
                return "IDD Record Access Control Point";
            case 0x2B21:
                return "IDD Status";
            case 0x2B20:
                return "IDD Status Changed";
            case 0x2B24:
                return "IDD Status Reader Control Point";
            case 0x2A2A:
                return "IEEE 11073-20601 Regulatory Certification Data List";
            case 0x2AD2:
                return "Indoor Bike Data";
            case 0x2AAD:
                return "Indoor Positioning Configuration";
            case 0x2A36:
                return "Intermediate Cuff Pressure";
            case 0x2A1E:
                return "Intermediate Temperature";
            case 0x2A77:
                return "Irradiance";
            case 0x2AA2:
                return "Language";
            case 0x2A90:
                return "Last Name";
            case 0x2AAE:
                return "Latitude";
            case 0x2A6B:
                return "LN Control Point";
            case 0x2A6A:
                return "LN Feature";
            case 0x2AB1:
                return "Local East Coordinate";
            case 0x2AB0:
                return "Local North Coordinate";
            case 0x2A0F:
                return "Local Time Information";
            case 0x2A67:
                return "Location and Speed Characteristic";
            case 0x2AB5:
                return "Location Name";
            case 0x2AAF:
                return "Longitude";
            case 0x2A2C:
                return "Magnetic Declination";
            case 0x2AA0:
                return "Magnetic Flux Density - 2D";
            case 0x2AA1:
                return "Magnetic Flux Density - 3D";
            case 0x2A29:
                return "Manufacturer Name String";
            case 0x2A91:
                return "Maximum Recommended Heart Rate";
            case 0x2A21:
                return "Measurement Interval";
            case 0x2A24:
                return "Model Number String";
            case 0x2A68:
                return "Navigation";
            case 0x2A3E:
                return "Network Availability";
            case 0x2A46:
                return "New Alert";
            case 0x2AC5:
                return "Object Action Control Point";
            case 0x2AC8:
                return "Object Changed";
            case 0x2AC1:
                return "Object First-Created";
            case 0x2AC3:
                return "Object ID";
            case 0x2AC2:
                return "Object Last-Modified";
            case 0x2AC6:
                return "Object List Control Point";
            case 0x2AC7:
                return "Object List Filter";
            case 0x2ABE:
                return "Object Name";
            case 0x2AC4:
                return "Object Properties";
            case 0x2AC0:
                return "Object Size";
            case 0x2ABF:
                return "Object Type";
            case 0x2ABD:
                return "OTS Feature";
            case 0x2A5F:
                return "PLX Continuous Measurement Characteristic";
            case 0x2A60:
                return "PLX Features";
            case 0x2A5E:
                return "PLX Spot-Check Measurement";
            case 0x2A50:
                return "PnP ID";
            case 0x2A75:
                return "Pollen Concentration";
            case 0x2A2F:
                return "Position 2D";
            case 0x2A30:
                return "Position 3D";
            case 0x2A69:
                return "Position Quality";
            case 0x2A6D:
                return "Pressure";
            case 0x2A4E:
                return "Protocol Mode";
            case 0x2A62:
                return "Pulse Oximetry Control Point";
            case 0x2A78:
                return "Rainfall";
            case 0x2B1D:
                return "RC Feature";
            case 0x2B1E:
                return "RC Settings";
            case 0x2B1F:
                return "Reconnection Configuration Control Point";
            case 0x2A52:
                return "Record Access Control Point";
            case 0x2A14:
                return "Reference Time Information";
            case 0x2A3A:
                return "Removable";
            case 0x2A4D:
                return "Report";
            case 0x2A4B:
                return "Report Map";
            case 0x2AC9:
                return "Resolvable Private Address Only";
            case 0x2A92:
                return "Resting Heart Rate";
            case 0x2A40:
                return "Ringer Control point";
            case 0x2A41:
                return "Ringer Setting";
            case 0x2AD1:
                return "Rower Data";
            case 0x2A54:
                return "RSC Feature";
            case 0x2A53:
                return "RSC Measurement";
            case 0x2A55:
                return "SC Control Point";
            case 0x2A4F:
                return "Scan Interval Window";
            case 0x2A31:
                return "Scan Refresh";
            case 0x2A3C:
                return "Scientific Temperature Celsius";
            case 0x2A10:
                return "Secondary Time Zone";
            case 0x2A5D:
                return "Sensor Location";
            case 0x2A25:
                return "Serial Number String";
            case 0x2A3B:
                return "Service Required";
            case 0x2A28:
                return "Software Revision String";
            case 0x2A93:
                return "Sport Type for Aerobic and Anaerobic Thresholds";
            case 0x2AD0:
                return "Stair Climber Data";
            case 0x2ACF:
                return "Step Climber Data";
            case 0x2A3D:
                return "String";
            case 0x2AD7:
                return "Supported Heart Rate Range";
            case 0x2AD5:
                return "Supported Inclination Range";
            case 0x2A47:
                return "Supported New Alert Category";
            case 0x2AD8:
                return "Supported Power Range";
            case 0x2AD6:
                return "Supported Resistance Level Range";
            case 0x2AD4:
                return "Supported Speed Range";
            case 0x2A48:
                return "Supported Unread Alert Category";
            case 0x2A23:
                return "System ID";
            case 0x2ABC:
                return "TDS Control Point";
            case 0x2A6E:
                return "Temperature";
            case 0x2A1F:
                return "Temperature Celsius";
            case 0x2A20:
                return "Temperature Fahrenheit";
            case 0x2A1C:
                return "Temperature Measurement";
            case 0x2A1D:
                return "Temperature Type";
            case 0x2A94:
                return "Three Zone Heart Rate Limits";
            case 0x2A12:
                return "Time Accuracy";
            case 0x2A15:
                return "Time Broadcast";
            case 0x2A13:
                return "Time Source";
            case 0x2A16:
                return "Time Update Control Point";
            case 0x2A17:
                return "Time Update State";
            case 0x2A11:
                return "Time with DST";
            case 0x2A0E:
                return "Time Zone";
            case 0x2AD3:
                return "Training Status";
            case 0x2ACD:
                return "Treadmill Data";
            case 0x2A71:
                return "True Wind Direction";
            case 0x2A70:
                return "True Wind Speed";
            case 0x2A95:
                return "Two Zone Heart Rate Limit";
            case 0x2A07:
                return "Tx Power Level";
            case 0x2AB4:
                return "Uncertainty";
            case 0x2A45:
                return "Unread Alert Status";
            case 0x2AB6:
                return "URI";
            case 0x2A9F:
                return "User Control Point";
            case 0x2A9A:
                return "User Index";
            case 0x2A76:
                return "UV Index";
            case 0x2A96:
                return "VO2 Max";
            case 0x2A97:
                return "Waist Circumference";
            case 0x2A98:
                return "Weight";
            case 0x2A9D:
                return "Weight Measurement";
            case 0x2A9E:
                return "Weight Scale Feature";
            case 0x2A79:
                return "Wind Chill";
            default:
                return "Undefined";
        }
    }

    static String DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = "Client Characteristic Configuration";
    /**
     * Parse a uuid according to the descriptor specification.
     * @param uuid UUID of a descriptor.
     * @return  A string representing the descriptor name.
     */
    @NotNull
    static String parseDescriptorName(@NotNull UUID uuid) {
        int assignedNumber = (int) ((uuid.getMostSignificantBits() >> 32) & 0xffff);
        switch (assignedNumber) {
            case 0x2905:
                return "Characteristic Aggregate Format";
            case 0x2900:
                return "Characteristic Extended Properties";
            case 0x2904:
                return "Characteristic Presentation Format";
            case 0x2901:
                return "Characteristic User Description";
            case 0x2902:
                return DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION;
            case 0x290B:
                return "Environmental Sensing Configuration";
            case 0x290C:
                return "Environmental Sensing Measurement";
            case 0x290D:
                return "Environmental Sensing Trigger Setting";
            case 0x2907:
                return "External Report Reference";
            case 0x2909:
                return "Number of Digitals";
            case 0x2908:
                return "Report Reference";
            case 0x2903:
                return "Server Characteristic Configuration";
            case 0x290E:
                return "Time Trigger Setting";
            case 0x2906:
                return "Valid Range";
            case 0x290A:
                return "Value Trigger Setting";
            default:
                return "Undefined";
        }
    }

    static final String CHARACTERISTIC_PROPERTY_BROADCAST =
            "Broadcast";
    static final String CHARACTERISTIC_PROPERTY_READ =
            "Read";
    static final String CHARACTERISTIC_PROPERTY_WRITE_NO_RESPONSE =
            "Write No Response";
    static final String CHARACTERISTIC_PROPERTY_WRITE =
            "Write";
    static final String CHARACTERISTIC_PROPERTY_NOTIFY =
            "Notify";
    static final String CHARACTERISTIC_PROPERTY_INDICATE =
            "Indication";
    static final String CHARACTERISTIC_PROPERTY_SIGNED_WRITE =
            "Signed Write";
    static final String CHARACTERISTIC_PROPERTY_EXTENDED_PROPS =
            "Extended Properties";

    /**
     * Parse the characteristic properties represented by an integer.
     * @param property  An integer whose bits represent different properties.
     * @return  A string array contains properties.
     */
    static List<String> parseCharacteristicProperty(int property) {
        List<String> tmp = new ArrayList<>();
        if ((property & BluetoothGattCharacteristic.PROPERTY_BROADCAST) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_BROADCAST);
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_READ);
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_WRITE_NO_RESPONSE);
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_WRITE);
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_NOTIFY);
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_INDICATE);
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_SIGNED_WRITE);
        }
        if ((property & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) > 0) {
            tmp.add(CHARACTERISTIC_PROPERTY_EXTENDED_PROPS);
        }
        return tmp;
    }

    static final String CHARACTERISTIC_PERMISSION_READ =
            "Read";
    static final String CHARACTERISTIC_PERMISSION_READ_ENCRYPTED =
            "Encrypted Read";
    static final String CHARACTERISTIC_PERMISSION_READ_ENCRYPTED_MITM =
            "Read With Man-in-the-middle Protection";
    static final String CHARACTERISTIC_PERMISSION_WRITE =
            "Write";
    static final String CHARACTERISTIC_PERMISSION_WRITE_ENCRYPTED =
            "Encrypted Write";
    static final String CHARACTERISTIC_PERMISSION_WRITE_ENCRYPTED_MITM =
            "Write With Man-in-the-middle Protection";
    static final String CHARACTERISTIC_PERMISSION_WRITE_SIGNED =
            "Signed Write";
    static final String CHARACTERISTIC_PERMISSION_WRITE_SIGNED_MITM =
            "Signed Write With Man-in-the-middle Protection";

    /**
     * Parse the characteristic permissions represented by an integer.
     * @param permission An integer whose bits represent different permissions.
     * @return A string array contains permissions.
     */
    static String[] parseCharacteristicPermission(int permission) {
        List<String> tmp = new ArrayList<>();
        if ((permission & BluetoothGattCharacteristic.PERMISSION_READ) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_READ);
        }
        if ((permission & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_READ_ENCRYPTED);
        }
        if ((permission & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_READ_ENCRYPTED_MITM);
        }
        if ((permission & BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_WRITE);
        }
        if ((permission & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_WRITE_ENCRYPTED);
        }
        if ((permission & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_WRITE_ENCRYPTED_MITM);
        }
        if ((permission & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_WRITE_SIGNED);
        }
        if ((permission & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) > 0) {
            tmp.add(CHARACTERISTIC_PERMISSION_WRITE_SIGNED_MITM);
        }
        return tmp.toArray(new String[0]);
    }

    static final String CHARACTERISTIC_WRITE_TYPE_DEFAULT =
            "Write";
    static final String CHARACTERISTIC_WRITE_TYPE_NO_RESPONSE =
            "Write No Response";
    static final String CHARACTERISTIC_WRITE_TYPE_SIGNED =
            "Write With Authentication Signature";

    /**
     * Parse supported types of writing characteristic represented by an integer.
     * @param writeType An integer whose bits represent different types of writing.
     * @return A string array contains supported writing type.
     */
    static String[] parseWriteType(int writeType) {
        List<String> tmp = new ArrayList<>();
        if ((writeType & BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) > 0) {
            tmp.add(CHARACTERISTIC_WRITE_TYPE_DEFAULT);
        }
        if ((writeType & BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) > 0) {
            tmp.add(CHARACTERISTIC_WRITE_TYPE_NO_RESPONSE);
        }
        if ((writeType & BluetoothGattCharacteristic.WRITE_TYPE_SIGNED) > 0) {
            tmp.add(CHARACTERISTIC_WRITE_TYPE_SIGNED);
        }
        return tmp.toArray(new String[0]);
    }

    /**
     * Convert a byte array to a string.
     * @param bytes The byte array to convert.
     * @return The string representing the array, in the format of hex.
     */
    static String bytes2HexString(@NotNull byte[] bytes) {
        String tmp = "";
        for (byte aByte : bytes) {
            tmp += String.format("%02x", aByte & 0xff);
        }
        return tmp;
    }

    /**
     * Convert a hex string to a byte array.
     * @param string A hex string represent a byte array, every two chars for one byte.
     * @return  A byte array.
     */
    static byte[] hexString2Bytes(String string) {
        int stringLength = string.length();
        if (stringLength / 2 != 0) {
            Log.e(TAG, "The provided string is of wrong length!");
            return null;
        }
        byte[] tmp = new byte[stringLength / 2];
        for (int i = 0, j = 0; i < stringLength; i += 2, ++j) {
            tmp[j] = (byte) (Integer.parseInt(string.substring(i, i + 2), 16) & 0xff);
        }
        return tmp;
    }
}
