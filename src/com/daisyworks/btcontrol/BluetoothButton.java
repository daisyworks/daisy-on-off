package com.daisyworks.btcontrol;

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
   * @see com.daisyworks.btcontrol.AbstractOnOffButton#turnPinOn(com.daisyworks.btcontrol.DaisyOnOffActivity)
   */
  @Override
  public void turnPinOn(final DaisyOnOffActivity context)
  {
    context.bluetoothPinOn(this);
  }

  /**
   * @see com.daisyworks.btcontrol.AbstractOnOffButton#turnPinOff(com.daisyworks.btcontrol.DaisyOnOffActivity)
   */
  @Override
  public void turnPinOff(final DaisyOnOffActivity context)
  {
    context.bluetoothPinOff(this);
  }

  /**
   * @see com.daisyworks.btcontrol.AbstractOnOffButton#sendPulse(com.daisyworks.btcontrol.DaisyOnOffActivity)
   */
  @Override
  public void sendPulse(final DaisyOnOffActivity context)
  {
    context.bluetoothSendPulse(this);
  }
}
