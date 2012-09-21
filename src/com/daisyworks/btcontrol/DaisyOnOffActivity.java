/*
    This file is part of the DaisyWorks On/Off application.

    The DaisyWorks On/Off application is free software: you can redistribute
    it and/or modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation, either version 3
    of the License, or (at your option) any later version.

    The DaisyWorks On/Off application is distributed in the hope that it
    will be useful, but WITHOUT ANY WARRANTY; without even the implied
    warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public
    License along with the DaisyWorks On/Off application.
    If not, see <http://www.gnu.org/licenses/>.

    Copyright 2011 DaisyWorks, Inc
 */
package com.daisyworks.btcontrol;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.daisyworks.android.HelpActivity;
import com.daisyworks.android.IOUtil;
import com.daisyworks.android.bluetooth.AbstractBluetoothActivity;
import com.daisyworks.android.bluetooth.BTCommThread;
import com.daisyworks.android.bluetooth.BluetoothAction;
import com.daisyworks.android.bluetooth.BluetoothApplication;
import com.daisyworks.android.bluetooth.EnterCmdModeAction;
import com.daisyworks.android.bluetooth.R;
import com.daisyworks.btcontrol.action.SendOnOffAction;
import com.daisyworks.btcontrol.action.SendPulseAction;
import com.daisyworks.btcontrol.action.SetupAction;

public class DaisyOnOffActivity extends AbstractBluetoothActivity implements OnClickListener, OnTouchListener {
	public static final boolean DEBUG = false;

	public final static int BUTTON_STATE_MESSAGE = 100;
	public final static int CLEAR_MESSAGE = 200;

	private static final int OFF = 0;
	private static final int ON = 1;
	private static final String[][] COMMANDS = new String[][] { new String[] { "S&,0800", "S&,0808" },
			new String[] { "S&,4000", "S&,4040" }, new String[] { "S&,8000", "S&,8080" },
			new String[] { "S*,0400", "S*,0404" }, new String[] { "S*,0800", "S*,0808" } };

	private List<AbstractOnOffButton> buttonAttributes;

	private Map<Integer, ToggleButton> toggleButtons = new HashMap<Integer, ToggleButton>();
	private final Handler handler = new CommHandler();

	public DaisyOnOffActivity() {
		super(R.id.main_connectionProgress, R.id.main_connectionStatus);
	}

	@Override
	protected void bluetoothEnabled() {
		((CommHandler) handler).start();
		buttonAttributes = Config.loadButtons(this);
		redoLayout();

		stopCommThreads();

		for (final AbstractOnOffButton buttonAttr : buttonAttributes) {
			final String deviceId = buttonAttr.getDeviceId();
			if (!getDevices().containsKey(deviceId)) {
				final BluetoothAction[] initialActions = { new EnterCmdModeAction(), new SetupAction() };
				final BTCommThread btComm = getBtCommThreadforNewActivity(handler, deviceId, 60000, initialActions);
				if (DaisyOnOffActivity.DEBUG)
					Log.d(Config.LOG_TAG, "Putting device: " + deviceId);
				getDevices().put(deviceId, btComm);
				// try to pre-connect to make response faster
				btComm.ensureConnected();
			}
		}
	}

	private Map<String, BTCommThread> getDevices() {
		return ((BluetoothApplication) getApplication()).getDevices();
	}

