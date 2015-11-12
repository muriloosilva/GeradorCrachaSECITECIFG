package br.com.edu.ifg.cracha.geral.main;

import java.io.FileNotFoundException;
import java.io.IOException;

import br.com.edu.ifg.cracha.geral.controle.ListenersControle;
import br.com.edu.ifg.cracha.geral.visao.VisaoCracha;

public class PrincipalCracha {

    public static void main(String[] args) throws FileNotFoundException, IOException {
    	
    	//LAF
       /* try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VisaoCracha.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VisaoCracha.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VisaoCracha.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VisaoCracha.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/
        
        //Variáveis
        //--Visão
        VisaoCracha vC = new VisaoCracha();
        //Controle
        ListenersControle lC = new ListenersControle(vC);
        
        vC.setVisible(true);
    }
}
