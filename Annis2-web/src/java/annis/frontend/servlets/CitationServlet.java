package annis.frontend.servlets;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CitationServlet extends HttpServlet {
	private static final long serialVersionUID = -4188886565776492022L;
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();

		String parameters = request.getRequestURI().replaceAll(".*?/Cite(/)?", "");
		
		if(!"".equals(parameters)) {
			//set the cookie and redirect to index.jsp
			Cookie citationCookie = new Cookie("citation", parameters);
			citationCookie.setPath("/");
			response.addCookie(citationCookie);
		}
		response.sendRedirect(getServletContext().getContextPath());
	}
}
