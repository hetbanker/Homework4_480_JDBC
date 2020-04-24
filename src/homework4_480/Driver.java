/* Het Banker  
 * CS 480 
 */

package homework4_480; 
import java.io.File;   
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/*************************************************************************************************************/
public class Driver 
{
	//five global variables that we need for two tables
	static int pid ;
	static int age;
	static String name ;
	static int mid;
	
	
	//main method
	public static void main(String[ ] args) throws Exception
	{
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			//gets the connection to the database
			Connection connection = getConnection( );		
			
			//path to where the transfile is located
			String pathFile = "/Users/hetbanker/Desktop/CS 480/homework4_480/transfile.txt";
			
			Statement stat  = connection.createStatement( );
 
			System.out.println("Database connected");
			
			//creates the person table
			stat.execute("CREATE TABLE IF NOT EXISTS person (pid int NOT NULL auto_increment, "
					+ "name varchar(255), "
					+ "age int, PRIMARY KEY(pid))");
			
			//creates the likes table
			stat.execute("CREATE TABLE IF NOT EXISTS likes (pid int NOT NULL, "
						+ "mid int NOT NULL, "
						+ "PRIMARY KEY(pid,mid))");

			//scans the files
			@SuppressWarnings("resource")
			Scanner scan = new Scanner(new File(pathFile));
			System.out.println("Found the transfile.txt\n");
			System.out.println("Reading the file...");
			
		    String line;
		    while(scan.hasNextLine()) 
		    {
		    	line = scan.nextLine();
		        System.out.println(line);
		    } 
			
		    System.out.println();
		    Scanner scan1 = new Scanner(new File(pathFile));
		    String lineString = readTheLine(scan1);	//reads the file 
			
		    //read until the end of the file
			for(;lineString != null;)
			{
				Scanner lineScan = new Scanner(lineString);
				int transaction = lineScan.nextInt();	//first int is the transaction number
				
				if(lineScan.hasNextInt()) { pid = lineScan.nextInt(); }	
				if(lineScan.hasNext())    { name = lineScan.next();   }
				if(lineScan.hasNextInt()) { age = lineScan.nextInt(); }

				if(transaction == 6) { transaction6(stat, lineScan); }
				if(transaction == 5) { transaction5(stat, lineScan); }
				if(transaction == 4) { transaction4(stat, lineScan); } 
				if(transaction == 3) { transaction3(stat, lineScan); }
				if(transaction == 2) { transaction2(stat, lineScan); } 
				if(transaction == 1) { transaction1(stat, lineScan); }
				
				//lineScanner.close();,
				lineString= readTheLine(scan1);
			}
			stat.execute("DROP TABLES person, likes");
			System.out.println("\nDropped all the tables \nFinished reading the file \nGood bye!");
			connection.close();
		} 
		
