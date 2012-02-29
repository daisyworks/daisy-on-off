package com.daisyworks.onoff;

import android.content.SharedPreferences;

public class BluetoothButton extends AbstractOnOffButton
{
  public BluetoothButton (final SharedPreferences prefs,
                          final int buttonId,
                          final String label,
                          final ButtonBehavior behavior,
                          final int pin,
                          final String deviceId,
                          final boolean powerOn)
  {
    super(prefs, ButtonTargetType.BLUETOOTH, buttonId, label, behavior, pin, deviceId, powerOn);
  }

  public BluetoothButton (final SharedPreferences prefs, final int buttonId)
  {
    super(prefs, ButtonTargetType.BLUETOOTH, buttonId);
  }

  /**
   * @see com.daisyworks.onoff.AbstractOnOffButton#turnPinOn(com.daisyworks.onoff.DaisyOnOffActivity)
   */
  @Override
  public void turnPinOn(final DaisyOnOffActivity context)
  {
    context.bluetoothPinOn(this);
  }

  /**
   * @see com.daisyworks.onoff.AbstractOnOffButton#turnPinOff(com.daisyworks.onoff.DaisyOnOffActivity)
   */
  @Override
  public void turnPinOff(final DaisyOnOffActivity context)
  {
    context.bluetoothPinOff(this);
  }

  /**
   * @see com.daisyworks.onoff.AbstractOnOffButton#sendPulse(com.daisyworks.onoff.DaisyOnOffActivity)
   */
  @Override
  public void sendPulse(final DaisyOnOffActivity context)
  {
    context.bluetoothSendPulse(this);
  }
}
