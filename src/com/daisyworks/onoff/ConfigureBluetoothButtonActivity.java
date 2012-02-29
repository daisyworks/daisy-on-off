package com.daisyworks.onoff;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.daisyworks.android.widget.AbstractTextWatcher;
import com.daisyworks.android.widget.ListEntry;

public class ConfigureBluetoothButtonActivity extends Activity implements OnClickListener
{
  private int buttonId = 0;
  private boolean powerOn = false;

  /**
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.configure_bt_button);
  }

  /**
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume()
  {
    super.onResume();

    final Set<BluetoothDevice> deviceSet = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    final List<ListEntry> devices = new ArrayList<ListEntry>(deviceSet.size());

    for (final BluetoothDevice device : deviceSet)
    {
      devices.add(new ListEntry(device.getAddress(), device.getName()));
    }

    final Spinner daisyInput = (Spinner) findViewById(R.id.daisyInput);
    final ArrayAdapter<ListEntry> daisyInputAdapter = ListEntry.fromList(this, devices, android.R.layout.simple_spinner_item, true);
    daisyInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    daisyInput.setAdapter(daisyInputAdapter);

    final Spinner pinInput = (Spinner) findViewById(R.id.pinInput);
    final ArrayAdapter<ListEntry> pinInputAdapter =
        ListEntry.fromResources(this,
                                R.array.bt_pin_entry_values,
                                R.array.bt_pin_entries,
                                android.R.layout.simple_spinner_item,
                                false);
    pinInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    pinInput.setAdapter(pinInputAdapter);

    final Spinner buttonTypeInput = (Spinner) findViewById(R.id.buttonTypeInput);
    final ArrayAdapter<ListEntry> buttonTypeInputAdapter =
        ListEntry.fromResources(this,
                                R.array.button_type_entry_values,
                                R.array.button_type_entries,
                                android.R.layout.simple_spinner_item,
                                false);
    buttonTypeInputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    buttonTypeInput.setAdapter(buttonTypeInputAdapter);

    findViewById(R.id.cancelButton).setOnClickListener(this);
    final Button saveButton = (Button)findViewById(R.id.saveButton);
    saveButton.setOnClickListener(this);

    final EditText labelInput = (EditText)findViewById(R.id.buttonLabelInput);
    labelInput.addTextChangedListener(new AbstractTextWatcher()
    {
      @Override
      public void afterTextChanged(final Editable s)
      {
        boolean hasLabel = false;
        final int length = s.length();
        for (int i = 0; i < length; i++)
        {
          if (!Character.isWhitespace(s.charAt(i)))
          {
            hasLabel = true;
            break;
          }
        }

        saveButton.setEnabled(hasLabel);
      }
    });

    Bundle extras = getIntent().getExtras();
    buttonId = extras == null ? 0 : extras.getInt("buttonId");

    if (buttonId > 0)
    {
      final AbstractOnOffButton button = Config.loadButton(this, buttonId);
      labelInput.setText(button.getLabel());
      setSelection(daisyInput, button.getDeviceId());
      setSelection(pinInput, Integer.toString(button.getPin()));
      setSelection(buttonTypeInput, button.getBehavior().name());
      powerOn = button.isPowerOn();
      saveButton.setEnabled(true);
    }
    else
    {
      saveButton.setEnabled(false);
    }
  }

  private void setSelection(final Spinner spinner, final String value)
  {
    if (value == null)
    {
      return;
    }
    @SuppressWarnings("unchecked")
    final ArrayAdapter<ListEntry> adapter = (ArrayAdapter<ListEntry>) spinner.getAdapter();
    for (int i = 0; i < adapter.getCount(); i++)
    {
      if (value.equals(adapter.getItem(i).id))
      {
        spinner.setSelection(i);
        return;
      }
    }
  }

  /**
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  @Override
  public void onClick(final View v)
  {
    if (v.getId() == R.id.cancelButton)
    {
      finish();
    }
    else if (v.getId() == R.id.saveButton)
    {
      save();
      finish();
    }
  }

  public void save()
  {
    List<String> ids = null;
    if (buttonId < 1)
    {
      ids = Config.loadIds(this);
      buttonId = Config.allocateNextId(ids);
    }

    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    final EditText labelInput = ((EditText)findViewById(R.id.buttonLabelInput));
    final Spinner daisyInput = (Spinner) findViewById(R.id.daisyInput);
    final Spinner pinInput = (Spinner) findViewById(R.id.pinInput);
    final Spinner buttonTypeInput = (Spinner) findViewById(R.id.buttonTypeInput);

    final String label = labelInput.getText().toString();
    final String deviceId = ((ListEntry)daisyInput.getSelectedItem()).id;
    final int pin = Integer.valueOf(((ListEntry)pinInput.getSelectedItem()).id);
    final String typeString = ((ListEntry)buttonTypeInput.getSelectedItem()).id;
    final ButtonBehavior behavior = Enum.valueOf(ButtonBehavior.class, typeString);

    final BluetoothButton button = new BluetoothButton(prefs, buttonId, label, behavior, pin, deviceId, powerOn);
    button.save();

    if (ids != null)
    {
      ids.add(String.valueOf(buttonId));
      Config.storeIds(this, ids);
    }
  }
}