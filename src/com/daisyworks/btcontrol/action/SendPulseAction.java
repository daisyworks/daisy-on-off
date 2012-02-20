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

import com.daisyworks.android.ThreadUtil;
import com.daisyworks.android.bluetooth.AsyncReader;
import com.daisyworks.android.bluetooth.BTCommThread;
import com.daisyworks.android.bluetooth.BaseBluetoothAction;

public class SendPulseAction extends BaseBluetoothAction
{
  private final String cmdOn;
  private final String cmdOff;

  private final long[] pulseArray;


  public SendPulseAction (final String cmdOn, final String cmdOff, final long[] pulseArray)
  {
    this.cmdOn = cmdOn;
    this.cmdOff = cmdOff;
    this.pulseArray = pulseArray;
  }

  @Override
  protected void performIOAction (final AsyncReader reader, final Handler handler) throws IOException
  {
    writeln(cmdOn);
    Log.i(BTCommThread.LOG_TAG, "SendPulseAction: " + reader.readLine(100));

    boolean on = true;

    for (int i = 0; i < pulseArray.length; i++)
    {
      long target = System.currentTimeMillis() + pulseArray[i];
      writeln(on ? cmdOn : cmdOff);
      ThreadUtil.waitUntil(target);
      on = !on;
    }

    if (!on)
    {
      writeln(cmdOff);
    }

    Log.i(BTCommThread.LOG_TAG, "SendPulseAction: " + reader.readLine(100));
  }
}
