import algorithm.*;
import format.Pub;

public class demo {
	/**
	 * @author	Chih-Hung Wang, Ying-Jung Hsu
	 * @mail	s1050465@mail.ncyu.edu.tw
	 * @version 1.0
	 * @since	1.8.0_6
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("------------------The DEMO of simple Simulating the Outsourcing CP-ABE in IoT Environment---------------------");
		String SP = "Test";
		String U  = "test";
		double SecurityDegree=1.5;
		
		setup server= new setup(SP,U,SecurityDegree);

		keygen Alice 	= new keygen(server.msk,"Alice:Rehabilitation_Dept:Chiayi:password_Alice");
		keygen Bob 		= new keygen(server.msk,"Bob:Rehabilitation_Dept:Taipei:password_Bob");
		keygen Andy 	= new keygen(server.msk,"Doctor:Rehabilitation_Dept:in_Chiayi:password_Alice");
		keygen May 		= new keygen(server.msk,"Doctor:Rehabilitation_Dept:in_Taipei");
		
		encrypt Smart_Insole= new encrypt();
		//Smart_Insole.enc_device(server.msk.PK,"the data of Alice's running habits.","((A,B,C,2),(D,(E,F,G,2),(H,I,J,2),K,L,3),M,4)");
		Smart_Insole.enc_device(server.msk.PK,"the data of Alice's running habits.","((Alice,Rehabilitation_Dept,Chiayi,3),(Doctor,in_Chiayi,in_Taipai,2),password_Alice,2)");
		//Smart_Insole.enc_device(server.msk.PK,"the data of Alice's running habits","(Doctor,Rehabilitation,2)");
		
		//System.out.println("-------------------Alice_in_Chiayi----------------------");
		//dec Alice_in_Chiayi= new dec(Alice.prv,Smart_Insole.cph);
		/*
		System.out.println("-------------------Bob_in_Taipei----------------------");
		dec Bob_in_Taipei= new dec(Bob.prv,Smart_Insole.cph);
		System.out.println("-------------------Doctor Andy_in_Chiayi----------------------");
		dec Andy_in_Chiayi= new dec(Andy.prv,Smart_Insole.cph);
		System.out.println("-------------------Doctor May_in_Taipei----------------------");
		dec May_in_Taipei= new dec(May.prv,Smart_Insole.cph);
		*/
		//test.keygen(test.msk,"test");
		
	}
}
