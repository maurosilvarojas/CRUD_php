package rms.dbsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class AddressbookEntry
{
    String id;
    String firstname;
    String lastname;
    String address;
    String telephone;

    public AddressbookEntry (String i, String fn, String ln, String ad, String tl)
    {
        id = i;
        firstname = fn;
        lastname = ln;
        address = ad;
        telephone = tl;
    }
}

class AddressbookList
{
    static AddressbookEntry[] addressbookEntry;
    static int count = 0;
    static int newsInFocus = -1;

    public static void init (int n)
    {
        addressbookEntry = null; // OLD RECORDS IS NOW GARBAGE
        addressbookEntry = new AddressbookEntry[n];
        count = n;
    }
}

public class MainActivity extends AppCompatActivity {

    AddressbookEntry ab;
    int n = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button b1 = (Button)findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener()
                              {
                                  public void onClick (View view)
                                  {
                                      if (n > 0)
                                      {
                                          n --;
                                          loadRecord ();
                                      }
                                  }
                              }
        );

        final Button b2 = (Button)findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener()
                              {
                                  public void onClick (View view)
                                  {
                                      new Thread ()
                                      {
                                          @Override
                                          public void run ()
                                          {
                                              final String s = getContent ("http://www.eng.northampton.ac.uk/~16442932/dbLoadDB.php?");
                                              //final TextView t = (TextView)findViewById(R.id.textView1);

                                              runOnUiThread (new Thread(new Runnable()
                                              {
                                                  public void run()
                                                  {
                                                      parseContent (s);
                                                      n = 0;
                                                      loadRecord ();
                                                  }
                                              }));
                                          }
                                      }.start ();
                                  }
                              }
        );

        final Button b3 = (Button)findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener()
                              {
                                  public void onClick (View view)
                                  {
                                      if (n < AddressbookList.count - 1)
                                      {
                                          n ++;
                                          loadRecord ();
                                      }
                                  }
                              }
        );

        final Button b4 = (Button)findViewById(R.id.button4);
        b4.setOnClickListener(new View.OnClickListener()
                              {
                                  public void onClick (View view)
                                  {
                                      addRecord ();
                                  }
                              }
        );

        final Button b5 = (Button)findViewById(R.id.button5);
        b5.setOnClickListener(new View.OnClickListener()
                              {
                                  public void onClick (View view)
                                  {
                                      updateRecord ();
                                  }
                              }
        );

        final Button b6 = (Button)findViewById(R.id.button6);
        b6.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick (View view)
                    {
                        deleteRecord ();
                    }
                }
        );
    }

    private void deleteRecord() {
        TextView t1 = (TextView)findViewById(R.id.textView01);
        final String i = t1.getText ().toString ();

        EditText e1 = (EditText)findViewById(R.id.editText1);
        final String f = e1.getText ().toString ();

        EditText e2 = (EditText)findViewById(R.id.editText2);
        final String l = e2.getText ().toString ();

        EditText e3 = (EditText)findViewById(R.id.editText3);
        final String a = e3.getText ().toString ();

        EditText e4 = (EditText)findViewById(R.id.editText4);
        final String t  = e4.getText ().toString ();

        new Thread ()
        {
            @Override
            public void run ()
            {
                final String s = getContent ("http://www.eng.northampton.ac.uk/~16442932/dbDeleteDB.php?id=" + i + "&firstname=" + f + "&lastname=" + l + "&address=" + a + "&telephone=" + t);
                //final TextView t = (TextView)findViewById(R.id.textView1);

                runOnUiThread (new Thread(new Runnable()
                {
                    public void run()
                    {
                        parseContent (s);
                        //loadRecord ();
                    }
                }));
            }
        }.start ();
    }



    public String getContent (String theURL)
    {
        String s = "";

        try
        {
            URL url  = new URL (theURL);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            int l = httpConnection.getContentLength();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                InputStream in = httpConnection.getInputStream();
                InputStreamReader inn= new InputStreamReader(in);
                BufferedReader bin= new BufferedReader(inn);

                do
                {
                    s = s + bin.readLine() + "\n";
                }
                while (s.length() < l);

                in.close();
            }
            else
            {
            }
        }
        catch (MalformedURLException e) {}
        catch (IOException e) {}
        finally {}

        return s;
    }

    public static void parseContent (String s)
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();;
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse (new ByteArrayInputStream(s.getBytes()));
            Element docEle = dom.getDocumentElement ();

            NodeList nl = docEle.getElementsByTagName("record");

            if (AddressbookList.count == -1)
            {
                AddressbookList.init (nl.getLength());
            }
            else // Unlikely!
            {
                AddressbookList.count = -1;
                AddressbookList.addressbookEntry = null;
                AddressbookList.init (nl.getLength());
            }

            for (int i = 0; i < nl.getLength(); i ++)
            {
                Element entry = (Element)nl.item(i);

                AddressbookList.addressbookEntry[i] = new AddressbookEntry ("", "", "", "", "");

                Element id = (Element)entry.getElementsByTagName("id").item(0);
                Element firstname = (Element)entry.getElementsByTagName("firstname").item(0);
                Element lastname = (Element)entry.getElementsByTagName("lastname").item(0);
                Element address = (Element)entry.getElementsByTagName("address").item(0);
                Element telephone = (Element)entry.getElementsByTagName("telephone").item(0);

                AddressbookList.addressbookEntry[i].id = id.getFirstChild().getNodeValue();
                AddressbookList.addressbookEntry[i].firstname = firstname.getFirstChild().getNodeValue();
                AddressbookList.addressbookEntry[i].lastname = lastname.getFirstChild().getNodeValue();
                AddressbookList.addressbookEntry[i].address = address.getFirstChild().getNodeValue();
                AddressbookList.addressbookEntry[i].telephone = telephone.getFirstChild().getNodeValue();
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        finally
        {
        }
    }

    public void loadRecord ()
    {
        TextView t1 = (TextView)findViewById(R.id.textView01);
        t1.setText (AddressbookList.addressbookEntry[n].id);

        EditText e1 = (EditText)findViewById(R.id.editText1);
        e1.setText (AddressbookList.addressbookEntry[n].firstname);

        EditText e2 = (EditText)findViewById(R.id.editText2);
        e2.setText (AddressbookList.addressbookEntry[n].lastname);

        EditText e3 = (EditText)findViewById(R.id.editText3);
        e3.setText (AddressbookList.addressbookEntry[n].address);

        EditText e4 = (EditText)findViewById(R.id.editText4);
        e4.setText (AddressbookList.addressbookEntry[n].telephone);
    }

    public void addRecord ()
    {
        EditText e1 = (EditText)findViewById(R.id.editText1);
        final String f = e1.getText ().toString ();

        EditText e2 = (EditText)findViewById(R.id.editText2);
        final String l = e2.getText ().toString ();

        EditText e3 = (EditText)findViewById(R.id.editText3);
        final String a = e3.getText ().toString ();

        EditText e4 = (EditText)findViewById(R.id.editText4);
        final String t  = e4.getText ().toString ();

        new Thread ()
        {
            @Override
            public void run ()
            {
                final String s = getContent ("http://www.eng.northampton.ac.uk/~16442932/dbAddDB.php?firstname=" + f + "&lastname=" + l + "&address=" + a + "&telephone=" + t);
                //final TextView t = (TextView)findViewById(R.id.textView1);

                runOnUiThread (new Thread(new Runnable()
                {
                    public void run()
                    {
                        parseContent (s);
                        n ++;
                        loadRecord ();
                    }
                }));
            }
        }.start ();
    }

    public void updateRecord ()
    {
        TextView t1 = (TextView)findViewById(R.id.textView01);
        final String i = t1.getText ().toString ();

        EditText e1 = (EditText)findViewById(R.id.editText1);
        final String f = e1.getText ().toString ();

        EditText e2 = (EditText)findViewById(R.id.editText2);
        final String l = e2.getText ().toString ();

        EditText e3 = (EditText)findViewById(R.id.editText3);
        final String a = e3.getText ().toString ();

        EditText e4 = (EditText)findViewById(R.id.editText4);
        final String t  = e4.getText ().toString ();

        new Thread ()
        {
            @Override
            public void run ()
            {
                final String s = getContent ("http://www.eng.northampton.ac.uk/~16442932/dbUpdateDB.php?id=" + i + "&firstname=" + f + "&lastname=" + l + "&address=" + a + "&telephone=" + t);
                //final TextView t = (TextView)findViewById(R.id.textView1);

                runOnUiThread (new Thread(new Runnable()
                {
                    public void run()
                    {
                        parseContent (s);
                        loadRecord ();
                    }
                }));
            }
        }.start ();
    }


}

