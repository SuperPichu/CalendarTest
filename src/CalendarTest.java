import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.util.ICalDate;
import com.google.ical.compat.javautil.DateIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class CalendarTest {
	public static void main(String[] args) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet("https://www.google.com/calendar/ical/en.usa%23holiday%40group.v.calendar.google.com/public/basic.ics");
		String icalRaw = "";
		try {
			HttpResponse response = client.execute(get);
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line + "\r\n");
			}
			icalRaw = result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ICalendar ical = Biweekly.parse(icalRaw).first();
		List<VEvent> events = new ArrayList<>();
		for (VEvent event : ical.getEvents()) {
			if(event.getRecurrenceRule() != null) {
				RecurrenceRule rule = event.getRecurrenceRule();
				DateIterator it = rule.getDateIterator(event.getDateStart().getValue());
				RecurrenceDates recurrenceDates = new RecurrenceDates();
				while (it.hasNext()) {
					recurrenceDates.addDate(it.next());
				}
				for (ICalDate date : recurrenceDates.getDates()) {
					VEvent event1 = new VEvent();
					event1.setDateStart(date);
					Date end = date;
					end.setHours(event.getDateEnd().getValue().getHours());
					end.setMinutes(event.getDateEnd().getValue().getMinutes());
					event1.setDateEnd(end);
					event1.setSummary(event.getSummary());
					event1.setLocation(event.getLocation());
					if (!event1.getDateEnd().getValue().before(Calendar.getInstance().getTime())) {
						events.add(event1);
					}
				}
			}else{
				if(!event.getDateEnd().getValue().before(Calendar.getInstance().getTime())) {
					events.add(event);
				}
			}
		}
		Collections.sort(events, new Comparator<VEvent>(){
			public int compare(VEvent event1, VEvent event2) {
				return event1.getDateStart().getValue().compareTo(event2.getDateStart().getValue());
			}
		});
		for (VEvent event : events) {
			System.out.println(event.getSummary().getValue());
			//System.out.println(event.getLocation().getValue());
			System.out.println(event.getDateStart().getValue());
			System.out.println(event.getDateEnd().getValue());
			System.out.println();
		}
	}
}