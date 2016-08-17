import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class Toggle extends HttpServlet {
	
	static Map<String, String> status = new TreeMap<String, String>();
	static Map<String, Long> timeCheckedOut = new HashMap<String, Long>();
	static String[] statusKeys= new String[10];
	
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		//System.out.println(status.entrySet().size() + " known entries in status");
		
		String newEntity = request.getParameter("newEntity");
		if (newEntity != null && !newEntity.trim().equals("")) {
			status.put(newEntity, "in");
			timeCheckedOut.put(newEntity, 0L);
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.close();
			return;
		}
		
		String entityToRemove = request.getParameter("entityToRemove");
		if (entityToRemove != null) {
			status.remove(entityToRemove);
			timeCheckedOut.remove(entityToRemove);
		}
		
		String sourceName = request.getParameter("sourceName");
		String currentStatus = request.getParameter("currentStatus");
		String newStatus = (currentStatus.equals("in")) ? "out" : "in" ;
			
		if (sourceName != null && currentStatus != null) {
			if (newStatus.equals("out")) timeCheckedOut.put(sourceName, System.currentTimeMillis() - 1);
			else timeCheckedOut.put(sourceName, 0L);
			status.put(sourceName, newStatus);
			sendAll(response);
		}
		
				
		SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mm:ss aa");
		String date = sdf.format(new Date()); 
		String time = date;
		
		String user = "UFO";
		Cookie[] cookies = request.getCookies();
		 if (cookies != null){
			 for (Cookie c : cookies) {
				if (c.getName().equals("name")){
					user = c.getValue();
					break;
				}
			 }
		 }
		
		String newStatusCapitalized = newStatus.substring(0, 1).toUpperCase() + newStatus.substring(1);
		String uAction = sourceName + " <div class=\"" + newStatus + "\">" + newStatusCapitalized + "</div>";
		
		StringBuilder sb = new StringBuilder();       
		sb.append("<div id=\"event\">");			
		sb.append("<div class=\"action\">");
		sb.append(uAction);
		sb.append("</div>");
		sb.append("<div class=\"user\">");
		sb.append(user);
		sb.append("</div>");
		sb.append("<div class=\"time\">");
		sb.append(time);
		sb.append("</div>");
		sb.append("</div>\n");
		appendLog(sb.toString());
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sendAll(response);
    }
	
	public void sendAll(HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		
		out.print("[");
		
		Iterator statusIterator = status.entrySet().iterator();
		Iterator timeOutIterator = timeCheckedOut.entrySet().iterator();
		while (statusIterator.hasNext()) {
			Map.Entry statusPairs = (Map.Entry)statusIterator.next();
			Map.Entry timeOutPairs = (Map.Entry)timeOutIterator.next();
			out.print("{\"sourceName\":\"" + statusPairs.getKey() +  "\", \"newStatus\":\"" + statusPairs.getValue()
			+ "\", \"timeCheckedOut\":\"" + ((timeCheckedOut.get(statusPairs.getKey()) == 0) ? 0 : (System.currentTimeMillis() - timeCheckedOut.get(statusPairs.getKey()))) + "\"}");

			if (statusIterator.hasNext()) out.print(",");
		}
		
		out.print("]");
	}
	
	public static synchronized void appendLog(String s) {	
		 try {
            Scanner in = new Scanner(new File("../webapps/CatTracker/data/log.html"));
            StringBuilder sb = new StringBuilder();
            int line = 0;
            while (in.hasNextLine()){
                line++;
                sb.append(in.nextLine() + "\n");
                if (line == 7) sb.append(s + "\n");
            }
            in.close();
            PrintWriter out = new PrintWriter(new File("../webapps/CatTracker/data/log.html"));
            out.print(sb);
            out.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        }
        
    }
}
