package csw.training.employee;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unused")
public class EmployeeResource {
	
	@Autowired
	DbController dbcon;
	
	private String email;
	private int id;

	public EmployeeResource() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getIDofEmail(String email,int create) {
		int empID = dbcon.getEmployeeID(email);
		if( empID == 0 && create == 1) {
			empID = this.setEmployeeData(email);
		}
		return empID;
	}

	private int setEmployeeData(String email) {
		// TODO Auto-generated method stub
		int empID = dbcon.createEmployee(email);
		return empID;	
	}
}
