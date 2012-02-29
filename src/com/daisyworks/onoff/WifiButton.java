package com.daisyworks.onoff;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class WifiButton extends AbstractOnOffButton
{
  private String server;

  public WifiButton (final SharedPreferences prefs,
                     final int buttonId,
                     final String label,
                     final ButtonBehavior behavior,
                     final int pin,
                     final String deviceId,
                     final String server,
                     final boolean powerOn)
  {
    super(prefs, ButtonTargetType.WIFI, buttonId, label, behavior, pin, deviceId, powerOn);
    this.server = server;
  }

  public WifiButton (final SharedPreferences prefs, final int buttonId)
  {
    super(prefs, ButtonTargetType.WIFI, buttonId);
  }

  public String getServer()
  {
    return server;
  }

  /**
   * @see com.daisyworks.onoff.AbstractOnOffButton#turnPinOn(com.daisyworks.onoff.DaisyOnOffActivity)
   */
  @Override
  public void turnPinOn(final DaisyOnOffActivity context)
  {
    context.sendWifiCommand(this, "toggle/true");
  }

  /**
   * @see com.daisyworks.onoff.AbstractOnOffButton#turnPinOff(com.daisyworks.onoff.DaisyOnOffActivity)
   */
  @Override
  public void turnPinOff(final DaisyOnOffActivity context)
  {
    context.sendWifiCommand(this, "toggle/false");
  }

  /**
   * @see com.daisyworks.onoff.AbstractOnOffButton#sendPulse(com.daisyworks.onoff.DaisyOnOffActivity)
   */
  @Override
  public void sendPulse(final DaisyOnOffActivity context)
  {
    context.sendWifiCommand(this, "pulse/1000");
  }

  /**
   * @see com.daisyworks.onoff.AbstractOnOffButton#save(android.content.SharedPreferences.Editor)
   */
  @Override
  public void save(final Editor editor)
  {
    super.save(editor);
    editor.putString(Config.key(getButtonId(), Config.PREF_SERVER), server);
  }

  @Override
  public void load()
  {
    super.load();
    server = prefs.getString(Config.key(getButtonId(), Config.PREF_SERVER), null);
  }
}