		catch (Exception e) 
		{
		    
			System.out.println(e);
		}
	}

	/****************************************************************************************************************/
	
	//connect to the database
	private static Connection getConnection() throws Exception
	{
		//connection to JDBC
		try
		{
			Connection myConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/homework4db"
					+ "?allowMultiQueries=true&useUnicode=true","root","");
			return myConnection;
		}
		
		catch (Exception e) 
		{
			System.out.println(e);
			System.out.println("Problem with connecting to Database"); 
		}
		return null;
	}
	
	/****************************************************************************************************************/
	
	//reads the transfile
	private static String readTheLine(Scanner scan)
	{
		String lineString = " "; 
		for(;;)
		{
			if(scan.hasNextLine()) 
			{ 
				lineString = scan.nextLine( );
				lineString = lineString.trim( ); 	//trim the spaces before the transaction number
				if(lineString.length() > 0) { return lineString; }  
				else return null;
			}
			return null; 
		}
	}

	/****************************************************************************************************************/
	
	//transaction 6 - check if there is any person who directly likes two or more persons
	private static void transaction6(Statement stat, Scanner scan) throws SQLException
	{
		//storing the result of the query in the result set
		ResultSet resultSet= stat.executeQuery("SELECT DISTINCT(p.name) "
				                             + "FROM person AS p, likes AS l  "
				                             + "WHERE p.pid = l.pid "
				                             + "GROUP BY p.name "
				                             + "HAVING COUNT(p.name) >= 2");
		
		String nameString = null;
		
		System.out.print("\nTransaction 6: \n");
		
		int counter = 1;
		while(resultSet.next())		//loop through all the names
		{
			nameString = resultSet.getString("name");
			System.out.println(counter++ + ". " + nameString + " ");	
		}
		
		//System.out.println();
		counter--;
		if(counter == 0) 
		{
			System.out.println("There is no person that likes two or more persons");
		}
		System.out.println(); 
	}
	 
	/**
	 * @throws Exception **************************************************************************************************************/
	
	//transaction 5 - Output the average age of persons liked, directly or indirectly, by the person with given pid
	private static void transaction5(Statement stat,Scanner scan) throws Exception
	{
		//5 103
		System.out.print("\nTransaction 5: Average age = ");
		
		ArrayList<Integer> IDHolder  = new ArrayList<Integer>();
		ArrayList<Integer> ageHolder = new ArrayList<Integer>();
		
		getPID(IDHolder, pid);  
		
	
		int holdname = 0;
		for(int pidPerson : IDHolder)
		{
			ResultSet resultSet = stat.executeQuery("SELECT age "
												  + "FROM person " 
												  + "WHERE pid = " + pidPerson); 

			while(resultSet.next( ))
			{
				holdname = resultSet.getInt("age");
				ageHolder.add(holdname);
			}
		}
		
		Double add = (double) 0.0;
		for(int index: ageHolder)
		{
			add = index + add;
		}
	
		System.out.println(Math.round(add/ageHolder.size()));
	}
	
	/****************************************************************************************************************/
	
	//transaction 4 - output all the names of persons that are directly or indirectly liked by given pid
	private static void transaction4(Statement stat, Scanner scan) throws SQLException
	{
		try 
		{
			//4 103
			System.out.println("\nTransaction 4: ");
			ArrayList<Integer> IDHolder = new ArrayList<Integer>();  
			HashSet<String> nameHolder = new HashSet<String>();
			
			getPID(IDHolder, pid); 

			
			for(int pidPerson : IDHolder)
			{
				ResultSet resultSet = stat.executeQuery("SELECT name "
													  + "FROM person "
													  + "WHERE pid = " + pidPerson);
				
				String holdname;
				while(resultSet.next( )) 
				{
					holdname = resultSet.getString("name");  
					nameHolder.add(holdname);	
				}
			} 
			
			int counter = 1;
			for(String printName : nameHolder)
			{
				System.out.println(counter++ + ". " + printName + " ");
			}
			
			counter--;
			if(counter == 0)
			{
				System.out.println(pid + " does not like any one directly or indirectly"); 
			}
		}
		
		catch (Exception e) 
		{
			System.out.println(e);
			System.out.println("Error in the transaction4()");  
		}
	}
	 
	/****************************************************************************************************************/

	//helper function for transaction 4 and 5
	private static void getPID(ArrayList<Integer> IDHolder, int personID) throws Exception
	{
		Connection connection = getConnection( );
		PreparedStatement ps = connection.prepareStatement("SELECT l.mid as mid " 
                + "FROM person AS k, likes As l "
                + "WHERE k.pid = l.mid and "
                + "l.pid = " + personID);
	
		ResultSet resultSet =  ps.executeQuery();  
		
	    HashSet<Integer> ID = new HashSet<Integer>( );  
	    
		while(resultSet.next( ))  
		{ 
			int i = resultSet.getInt("mid"); 
			ID.add(i);
			if(ID.isEmpty()) return;  
			getPID(IDHolder, i);
		}  
		
		IDHolder.addAll(ID);      
	}  
		 
	/****************************************************************************************************************/
	
	//transaction 3 - output the average age of all persons
	private static void transaction3(Statement stat, Scanner scan) throws SQLException
	{
		//3
		ResultSet resultSet =  stat.executeQuery("SELECT AVG(age) as AverageAge FROM person");
		Float averageAge = (float) 0.0;
		while(resultSet.next())
		{
			averageAge = resultSet.getFloat("AverageAge");
		}
		System.out.println("\nTransaction 3: Average age = "+ Math.round(averageAge)); //rounds to the nearest int
	}
	
	/****************************************************************************************************************/
	
	//transaction 2 - enteres a new person in tables
	private static void transaction2(Statement stat, Scanner scan) throws SQLException
	{
		try 
		{
			//2 50 Mary 10
			stat.execute("Insert into person VALUES (" + pid + ", '" + name + "' , " + age + ")");
			
			for(;scan.hasNextInt();) 
			{
				mid = scan.nextInt();
				ResultSet resultSet = stat.executeQuery("Select pid "
													  + "FROM person "
													  + "WHERE pid = " + mid);
				if(resultSet.next())
				{
					stat.execute("INSERT INTO likes VALUES(" + pid + ", " + mid + ")");
				}
				else 
				{
					System.out.println("Error - person " + mid +" doesn't exist yet");
				}
			}
			System.out.println("Transaction 2: Successfully entered the person: " + pid); 
		} 
		
		catch (Exception e) 
		{
			System.out.println("Error entering the person to tables");
		}		
	}
	
	/****************************************************************************************************************/
	
	
	//transaction 1 - deletes the existing tuple from tables
	private static void transaction1(Statement stat, Scanner scan) throws SQLException 
	{
		try 
		{
			//1 100
			ResultSet resultSet = stat.executeQuery("SELECT pid "
					                               + "FROM person "
					                               + "WHERE pid =" + pid);
			if(resultSet.next())
			{
				stat.execute("DELETE FROM person WHERE pid = " + pid);
				stat.execute("DELETE FROM likes WHERE pid = " + pid + " OR mid = " + pid);
				System.out.println("Transaction 1: Successfully deleted the person: " + pid);
				return;
			}
			
			System.out.println("Transaction 1: Error - person " +pid+" doesn't exist");  
		} 
		catch (Exception e) 
		{
			System.out.println("Error deleting the person from tables");
		}
	}
	/****************************************************************************************************************/
}