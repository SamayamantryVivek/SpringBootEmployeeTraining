package csw.training.employee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrainingResource {

	@Autowired
	ScheduleArray sch;
	@Autowired
	DbController dbcon;
	
	public List<ScheduleArray> getSchedules(String email,String date) {
		// TODO Auto-generated method stub
		List<ScheduleArray> schedules = dbcon.getSchedules(email,date);
		return schedules;
	}

	public String modifySchedule(int emp_id, int schedule_id, String startDate, String time, int duration) {
		// TODO Auto-generated method stub
		String schedule_status = dbcon.checkEmployeeScheduleExists(emp_id, schedule_id);
		String status = "";
		int conflict = 0;
		int modify_status = 0;
		if(!schedule_status.equals("not found")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");	
			Date varDate;
			try {
				varDate = dateFormat.parse(startDate);
				dateFormat=new SimpleDateFormat("yyyy-MM-dd");
			    startDate= dateFormat.format(varDate);
				conflict = dbcon.checkIfScheduleExists(emp_id,startDate,time,schedule_id);
				if(conflict == 1) {
					status = "Cannot be modified due to conflict.";
				}else {
					modify_status = dbcon.modifySchedule(schedule_id,startDate,time,duration);
					if(modify_status == 1) {
						status = "Modified Training Schedule Successful";
					}else {
						status = "Cannot Modify Training Schedule";
					}
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}else {
			status = "Training Schedule not found for Employee";
		}
		return status;
		
	}

	public String cancelSchedule(int emp_id, int schedule_id) {
		// TODO Auto-generated method stub
		String schedule_status = dbcon.checkEmployeeScheduleExists(emp_id, schedule_id);
		String status = "";
		int cancel_status = 0;
		if(!schedule_status.equals("not found")) {
			if(schedule_status.equals("active")) {
				status ="Need to Cancel";
				cancel_status = dbcon.cancelSchedule(schedule_id);
				if(cancel_status == 1) {
					status = "Cancelled Training Schedule Successful";
				}else {
					status = "Cannot Cancel Training  Schedule";
				}
			}else {
				status = "Training Schedule already cancelled";
			}		
		}else {
			status = "Training Schedule not found for Employee";
		}
		return status;
	}
}
