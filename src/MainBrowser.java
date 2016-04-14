/**
 * Created by System64 on 12.04.2016.
 */

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static javafx.concurrent.Worker.State.FAILED;

 class SimpleSwingBrowser extends JFrame {
     private ArrayList pageList = new ArrayList();
    private final JFXPanel jfxPanel = new JFXPanel();
    private WebEngine engine;
     private String tmpurl;

    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();

    private final JButton btnGo = new JButton("Go");
    private final JButton btnBack = new JButton("Back");
    private final JTextField txtURL = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();

    public SimpleSwingBrowser() {
        super();
        initComponents();
    }


    private void initComponents() {
        createScene();

        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadURL(txtURL.getText());
            }
        };

        btnGo.addActionListener(al);
        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionBack();
            }
        });

        txtURL.addActionListener(al);

        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);
        topBar.add(btnBack, BorderLayout.WEST);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);

        getContentPane().add(panel);

        setPreferredSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

    }

    private void createScene() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                WebView view = new WebView();
                engine = view.getEngine();

                engine.titleProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                SimpleSwingBrowser.this.setTitle(newValue);
                            }
                        });
                    }
                });

                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lblStatus.setText(event.getData());
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                txtURL.setText(newValue);
                            }
                        });
                    }
                });

                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                engine.getLoadWorker()
                        .exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {

                            public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                if (engine.getLoadWorker().getState() == FAILED) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override public void run() {
                                            JOptionPane.showMessageDialog(
                                                    panel,
                                                    (value != null) ?
                                                            engine.getLocation() + "\n" + value.getMessage() :
                                                            engine.getLocation() + "\nUnexpected error.",
                                                    "Loading error...",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                }
                            }
                        });

                jfxPanel.setScene(new Scene(view));
            }
        });
        tmpurl="http://www.oracle.com/index.html";
    }

    public void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tmp = toURL(url);

                if (tmp == null) {
                    tmp = toURL("http://" + url);
                }

                engine.load(tmp);
                URL domain = null;
                try {
                    domain = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
               // showPage(domain, true);
                pageList.add(domain);




            }
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

     private void actionBack() {
         String currentUrl = txtURL.getText();

         final int pageIndex = pageList.size()-2;
                 //pageList.indexOf(currentUrl);

       /*  try {
             showPage(
                     new URL((String) pageList.get(pageIndex - 1)), false);
         } catch (Exception e) {e.printStackTrace();}*/
         Platform.runLater(new Runnable() {
             @Override
             public void run() {
                 String tmp = toURL(tmpurl);

                 if (tmp == null) {
                     tmp = toURL("http://" + tmpurl);
                 }
                 engine.load(pageList.get(pageIndex).toString());
             }
         });
     }

     private void showPage(URL pageUrl, boolean addToList) {
         // Show hour glass cursor while crawling is under way.
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

         try {
             // Get URL of page currently being displayed.

             String currentUrl = tmpurl;

             // Load and display specified page.
             loadURL(pageUrl.toString());

             // Get URL of new page being displayed.
             String newUrl = txtURL.getText();

             // Add page to list if specified.
             if (addToList) {
                 int listSize = pageList.size();
                 if (listSize > 0) {
                     int pageIndex =
                             pageList.indexOf(currentUrl);
                     if (pageIndex < listSize - 1) {
                         for (int i = listSize - 1; i > pageIndex; i--) {
                             pageList.remove(i);
                         }
                     }
                 }
                 pageList.add(newUrl);
             }




         } catch (Exception e) {
             // Show error messsage.

         } finally {
             // Return to default cursor.
             setCursor(Cursor.getDefaultCursor());
         }
     }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                SimpleSwingBrowser browser = new SimpleSwingBrowser();
                browser.setVisible(true);
                browser.loadURL("https://www.youtube.com");

            }
        });
    }
}
