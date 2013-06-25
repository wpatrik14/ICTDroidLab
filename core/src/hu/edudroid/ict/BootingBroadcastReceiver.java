package hu.edudroid.ict;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootingBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		Intent startServiceIntent = new Intent(context, CoreService.class);
        context.startService(startServiceIntent);
	}

}
