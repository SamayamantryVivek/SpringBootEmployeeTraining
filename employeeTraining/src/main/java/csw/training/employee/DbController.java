package csw.training.employee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class DbController {
	
	Connection con;


	public DbController() {
		super();
		// TODO Auto-generated constructor stub
		String url = "jdbc:mysql://localhost:3306/emp_training_data";
		String username = "emp_admin";
		String password = "TraEmp@Data21";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(url,username,password);
		} catch (Exception exp) {
			// TODO Auto-generated catch block
			exp.printStackTrace();
		}
	}

	public int getEmployeeID(String email) {
		// TODO Auto-generated method stub
		int id = 0;
		String sql = "select id from employee where employee_status='active' and email = '"+(email)+"'";
		try {
			Statement st =con.createStatement();
			ResultSet res = st.executeQuery(sql);
			while(res.next()) {
				id = res.getInt(1);	
			}
		}catch (Exception st) {
			// TODO Auto-generated catch block
			st.printStackTrace();
		}
		return id;	
	}

	public int createEmployee(String email) {
		// TODO Auto-generated method stub
		int empID = 0;
		String sql = "insert into employee(email,employee_status) values(?,?)";
		try {
			PreparedStatement st =con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			st.setString(1,email);
			st.setString(2,"active");
			int res = st.executeUpdate();
			if(res == 1) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next())
				{
					empID = rs.getInt(1);
				}
			}		
		}catch (Exception st) {
			// TODO Auto-generated catch block
			st.printStackTrace();
		}
		return empID;
	}

	@SuppressWarnings("unused")
	public int checkIfScheduleExists(int emp_id, String startDate, String time,int schedule_id) {
		// TODO Auto-generated method stub
		int id = 0;
		String start_time = "";
		int scheduled_duration = 0;
		int time_scheduled = 0;
		try {
		    String[] hourMin = time.split(":");
		    int hour = Integer.parseInt(hourMin[0]);
		    int mins = Integer.parseInt(hourMin[1]);
		    int hoursInMins = hour * 60;
		    int new_scheduled_time = hoursInMins + mins;
		    
		    String sql = "";
		    if( schedule_id == 0 ) {
		    	sql = "select id,start_time,duration from training_schedules where training_status='active' and employee_id = "+emp_id+" and date='"+(startDate)+"'";
		    }else {
		    	sql = "select id,start_time,duration from training_schedules where training_status='active' and employee_id = "+emp_id+" and date='"+(startDate)+"' and id != "+schedule_id;
		    }
		      
		    Statement st =con.createStatement();
			ResultSet res = st.executeQuery(sql);

		    while(res.next()) {
				id = res.getInt(1);	
				start_time = res.getString(2);
				hourMin = start_time.split(":");
			    hour = Integer.parseInt(hourMin[0]);
			    mins = Integer.parseInt(hourMin[1]);
			    hoursInMins = hour * 60;
			    time_scheduled = hoursInMins + mins;
			    scheduled_duration = res.getInt(3);
				if(time_scheduled <= new_scheduled_time && new_scheduled_time < (scheduled_duration+time_scheduled)) {
					return 1;
				}
			}
		}catch (Exception e) {
		    // TODO: handle exception
		    e.printStackTrace();
		}
		return 0;
		
	}

	public int scheduleTraining(int emp_id, String startDate, String time, int duration) {
		// TODO Auto-generated method stub
		int scheduleId = 0;
		String sql = "insert into training_schedules(employee_id,date,start_time,duration,training_status) values(?,?,?,?,?)";
		try {
			PreparedStatement st =con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			st.setInt(1, emp_id);
			st.setString(2,startDate);
			st.setString(3,time);
			st.setInt(4, duration);
			st.setString(5,"active");
			int res = st.executeUpdate();
			if(res == 1) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next())
				{
					scheduleId = rs.getInt(1);
				}
			}		
		}catch (Exception st) {
			// TODO Auto-generated catch block
			st.printStackTrace();
		}
		return scheduleId;
	}

	public List<ScheduleArray> getSchedules(String email,String edate) {
		// TODO Auto-generated method stub
		List<ScheduleArray> schedules = new ArrayList<>();
		String sql = "";
		try {
			if(edate.equals("all")) {
				sql = "select trn.id,trn.date,trn.start_time,trn.duration from training_schedules trn inner join employee emp on emp.id = trn.employee_id where trn.training_status='active' and emp.email = '"+(email)+"'";
			}else {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
				Date varDate=dateFormat.parse(edate);
				dateFormat=new SimpleDateFormat("yyyy-MM-dd");
			    edate= dateFormat.format(varDate);
			    sql = "select trn.id,trn.date,trn.start_time,trn.duration from training_schedules trn inner join employee emp on emp.id = trn.employee_id where trn.training_status='active' and emp.email = '"+(email)+"' and trn.date = '"+(edate)+"'";
			}
			Statement st =con.createStatement();
			ResultSet res = st.executeQuery(sql);
			SimpleDateFormat fromdateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat todateFormat=new SimpleDateFormat("dd MMM yyyy");
			String date = "";
			while(res.next()) {
				Date varDate=fromdateFormat.parse(res.getString(2));
			    date= todateFormat.format(varDate);
				ScheduleArray sch = new ScheduleArray();
				sch.setScheduleId(res.getInt(1));
				sch.setStartDate(date);
				sch.setEndDate(date);
				sch.setTime(res.getString(3));
				sch.setDuration(res.getInt(4));	
				sch.setRepeat("false");
				
				schedules.add(sch);
			}
		}catch (Exception st) {
			// TODO Auto-generated catch block
			st.printStackTrace();
		}
		return schedules;	
		
	}

	public String checkEmployeeScheduleExists(int emp_id, int schedule_id) {
		// TODO Auto-generated method stub
		String sql = "select training_status from training_schedules where employee_id = "+emp_id+" and id="+schedule_id;

	    Statement st;
		try {
			st = con.createStatement();
			ResultSet res = st.executeQuery(sql);
			while(res.next()) {
				return res.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "not found";
		
	}

	public int modifySchedule(int schedule_id, String startDate, String time, int duration) {
		// TODO Auto-generated method stub
		String sql = "update training_schedules set date = ? ,start_time = ?, duration =? where id = ?";
		
		try {		
			PreparedStatement st =con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			st.setString(1,startDate);
			st.setString(2,time);
			st.setInt(3, duration);
			st.setInt(4, schedule_id);			
			int res = st.executeUpdate();
			return res;
		}catch (Exception st) {
			// TODO Auto-generated catch block
			st.printStackTrace();
		}
		return 0;
	}

	public int cancelSchedule(int schedule_id) {
		// TODO Auto-generated method stub
		String sql = "update training_schedules set training_status = ?  where id = ?";
		try {
			PreparedStatement st =con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			st.setString(1,"cancelled");
			st.setInt(2, schedule_id);
			int res = st.executeUpdate();
			return res;
		}catch (Exception st) {
			// TODO Auto-generated catch block
			st.printStackTrace();
		}
		return 0;
	}
}
