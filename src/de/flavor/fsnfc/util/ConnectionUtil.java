package de.flavor.fsnfc.util;

import android.content.Context;
import android.provider.Settings;

public class ConnectionUtil {

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }
	
}