	protected void redoLayout() {
		toggleButtons.clear();

		setContentView(R.layout.main);

		findViewById(R.id.configureButton).setOnClickListener(this);

		final ViewGroup buttonsParent = (ViewGroup) findViewById(R.id.buttonsParent);
		final LayoutInflater inflater = getLayoutInflater();
		ViewGroup buttonRow = null;

		int i = 0;
		for (final AbstractOnOffButton buttonAttr : buttonAttributes) {
			int pushButtonId = 0;
			int toggleButtonId = 0;
			if (i % 2 == 0) {
				buttonRow = newButtonRow(inflater, buttonsParent);
				pushButtonId = R.id.main_pushButton1;
				toggleButtonId = R.id.main_toggleButton1;
			} else {
				pushButtonId = R.id.main_pushButton2;
				toggleButtonId = R.id.main_toggleButton2;
			}

			final Button pushButton = (Button) buttonRow.findViewById(pushButtonId);
			final ToggleButton toggleButton = (ToggleButton) buttonRow.findViewById(toggleButtonId);
			final ViewGroup layout = (ViewGroup) pushButton.getParent();

			Button button = pushButton;
			switch (buttonAttr.getBehavior()) {
			case HOLD_PULSE:
				layout.removeView(toggleButton);
				button.setOnTouchListener(this);
				break;
			case PULSE:
				button.setOnClickListener(this);
				layout.removeView(toggleButton);
				break;
			case ON_OFF:
				button = toggleButton;
				toggleButton.setChecked(buttonAttr.isPowerOn());
				toggleButton.setTextOn(buttonAttr.getLabel());
				toggleButton.setTextOff(buttonAttr.getLabel());
				button.setOnClickListener(this);
				layout.removeView(pushButton);
				toggleButtons.put(buttonAttr.getButtonId(), toggleButton);
				break;
			}

			button.setText(buttonAttr.getLabel());
			button.setTag(R.id.buttonAttributes, buttonAttr);

			i++;
		}

		if (i % 2 != 0) {
			final View pushButton2 = buttonRow.findViewById(R.id.main_pushButton2);
			pushButton2.setVisibility(View.INVISIBLE);

			final View toggleButton2 = buttonRow.findViewById(R.id.main_toggleButton2);
			toggleButton2.setVisibility(View.GONE);
		}
	}

	public ViewGroup newButtonRow(final LayoutInflater inflater, final ViewGroup parent) {
		final ViewGroup buttonRow = (ViewGroup) inflater.inflate(R.layout.button_row, parent, false);
		parent.addView(buttonRow);
		return buttonRow;
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopCommThreads();
		((CommHandler) handler).stop();
	}

