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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import com.daisyworks.android.bluetooth.AbstractBluetoothActivity;
import com.daisyworks.android.bluetooth.BTCommThread;
import com.daisyworks.android.bluetooth.EnterCmdModeAction;
import com.daisyworks.android.bluetooth.R;
import com.daisyworks.btcontrol.action.SendOnOffAction;
import com.daisyworks.btcontrol.action.SendPulseAction;
import com.daisyworks.btcontrol.action.SetupAction;

public class DaisyOnOffActivity extends AbstractBluetoothActivity implements OnClickListener, OnTouchListener
{
  public final static int BUTTON_STATE_MESSAGE = 100;

  // private static final String LOG_TAG = "BluetoothControlActivity";

  private static final int OFF = 0;
  private static final int ON = 1;
  private static final String[][] COMMANDS =
      new String[][] { new String[] {"S&,0800", "S&,0808" },
                       new String[] {"S&,4000", "S&,4040" },
                       new String[] {"S&,8000", "S&,8080" },
                       new String[] {"S*,0400", "S*,0404" },
                       new String[] {"S*,0800", "S*,0808" }};

  private List<ButtonAttributes> buttonAttributes;

  private Map<String, BTCommThread> devices = new HashMap<String, BTCommThread>();
  private Map<Integer, ToggleButton> toggleButtons = new HashMap<Integer, ToggleButton>();
  private final Handler handler = new CommHandler();

  public DaisyOnOffActivity()
  {
    super(R.id.main_connectionProgress, R.id.main_connectionStatus);
  }

  @Override
  protected void bluetoothEnabled ()
  {
    buttonAttributes = Config.loadButtons(this);
    redoLayout();

    stopCommThreads();

    for (final ButtonAttributes buttonAttr : buttonAttributes)
    {
      final String deviceId = buttonAttr.getDeviceId();
      if (!devices.containsKey(deviceId))
      {
        final BTCommThread btComm = getBtCommThreadforNewActivity(handler, deviceId, 60000, new EnterCmdModeAction());
        devices.put(deviceId, btComm);
        btComm.enqueueAction(new SetupAction());
      }
    }
  }

