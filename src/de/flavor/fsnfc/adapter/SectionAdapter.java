package de.flavor.fsnfc.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.flavor.fsnfc.R;
import de.flavor.fsnfc.dto.Venue;

public class SectionAdapter extends BaseAdapter {

	private List<ElementWrapper> items;
	
	private static int TYPE_SECTION = 0;
	private static int TYPE_CONTENT = 1;
	
	private LayoutInflater inflater = null;
	
	public SectionAdapter(Map<String, List<Venue>> headersAndVenues, LayoutInflater inflater) {
		super();
		
		items = new ArrayList<ElementWrapper>();
		this.inflater = inflater; 
		
		for (Entry<String, List<Venue>> entry :  headersAndVenues.entrySet())
		{
			items.add(new ElementWrapper(entry.getKey()));
			
			for (Venue venue : entry.getValue())
			{
				items.add(new ElementWrapper(venue));
			}
		}
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int pos) {
		return items.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		
		if (getItemViewType(position) == TYPE_SECTION)
		{
			view = (convertView == null) ? inflater.inflate(R.layout.header, null) : convertView;
			SimpleHolder holder = null;
			if (view.getTag() == null)
			{
				holder = new SimpleHolder();
				holder.text = (TextView)view.findViewById(R.id.text);
				view.setTag(holder);
			}
			else
			{
				holder = (SimpleHolder)view.getTag();
			}
			
			ElementWrapper wrapper = items.get(position);
			holder.text.setText(wrapper.getHeader());
			
		}
		else
		{
			view = (convertView == null) ? inflater.inflate(android.R.layout.simple_list_item_1, null) : convertView;
			SimpleHolder holder = null;
			if (view.getTag() == null)
			{
				holder = new SimpleHolder();
				holder.text = (TextView)view.findViewById(android.R.id.text1);
				view.setTag(holder);
			}
			else
			{
				holder = (SimpleHolder)view.getTag();
			}
			
			ElementWrapper wrapper = items.get(position);
			holder.text.setText(wrapper.getVenue().getTitle());			
		}
		
		
		return view;
		
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getItemViewType(int position) {
		ElementWrapper wrapper = items.get(position);
		
		if (wrapper.isHeader())
			return TYPE_SECTION;
		else 
			return TYPE_CONTENT;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean isEnabled(int position) {
		ElementWrapper wrapper = items.get(position);
		
		if (wrapper.isHeader())
			return false;
		else
			return true;
	}
	
	private static class SimpleHolder {
		public TextView text;
	}
	
	

}
