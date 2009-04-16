package annis.frontend.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Servlet that gives returns you metadata for a given token.
 * @author thomas
 */
public class MetaDataServlet extends HttpServlet
{

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {

    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("application/x-json");

    ServletOutputStream out = resp.getOutputStream();

    StringWriter writer = new StringWriter();
    JSONWriter json = new JSONWriter(writer);


    String idAsString = req.getParameter("tokID");
    if(idAsString != null)
    {
      try
      {
        long id = Long.parseLong(idAsString);
        List<SortedMap<String, String>> queryResult = createDummyData(id);
        int size = 0;
        try
        {
          json.object();

          json.key("metadata");
          json.array();

          for(SortedMap<String, String> m : queryResult)
          {
            for(Entry<String, String> e : m.entrySet())
            {
              json.object();
              size++;
              json.key("key");
              json.value(e.getKey());
              json.key("value");
              json.value(e.getValue());

              json.endObject();
            }
          }

          json.endArray();


          json.key("size");
          json.value(size);

          json.endObject();

        }
        catch(JSONException ex)
        {
          Logger.getLogger(MetaDataServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

      }
      catch(NumberFormatException ex)
      {
      }
    }

    out.print(writer.toString());


  }

  private List<SortedMap<String, String>> createDummyData(long id)
  {
    LinkedList<SortedMap<String, String>> result = new LinkedList<SortedMap<String, String>>();

    TreeMap<String, String> map1 = new TreeMap<String, String>();
    map1.put("author", "Thomas Krause");
    map1.put("language", "de-DE");

    TreeMap<String, String> map2 = new TreeMap<String, String>();
    map2.put("organization", "Lehrstuhl für Korpuslinguistik");
    map2.put("type", "Essay");

    result.add(map1);
    result.add(map2);

    return result;
  }
}
