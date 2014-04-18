/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eproctor.commons;
import com.mongodb.DBCollection;
import static eproctor.commons.LoginFormController.getMD5;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author YUAN0_000
 */
public class DatabaseInterfaceTest {
    
        
       
    public DatabaseInterfaceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws UnknownHostException {
        DBCollection user, record, course, session, student, proctor, message;
        DatabaseInterface.connectEProctorServer();
       DatabaseInterface.connectSchoolServer();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isUser method, of class DatabaseInterface.
     * @throws java.security.NoSuchAlgorithmException
     */
    @Test
    public void testIsUser() throws NoSuchAlgorithmException{
        boolean expResult = true;
        boolean result = DatabaseInterface.isUser("Student", "gong0025", getMD5("iamadmin", true));;
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsUser1() throws NoSuchAlgorithmException{
        boolean expResult = false;
        boolean result = DatabaseInterface.isUser("Student", "gong0025", getMD5("somethingelse", true));;
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsUser2() throws NoSuchAlgorithmException{
        boolean expResult = false;
        boolean result = DatabaseInterface.isUser("Proctor", "gong0025", getMD5("iamadmin", true));;
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsUser3() throws NoSuchAlgorithmException{
        boolean expResult = false;
        boolean result = DatabaseInterface.isUser("Student", "gong00255", getMD5("iamadmin", true));;
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsUser4() throws NoSuchAlgorithmException{
        boolean expResult = true;
        boolean result = DatabaseInterface.isUser("Proctor", "chen0818", getMD5("iamadmin", true));;
        assertEquals(expResult, result);
    }
    
     @Test
    public void testIsUser5() throws NoSuchAlgorithmException{
        boolean expResult = false;
        boolean result = DatabaseInterface.isUser("Proctor", "chen08188", getMD5("iamadmin", true));;
        assertEquals(expResult, result);
    }
    
     @Test
    public void testIsUser6() throws NoSuchAlgorithmException{
        boolean expResult = false;
        boolean result = DatabaseInterface.isUser("Proctor", "chen0818", getMD5("somethingelse", true));;
        assertEquals(expResult, result);
    }
    
     @Test
    public void testIsUser7() throws NoSuchAlgorithmException{
        boolean expResult = false;
        boolean result = DatabaseInterface.isUser("Student", "chen0818", getMD5("iamadmin", true));;
        assertEquals(expResult, result);
    }
    
    

    /**
     * Test of addBookingStudent method, of class DatabaseInterface.
     */
//    @Test
//    public void testAddBookingStudent() {
//        System.out.println("addBookingStudent");
//        List<DatabaseInterface.SessionRow> sessions=null;
//        DatabaseInterface.CourseRow courseRow = new DatabaseInterface.CourseRow("fff", "code", "name", sessions);
//        DatabaseInterface.SessionRow sessionRow = new DatabaseInterface.SessionRow("id", "name", ISODate("2014-04-06T01:00:00Z"), ISODate("2014-04-06T03:00:40Z"), "location");
//        DatabaseInterface.RecordRowStudent expResult = null;
//        DatabaseInterface.RecordRowStudent result = DatabaseInterface.addBookingStudent(courseRow, sessionRow);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of deleteBookingStudent method, of class DatabaseInterface.
//     */
//    @Test
//    public void testDeleteBookingStudent() {
//        System.out.println("deleteBookingStudent");
//        String id = "";
//        DatabaseInterface.deleteBookingStudent(id);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
