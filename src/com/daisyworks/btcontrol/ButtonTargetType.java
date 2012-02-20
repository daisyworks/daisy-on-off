package com.daisyworks.btcontrol;

public enum ButtonTargetType
{
  BLUETOOTH(ConfigureBluetoothButtonActivity.class),
  WIFI(ConfigureWifiButtonActivity.class);

  private final Class<?> configurationActivity;

  private ButtonTargetType (final Class<?> configurationActivity)
  {
    this.configurationActivity = configurationActivity;
  }

  public Class<?> getConfigurationActivity()
  {
    return configurationActivity;
  }
}
