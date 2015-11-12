package br.com.edu.ifg.cracha.geral.controle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import br.com.edu.ifg.cracha.geral.modelo.ModeloCracha;
import br.com.edu.ifg.cracha.geral.modelo.ModeloSQL;
import br.com.edu.ifg.cracha.geral.visao.VisaoCracha;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class ListenersControle {

    //Variaveis
    //--Boolean
    private boolean filled = false;
    //--Modelo
    private ModeloCracha mC = null;
    private ModeloSQL mSQL = null;

    //--Visao
    private VisaoCracha vC = null;

    //--Point
    private static Point point = new Point();

    //--Connection e Statement
    private Connection con = null;
    private Statement stm = null;    
    private Statement stm1 = null;
    private Statement stm2 = null;

    //--ResultSet
    private ResultSet rs = null;

    //--Vector
    private Vector<ModeloCracha> vMC = null;

    //--iReport no geral
    private JasperPrint jpPrint = null;
    private JasperViewer jV = null;
    private CrachaJRDataSource relatResult = null;

    //Métodos
    //--Construtor
    public ListenersControle(final VisaoCracha vC) {
        //Necessário para realizar a conexão com  BD
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Entrei aqui");
        }

        //Instancias
        this.vC = vC;
        vMC = new Vector<>();

        //Listeners
        //--Botao Sair
        vC.getbSair().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        //--Mover Frame
        vC.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                point.x = e.getX();
                point.y = e.getY();
            }
        });
        vC.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point p = vC.getLocation();
                vC.setLocation(p.x + e.getX() - point.x, p.y + e.getY() - point.y);
            }
        });
        //--Botao Conecta do Banco de Dados
        vC.getbConecta().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (vC.getbConecta().getText().equalsIgnoreCase("Conectar")) {
                    if (checkDBFields()) {
                        mSQL = new ModeloSQL(vC.getTfURLDB().getText(), vC.getTfUser().getText(), vC.getPfPassword().getText());
                        try {
                            con = DriverManager.getConnection(mSQL.getURL(), mSQL.getUsername(), mSQL.getPassword());
                            stm = con.createStatement();

                            vC.getbConecta().setText("Resetar");
                            vC.getTfURLDB().setEnabled(false);
                            vC.getTfUser().setEnabled(false);
                            vC.getPfPassword().setEnabled(false);

                            JOptionPane.showMessageDialog(null, "Conectado com Sucesso!", "Conectar", JOptionPane.INFORMATION_MESSAGE);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Houve um erro com a conexão. Verifique os dados digitados e tente novamente!", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    try {
                        con.close();

                        vC.getbConecta().setText("Conectar");
                        vC.getTfURLDB().setEnabled(true);
                        vC.getTfUser().setEnabled(true);
                        vC.getPfPassword().setEnabled(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        //--Botão Imprimir
        vC.getbPrint().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkReportFields()) {
                    setEnabledFalse();
                    if (!filled) {
                        try {
                            mSQL.setSQL(vC.getTaSQL().getText());

                            rs = stm.executeQuery(mSQL.getSQL());
                            stm1 = con.createStatement();
                            stm2 = con.createStatement();
                                    
                            String URLQR = vC.getTfURLQR().getText();

                            while (rs.next()) {
                                String cpf = rs.getString("cpf_partic");
                                String nome = rs.getString("nome_partic");
                                
                                geraQR(nome, cpf, URLQR);
                                System.out.println("Gerei QR Code " + nome);
                                stm1.execute("UPDATE participantes SET url='" + URLQR + "\\" + nome + ".png" + "' WHERE cpf_partic='" + cpf + "';");
                                
                                String nomeS = rs.getString("nome_simplif");
                                stm2.execute("UPDATE participantes SET nome_simplif='" + nomeSimplif(nome) + "' WHERE cpf_partic='" + cpf + "';");
                                System.out.println("Atualizei url no cpf " + cpf);

                                ModeloCracha mC = new ModeloCracha(nome, cpf, "null", URLQR + "\\" + nome + ".png", nomeS);
                                vMC.add(mC);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(ListenersControle.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        relatResult = new CrachaJRDataSource(vMC);
                        filled = true;
                    }
                    try {
                    	
                        jpPrint = JasperFillManager.fillReport("CrachaFinalSecitec.jasper", new HashMap(), relatResult);

                        jV = new JasperViewer(jpPrint, false);
                        jV.setVisible(true);
                    } catch (JRException ex) {
                        JOptionPane.showMessageDialog(null, "Verifique a URL do relatório.", "Erro", JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(ListenersControle.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    setEnableTrue();
                }
            }
        });

    }//Fim do construtor
    
    //Verifica o nome simplificado para imprimir o crachá
    private String nomeSimplif(String nome) {  
        String retorno;
        
        System.out.println("Simplificando: " + nome);
        
        if (nome.split(" ")[0] == null)
            return nome;
        
        try{
            if(nome.split(" ")[1].length() <= 3)
                retorno = nome.split(" ")[0] + " " + nome.split(" ")[1] + " " +nome.split(" ")[2];
            else
                retorno = nome.split(" ")[0] + " " + nome.split(" ")[1];
        } catch(ArrayIndexOutOfBoundsException aIOOBE) {
            retorno = nome;
        }
        
        return retorno.toUpperCase();
    }

    //--Habilita os campos do relatorio
    private void setEnableTrue() {
        vC.getTaSQL().setEnabled(true);
        vC.getTfURLQR().setEnabled(true);
        vC.getTfURLReport().setEnabled(true);
        vC.getbSair().setEnabled(true);
        vC.getbPrint().setEnabled(true);
        vC.getbConecta().setEnabled(true);
    }
    //--Desabilita os campos do relatorio
    private void setEnabledFalse() {        
        vC.getTaSQL().setEnabled(false);
        vC.getTfURLQR().setEnabled(false);
        vC.getTfURLReport().setEnabled(false);
        vC.getbSair().setEnabled(false);
        vC.getbPrint().setEnabled(false);
        vC.getbConecta().setEnabled(false);
    }
    
    //--Verifica se os campos de Banco de Dados foram preenchidos
    public boolean checkDBFields() {
        if (vC.getTfURLDB().getText().equalsIgnoreCase("")
                && vC.getTfUser().getText().equalsIgnoreCase("")
                && vC.getPfPassword().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do banco de dados, o usuário e a senha.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if ((!vC.getTfURLDB().getText().equalsIgnoreCase(""))
                && vC.getTfUser().getText().equalsIgnoreCase("")
                && vC.getPfPassword().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha o usuário e a senha.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (vC.getTfURLDB().getText().equalsIgnoreCase("")
                && (!vC.getTfUser().getText().equalsIgnoreCase(""))
                && vC.getPfPassword().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do banco de dados e a senha.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (vC.getTfURLDB().getText().equalsIgnoreCase("")
                && vC.getTfUser().getText().equalsIgnoreCase("")
                && (!vC.getPfPassword().getText().equalsIgnoreCase(""))) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do banco de dados e o usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if ((!vC.getTfURLDB().getText().equalsIgnoreCase(""))
                && (!vC.getTfUser().getText().equalsIgnoreCase(""))
                && vC.getPfPassword().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha a senha.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if ((!vC.getTfURLDB().getText().equalsIgnoreCase(""))
                && vC.getTfUser().getText().equalsIgnoreCase("")
                && (!vC.getPfPassword().getText().equalsIgnoreCase(""))) {
            JOptionPane.showMessageDialog(null, "Preencha o usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (vC.getTfURLDB().getText().equalsIgnoreCase("")
                && (!vC.getTfUser().getText().equalsIgnoreCase(""))
                && (!vC.getPfPassword().getText().equalsIgnoreCase(""))) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            return true;
        }
    }

    //--Verifica se os campos necessários para gerar o relatorio foram preenchidos
    private boolean checkReportFields() {
        if (vC.getTfURLReport().getText().equalsIgnoreCase("")
                && vC.getTfURLQR().getText().equalsIgnoreCase("")
                && vC.getTaSQL().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do relatório, a URL para salvar os QR Codes e o código SQL.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if ((!vC.getTfURLReport().getText().equalsIgnoreCase(""))
                && vC.getTfURLQR().getText().equalsIgnoreCase("")
                && vC.getTaSQL().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha a URL para salvar os QR Codes e o código SQL.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (vC.getTfURLReport().getText().equalsIgnoreCase("")
                && (!vC.getTfURLQR().getText().equalsIgnoreCase(""))
                && vC.getTaSQL().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do relatório e o código SQL.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (vC.getTfURLReport().getText().equalsIgnoreCase("")
                && vC.getTfURLQR().getText().equalsIgnoreCase("")
                && (!vC.getTaSQL().getText().equalsIgnoreCase(""))) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do relatório e a URL para salvar os QR Codes.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if ((!vC.getTfURLReport().getText().equalsIgnoreCase(""))
                && (!vC.getTfURLQR().getText().equalsIgnoreCase(""))
                && vC.getTaSQL().getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Preencha o código SQL.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if ((!vC.getTfURLReport().getText().equalsIgnoreCase(""))
                && vC.getTfURLQR().getText().equalsIgnoreCase("")
                && (!vC.getTaSQL().getText().equalsIgnoreCase(""))) {
            JOptionPane.showMessageDialog(null, "Preencha a URL para salvar os QR Codes.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (vC.getTfURLReport().getText().equalsIgnoreCase("")
                && (!vC.getTfURLQR().getText().equalsIgnoreCase(""))
                && (!vC.getTaSQL().getText().equalsIgnoreCase(""))) {
            JOptionPane.showMessageDialog(null, "Preencha a URL do relatório.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            if (!vC.getbConecta().getText().equalsIgnoreCase("Resetar")) {
                JOptionPane.showMessageDialog(null, "Conecte-se com um banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                return true;
            }
        }
    }

    //--Gerar QRCode
    public void geraQR(String nomeAluno, String cpf, String URLQRCode) {
        String myCodeText = "CPF:" + cpf + ":" + nomeAluno;
        String filePath = URLQRCode + "\\" + nomeAluno + ".png";
        int size = 85;
        String fileType = "png";
        File myFile = new File(filePath);
        
        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            
            Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, size, size, hints);
            int CrunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D g = (Graphics2D) image.getGraphics();
            
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            
            g.setColor(Color.BLACK);

            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        g.fillRect(i, j, 1, 1);
                    }
                }
            }
            ImageIO.write(image, fileType, myFile);
        } catch (WriterException wE) {
            wE.printStackTrace();
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }
}
