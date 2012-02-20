package com.daisyworks.btcontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class Config
{
  public static final String LOG_TAG = "DaisyOnOffConfig";

  private static final String PREFS_PREFIX = "com.daisyworks.on_off.";
  private static final String VERSION_KEY = PREFS_PREFIX + "version";
  private static final String BUTTON_PREFIX = PREFS_PREFIX + "button.";
  private static final String IDS_KEY = PREFS_PREFIX + "button_ids";

  public static final String PREF_TARGET_TYPE = ".target_type";
  public static final String PREF_LABEL = ".label";
  public static final String PREF_DEVICE_ID = ".device_id";
  public static final String PREF_PIN = ".pin";
  public static final String PREF_TYPE = ".type";
  public static final String PREF_POWER_ON = ".power_on";
  public static final String PREF_SERVER = ".server";

  private static final String CURRENT_POWER_STATE = "com.daisyworks.prefs.powerOn";
  private static final String DEPRECATED_BUTTON_COUNT_KEY = "com.daisyworks.prefs.buttonCount";

  public static List<String> loadIds(final Context context)
  {
    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    final String buttonIdsStr = sharedPrefs.getString(IDS_KEY, null);

    final String[] buttonIds = buttonIdsStr == null || buttonIdsStr.trim().length() == 0 ? new String[0] : buttonIdsStr.split(",");

    final List<String> ids = new ArrayList<String>(12);
    ids.addAll(Arrays.asList(buttonIds));
    return ids;
  }

  public static int allocateNextId(final List<String> ids)
  {
    int id = 1;
    while (ids.contains(String.valueOf(id)))
    {
      id++;
    }

    return id;
  }

  public static void storeIds(final Context context, final List<String> ids)
  {
    final StringBuilder buf = new StringBuilder();
    final Iterator<String> iter = ids.iterator();

    if (iter.hasNext())
    {
      buf.append(iter.next());
    }
    while(iter.hasNext())
    {
      buf.append(",");
      buf.append(iter.next());
    }

    final String idsString = buf.toString();

    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    final Editor editor = sharedPrefs.edit();
    editor.putString(IDS_KEY, idsString);
    editor.commit();

    Log.i(LOG_TAG, "Stored ids: " + idsString);
  }

  public static List<AbstractOnOffButton> loadButtons(final Context context)
  {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    final int version = prefs.getInt(VERSION_KEY, 0);

    final List<AbstractOnOffButton> buttonList = new ArrayList<AbstractOnOffButton>();

    if (version < 3)
    {
      final List<String> ids = new ArrayList<String>();
      int buttonCount = Integer.valueOf(prefs.getString(DEPRECATED_BUTTON_COUNT_KEY, "0"));
      for (int i = 1; i <= buttonCount; i++)
      {
        buttonList.add(Config.loadButton(context, i));
        ids.add(String.valueOf(i));
      }
      Config.storeIds(context, ids);
      Editor editor = prefs.edit();
      editor.putInt(VERSION_KEY, 3);
      editor.commit();
    }
    else
    {
      final List<String> ids = loadIds(context);
      for (final String id : ids)
      {
        final int buttonId = Integer.valueOf(id);
        buttonList.add(loadButton(context, buttonId));
      }
    }

    return buttonList;
  }

  public static AbstractOnOffButton loadButton(final Context context, final int buttonId)
  {
    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    return loadV3(sharedPrefs, buttonId);
  }

  public static AbstractOnOffButton loadButton(final SharedPreferences sharedPrefs, final int buttonId)
  {
    final int version = sharedPrefs.getInt("com.daisyworks.on_off.version", 0);

    if (version < 3)
    {
      return loadV1V2(sharedPrefs, buttonId);
    }
    if (version == 3)
    {
      return loadV3(sharedPrefs, buttonId);
    }

    throw new IllegalStateException("Version " + version + " not supported.");
  }

  public static String key(final int buttonId, final String pref)
  {
    return BUTTON_PREFIX + buttonId + pref;
  }

  private static AbstractOnOffButton loadV3(final SharedPreferences sharedPrefs, final int buttonId)
  {
    final String targetTypeString = sharedPrefs.getString(key(buttonId, PREF_TARGET_TYPE), ButtonTargetType.BLUETOOTH.name());
    final ButtonTargetType targetType = Enum.valueOf(ButtonTargetType.class, targetTypeString);

    return targetType == ButtonTargetType.BLUETOOTH ? new BluetoothButton(sharedPrefs, buttonId) : new WifiButton(sharedPrefs, buttonId);
  }

  private static AbstractOnOffButton loadV1V2(final SharedPreferences sharedPrefs, final int buttonId)
  {
    final String v1DeviceId = sharedPrefs.getString("com.daisyworks.prefs.whichDaisy", null);

    final String label = sharedPrefs.getString("com.daisyworks.prefs.buttonLabel" + buttonId, "Button");
    final String pinString = sharedPrefs.getString("com.daisyworks.prefs.buttonPin" + buttonId, "0");
    final String behaviorString = sharedPrefs.getString("com.daisyworks.prefs.buttonType" + buttonId, "ON_OFF");
    final String deviceId = sharedPrefs.getString("com.daisyworks.prefs.button" + buttonId + "WhichDaisy", v1DeviceId);
    final boolean powerOn = sharedPrefs.getBoolean(CURRENT_POWER_STATE + buttonId, false);

    final int pin = Integer.valueOf(pinString);
    final ButtonBehavior behavior = Enum.valueOf(ButtonBehavior.class, behaviorString);

    final AbstractOnOffButton attr = new BluetoothButton(sharedPrefs, buttonId, label, behavior, pin, deviceId, powerOn);

    return attr;
  }
}