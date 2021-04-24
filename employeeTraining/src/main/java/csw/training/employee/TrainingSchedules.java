package csw.training.employee;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("unused")
@Service
public class TrainingSchedules {
	
	@Autowired
	DbController dbcon;

	public TrainingSchedules() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	public String createNonRepeatingSchedule(int emp_id, String startDate, String time, int duration, int repeat) {
		// TODO Auto-generated method stub
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");		
		if(repeat == 1) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		}
		try {
			Date varDate=dateFormat.parse(startDate);
			dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		    startDate= dateFormat.format(varDate);
		    int conflict = dbcon.checkIfScheduleExists(emp_id,startDate,time,0);
			int id = 0;
			if(conflict == 1) {
				return "Conflict -  Cannot Schedule Training for Employee";
			}else {
				id = dbcon.scheduleTraining(emp_id,startDate,time,duration);
			}
			return "Training Scheduled Successfully";
		}catch (Exception e) {
		    // TODO: handle exception
		    e.printStackTrace();
		}
		return "Training Scheduled Successfully";
		
	}

	public String createRepeatingSchedule(int emp_id, String startDate, String time, int duration, String endDate,
			String frequency) {
		// TODO Auto-generated method stub
		List<String> dates = new ArrayList<String>();  
		List<String> scheduleStatus = new ArrayList<String>();
		String status = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
		try{
			Date startD=dateFormat.parse(startDate);
			Date endD=dateFormat.parse(endDate);
			dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		    startDate= dateFormat.format(startD);
		    endDate= dateFormat.format(endD);
		    String nextDate = startDate;
		    Date nextD= startD;
		    int day = 0;
		    while(endD.compareTo(nextD) >= 0) {
		    	if(frequency.equalsIgnoreCase("Daily")) {
		    		dates.add(nextDate);
        		    final Calendar calendar = Calendar.getInstance();
        		    calendar.setTime(nextD);
        		    calendar.add(Calendar.DAY_OF_YEAR, 1);
        		    nextDate = dateFormat.format(calendar.getTime());
        		    nextD = dateFormat.parse(nextDate);
        		}else if(frequency.equalsIgnoreCase("WeekDays")) {
    		    	final Calendar calendar = Calendar.getInstance();
        		    calendar.setTime(nextD);
        		    day = (int) calendar.get(Calendar.DAY_OF_WEEK);
        		    if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        		    	dates.add(nextDate);
        	        }
        		    calendar.add(Calendar.DAY_OF_YEAR, 1);
        		    nextDate = dateFormat.format(calendar.getTime());
        		    nextD = dateFormat.parse(nextDate);
    		    }else if(frequency.equalsIgnoreCase("Weekly")) {
		    		dates.add(nextDate);
    		    	final Calendar calendar = Calendar.getInstance();
        		    calendar.setTime(nextD);
        		    calendar.add(Calendar.DAY_OF_YEAR, 7);
        		    nextDate = dateFormat.format(calendar.getTime());
        		    nextD = dateFormat.parse(nextDate);
    		    }else if(frequency.equalsIgnoreCase("Monthly")) {
		    		dates.add(nextDate);
    		    	final Calendar calendar = Calendar.getInstance();
        		    calendar.setTime(nextD);
        		    calendar.add(Calendar.MONTH, 1);
        		    nextDate = dateFormat.format(calendar.getTime());
        		    nextD = dateFormat.parse(nextDate);
    		    }
		    }
		    int total = 0;
		    int success = 0;
		    for (String start_date: dates) {
		    	status = createNonRepeatingSchedule(emp_id,start_date,time,duration,1);	
		    	scheduleStatus.add(status);
		    	total++;
		    	if(status == "Training Scheduled Successfully") {
		    		success++;
		    	}
	        }
		    if(success == total) {
		    	status = "Training Scheduled Successfully";
		    }else if(success < total && success != 0) {
		    	status = "Training Scheduled Partially";
		    }else {
		    	status = "Conflict -  Cannot Schedule Training for Employee";
		    }

		    return status;
		    
		    
		}catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
}
