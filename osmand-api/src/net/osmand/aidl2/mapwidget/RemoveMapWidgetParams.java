package net.osmand.aidl2.mapwidget;

import android.os.Bundle;
import android.os.Parcel;

import net.osmand.aidl2.AidlParams;

public class RemoveMapWidgetParams extends AidlParams {

	private String id;

	public RemoveMapWidgetParams(String id) {
		this.id = id;
	}

	public RemoveMapWidgetParams(Parcel in) {
		super(in);
	}

	public static final Creator<RemoveMapWidgetParams> CREATOR = new Creator<RemoveMapWidgetParams>() {
		@Override
		public RemoveMapWidgetParams createFromParcel(Parcel in) {
			return new RemoveMapWidgetParams(in);
		}

		@Override
		public RemoveMapWidgetParams[] newArray(int size) {
			return new RemoveMapWidgetParams[size];
		}
	};

	public String getId() {
		return id;
	}

	@Override
	public void writeToBundle(Bundle bundle) {
		bundle.putString("id", id);
	}

	@Override
	protected void readFromBundle(Bundle bundle) {
		id = bundle.getString("id");
	}
}