  @SuppressWarnings("null")
  protected void redoLayout()
  {
    toggleButtons.clear();

    setContentView(R.layout.main);

    findViewById(R.id.configureButton).setOnClickListener(this);

    final ViewGroup buttonsParent = (ViewGroup) findViewById(R.id.buttonsParent);
    final LayoutInflater inflater = getLayoutInflater();
    ViewGroup buttonRow = null;

    int i = 0;
    for (final ButtonAttributes buttonAttr : buttonAttributes)
    {
      int pushButtonId = 0;
      int toggleButtonId = 0;
      if (i % 2 == 0)
      {
        buttonRow = newButtonRow(inflater, buttonsParent);
        pushButtonId = R.id.main_pushButton1;
        toggleButtonId = R.id.main_toggleButton1;
      }
      else
      {
        pushButtonId = R.id.main_pushButton2;
        toggleButtonId = R.id.main_toggleButton2;
      }

      final Button pushButton = (Button)buttonRow.findViewById(pushButtonId);
      final ToggleButton toggleButton = (ToggleButton)buttonRow.findViewById(toggleButtonId);
      final ViewGroup layout = (ViewGroup) pushButton.getParent();

      Button button = pushButton;
      switch(buttonAttr.getBehavior()) {
        case HOLD_PULSE :
          layout.removeView(toggleButton);
          button.setOnTouchListener(this);
          break;
        case PULSE :
          button.setOnClickListener(this);
          layout.removeView(toggleButton);
          break;
        case ON_OFF :
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

    if (i % 2 != 0)
    {
      final View pushButton2 = buttonRow.findViewById(R.id.main_pushButton2);
      pushButton2.setVisibility(View.INVISIBLE);

      final View toggleButton2 = buttonRow.findViewById(R.id.main_toggleButton2);
      toggleButton2.setVisibility(View.GONE);
    }
  }

  public ViewGroup newButtonRow(final LayoutInflater inflater, final ViewGroup parent)
  {
    final ViewGroup buttonRow = (ViewGroup) inflater.inflate(R.layout.button_row, parent, false);
    parent.addView(buttonRow);
    return buttonRow;
  }

  @Override
  protected void onPause ()
  {
    super.onPause();
    stopCommThreads();
  }

  protected void stopCommThreads()
  {
    for (final BTCommThread btComm : devices.values())
    {
      if (btComm != null)
      {
        btComm.shutdown();
      }
    }

    devices.clear();
  }

  @Override
  public boolean onCreateOptionsMenu (final Menu menu)
  {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.layout.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected (final MenuItem item)
  {
    if (item.getItemId() == R.id.menu_help)
    {
      final Intent intent = new Intent(this, HelpActivity.class);
      startActivity(intent);
    }
    return super.onOptionsItemSelected(item);
  }

  void updateButtonState(final ButtonState buttonState)
  {
    for (final ButtonAttributes buttonAttr : buttonAttributes)
    {
      final boolean isOn = buttonState.isPinHigh(buttonAttr.getPhysicalPin());

      if (buttonAttr.getBehavior() == ButtonBehavior.ON_OFF)
      {
        final ToggleButton button = toggleButtons.get(buttonAttr.getButtonId());
        if (button != null)
        {
          button.setChecked(isOn);
        }
        buttonAttr.setPowerOn(isOn);
      }
      else if (buttonAttr.isPowerOn())
      {
        pinOff(buttonAttr);
        buttonAttr.setPowerOn(false);
      }
    }
  }

  private void pinOn(final ButtonAttributes button)
  {
    final String deviceId = button.getDeviceId();
    final BTCommThread btComm = devices.get(deviceId);
    final int pin = button.getPin();

    btComm.enqueueAction(new SendOnOffAction(COMMANDS[pin][ON]));
  }

  private void pinOff(final ButtonAttributes button)
  {
    final String deviceId = button.getDeviceId();
    final BTCommThread btComm = devices.get(deviceId);
    final int pin = button.getPin();

    btComm.enqueueAction(new SendOnOffAction(COMMANDS[pin][OFF]));
  }

  private void sendPulse(final ButtonAttributes button)
  {
    final String deviceId = button.getDeviceId();
    final BTCommThread btComm = devices.get(deviceId);
    final int pin = button.getPin();

    btComm.enqueueAction(new SendPulseAction(COMMANDS[pin][ON], COMMANDS[pin][OFF]));
  }

  @Override
  public boolean onTouch (final View v, final MotionEvent event)
  {
    final ButtonAttributes buttonAttr = (ButtonAttributes)v.getTag(R.id.buttonAttributes);

    if (event.getAction() == MotionEvent.ACTION_DOWN)
    {
      pinOn(buttonAttr);
    }
    else if (event.getAction() == MotionEvent.ACTION_UP)
    {
      pinOff(buttonAttr);
    }
    return false;
  }

  @Override
  public void onClick (final View v)
  {
    if (v.getId() == R.id.configureButton)
    {
      startActivity(new Intent(this, ConfigurationActivity.class));
      return;
    }
    final ButtonAttributes buttonAttr = (ButtonAttributes)v.getTag(R.id.buttonAttributes);
    if (buttonAttr.getBehavior() == ButtonBehavior.PULSE)
    {
      sendPulse(buttonAttr);
    }
    else if (buttonAttr.getBehavior() == ButtonBehavior.ON_OFF)
    {
      if (buttonAttr.isPowerOn())
      {
        pinOff(buttonAttr);
        buttonAttr.setPowerOn(false);
      }
      else
      {
        pinOn(buttonAttr);
        buttonAttr.setPowerOn(true);
      }
      ((Button)v).setText(buttonAttr.getLabel());
    }
  }

  class CommHandler extends Handler
  {
    @SuppressWarnings("synthetic-access")
    @Override
    public void handleMessage(final Message msg)
    {
      switch(msg.what) {
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
          updateButtonState((ButtonState)msg.obj);
          break;
        default:
          super.handleMessage(msg);
      }
    }
  }
}