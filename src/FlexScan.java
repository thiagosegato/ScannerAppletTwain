import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

//import netscape.javascript.JSObject;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.sun.jna.Native;

import libs.Biblioteca;

public class FlexScan extends JApplet {
	
	public static Biblioteca biblioteca; 
	
	private static BufferedImage dest;
	private static com.itextpdf.text.Rectangle pageSize;
		
	private ImageArea img;
	private JScrollPane panelImagens;
    private JPanel panelOpcoes;
    private JButton btDigitalizar;
    private JButton btSelecionar;
    private JButton btAnterior;
    private JButton btProxima;
    private JButton btExcluir;
    private JButton btEnviar;
    private JButton btCancel;
    private JLabel labelPagina;   
    private List<String> pages;
    private List<Image> images;
    //private JSObject jsObject;
    private JDialog dialog;
    private JProgressBar progress;
    private int imgNumber;
    private int selected;
    private int ci_empresa;
    private int ci_file;
    private JLabel labelDialog;
    private FTPClient ftp;
    private Thread threadSend;
    public static long fileSize;
   
    public void init(){
       
    	AccessController.doPrivileged(new PrivilegedAction<Void>() {
    		public Void run(){
    			String dllName = "Biblioteca5.dll";
    			File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    	        File tmpFile = new File(tmpDir, dllName);
    			
	            try {
	            	InputStream in = getClass().getResourceAsStream(dllName);
		            OutputStream out = new FileOutputStream(tmpFile);
	            	
    	            byte[] buf = new byte[8192];
    	            int len;
    	            while ((len = in.read(buf)) != -1) {
    	                out.write(buf, 0, len);
    	            }

    	            in.close();
    	            out.close();
    	            System.load(tmpFile.getAbsolutePath());  
    	            
    	            biblioteca = (Biblioteca) Native.loadLibrary("Biblioteca5", Biblioteca.class);
    	            
    	        } catch (Exception e) {
    	            // deal with exception
    	        }
    	        return null;
    		}
		});
    	
    	Image background = null;
    	try {
    		InputStream _back = getClass().getResourceAsStream("background.jpg");
    		background = ImageIO.read(_back); //ImageIO.read(new File("background.jpg"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	
    	img = new ImageArea();
    	img.setImageBackground(background);
		img.setBackground(Color.WHITE);
        panelImagens = new JScrollPane(img);
        panelImagens.setBackground(Color.WHITE);
        panelImagens.setPreferredSize(new Dimension(100, 100));
        panelImagens.addMouseMotionListener(new DragMoverListener(panelImagens.getViewport(), img));
    	
        panelOpcoes = new JPanel();
        panelOpcoes.setLayout(null);
        panelOpcoes.setPreferredSize(new Dimension(200, 100));
       
        JLabel label = new JLabel("Digitalizar");
        label.setBounds(0, 5, 150, 15);
        panelOpcoes.add(label);
        label = new JLabel("Vizualização");
        label.setBounds(0, 140, 150, 15);
        panelOpcoes.add(label);
        label = new JLabel("Operações");
        label.setBounds(0, 300, 150, 15);
        panelOpcoes.add(label);
        label = new JLabel("Finalizar");
        label.setBounds(0, 500, 150, 15);
        panelOpcoes.add(label);
       
        labelPagina = new JLabel();
        labelPagina.setBounds(0, 220, 150, 15);
        panelOpcoes.add(labelPagina);
       
        JSeparator sep = new JSeparator();
        sep.setBounds(0, 23, 190, 10);
        panelOpcoes.add(sep);
        sep = new JSeparator();
        sep.setBounds(0, 158, 190, 10);
        panelOpcoes.add(sep);
        sep = new JSeparator();
        sep.setBounds(0, 319, 190, 10);
        panelOpcoes.add(sep);
        sep = new JSeparator();
        sep.setBounds(0, 518, 190, 10);
        panelOpcoes.add(sep);
       
        ActionListener al;
       
        btDigitalizar = new JButton("Digitalizar");
        btDigitalizar.setBounds(0, 35, 190, 24);
        al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                digitalizar();               
            }
        };
        btDigitalizar.addActionListener(al);
        panelOpcoes.add(btDigitalizar);
        
