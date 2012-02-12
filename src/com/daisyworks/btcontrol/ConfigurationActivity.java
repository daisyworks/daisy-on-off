package com.daisyworks.btcontrol;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.daisyworks.android.bluetooth.R;

public class ConfigurationActivity extends Activity implements OnClickListener
{
  private static final String LOG_TAG = "DaisyOnOffConfig";
  private static final Integer TYPE_ADD_BUTTON = 1;
  private static final Integer TYPE_CONFIG_BUTTON = 2;

  @Override
  protected void onCreate (final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  /**
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume()
  {
    super.onResume();
    setContentView(R.layout.configure);

    final ViewGroup buttonsParent = (ViewGroup) findViewById(R.id.buttonsParent);

    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    final String buttonIdsStr = sharedPrefs.getString(getString(R.string.prefs_button_ids_key), null);

    final String[] buttonIds = buttonIdsStr == null ? new String[0] : buttonIdsStr.split(",");

    final LayoutInflater inflater = getLayoutInflater();
    ViewGroup buttonRow = null;

    for (int i = 0; i < buttonIds.length; i++)
    {
      if (i % 2 == 0)
      {
        buttonRow = newButtonRow(inflater, buttonsParent);
      }
    }

    if (buttonIds.length % 2 == 0)
    {
      buttonRow = newButtonRow(inflater, buttonsParent);
      addAddButton(buttonRow, true);
    }
    else
    {
      addAddButton(buttonRow, false);
    }
  }

  public void addAddButton(final ViewGroup buttonRow, final boolean first)
  {
    final Button button = (Button)buttonRow.findViewById(first ? R.id.main_button1 : R.id.main_button2);
    button.setText("Add Button");
    button.setTag(R.id.buttonType, TYPE_ADD_BUTTON);

    if (first)
    {
      buttonRow.findViewById(R.id.main_button2).setVisibility(View.INVISIBLE);
    }
    else
    {
      button.setVisibility(View.VISIBLE);
    }
  }

  public ViewGroup newButtonRow(final LayoutInflater inflater, final ViewGroup parent)
  {
    final ViewGroup buttonRow = (ViewGroup) inflater.inflate(R.layout.button_row, parent, false);
    remove(buttonRow.findViewById(R.id.main_toggleButton1));
    remove(buttonRow.findViewById(R.id.main_toggleButton2));

    final Button button1 = (Button)buttonRow.findViewById(R.id.main_button1);
    button1.setOnClickListener(this);
    button1.setTag(R.id.buttonType, TYPE_CONFIG_BUTTON);
    button1.setText("Configure");

    final Button button2 = (Button)buttonRow.findViewById(R.id.main_button2);
    button2.setOnClickListener(this);
    button2.setTag(R.id.buttonType, TYPE_CONFIG_BUTTON);
    button2.setText("Configure");

    parent.addView(buttonRow);

    return buttonRow;
  }

  private void remove(final View view)
  {
    ((ViewGroup)view.getParent()).removeView(view);
  }

  /**
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  @Override
  public void onClick(final View v)
  {
    if (v instanceof Button)
    {
      final Button b = (Button)v;
      if (b.getTag(R.id.buttonType) == TYPE_ADD_BUTTON)
      {
        addButton(b);
      }
    }
  }

  private void addButton(final Button addButton)
  {
    if (addButton.getId() == R.id.main_button1)
    {
      addAddButton((ViewGroup) addButton.getParent(), false);
    }
    else
    {
      final ViewGroup buttonsParent = (ViewGroup) findViewById(R.id.buttonsParent);
      final ViewGroup buttonRow = newButtonRow(getLayoutInflater(), buttonsParent);
      Log.i(LOG_TAG, "Parent: " + buttonsParent);
      Log.i(LOG_TAG, "New Button Row: " + buttonRow);
      Log.i(LOG_TAG, "Children: " + buttonsParent.getChildCount());


      addAddButton(buttonRow, true);

      ViewGroup top = (ViewGroup) findViewById(R.id.contentView);
      top.invalidate();
      top.requestLayout();
      top.refreshDrawableState();
    }

    addButton.setText("Configure");
    addButton.setTag(R.id.buttonType, TYPE_CONFIG_BUTTON);
  }
}
