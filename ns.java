package com.staples.etps;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.staples.apns.SQL.DatabaseConnection;
import com.staples.apns.SQL.Parameters;

public class NotificationScheduler{
	
	private final String STORE_REFRESH_DATE_QUERY = "select distinct(StoreNumber), RefreshDT from dbo.MobileDeviceAlertRefresh where RefreshDT is not null order by RefreshDT asc";
	private final String UPDATE_REFRESH_DATE_QUERY = "update dbo.MobileDeviceAlertRefresh set RefreshDT = :@RefreshDT  where StoreNumber = :@StoreNumber";
	private final String DEVICE_TOKEN_QUERY = "select DeviceToken from dbo.MobileDeviceAlertRefresh where StoreNumber = :@StoreNumber";
	
	protected Properties properties;
	private DateFormat refreshDTFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	public List<String> listOfStores;
	public String nextNotificationDT = null;
	private DatabaseConnection connection;
	
	public NotificationScheduler(String configPath) {
		refreshDTFormat.setTimeZone(TimeZone.getTimeZone("EST"));
		properties = new Properties();
		try {
			properties.load(new FileInputStream(configPath));
			connection = new DatabaseConnection(configPath);
		} catch (FileNotFoundException e) {
			// TODO LOGGING
			System.out.println("Config file not found: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void scheduleNotification() {
		if(listOfStores != null && listOfStores.size() > 0) {
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(listOfStores.size());
			Date refresh = null, now = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
			long delay;
			try {
				refresh = refreshDTFormat.parse(nextNotificationDT);
			} catch (ParseException e) {
				// TODO LOGGING 
				System.out.println("Failed to parse refreshDT: " + e.getMessage());
				System.exit(1);
			}
			delay = refresh.getTime() - now.getTime();
			delay = delay < 0 ? 0 : delay;
			for(String store : listOfStores) {
				System.out.println(listOfStores.get(0) + "    " + nextNotificationDT);
				System.out.println("waiting for: " + (double)delay / 60000 + " minutes");
				scheduler.schedule(new NotificationSender(this, listOfStores.get(0)), delay, TimeUnit.MILLISECONDS);
			}
			
			//This tells all threads to shutdown and gives them one minutes to finish up.
			try {
				scheduler.shutdown();
				scheduler.awaitTermination(delay + 60000, TimeUnit.MILLISECONDS);
				if(!scheduler.isTerminated()) {
					//LOGGING
					System.out.println("Failed to send notification to stores");
					this.closeConnection();
					System.exit(2);
				}
			} catch (InterruptedException e) {
				// TODO LOGGING 
				System.out.println("Threads interrupted unsuccessfully: " + e.getMessage());
			}
			
		}
	}
	/**
	 * getRefreshTable is a method that query the database through the class's connection
	 * and updates the class's listOfStores and nextNotificationDT to the stores that have 
	 * the most resent refreshDT and the most resent refreshDT respectively.
	 */
	public void updateRefreshTable() {
		List<Object[]> dbResults = connection.listQuery(STORE_REFRESH_DATE_QUERY);
		listOfStores = new ArrayList<String>();
		nextNotificationDT = null;
		for(Object[] store : dbResults) {
			if(store[1] != null) {
				//System.out.println(store[0].toString() + "  " + store[1].toString());
				if(nextNotificationDT == null) {
					nextNotificationDT = store[1].toString();
				}else if(!nextNotificationDT.equals(store[1].toString())) {
					break;
				}
				if(store[0] != null) {
					listOfStores.add(store[0].toString());
				}
			}
		}
		
		
	}
	
	/**
	 * getDeviceTokens returns a list of device tokens for the given store.  It makes a 
	 * com.staples.apns.SQL.Parameters object to replace '@StoreNumber' with the given 
	 * store number in the query.
	 * @param storeNum
	 * @return List<String> of deviceTokens
	 */
	public List<String> getDeviceTokens(String storeNum){
		Parameters param = new Parameters();
		param.addParam("@StoreNumber", storeNum);
		List<Object[]> dbResults = connection.listQuery(DEVICE_TOKEN_QUERY, param);
		List<String> tokens = new ArrayList<String>(dbResults.size());
		for(Object token : dbResults) {
			if(token != null)
				tokens.add(token.toString());
		}
		return tokens;
	}

	/**
	 * updateSToreRefreshDate updates the refreshDT collumn for all devices connected
	 * to the given store.  This updates the database without the IOS device opening the app.
	 * @param storeNumber the storeNumber to update
	 * @param newRefreshDT the next time a notification should be sent to the store devices
	 */
	public void updateStoreRefreshDate(String storeNumber, String newRefreshDT) {
		DateFormat df = new SimpleDateFormat("h:mm:ss.S");
		Date now = Calendar.getInstance().getTime();
		Calendar cal = Calendar.getInstance();
		try {
			Calendar nextTime = Calendar.getInstance();
			nextTime.setTime(df.parse(newRefreshDT));
			cal.set(cal.HOUR,nextTime.get(cal.HOUR));//(df.parse(newRefreshDT));
			cal.set(cal.MINUTE, nextTime.get(cal.MINUTE));
			cal.set(cal.SECOND, nextTime.get(cal.SECOND));
			cal.set(cal.MILLISECOND, nextTime.get(cal.MILLISECOND));
		} catch (ParseException e) {
			// TODO LOGGING 
			System.out.println("Failed to parse refreshDT: " + e.getMessage());
		}
		if (now.after(cal.getTime())) {
			System.out.println(refreshDTFormat.format(cal.getTime()));
			cal.add(Calendar.DATE, 1);
		}
		System.out.println(newRefreshDT);
		System.out.println(refreshDTFormat.format(cal.getTime()));
		System.out.println(refreshDTFormat.format(now));
		Parameters params = new Parameters();
		params.addParam("@StoreNumber", storeNumber);
		params.addParam("@RefreshDT", refreshDTFormat.format(cal.getTime()));
		connection.executeUpdate(UPDATE_REFRESH_DATE_QUERY, params);
	}

	public void closeConnection() {
		connection.close();
	}
	public static void main(String[] args) {
		boolean run = true;
		String config = "/Users/dunmi001/Documents/hibernate.properties";
		NotificationScheduler ns = new NotificationScheduler(config);
		//ns.updateStoreRefreshDate("0045", "2:45:00.0");
		//run = !run
		while(run) {
			ns.updateRefreshTable();
			ns.scheduleNotification();
		}
		//ns.updateStoreRefreshDate("9999", "2019-03-01 16:01:00.0");
		//ns.updateStoreRefreshDate("0999", "2019-03-01 16:01:00.0");
		//ns.updateStoreRefreshDate("0099", "2019-03-01 16:01:00.0");

		//ns.updateStoreRefreshDate("0083", "2019-03-01 16:01:00.0");
		//ns.getRefreshTable();
		
		//System.out.println(ns.nextNotificationDT);
		//for(String store : ns.listOfStores) {
		//	System.out.println(store);
		//}
		//ns.closeConnection();
	}
	
}

class NotificationSender implements Runnable{

	private NotificationScheduler ns;
	private String storeNumber;
	
	public NotificationSender(NotificationScheduler scheduler, String storeNumber) {
		ns = scheduler;
		this.storeNumber = storeNumber;
	}
	public void run() {
	
		EZNotificationBuilder notification = new EZNotificationBuilder(storeNumber, ns.getDeviceTokens(storeNumber), ns.properties.getProperty("pathToCert"), ns.properties.getProperty("certPassword"), ns.properties.getProperty("egURL"), ns.properties.getProperty("egUsername"), ns.properties.getProperty("egPassword"), ns.properties.getProperty("soundPath") == null ? "Default" : ns.properties.getProperty("soundPath") );
		ns.updateStoreRefreshDate(storeNumber, notification.getStoreNextRefreshDT());
		
	}
	
}