        btSelecionar = new JButton("Selecionar Scanner");
        btSelecionar.setBounds(0, 67, 190, 24);
        al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                biblioteca.selectSource();               
            }
        };
        btSelecionar.addActionListener(al);
        panelOpcoes.add(btSelecionar);
       
        btAnterior = new JButton("< Anterior");
        btAnterior.setBounds(0, 170, 95, 24);
        btAnterior.setEnabled(false);
        al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                back();
            }
        };
        btAnterior.addActionListener(al);
        panelOpcoes.add(btAnterior);
       
        btProxima = new JButton("Próxima >");
        btProxima.setBounds(95, 170, 95, 24);
        btProxima.setEnabled(false);
        al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                next();
            }
        };
        btProxima.addActionListener(al);
        panelOpcoes.add(btProxima);
       
        btExcluir = new JButton("Excluir");
        btExcluir.setBounds(0, 333, 95, 24);
        btExcluir.setEnabled(false);
        al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        };
        btExcluir.addActionListener(al);
        panelOpcoes.add(btExcluir);
       
        btEnviar = new JButton("Enviar Digitalização");
        btEnviar.setBounds(0, 530, 190, 24);
        al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(JOptionPane.showConfirmDialog(null, "Tem certeza que deseja finalizar?", "FlexScan", JOptionPane.OK_OPTION | JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == 0){
					goPDF();
				}				
			}
		};
		btEnviar.addActionListener(al);
		btEnviar.setEnabled(false);
        panelOpcoes.add(btEnviar);       
       
        setLayout(new BorderLayout());
        add(panelOpcoes, BorderLayout.EAST);
        add(panelImagens);
        
        progress = new JProgressBar(0, 100);
		progress.setValue(0);
        progress.setStringPainted(true);
        progress.setBounds(10, 50, 310, 30);
		labelDialog = new JLabel("Gerando arquivo PDF!");
		labelDialog.setBounds(10, 10, 340, 40);
		
		btCancel = new JButton("Cancelar");
		btCancel.setBounds(115, 85, 100, 25);
		al = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				threadSend.stop();
				dialog.setVisible(false);
				enableButtons();
				btCancel.setVisible(false);
				JOptionPane.showMessageDialog(null, "Transferência abortada!", "FlexScan", JOptionPane.ERROR_MESSAGE);
			}
		};
		btCancel.addActionListener(al);
		btCancel.setVisible(false);
		
		Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
		dialog = new JDialog(w, "Aguarde!");
		dialog.setPreferredSize(new Dimension(350, 150));
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.setLayout(null);
		dialog.setResizable(false);
		dialog.add(labelDialog);
		dialog.add(progress);
		dialog.add(btCancel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		
		pages = new ArrayList<String>(0);  
        images = new ArrayList<Image>(0);
        
        imgNumber = 0;
        selected = 0;
        
        //ci_empresa = 1;
        //ci_file = 33;
        
        //JOptionPane.showMessageDialog(null, "message", "titulo", JOptionPane.INFORMATION_MESSAGE);
        
        try {
			getAppletContext().showDocument(new URL("javascript:init()"));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
       // jsObject = JSObject.getWindow(this);
        //jsObject.eval("alert('Applet Carregado!');");
       // jsObject.call("init", null);
    }
     
    public void disableButtons(){
    	btDigitalizar.setEnabled(false);
    	btEnviar.setEnabled(false);
    	btExcluir.setEnabled(false);
    	btSelecionar.setEnabled(false);
    }
    
    public void enableButtons(){
    	btDigitalizar.setEnabled(true);
    	btEnviar.setEnabled(true);
    	btExcluir.setEnabled(true);
    	btSelecionar.setEnabled(true);
    }
    
    public void setVars(int _ci_empresa, int _ci_file){
    	ci_empresa = _ci_empresa;
    	ci_file = _ci_file;
    }
    
    public void enviarArquivo()
    {		
    	progress.setValue(0);
    	labelDialog.setText("Enviando digitalização...");
    	btCancel.setVisible(true);
		dialog.setVisible(true);
		threadSend = new Thread(new Runnable() {
			public void run() {
				sendFTP();				
			}
		});		
		threadSend.start();	
		//dialog.setVisible(false);
		//jsObject.call("finish", null);
    }    
    
    public void sendFTP(){
		ftp = new FTPClient();
		try{
			ftp.connect("www.quasarsolutions.com.br");
			
			if(FTPReply.isPositiveCompletion(ftp.getReplyCode())){
				ftp.login("quasarso", "CeTcBtQ$aWq1");
				//System.out.println("Conectado!!!");
			}
			else{
				ftp.disconnect();
				//System.out.println("Conexão recusada");
			}			
			
			File tmp = new File(System.getProperty("java.io.tmpdir") + ci_file + ".pdf");
			//System.out.println(System.getProperty("java.io.tmpdir") + ci_file + ".pdf" + "tamanho do arquivo!!!: " + tmp.length());
			fileSize = tmp.length();
			InputStream is = new FileInputStream(tmp);			
			
			ftp.setCopyStreamListener(new CopyStreamListener() {
				public void bytesTransferred(long arg0, int arg1, long arg2) {
					double percent = (arg0 * 100) / fileSize;                       
                    progress.setValue((int) percent);
                    //System.out.println("totalBytesTransferred: " + arg0 + ", bytesTrasferred: " + arg1 + ", streamSize: " + arg2);
				}
				public void bytesTransferred(CopyStreamEvent event) {
					bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
				}
			});
			
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftp.storeFile("/flexscan/files/" + ci_empresa + "/" + ci_file + ".pdf", is);			
			ftp.disconnect();
			
			dialog.setVisible(false);
			enableButtons();
			btCancel.setVisible(false);
			
			//System.out.println("Arquivo enviado!");
			getAppletContext().showDocument(new URL("javascript:finish()"));
			//JOptionPane.showMessageDialog(null, "Arquivo enviado com sucesso!", "FlexScan", JOptionPane.INFORMATION_MESSAGE);
			//jsObject.call("finish", null);
		}
		catch(Exception e){
			System.out.println("Ocorreu um erro");
		}
	}
    
    public void atualizaLabelPagina(){
        if(pages.size() == 0)
            labelPagina.setText("Nenhuma página!");
        else       
            labelPagina.setText("Página " + selected + " de " + pages.size());
    }
   
    public void digitalizar(){
        imgNumber++;
       
        if(biblioteca.aquire("flexscan" + imgNumber))
        {
        	Image temp = null;
			try {
				temp = ImageIO.read(new File(System.getProperty("java.io.tmpdir") + "flexscan" + imgNumber + ".jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}			
        	if(temp != null)
        	{
        		pages.add("flexscan" + imgNumber);
        		images.add(temp);
        		next();
        		btExcluir.setEnabled(true);
        		btEnviar.setEnabled(true);
        	}
        }
    }
   
    public void next()
    {        
        selected++;
        atualizaLabelPagina();
       
        //System.out.println(pages);
        //System.out.println(images);
        
    	img.setImage(images.get(selected - 1));
		panelImagens.getHorizontalScrollBar().setValue(0);
		panelImagens.getVerticalScrollBar().setValue(0);
			
		if(selected == pages.size())
            btProxima.setEnabled(false);
       
        if(selected > 1)
            btAnterior.setEnabled(true);
        else
            btAnterior.setEnabled(false);		
    }
   
    public void back()
    {        
        selected--;
        atualizaLabelPagina();
       
        //System.out.println(pages);
        //System.out.println(images);
       
        img.setImage(images.get(selected - 1));
		panelImagens.getHorizontalScrollBar().setValue(0);
		panelImagens.getVerticalScrollBar().setValue(0);			
		
		if(selected == 1)
			btAnterior.setEnabled(false);
	       
		if(selected != pages.size())
			btProxima.setEnabled(true);
		else
			btProxima.setEnabled(false);        
    }
   
    public void delete()
    {
        pages.remove(selected - 1);
        images.remove(selected - 1);
       
        if(pages.size() == 0){           
            atualizaLabelPagina();
            img.removeImage();
            selected = 0;
            btExcluir.setEnabled(false);
            btEnviar.setEnabled(false);
        }
        else{
            if(selected == 1){
                selected = 0;
                next();
            }
            else{
                back();
            }               
        }
    }
   
    public void deleteImgs(){
    	File f;
    	for(int i=0;i<pages.size();i++){
    		f = new File(System.getProperty("java.io.tmpdir") + pages.get(i) + ".jpg");
    		f.delete();
    	}
    }
    
    public void goPDF()
    {    
    	disableButtons();
    	labelDialog.setText("Gerando arquivo PDF...");
    	dialog.setVisible(true);
    	final com.itextpdf.text.Image[] png = new com.itextpdf.text.Image[pages.size()];
        final BufferedImage[] src = new BufferedImage[pages.size()];
        final double percentPart = Math.ceil(100 / pages.size());
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String fileName = System.getProperty("java.io.tmpdir") + ci_file + ".pdf";
                    Document doc = null;
                    for (int i = 0; i < pages.size(); i++) {
                        
                    	src[i] = (BufferedImage) images.get(i);
                        png[i] = com.itextpdf.text.Image.getInstance(System.getProperty("java.io.tmpdir") + pages.get(i) + ".jpg");
                        png[i].setAlignment(com.itextpdf.text.Image.MIDDLE);                        
                        pageSize = new com.itextpdf.text.Rectangle(png[i].getWidth(), png[i].getHeight());

                        if(i == 0) {
                            doc = new Document(pageSize, 0, 0, 0, 0);
                            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
                            doc.open();
                        } else {
                            doc.setPageSize(pageSize);
                            doc.newPage();
                        }

                        doc.setMargins(0, 0, 0, 0);
                        doc.add(png[i]);
                        //System.out.println(i);
                        
                        progress.setValue((int)((i + 1) * percentPart));
                        
                        if(i == (pages.size() - 1)){
                        	dialog.setVisible(false);                        	
                        }                        
                        try {
                            Thread.sleep(100);                            
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    doc.close();                    
                    //deleteImgs();
                    enviarArquivo();
                    
                } catch (IOException ex) {
                	ex.printStackTrace();
                } catch (DocumentException ex) {
                    ex.printStackTrace();
                }
            }
            
        };
        thread.start();
    }
    
    public static BufferedImage toBufferedImage(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return dest;
    }
	
	/*private static void save(BufferedImage image, String fileName) {
        File file = new File(fileName);
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/
	
	/*public static boolean deleteDir(File f) {
        if(f.exists()) {
            File[] childs = f.listFiles();
            for(int i=0; i<childs.length; i++) {
                if(childs[i].isFile()) {
                    childs[i].delete();
                }
            }
        }
        return f.delete();
    }*/
}