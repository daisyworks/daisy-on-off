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
package com.daisyworks.btcontrol.action;

import java.io.IOException;

import android.os.Handler;
import android.util.Log;

import com.daisyworks.android.bluetooth.AsyncReader;
import com.daisyworks.android.bluetooth.BTCommThread;
import com.daisyworks.android.bluetooth.BaseBluetoothAction;
import com.daisyworks.btcontrol.ButtonState;
import com.daisyworks.btcontrol.DaisyOnOffActivity;

public class SetupAction extends BaseBluetoothAction
{  
  @Override
  protected void performIOAction (final AsyncReader reader, final Handler handler) throws IOException
  {
    long startTime = System.currentTimeMillis();
    writeln("g@");
    String val = reader.readLine(1000);
    if(DaisyOnOffActivity.DEBUG) Log.i(BTCommThread.LOG_TAG, "SetupAction - Current GPIO direction mask: " + val);

    boolean pin3setToOutput = false;
    boolean pin6setToOutput = false;
    boolean pin7setToOutput = false;

    if (val != null)
    {
      final int mask = Integer.parseInt(val.trim(), 16);
      pin3setToOutput = (mask & 0x08) == 0x08;
      pin6setToOutput = (mask & 0x40) == 0x40;
      pin7setToOutput = (mask & 0x80) == 0x80;
    }

    if (!pin3setToOutput)
    {
      writeln("S@,0808");
      if(DaisyOnOffActivity.DEBUG) Log.i(BTCommThread.LOG_TAG, "SetupAction: Setting pin 3 output" + reader.readLine(200));
    }

    if (!pin6setToOutput)
    {
      writeln("S@,4040");
      if(DaisyOnOffActivity.DEBUG) Log.i(BTCommThread.LOG_TAG, "SetupAction: Setting pin 6 output" + reader.readLine(200));
    }

    if (!pin7setToOutput)
    {
      writeln("S@,8080");
      if(DaisyOnOffActivity.DEBUG) Log.i(BTCommThread.LOG_TAG, "SetupAction: Setting pin 7 output" + reader.readLine(200));
    }

    writeln("g&");
    val = reader.readLine(1000);
    if(DaisyOnOffActivity.DEBUG) Log.i(BTCommThread.LOG_TAG, "SetupAction - Current GPIO output mask: " + val);
    final int gpioMask = val == null ? 0 : Integer.parseInt(val.trim(), 16);

    writeln("g*");
    val = reader.readLine(1000);
    if(DaisyOnOffActivity.DEBUG) Log.i(BTCommThread.LOG_TAG, "SetupAction - Current PIO output mask: " + val);
    final int pioMask = val == null ? 0 : Integer.parseInt(val.trim(), 16);

    long totalTime = System.currentTimeMillis() - startTime;
    if(DaisyOnOffActivity.DEBUG) Log.i(BTCommThread.LOG_TAG, "SetupAction: took " + totalTime + "ms");

    handler.obtainMessage(DaisyOnOffActivity.BUTTON_STATE_MESSAGE, new ButtonState(gpioMask, pioMask)).sendToTarget();
  }
}
