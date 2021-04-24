package csw.training.employee;

import java.util.regex.*;    
import java.util.*;  
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.json.simple.JSONObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RestControllerAdvice
public class RestAPIController {

	@Autowired
	EmployeeResource emp_res;
	@Autowired
	TrainingResource trn_res;
	@Autowired
	ScheduleArray sched_arr;
	@Autowired
	createScheduleArray createschArr;
	@Autowired
	TrainingSchedules trn_schedule;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/createSchedule", 
			  produces = "application/json",
			  consumes = "application/json",
			  method=RequestMethod.POST)
	@ApiResponses(value = {
		    @ApiResponse(responseCode = "200", description = "Object found"),
		    @ApiResponse(responseCode = "400", description = "Object format Error")
		})
	public JSONObject createSchecule(@RequestBody createScheduleArray schedule) {
		
		List<String> scheduleStatus = new ArrayList<String>();
		System.out.println("schedule ID->"+schedule);
		String emp_mail = (String) schedule.getEmployee_id();
		System.out.println("Employee email->"+emp_mail);
		if(emp_mail == null || emp_mail == "" ) {
			JSONObject obj=new JSONObject();    
			  obj.put("Error","employee_id is required, employee_id is email of employee");      
			   System.out.print(obj);  
			   return obj;
		}
        //Regular Expression   
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"; 
        //Compile regular expression to get the pattern  
        Pattern pattern = Pattern.compile(regex);  
        Matcher matcher = pattern.matcher(emp_mail);  
        if(matcher.matches() == false ) {
			JSONObject obj=new JSONObject();    
			  obj.put("Error","invalid email format");      
			   System.out.print(obj);  
			   return obj;
		}
        
        int emp_id = emp_res.getIDofEmail(emp_mail,1);
        List<ScheduleArray> sched_arr_list =  (List<ScheduleArray>) schedule.getSchedule();

		if(sched_arr_list == null ) {
			JSONObject obj=new JSONObject();    
			obj.put("Error","scheduled is required. schedule consists of repeat,startDate,time,duration");       
			return obj;
		}
		for (ScheduleArray sched_arr : sched_arr_list) {
			String err = "";
	        String repeat = sched_arr.getRepeat();
	        if(repeat == null || repeat == "" ) {
	        	err += "repeat,";
	        }
	        String startDate = sched_arr.getStartDate();
	        if(startDate == null || startDate == "" ) {
	        	err += "startDate,";
	        }
	        String time = sched_arr.getTime();
	        if(time == null || time == "" ) {
	        	err += "time,";
	        }
	        int duration = sched_arr.getDuration();
	        if(duration == 0) {
	        	err += "duration,";
	        }
	        if(err.length()>0) {
	        	err += " are required.";
	        }
	        if(err.length()>0) {
        		JSONObject obj=new JSONObject();    
  			  	obj.put("Error",err);      
  			  	System.out.print(obj);  
  			  	return obj;
        	}
	        String status = "";
	        if(!(repeat.equalsIgnoreCase("true") || repeat.equalsIgnoreCase("false"))) {
	        	JSONObject obj=new JSONObject();    
				obj.put("Error","repeat should be any one from true,false.");      
			  	System.out.print(obj);  
			  	return obj;
	        }
	        String[] hourMin = time.split(":");
		    int hour = Integer.parseInt(hourMin[0]);
		    int mins = Integer.parseInt(hourMin[1]);
		    int hoursInMins = hour * 60;
		    int new_scheduled_time = hoursInMins + mins;
		    if(new_scheduled_time > 1439) {
		        JSONObject obj=new JSONObject();    
				obj.put("Error","time should be between 00:00 to 23:59 (24 Hour format) .");      
			  	System.out.print(obj);  
			  	return obj;
		    }
	        if( repeat == "true" ) {
	        	String err1 = "";
	        	String endDate = sched_arr.getEndDate();
	        	if(endDate == "" || endDate == null) {
	            	err1 += "endDate,";
	            }
	        	String frequency = sched_arr.getFrequency();
	        	if(frequency == null || frequency == "") {
	            	err1 += "frequency,";
	            }
	        	if(err1.length()>0) {
	            	err1 += " are required when repeat is set to true.";
	            }
	        	if(err1.length()>0) {
	        		JSONObject obj=new JSONObject();    
	  			  	obj.put("Error",err1);      
	  			  	System.out.print(obj);  
	  			  	return obj;
	        	}
	        	 
	        	if(frequency.equalsIgnoreCase("Daily") || frequency.equalsIgnoreCase("Weekdays") || frequency.equalsIgnoreCase("Weekly") || frequency.equalsIgnoreCase("Monthly")) {
	        		status = trn_schedule.createRepeatingSchedule(emp_id,startDate,time,duration,endDate,frequency);
	        		scheduleStatus.add(status);
	        	}else {
	        		JSONObject obj=new JSONObject();    
	  			  	obj.put("Error","frequency should be any one from Daily, Weekdays, Weekly, Monthly.");      
	  			  	System.out.print(obj);  
	  			  	return obj;
	        	}
	        }
	        else {
	            status = trn_schedule.createNonRepeatingSchedule(emp_id,startDate,time,duration,0);
	            scheduleStatus.add(status);
	        }
		}
		JSONObject obj=new JSONObject();    
    	obj.put("schedule status",scheduleStatus);
	   	return obj;
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/listSchedule", 
			  produces = "application/json",
			  method=RequestMethod.GET)
	public JSONObject  listSchedule(@RequestParam("employee_id") String email) {
				
		List<ScheduleArray> Scheduled_array;
		
		int emp_id = emp_res.getIDofEmail(email,0);
		if(emp_id == 0) {
			JSONObject obj=new JSONObject();    
	    	obj.put("employee_id",email);
	    	obj.put("Error","Employee Not found");
		   	return obj;	
		}
		
		Scheduled_array = trn_res.getSchedules(email,"all");
		JSONObject obj=new JSONObject();    
    	obj.put("employee_id",emp_id);
    	obj.put("employee_email",email);
    	obj.put("schedules",Scheduled_array);
	   	return obj;			
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/modifySchedule", 
			  produces = "application/json",
			  consumes = "application/json",
			  method=RequestMethod.PATCH)
	public JSONObject modifySchedule(@RequestParam("employee_id") String email, @RequestParam("schedule_id") int schedule_id,@RequestBody ScheduleArray schedule) {
				
		int emp_id = emp_res.getIDofEmail(email,0);
		if(emp_id == 0) {
			JSONObject obj=new JSONObject();    
	    	obj.put("employee_id",email);
	    	obj.put("Error","Employee Not found");
		   	return obj;	
		}
		String err = "";
		String error = "";
        String repeat = schedule.getRepeat();
        if(!(repeat == "" || repeat == null)) {
        	error = "repeat cannot be modified, remove repeat.";
        	JSONObject obj=new JSONObject();    
	    	obj.put("employee_id",email);
	    	obj.put("schedule_id",schedule_id);
	    	obj.put("Error",error);
		   	return obj;	
        }
        String frequency = schedule.getFrequency();
        if(!(frequency == "" || frequency == null)) {
        	error = "frequency cannot be modified, remove frequency.";
        	JSONObject obj=new JSONObject();    
	    	obj.put("employee_id",email);
	    	obj.put("schedule_id",schedule_id);
	    	obj.put("Error",error);
		   	return obj;	
        }
        String endDate = schedule.getEndDate();
        if(!(endDate == null || endDate == "")) {
        	error = "endDate not required for modification, remove endDate.";
        	JSONObject obj=new JSONObject();    
	    	obj.put("employee_id",email);
	    	obj.put("schedule_id",schedule_id);
	    	obj.put("Error",error);
		   	return obj;	
        }
        String startDate = schedule.getStartDate();
        if(startDate == null || startDate == "" ) {
        	err += "startDate,";
        }
        String time = schedule.getTime();
        if(time == null || time == "" ) {
        	err += "time,";
        }
        int duration = schedule.getDuration();
        if(duration == 0) {
        	err += "duration,";
        }
        if(err.length()>0) {
        	err += " are required.";
        }
        String[] hourMin = time.split(":");
	    int hour = Integer.parseInt(hourMin[0]);
	    int mins = Integer.parseInt(hourMin[1]);
	    int hoursInMins = hour * 60;
	    int new_scheduled_time = hoursInMins + mins;
	    if(new_scheduled_time > 1439) {
	        JSONObject obj=new JSONObject();    
			obj.put("Error","time should be between 00:00 to 23:59 (24 Hour format) .");      
		  	System.out.print(obj);  
		  	return obj;
	    }
	    
	    String status = trn_res.modifySchedule(emp_id,schedule_id,startDate,time,duration);
	    JSONObject obj=new JSONObject();    
    	obj.put("employee_id",email);
    	obj.put("schedule_id",schedule_id);
    	obj.put("Modify Status",status);
	   	return obj;		
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/cancelSchedule", 
			  produces = "application/json",
			  method=RequestMethod.PATCH)
	public JSONObject cancelSchedule(@RequestParam("employee_id") String email, @RequestParam("schedule_id") int schedule_id) {
				
		int emp_id = emp_res.getIDofEmail(email,0);
		if(emp_id == 0) {
			JSONObject obj=new JSONObject();    
	    	obj.put("employee_id",email);
	    	obj.put("Error","Employee Not found");
		   	return obj;	
		}
      
	    String status = trn_res.cancelSchedule(emp_id,schedule_id);
	    JSONObject obj=new JSONObject();    
	    obj.put("employee_id",email);
	    obj.put("schedule_id",schedule_id);
	    obj.put("Cancel Status",status);
	   	return obj;	
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/listSchedulebyDate", 
			  produces = "application/json",
			  method=RequestMethod.GET)
	public JSONObject  listSchedulebyDate(@RequestParam("employee_id") String email,@RequestParam("date") String date) {
				
		List<ScheduleArray> Scheduled_array;
		
		int emp_id = emp_res.getIDofEmail(email,0);
		if(emp_id == 0) {
			JSONObject obj=new JSONObject();    
	    	obj.put("employee_id",email);
	    	obj.put("Error","Employee Not found");
		   	return obj;	
		}
		
		Scheduled_array = trn_res.getSchedules(email,date);
		JSONObject obj=new JSONObject();    
    	obj.put("employee_id",emp_id);
    	obj.put("employee_email",email);
    	obj.put("schedules",Scheduled_array);
	   	return obj;			
	}
}
