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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ButtonAttributes
{
  private static int[] PHYSICAL_PINS = {3,6,7,10,11};

  private static final String CURRENT_POWER_STATE = "com.daisyworks.prefs.powerOn";

  private final SharedPreferences prefs;

  private final int buttonId;
  private final int toggleButtonId;
  private final int buttonNumber;
  private final String label;
  private final ButtonBehavior behavior;
  private final int pin;
  private final String deviceId;
  private boolean powerOn;

  public static ButtonAttributes newInstance(final SharedPreferences sharedPrefs, final int buttonId, final int toggleButtonId, final int buttonNumber)
  {
    final String label = sharedPrefs.getString("com.daisyworks.prefs.buttonLabel" + buttonNumber, "Button");
    final String pinString = sharedPrefs.getString("com.daisyworks.prefs.buttonPin" + buttonNumber, "0");
    final String behaviorString = sharedPrefs.getString("com.daisyworks.prefs.buttonType" + buttonNumber, "ON_OFF");
    final String deviceId = sharedPrefs.getString("com.daisyworks.prefs.button" + buttonNumber + "WhichDaisy", null);
    final boolean powerOn = sharedPrefs.getBoolean(CURRENT_POWER_STATE + buttonNumber, false);

    final int pin = Integer.valueOf(pinString);
    final ButtonBehavior behavior = Enum.valueOf(ButtonBehavior.class, behaviorString);

    return new ButtonAttributes(sharedPrefs, buttonId, toggleButtonId, buttonNumber, label, behavior, pin, deviceId, powerOn);
  }

  public ButtonAttributes (final SharedPreferences prefs,
                           final int buttonId,
                           final int toggleButtonId,
                           final int buttonNumber,
                           final String label,
                           final ButtonBehavior behavior,
                           final int pin,
                           final String deviceId,
                           final boolean powerOn)
  {
    this.prefs = prefs;
    this.buttonId = buttonId;
    this.toggleButtonId = toggleButtonId;
    this.buttonNumber = buttonNumber;
    this.label = label;
    this.behavior = behavior;
    this.pin = Math.min(Math.max(pin,0), 4); // ensure pin is between 0 and 4
    this.deviceId = deviceId;
    this.powerOn = powerOn;
  }

  public int getButtonId ()
  {
    return buttonId;
  }

  public int getToggleButtonId ()
  {
    return toggleButtonId;
  }

  public int getButtonNumber ()
  {
    return buttonNumber;
  }

  public String getLabel ()
  {
    return label;
  }

  public ButtonBehavior getBehavior ()
  {
    return behavior;
  }

  public int getPin ()
  {
    return pin;
  }

  public int getPhysicalPin ()
  {
    return PHYSICAL_PINS[pin];
  }

  public String getDeviceId()
  {
    return deviceId;
  }

  public boolean isPowerOn ()
  {
    return powerOn;
  }

  public void setPowerOn (final boolean powerOn)
  {
    this.powerOn = powerOn;

    final Editor editor = prefs.edit();
    editor.putBoolean(CURRENT_POWER_STATE + buttonNumber, powerOn);
    editor.commit();
  }
}