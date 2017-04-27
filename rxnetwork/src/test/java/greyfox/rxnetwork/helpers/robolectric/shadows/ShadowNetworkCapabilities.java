package greyfox.rxnetwork.helpers.robolectric.shadows;

import static android.net.NetworkCapabilities.NET_CAPABILITY_MMS;
import static android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_ETHERNET;
import static android.net.NetworkCapabilities.TRANSPORT_VPN;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.net.NetworkCapabilities;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/**
 * Shadow for {@link android.net.ConnectivityManager}.
 *
 * @author Radek Kozak
 */
@SuppressWarnings("PointlessBitwiseExpression")
@Implements(NetworkCapabilities.class)
@RequiresApi(LOLLIPOP)
public class ShadowNetworkCapabilities {

    public static final int SIGNAL_STRENGTH_UNSPECIFIED = Integer.MIN_VALUE;
    public static final int NET_CAPABILITY_FOREGROUND = 18;
    private static final int MIN_TRANSPORT = TRANSPORT_CELLULAR;
    private static final int MAX_TRANSPORT = TRANSPORT_VPN;
    private static final int MIN_NET_CAPABILITY = NET_CAPABILITY_MMS;
    private static final int MAX_NET_CAPABILITY = NET_CAPABILITY_FOREGROUND;

    private long mNetworkCapabilities;
    private long mTransportTypes;
    private int mLinkUpBandwidthKbps;
    private int mLinkDownBandwidthKbps;
    private String mNetworkSpecifier;
    private int mSignalStrength;

    public static NetworkCapabilities newInstance(long networkCapabilities, long transportTypes,
            int linkUpBandwidthKbps, int linkDownBandwidthKbps, String networkSpecifier,
            int signalStrength) {

        NetworkCapabilities nc = Shadow.newInstanceOf(NetworkCapabilities.class);
        final ShadowNetworkCapabilities capabilities
                = Shadow.extract(nc);

        capabilities.mNetworkCapabilities = networkCapabilities;
        capabilities.mTransportTypes = transportTypes;
        capabilities.mLinkUpBandwidthKbps = linkUpBandwidthKbps;
        capabilities.mLinkDownBandwidthKbps = linkDownBandwidthKbps;
        capabilities.mNetworkSpecifier = networkSpecifier;
        capabilities.mSignalStrength = signalStrength;
        return nc;
    }

    public static String transportNamesOf(int[] types) {
        String transports = "";
        for (int i = 0; i < types.length; ) {
            switch (types[i]) {
                case TRANSPORT_CELLULAR:
                    transports += "CELLULAR";
                    break;
                case TRANSPORT_WIFI:
                    transports += "WIFI";
                    break;
                case TRANSPORT_BLUETOOTH:
                    transports += "BLUETOOTH";
                    break;
                case TRANSPORT_ETHERNET:
                    transports += "ETHERNET";
                    break;
                case TRANSPORT_VPN:
                    transports += "VPN";
                    break;
            }
            if (++i < types.length) transports += "|";
        }
        return transports;
    }

    public void addTransportType(int transportType) {
        if (transportType < MIN_TRANSPORT || transportType > MAX_TRANSPORT) {
            throw new IllegalArgumentException("TransportType out of range");
        }
        mTransportTypes |= 1 << transportType;
        setNetworkSpecifier(mNetworkSpecifier); // used for exception checking
    }

    public void removeTransportType(int transportType) {
        if (transportType < MIN_TRANSPORT || transportType > MAX_TRANSPORT) {
            throw new IllegalArgumentException("TransportType out of range");
        }
        mTransportTypes &= ~(1 << transportType);
        setNetworkSpecifier(mNetworkSpecifier); // used for exception checking
    }

    @Implementation
    public int getLinkUpstreamBandwidthKbps() {
        return mLinkUpBandwidthKbps;
    }

    @Implementation
    public void setLinkUpstreamBandwidthKbps(int upKbps) {
        mLinkUpBandwidthKbps = upKbps;
    }

    @Implementation
    public int getLinkDownstreamBandwidthKbps() {
        return mLinkDownBandwidthKbps;
    }

    @Implementation
    public void setLinkDownstreamBandwidthKbps(int downKbps) {
        mLinkDownBandwidthKbps = downKbps;
    }

    @Implementation
    public String getNetworkSpecifier() {
        return mNetworkSpecifier;
    }

    public void setNetworkSpecifier(String networkSpecifier) {
        if (!TextUtils.isEmpty(networkSpecifier) && Long.bitCount(mTransportTypes) != 1) {
            throw new IllegalStateException("Must have a single transport specified to use " +
                    "setNetworkSpecifier");
        }
        mNetworkSpecifier = networkSpecifier;
    }

    private int[] enumerateBits(long val) {
        int size = Long.bitCount(val);
        int[] result = new int[size];
        int index = 0;
        int resource = 0;
        while (val > 0) {
            if ((val & 1) == 1) result[index++] = resource;
            val = val >> 1;
            resource++;
        }
        return result;
    }

    public boolean hasCapability(int capability) {
        if (capability < MIN_NET_CAPABILITY || capability > MAX_NET_CAPABILITY) {
            return false;
        }
        return ((mNetworkCapabilities & (1 << capability)) != 0);
    }

    @Implementation
    public boolean hasSignalStrength() {
        return mSignalStrength > SIGNAL_STRENGTH_UNSPECIFIED;
    }

    @Implementation
    public int getSignalStrength() {
        return mSignalStrength;
    }

