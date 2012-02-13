package com.daisyworks.btcontrol;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
  @SuppressWarnings("unused")
  private static final String LOG_TAG = "DaisyOnOffConfig";
  private static final Integer TYPE_ADD_BUTTON = 1;
  private static final Integer TYPE_CONFIG_BUTTON = 2;
  private static final Integer TYPE_REMOVE_BUTTON = 3;

  private static final int DIALOG_CONFIRM_REMOVE = 1;

  private View selectedButton = null;

  private List<String> ids = new LinkedList<String>();

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

    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    final String buttonIdsStr = sharedPrefs.getString(getString(R.string.prefs_button_ids_key), null);

    final String[] buttonIds = buttonIdsStr == null ? new String[0] : buttonIdsStr.split(",");

    ids.addAll(Arrays.asList(buttonIds));

    redoLayout();
  }

  protected void redoLayout()
  {
    Log.i(LOG_TAG, "Laying out with ids: " + ids);

    setContentView(R.layout.configure);

    final ViewGroup buttonsParent = (ViewGroup) findViewById(R.id.buttonsParent);
    final LayoutInflater inflater = getLayoutInflater();
    ViewGroup buttonRow = null;

    int i = 0;
    for (final String buttonId : ids)
    {
      if (i % 2 == 0)
      {
        buttonRow = newButtonRow(inflater, buttonsParent);
        final View button = buttonRow.findViewById(R.id.main_button1);
        button.setTag(R.id.buttonId, buttonId);
      }
      else
      {
        @SuppressWarnings("null")
        final View button = buttonRow.findViewById(R.id.main_button2);
        button.setTag(R.id.buttonId, buttonId);
      }
      i++;
    }

    if (ids.size() % 2 == 0)
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

    final View removeButton = (View) button.getTag(R.id.linkedButton);
    removeButton.setVisibility(View.GONE);

    if (first)
    {
      final Button button2 = (Button)buttonRow.findViewById(R.id.main_button2);
      button2.setVisibility(View.INVISIBLE);
      final View removeButton2 = (View) button2.getTag(R.id.linkedButton);
      removeButton2.setVisibility(View.INVISIBLE);
    }
    else
    {
      button.setVisibility(View.VISIBLE);
    }
  }

  public ViewGroup newButtonRow(final LayoutInflater inflater, final ViewGroup parent)
  {
    final ViewGroup buttonRow = (ViewGroup) inflater.inflate(R.layout.config_button_row, parent, false);

    final Button button1 = (Button)buttonRow.findViewById(R.id.main_button1);
    button1.setOnClickListener(this);
    button1.setTag(R.id.buttonType, TYPE_CONFIG_BUTTON);
    button1.setText("Configure");

    final View remove1 = buttonRow.findViewById(R.id.main_removeButton1);
    remove1.setOnClickListener(this);
    remove1.setTag(R.id.buttonType, TYPE_REMOVE_BUTTON);
    remove1.setTag(R.id.linkedButton, button1);
    button1.setTag(R.id.linkedButton, remove1);

    final Button button2 = (Button)buttonRow.findViewById(R.id.main_button2);
    button2.setOnClickListener(this);
    button2.setTag(R.id.buttonType, TYPE_CONFIG_BUTTON);
    button2.setText("Configure");

    final View remove2 = buttonRow.findViewById(R.id.main_removeButton2);
    remove2.setOnClickListener(this);
    remove2.setTag(R.id.buttonType, TYPE_REMOVE_BUTTON);
    remove2.setTag(R.id.linkedButton, button2);
    button2.setTag(R.id.linkedButton, remove2);

    parent.addView(buttonRow);

    return buttonRow;
  }

  /**
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  @Override
  public void onClick(final View v)
  {
    final Object buttonType = v.getTag(R.id.buttonType);
    if (buttonType == TYPE_ADD_BUTTON)
    {
      addButton((Button)v);
    }
    else if (buttonType == TYPE_CONFIG_BUTTON)
    {

    }
    else if (buttonType == TYPE_REMOVE_BUTTON)
    {
      selectedButton = v;
      showDialog(DIALOG_CONFIRM_REMOVE);
    }
  }

  /**
   * @see android.app.Activity#onCreateDialog(int)
   */
  @Override
  protected Dialog onCreateDialog(final int id)
  {
    if (id == DIALOG_CONFIRM_REMOVE)
    {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);

      builder.setPositiveButton("Remove", new DialogInterface.OnClickListener()
      {

        @Override
        public void onClick(final DialogInterface dialog, final int which)
        {
          removeSelectedButton();
        }
      });

      builder.setNegativeButton("Cancel", null);

      builder.setTitle("Confirm Remove");
      builder.setMessage("Remove this button?");
      return builder.create();
    }

    return super.onCreateDialog(id);
  }

  void removeSelectedButton()
  {
    final View button = (View) selectedButton.getTag(R.id.linkedButton);
    final String buttonId = (String)button.getTag(R.id.buttonId);
    Log.i(LOG_TAG, "Contains id '" + buttonId + "' " + ids.contains(buttonId));
    ids.remove(buttonId);

    storeIds();

    redoLayout();
  }

  protected void storeIds()
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

    final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    final Editor editor = sharedPrefs.edit();
    editor.putString(getString(R.string.prefs_button_ids_key), idsString);
    editor.commit();

    Log.i(LOG_TAG, "Stored ids: " + idsString);
  }

  private void addButton(final Button addButton)
  {
    final ViewGroup buttonsParent = (ViewGroup) findViewById(R.id.buttonsParent);
    if (addButton.getId() == R.id.main_button1)
    {
      addAddButton((ViewGroup) addButton.getParent().getParent(), false);
    }
    else if (buttonsParent.getChildCount() < 3)
    {
      final ViewGroup buttonRow = newButtonRow(getLayoutInflater(), buttonsParent);
      addAddButton(buttonRow, true);
    }

    addButton.setText("Configure");
    addButton.setTag(R.id.buttonType, TYPE_CONFIG_BUTTON);

    final String nextId = allocateNextId();
    addButton.setTag(R.id.buttonId, nextId);
    Log.i(LOG_TAG, "New button id set to '" + nextId + "', stored=" + addButton.getTag(R.id.buttonId) + " button: "  + addButton);

    final View removeButton = (View) addButton.getTag(R.id.linkedButton);
    removeButton.setVisibility(View.VISIBLE);
  }

  protected String allocateNextId()
  {
    int id = 1;
    while (ids.contains(String.valueOf(id)))
    {
      id++;
    }

    final String nextId = String.valueOf(id);
    ids.add(nextId);
    storeIds();
    return nextId;
  }
}