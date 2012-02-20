package com.daisyworks.btcontrol;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
  private static final Integer TYPE_CONFIG_BUTTON = 2;
  private static final Integer TYPE_REMOVE_BUTTON = 3;

  private static final int DIALOG_CONFIRM_REMOVE = 1;
  private static final int DIALOG_BUTTON_TYPE = 2;

  private View selectedButton = null;

  private List<String> ids = null;

  @Override
  protected void onCreate(final Bundle savedInstanceState)
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
    ids = Config.loadIds(this);
    redoLayout();
  }

  @SuppressWarnings("null")
  protected void redoLayout()
  {
    Log.i(LOG_TAG, "Laying out with ids: " + ids);

    setContentView(R.layout.configure);

    findViewById(R.id.doneButton).setOnClickListener(this);
    findViewById(R.id.addButton).setOnClickListener(this);

    final ViewGroup buttonsParent = (ViewGroup) findViewById(R.id.buttonsParent);
    final LayoutInflater inflater = getLayoutInflater();
    ViewGroup buttonRow = null;

    int i = 0;
    for (final String buttonId : ids)
    {
      Button button = null;
      if (i % 2 == 0)
      {
        buttonRow = newButtonRow(inflater, buttonsParent);
        button = (Button) buttonRow.findViewById(R.id.main_button1);
      }
      else
      {
        button = ((Button)buttonRow.findViewById(R.id.main_button2));
      }

      ButtonAttributes buttonAttr = Config.loadButton(this, Integer.valueOf(buttonId));
      final String label = button.getText() + "\n" + buttonAttr.getLabel();
      button.setText(label);
      button.setTag(R.id.buttonId, buttonId);
      button.setTag(R.id.buttonAttributes, buttonAttr);

      i++;
    }

    if (i % 2 != 0)
    {
      final Button button2 = (Button)buttonRow.findViewById(R.id.main_button2);
      button2.setVisibility(View.INVISIBLE);
      final View removeButton2 = (View) button2.getTag(R.id.linkedButton);
      removeButton2.setVisibility(View.INVISIBLE);
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
    if (v.getId() == R.id.addButton)
    {
      showDialog(DIALOG_BUTTON_TYPE);
    }
    else if (v.getId() == R.id.doneButton)
    {
      finish();
    }
    else if (buttonType == TYPE_CONFIG_BUTTON)
    {
      final ButtonAttributes buttonAttr = (ButtonAttributes) v.getTag(R.id.buttonAttributes);
      final Intent intent = new Intent(this, buttonAttr.getTargetType().getConfigurationActivity());
      intent.putExtra("buttonId", Integer.parseInt((String)v.getTag(R.id.buttonId)));
      startActivity(intent);
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
      return createConfirmRemoveDialog();
    }
    if (id == DIALOG_BUTTON_TYPE)
    {
      return createButtonTypeDialog();
    }

    return super.onCreateDialog(id);
  }

  Dialog createConfirmRemoveDialog()
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setPositiveButton("Remove", new DialogInterface.OnClickListener()
    {
      @Override public void onClick(final DialogInterface dialog, final int which)
      {
        removeSelectedButton();
      }
    });

    builder.setNegativeButton("Cancel", null);
    builder.setTitle("Confirm Remove");
    builder.setMessage("Remove this button?");
    return builder.create();
  }

  Dialog createButtonTypeDialog()
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setPositiveButton("WIFI", new DialogInterface.OnClickListener()
    {
      @Override public void onClick(final DialogInterface dialog, final int which)
      {
        final Intent intent = new Intent(ConfigurationActivity.this, ConfigureWifiButtonActivity.class);
        startActivity(intent);
      }
    });

    builder.setNeutralButton("Bluetooth", new DialogInterface.OnClickListener()
    {
      @Override public void onClick(final DialogInterface dialog, final int which)
      {
        final Intent intent = new Intent(ConfigurationActivity.this, ConfigureBluetoothButtonActivity.class);
        startActivity(intent);
      }
    });

    builder.setNegativeButton("Cancel", null);
    builder.setTitle("Select Type");
    builder.setMessage("Will this button work with a Bluetooth Daisy or WiFi Daisy?");
    return builder.create();
  }

  void removeSelectedButton()
  {
    int oldSize = ids.size();
    final View button = (View) selectedButton.getTag(R.id.linkedButton);
    final String buttonId = (String)button.getTag(R.id.buttonId);
    Log.i(LOG_TAG, "Contains id '" + buttonId + "' " + ids.contains(buttonId));
    ids.remove(buttonId);

    Config.storeIds(this, ids);

    redoLayout();

    if (oldSize == 6)
    {
      findViewById(R.id.addButton).setEnabled(true);
    }
  }
}