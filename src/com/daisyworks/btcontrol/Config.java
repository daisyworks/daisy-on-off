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

  private static final String PREF_LABEL = ".label";
  private static final String PREF_DEVICE_ID = ".device_id";
  private static final String PREF_PIN = ".pin";
  private static final String PREF_TYPE = ".type";
  private static final String PREF_POWER_ON = ".power_on";

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

  public static List<ButtonAttributes> loadButtons(final Context context)
  {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    final int version = prefs.getInt(VERSION_KEY, 0);

    final List<ButtonAttributes> buttonList = new ArrayList<ButtonAttributes>();

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

  public static ButtonAttributes loadButton(final Context context, final int buttonId)
  {
    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    return loadV3(sharedPrefs, buttonId);
  }

  public static void saveButton(final SharedPreferences prefs, final ButtonAttributes button)
  {
    final int buttonId = button.getButtonId();
    final Editor editor = prefs.edit();
    editor.putString(key(buttonId, PREF_LABEL), button.getLabel());
    editor.putString(key(buttonId, PREF_DEVICE_ID), button.getDeviceId());
    editor.putInt(key(buttonId, PREF_PIN), button.getPin());
    editor.putString(key(buttonId, PREF_TYPE), button.getBehavior().name());
    editor.putBoolean(CURRENT_POWER_STATE + buttonId, button.isPowerOn());
    editor.commit();
  }

  public static ButtonAttributes loadButton(final SharedPreferences sharedPrefs, final int buttonId)
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

  private static String key(final int buttonId, final String pref)
  {
    return BUTTON_PREFIX + buttonId + pref;
  }

  private static ButtonAttributes loadV3(final SharedPreferences sharedPrefs, final int buttonId)
  {
    final String label = sharedPrefs.getString(key(buttonId, PREF_LABEL), "Button");
    final String deviceId = sharedPrefs.getString(key(buttonId, PREF_DEVICE_ID), null);
    final int pin = sharedPrefs.getInt(key(buttonId, PREF_PIN), 0);
    final String behaviorString = sharedPrefs.getString(key(buttonId, PREF_TYPE), ButtonBehavior.ON_OFF.name());

    final boolean powerOn = sharedPrefs.getBoolean(key(buttonId, PREF_POWER_ON), false);

    final ButtonBehavior behavior = Enum.valueOf(ButtonBehavior.class, behaviorString);

    return new ButtonAttributes(sharedPrefs, buttonId, label, behavior, pin, deviceId, powerOn);
  }

  private static ButtonAttributes loadV1V2(final SharedPreferences sharedPrefs, final int buttonId)
  {
    final String v1DeviceId = sharedPrefs.getString("com.daisyworks.prefs.whichDaisy", null);

    final String label = sharedPrefs.getString("com.daisyworks.prefs.buttonLabel" + buttonId, "Button");
    final String pinString = sharedPrefs.getString("com.daisyworks.prefs.buttonPin" + buttonId, "0");
    final String behaviorString = sharedPrefs.getString("com.daisyworks.prefs.buttonType" + buttonId, "ON_OFF");
    final String deviceId = sharedPrefs.getString("com.daisyworks.prefs.button" + buttonId + "WhichDaisy", v1DeviceId);
    final boolean powerOn = sharedPrefs.getBoolean(CURRENT_POWER_STATE + buttonId, false);

    final int pin = Integer.valueOf(pinString);
    final ButtonBehavior behavior = Enum.valueOf(ButtonBehavior.class, behaviorString);

    final ButtonAttributes attr = new ButtonAttributes(sharedPrefs, buttonId, label, behavior, pin, deviceId, powerOn);

    return attr;
  }
}