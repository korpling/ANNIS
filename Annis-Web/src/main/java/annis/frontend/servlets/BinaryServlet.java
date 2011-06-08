/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.frontend.servlets;


import annis.exceptions.AnnisBinaryNotFoundException;
import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisBinary;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import java.io.IOException;

public class BinaryServlet extends HttpServlet {

	private static final long serialVersionUID = -8182635617256833563L;

	@SuppressWarnings("unchecked")
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		String binaryParameter = request.getRequestURI().replaceFirst("/.+/.+/", "");
		String[] split = binaryParameter.split("_");
		
		if(split.length < 1)
			throw new RuntimeException("Parameter 'id' must no be null.");
		
		String binaryId = split[0];
		
    int foundMp3 = binaryId.lastIndexOf(".mp3");
    if(foundMp3 > -1)
    {
      binaryId = binaryId.substring(0, foundMp3);
    }
    
		ServletOutputStream out = response.getOutputStream();

		//TODO: maybe we should implement Caching
    
		try {
			AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
			AnnisBinary binary = service.getBinary(Long.parseLong(binaryId));

      String mimeType = binary.getMimeType();



			response.setContentType(binary.getMimeType());
			response.setContentLength(binary.getBytes().length);
			out.write(binary.getBytes());
			out.flush();
		} catch (AnnisServiceFactoryException e) {
			throw new RuntimeException(e.getMessage());
		} catch (NumberFormatException e) {
			throw new RuntimeException("Parameter 'id' must be numeric");
		} catch (AnnisBinaryNotFoundException e) {
			throw new RuntimeException("Binary with id '" + binaryId + "' does not exist.");
		}
	}
}
