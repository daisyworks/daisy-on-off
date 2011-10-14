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

import java.util.LinkedList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class BluetoothControlPreferenceActivity extends PreferenceActivity
{
  @Override
  public void onCreate (final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.main_prefs);

    final List<String> names = new LinkedList<String>();
    final List<String> ids = new LinkedList<String>();
    for (final BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices())
    {
      names.add(device.getName());
      ids.add(device.getAddress());
    }

    final ListPreference pref = (ListPreference)findPreference(getString(R.string.prefs_which_daisy_key));
    pref.setEntries(names.toArray(new String[names.size()]));
    pref.setEntryValues(ids.toArray(new String[ids.size()]));
  }
}
