package be.emich.labs.uselesswidget.widgetprovider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViews.RemoteView;
import be.emich.labs.uselesswidget.R;
import be.emich.labs.uselesswidget.settings.Preferences;

public class UselessWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet("http://search.twitter.com/search.json?q=%23m%C3%A9t%C3%A9o%20from%3Adreynders");
		BasicResponseHandler handler = new BasicResponseHandler();
		String response = null;
		String text=null;
		Date d=null;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		try{
			
			response = client.execute(get,handler);
			if(response!=null){
				JSONObject object = new JSONObject(response);
				JSONArray array = object.getJSONArray("results");
				if(array.length()>0){
					JSONObject tweet = array.getJSONObject(0);
					text = tweet.getString("text");
					String dateStr = tweet.getString("created_at");
					SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",Locale.US);
					d = sdf.parse(dateStr);
					Log.v(getClass().getName(),"Date: "+d+" Text: "+text);
					
					Editor e = preferences.edit();
					e.putString(Preferences.PREFERENCE_TWEET, text);
					e.putLong(Preferences.PREFERENCE_TIME, d.getTime());
					e.commit();
				}
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		SimpleDateFormat sdf2 = new SimpleDateFormat("d/MM/yy HH:mm");
		
		if(d==null && text==null){
			
			text = preferences.getString(Preferences.PREFERENCE_TWEET, "Indisponible en ce moment!");
			Long dateLong = preferences.getLong(Preferences.PREFERENCE_TIME, -1);
			if(dateLong!=-1)d=new Date(dateLong);
		}
		
		if(d!=null && text!=null){
			for(int i = 0; i<appWidgetIds.length;i++){
				int appWidgetId = appWidgetIds[i];
				
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
				remoteViews.setTextViewText(R.id.textViewText, text);
				remoteViews.setTextViewText(R.id.textViewTime, sdf2.format(d));
				
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			}
		}
	}
}
