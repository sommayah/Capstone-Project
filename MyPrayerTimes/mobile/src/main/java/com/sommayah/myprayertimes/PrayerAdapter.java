package com.sommayah.myprayertimes;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sommayah.myprayertimes.dataModels.PrayTime;

import org.joda.time.LocalTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by sommayahsoliman on 2/25/16.
 */
public class PrayerAdapter extends RecyclerView.Adapter<PrayerAdapter.PrayerAdapterViewHolder> {
    private static final int VIEW_TYPE_NEXT = 0; //representing the next prayer
    private static final int VIEW_TYPE_ALL = 1;  //representing all other prayers


    private boolean mUseNextPrayerLayout = true; //for the coming prayer use a bigger list item with time remaining

    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;
    ArrayList<String> prayerTimes;
    ArrayList<String> friendlyPrayerTimes;


    public PrayerAdapter(Context context, View emptyView){
        mContext = context;
        mEmptyView = emptyView;

    }

    public PrayerAdapter(ArrayList<String> array,Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
        prayerTimes = new ArrayList<>();
        friendlyPrayerTimes = new ArrayList<>();
        //times = new double[array.size()];
        for(int i=0 ; i<array.size();i++){
            prayerTimes.add(array.get(i));
        }
    }


    @Override
    public PrayerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_NEXT: {
                    layoutId = R.layout.prayer_list_item_big;
                    break;
                }
                case VIEW_TYPE_ALL: {
                    layoutId = R.layout.prayer_list_item;
                    break;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new PrayerAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(PrayerAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(MainActivityFragment.COL_PRAYER_NAME);
        holder.mPrayerName.setText(name);
        String timeString = mCursor.getString(MainActivityFragment.COL_PRAYER_TIME);
        LocalTime time = new LocalTime(timeString);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("hh:mm aa");
        if(Utility.getPreferredTimeFormat(mContext) == PrayTime.TIME12) { //12 hr or 24 formate
            String str = fmt.print(time);
            holder.mPrayerTime.setText(str);
        }else{
            holder.mPrayerTime.setText(timeString);
        }
        if(getItemViewType(position) == VIEW_TYPE_NEXT){
            LocalTime now = LocalTime.now();
            int minutes = Minutes.minutesBetween(now, time).getMinutes();
            holder.mTimeRemaining.setText(getFriendlyTimeString(minutes));
        }

    }

    private String getFriendlyTimeString(int minutes) {
        String remainingTime = "";
        long hours = 0;
        if(minutes<0)
            minutes = 24*60 + minutes;
        if(minutes >59){
            hours = minutes/60;
            minutes = minutes%60;
            remainingTime = String.valueOf(hours)+ " hours, and " + String.valueOf(minutes)
                    + " minutes remaining.";
            return remainingTime;
        }else if(minutes == 0){
            return "Time for Prayer.";
        }
        return String.valueOf(minutes) + " minutes remaining";

    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    void clear(){
        prayerTimes.clear();
    }

    void add(ArrayList<String> times){

        for(String time: times){
            prayerTimes.add(time);
        }

    }


    @Override
    public int getItemViewType(int position) {
        return (position == getNextPos() && mUseNextPrayerLayout) ? VIEW_TYPE_NEXT : VIEW_TYPE_ALL;
    }

    public int getNextPos(){
        int pos = 0;
        LocalTime now = LocalTime.now();
        Log.d("get current time",now.toString());
        LocalTime limit;
        mUseNextPrayerLayout = true;
        mCursor.moveToFirst();
        do{
            limit = new LocalTime(mCursor.getString(MainActivityFragment.COL_PRAYER_TIME));
            Boolean isLate = now.isAfter(limit);
            if(isLate) {
                pos++;
            }

        } while (mCursor.moveToNext());
        //case pos is out of bound
        if(pos == getItemCount()){
           // mUseNextPrayerLayout = false;
            pos = 0; //just for now
        }
        return pos;
    }

    public class PrayerAdapterViewHolder extends RecyclerView.ViewHolder  {
        @Bind(R.id.prayer_name) TextView mPrayerName;
        @Bind(R.id.prayer_time) TextView mPrayerTime;
     //   @Bind(R.id.prayer_time_remaining) TextView mTimeRemaining;
        public final TextView mTimeRemaining;

        public PrayerAdapterViewHolder(View view) {
            super(view);
//            mPrayerName = (TextView) view.findViewById(R.id.prayer_name);
//            mPrayerTime = (TextView) view.findViewById(R.id.prayer_time);
            ButterKnife.bind(this,view);
            mTimeRemaining = (TextView)view.findViewById(R.id.prayer_time_remaining);

            mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);


        }

    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

}
