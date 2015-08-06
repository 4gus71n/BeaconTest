package com.estimote.examples.demos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


/**
 * Displays basic information about beacon.
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class LeDeviceListAdapter extends BaseAdapter {

  private ArrayList<Beacon> beacons;
  private LayoutInflater inflater;

  public LeDeviceListAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
    this.beacons = new ArrayList<Beacon>();
  }

  public void replaceWith(Collection<Beacon> newBeacons) {
    for (Beacon b : newBeacons) {
        if (this.beacons.contains(b)) {
            this.beacons.set(this.beacons.indexOf(b), b);
        } else {
            this.beacons.add(b);
        }
    }
      Collections.sort(this.beacons, new Comparator<Beacon>() {
          @Override
          public int compare(Beacon lhs, Beacon rhs) {
              return Utils.computeProximity(lhs).compareTo(Utils.computeProximity(rhs));
          }
      });
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return beacons.size();
  }

  @Override
  public Beacon getItem(int position) {
    return beacons.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {
    view = inflateIfRequired(view, position, parent);
    bind(getItem(position), view);
    return view;
  }

  private void bind(Beacon beacon, View view) {
    ViewHolder holder = (ViewHolder) view.getTag();
    holder.macTextView.setText(String.format("MAC: %s (%.2fm)", beacon.getMacAddress(), Utils.computeAccuracy(beacon)));
      holder.macTextView.setTextColor((Utils.computeAccuracy(beacon) < 4)?(view.getContext().getResources().getColor(R.color.blue)):
              (view.getContext().getResources().getColor(R.color.red)));
    holder.majorTextView.setText("Major: " + beacon.getMajor() + " Name: " + getName(beacon.getMajor()));
    holder.minorTextView.setText("Minor: " + beacon.getMinor());
    holder.measuredPowerTextView.setText("MPower: " + beacon.getMeasuredPower());
    holder.rssiTextView.setText("RSSI: " + beacon.getRssi());
  }

    private String getName(int major) {
        switch (major) {
            case 6767:
                return "Mobile Island";
            case 7171:
                return "Meeting room";
            case 2323:
                return "Lunch court";
            default:
                return "Unkown";
        }
    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
    if (view == null) {
      view = inflater.inflate(R.layout.device_item, null);
      view.setTag(new ViewHolder(view));
    }
    return view;
  }

  static class ViewHolder {
    final TextView macTextView;
    final TextView majorTextView;
    final TextView minorTextView;
    final TextView measuredPowerTextView;
    final TextView rssiTextView;

    ViewHolder(View view) {
      macTextView = (TextView) view.findViewWithTag("mac");
      majorTextView = (TextView) view.findViewWithTag("major");
      minorTextView = (TextView) view.findViewWithTag("minor");
      measuredPowerTextView = (TextView) view.findViewWithTag("mpower");
      rssiTextView = (TextView) view.findViewWithTag("rssi");
    }
  }
}