    @Implementation
    public void setSignalStrength(int signalStrength) {
        mSignalStrength = signalStrength;
    }

    /*@Override
    public int hashCode() {
        return ((int) (mNetworkCapabilities & 0xFFFFFFFF) +
                ((int) (mNetworkCapabilities >> 32) * 3) +
                ((int) (mTransportTypes & 0xFFFFFFFF) * 5) +
                ((int) (mTransportTypes >> 32) * 7) +
                (mLinkUpBandwidthKbps * 11) +
                (mLinkDownBandwidthKbps * 13) +
                (TextUtils.isEmpty(mNetworkSpecifier) ? 0 : mNetworkSpecifier.hashCode() * 17) +
                (mSignalStrength * 19));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || (!(obj instanceof ShadowNetworkCapabilities))) return false;
        ShadowNetworkCapabilities that = (ShadowNetworkCapabilities) obj;
        return (equalsNetCapabilities(that) &&
                equalsTransportTypes(that) &&
                equalsLinkBandwidths(that) &&
                equalsSignalStrength(that) &&
                equalsSpecifier(that));
    }

    @Override
    public String toString() {
        int[] types = getTransportTypes();
        String transports = (types.length > 0) ? " Transports: " + transportNamesOf(types) : "";

        types = getCapabilities();
        String capabilities = (types.length > 0 ? " Capabilities: " : "");
        for (int i = 0; i < types.length; ) {
            switch (types[i]) {
                case NET_CAPABILITY_MMS:
                    capabilities += "MMS";
                    break;
                case NET_CAPABILITY_SUPL:
                    capabilities += "SUPL";
                    break;
                case NET_CAPABILITY_DUN:
                    capabilities += "DUN";
                    break;
                case NET_CAPABILITY_FOTA:
                    capabilities += "FOTA";
                    break;
                case NET_CAPABILITY_IMS:
                    capabilities += "IMS";
                    break;
                case NET_CAPABILITY_CBS:
                    capabilities += "CBS";
                    break;
                case NET_CAPABILITY_WIFI_P2P:
                    capabilities += "WIFI_P2P";
                    break;
                case NET_CAPABILITY_IA:
                    capabilities += "IA";
                    break;
                case NET_CAPABILITY_RCS:
                    capabilities += "RCS";
                    break;
                case NET_CAPABILITY_XCAP:
                    capabilities += "XCAP";
                    break;
                case NET_CAPABILITY_EIMS:
                    capabilities += "EIMS";
                    break;
                case NET_CAPABILITY_NOT_METERED:
                    capabilities += "NOT_METERED";
                    break;
                case NET_CAPABILITY_INTERNET:
                    capabilities += "INTERNET";
                    break;
                case NET_CAPABILITY_NOT_RESTRICTED:
                    capabilities += "NOT_RESTRICTED";
                    break;
                case NET_CAPABILITY_TRUSTED:
                    capabilities += "TRUSTED";
                    break;
                case NET_CAPABILITY_NOT_VPN:
                    capabilities += "NOT_VPN";
                    break;
                case NET_CAPABILITY_VALIDATED:
                    capabilities += "VALIDATED";
                    break;
                case NET_CAPABILITY_CAPTIVE_PORTAL:
                    capabilities += "CAPTIVE_PORTAL";
                    break;
                case NET_CAPABILITY_FOREGROUND:
                    capabilities += "FOREGROUND";
                    break;
            }
            if (++i < types.length) capabilities += "&";
        }

        String upBand = ((mLinkUpBandwidthKbps > 0) ? " LinkUpBandwidth>=" +
                mLinkUpBandwidthKbps + "Kbps" : "");
        String dnBand = ((mLinkDownBandwidthKbps > 0) ? " LinkDnBandwidth>=" +
                mLinkDownBandwidthKbps + "Kbps" : "");

        String specifier = (mNetworkSpecifier == null ?
                "" : " Specifier: <" + mNetworkSpecifier + ">");

        String signalStrength = (hasSignalStrength() ? " SignalStrength: " + mSignalStrength : "");

        return "[" + transports + capabilities + upBand + dnBand + specifier + signalStrength + "]";
    }

    public boolean equalsNetCapabilities(ShadowNetworkCapabilities nc) {
        return (nc.mNetworkCapabilities == this.mNetworkCapabilities);
    }

    public boolean equalsTransportTypes(ShadowNetworkCapabilities nc) {
        return (nc.mTransportTypes == this.mTransportTypes);
    }

    private boolean equalsLinkBandwidths(ShadowNetworkCapabilities nc) {
        return (this.mLinkUpBandwidthKbps == nc.mLinkUpBandwidthKbps &&
                this.mLinkDownBandwidthKbps == nc.mLinkDownBandwidthKbps);
    }

    private boolean equalsSignalStrength(ShadowNetworkCapabilities nc) {
        return this.mSignalStrength == nc.mSignalStrength;
    }

    private boolean equalsSpecifier(ShadowNetworkCapabilities nc) {
        if (TextUtils.isEmpty(mNetworkSpecifier)) {
            return TextUtils.isEmpty(nc.mNetworkSpecifier);
        } else {
            return mNetworkSpecifier.equals(nc.mNetworkSpecifier);
        }
    }

    public int[] getTransportTypes() {
        return enumerateBits(mTransportTypes);
    }

    public int[] getCapabilities() {
        return enumerateBits(mNetworkCapabilities);
    }*/
}
