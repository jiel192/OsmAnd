package net.osmand.plus.activities;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import net.osmand.plus.GpxSelectionHelper;
import net.osmand.plus.GpxSelectionHelper.GpxDisplayGroup;
import net.osmand.plus.GpxSelectionHelper.GpxDisplayItem;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

public class SelectedGPXFragment extends OsmandExpandableListFragment {

	public static final int SEARCH_ID = -1;
	public static final int ACTION_ID = 0;
	protected static final int DELETE_ACTION_ID = 1;
	private ActionMode actionMode;
	private SearchView searchView;
	private OsmandApplication app;
	private GpxSelectionHelper selectedGpxHelper;
	private SelectedGPXAdapter adapter;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		final Collator collator = Collator.getInstance();
		collator.setStrength(Collator.SECONDARY);
		app = (OsmandApplication) activity.getApplication();
		selectedGpxHelper = app.getSelectedGpxHelper();

		adapter = new SelectedGPXAdapter();
		setAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter.setDisplayGroups(selectedGpxHelper.getDisplayGroups());
	}


	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		if (item.getItemId() == ACTION_ID) {
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem mi = createMenuItem(menu, SEARCH_ID, R.string.export_fav, R.drawable.ic_action_search_light,
				R.drawable.ic_action_search_dark, MenuItem.SHOW_AS_ACTION_ALWAYS
						| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		searchView = new com.actionbarsherlock.widget.SearchView(getActivity());
		mi.setActionView(searchView);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return true;
			}
		});
		createMenuItem(menu, ACTION_ID, R.string.export_fav, R.drawable.ic_action_gsave_light,
				R.drawable.ic_action_gsave_dark, MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	public void showProgressBar() {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
	}

	public void hideProgressBar() {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}


	class SelectedGPXAdapter extends OsmandBaseExpandableListAdapter  {

		Filter myFilter;
		private List<GpxDisplayGroup> displayGroups = new ArrayList<GpxDisplayGroup>();
		
		public void setDisplayGroups(List<GpxDisplayGroup> displayGroups) {
			this.displayGroups = displayGroups;
		}


		@Override
		public GpxDisplayItem getChild(int groupPosition, int childPosition) {
			GpxDisplayGroup group = getGroup(groupPosition);
			return group.getModifiableList().get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition * 10000 + childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return getGroup(groupPosition).getModifiableList().size();
		}

		@Override
		public GpxDisplayGroup getGroup(int groupPosition) {
			return displayGroups.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return displayGroups.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.expandable_list_item_category, parent, false);
				fixBackgroundRepeat(row);
			}
			adjustIndicator(groupPosition, isExpanded, row);
			TextView label = (TextView) row.findViewById(R.id.category_name);
			final GpxDisplayGroup model = getGroup(groupPosition);
			label.setText(model.getGroupName());
//			final CheckBox ch = (CheckBox) row.findViewById(R.id.check_item);
//			if (selectionMode) {
//				ch.setVisibility(View.VISIBLE);
//				ch.setChecked(groupsToDelete.contains(model));
//
//				ch.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (ch.isChecked()) {
//							groupsToDelete.add(model);
//							List<FavouritePoint> fvs = helper.getFavoriteGroups().get(model);
//							if (fvs != null) {
//								favoritesToDelete.addAll(fvs);
//							}
//							favouritesAdapter.notifyDataSetInvalidated();
//						} else {
//							groupsToDelete.remove(model);
//						}
//					}
//				});
//			} else {
//				ch.setVisibility(View.GONE);
//			}
			return row;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.favourites_list_item, parent, false);
			}

//			TextView label = (TextView) row.findViewById(R.id.favourite_label);
//			ImageView icon = (ImageView) row.findViewById(R.id.favourite_icon);
//			final FavouritePoint model = (FavouritePoint) getChild(groupPosition, childPosition);
//			row.setTag(model);
//			if (model.isStored()) {
//				icon.setImageResource(R.drawable.list_favorite);
//			} else {
//				icon.setImageResource(R.drawable.opened_poi);
//			}
//			LatLon lastKnownMapLocation = getMyApplication().getSettings().getLastKnownMapLocation();
//			int dist = (int) (MapUtils.getDistance(model.getLatitude(), model.getLongitude(),
//					lastKnownMapLocation.getLatitude(), lastKnownMapLocation.getLongitude()));
//			String distance = OsmAndFormatter.getFormattedDistance(dist, getMyApplication()) + "  ";
//			label.setText(distance + model.getName(), TextView.BufferType.SPANNABLE);
//			((Spannable) label.getText()).setSpan(
//					new ForegroundColorSpan(getResources().getColor(R.color.color_distance)), 0, distance.length() - 1,
//					0);
//			final CheckBox ch = (CheckBox) row.findViewById(R.id.check_item);
//			if (selectionMode && model.isStored()) {
//				ch.setVisibility(View.VISIBLE);
//				ch.setChecked(favoritesToDelete.contains(model));
//				row.findViewById(R.id.favourite_icon).setVisibility(View.GONE);
//				ch.setOnClickListener(new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						if (ch.isChecked()) {
//							favoritesToDelete.add(model);
//						} else {
//							favoritesToDelete.remove(model);
//							if (groupsToDelete.contains(model.getCategory())) {
//								groupsToDelete.remove(model.getCategory());
//								favouritesAdapter.notifyDataSetInvalidated();
//							}
//						}
//					}
//				});
//			} else {
//				row.findViewById(R.id.favourite_icon).setVisibility(View.VISIBLE);
//				ch.setVisibility(View.GONE);
//			}
			return row;
		}

	}


	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		return false;
	}

	
}
