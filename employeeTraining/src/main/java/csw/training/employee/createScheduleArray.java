package csw.training.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;  

@Service
public class createScheduleArray {

	private String employee_id;
	@Autowired
	private List<ScheduleArray> schedule;
	
	public String getEmployee_id() {
		return employee_id;
	}
	public void setEmployee_id(String employee_id) {
		this.employee_id = employee_id;
	}
	public List<ScheduleArray> getSchedule() {
		return schedule;
	}
	public void setSchedule(List<ScheduleArray> schedule) {
		this.schedule = schedule;
	}
	
	@Override
	public String toString() {
		return "createScheduleArray [employee_id=" + employee_id + ", schedule=" + schedule + "]";
	}	
}