	protected void stopCommThreads() {
		for (final BTCommThread btComm : getDevices().values()) {
			if (btComm != null) {
				btComm.shutdown();
			}
		}

		getDevices().clear();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.menu_help) {
			final Intent intent = new Intent(this, HelpActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	void updateButtonState(final ButtonState buttonState) {
		for (final AbstractOnOffButton button : buttonAttributes) {
			final boolean isOn = buttonState.isPinHigh(button.getPhysicalPin());

			if (button.getBehavior() == ButtonBehavior.ON_OFF) {
				final ToggleButton toggleButton = toggleButtons.get(button.getButtonId());
				if (toggleButton != null) {
					toggleButton.setChecked(isOn);
				}
				button.setPowerOn(isOn);
			} else if (button.isPowerOn()) {
				button.turnPinOff(this);
				button.setPowerOn(false);
			}
		}
	}

	public void bluetoothPinOn(final BluetoothButton button) {
		final String deviceId = button.getDeviceId();
		final BTCommThread btComm = getDevices().get(deviceId);
		final int pin = button.getPin();

		btComm.enqueueAction(new SendOnOffAction(COMMANDS[pin][ON]));
	}

	public void bluetoothPinOff(final BluetoothButton button) {
		final String deviceId = button.getDeviceId();
		final BTCommThread btComm = getDevices().get(deviceId);
		final int pin = button.getPin();

		btComm.enqueueAction(new SendOnOffAction(COMMANDS[pin][OFF]));
	}

	public void bluetoothSendPulse(final BluetoothButton button) {
		final String deviceId = button.getDeviceId();
		final BTCommThread btComm = getDevices().get(deviceId);
		final int pin = button.getPin();

		btComm.enqueueAction(new SendPulseAction(COMMANDS[pin][ON], COMMANDS[pin][OFF], new long[] { 1000 }));
	}

	public void sendWifiCommand(final WifiButton button, final String cmd) {
		final String url = "http://" + button.getServer() + "/services/daisy/" + button.getDeviceId() + "/"
				+ button.getPin() + "/" + cmd;

		new AsyncTask<Void, Integer, Boolean>() {
			@Override
			protected Boolean doInBackground(final Void... params) {
				InputStream in = null;

				try {
					in = new URL(url).openStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String line = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
					return Boolean.TRUE;
				} catch (final Exception e) {
					if (DaisyOnOffActivity.DEBUG)
						Log.e(Config.LOG_TAG, "Failed to execute command", e);
				} finally {
					IOUtil.closeQuietly(in);
				}
				return Boolean.FALSE;
			}

			@Override
			protected void onCancelled() {
				// dismissDialog(DAISY_WIFI_PROGRESS_DIALOG);
				// showDialog(DAISY_WIFI_FAILED_DIALOG);
			}

			@Override
			protected void onPostExecute(final Boolean result) {
				if (result == null || !result) {
					onCancelled();
				} else {
					// dismissDialog(DAISY_WIFI_PROGRESS_DIALOG);
				}
			}
		}.execute();
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		final AbstractOnOffButton button = (AbstractOnOffButton) v.getTag(R.id.buttonAttributes);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			button.turnPinOn(this);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			button.turnPinOff(this);
		}
		return false;
	}

	@Override
	public void onClick(final View v) {
		if (v.getId() == R.id.configureButton) {
			startActivity(new Intent(this, ConfigurationActivity.class));
			return;
		}
		final AbstractOnOffButton button = (AbstractOnOffButton) v.getTag(R.id.buttonAttributes);
		if (button.getBehavior() == ButtonBehavior.PULSE) {
			button.sendPulse(this);
		} else if (button.getBehavior() == ButtonBehavior.ON_OFF) {
			if (button.isPowerOn()) {
				button.turnPinOff(this);
				button.setPowerOn(false);
			} else {
				button.turnPinOn(this);
				button.setPowerOn(true);
			}
			((Button) v).setText(button.getLabel());
		}
	}

	class CommHandler extends Handler {
		@SuppressWarnings("synthetic-access")
		@Override
		public void handleMessage(final Message msg) {
			timeout.set(System.currentTimeMillis() + 2000);
			switch (msg.what) {
			case BTCommThread.BLUETOOTH_START_CONNECT:
				setStatus(R.string.bluetooth_connecting);
				spinProgressBar(true);
				break;

			case BTCommThread.BLUETOOTH_CONNECTED:
				setStatus(R.string.bluetooth_connected);
				spinProgressBar(false);
				break;

			case BTCommThread.BLUETOOTH_CONNECTION_ERROR:
				setStatus(R.string.bluetooth_connection_error);
				spinProgressBar(false);
				Toast.makeText(DaisyOnOffActivity.this, R.string.bluetooth_connection_error_toast, Toast.LENGTH_SHORT);
				break;
			case BTCommThread.BLUETOOTH_CONNECTION_CLOSED:
				setStatus(R.string.bluetooth_not_connected);
				spinProgressBar(false);
				break;
			case BUTTON_STATE_MESSAGE:
				updateButtonState((ButtonState) msg.obj);
				break;
			case CLEAR_MESSAGE:
				setStatus(R.string.clear_message);
				break;
			default:
				super.handleMessage(msg);
			}
		}

		Thread t;
		AtomicLong timeout = new AtomicLong(System.currentTimeMillis() + 2000);

		void start() {
			t = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						if (System.currentTimeMillis() > timeout.get()) {
							handler.obtainMessage(CLEAR_MESSAGE).sendToTarget();
						}
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							break;
						}
					}
				}
			});
			t.start();
		}

		void stop() {
			if (t != null) {
				t.interrupt();
			}
		}
	}
}